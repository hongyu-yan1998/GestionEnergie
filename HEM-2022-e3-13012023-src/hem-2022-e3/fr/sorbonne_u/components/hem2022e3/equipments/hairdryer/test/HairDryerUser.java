package fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.test;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// new implementation of the DEVS simulation standard for Java.
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

import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerCI;
import fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerConnector;
import fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerImplementationI;
import fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerOutboundPort;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.HairDryerUserModel;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.HairDryerUserRTAtomicSimulatorPlugin;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>HairDryerUser</code> implements a usage scenario for the
 * hair dryer in the household energy manager project.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This component illustrates one of the way to program a user tester, use a
 * DEVS simulator as user model. The user actions are triggered by a simulation
 * model which leverages the capability of a simulation model to call the
 * component code in the context of the integration of simulators and components
 * offered by BCM4Java-CyPhy. Hence, the component creates its simulation
 * plug-in containing its simulator and provides methods like {@code turnOn()}
 * that will be called by the simulator and which in turn calls the
 * corresponding servide defined by the {@code HairDryer} component through a
 * standard component interface.
 * </p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code accFactor > 0.0}
 * invariant	{@code simArchitectureURI != null && !simArchitectureURI.isEmpty()}
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code REFLECTION_INBOUND_PORT_URI != null && !REFLECTION_INBOUND_PORT_URI.isEmpty()}
 * </pre>
 * 
 * <p>Created on : 2022-10-24</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@RequiredInterfaces(required={HairDryerCI.class})
public class			HairDryerUser
extends		AbstractCyPhyComponent
implements	HairDryerImplementationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** URI of the hair dryer inbound port used in tests.					*/
	public static final String		REFLECTION_INBOUND_PORT_URI =
												"HAIR-DRYER-USER-RIP-URI";	
	/** when true, operations are traced.	*/
	protected static final boolean	VERBOSE = true ;

	/** outbound port to the {@code HairDryer} component.					*/
	protected HairDryerOutboundPort	hdop;
	/** inbound port URI of the {@code HairDryer} component.				*/
	protected String				hairDryerInboundPortURI;

	// SIL simulation

	/** simulator plug-in that holds the SIL simulator for this component.	*/
	protected HairDryerUserRTAtomicSimulatorPlugin	simulatorPlugin;
	/** URI of the local simulation architecture to be constructed.			*/
	protected String								simArchitectureURI;
	/** acceleration factor to be used when running the real time
	 *  simulation.															*/
	protected double								accFactor;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a hair dryer user component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hairDryerInboundPortURI != null && !hairDryerInboundPortURI.isEmpty()}
	 * pre	{@code simArchitectureURI != null && !simArchitectureURI.isEmpty()}
	 * pre	{@code accFactor > 0.0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param hairDryerInboundPortURI	URI of the inbound port of the hair dryer component.
	 * @param simArchitectureURI		URI of the simulation architecture to be created or the empty string  if the component does not execute as a SIL simulation.
	 * @param accFactor					acceleration factor for the simulation.
	 * @throws Exception 				<i>to do</i>.
	 */
	protected			HairDryerUser(
		String hairDryerInboundPortURI,
		String simArchitectureURI,
		double accFactor
		) throws Exception
	{
		super(REFLECTION_INBOUND_PORT_URI, 1, 1);

		assert	hairDryerInboundPortURI != null
									&& !hairDryerInboundPortURI.isEmpty() :
				new PreconditionException(
						"hairDryerInboundPortURI != null && "
						+ "!hairDryerInboundPortURI.isEmpty()");
		assert	simArchitectureURI != null && !simArchitectureURI.isEmpty() :
				new PreconditionException(
						"simArchitectureURI != null "
						+ "&& !simArchitectureURI.isEmpty()");
		assert	accFactor > 0.0 : new PreconditionException("accFactor > 0.0");

		this.hairDryerInboundPortURI = hairDryerInboundPortURI;
		this.hdop = new HairDryerOutboundPort(this);
		this.hdop.publishPort();

		this.simArchitectureURI = simArchitectureURI;
		this.accFactor = accFactor;

		if (VERBOSE) {
			this.tracer.get().setTitle("Hair dryer user component");
			this.tracer.get().setRelativePosition(1, 1);
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

		this.traceMessage("start.\n");
		try {
			this.doPortConnection(
					this.hdop.getPortURI(),
					this.hairDryerInboundPortURI,
					HairDryerConnector.class.getCanonicalName());

			this.simulatorPlugin = new HairDryerUserRTAtomicSimulatorPlugin();
			this.simulatorPlugin.setPluginURI(HairDryerUserModel.URI);
			this.simulatorPlugin.setSimulationExecutorService(
										STANDARD_SCHEDULABLE_HANDLER_URI);
			this.simulatorPlugin.initialiseSimulationArchitecture(
										this.simArchitectureURI,
										this.accFactor);
			this.installPlugin(this.simulatorPlugin);
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void	finalise() throws Exception
	{
		this.doPortDisconnection(this.hdop.getPortURI());
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		this.traceMessage("shutdown.\n");
		try {
			this.hdop.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerImplementationI#getState()
	 */
	@Override
	public HairDryerState	getState() throws Exception
	{
		if (VERBOSE) {
			this.traceMessage("getState().\n");
		}
		return this.hdop.getState();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerImplementationI#getMode()
	 */
	@Override
	public HairDryerMode	getMode() throws Exception
	{
		if (VERBOSE) {
			this.traceMessage("getMode().\n");
		}
		return this.hdop.getMode();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerImplementationI#turnOn()
	 */
	@Override
	public void			turnOn() throws Exception
	{
		if (VERBOSE) {
			this.traceMessage("turnOn().\n");
		}
		this.hdop.turnOn();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerImplementationI#turnOff()
	 */
	@Override
	public void			turnOff() throws Exception
	{
		if (VERBOSE) {
			this.traceMessage("turnOff().\n");
		}
		this.hdop.turnOff();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerImplementationI#setHigh()
	 */
	@Override
	public void			setHigh() throws Exception
	{
		if (VERBOSE) {
			this.traceMessage("setHigh().\n");
		}
		this.hdop.setHigh();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerImplementationI#setLow()
	 */
	@Override
	public void			setLow() throws Exception
	{
		if (VERBOSE) {
			this.traceMessage("setLow().\n");
		}
		this.hdop.setLow();
	}
}
// -----------------------------------------------------------------------------
