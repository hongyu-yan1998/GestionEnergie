package fr.sorbonne_u.components.hem2022e3.equipments.heater;

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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.CompletableFuture;
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
import fr.sorbonne_u.components.hem2022e3.equipments.heater.ThermostatedHeater.HeaterState;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.HeaterSensorCI.StateDataI;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.HeaterSensorCI.TemperatureDataI;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.connections.HeaterSensorDataOutboundPort;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.connections.HeaterActuatorConnector;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.connections.HeaterActuatorOutboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>ThermostatedHeaterController</code> implements a controller
 * component for the thermostated heater.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The controller is a simple fixed period threshold-based controller with
 * hysteresis.
 * </p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code hysteresis > 0.0}
 * invariant	{@code controlPeriod > 0}
 * invariant	{@code accFactor > 0.0}
 * invariant	{@code !isUnderTest || clockURI != null && !clockURI.isEmpty()}
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code STANDARD_HYSTERESIS > 0.0}
 * invariant	{@code STANDARD_CONTROL_PERIOD > 0}
 * </pre>
 * 
 * <p>Created on : 2022-10-27</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@RequiredInterfaces(required={DataRequiredCI.PullCI.class,
							  HeaterActuatorCI.class,
							  ClockServerCI.class})
@OfferedInterfaces(offered={DataRequiredCI.PushCI.class})
//-----------------------------------------------------------------------------
public class			ThermostatedHeaterController
extends		AbstractComponent
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

	/** URI of the sensor inbound port on the {@code ThermostatedHeater}.	*/
	protected String								sensorIBP_URI;
	/** URI of the actuator inbound port on the {@code ThermostatedHeater}.	*/
	protected String								actuatorIBPURI;
	/** sensor data outbound port connected to the
	 *  {@code ThermostatedHeater}.											*/
	protected HeaterSensorDataOutboundPort			sensorOutboundPort;
	/** actuator outbound port connected to the {@code ThermostatedHeater}.	*/
	protected HeaterActuatorOutboundPort			actuatorOutboundPort;

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

	/** the current state of the heater as perceived through the sensor
	 *  data received from the {@code ThermostatedHeater}.					*/
	protected final AtomicReference<HeaterState>	currentState;

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
	 * @param sensorIBP_URI		URI of the heater sensor inbound port.
	 * @param actuatorIBP_URI	URI of the heater actuator inbound port.
	 * @throws Exception		<i>to do</i>.
	 */
	protected			ThermostatedHeaterController(
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
	 * @param sensorIBP_URI		URI of the heater sensor inbound port.
	 * @param actuatorIBP_URI	URI of the heater actuator inbound port.
	 * @param hysteresis		control hysteresis around the target temperature.
	 * @param controlPeriod		control period in seconds.
	 * @throws Exception 		<i>to do</i>.
	 */
	protected			ThermostatedHeaterController(
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
	 * @param sensorIBP_URI		URI of the heater sensor inbound port.
	 * @param actuatorIBP_URI	URI of the heater actuator inbound port.
	 * @param hysteresis		control hysteresis around the target temperature.
	 * @param controlPeriod		control period in seconds.
	 * @param isUnderTest		when true, the component is executed in test mode.
	 * @param clockURI			URI of the clock to be used to synchronise the test scenarios and the simulation.
	 * @throws Exception 		<i>to do</i>.
	 */
	protected			ThermostatedHeaterController(
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
		this.currentState = new AtomicReference<HeaterState>();

		this.sensorOutboundPort =
				new HeaterSensorDataOutboundPort(this);
		this.sensorOutboundPort.publishPort();
		this.actuatorOutboundPort =
				new HeaterActuatorOutboundPort(this);
		this.actuatorOutboundPort.publishPort();

		if (VERBOSE || DEBUG) {
			this.tracer.get().setTitle("Heater controller component");
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
					HeaterActuatorConnector.class.getCanonicalName());

			if (this.isUnderTest) {
				this.clockServerOBP = new ClockServerOutboundPort(this);
				this.clockServerOBP.publishPort();
				this.doPortConnection(
						this.clockServerOBP.getPortURI(),
						ClockServer.STANDARD_INBOUNDPORT_URI,
						ClockServerConnector.class.getCanonicalName());
			}

			this.currentState.set(HeaterState.OFF);
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
	 * receive and process the state data coming from the heater component,
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
	 * @param sd		state data received from the heater component.
	 */
	public void			receiveRunningState(StateDataI sd)
	{
		assert	sd != null : new PreconditionException("sd != null");

		if (DEBUG) {
			this.traceMessage("receives heater running state " + sd.getState()
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
		// the heater is switched on that the controller begins to perform
		// the temperature control
		if (HeaterState.OFF == this.currentState.getAndSet(sd.getState())) {
			this.traceMessage("start control.\n");
			// if a state change has been detected from OFF to ON, schedule a
			// first execution of the control loop, which in turn will schedule
			// its next execution if needed
			this.scheduleTask(
				o -> ((ThermostatedHeaterController)o).controLoop(),
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
			// execute the control only of the heater is still ON
			if (this.currentState.get() != HeaterState.OFF) {
				// get the temperature data from the heater
				TemperatureDataI td =
						(TemperatureDataI) this.sensorOutboundPort.request();

				if (DEBUG) {
					this.traceMessage(td.toString() + "\n");
				}

				if (td.getCurrent() < td.getTarget() - this.hysteresis) {
					// the current room temperature is too low, start heating
					if (HeaterState.HEATING != this.currentState.get()) {
						// tracing
						StringBuffer sb =
								new StringBuffer("start heating with ");
						sb.append(td.getCurrent());
						sb.append(" < ");
						sb.append(td.getTarget());
						sb.append(" - ");
						sb.append(this.hysteresis);
						sb.append(" at ");
						sb.append(this.clock.get().currentInstant());
						sb.append(".\n");
						this.traceMessage(sb.toString());
						// actuate
						this.actuatorOutboundPort.heat();
					}
				} else if (td.getCurrent() > td.getTarget() + this.hysteresis) {
					// the current room temperature is high enough, stop heating
					if (HeaterState.HEATING == this.currentState.get()) {
						// tracing
						StringBuffer sb =
								new StringBuffer("stop heating with ");
						sb.append(td.getCurrent());
						sb.append(" > ");
						sb.append(td.getTarget());
						sb.append(" + ");
						sb.append(this.hysteresis);
						sb.append(" at ");
						sb.append(this.clock.get().currentInstant());
						sb.append(".\n");
						this.traceMessage(sb.toString());
						// actuate
						this.actuatorOutboundPort.doNotHeat();
					}
				}

				// schedule the next execution of the loop
				this.scheduleTask(
						o -> ((ThermostatedHeaterController)o).controLoop(),
						this.actualControlPeriod, 
						TimeUnit.NANOSECONDS);
			} else {
				// when the heater is OFF, exit the control loop
				this.traceMessage("exit the control.\n");
			}
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}
// -----------------------------------------------------------------------------
