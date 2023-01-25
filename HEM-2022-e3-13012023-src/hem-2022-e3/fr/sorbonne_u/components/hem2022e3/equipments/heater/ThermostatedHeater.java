package fr.sorbonne_u.components.hem2022e3.equipments.heater;

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
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.tools.aclocks.AcceleratedClock;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServer;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerCI;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerConnector;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerOutboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.hem2022e1.equipments.heater.HeaterCI;
import fr.sorbonne_u.components.hem2022e1.equipments.heater.HeaterInboundPort;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.HeaterTemperatureModel;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.HeaterSensorCI.StateDataI;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.HeaterSensorCI.SensorDataI;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.HeaterSensorCI.TemperatureDataI;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.connections.HeaterSensorDataInboundPort;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.connections.HeaterActuatorInboundPort;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.HeaterCoupledModel;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.HeaterRTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.HeaterStateModel;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.events.DoNotHeat;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.events.Heat;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.events.SwitchOffHeater;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.events.SwitchOnHeater;
import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.exceptions.PreconditionException;
import java.time.Instant;

// -----------------------------------------------------------------------------
/**
 * The class <code>ThermostatedHeater</code> a thermostated heater component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code targetTemperature >= -50.0 && targetTemperature <= 50.0}
 * invariant	{@code accFactor > 0.0}
 * invariant	{@code !isUnderTest || clockURI != null && !clockURI.isEmpty()}
 * invariant	{@code !isSimulated || simArchitectureURI != null && !simArchitectureURI.isEmpty()}
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code REFLECTION_INBOUND_PORT_URI != null && !REFLECTION_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code SENSOR_INBOUND_PORT_URI != null && !SENSOR_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code ACTUATOR_INBOUND_PORT_URI != null && !ACTUATOR_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code REFLECTION_INBOUND_PORT_URI != null && !REFLECTION_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code STANDARD_TARGET_TEMPERATURE >= -50.0 && STANDARD_TARGET_TEMPERATURE <= 50.0}
 * </pre>
 * 
 * <p>Created on : 2021-09-10</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@OfferedInterfaces(offered={HeaterCI.class,
							DataOfferedCI.PullCI.class,
							HeaterActuatorCI.class})
@RequiredInterfaces(required={DataOfferedCI.PushCI.class,
							  ClockServerCI.class})
//-----------------------------------------------------------------------------
public class			ThermostatedHeater
extends		AbstractCyPhyComponent
implements	ControlledHeaterImplementationI
{
	// -------------------------------------------------------------------------
	// Inner interfaces and types
	// -------------------------------------------------------------------------

	/**
	 * The enumeration <code>HeaterState</code> describes the operation
	 * states of the heater.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Created on : 2021-09-10</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	protected static enum	HeaterState
	{
		/** heater is on.													*/
		ON,
		/** heater is heating.												*/
		HEATING,
		/** heater is off.													*/
		OFF
	}

	/**
	 * The abstract class <code>SensorData</code> implements the common
	 * variables and methods for the data objects that are exchanged
	 * between this component and the controller component.
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
	 * invariant	{@code getTime() != null}
	 * </pre>
	 * 
	 * <p>Created on : 2022-10-28</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static abstract class	SensorData
	implements	SensorDataI,
				DataOfferedCI.DataI,
				DataRequiredCI.DataI
	{
		private static final long serialVersionUID = 1L;
		protected final Instant	time;

		/**
		 * initialise the time in the data object.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code time != null}
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @param time	the time at which the data is returned.
		 */
		public				SensorData(Instant time)
		{
			super();

			assert	time != null : new PreconditionException("time != null");

			this.time = time;
		}

		/**
		 * @see fr.sorbonne_u.components.hem2022e3.equipments.heater.HeaterSensorCI.SensorDataI#getTime()
		 */
		@Override
		public Instant		getTime()		{ return this.time; }
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
	 * <p>Created on : 2022-10-28</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static class		TemperatureData
	extends		SensorData
	implements	TemperatureDataI
	{
		private static final long serialVersionUID = 1L;
		protected final double	current;
		protected final double	target;

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
		 * @param current	current room temperature.
		 * @param target	current target temperature.
		 * @param time		the time at which the data is returned.
		 */
		public				TemperatureData(
			double current,
			double target,
			Instant time
			)
		{
			super(time);

			this.current = current;
			this.target = target;
		}

		/**
		 * @see fr.sorbonne_u.components.hem2022e3.equipments.heater.HeaterSensorCI.TemperatureDataI#getTarget()
		 */
		@Override
		public double		getTarget()		{ return this.target; }

		/**
		 * @see fr.sorbonne_u.components.hem2022e3.equipments.heater.HeaterSensorCI.TemperatureDataI#getCurrent()
		 */
		@Override
		public double		getCurrent()	{ return this.current; }

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String		toString() {
			StringBuffer sb = new StringBuffer(this.getClass().getSimpleName());
			sb.append('[');
			sb.append(this.getCurrent());
			sb.append(", ");
			sb.append(this.getTarget());
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
	 * invariant	{@code true}	// TODO	// no more invariant
	 * </pre>
	 * 
	 * <p><strong>Black-box Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code getState() != null}
	 * </pre>
	 * 
	 * <p>Created on : 2022-10-28</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static class		StateData
	extends		SensorData
	implements	StateDataI
	{
		private static final long serialVersionUID = 1L;
		protected final HeaterState	state;

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
		 * @param state	the current state of the thermostated heater.
		 * @param time	the time at which the data is returned.
		 */
		public				StateData(HeaterState state, Instant time)
		{
			super(time);

			assert	state != null : new PreconditionException("value != null");
					
			this.state = state;
		}

		/**
		 * @see fr.sorbonne_u.components.hem2022e3.equipments.heater.HeaterSensorCI.StateDataI#getState()
		 */
		@Override
		public HeaterState	getState()		{ return this.state; }

		/**
		 * @see java.lang.Object#toString()
		 */
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

	/** URI of the heater reflection inbound port used in tests.			*/
	public static final String	REFLECTION_INBOUND_PORT_URI = "HEATER-RIP-URI";	
	/** URI of the heater inbound port used in tests.						*/
	public static final String	INBOUND_PORT_URI = "HEATER-INBOUND-PORT-URI";
	/** URI of the heater inbound port used in tests.						*/
	public static final String	SENSOR_INBOUND_PORT_URI =
											"HEATER-SENSOR-INBOUND-PORT-URI";
	/** URI of the heater inbound port used in tests.						*/
	public static final String	ACTUATOR_INBOUND_PORT_URI =
											"HEATER-ACTUATOR-INBOUND-PORT-URI";
	/** when 0, no trace is produced, when 1 methods making state changes
	 *  trace their actions and when 2 getter methods also trace their
	 *  actions.															*/
	public static int			VERBOSE = 1;
	/** unless explicitly specified, track this target temperature.			*/
	public static final double	STANDARD_TARGET_TEMPERATURE = 20.0;
	/** fake current temperature used in tests, when not state from a user
	 *  component.															*/
	public static final double	FAKE_CURRENT_TEMPERATURE = 10.0;

	/** current state (on, off) of the heater.								*/
	protected HeaterState					currentState;
	/** target temperature for the heating.									*/
	protected double						targetTemperature;

	/** inbound port offering the <code>HeaterCI</code> interface.			*/
	protected HeaterInboundPort				hip;
	/** inbound port offering the {@code ThermostatedHeaterSensorCI}
	 *  interface.															*/
	protected HeaterSensorDataInboundPort	hsip;
	/** inbound port offering the {@code ThermostatedHeaterActuatorCI}
	 *  interface.															*/
	protected HeaterActuatorInboundPort		haip;

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
	protected HeaterRTAtomicSimulatorPlugin	simulatorPlugin;
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

	/**
	 * create a new thermostated heater.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty()}
	 * pre	{@code SENSOR_INBOUND_PORT_URI != null && !SENSOR_INBOUND_PORT_URI.isEmpty()}
	 * pre	{@code ACTUATOR_INBOUND_PORT_URI != null && !ACTUATOR_INBOUND_PORT_URI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 * 
	 * @throws Exception <i>to do</i>.
	 */
	protected			ThermostatedHeater() throws Exception
	{
		this(false, false, false, null, 1.0, null);
	}

	/**
	 * create a new thermostated heater.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty()}
	 * pre	{@code SENSOR_INBOUND_PORT_URI != null && !SENSOR_INBOUND_PORT_URI.isEmpty()}
	 * pre	{@code ACTUATOR_INBOUND_PORT_URI != null && !ACTUATOR_INBOUND_PORT_URI.isEmpty()}
	 * pre	{@code !isSimulated || simArchitectureURI != null && !simArchitectureURI.isEmpty()}
	 * pre	{@code accFactor > 0.0}
	 * pre	{@code !isUnderTest || clockURI != null && !clockURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 * 
	 * @param isUnderTest			when true, the simulated test run is test mode.
	 * @param isUnitTest			when true, the simulated test run is unit test mode, otherwise it is an integration test.
	 * @param isSimulated			when true, the component performs a SIL simulation in a test run.
	 * @param simArchitectureURI	URI of the simulation architecture to be created or the empty string  if the component does not execute as a SIL simulation.
	 * @param accFactor				acceleration factor used in timing actions.
	 * @param clockURI				URI of the clock to be used to synchronise the test scenarios and the simulation.
	 * @throws Exception 			<i>to do</i>.
	 */
	protected			ThermostatedHeater(
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

	/**
	 * create a new thermostated heater.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code heaterInboundPortURI != null && !heaterInboundPortURI.isEmpty()}
	 * pre	{@code heaterSensorInboundPortURI != null && !heaterSensorInboundPortURI.isEmpty()}
	 * pre	{@code heaterActuatorInboundPortURI != null && !heaterActuatorInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 * 
	 * @param heaterInboundPortURI			URI of the inbound port to call the heater component.
	 * @param heaterSensorInboundPortURI	URI of inbound port offering the {@code ThermostatedHeaterSensorCI} interface.
	 * @param heaterActuatorInboundPortURI	URI of inbound port offering the {@code ThermostatedHeaterActuatorCI} interface.
	 * @throws Exception					<i>to do</i>.
	 */
	protected			ThermostatedHeater(
		String heaterInboundPortURI,
		String heaterSensorInboundPortURI,
		String heaterActuatorInboundPortURI
		) throws Exception
	{
		this(heaterInboundPortURI, heaterSensorInboundPortURI,
			 heaterActuatorInboundPortURI,
			 false, false, false, null, 1.0, null);
	}

	/**
	 * create a new thermostated heater.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code heaterInboundPortURI != null && !heaterInboundPortURI.isEmpty()}
	 * pre	{@code heaterSensorInboundPortURI != null && !heaterSensorInboundPortURI.isEmpty()}
	 * pre	{@code heaterActuatorInboundPortURI != null && !heaterActuatorInboundPortURI.isEmpty()}
	 * pre	{@code !isSimulated || simArchitectureURI != null && !simArchitectureURI.isEmpty()}
	 * pre	{@code accFactor > 0.0}
	 * pre	{@code !isUnderTest || clockURI != null && !clockURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 * 
	 * @param heaterInboundPortURI			URI of the inbound port to call the heater component.
	 * @param heaterSensorInboundPortURI	URI of inbound port offering the {@code ThermostatedHeaterSensorCI} interface.
	 * @param heaterActuatorInboundPortURI	URI of inbound port offering the {@code ThermostatedHeaterActuatorCI} interface.
	 * @param isUnderTest					when true, the simulated test run is test mode.
	 * @param isUnitTest					when true, the simulated test run is unit test mode, otherwise it is an integration test.
	 * @param isSimulated					when true, the component performs a SIL simulation in a test run.
	 * @param simArchitectureURI			URI of the simulation architecture to be created or the empty string  if the component does not execute as a SIL simulation.
	 * @param accFactor						acceleration factor used in timing actions.
	 * @param clockURI						URI of the clock to be used to synchronise the test scenarios and the simulation.
	 * @throws Exception					<i>to do</i>.
	 */
	protected			ThermostatedHeater(
		String heaterInboundPortURI,
		String heaterSensorInboundPortURI,
		String heaterActuatorInboundPortURI,
		boolean isUnderTest,
		boolean isUnitTest,
		boolean isSimulated,
		String simArchitectureURI,
		double accFactor,
		String clockURI
		) throws Exception
	{
		super(REFLECTION_INBOUND_PORT_URI, 1, 1);

		assert	heaterInboundPortURI != null && !heaterInboundPortURI.isEmpty() :
				new PreconditionException(
						"heaterInboundPortURI != null && "
						+ "!heaterInboundPortURI.isEmpty()");
		assert	heaterSensorInboundPortURI != null &&
										!heaterSensorInboundPortURI.isEmpty() :
				new PreconditionException(
						"heaterSensorInboundPortURI != null && "
						+ "!heaterSensorInboundPortURI.isEmpty()");
		assert	heaterActuatorInboundPortURI != null &&
										!heaterActuatorInboundPortURI.isEmpty() :
				new PreconditionException(
						"heaterActuatorInboundPortURI != null && "
						+ "!heaterActuatorInboundPortURI.isEmpty()");
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
		this.targetTemperature = STANDARD_TARGET_TEMPERATURE;

		this.hip = new HeaterInboundPort(heaterInboundPortURI, this);
		this.hip.publishPort();
		this.hsip =
			new HeaterSensorDataInboundPort(heaterSensorInboundPortURI, this);
		this.hsip.publishPort();
		this.haip =
			new HeaterActuatorInboundPort(heaterActuatorInboundPortURI, this);
		this.haip.publishPort();

		if (VERBOSE > 0) {
			this.tracer.get().setTitle("Thermostated heater component");
			this.tracer.get().setRelativePosition(0, 2);
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

		// initialisations
		this.currentState = HeaterState.OFF;
		this.targetTemperature = STANDARD_TARGET_TEMPERATURE;

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
					this.simulatorPlugin = new HeaterRTAtomicSimulatorPlugin();
					this.simulatorPlugin.setPluginURI(HeaterCoupledModel.URI);
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
			this.hip.unpublishPort();
			this.hsip.unpublishPort();
			this.haip.unpublishPort();
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
	 * return true if the heater is running.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the heater is running.
	 */
	public boolean		internalIsRunning()
	{
		return this.currentState == HeaterState.ON ||
									this.currentState == HeaterState.HEATING;
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.heater.HeaterImplementationI#isRunning()
	 */
	@Override
	public boolean		isRunning() throws Exception
	{
		if (VERBOSE > 1) {
			this.traceMessage("state: " + this.currentState + ".\n");
		}
		return this.internalIsRunning();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.heater.HeaterImplementationI#startHeater()
	 */
	@Override
	public void			startHeater() throws Exception
	{
		if (VERBOSE > 0) {
			this.traceMessage("heater starts.\n");
		}
		assert	!this.internalIsRunning();

		this.currentState = HeaterState.ON;

		// When SIL simulated, actions that change the state of the heater must
		// be transmitted to the simulation models in order to keep the
		// component and simulation states in synchronisation.
		if (this.isSimulated) {
			// trigger an immediate SwitchOnHeater event on the HeaterStateModel,
			// which in turn will emit this event towards the other models of
			// the heater
			// the t parameter in the lambda expression represents the current
			// simulation time to be provided by he simulator before passing
			// the event instance to the model
			this.simulatorPlugin.triggerExternalEvent(
					HeaterStateModel.URI,
					t -> new SwitchOnHeater(t));
		} else {
			// In a fully operational implementation of an embedded software,
			// this method would call the physical actuator turning on the heater.
			// As we are not developing for an actual device, we only set the new
			// state in the component.
		}

		// force a push of the new state towards the controller
		this.heaterStateSensor();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.heater.HeaterImplementationI#stopHeater()
	 */
	@Override
	public void			stopHeater() throws Exception
	{
		if (VERBOSE > 0) {
			this.traceMessage("heater stops.\n");
		}
		assert	this.internalIsRunning();

		this.currentState = HeaterState.OFF;

		// When SIL simulated, actions that change the state of the heater must
		// be transmitted to the simulation models in order to keep the
		// component and simulation states in synchronisation.
		if (this.isSimulated) {
			// trigger an immediate SwitchOffHeater event on the HeaterStateModel,
			// which in turn will emit this event towards the other models of
			// the heater
			// the t parameter in the lambda expression represents the current
			// simulation time to be provided by he simulator before passing
			// the event instance to the model
			this.simulatorPlugin.triggerExternalEvent(
					HeaterStateModel.URI,
					t -> new SwitchOffHeater(t));
		} else {
			// In a fully operational implementation of an embedded software,
			// this method would call the physical actuator turning on the heater.
			// As we are not developing for an actual device, we only set the new
			// state in the component.
		}

		// force a push of the new state towards the controller
		this.heaterStateSensor();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.heater.HeaterImplementationI#setTargetTemperature(double)
	 */
	@Override
	public void			setTargetTemperature(double target) throws Exception
	{
		if (VERBOSE > 0) {
			this.traceMessage("new target temperature: " + target + ".\n");
		}

		assert	this.internalIsRunning();
		assert	target >= -50.0 && target <= 50.0;

		this.targetTemperature = target;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.heater.HeaterImplementationI#getTargetTemperature()
	 */
	@Override
	public double		getTargetTemperature() throws Exception
	{
		if (VERBOSE > 1) {
			this.traceMessage(
					"target temperature " + this.targetTemperature + ".\n");
		}

		return this.targetTemperature;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.heater.HeaterImplementationI#getCurrentTemperature()
	 */
	@Override
	public double		getCurrentTemperature() throws Exception
	{
		double currentTemperature = 0.0;
		if (this.isSimulated) {
			// use the simulated value access protocol to get the computed
			// current room temperature
			currentTemperature = (double)
				this.simulatorPlugin.getModelStateValue(
					HeaterTemperatureModel.URI,
					HeaterRTAtomicSimulatorPlugin.CURRENT_ROOM_TERMPERATURE);
		} else {
			// Temporary implementation; an operational implementation would
			// need an access to an actual temperature sensor device.
			currentTemperature = FAKE_CURRENT_TEMPERATURE;
		}

		if (VERBOSE > 1) {
			this.traceMessage(
					"current temperature " + currentTemperature + ".\n");
		}

		return  currentTemperature;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e3.equipments.heater.ControlledHeaterImplementationI#heaterStateSensor()
	 */
	@Override
	public void			heaterStateSensor() throws Exception
	{
		Instant i = null;
		if (this.isSimulated) {
			i = this.simulatorPlugin.getCurrentInstant();
		} else if (this.isUnderTest) {
			i = this.clock.currentInstant();
		} else {
			i = Instant.now();
		}

		this.hsip.send(new StateData(this.currentState, i));
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e3.equipments.heater.ControlledHeaterImplementationI#currentTemperaturesSensor()
	 */
	@Override
	public TemperatureDataI	currentTemperaturesSensor() throws Exception
	{
		Instant i = null;
		if (this.isSimulated) {
			i = this.simulatorPlugin.getCurrentInstant();
		} else if (this.isUnderTest) {
			i = this.clock.currentInstant();
		} else {
			i = Instant.now();
		}

		return new TemperatureData(this.getCurrentTemperature(),
								   this.getTargetTemperature(), i);
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e3.equipments.heater.ControlledHeaterImplementationI#heat()
	 */
	@Override
	public void			heat()
	{
		if (VERBOSE > 0) {
			this.traceMessage("heater starts heating.\n");
		}

		this.currentState = HeaterState.HEATING;

		// When SIL simulated, actions that change the state of the heater must
		// be transmitted to the simulation models in order to keep the
		// component and simulation states in synchronisation.
		if (this.isSimulated) {
			try {
				// trigger an immediate Heat event on the HeaterStateModel,
				// which in turn will emit this event towards the other models
				// of the heater
				// the t parameter in the lambda expression represents the
				// current simulation time to be provided by he simulator before
				// passing the event instance to the model
				this.simulatorPlugin.triggerExternalEvent(
						HeaterStateModel.URI,
						t -> new Heat(t));
			} catch (Exception e) {
				throw new RuntimeException(e) ;
			}
		} else {
			// In a fully operational implementation of an embedded software,
			// this method would call the physical actuator switching on the
			// heating on the heater. As we are not developing for an actual
			// device, we only set the new state in the component.
		}

		// force a push of the new state data towards the controller
		try {
			this.heaterStateSensor();
		} catch (Exception e1) {
			throw new RuntimeException(e1) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e3.equipments.heater.ControlledHeaterImplementationI#doNotHeat()
	 */
	@Override
	public void			doNotHeat()
	{
		if (VERBOSE > 0) {
			this.traceMessage("heater stops heating.\n");
		}

		this.currentState = HeaterState.ON;

		// When SIL simulated, actions that change the state of the heater must
		// be transmitted to the simulation models in order to keep the
		// component and simulation states in synchronisation.
		if (this.isSimulated) {
			try {
				// trigger an immediate DoNotHeat event on the HeaterStateModel,
				// which in turn will emit this event towards the other models
				// of the heater
				// the t parameter in the lambda expression represents the
				// current simulation time to be provided by he simulator before
				// passing the event instance to the model
				this.simulatorPlugin.triggerExternalEvent(
						HeaterStateModel.URI,
						t -> new DoNotHeat(t));
			} catch (Exception e) {
				throw new RuntimeException(e) ;
			}
		} else {
			// In a fully operational implementation of an embedded software,
			// this method would call the physical actuator switching off the
			// heating on the heater. As we are not developing for an actual
			// device, we only set the new state in the component.
		}

		// force a push of the new state data towards the controller
		try {
			this.heaterStateSensor();
		} catch (Exception e1) {
			throw new RuntimeException(e1) ;
		}
	}
}
// -----------------------------------------------------------------------------
