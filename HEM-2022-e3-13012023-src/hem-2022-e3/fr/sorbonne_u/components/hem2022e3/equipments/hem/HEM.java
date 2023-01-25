package fr.sorbonne_u.components.hem2022e3.equipments.hem;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a basic
// household management systems as an example of a cyber-physical system.
//
// This software is governed by the CeCILL-C license under French law and
// abiding by the rules of distribution of free software.  You can use,
// modify and/ or redistribute the software under the terms of the
// CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
// URL "http://www.cecill.info".
//
// As a counterpart to the access to the source code and  rights to copy,
// modify and redistribute granted by the license, users are provided only
// with a limited warranty  and the software's author,  the holder of the
// economic rights,  and the successive licensors  have only  limited
// liability. 
//
// In this respect, the user's attention is drawn to the risks associated
// with loading,  using,  modifying and/or developing or reproducing the
// software by the user in light of its specific status of free software,
// that may mean  that it is complicated to manipulate,  and  that  also
// therefore means  that it is reserved for developers  and  experienced
// professionals having in-depth computer knowledge. Users are therefore
// encouraged to load and test the software's suitability as regards their
// requirements in conditions enabling the security of their systems and/or 
// data to be ensured and,  more generally, to use and operate it in the 
// same conditions as regards security. 
//
// The fact that you are presently reading this means that you have had
// knowledge of the CeCILL-C license and that you accept its terms.

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.tools.aclocks.AcceleratedClock;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServer;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerCI;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerConnector;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerOutboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI;
import fr.sorbonne_u.components.hem2022.interfaces.SuspensionAndPlanningEquipmentControlCI;
import fr.sorbonne_u.components.hem2022.interfaces.SuspensionEquipmentControlCI;
import fr.sorbonne_u.components.hem2022e1.equipments.hem.HeaterConnector;
import fr.sorbonne_u.components.hem2022e1.equipments.hem.SuspensionAndPlanningEquipmentControlOutboundPort;
import fr.sorbonne_u.components.hem2022e1.equipments.hem.SuspensionEquipmentControlOutboundPort;
import fr.sorbonne_u.components.hem2022e1.equipments.meter.ElectricMeterCI;
import fr.sorbonne_u.components.hem2022e1.equipments.meter.ElectricMeterConnector;
import fr.sorbonne_u.components.hem2022e1.equipments.meter.ElectricMeterOutboundPort;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.ThermostatedHeater;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.IndoorGarden;
import fr.sorbonne_u.components.hem2022e3.equipments.meter.ElectricMeter;
import fr.sorbonne_u.exceptions.PreconditionException;
import java.util.concurrent.TimeUnit;

