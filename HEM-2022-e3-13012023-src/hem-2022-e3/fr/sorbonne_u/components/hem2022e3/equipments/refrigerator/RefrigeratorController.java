/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.refrigerator;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.connectors.DataConnector;
import fr.sorbonne_u.components.cyphy.tools.aclocks.AcceleratedClock;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServer;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerCI;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerConnector;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerOutboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.Refrigerator.RefrigeratorState;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.RefrigeratorSensorCI.StateDataI;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.RefrigeratorSensorCI.TemperatureDataI;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.connections.RefrigeratoActuatorConnector;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.connections.RefrigeratorActuatorOutboundPort;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.connections.RefrigeratorSensorDataOutboundPort;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.exceptions.PreconditionException;

/**
 * The class <code>RefrigeratorController</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-04</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
//-----------------------------------------------------------------------------
@RequiredInterfaces(required={DataRequiredCI.PullCI.class,
							  RefrigeratorActuatorCI.class,
							  ClockServerCI.class})
@OfferedInterfaces(offered={DataRequiredCI.PushCI.class})
//-----------------------------------------------------------------------------
public class RefrigeratorController 
extends AbstractComponent 
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** when true, some methods trace their actions.						*/
	protected static boolean		VERBOSE = true;
	/** when true, some methods trace their actions.						*/
	public static boolean			DEBUG = false;

	/** the standard hysteresis used by the controller.						*/
	public static final double	STANDARD_HYSTERESIS = 0.1;
	/** standard control period in seconds.									*/
	public static final double	STANDARD_CONTROL_PERIOD = 60.0;

	/** URI of the sensor inbound port on the {@code Refrigerator}.	*/
	protected String								sensorIBP_URI;
	/** URI of the actuator inbound port on the {@code Refrigerator}.	*/
	protected String								actuatorIBPURI;
	/** sensor data outbound port connected to the
	 *  {@code Refrigerator}.											*/
	protected RefrigeratorSensorDataOutboundPort		sensorOutboundPort;
	/** actuator outbound port connected to the {@code Refrigerator}.	*/
	protected RefrigeratorActuatorOutboundPort			actuatorOutboundPort;

	/** the actual hysteresis used in the control loop.						*/
	protected double								hysteresis;
	/* user set control period in seconds.									*/
	protected double								controlPeriod;
	/* actual control period, either in pure real time (not under test)
	 * or in accelerated time (under test), expressed in nanoseconds;
	 * used for scheduling the control task.								*/
	protected long									actualControlPeriod;

	/** when true, the run is done in test mode.							*/
	protected boolean								isUnderTest;
	/** outbound port to connect to the centralised clock server.			*/
	protected ClockServerOutboundPort				clockServerOBP;
	/** URI of the clock to be used to synchronise the test scenarios and
	 *  the simulation.														*/
	protected final String							clockURI;
	/** accelerated clock governing the timing of actions in the test
	 *  scenarios.															*/
	protected final CompletableFuture<AcceleratedClock>		clock;

	/** the current state of the refrigerator as perceived through the sensor
	 *  data received from the {@code Refrigerator}.					*/
	protected final AtomicReference<RefrigeratorState>	currentState;
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create the controller component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code sensorIBP_URI != null && !sensorIBP_URI.isEmpty()}
	 * pre	{@code actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param sensorIBP_URI		URI of the refrigerator sensor inbound port.
	 * @param actuatorIBP_URI	URI of the refrigerator actuator inbound port.
	 * @throws Exception		<i>to do</i>.
	 */
	protected			RefrigeratorController(
		String sensorIBP_URI,
		String actuatorIBP_URI
		) throws Exception
	{
		this(sensorIBP_URI, actuatorIBP_URI,
			 STANDARD_HYSTERESIS, STANDARD_CONTROL_PERIOD);
	}

	/**
	 * create the controller component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code sensorIBP_URI != null && !sensorIBP_URI.isEmpty()}
	 * pre	{@code actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty()}
	 * pre	{@code hysteresis > 0.0}
	 * pre	{@code controlPeriod > 0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param sensorIBP_URI		URI of the refrigerator sensor inbound port.
	 * @param actuatorIBP_URI	URI of the refrigerator actuator inbound port.
	 * @param hysteresis		control hysteresis around the target temperature.
	 * @param controlPeriod		control period in seconds.
	 * @throws Exception 		<i>to do</i>.
	 */
	protected			RefrigeratorController(
		String sensorIBP_URI,
		String actuatorIBP_URI,
		double hysteresis,
		double controlPeriod
		) throws Exception
	{
		this(sensorIBP_URI, actuatorIBP_URI, hysteresis, controlPeriod,
			 false, null);
	}

	/**
	 * create the controller component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code sensorIBP_URI != null && !sensorIBP_URI.isEmpty()}
	 * pre	{@code actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty()}
	 * pre	{@code hysteresis > 0.0}
	 * pre	{@code controlPeriod > 0}
	 * pre	{@code !isUnderTest || clockURI != null && !clockURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param sensorIBP_URI		URI of the refrigerator sensor inbound port.
	 * @param actuatorIBP_URI	URI of the refrigerator actuator inbound port.
	 * @param hysteresis		control hysteresis around the target temperature.
	 * @param controlPeriod		control period in seconds.
	 * @param isUnderTest		when true, the component is executed in test mode.
	 * @param clockURI			URI of the clock to be used to synchronise the test scenarios and the simulation.
	 * @throws Exception 		<i>to do</i>.
	 */
	protected			RefrigeratorController(
		String sensorIBP_URI,
		String actuatorIBP_URI,
		double hysteresis,
		double controlPeriod,
		boolean isUnderTest,
		String clockURI
		) throws Exception
	{
		// two standard threads in case the thread that runs the method execute
		// can be prevented to run by the thread running receiveRunningState
		// the schedulable thread pool is used to run the control task
		super(2, 1);

		assert	sensorIBP_URI != null && !sensorIBP_URI.isEmpty() :
				new PreconditionException(
						"sensorIBP_URI != null && !sensorIBP_URI.isEmpty()");
		assert	actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty() :
				new PreconditionException(
					"actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty()");
		assert	hysteresis > 0.0 :
				new PreconditionException("hysteresis > 0.0");
		assert	controlPeriod > 0 :
				new PreconditionException("controlPeriod > 0");
		assert	!isUnderTest || clockURI != null && !clockURI.isEmpty() :
				new PreconditionException(
						"!isUnderTest || "
						+ "clockURI != null && !clockURI.isEmpty()");

		this.sensorIBP_URI = sensorIBP_URI;
		this.actuatorIBPURI = actuatorIBP_URI;
		this.hysteresis = hysteresis;
		this.controlPeriod = controlPeriod;
		this.isUnderTest = isUnderTest;
		this.clockURI = clockURI;
		this.clock = new CompletableFuture<>();

		// just a common initialisation; if the run is in test mode, the
		// acceleration factor will be taken into account at start time
		// first convert to nanoseconds before casting to get a better
		// precision for fractional control periods
		this.actualControlPeriod =
				(long) (this.controlPeriod * TimeUnit.SECONDS.toNanos(1));
		this.currentState = new AtomicReference<RefrigeratorState>();

		this.sensorOutboundPort =
				new RefrigeratorSensorDataOutboundPort(this);
		this.sensorOutboundPort.publishPort();
		this.actuatorOutboundPort =
				new RefrigeratorActuatorOutboundPort(this);
		this.actuatorOutboundPort.publishPort();

		if (VERBOSE || DEBUG) {
			this.tracer.get().setTitle("refrigerator controller component");
			this.tracer.get().setRelativePosition(1, 2);
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

		try {
			this.doPortConnection(this.sensorOutboundPort.getPortURI(),
								  sensorIBP_URI,
								  DataConnector.class.getCanonicalName());
			this.doPortConnection(
					this.actuatorOutboundPort.getPortURI(),
					this.actuatorIBPURI,
					RefrigeratoActuatorConnector.class.getCanonicalName());

			if (this.isUnderTest) {
				this.clockServerOBP = new ClockServerOutboundPort(this);
				this.clockServerOBP.publishPort();
				this.doPortConnection(
						this.clockServerOBP.getPortURI(),
						ClockServer.STANDARD_INBOUNDPORT_URI,
						ClockServerConnector.class.getCanonicalName());
			}

			this.currentState.set(RefrigeratorState.OFF);
			this.traceMessage("start.\n");
		} catch (Exception e) {
			throw new ComponentStartException(e);
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		if (this.isUnderTest) {
			AcceleratedClock clock =
					this.clockServerOBP.getClock(this.clockURI);
			// the accelerated period is in nanoseconds, hence first convert
			// the period to nanoseconds, perform the division and then
			// convert to long (hence providing a better precision than
			// first dividing and then converting to nanoseconds...)
			this.actualControlPeriod =
				(long)((this.controlPeriod * TimeUnit.SECONDS.toNanos(1))/
											clock.getAccelerationFactor());
			// release readers so that we make sure that actualControlPeriod
			// has also been properly set before
			this.clock.complete(clock);
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void	finalise() throws Exception
	{
		this.traceMessage("finalise.\n");
		this.doPortDisconnection(this.sensorOutboundPort.getPortURI());
		this.doPortDisconnection(this.actuatorOutboundPort.getPortURI());
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
			this.sensorOutboundPort.unpublishPort();
			this.actuatorOutboundPort.unpublishPort();
			this.clockServerOBP.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component internal methods
	// -------------------------------------------------------------------------

	/**
	 * receive and process the state data coming from the refrigerator component,
	 * starting the control loop if the state has changed from {@code OFF} to
	 * {@code ON}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code sd != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param sd		state data received from the refrigerator component.
	 */
	public void			receiveRunningState(StateDataI sd)
	{
		assert	sd != null : new PreconditionException("sd != null");

		if (DEBUG) {
			this.traceMessage("receives refrigerator running state " + sd.getState()
							  + " at " + sd.getTime() + ".\n");
		}

		if (this.clock == null) {	// not initialised yet
			try {
				// make sure that the clock and the accelerated control period
				// have been initialised
				this.clock.get();
				// sanity checking, the standard Java scheduler has a precision
				// no less than 10 milliseconds...
				if (this.actualControlPeriod <
										TimeUnit.MILLISECONDS.toNanos(10)) {
					System.out.println(
						"Warning: accelerated control period is too small (" +
						this.actualControlPeriod +
						"), unexpected scheduling problems may occur!");
				}
			} catch (Exception e) {
				throw new RuntimeException(e) ;
			}
		}

		// the current state is always updated, but only in the case when
		// the refrigerator is switched on that the controller begins to perform
		// the temperature control
		if (RefrigeratorState.OFF == this.currentState.getAndSet(sd.getState())) {
			this.traceMessage("start control.\n");
			// if a state change has been detected from OFF to ON, schedule a
			// first execution of the control loop, which in turn will schedule
			// its next execution if needed
			this.scheduleTask(
				o -> ((RefrigeratorController)o).controLoop(),
				this.actualControlPeriod, 
				TimeUnit.NANOSECONDS);
		}
	}

	/**
	 * implement the control loop.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void		controLoop()
	{
		try {
			// execute the control only of the refrigerator is still ON
			if (this.currentState.get() != RefrigeratorState.OFF) {
				// get the temperature data from the refrigerator
				TemperatureDataI td =
						(TemperatureDataI) this.sensorOutboundPort.request();

				if (DEBUG) {
					this.traceMessage(td.toString() + "\n");
				}

				if (td.getCurrentFreezingTemperature() > td.getTargetFreezingTemperature() + this.hysteresis
						|| td.getCurrentRefrigerationTemperature() > td.getTargetRefrigerationTemperature() + this.hysteresis) {
					// the current temperature is too high, start running
					if (RefrigeratorState.RUNNING != this.currentState.get()) {
						// tracing
						StringBuffer sb =
								new StringBuffer("start running with ");
						sb.append(td.getCurrentFreezingTemperature());
						sb.append(" > ");
						sb.append(td.getTargetFreezingTemperature());
						sb.append(" + ");
						sb.append(this.hysteresis);
						sb.append(" or ");
						sb.append(td.getCurrentRefrigerationTemperature());
						sb.append(" > ");
						sb.append(td.getTargetRefrigerationTemperature());
						sb.append(" + ");
						sb.append(this.hysteresis);
						sb.append(" at ");
						sb.append(this.clock.get().currentInstant());
						sb.append(".\n");
						this.traceMessage(sb.toString());
						// actuate
						this.actuatorOutboundPort.run();
					}
				} else if (td.getCurrentFreezingTemperature() < td.getTargetFreezingTemperature() - this.hysteresis
						|| td.getCurrentRefrigerationTemperature() < td.getTargetRefrigerationTemperature() - this.hysteresis) {
					// the current room temperature is low enough, stop running
					if (RefrigeratorState.RUNNING == this.currentState.get()) {
						// tracing
						StringBuffer sb =
								new StringBuffer("stop running with ");
						sb.append(td.getCurrentFreezingTemperature());
						sb.append(" < ");
						sb.append(td.getTargetFreezingTemperature());
						sb.append(" - ");
						sb.append(this.hysteresis);
						sb.append(" or ");
						sb.append(td.getCurrentRefrigerationTemperature());
						sb.append(" < ");
						sb.append(td.getTargetRefrigerationTemperature());
						sb.append(" - ");
						sb.append(this.hysteresis);
						sb.append(" at ");
						sb.append(this.clock.get().currentInstant());
						sb.append(".\n");
						this.traceMessage(sb.toString());
						// actuate
						this.actuatorOutboundPort.doNotRun();
					}
				}

				// schedule the next execution of the loop
				this.scheduleTask(
						o -> ((RefrigeratorController)o).controLoop(),
						this.actualControlPeriod, 
						TimeUnit.NANOSECONDS);
			} else {
				// when the refrigerator is OFF, exit the control loop
				this.traceMessage("exit the control.\n");
			}
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}
