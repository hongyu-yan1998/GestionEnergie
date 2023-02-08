package fr.sorbonne_u.components.hem2022e3.equipments.meter;

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

import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.hem2022e1.equipments.meter.ElectricMeterCI;
import fr.sorbonne_u.components.hem2022e1.equipments.meter.ElectricMeterImplementationI;
import fr.sorbonne_u.components.hem2022e1.equipments.meter.ElectricMeterInboundPort;
import fr.sorbonne_u.components.hem2022e3.equipments.meter.sil.ElectricMeterCoupledModel;
import fr.sorbonne_u.components.hem2022e3.equipments.meter.sil.ElectricMeterElectricityModel;
import fr.sorbonne_u.components.hem2022e3.equipments.meter.sil.ElectricMeterRTAtomicSimulatorPlugin;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.events.AbstractAtomicSinkReference;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>ElectricMeter</code> implements a simplified electric meter
 * component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code !isSimulated || simArchitectureURI != null && !simArchitectureURI.isEmpty()}
 * invariant	{@code !isSimulated || accFactor > 0.0}
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code REFLECTION_INBOUND_PORT_URI != null && !REFLECTION_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code ELECTRIC_METER_INBOUND_PORT_URI != null && !ELECTRIC_METER_INBOUND_PORT_URI.isEmpty()}
 * </pre>
 * 
 * <p>Created on : 2021-09-13</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@OfferedInterfaces(offered={ElectricMeterCI.class})
//-----------------------------------------------------------------------------
public class			ElectricMeter
extends		AbstractCyPhyComponent
implements	ElectricMeterImplementationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------


	/** URI of the indoor garden reflection inbound port used in tests.		*/
	public static final String		REFLECTION_INBOUND_PORT_URI = "EM-RIP-URI";	
	/** URI of the electric meter inbound port used in tests.				*/
	public static final String		ELECTRIC_METER_INBOUND_PORT_URI =
															"ELECTRIC-METER";
	/** when true, methods trace their actions.								*/
	public static final boolean		VERBOSE = true;

	/** inbound port offering the <code>ElectricMeterCI</code> interface.	*/
	protected ElectricMeterInboundPort	emip;

	/** when true, the component executes in test mode.						*/
	protected final boolean							isUnderTest;
	/** true if the component must be execute in simulated mode.			*/
	protected final boolean							isSimulated;
	/** URI of the simulation architecture to be created or the empty string
	 *  if the component does not execute as a SIL simulation.				*/
	protected final String							simArchitectureURI;
	/** simulator plug-in that holds the SIL simulator for this component.	*/
	protected ElectricMeterRTAtomicSimulatorPlugin	simulatorPlugin;
	/** acceleration factor.												*/
	protected final double							accFactor;


	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an electric meter component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code ELECTRIC_METER_INBOUND_PORT_URI != null}
	 * pre	{@code !ELECTRIC_METER_INBOUND_PORT_URI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 * 
	 * @throws Exception	<i>to do</i>.
	 */
	protected			ElectricMeter() throws Exception
	{
		this(ELECTRIC_METER_INBOUND_PORT_URI);
	}

	/**
	 * create an electric meter component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code electricMeterInboundPortURI != null && !electricMeterInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param electricMeterInboundPortURI	URI of the electric meter inbound port.
	 * @throws Exception					<i>to do</i>.
	 */
	protected			ElectricMeter(
		String electricMeterInboundPortURI
		) throws Exception
	{
		this(REFLECTION_INBOUND_PORT_URI, electricMeterInboundPortURI,
			 false, false, null, 1.0);
	}

	/**
	 * create an electric meter component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code electricMeterInboundPortURI != null && !electricMeterInboundPortURI.isEmpty()}
	 * pre	{@code !isSimulated || simArchitectureURI != null && !simArchitectureURI.isEmpty()}
	 * pre	{@code !isSimulated || accFactor > 0.0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param reflectionInboundPortURI		URI of the reflection innbound port of the component.
	 * @param electricMeterInboundPortURI	URI of the electric meter inbound port.
	 * @param isUnderTest					if true, execute the component in unit testing mode.
	 * @param isSimulated					if true, execute the component in SIL simulated mode.
	 * @param simArchitectureURI			URI of the simulation architecture to be created or the empty string  if the component does not execute as a SIL simulation.
	 * @param accFactor						acceleration factor used in timing actions.
	 * @throws Exception					<i>to do</i>.
	 */
	protected			ElectricMeter(
		String reflectionInboundPortURI,
		String electricMeterInboundPortURI,
		boolean isUnderTest,
		boolean isSimulated,
		String simArchitectureURI,
		double accFactor
		) throws Exception
	{
		super(reflectionInboundPortURI, 1, 1);

		assert	electricMeterInboundPortURI != null &&
										!electricMeterInboundPortURI.isEmpty() :
				new PreconditionException(
						"electricMeterInboundPortURI != null && "
						+ "!electricMeterInboundPortURI.isEmpty()");
		assert	!isSimulated || simArchitectureURI != null && 
												!simArchitectureURI.isEmpty() :
				new PreconditionException(
						"!isSimulated || simArchitectureURI != null && "
						+ "!simArchitectureURI.isEmpty()");
		assert	!isSimulated || accFactor > 0.0 :
				new PreconditionException("!isSimulated || accFactor > 0.0");

		this.isUnderTest = isUnderTest;
		this.isSimulated = isSimulated;
		this.accFactor = accFactor;
		this.simArchitectureURI = simArchitectureURI;
		
		this.initialise(electricMeterInboundPortURI);
	}

	/**
	 * initialise an electric meter component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code electricMeterInboundPortURI != null}
	 * pre	{@code !electricMeterInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param electricMeterInboundPortURI	URI of the electric meter inbound port.
	 * @throws Exception					<i>to do</i>.
	 */
	protected void		initialise(String electricMeterInboundPortURI)
	throws Exception
	{
		this.emip =
				new ElectricMeterInboundPort(electricMeterInboundPortURI, this);
		this.emip.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Electric meter component");
			this.tracer.get().setRelativePosition(0, 4);
			this.toggleTracing();
		}
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

		if (this.isSimulated) {
			this.simulatorPlugin = new ElectricMeterRTAtomicSimulatorPlugin();
			this.simulatorPlugin.setPluginURI(ElectricMeterCoupledModel.URI);
			this.simulatorPlugin.setSimulationExecutorService(
											STANDARD_SCHEDULABLE_HANDLER_URI);
			try {
				this.simulatorPlugin.initialiseSimulationArchitecture(
										this.simArchitectureURI,
										this.accFactor);
				this.installPlugin(this.simulatorPlugin);
			} catch (Exception e) {
				throw new ComponentStartException(e) ;
			}
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.emip.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.meter.ElectricMeterImplementationI#getCurrentConsumption()
	 */
	@Override
	public double		getCurrentConsumption() throws Exception
	{
		return this.simulatorPlugin.getTotalConsumption();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.meter.ElectricMeterImplementationI#getCurrentProduction()
	 */
	@Override
	public double		getCurrentProduction() throws Exception
	{
		return this.simulatorPlugin.getTotalProduction();
	}
}
// -----------------------------------------------------------------------------
