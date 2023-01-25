package fr.sorbonne_u.components.hem2022e3.equipments.hairdryer;

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
import fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerCI;
import fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerImplementationI;
import fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerInboundPort;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.HairDryerCoupledModel;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.HairDryerRTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.HairDryerStateModel;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.events.SetHighHairDryer;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.events.SetLowHairDryer;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.events.SwitchOffHairDryer;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.events.SwitchOnHairDryer;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>HairDryer</code> implements the hair dryer component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The hair dryer is an uncontrollable appliance, hence it does not connect
 * with the household energy manager. However, it will connect later to the
 * electric panel to take its (simulated) electricity consumption into account.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code INITIAL_STATE != null}
 * invariant	{@code INITIAL_MODE != null}
 * invariant	{@code currentState != null}
 * invariant	{@code currentMode != null}
 * invariant	{@code !isSILsimulated || simArchitectureURI != null && !simArchitectureURI.isEmpty()}
 * </pre>
 * 
 * <p>Created on : 2021-09-09</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@OfferedInterfaces(offered={HairDryerCI.class})
//-----------------------------------------------------------------------------
public class			HairDryer
extends		AbstractCyPhyComponent
implements	HairDryerImplementationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** URI of the hair dryer inbound port used in tests.					*/
	public static final String			REFLECTION_INBOUND_PORT_URI =
												"HAIR-DRYER-RIP-URI";	
	/** URI of the hair dryer inbound port used in tests.					*/
	public static final String			INBOUND_PORT_URI =
												"HAIR-DRYER-INBOUND-PORT-URI";
	/** when true, methods trace their actions.								*/
	public static final boolean			VERBOSE = true;
	/** initial state in which the hair dryer is put.						*/
	public static final HairDryerState	INITIAL_STATE = HairDryerState.OFF;
	/** initial mode in which the hair dryer is put.						*/
	public static final HairDryerMode	INITIAL_MODE = HairDryerMode.LOW;

	/** current state (on, off) of the hair dryer.							*/
	protected HairDryerState					currentState;
	/** current mode of operation (low, high) of the hair dryer.			*/
	protected HairDryerMode						currentMode;

	/** inbound port offering the <code>HairDryerCI</code> interface.		*/
	protected HairDryerInboundPort				hdip;

	// SIL simulation

	/** true if the component executes as a SIL simulation, false otherwise.*/
	protected final boolean						isSimulated;
	/** when true, the execution is a unit test, otherwise an it
	 *  is an integration test.												*/
	protected final boolean						isUnitTesting;
	/** URI of the simulation architecture to be created or the empty string
	 *  if the component does not execute as a SIL simulation.				*/
	protected final String						simArchitectureURI;
	/** simulator plug-in that holds the SIL simulator for this component.	*/
	protected HairDryerRTAtomicSimulatorPlugin	simulatorPlugin;
	/** acceleration factor to be used when running the real time
	 *  simulation.															*/
	protected double							accFactor;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a hair dryer component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty()}
	 * post	{@code getState() == HairDryerState.OFF}
	 * post	{@code getMode() == HairDryerMode.LOW}
	 * </pre>
	 * 
	 * @throws Exception	<i>to do</i>.
	 */
	protected			HairDryer() throws Exception
	{
		this(INBOUND_PORT_URI);
	}

	/**
	 * create a hair dryer component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hairDryerInboundPortURI != null && !hairDryerInboundPortURI.isEmpty()}
	 * post	{@code getState() == HairDryerState.OFF}
	 * post	{@code getMode() == HairDryerMode.LOW}
	 * </pre>
	 * 
	 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			HairDryer(String hairDryerInboundPortURI)
	throws Exception
	{
		this(hairDryerInboundPortURI, false, false, null, 1.0);
	}

	/**
	 * create a hair dryer component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty()}
	 * pre	{@code !isSimulated || simArchitectureURI != null && !simArchitectureURI.isEmpty()}
	 * pre	{@code !isSimulated || accFactor > 0.0}
	 * post	{@code getState() == HairDryerState.OFF}
	 * post	{@code getMode() == HairDryerMode.LOW}
	 * </pre>
	 * 
	 * @param isSimulated			when true, the component performs a SIL simulation in a test run.
	 * @param isUnitTesting			when true, the simulated test run is a unit test, otherwise it is an integration test.
	 * @param simArchitectureURI	URI of the simulation architecture to be created or the empty string  if the component does not execute as a SIL simulation.
	 * @param accFactor				acceleration factor for the simulation.
	 * @throws Exception			<i>to do</i>.
	 */
	protected			HairDryer(
		boolean isSimulated,
		boolean isUnitTesting,
		String simArchitectureURI,
		double accFactor
		) throws Exception
	{
		this(INBOUND_PORT_URI, isSimulated, isUnitTesting,
			 simArchitectureURI, accFactor);
	}

	/**
	 * create a hair dryer component with the given reflection innbound port
	 * URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hairDryerInboundPortURI != null && !hairDryerInboundPortURI.isEmpty()}
	 * pre	{@code !isSimulated || simArchitectureURI != null && !simArchitectureURI.isEmpty()}
	 * pre	{@code !isSimulated || accFactor > 0.0}
	 * post	{@code getState() == HairDryerState.OFF}
	 * post	{@code getMode() == HairDryerMode.LOW}
	 * </pre>
	 *
	 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port.
	 * @param isSimulated				when true, the component performs a SIL simulation in a test run.
	 * @param isUnitTesting				when true, the simulated test run is a unit test, otherwise it is an integration test.
	 * @param simArchitectureURI		URI of the simulation architecture to be created or the empty string  if the component does not execute as a SIL simulation.
	 * @param accFactor					acceleration factor for the simulation.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			HairDryer(
		String hairDryerInboundPortURI,
		boolean isSimulated,
		boolean isUnitTesting,
		String simArchitectureURI,
		double accFactor
		) throws Exception
	{
		super(REFLECTION_INBOUND_PORT_URI, 1, 1);

		assert	hairDryerInboundPortURI != null &&
											!hairDryerInboundPortURI.isEmpty() :
				new PreconditionException(
						"hairDryerInboundPortURI != null && "
						+ "!hairDryerInboundPortURI.isEmpty()");
		assert	!isSimulated || simArchitectureURI != null &&
											!simArchitectureURI.isEmpty() :
				new PreconditionException(
						"!isSimulated || "
						+ "simArchitectureURI != null && "
						+ "!simArchitectureURI.isEmpty()");
		assert	!isSimulated || accFactor > 0.0 :
				new PreconditionException("!isSimulated || accFactor > 0.0");

		this.isSimulated = isSimulated;
		this.isUnitTesting = isUnitTesting;
		this.simArchitectureURI = simArchitectureURI;
		this.accFactor = accFactor;
		
		this.currentState = INITIAL_STATE;
		this.currentMode = INITIAL_MODE;
		this.hdip = new HairDryerInboundPort(hairDryerInboundPortURI, this);
		this.hdip.publishPort();

		if (HairDryer.VERBOSE) {
			this.tracer.get().setTitle("Hair dryer component");
			this.tracer.get().setRelativePosition(0, 1);
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
			this.simulatorPlugin = new HairDryerRTAtomicSimulatorPlugin();
			this.simulatorPlugin.setPluginURI(this.isUnitTesting ?
												HairDryerCoupledModel.URI
											  :	HairDryerStateModel.URI);
			this.simulatorPlugin.setSimulationExecutorService(
											STANDARD_SCHEDULABLE_HANDLER_URI);
			try {
				this.simulatorPlugin.initialiseSimulationArchitecture(
						this.simArchitectureURI,
						this.accFactor,
						this.isUnitTesting);
				this.installPlugin(this.simulatorPlugin);
			} catch (Exception e) {
				throw new ComponentStartException(e) ;
			}
		}
		this.traceMessage("start.\n");
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		this.traceMessage("shutdown.\n");
		try {
			this.hdip.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerImplementationI#getState()
	 */
	@Override
	public HairDryerState	getState() throws Exception
	{
		if (HairDryer.VERBOSE) {
			this.traceMessage("getState() : " + this.currentState + ".\n");
		}

		return this.currentState;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerImplementationI#getMode()
	 */
	@Override
	public HairDryerMode	getMode() throws Exception
	{
		if (HairDryer.VERBOSE) {
			this.traceMessage("getMode() : " + this.currentMode + ".\n");
		}

		return this.currentMode;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerImplementationI#turnOn()
	 */
	@Override
	public void			turnOn() throws Exception
	{
		if (HairDryer.VERBOSE) {
			this.traceMessage("turnOn().\n");
		}

		assert	this.getState() == HairDryerState.OFF :
				new PreconditionException("getState() == HairDryerState.OFF");

		this.currentState = HairDryerState.ON;
		this.currentMode = HairDryerMode.LOW;

		if (this.isSimulated) {
			// trigger an immediate SwitchOnHairDryer event on the
			// HairDryerStateModel, which in turn will emit this event
			// towards the other models of the hair dryer
			// the t parameter in the lambda expression represents the current
			// simulation time to be provided by he simulator before passing
			// the event instance to the model
			this.simulatorPlugin.triggerExternalEvent(
					HairDryerStateModel.URI,
					t -> new SwitchOnHairDryer(t));
		}
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerImplementationI#turnOff()
	 */
	@Override
	public void			turnOff() throws Exception
	{
		if (HairDryer.VERBOSE) {
			this.traceMessage("turnOff().\n");
		}

		assert	this.getState() == HairDryerState.ON :
				new PreconditionException("getState() == HairDryerState.ON");

		this.currentState = HairDryerState.OFF;

		if (this.isSimulated) {
			// trigger an immediate SwitchOnHairDryer event on the
			// HairDryerStateModel, which in turn will emit this event
			// towards the other models of the hair dryer
			// the t parameter in the lambda expression represents the current
			// simulation time to be provided by he simulator before passing
			// the event instance to the model
			this.simulatorPlugin.triggerExternalEvent(
					HairDryerStateModel.URI,
					t -> new SwitchOffHairDryer(t));
		}
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerImplementationI#setHigh()
	 */
	@Override
	public void			setHigh() throws Exception
	{
		if (HairDryer.VERBOSE) {
			this.traceMessage("setHigh().\n");
		}

		assert	this.getState() == HairDryerState.ON :
				new PreconditionException("getState() == HairDryerState.ON");
		assert	this.getMode() == HairDryerMode.LOW :
				new PreconditionException("getMode() == HairDryerMode.LOW");

		this.currentMode = HairDryerMode.HIGH;

		if (this.isSimulated) {
			// trigger an immediate SwitchOnHairDryer event on the
			// HairDryerStateModel, which in turn will emit this event
			// towards the other models of the hair dryer
			// the t parameter in the lambda expression represents the current
			// simulation time to be provided by he simulator before passing
			// the event instance to the model
			this.simulatorPlugin.triggerExternalEvent(
					HairDryerStateModel.URI,
					t -> new SetHighHairDryer(t));
		}
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerImplementationI#setLow()
	 */
	@Override
	public void			setLow() throws Exception
	{
		if (HairDryer.VERBOSE) {
			this.traceMessage("setLow().\n");
		}

		assert	this.getState() == HairDryerState.ON :
				new PreconditionException("getState() == HairDryerState.ON");
		assert	this.getMode() == HairDryerMode.HIGH :
				new PreconditionException("getMode() == HairDryerMode.HIGH");

		this.currentMode = HairDryerMode.LOW;

		if (this.isSimulated) {
			// trigger an immediate SwitchOnHairDryer event on the
			// HairDryerStateModel, which in turn will emit this event
			// towards the other models of the hair dryer
			// the t parameter in the lambda expression represents the current
			// simulation time to be provided by he simulator before passing
			// the event instance to the model
			this.simulatorPlugin.triggerExternalEvent(
					HairDryerStateModel.URI,
					t -> new SetLowHairDryer(t));
		}
	}
}
// -----------------------------------------------------------------------------
