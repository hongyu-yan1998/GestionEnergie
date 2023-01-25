package fr.sorbonne_u.components.hem2022e3.equipments.airconditioner;

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
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.connections.AirConditionerInboundPort;
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.sil.AirConditionerCoupledModel;
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.sil.AirConditionerRTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.sil.AirConditionerStateModel;
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.sil.events.TurnOffAirConditioner;
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.sil.events.TurnOnAirConditioner;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;

/**
 * The class <code>Airconditioner.java</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2022-12-08</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
//-----------------------------------------------------------------------------
@OfferedInterfaces(offered = {AirConditionerCI.class})
@RequiredInterfaces(required={ClockServerCI.class})
//-----------------------------------------------------------------------------
public class AirConditioner 
extends AbstractCyPhyComponent 
implements AirConditionerImplementationI 
{
	// -------------------------------------------------------------------------
	// Inner classes and types
	// -------------------------------------------------------------------------
	/**
	 * The enumeration <code>State</code> describes the discrete states or
	 * modes of the air conditioner.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * The air conditioner can be <code>OFF</code> or <code>ON</code>, and then it is either
	 * in <code>LIGHT_OFF</code> mode (the light is off) or in
	 * <code>LIGHT_ON</code> mode (the air conditioner is on).
	 * 
	 */
	public static enum State {
		OFF,
		/** on, but not planned												*/
		ON,
		/** air conditioner on and off is planned.							*/
		PLANNED,	
//		/** appliance is on, planned and the air conditioner is on.			*/
//		PLANNED_ON,
		/** appliance is on, planned but plan is deactivated, hence the
		 *  air conditioner is off and remains off if reactivated.					*/
		DEACTIVATED,
		/** appliance is on, planned and air conditioner was on but plan is
		 *  deactivated, hence the air conditioner is off but will be on if
		 *  reactivated.													*/
		DEACTIVATED_ON
	}
	
	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	/** URI of the air conditioner reflection inbound port used in tests.	*/
	public static final String	REFLECTION_INBOUND_PORT_URI = "AC-RIP-URI";	
	/** a convenient string to be used as inbound port URI prefix.			*/
	public static final String	INBOUND_PORT_URI_PREFIX = "air-conditioner-ibp";
	/** when true, methods trace their actions.								*/
	public static final boolean	VERBOSE = true;
	
	/** the inbound port of the component.									*/	
	protected AirConditionerInboundPort				inboundPort;

	/** current state of the air conditioner.								*/
	protected State									currentState;
	/** if the running plan has been defined, the time at which the air conditioner
	 *  must be turned on.												*/
	protected Instant								timeToTurnOn;
	/** if the running plan has been defined, the time at which the air conditioner
	 *  must be turned off.												*/
	protected Instant								timeToTurnOff;

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
	protected AirConditionerRTAtomicSimulatorPlugin	simulatorPlugin;
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

	protected AirConditioner(
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
		this.inboundPort = new AirConditionerInboundPort(
				AirConditioner.INBOUND_PORT_URI_PREFIX, this);
		this.inboundPort.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Air conditioner component");
			this.tracer.get().setRelativePosition(2, 1);
			this.toggleTracing();		
		}

		assert	!this.isOn();
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	@Override
	public synchronized void start() throws ComponentStartException {
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
									new AirConditionerRTAtomicSimulatorPlugin();
					this.simulatorPlugin.setPluginURI(
							this.isUnitTesting ?
								AirConditionerCoupledModel.URI
							:	AirConditionerStateModel.URI);
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

	@Override
	public void execute() throws Exception {
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
	
	@Override
	public synchronized void finalise() throws Exception {
		this.doPortDisconnection(this.clockServerOBP.getPortURI());
		super.finalise();
	}

	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
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

	@Override
	public boolean isOn() throws Exception {
		if (VERBOSE) {
			this.traceMessage("air conditioner is on? " 
								+ this.isInState(State.ON)
								+ ".\n");
		}

		return this.isInState(State.ON);
	}

	@Override
	public void on() throws Exception {
		assert	!isOn() : new PreconditionException("!isOn()");
		
		if (VERBOSE) {
			this.traceMessage("air conditioner is turned on.\n");
		}
		
		this.turnOn();
		this.currentState = State.ON;
		
		assert	isOn() : new PostconditionException("isOn()");
	}

	@Override
	public void off() throws Exception {
		assert isOn() : new PreconditionException("isOn()");

		if (VERBOSE) {
			this.traceMessage("turned off.\n");
		}

		this.turnOff();
		this.currentState = State.OFF;

		assert !isOn() : new PostconditionException("!isOn()");
	}

	@Override
	public boolean isAirConditionerPlanned() throws Exception {
		if (VERBOSE) {
			this.traceMessage("is air conditioner planned: "
								+  this.notInStates(new State[]{
														State.OFF})
							  	+ ".\n");
		}

		return this.notInStates(new State[]{State.OFF});
	}

	@Override
	public void plan(Instant timeOn, Instant timeOff) throws Exception {
		assert	!isAirConditionerPlanned() :
			new PreconditionException("!isAirConditionerPlanned()");
		assert	timeOn != null : new PreconditionException("timeOn != null");
		assert	timeOff != null : new PreconditionException("timeOff != null");
		assert	timeOff.isAfter(timeOn) :
				new PreconditionException("timeOff.isAfter(timeOn)");
		
		if (VERBOSE) {
			this.traceMessage("plan air conditioner from " + timeOn + " to " + timeOff + ".\n");
		}
		
		this.timeToTurnOn = timeOn;
		this.timeToTurnOff = timeOff;
		if(this.currentPlanActive()) {
			this.currentState = State.ON;
		} else {
			this.currentState = State.PLANNED;
		}
		this.schedulePlan();
		
		assert  isAirConditionerPlanned() :
			new PostconditionException("isAirConditionerPlanned()");
		assert	!isDeactivated() :
			new PostconditionException("!isDeactivated()");   
	}

	@Override
	public void cancelPlan() throws Exception {
		assert  isAirConditionerPlanned() :
			new PostconditionException("isAirConditionerPlanned()");
		
		if (VERBOSE) {
			this.traceMessage("cancelled plan at " +
							  this.clock.currentInstant() + ".\n");
		}
		
		if (this.isOn()) {
			this.turnOff();
		}
		
		this.currentState = State.OFF;
		this.timeToTurnOn = null;
		this.timeToTurnOff = null;
		this.cancelSchedule();
		
		assert	!isAirConditionerPlanned() :
			new PreconditionException("!isAirConditionerPlanned()");
		assert	!isOn() : new PostconditionException("!isOn()");
	}

	@Override
	public Instant getPlannedAirConditionerOn() throws Exception {
		assert	isAirConditionerPlanned() :
			new PreconditionException("isAirConditionerPlanned()");
		
		if (VERBOSE) {
			this.traceMessage("planned air conditioner on at " +
					this.timeToTurnOn + ".\n");
		}
		
		return this.timeToTurnOn;
	}

	@Override
	public Instant getPlannedAirConditionerOff() throws Exception {
		assert	isAirConditionerPlanned() :
			new PreconditionException("isAirConditionerPlanned()");
		
		if (VERBOSE) {
			this.traceMessage("planned air conditioner off at " +
					this.timeToTurnOff + ".\n");
		}
		
		return this.timeToTurnOff;		
	}

	@Override
	public boolean isDeactivated() throws Exception {
		if (VERBOSE) {
			this.traceMessage("is deactivated? "
								+ this.isInStates(new State[]{
										State.DEACTIVATED,
										State.DEACTIVATED_ON})
								+ ".\n");
		}

		return this.isInStates(new State[]{State.DEACTIVATED,
										   State.DEACTIVATED_ON});
	}

	@Override
	public void deactivate() throws Exception {
		assert	isAirConditionerPlanned() : new PreconditionException("isAirConditionerPlanned()");
		assert	!isDeactivated() : new PreconditionException("!isDeactivated()");
		
		if (VERBOSE) {
			this.traceMessage("deactivating at " +
							  this.clock.currentInstant() + ".\n");
		}
		
		if (this.isOn()) {
			this.turnOff();
		}
		if (this.isInState(State.PLANNED)) {
			this.currentState = State.DEACTIVATED;
		} else {
			assert	 this.isInState(State.ON);
			this.currentState = State.DEACTIVATED_ON;
		}
		
		assert	isDeactivated() : new PostconditionException("isDeactivated()");
		assert	!isOn() : new PostconditionException("!isOn()");
	}

	@Override
	public void reactivate() throws Exception {
		assert	isAirConditionerPlanned() : new PreconditionException("isAirConditionerPlanned()");
		assert	isDeactivated() : new PreconditionException("isDeactivated()");
		
		if (VERBOSE) {
			this.traceMessage(
					"reactivating at " + this.clock.currentInstant() + ".\n");
		}
		
		this.currentState = State.PLANNED;
		if (this.currentPlanActive() && !this.isOn()) {
			this.turnOn();
			this.currentState = State.ON;
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
		return now.isAfter(this.timeToTurnOn) &&
									now.isBefore(this.timeToTurnOff);
	}

	/**
	 * physically turn on the air conditioner (actuator implementation).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void		turnOn()
	{
		this.traceMessage("turn the air conditioner on.\n");
		if (this.isSimulated) {
			try {
				// trigger an immediate TurnOnAirConditioner event on the
				// AirConditionerStateModel, which in turn will emit this event
				// towards the other models of the air conditioner
				// the t parameter in the lambda expression represents the
				// current simulation time to be provided by he simulator before
				// passing the event instance to the model
				this.simulatorPlugin.triggerExternalEvent(
											AirConditionerStateModel.URI,
											t -> new TurnOnAirConditioner(t));
			} catch (Exception e) {
				throw new RuntimeException(e) ;
			}
		} else {
			// In a fully operational implementation of an embedded software,
			// this method would call the physical actuator switching on the
			// air conditioner. As we are not developing for an actual device,
			// nothing is done.
			if (VERBOSE) {
				System.out.print(this.clock.currentInstant()
										+ " turn the air conditioner on.\n");
			}
		}
	}

	/**
	 * physically turn off the air conditioner (actuator implementation).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void		turnOff()
	{
		this.traceMessage("turn the air conditioner off.\n");
		if (this.isSimulated) {
			try {
				// trigger an immediate TurnOffAirConditioner event on the
				// AirConditionerStateModel, which in turn will emit this event
				// towards the other models of the air conditioner
				// the t parameter in the lambda expression represents the
				// current simulation time to be provided by he simulator before
				// passing the event instance to the model
				this.simulatorPlugin.triggerExternalEvent(
											AirConditionerStateModel.URI,
											t -> new TurnOffAirConditioner(t));
			} catch (Exception e) {
				throw new RuntimeException(e) ;
			}
		} else {
			// In a fully operational implementation of an embedded software,
			// this method would call the physical actuator switching off the
			// air conditioner. As we are not developing for an actual device,
			// nothing is done.
			if (VERBOSE) {
				System.out.print(this.clock.currentInstant()
										+ " turn the air conditioner off.\n");
			}
		}
	}
	
	/** future object providing a handle on the task that turns on
	 *  the air conditioner.															*/
	protected Future<?>	turnAirConditionerOnTaskRef;
	/** future object providing a handle on the task that turns off
	 *  the air conditioner.															*/
	protected Future<?>	turnAirConditionerOffTaskRef;
	
	/**
	 * perform the turn air conditioner on at the time given by the plan.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void		turnAirConditionerOnTask()
	{
		if (this.isInState(State.PLANNED)) {
			this.turnOn();
			this.currentState = State.ON;
		} else {
			assert	this.isInState(State.DEACTIVATED);
			this.currentState = State.DEACTIVATED_ON;
		}
	}
	
	/**
	 * perform the turn air conditioner off at the time given by the plan.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void		turnAirConditionerOffTask()
	{
		if (this.isInState(State.ON)) {
			this.turnOff();
			this.currentState = State.PLANNED;
		} else {
			assert	this.isInState(State.DEACTIVATED_ON);
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
		return this.turnAirConditionerOnTaskRef != null || this.turnAirConditionerOffTaskRef != null;
	}
	
	/**
	 * schedule the next program actions (turning the air conditioner on then off).
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
												this.timeToTurnOn);
			} else {
				delayInNanos =
					(this.timeToTurnOn.toEpochMilli()
										- System.currentTimeMillis()) * 1000000;
			}
			this.turnAirConditionerOnTaskRef = 
				this.scheduleTaskOnComponent(
					new AbstractTask() {
						@Override
						public void run() {
							try {
								((AirConditioner)this.getTaskOwner()).
										turnAirConditionerOnTask();
							} catch (Exception e) {
								throw new RuntimeException(e) ;
							}
						}
					},
					delayInNanos,
					TimeUnit.NANOSECONDS);
		} else {
			if (!this.isOn()) {
				this.turnOn();
			}
		}
		if (this.isUnderTest) {
			delayInNanos = this.clock.delayToAcceleratedInstantInNanos(
													this.timeToTurnOff);
		} else {
			delayInNanos =
					(this.timeToTurnOff.toEpochMilli()
										- System.currentTimeMillis()) * 1000000;
		}
		this.turnAirConditionerOffTaskRef =
			this.scheduleTaskOnComponent(
				new AbstractTask() {
					@Override
					public void run() {
						try {
							((AirConditioner)this.getTaskOwner()).
														turnAirConditionerOffTask();
							// schedule the next one
							((AirConditioner)this.getTaskOwner()).
														movePlanToNextDay();
							((AirConditioner)this.getTaskOwner()).schedulePlan();
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
	 * move the time to turn air conditioner on and off to the next day by adding 24
	 * hours to them.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code clock.currentInstant().isAfter(timeToTurnOff)}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void		movePlanToNextDay()
	{
		assert	clock.currentInstant().isAfter(timeToTurnOff) :
				new PreconditionException(
						"clock.currentInstant().isAfter(timeToTurnOff)");

		if (this.notInState(State.OFF)) {
			this.timeToTurnOn =
						this.timeToTurnOn.plusSeconds(24 * 3600);
			this.timeToTurnOff =
						this.timeToTurnOff.plusSeconds(24 * 3600);
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

		if (this.turnAirConditionerOnTaskRef != null && !this.turnAirConditionerOnTaskRef.isCancelled()) {
			this.turnAirConditionerOnTaskRef.cancel(false);
			this.turnAirConditionerOnTaskRef = null;
		}
		if (this.turnAirConditionerOffTaskRef != null && !this.turnAirConditionerOffTaskRef.isCancelled()) {
			this.turnAirConditionerOffTaskRef.cancel(false);
			this.turnAirConditionerOffTaskRef = null;
		}

		if (this.isOn()) {
			this.turnOff();
		}

		assert	!this.isScheduled() :
				new PostconditionException("!isScheduled()");
	}
}
