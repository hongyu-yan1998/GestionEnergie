package fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden;

// Copyright Jacques Malenfant, Sorbonne Universite.
//
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

import java.time.Instant;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.tools.aclocks.AcceleratedClock;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServer;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerCI;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerConnector;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerOutboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.connections.IndoorGardenInboundPort;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.IndoorGardenCoupledModel;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.IndoorGardenRTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.IndoorGardenStateModel;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.events.SwitchLightOffIndoorGarden;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.events.SwitchLightOnIndoorGarden;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.events.SwitchOffIndoorGarden;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.events.SwitchOnIndoorGarden;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>IndoorGarden</code> implements the distant control unit of
 * a small indoor garden which turns a light on during the day to help growing
 * the crops.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This component is provided to show a kind of electric equipment that can
 * both be planned and suspended. However, the plan here is a bit different
 * from plans in other equipments like washing machines where a plan can be
 * postponed but then it will still be fully completed at a later time. The
 * indoor garden can postpone the beginning of its plan, but then the plan will
 * be shortened as the end of the plan will remain the same. Also, the plan is
 * meant to ne repeated each day, another difference from a plan for a
 * washing machine.
 * </p>
 * <p>
 * The indoor garden is implemented in such a way that when switched on, the
 * light is off. Switching the light on and off is done automatically through
 * the plan defining the time at which it must be switched on and the time at
 * which it must be switched off. Hence, the light will not be operative until
 * a plan is defined.
 * </p>
 * <p>
 * As the indoor garden needs an access to the current real time to operate
 * properly, a simulated mode if provided that allows to set the current time
 * arbitrarily.
 * </p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code !isSimulated || simArchitectureURI != null && !simArchitectureURI.isEmpty()}
 * invariant	{@code !isSimulated || accFactor > 0.0}
 * invariant	{@code !isUnderTest || clockURI != null && !clockURI.isEmpty()}
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code REFLECTION_INBOUND_PORT_URI != null && !REFLECTION_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code INBOUND_PORT_URI_PREFIX != null && !INBOUND_PORT_URI_PREFIX.isEmpty()}
 * </pre>
 * 
 * <p>Created on : 2022-09-14</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@OfferedInterfaces(offered = {IndoorGardenCI.class})
