package fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.events.DoNotHeat;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.events.SetHighTemperature;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.events.SetLowTemperature;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.events.SwitchOffBlanket;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.events.SwitchOnBlanket;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.architectures.SimulationEngineCreationMode;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTAtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTCoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.models.time.Time;

/**
* @author Liuyi CHEN & Hongyu YAN
* @date 4 janv. 2023
* @Description 
*/
public class ElectricBlanketRTAtomicSimulatorPlugin
extends RTAtomicSimulatorPlugin {
	
	private static final long	serialVersionUID = 1L;
	public static final String	OWNER_RPNAME = "EBCRN";
	public static final String	CURRENT_PERSON_TERMPERATURE = "crt";
	
	/** start time of the simulation in {@code LocaTime}.					*/
	protected Instant	localStartTime;
	/** start time of the simulation in simulated time.						*/
	protected Time		simulatedStartTime;
	

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------
	
	/**
	 * @see fr.sorbonne_u.components.cyphy.plugins.devs.AbstractSimulatorPlugin#setSimulationRunParameters(java.util.Map)
	 */
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
	 * @see fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin#startRTSimulation(long, double, double)
	 */
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
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
				new HashMap<>();
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
				new HashMap<>();
		Map<Class<? extends EventI>,EventSink[]> imported = new HashMap<>();
		Map<EventSource, EventSink[]> connections = new HashMap<>();
		
		atomicModelDescriptors.put(
				ElectricBlanketStateModel.URI,
				RTAtomicModelDescriptor.create(
						ElectricBlanketStateModel.class,
						ElectricBlanketStateModel.URI,
						TimeUnit.HOURS,
						null,
						SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
						accFactor));
		if(isUnitTesting) {
			imported.put(
					SwitchOnBlanket.class,
					new EventSink[] {
							new EventSink(
									ElectricBlanketStateModel.URI,
									SwitchOnBlanket.class)							
					});
			imported.put(
					SwitchOffBlanket.class,
					new EventSink[] {
							new EventSink(
									ElectricBlanketStateModel.URI,
									SwitchOffBlanket.class)							
					});
			imported.put(
					SetLowTemperature.class,
					new EventSink[] {
							new EventSink(
									ElectricBlanketStateModel.URI,
									SetLowTemperature.class)							
					});
			imported.put(
					SetHighTemperature.class,
					new EventSink[] {
							new EventSink(
									ElectricBlanketStateModel.URI,
									SetHighTemperature.class)							
					});
			imported.put(
					DoNotHeat.class,
					new EventSink[] {
							new EventSink(
									ElectricBlanketStateModel.URI,
									DoNotHeat.class)							
					});
			
			atomicModelDescriptors.put(
					ElectricBlanketElectricityModel.URI,
					RTAtomicHIOA_Descriptor.create(
							ElectricBlanketElectricityModel.class,
							ElectricBlanketElectricityModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
							accFactor));
			
			Set<String> submodels = new HashSet<String>();
			submodels.add(ElectricBlanketStateModel.URI);
			submodels.add(ElectricBlanketElectricityModel.URI);
			
			// event exchanging connections between exporting and importing
			// models

			connections.put(
					new EventSource(ElectricBlanketStateModel.URI,
									SwitchOnBlanket.class),
					new EventSink[] {
							new EventSink(
									ElectricBlanketElectricityModel.URI,
									SwitchOnBlanket.class)							
					});
			connections.put(
					new EventSource(ElectricBlanketStateModel.URI,
									SwitchOffBlanket.class),
					new EventSink[] {
							new EventSink(
									ElectricBlanketElectricityModel.URI,
									SwitchOffBlanket.class)							
					});
			connections.put(
					new EventSource(ElectricBlanketStateModel.URI,
									SetLowTemperature.class),
					new EventSink[] {
							new EventSink(
									ElectricBlanketElectricityModel.URI,
									SetLowTemperature.class)							
					});
			connections.put(
					new EventSource(ElectricBlanketStateModel.URI,
									SetHighTemperature.class),
					new EventSink[] {
							new EventSink(
									ElectricBlanketElectricityModel.URI,
									SetHighTemperature.class)							
					});
			connections.put(
					new EventSource(ElectricBlanketStateModel.URI,
									DoNotHeat.class),
					new EventSink[] {
							new EventSink(
									ElectricBlanketElectricityModel.URI,
									DoNotHeat.class)							
					});	
			
			// coupled model descriptor
			coupledModelDescriptors.put(
					ElectricBlanketCoupledModel.URI,
					new RTCoupledModelDescriptor(
							ElectricBlanketCoupledModel.class,
							ElectricBlanketCoupledModel.URI,
							submodels,
							imported,
							null,
							connections,
							null,
							SimulationEngineCreationMode.COORDINATION_RT_ENGINE,
							accFactor));
			
			// simulation architecture
			this.setSimulationArchitecture(
					new RTArchitecture(
							simArchURI,
							ElectricBlanketCoupledModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							TimeUnit.HOURS,
							accFactor));
		} else {
			assert	!isUnitTesting;
			
			this.setSimulationArchitecture(
					new RTArchitecture(
							simArchURI,
							ElectricBlanketStateModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							TimeUnit.HOURS,
							accFactor));
		}
	}
	
}
