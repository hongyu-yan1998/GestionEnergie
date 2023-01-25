/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.architectures.SimulationEngineCreationMode;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTAtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTCoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.interfaces.ModelDescriptionI;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.models.events.ReexportedEvent;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.models.time.TimeUtils;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.DoNotRun;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.Run;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.StartRefrigerator;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.StopRefrigerator;

/**
 * The class <code>RefrigeratorRTAtomicSimulatorPlugin</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-03</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class RefrigeratorRTAtomicSimulatorPlugin 
extends RTAtomicSimulatorPlugin {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** name used to pass the refrigerator owner component reference as
	 *  simulation parameter.												*/
	public static final String	OWNER_RPNAME = "RCRN";
	/** name used to access the current freezing temperature in the
	 *  {@code RefrigeratorTemperatureModel}.								 	*/
	public static final String	CURRENT_FREEZING_TERMPERATURE = "cft";
	/** name used to access the current refrigeration temperature in the
	 *  {@code RefrigeratorTemperatureModel}.								 	*/
	public static final String	CURRENT_REFRIGERATION_TERMPERATURE = "crt";

	/** start time of the simulation in {@code LocaTime}.					*/
	protected Instant	localStartTime;
	/** start time of the simulation in simulated time.						*/
	protected Time		simulatedStartTime;

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	@Override
	public void			setSimulationRunParameters(
		Map<String, Object> simParams
		) throws Exception
	{
		// initialise the simulation parameter giving the reference to the
		// owner component before passing the parameters to the simulation
		// models
		simParams.put(OWNER_RPNAME, this.getOwner());

		// this will pass the parameters to the simulation models that will
		// then be able to get their own parameters.
		super.setSimulationRunParameters(simParams);

		// remove the value so that the reference may not exit the context of
		// the component
		simParams.remove(OWNER_RPNAME);
	}
	
	/**
	 * The only model state value in the refrigerator SIL simulators is the room
	 * temperature computed by the {@code RefrigeratorTemperatureModel}, hence
	 * {@code modelURI} must be the URI of a {@code RefrigeratorTemperatureModel}
	 * and {@code name} must equal {@code CURRENT_ROOM_TERMPERATURE} otherwise
	 * an assertion error will be triggered.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more preconditions.
	 * post	{@code true}	// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin#getModelStateValue(java.lang.String, java.lang.String)
	 */
	@Override
	public Object		getModelStateValue(String modelURI, String name)
	throws Exception
	{
		// The only model state value in the refrigerator SIL simulators is the
		// room temperature computed by the RefrigeratorTemperatureModel
		assert	modelURI != null && !modelURI.isEmpty() :
				new PreconditionException(
						"modelURI != null && !modelURI.isEmpty()");
		assert	name != null && !name.isEmpty() :
				new PreconditionException("name != null && !name.isEmpty()");

		ModelDescriptionI m = this.simulator.getDescendentModel(modelURI);
		assert	m instanceof RefrigeratorTemperatureModel :
				new AssertionError(modelURI + " is not the URI of a "
						+ RefrigeratorTemperatureModel.class.getSimpleName()
						+ "model!");
		
		assert	name.equals(CURRENT_FREEZING_TERMPERATURE) || 
					name.equals(CURRENT_REFRIGERATION_TERMPERATURE):
				new AssertionError(name + " is an unknown variable name!");

		if (name.equals(CURRENT_FREEZING_TERMPERATURE)) {
			return ((RefrigeratorTemperatureModel)m).getCurrentFreezingTemperature();
		} 
		return ((RefrigeratorTemperatureModel)m).getCurrentRefrigerationTemperature();
	}

	@Override
	public void			startRTSimulation(
		long realTimeOfStart,
		double simulationStartTime,
		double simulationDuration
		) throws Exception
	{
		// keep track of the simulation real time of start as an instance
		// of Instant to be able to convert simulated times into instants
		this.localStartTime = Instant.ofEpochMilli(realTimeOfStart);
		// also keep track of the simulated start time in order to interpret
		// correctly the simulated times against localStartTime
		this.simulatedStartTime =
				new Time(simulationStartTime,
						 this.localArchitecture.getSimulationTimeUnit());
		// actually start the real time simulation
		super.startRTSimulation(
					realTimeOfStart, simulationStartTime, simulationDuration);
	}
	
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * compute the {@code Instant} corresponding to the current simulation time
	 * of the {@code RefrigeratorTemperatureModel}, assuming that the start time and
	 * the start instant are known to interpret correctly the current simulation
	 * time as an {@code Instant}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition
	 * post	{@code ret != null}
	 * </pre>
	 *
	 * @return	the instant corresponding to the current simulation time.
	 */
	public Instant		getCurrentInstant()
	{
		try {
			RefrigeratorTemperatureModel m =
				(RefrigeratorTemperatureModel)
					this.simulator.getDescendentModel(RefrigeratorTemperatureModel.URI);
			Time t = m.getCurrentStateTime();
			Duration d = t.subtract(this.simulatedStartTime);
			long dInNanos =
				TimeUtils.toNanos(d.getSimulatedDuration(), d.getTimeUnit());
			return this.localStartTime.plusNanos(dInNanos);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

	/**
	 * create and set the simulation architecture internal to this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code simArchURI != null && !simArchURIisEmpty()}
	 * pre	{@code accFactor > 0.0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param simArchURI	URI of the simulation architecture to be created.
	 * @param accFactor		acceleration factor used in the real time simulation.
	 * @param isUnitTesting	when true, create the unit test simulation architecture.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			initialiseSimulationArchitecture(
		String simArchURI,
		double accFactor,
		boolean isUnitTesting
		) throws Exception
	{
		// map that will contain the atomic model descriptors to construct
		// the simulation architecture
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();
		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();
		Map<Class<? extends EventI>,EventSink[]> imported = new HashMap<>();
		Map<Class<? extends EventI>,ReexportedEvent> reexported = null;
		Map<EventSource, EventSink[]> connections = new HashMap<>();

		// the refrigerator models simulating its electricity consumption, its
		// temperatures are atomic HIOA models
		// hence we use an AtomicHIOA_Descriptor(s)
		atomicModelDescriptors.put(
				RefrigeratorTemperatureModel.URI,
				RTAtomicHIOA_Descriptor.create(
						RefrigeratorTemperatureModel.class,
						RefrigeratorTemperatureModel.URI,
						TimeUnit.HOURS,
						null,
						SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
						accFactor));
		
		// the refrigerator state and unit tester models only exchanges event, an
		// atomic model hence we use an AtomicModelDescriptor
		atomicModelDescriptors.put(
				RefrigeratorStateModel.URI,
				RTAtomicModelDescriptor.create(
						RefrigeratorStateModel.class,
						RefrigeratorStateModel.URI,
						TimeUnit.HOURS,
						null,
						SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
						accFactor));

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(RefrigeratorTemperatureModel.URI);
		submodels.add(RefrigeratorStateModel.URI);

		imported.put(
				StartRefrigerator.class,
				new EventSink[] {
					new EventSink(RefrigeratorStateModel.URI, StartRefrigerator.class)
				});
		imported.put(
				StopRefrigerator.class,
				new EventSink[] {
					new EventSink(RefrigeratorStateModel.URI, StopRefrigerator.class)
				});
		imported.put(
				Run.class,
				new EventSink[] {
					new EventSink(RefrigeratorStateModel.URI, Run.class)
				});
		imported.put(
				DoNotRun.class,
				new EventSink[] {
					new EventSink(RefrigeratorStateModel.URI, DoNotRun.class)
				});

		if (isUnitTesting) {
			// when the component performs a SIL simulated unit test, the
			// RefrigeratorElectricityModel must be located within the
			// ThermostatedHeater component SIL simulator and the events
			// exported by the RefrigeratorStateModel are sent to
			// RefrigeratorElectricityModel
			atomicModelDescriptors.put(
					RefrigeratorElectricityModel.URI,
					RTAtomicHIOA_Descriptor.create(
							RefrigeratorElectricityModel.class,
							RefrigeratorElectricityModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
							accFactor));
			submodels.add(RefrigeratorElectricityModel.URI);

			connections.put(
				new EventSource(RefrigeratorStateModel.URI, StartRefrigerator.class),
				new EventSink[] {
						new EventSink(RefrigeratorElectricityModel.URI,
									  StartRefrigerator.class)
				});
			connections.put(
				new EventSource(RefrigeratorStateModel.URI, StopRefrigerator.class),
				new EventSink[] {
						new EventSink(RefrigeratorElectricityModel.URI,
									  StopRefrigerator.class)
				});
			connections.put(
				new EventSource(RefrigeratorStateModel.URI, Run.class),
				new EventSink[] {
						new EventSink(RefrigeratorElectricityModel.URI, Run.class),
						new EventSink(RefrigeratorTemperatureModel.URI, Run.class)
				});
			connections.put(
				new EventSource(RefrigeratorStateModel.URI, DoNotRun.class),
				new EventSink[] {
						new EventSink(RefrigeratorElectricityModel.URI, DoNotRun.class),
						new EventSink(RefrigeratorTemperatureModel.URI, DoNotRun.class)
				});
		} else {
			// when the component performs a SIL simulated integration test, the
			// RefrigeratorElectricityModel will be located outside the
			// ThermostatedHeater component SIL simulator and the events
			// exported by the RefrigeratorStateModel are reexported by the
			// RefrigeratorCoupledModel to be received by the RefrigeratorElectricityModel
			// wherever it is located in the simulation architecture
			connections.put(
				new EventSource(RefrigeratorStateModel.URI, Run.class),
				new EventSink[] {
						new EventSink(RefrigeratorTemperatureModel.URI, Run.class)
				});
			connections.put(
				new EventSource(RefrigeratorStateModel.URI, DoNotRun.class),
				new EventSink[] {
						new EventSink(RefrigeratorTemperatureModel.URI, DoNotRun.class)
				});

			reexported = new HashMap<>();
			reexported.put(
				StartRefrigerator.class,
				new ReexportedEvent(RefrigeratorStateModel.URI, StartRefrigerator.class));
			reexported.put(
				StopRefrigerator.class,
				new ReexportedEvent(RefrigeratorStateModel.URI, StopRefrigerator.class));
			reexported.put(
				Run.class,
				new ReexportedEvent(RefrigeratorStateModel.URI, Run.class));
			reexported.put(
				DoNotRun.class,
				new ReexportedEvent(RefrigeratorStateModel.URI, DoNotRun.class));

		}

		// coupled model descriptor
		coupledModelDescriptors.put(
				RefrigeratorCoupledModel.URI,
				new RTCoupledHIOA_Descriptor(
						RefrigeratorCoupledModel.class,
						RefrigeratorCoupledModel.URI,
						submodels,
						imported,
						reexported,
						connections,
						null,
						SimulationEngineCreationMode.COORDINATION_RT_ENGINE,
						null,
						null,
						null,
						accFactor));

		// simulation architecture
		this.setSimulationArchitecture(
				new RTArchitecture(
						simArchURI,
						RefrigeratorCoupledModel.URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						TimeUnit.HOURS,
						accFactor));
	}
}