@RequiredInterfaces(required={ClockServerCI.class})
//-----------------------------------------------------------------------------
public class			IndoorGarden
extends		AbstractCyPhyComponent
implements	IndoorGardenImplementationI
{
	// -------------------------------------------------------------------------
	// Inner classes and types
	// -------------------------------------------------------------------------

	/**
	 * The enumeration <code>State</code> describes the discrete states or
	 * modes of the indoor garden.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * The indoor garden can be <code>OFF</code> or on, and then it is either
	 * in <code>LIGHT_OFF</code> mode (the light is off) or in
	 * <code>LIGHT_ON</code> mode (the light is on).
	 * 
	 * <p>Created on : 2022-10-04</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static enum State {
		OFF,
		/** on, but not planned, hence the light does not switch on.		*/
		ON,
		/** light on and off is planned.									*/
		PLANNED,
		/** appliance is on, planned and the light is on.					*/
		LIGHT_ON,			
		/** appliance is on, planned but plan is deactivated, hence the
		 *  light is off and remains off if reactivated.					*/
		DEACTIVATED,
		/** appliance is on, planned and light was on but plan is
		 *  deactivated, hence the light is off but will be on if
		 *  reactivated.													*/
		DEACTIVATED_LIGHT_ON
	}

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** URI of the indoor garden reflection inbound port used in tests.		*/
	public static final String	REFLECTION_INBOUND_PORT_URI = "IG-RIP-URI";	
	/** a convenient string to be used as inbound port URI prefix.			*/
	public static final String	INBOUND_PORT_URI_PREFIX = "indoor-garden-ibp";
	/** when true, methods trace their actions.								*/
	public static final boolean	VERBOSE = true;

	/** the inbound port of the component.									*/
	protected IndoorGardenInboundPort				inboundPort;

	/** current state of the indoor garden.									*/
	protected State									currentState;
	/** if the lighting plan has been defined, the time at which the light
	 *  must be switched on.												*/
	protected Instant								timeToSwitchLightOn;
	/** if the lighting plan has been defined, the time at which the light
	 *  must be switched off.												*/
	protected Instant								timeToSwitchLightOff;

	/** when true, the component executes in test mode.						*/
	protected final boolean							isUnderTest;
	/** when true, the component executes in unit test mode.				*/	
	protected final boolean							isUnitTesting;
	/** true if the component must be execute in simulated mode.			*/
	protected final boolean							isSimulated;
	/** URI of the simulation architecture to be created or the empty string
	 *  if the component does not execute as a SIL simulation.				*/
	protected final String							simArchitectureURI;
	/** simulator plug-in that holds the SIL simulator for this component.	*/
	protected IndoorGardenRTAtomicSimulatorPlugin	simulatorPlugin;
	/** acceleration factor.												*/
	protected final double							accFactor;

	/** outbound port to connect to the centralised clock server.			*/
	protected ClockServerOutboundPort				clockServerOBP;
	/** URI of the clock to be used to synchronise the test scenarios and
	 *  the simulation.														*/
	protected final String							clockURI;
	/** accelerated clock used to implement the simulation scenario.		*/
	protected AcceleratedClock						clock;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a component instance.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isSimulated || simArchitectureURI != null && !simArchitectureURI.isEmpty()}
	 * pre	{@code !isSimulated || accFactor > 0.0}
	 * pre	{@code !isUnderTest || clockURI != null && !clockURI.isEmpty()}
	 * post	{@code !isOn()}
	 * </pre>
	 *
	 * @param isUnderTest			if true, execute the component in test mode.
	 * @param isUnitTesting			if true, execute the component in unit testing mode.
	 * @param isSimulated			if true, execute the component in SIL simulated mode.
	 * @param simArchitectureURI	URI of the simulation architecture to be created or the empty string  if the component does not execute as a SIL simulation.
	 * @param accFactor				acceleration factor used in timing actions.
	 * @param clockURI				URI of the clock to be used to synchronise the test scenarios and the simulation.
	 * @throws Exception			<i>to do</i>.
	 */
	protected			IndoorGarden(
		boolean isUnderTest,
		boolean isUnitTesting,
		boolean isSimulated,
		String simArchitectureURI,
		double accFactor,
		String clockURI
		) throws Exception
	{
		super(REFLECTION_INBOUND_PORT_URI, 1, 1);

		assert	!isSimulated || simArchitectureURI != null && !simArchitectureURI.isEmpty() :
				new PreconditionException(
						"!isSimulated || simArchitectureURI != null && "
						+ "!simArchitectureURI.isEmpty()");
		assert	!isSimulated || accFactor > 0.0 :
				new PreconditionException("!isSimulated || accFactor > 0.0");
		assert	!isUnderTest || clockURI != null && !clockURI.isEmpty() :
				new PreconditionException(
						"!isUnderTest || "
						+ "clockURI != null && !clockURI.isEmpty()");

		this.isUnderTest = isUnderTest;
		this.isUnitTesting = isUnitTesting;
		this.isSimulated = isSimulated;
		this.simArchitectureURI = simArchitectureURI;
		this.accFactor = accFactor;
		this.clockURI = clockURI;

		this.currentState = State.OFF;
		this.inboundPort = new IndoorGardenInboundPort(
									IndoorGarden.INBOUND_PORT_URI_PREFIX, this);
		this.inboundPort.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Indoor garden component");
			this.tracer.get().setRelativePosition(0, 3);
			this.toggleTracing();		
		}

		assert	!this.isOn();
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
			if (this.isUnderTest) {
				this.clockServerOBP = new ClockServerOutboundPort(this);
				this.clockServerOBP.publishPort();
				this.doPortConnection(
						this.clockServerOBP.getPortURI(),
						ClockServer.STANDARD_INBOUNDPORT_URI,
						ClockServerConnector.class.getCanonicalName());
				if (this.isSimulated) {
					this.simulatorPlugin =
									new IndoorGardenRTAtomicSimulatorPlugin();
					this.simulatorPlugin.setPluginURI(
							this.isUnitTesting ?
								IndoorGardenCoupledModel.URI
							:	IndoorGardenStateModel.URI);
					this.simulatorPlugin.setSimulationExecutorService(
											STANDARD_SCHEDULABLE_HANDLER_URI);
					this.simulatorPlugin.initialiseSimulationArchitecture(
							this.simArchitectureURI,
							this.accFactor,
							this.isUnitTesting);
					this.installPlugin(this.simulatorPlugin);
				}
			} else {
				throw new ComponentStartException("only test version available!");
			}
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
		this.traceMessage("start.\n");
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		if (this.isUnderTest) {
			this.clock = this.clockServerOBP.getClock(this.clockURI);
			assert	this.clock.getAccelerationFactor() == this.accFactor :
				new AssertionError(
						"Incompatible clock having acceleration factor "
								+ this.clock.getAccelerationFactor()
								+ "with predefined acceleration factor "
								+ this.accFactor);
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void	finalise() throws Exception
	{
		this.doPortDisconnection(this.clockServerOBP.getPortURI());
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
			this.inboundPort.unpublishPort();
			this.clockServerOBP.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#isOn()
	 */
	@Override
	public boolean		isOn() throws Exception
	{
		if (VERBOSE) {
			this.traceMessage("Indoor garden is on? " +
								(this.currentState != State.OFF) + ".\n");
		}

		return this.currentState != State.OFF;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#on()
	 */
	@Override
	public void			on() throws Exception
	{
		assert	!isOn() : new PreconditionException("!isOn()");

		if (VERBOSE) {
			this.traceMessage("Indoor garden is switched on.\n");
		}

		this.switchOn();
		this.currentState = State.ON;

		assert	isOn() : new PostconditionException("isOn()");
		assert	!isLightOn() : new PostconditionException("!isLightOn()");
		assert	!isLightPlanned() :
				new PostconditionException("!isLightPlanned()");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#off()
	 */
	@Override
	public void			off() throws Exception
	{
		assert isOn() : new PreconditionException("isOn()");

		if (VERBOSE) {
			this.traceMessage("switched off.\n");
		}

		this.switchOff();
		this.currentState = State.OFF;

		assert !isOn() : new PostconditionException("!isOn()");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#isLightPlanned()
	 */
	@Override
	public boolean		isLightPlanned() throws Exception
	{
		if (VERBOSE) {
			this.traceMessage("is light planned: "
								+  this.notInStates(new State[]{
														State.OFF, State.ON})
							  	+ ".\n");
		}

		return this.notInStates(new State[]{State.OFF, State.ON});
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.IndoorGardenImplementationI#plan(java.time.Instant, java.time.Instant)
	 */
	@Override
	public void			plan(Instant timeOn, Instant timeOff)
	throws Exception
	{
		assert	isOn() : new PreconditionException("isOn()");
		assert	!isLightPlanned() :
				new PreconditionException("!isLightPlanned()");
		assert	timeOn != null : new PreconditionException("timeOn != null");
		assert	timeOff != null : new PreconditionException("timeOff != null");
		assert	timeOff.isAfter(timeOn) :
				new PreconditionException("timeOff.isAfter(timeOn)");

		if (VERBOSE) {
			this.traceMessage("plan light from " + timeOn + " to " + timeOff + ".\n");
		}

		this.timeToSwitchLightOn = timeOn;
		this.timeToSwitchLightOff = timeOff;
		if (this.currentPlanActive()) {
			this.currentState = State.LIGHT_ON;
		} else {
			this.currentState = State.PLANNED;
		}
		this.schedulePlan();

		assert	isLightPlanned() :
				new PostconditionException("isLightPlanned()");
		assert	!isDeactivated() :
				new PostconditionException("!isDeactivated()");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#cancelPlanLight()
	 */
	@Override
	public void			cancelPlan() throws Exception
	{
		assert	isOn() : new PreconditionException("isOn()");
		assert	isLightPlanned() :
				new PreconditionException("isLightPlanned()");

		if (VERBOSE) {
			this.traceMessage("cancelled plan at " +
							  this.clock.currentInstant() + ".\n");
		}

		if (this.isLightOn()) {
			this.switchLightOff();
		}
		this.currentState = State.ON;
		this.timeToSwitchLightOn = null;
		this.timeToSwitchLightOff = null;
		this.cancelSchedule();

		assert	!isLightPlanned() :
				new PostconditionException("!isLightPlanned()");
		assert	!isLightOn() : new PostconditionException("!isLightOn()");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.IndoorGardenImplementationI#getPlannedLightOn()
	 */
	@Override
	public Instant		getPlannedLightOn() throws Exception
	{
		assert	isOn() : new PreconditionException("isOn()");
		assert	isLightPlanned() :
				new PreconditionException("isLightPlanned()");

		if (VERBOSE) {
			this.traceMessage("planned light on at " +
										this.timeToSwitchLightOn + ".\n");
		}

		return this.timeToSwitchLightOn;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#getPlannedLightOff()
	 */
	@Override
	public Instant		getPlannedLightOff() throws Exception
	{
		assert	isOn() : new PreconditionException("isOn()");
		assert	isLightPlanned() :
				new PreconditionException("isLightPlanned()");

		if (VERBOSE) {
			this.traceMessage("planned light off at " +
							  			this.timeToSwitchLightOff + ".\n");
		}

		return this.timeToSwitchLightOff;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#isLightOn()
	 */
	@Override
	public boolean		isLightOn() throws Exception
	{
		if (VERBOSE) {
			this.traceMessage("is light on? "
								+ this.isInState(State.LIGHT_ON)
								+ ".\n");
		}

		return this.isInState(State.LIGHT_ON);
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#isDeactivated()
	 */
	@Override
	public boolean		isDeactivated() throws Exception
	{
		if (VERBOSE) {
			this.traceMessage("is deactivated? "
								+ this.isInStates(new State[]{State.DEACTIVATED,
										State.DEACTIVATED_LIGHT_ON})
								+ ".\n");
		}

		return this.isInStates(new State[]{State.DEACTIVATED,
										   State.DEACTIVATED_LIGHT_ON});
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#deactivate()
	 */
	@Override
	public void			deactivate() throws Exception
	{
		assert	isOn() : new PreconditionException("isOn()");
		assert	isLightPlanned() : new PreconditionException("isLightPlanned()");
		assert	!isDeactivated() : new PreconditionException("!isDeactivated()");

		if (VERBOSE) {
			this.traceMessage("deactivating at " +
							  this.clock.currentInstant() + ".\n");
		}

		if (this.isLightOn()) {
			this.switchLightOff();
		}
		if (this.isInState(State.PLANNED)) {
			this.currentState = State.DEACTIVATED;
		} else {
			assert	 this.isInState(State.LIGHT_ON);
			this.currentState = State.DEACTIVATED_LIGHT_ON;
		}

		assert	isDeactivated() : new PostconditionException("isDeactivated()");
		assert	!isLightOn() : new PostconditionException("!isLightOn()");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#reactivate()
	 */
	@Override
	public void			reactivate() throws Exception
	{
		assert	isOn() : new PreconditionException("isOn()");
		assert	isLightPlanned() : new PreconditionException("isLightPlanned()");
		assert	isDeactivated() : new PreconditionException("isDeactivated()");

		if (VERBOSE) {
			this.traceMessage(
					"reactivating at " + this.clock.currentInstant() + ".\n");
		}

		this.currentState = State.PLANNED;
		if (this.currentPlanActive() && !this.isLightOn()) {
			this.switchLightOn();
			this.currentState = State.LIGHT_ON;
		}

		assert	!isDeactivated() : new PostconditionException("!isDeactivated()");
	}

	// -------------------------------------------------------------------------
	// Component internal methods
	// -------------------------------------------------------------------------

	/**
	 * return true if the component is in state {@code s}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param s	a state.
	 * @return	true if the component is in state {@code s}.
	 */
	protected boolean	isInState(State s)
	{
		return this.currentState == s;
	}

	/**
	 * return true if the component is in one of {@code states}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code states != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param states	an array of states.
	 * @return			true if the component is in one of {@code states}.
	 */
	protected boolean	isInStates(State[] states)
	{
		assert	states != null : new PreconditionException("states != null");

		for (State s : states) {
			if (this.currentState == s) return true;
		}
		return false;
	}

	/**
	 * return true if the component is not in state {@code s}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param s	a state.
	 * @return	true if the component is not in state {@code s}.
	 */
	protected boolean	notInState(State s)
	{
		return this.currentState != s;
	}

	/**
	 * return true if the component is in none of {@code states}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code states != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param states	an array of states.
	 * @return			true if the component is in none of {@code states}.
	 */
	protected boolean	notInStates(State[] states)
	{
		assert	states != null : new PreconditionException("states != null");

		for (State s : states) {
			if (this.currentState == s) return false;
		}
		return true;
	}

	/**
	 * return true if the current plan is active now.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				true if the current plan is active now.
	 * @throws Exception	<i>to do</i>.
	 */
	protected boolean	currentPlanActive() throws Exception
	{
		Instant now = null;
		if (this.isUnderTest) {
			now = this.clock.currentInstant();
		} else {
			now = Instant.now();
		}
		return now.isAfter(this.timeToSwitchLightOn) &&
									now.isBefore(this.timeToSwitchLightOff);
	}

	/**
	 * physically switch on the indoor garden (actuator implementation).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void		switchOn()
	{
		this.traceMessage("switch the indoor garden on.\n");
		if (this.isSimulated) {
			try {
				// trigger an immediate SwitchOnIndoorGarden event on the
				// IndoorGardenStateModel, which in turn will emit this event
				// towards the other models of the indoor garden
				// the t parameter in the lambda expression represents the
				// current simulation time to be provided by he simulator before
				// passing the event instance to the model
				this.simulatorPlugin.triggerExternalEvent(
											IndoorGardenStateModel.URI,
											t -> new SwitchOnIndoorGarden(t));
			} catch (Exception e) {
				throw new RuntimeException(e) ;
			}
		} else {
			// In a fully operational implementation of an embedded software,
			// this method would call the physical actuator switching on the
			// indoor garden. As we are not developing for an actual device,
			// nothing is done.
			if (VERBOSE) {
				System.out.print(this.clock.currentInstant()
										+ " switch the indoor garden on.\n");
			}
		}
	}

	/**
	 * physically switch off the indoor garden (actuator implementation).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void		switchOff()
	{
		this.traceMessage("switch the indoor garden off.\n");
		if (this.isSimulated) {
			try {
				// trigger an immediate SwitchOffIndoorGarden event on the
				// IndoorGardenStateModel, which in turn will emit this event
				// towards the other models of the indoor garden
				// the t parameter in the lambda expression represents the
				// current simulation time to be provided by he simulator before
				// passing the event instance to the model
				this.simulatorPlugin.triggerExternalEvent(
											IndoorGardenStateModel.URI,
											t -> new SwitchOffIndoorGarden(t));
			} catch (Exception e) {
				throw new RuntimeException(e) ;
			}
		} else {
			// In a fully operational implementation of an embedded software,
			// this method would call the physical actuator switching off the
			// indoor garden. As we are not developing for an actual device,
			// nothing is done.
			if (VERBOSE) {
				System.out.print(this.clock.currentInstant()
										+ " switch the indoor garden off.\n");
			}
		}
	}

	/**
	 * physically switch the light on (actuator implementation).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void		switchLightOn()
	{
		this.traceMessage("switch the light on.\n");
		if (this.isSimulated) {
			try {
				// trigger an immediate SwitchLightOnIndoorGarden event on the
				// IndoorGardenStateModel, which in turn will emit this event
				// towards the other models of the indoor garden
				// the t parameter in the lambda expression represents the
				// current simulation time to be provided by he simulator before
				// passing the event instance to the model
				this.simulatorPlugin.triggerExternalEvent(
										IndoorGardenStateModel.URI,
										t -> new SwitchLightOnIndoorGarden(t));
			} catch (Exception e) {
				throw new RuntimeException(e) ;
			}
		} else {
			// In a fully operational implementation of an embedded software,
			// this method would call the physical actuator switching on the
			// light of the indoor garden. As we are not developing for an
			// actual device, nothing is done.
			if (VERBOSE) {
				System.out.print(this.clock.currentInstant()
										+ " switch the light on.\n");
			}
		}
	}

	/**
	 * physically switch the light off (actuator implementation).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void		switchLightOff()
	{
		this.traceMessage("switch the light off.\n");
		if (this.isSimulated) {
			try {
				// trigger an immediate SwitchLightOffIndoorGarden event on the
				// IndoorGardenStateModel, which in turn will emit this event
				// towards the other models of the indoor garden
				// the t parameter in the lambda expression represents the
				// current simulation time to be provided by he simulator before
				// passing the event instance to the model
				this.simulatorPlugin.triggerExternalEvent(
										IndoorGardenStateModel.URI,
										t -> new SwitchLightOffIndoorGarden(t));
			} catch (Exception e) {
				throw new RuntimeException(e) ;
			}
		} else {
			// In a fully operational implementation of an embedded software,
			// this method would call the physical actuator switching off the
			// light of the indoor garden. As we are not developing for an
			// actual device, nothing is done.
			if (VERBOSE) {
				System.out.print(this.clock.currentInstant()
										+ " switch the light off.\n");
			}
		}
	}

	/** future object providing a handle on the task that switches on
	 *  the light.															*/
	protected Future<?>	switchLightOnTaskRef;
	/** future object providing a handle on the task that switches off
	 *  the light.															*/
	protected Future<?>	switchLightOffTaskRef;

	/**
	 * perform the switch light on at the time given by the plan.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void		switchLightOnTask()
	{
		if (this.isInState(State.PLANNED)) {
			this.switchLightOn();
			this.currentState = State.LIGHT_ON;
		} else {
			assert	this.isInState(State.DEACTIVATED);
			this.currentState = State.DEACTIVATED_LIGHT_ON;
		}
	}

	/**
	 * perform the switch light off at the time given by the plan.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void		switchLightOffTask()
	{
		if (this.isInState(State.LIGHT_ON)) {
			this.switchLightOff();
			this.currentState = State.PLANNED;
		} else {
			assert	this.isInState(State.DEACTIVATED_LIGHT_ON);
			this.currentState = State.DEACTIVATED;
		}
	}

	/**
	 * return true if the plan has been scheduled, otherwise false.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the plan has been scheduled, otherwise false.
	 */
	protected boolean	isScheduled()
	{
		return this.switchLightOnTaskRef != null || this.switchLightOffTaskRef != null;
	}

	/**
	 * schedule the next program actions (switching the light on then off).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isScheduled()}
	 * post	{@code isScheduled()}
	 * </pre>
	 * 
	 * @throws Exception <i>to do</i>.
	 */
	protected void		schedulePlan() throws Exception
	{
		assert	!this.isScheduled() :
				new PreconditionException("!isScheduled()");

		long delayInNanos;
		if (!this.currentPlanActive()) {
			if (this.isUnderTest) {
				delayInNanos = this.clock.delayToAcceleratedInstantInNanos(
												this.timeToSwitchLightOn);
			} else {
				delayInNanos =
					(this.timeToSwitchLightOn.toEpochMilli()
										- System.currentTimeMillis()) * 1000000;
			}
			this.switchLightOnTaskRef = 
				this.scheduleTaskOnComponent(
					new AbstractTask() {
						@Override
						public void run() {
							try {
								((IndoorGarden)this.getTaskOwner()).
														switchLightOnTask();
							} catch (Exception e) {
								throw new RuntimeException(e) ;
							}
						}
					},
					delayInNanos,
					TimeUnit.NANOSECONDS);
		} else {
			if (!this.isLightOn()) {
				this.switchLightOn();
			}
		}
		if (this.isUnderTest) {
			delayInNanos = this.clock.delayToAcceleratedInstantInNanos(
													this.timeToSwitchLightOff);
		} else {
			delayInNanos =
					(this.timeToSwitchLightOff.toEpochMilli()
										- System.currentTimeMillis()) * 1000000;
		}
		this.switchLightOffTaskRef =
			this.scheduleTaskOnComponent(
				new AbstractTask() {
					@Override
					public void run() {
						try {
							((IndoorGarden)this.getTaskOwner()).
														switchLightOffTask();
							// schedule the next lighting
							((IndoorGarden)this.getTaskOwner()).
														movePlanToNextDay();
							((IndoorGarden)this.getTaskOwner()).schedulePlan();
						} catch (Exception e) {
							throw new RuntimeException(e) ;
						}
					}
				},
				delayInNanos,
				TimeUnit.NANOSECONDS);

		assert	this.isScheduled() :
				new PostconditionException("isScheduled()");
	}

	/**
	 * move the time to switch light on and off to the next day by adding 24
	 * hours to them.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code clock.currentInstant().isAfter(timeToSwitchLightOff)}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void		movePlanToNextDay()
	{
		assert	clock.currentInstant().isAfter(timeToSwitchLightOff) :
				new PreconditionException(
						"clock.currentInstant().isAfter(timeToSwitchLightOff)");

		if (this.notInStates(new State[]{State.OFF, State.ON})) {
			this.timeToSwitchLightOn =
						this.timeToSwitchLightOn.plusSeconds(24 * 3600);
			this.timeToSwitchLightOff =
						this.timeToSwitchLightOff.plusSeconds(24 * 3600);
		}
	}

	/**
	 * cancel the scheduled actions if any.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isScheduled()}
	 * post	{@code !isScheduled()}
	 * </pre>
	 * 
	 * @throws Exception <i>to do</i>.
	 */
	protected void		cancelSchedule() throws Exception
	{
		assert	this.isScheduled() :
				new PreconditionException("isScheduled()");

		if (this.switchLightOnTaskRef != null && !this.switchLightOnTaskRef.isCancelled()) {
			this.switchLightOnTaskRef.cancel(false);
			this.switchLightOnTaskRef = null;
		}
		if (this.switchLightOffTaskRef != null && !this.switchLightOffTaskRef.isCancelled()) {
			this.switchLightOffTaskRef.cancel(false);
			this.switchLightOffTaskRef = null;
		}

		if (this.isLightOn()) {
			this.switchLightOff();
		}

		assert	!this.isScheduled() :
				new PostconditionException("!isScheduled()");
	}
}
// -----------------------------------------------------------------------------
