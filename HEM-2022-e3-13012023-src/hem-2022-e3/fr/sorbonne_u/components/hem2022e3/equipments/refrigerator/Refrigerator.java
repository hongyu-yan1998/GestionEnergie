/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.refrigerator;

import java.time.Instant;

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
import fr.sorbonne_u.components.hem2022e3.equipments.heater.ThermostatedHeater.SensorData;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.RefrigeratorSensorCI.StateDataI;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.RefrigeratorSensorCI.TemperatureDataI;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.connections.RefrigeratorActuatorInboundPort;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.connections.RefrigeratorInboundPort;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.connections.RefrigeratorSensorDataInboundPort;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.RefrigeratorCoupledModel;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.RefrigeratorRTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.RefrigeratorStateModel;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.RefrigeratorTemperatureModel;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.DoNotRun;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.Run;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.StartRefrigerator;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.StopRefrigerator;
import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.exceptions.PreconditionException;

/**
 * The class <code>Refrigerator</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-03</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
//-----------------------------------------------------------------------------
@OfferedInterfaces(offered={RefrigeratorCI.class,
							DataOfferedCI.PullCI.class,
							RefrigeratorActuatorCI.class})
@RequiredInterfaces(required={DataOfferedCI.PushCI.class,
							  ClockServerCI.class})
//-----------------------------------------------------------------------------
public class Refrigerator 
extends AbstractCyPhyComponent
implements ControlledRefrigeratorImplementationI
{
	// -------------------------------------------------------------------------
	// Inner interfaces and types
	// -------------------------------------------------------------------------

	/**
	 * The enumeration <code>RefrigeratorState</code> describes the operation
	 * states of the refrigerator.
	 */
	protected static enum	RefrigeratorState
	{
		/** refrigerator is on.												*/
		ON,
		/** refrigerator is running											*/
		RUNNING,
		/** refrigerator is off.											*/
		OFF
	}
	
	/**
	 * The class <code>TemperatureData</code> represents the temperature data
	 * that are exchanged between this component and the controller component.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>White-box Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code true}	// no more invariant
	 * </pre>
	 * 
	 * <p><strong>Black-box Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code true}	// no more invariant
	 * </pre>
	 * 
	 * 
	 */
	public static class		TemperatureData
	extends		SensorData
	implements	TemperatureDataI
	{
		private static final long serialVersionUID = 1L;
		protected final double	currentFreezingTemperature;
		protected final double	currentRefrigerationTemperature;
		protected final double	targetFreezingTemperature;
		protected final double	targetRefrigerationTemperature;

		/**
		 * create a temperature data object.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code true}	// no precondition.
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @param currentFreezingTemperature	    current freezing temperature.
		 * @param currentRefrigerationTemperature	current refrigeration temperature.
		 * @param targetFreezingTemperature	        current target freezing temperature.
		 * @param targetRefrigerationTemperature	current target refrigeration temperature.
		 * 
		 * @param time		the time at which the data is returned.
		 */
		public				TemperatureData(
			double currentFreezingTemperature,
			double currentRefrigerationTemperature,
			double targetFreezingTemperature,
			double targetRefrigerationTemperature,
			Instant time
			)
		{
			super(time);

			this.currentFreezingTemperature = currentFreezingTemperature;
			this.currentRefrigerationTemperature = currentRefrigerationTemperature;
			this.targetFreezingTemperature = targetFreezingTemperature;
			this.targetRefrigerationTemperature = targetRefrigerationTemperature;
		}

		@Override
		public double getTargetFreezingTemperature() {
			return this.targetFreezingTemperature;
		}

		@Override
		public double getTargetRefrigerationTemperature() {
			return this.targetRefrigerationTemperature;
		}

		@Override
		public double getCurrentFreezingTemperature() {
			return this.currentFreezingTemperature;
		}

		@Override
		public double getCurrentRefrigerationTemperature() {
			return this.currentRefrigerationTemperature;
		}
		
		@Override
		public String		toString() {
			StringBuffer sb = new StringBuffer(this.getClass().getSimpleName());
			sb.append('[');
			sb.append(this.getTargetFreezingTemperature());
			sb.append(", ");
			sb.append(this.getTargetRefrigerationTemperature());
			sb.append(", ");
			sb.append(this.getCurrentFreezingTemperature());
			sb.append(", ");
			sb.append(this.getCurrentRefrigerationTemperature());
			sb.append(", ");
			sb.append(this.getTime());
			sb.append(']');
			return sb.toString();
		}
	}
	
	/**
	 * The class <code>StateData</code> represents the state data that are
	 * exchanged between this component and the controller component.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>White-box Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code true}	// no more invariant
	 * </pre>
	 * 
	 * <p><strong>Black-box Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code getState() != null}
	 * </pre>
	 */
	public static class		StateData
	extends		SensorData
	implements	StateDataI
	{
		private static final long serialVersionUID = 1L;
		protected final RefrigeratorState	state;

		/**
		 * create a state data object.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code state != null}
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @param state	the current state of the refrigerator.
		 * @param time	the time at which the data is returned.
		 */
		public				StateData(RefrigeratorState state, Instant time)
		{
			super(time);

			assert	state != null : new PreconditionException("value != null");
					
			this.state = state;
		}

		@Override
		public RefrigeratorState	getState()		{ return this.state; }

		@Override
		public String		toString()
		{
			StringBuffer sb = new StringBuffer(this.getClass().getSimpleName());
			sb.append('[');
			sb.append(this.getState());
			sb.append(", ");
			sb.append(this.getTime());
			sb.append(']');
			return sb.toString();
		}
	}
	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** URI of the refrigerator reflection inbound port used in tests.					*/
	public static final String		REFLECTION_INBOUND_PORT_URI =
												"REFRIGERATOR-RIP-URI";
	/** URI of the refrigerator inbound port used in tests.					*/
	public static final String		INBOUND_PORT_URI =
												"REFRIGERATOR-INBOUND-PORT-URI";
	/** URI of the refrigerator sensor inbound port used in tests.					*/
	public static final String		SENSOR_INBOUND_PORT_URI =
												"REFRIGERATOR-SENSOR-INBOUND-PORT-URI";
	/** URI of the refrigerator inbound port used in tests.					*/
	public static final String		ACTUATOR_INBOUND_PORT_URI =
												"REFRIGERATOR-ACTUATOR-INBOUND-PORT-URI";
	/** when 0, no trace is produced, when 1 methods making state changes
	 *  trace their actions and when 2 getter methods also trace their
	 *  actions.															*/
	public static int			VERBOSE = 1;
	/** unless explicitly specified, track this target temperature.			*/
	public static final double	STANDARD_TARGET_FREEZING_TEMPERATURE = -15.0;
	public static final double	STANDARD_TARGET_REFRIGERATION_TEMPERATURE = 2.0;
	
	/** fake current temperature used in tests, when not state from a user
	 *  component.															*/
	public static final double		FAKE_CURRENT_FREEZING_TEMPERATURE = -12.0;	
	public static final double		FAKE_CURRENT_REFRIGERATION_TEMPERATURE = 5.0;

	/** current state (on, running, off) of the refrigerator.								*/
	protected RefrigeratorState		currentState;
	/** target freezing temperature for the running.						*/
	protected double			targetFreezingTemperature;
	/** target refrigeration temperature for the running.					*/
	protected double			targetRefrigerationTemperature;
	
	/** inbound port offering the <code>RefrigeratorCI</code> interface.			*/
	protected RefrigeratorInboundPort			rip;
	/** inbound port offering the <code>RefrigeratorSensorCI</code> interface.			*/
	protected RefrigeratorSensorDataInboundPort	rsip;
	/** inbound port offering the <code>RefrigeratorActuatorCI</code> interface.			*/
	protected RefrigeratorActuatorInboundPort	raip;
	
	// SIL simulation
	/** URI of the pool of threads used to execute the simulator.			*/
	protected static final String			SIM_THREAD_POOL_URI =
														"SIMULATION-POOL";
	/** true if the component executes as a SIL simulation, false otherwise.*/
	protected final boolean					isSimulated;
	/** when true, the execution is a test.									*/
	protected final boolean					isUnderTest;
	/** when true, the execution is a unit test, otherwise an it is an
	 *  integration test.													*/
	protected final boolean					isUnitTest;
	/** URI of the simulation architecture to be created or the empty string
	 *  if the component does not execute as a SIL simulation.				*/
	protected final String					simArchitectureURI;
	/** simulator plug-in that holds the SIL simulator for this component.	*/
	protected RefrigeratorRTAtomicSimulatorPlugin	simulatorPlugin;
	/** acceleration factor to be used when running the real time
	 *  simulation.															*/
	protected final double					accFactor;

	/** outbound port to connect to the centralised clock server.			*/
	protected ClockServerOutboundPort		clockServerOBP;
	/** URI of the clock to be used to synchronise the test scenarios and
	 *  the simulation.														*/
	protected final String					clockURI;
	/** accelerated clock used to implement the simulation scenario.		*/
	protected AcceleratedClock				clock;
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------


	protected Refrigerator() throws Exception
	{
		this(false, false, false, null, 1.0, null);
	}


	protected Refrigerator(
			boolean isUnderTest,
			boolean isUnitTest,
			boolean isSimulated,
			String simArchitectureURI,
			double accFactor,
			String clockURI
			) throws Exception
	{
		this(INBOUND_PORT_URI, SENSOR_INBOUND_PORT_URI,
				 ACTUATOR_INBOUND_PORT_URI,
				 isUnderTest, isUnitTest,
				 isSimulated, simArchitectureURI, accFactor, clockURI);
	}
	
	protected Refrigerator(
			String refrigeratorInboundPortURI,
			String refrigeratorSensorInboundPortURI,
			String refrigeratorActuatorInboundPortURI
			) throws Exception
		{
			this(refrigeratorInboundPortURI, 
				 refrigeratorSensorInboundPortURI,
			     refrigeratorActuatorInboundPortURI,
				 false, false, false, null, 1.0, null);
		}

	protected Refrigerator(
			String refrigeratorInboundPortURI,
			String refrigeratorSensorInboundPortURI,
			String refrigeratorActuatorInboundPortURI,
			boolean isUnderTest,
			boolean isUnitTest,
			boolean isSimulated,
			String simArchitectureURI,
			double accFactor,
			String clockURI
			) throws Exception
	{
		super(REFLECTION_INBOUND_PORT_URI, 1, 1);
		

		assert	refrigeratorInboundPortURI != null && !refrigeratorInboundPortURI.isEmpty() :
				new PreconditionException(
						"refrigeratorInboundPortURI != null && "
						+ "!refrigeratorInboundPortURI.isEmpty()");
		assert	refrigeratorSensorInboundPortURI != null &&
										!refrigeratorSensorInboundPortURI.isEmpty() :
				new PreconditionException(
						"refrigeratorSensorInboundPortURI != null && "
						+ "!refrigeratorSensorInboundPortURI.isEmpty()");
		assert	refrigeratorActuatorInboundPortURI != null &&
										!refrigeratorActuatorInboundPortURI.isEmpty() :
				new PreconditionException(
						"refrigeratorActuatorInboundPortURI != null && "
						+ "!refrigeratorActuatorInboundPortURI.isEmpty()");
		assert	!isSimulated || simArchitectureURI != null &&
												!simArchitectureURI.isEmpty() :
				new PreconditionException(
						"!isSimulated || "
						+ "simArchitectureURI != null && "
						+ "!simArchitectureURI.isEmpty()");
		assert	accFactor > 0.0 : new PreconditionException("accFactor > 0.0");
		assert	!isUnderTest || clockURI != null && !clockURI.isEmpty() :
				new PreconditionException(
						"isUnderTest || "
						+ "clockURI != null && !clockURI.isEmpty()");

		this.isUnderTest = isUnderTest;
		this.isUnitTest = isUnitTest;
		this.isSimulated = isSimulated;
		this.simArchitectureURI = simArchitectureURI;
		this.accFactor = accFactor;
		this.clockURI = clockURI;
		this.targetFreezingTemperature = STANDARD_TARGET_FREEZING_TEMPERATURE;
		this.targetRefrigerationTemperature = STANDARD_TARGET_REFRIGERATION_TEMPERATURE;

		this.rip = new RefrigeratorInboundPort(refrigeratorInboundPortURI, this);
		this.rip.publishPort();
		this.rsip =
			new RefrigeratorSensorDataInboundPort(refrigeratorSensorInboundPortURI, this);
		this.rsip.publishPort();
		this.raip =
			new RefrigeratorActuatorInboundPort(refrigeratorActuatorInboundPortURI, this);
		this.raip.publishPort();

		if (VERBOSE > 0) {
			this.tracer.get().setTitle("Refrigerator component");
			this.tracer.get().setRelativePosition(0, 2);
			this.toggleTracing();		
		}
	}
	
	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------
	@Override
	public synchronized void	start() throws ComponentStartException
	{
		super.start();

		// initialisations
		this.currentState = RefrigeratorState.OFF;
		this.targetFreezingTemperature = STANDARD_TARGET_FREEZING_TEMPERATURE;
		this.targetRefrigerationTemperature = STANDARD_TARGET_REFRIGERATION_TEMPERATURE;

		try {
			if (this.isUnderTest) {
				this.clockServerOBP = new ClockServerOutboundPort(this);
				this.clockServerOBP.publishPort();
				this.doPortConnection(
						this.clockServerOBP.getPortURI(),
						ClockServer.STANDARD_INBOUNDPORT_URI,
						ClockServerConnector.class.getCanonicalName());
				if (this.isSimulated) {
					// create the simulation plug-in, initialise and then install it
					this.simulatorPlugin = new RefrigeratorRTAtomicSimulatorPlugin();
					this.simulatorPlugin.setPluginURI(RefrigeratorCoupledModel.URI);
					this.simulatorPlugin.setSimulationExecutorService(
											STANDARD_SCHEDULABLE_HANDLER_URI);
					try {
						this.simulatorPlugin.initialiseSimulationArchitecture(
								this.simArchitectureURI,
								this.accFactor,
								this.isUnitTest);
						this.installPlugin(this.simulatorPlugin);
					} catch (Exception e) {
						throw new ComponentStartException(e) ;
					}
				}
			}
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
		this.traceMessage("start.\n");
	}

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

	@Override
	public synchronized void	finalise() throws Exception
	{
		this.doPortDisconnection(this.clockServerOBP.getPortURI());
		super.finalise();
	}

	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		this.traceMessage("shutdown.\n");
		try {
			this.rip.unpublishPort();
			this.rsip.unpublishPort();
			this.raip.unpublishPort();
			this.clockServerOBP.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
	
	// -------------------------------------------------------------------------
	// Internal methods
	// -------------------------------------------------------------------------

	/**
	 * return true if the Refrigerator is running.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the refrigerator is running.
	 */
	public boolean		internalIsRunning()
	{
		return this.currentState == RefrigeratorState.ON ||
									this.currentState == RefrigeratorState.RUNNING;
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	@Override
	public boolean isRunning() throws Exception {
		if (VERBOSE > 1) {
			this.traceMessage("state: " + this.currentState + ".\n");
		}
		return this.internalIsRunning();
	}

	@Override
	public void startRefrigerator() throws Exception {
		if (VERBOSE > 0) {
			this.traceMessage("refrigerator starts. \n");
		}
		
		assert	!this.internalIsRunning();
		this.currentState = RefrigeratorState.ON;	
		
		if (this.isSimulated) {
			// trigger an immediate StartRefrigerator event on the RefrigeratorStateModel,
			// which in turn will emit this event towards the other models of
			// the refrigerator
			// the t parameter in the lambda expression represents the current
			// simulation time to be provided by he simulator before passing
			// the event instance to the model
			this.simulatorPlugin.triggerExternalEvent(
					RefrigeratorStateModel.URI,
					t -> new StartRefrigerator(t));
		} else {
			// In a fully operational implementation of an embedded software,
			// this method would call the physical actuator turning on the refrigerator.
			// As we are not developing for an actual device, we only set the new
			// state in the component.
		}

		// force a push of the new state towards the controller
		this.refrigeratorStateSensor();
	}

	@Override
	public void stopRefrigerator() throws Exception {
		if (VERBOSE > 0) {
			this.traceMessage("refrigerator stops.\n");
		}
		assert	this.internalIsRunning();

		this.currentState = RefrigeratorState.OFF;
		
		if (this.isSimulated) {
			// trigger an immediate StopRefrigerator event on the RefrigeratorStateModel,
			// which in turn will emit this event towards the other models of
			// the refrigerator
			// the t parameter in the lambda expression represents the current
			// simulation time to be provided by he simulator before passing
			// the event instance to the model
			this.simulatorPlugin.triggerExternalEvent(
					RefrigeratorStateModel.URI,
					t -> new StopRefrigerator(t));
		} else {
			// In a fully operational implementation of an embedded software,
			// this method would call the physical actuator turning on the refrigerator.
			// As we are not developing for an actual device, we only set the new
			// state in the component.
		}

		// force a push of the new state towards the controller
		this.refrigeratorStateSensor();		
	}

	@Override
	public void setTargetFreezingTemperature(double target) throws Exception {
		if (VERBOSE > 0) {
			this.traceMessage("Refrigerator sets a new target freezing temperature: " 
								+ target + ".\n");
		}
		
		assert	this.internalIsRunning();
		assert  target >= -26.0 && target <= -18.0;
		
		this.targetFreezingTemperature = target;		
	}

	@Override
	public double getTargetFreezingTemperature() throws Exception {
		if (VERBOSE > 1) {
			this.traceMessage("Refrigerator returns its target freezing temperature: " 
								+ this.targetFreezingTemperature + ".\n");
		}
		
		return this.targetFreezingTemperature;
	}

	@Override
	public void setTargetRefrigerationTemperature(double target) throws Exception {
		if (VERBOSE > 0) {
			this.traceMessage("Refrigerator sets a new target refrigeration temperature: " 
								+ target + ".\n");
		}
		
		assert	this.internalIsRunning();
		assert  target >= 1.0 && target <= 10.0;
		
		this.targetRefrigerationTemperature = target;		
	}

	@Override
	public double getTargetRefrigerationTemperature() throws Exception {
		if (VERBOSE > 1) {
			this.traceMessage("Refrigerator returns its target refrigeration temperature: " 
								+ this.targetRefrigerationTemperature + ".\n");
		}
		
		return this.targetRefrigerationTemperature;
	}

	@Override
	public double getCurrentFreezingTemperature() throws Exception {
		double currentTemperature = 0.0;
		if (this.isSimulated) {
			currentTemperature =
					(double) this.simulatorPlugin.getModelStateValue(
							RefrigeratorTemperatureModel.URI, 
							RefrigeratorRTAtomicSimulatorPlugin.CURRENT_FREEZING_TERMPERATURE);
		} else {
			currentTemperature = FAKE_CURRENT_FREEZING_TEMPERATURE;
		}
		
		if (VERBOSE > 1) {
			this.traceMessage("Refrigerator returns the current freezing temperature: " 
								+ currentTemperature + ".\n");
		}
		return currentTemperature;
	}

	@Override
	public double getCurrentRefrigerationTemperature() throws Exception {
		double currentTemperature = 0.0;
		
		if (this.isSimulated) {
			currentTemperature =
					(double) this.simulatorPlugin.getModelStateValue(
							RefrigeratorTemperatureModel.URI, 
							RefrigeratorRTAtomicSimulatorPlugin.CURRENT_REFRIGERATION_TERMPERATURE);
		} else {
			currentTemperature = FAKE_CURRENT_REFRIGERATION_TEMPERATURE;
		}
		;
		if (VERBOSE > 1) {
			this.traceMessage("Refrigerator returns the current refrigeration temperature: " 
								+ currentTemperature + ".\n");
		}
		return currentTemperature;
	}


	@Override
	public void refrigeratorStateSensor() throws Exception {
		Instant i = null;
		if (this.isSimulated) {
			i = this.simulatorPlugin.getCurrentInstant();
		} else if (this.isUnderTest) {
			i = this.clock.currentInstant();
		} else {
			i = Instant.now();
		}

		this.rsip.send(new StateData(this.currentState, i));		
	}

	@Override
	public TemperatureDataI currentTemperaturesSensor() throws Exception {
		Instant i = null;
		if (this.isSimulated) {
			i = this.simulatorPlugin.getCurrentInstant();
		} else if (this.isUnderTest) {
			i = this.clock.currentInstant();
		} else {
			i = Instant.now();
		}

		return new TemperatureData(this.getCurrentFreezingTemperature(),
								   this.getCurrentRefrigerationTemperature(),
								   this.getTargetFreezingTemperature(),
								   this.getTargetRefrigerationTemperature(),
								   i);
	}

	@Override
	public void run() throws Exception {
		if (VERBOSE > 0) {
			this.traceMessage("refrigerator starts running.\n");
		}

		this.currentState = RefrigeratorState.RUNNING;

		// When SIL simulated, actions that change the state of the refrigerator must
		// be transmitted to the simulation models in order to keep the
		// component and simulation states in synchronisation.
		if (this.isSimulated) {
			try {
				// trigger an immediate Run event on the RefrigeratorStateModel,
				// which in turn will emit this event towards the other models
				// of the refrigerator
				// the t parameter in the lambda expression represents the
				// current simulation time to be provided by he simulator before
				// passing the event instance to the model
				this.simulatorPlugin.triggerExternalEvent(
						RefrigeratorStateModel.URI,
						t -> new Run(t));
			} catch (Exception e) {
				throw new RuntimeException(e) ;
			}
		} else {
			// In a fully operational implementation of an embedded software,
			// this method would call the physical actuator switching on the
			// running on the refrigerator. As we are not developing for an actual
			// device, we only set the new state in the component.
		}

		// force a push of the new state data towards the controller
		try {
			this.refrigeratorStateSensor();
		} catch (Exception e1) {
			throw new RuntimeException(e1) ;
		}		
	}

	@Override
	public void doNotRun() throws Exception {
		if (VERBOSE > 0) {
			this.traceMessage("refrigerator stops running.\n");
		}

		this.currentState = RefrigeratorState.ON;

		// When SIL simulated, actions that change the state of the refrigerator must
		// be transmitted to the simulation models in order to keep the
		// component and simulation states in synchronisation.
		if (this.isSimulated) {
			try {
				// trigger an immediate DoNotRun event on the RefrigeratorStateModel,
				// which in turn will emit this event towards the other models
				// of the refrigerator
				// the t parameter in the lambda expression represents the
				// current simulation time to be provided by he simulator before
				// passing the event instance to the model
				this.simulatorPlugin.triggerExternalEvent(
						RefrigeratorStateModel.URI,
						t -> new DoNotRun(t));
			} catch (Exception e) {
				throw new RuntimeException(e) ;
			}
		} else {
			// In a fully operational implementation of an embedded software,
			// this method would call the physical actuator switching off the
			// running on the refrigerator. As we are not developing for an actual
			// device, we only set the new state in the component.
		}

		// force a push of the new state data towards the controller
		try {
			this.refrigeratorStateSensor();
		} catch (Exception e1) {
			throw new RuntimeException(e1) ;
		}		
	}
}