// -----------------------------------------------------------------------------
/**
 * The class <code>HEM</code> implements the basis for a household energy
 * management component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * As is, this component is only a very limited starting point for the actual
 * component. The given code is there only to ease the understanding of the
 * objectives, but most of it must be replaced to get the correct code.
 * Especially, no registration of the components representing the appliances
 * is given.
 * </p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code STANDARD_CONTROL_PERIOD > 0.0}
 * invariant	{@code controlPeriod > 0L}
 * invariant	{@code !isUnderTest || clockURI != null && !clockURI.isEmpty()}
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2021-09-09</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@RequiredInterfaces(required = {StandardEquipmentControlCI.class,
								SuspensionEquipmentControlCI.class,
								SuspensionAndPlanningEquipmentControlCI.class,
								ElectricMeterCI.class,
								ClockServerCI.class})
//-----------------------------------------------------------------------------
public class			HEM
extends		AbstractComponent
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** outbound port connecting to to the electric meter component.		*/
	protected ElectricMeterOutboundPort							meterop;
	/** outbound port connecting to to the thermostated heater component.	*/
	protected SuspensionEquipmentControlOutboundPort			heaterop;
	/** outbound port connecting to to the indoor garden component.			*/
	protected SuspensionAndPlanningEquipmentControlOutboundPort	indoorGardenop;

	/** standard control period in seconds.									*/
	protected static final double		STANDARD_CONTROL_PERIOD = 60.0;

	/** control period in nanoseconds.										*/
	protected long						controlPeriod;
	/** when true, the component executes in test mode.						*/
	protected final boolean				isUnderTest;
	/** outbound port to connect to the centralised clock server.			*/
	protected ClockServerOutboundPort	clockServerOBP;
	/** URI of the clock to be used to synchronise the test scenarios and
	 *  the simulation.														*/
	protected final String				clockURI;
	/** accelerated clock used to implement the simulation scenario.		*/
	protected AcceleratedClock			clock;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a household energy manager component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isUnderTest || clockURI != null && !clockURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param isUnderTest	if true, execute the component in unit testing mode.
	 * @param clockURI		URI of the clock to be used to synchronise the test scenarios and the simulation.
	 */
	protected 			HEM(
		boolean isUnderTest,
		String clockURI
		)
	{
		// 1 standard thread to execute the method execute and 1 schedulable
		// thread that is used to perform the tests
		super(1, 1);

		assert	!isUnderTest || clockURI != null && !clockURI.isEmpty() :
				new PreconditionException(
						"!isUnderTest || "
						+ "clockURI != null && !clockURI.isEmpty()");

		this.isUnderTest = isUnderTest;
		this.clockURI = clockURI;
		
		this.tracer.get().setTitle("Home Energy Manager component");
		this.tracer.get().setRelativePosition(1, 4);
		this.toggleTracing();		
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public synchronized void	start() throws ComponentStartException
	{
		super.start();

		try {
			this.meterop = new ElectricMeterOutboundPort(this);
			this.meterop.publishPort();
			this.doPortConnection(
					this.meterop.getPortURI(),
					ElectricMeter.ELECTRIC_METER_INBOUND_PORT_URI,
					ElectricMeterConnector.class.getCanonicalName());

			this.heaterop = new SuspensionEquipmentControlOutboundPort(this);
			this.heaterop.publishPort();
			this.doPortConnection(
					this.heaterop.getPortURI(),
					ThermostatedHeater.INBOUND_PORT_URI,
					HeaterConnector.class.getCanonicalName());

			this.indoorGardenop =
					new SuspensionAndPlanningEquipmentControlOutboundPort(this);
			this.indoorGardenop.publishPort();
			this.doPortConnection(
					this.indoorGardenop.getPortURI(),
					IndoorGarden.INBOUND_PORT_URI_PREFIX,
					IndoorGardenConnector.class.getCanonicalName());

			if (this.isUnderTest) {
				this.clockServerOBP = new ClockServerOutboundPort(this);
				this.clockServerOBP.publishPort();
				this.doPortConnection(
						this.clockServerOBP.getPortURI(),
						ClockServer.STANDARD_INBOUNDPORT_URI,
						ClockServerConnector.class.getCanonicalName());
			}
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public synchronized void	execute() throws Exception
	{
		if (this.isUnderTest) {
			this.clock = this.clockServerOBP.getClock(this.clockURI);
			this.doPortDisconnection(this.clockServerOBP.getPortURI());
			this.clockServerOBP.unpublishPort();
			this.controlPeriod = 
					(long) ((STANDARD_CONTROL_PERIOD *
												TimeUnit.SECONDS.toNanos(1))/
									this.clock.getAccelerationFactor());
			this.clock.waitUntilStartInMillis();
		} else {
			this.controlPeriod =
					(long) (STANDARD_CONTROL_PERIOD *
												TimeUnit.SECONDS.toNanos(1));
		}

		this.scheduleTask(
				o -> ((HEM)o).controlLoop(),
				this.controlPeriod,
				TimeUnit.NANOSECONDS);
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void	finalise() throws Exception
	{
		this.doPortDisconnection(this.meterop.getPortURI());
		this.doPortDisconnection(this.heaterop.getPortURI());
		this.doPortDisconnection(this.indoorGardenop.getPortURI());
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.meterop.unpublishPort();
			this.heaterop.unpublishPort();
			this.indoorGardenop.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component internal methods
	// -------------------------------------------------------------------------

	/**
	 * control loop of the household energy manager; to be completed.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void		controlLoop()
	{
		try {
			StringBuffer sb = new StringBuffer("consumption = ");
			sb.append(this.meterop.getCurrentConsumption());
			sb.append(" production = ");
			sb.append(this.meterop.getCurrentProduction());
			sb.append(".\n");
			this.traceMessage(sb.toString());
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}

		this.scheduleTask(
				o -> ((HEM)o).controlLoop(),
				this.controlPeriod,
				TimeUnit.NANOSECONDS);
	}
}
// -----------------------------------------------------------------------------
