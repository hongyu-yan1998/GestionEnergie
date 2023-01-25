/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.DoNotRun;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.Run;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.StartRefrigerator;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.StopRefrigerator;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.architectures.ArchitectureI;
import fr.sorbonne_u.devs_simulation.architectures.SimulationEngineCreationMode;
import fr.sorbonne_u.devs_simulation.hioa.architectures.AtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.CoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.simulators.SimulationEngine;

/**
 * The class <code>RunRefrigeratorUnitarySimulation</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-04</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class RunRefrigeratorUnitarySimulation {
	public static void main(String[] args)
	{
		try {
			// map that will contain the atomic model descriptors to construct
			// the simulation architecture
			Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

			// the refrigerator models simulating its electricity consumption, its
			// temperatures and the external temperature are atomic HIOA models
			// hence we use an AtomicHIOA_Descriptor(s)
			atomicModelDescriptors.put(
					RefrigeratorElectricityModel.URI,
					AtomicHIOA_Descriptor.create(
							RefrigeratorElectricityModel.class,
							RefrigeratorElectricityModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_ENGINE));
			atomicModelDescriptors.put(
					RefrigeratorTemperatureModel.URI,
					AtomicHIOA_Descriptor.create(
							RefrigeratorTemperatureModel.class,
							RefrigeratorTemperatureModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_ENGINE));
			// the refrigerator state and unit tester models only exchanges event, an
			// atomic model hence we use an AtomicModelDescriptor
			atomicModelDescriptors.put(
					RefrigeratorStateModel.URI,
					AtomicModelDescriptor.create(
							RefrigeratorStateModel.class,
							RefrigeratorStateModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_ENGINE));
			atomicModelDescriptors.put(
					RefrigeratorUnitUserModel.URI,
					AtomicModelDescriptor.create(
							RefrigeratorUnitUserModel.class,
							RefrigeratorUnitUserModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_ENGINE));

			// map that will contain the coupled model descriptors to construct
			// the simulation architecture
			Map<String,CoupledModelDescriptor> coupledModelDescriptors =
																new HashMap<>();

			// the set of submodels of the coupled model, given by their URIs
			Set<String> submodels = new HashSet<String>();
			submodels.add(RefrigeratorElectricityModel.URI);
			submodels.add(RefrigeratorTemperatureModel.URI);
			submodels.add(RefrigeratorStateModel.URI);
			submodels.add(RefrigeratorUnitUserModel.URI);
			
			// event exchanging connections between exporting and importing
			// models
			Map<EventSource,EventSink[]> connections = new HashMap<>();

			connections.put(
				new EventSource(RefrigeratorUnitUserModel.URI, StartRefrigerator.class),
				new EventSink[] {
					new EventSink(RefrigeratorStateModel.URI, StartRefrigerator.class)
				});
			connections.put(
				new EventSource(RefrigeratorUnitUserModel.URI, StopRefrigerator.class),
				new EventSink[] {
					new EventSink(RefrigeratorStateModel.URI, StopRefrigerator.class)
				});
			connections.put(
				new EventSource(RefrigeratorUnitUserModel.URI, Run.class),
				new EventSink[] {
					new EventSink(RefrigeratorStateModel.URI, Run.class)
				});
			connections.put(
				new EventSource(RefrigeratorUnitUserModel.URI, DoNotRun.class),
				new EventSink[] {
					new EventSink(RefrigeratorStateModel.URI, DoNotRun.class)
				});

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

			// coupled model descriptor
			coupledModelDescriptors.put(
					RefrigeratorCoupledModel.URI,
					new CoupledHIOA_Descriptor(
							RefrigeratorCoupledModel.class,
							RefrigeratorCoupledModel.URI,
							submodels,
							null,
							null,
							connections,
							null,
							SimulationEngineCreationMode.COORDINATION_ENGINE,
							null,
							null,
							null));

			// simulation architecture
			ArchitectureI architecture =
					new Architecture(
							RefrigeratorCoupledModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							TimeUnit.HOURS);

			// create the simulator from the simulation architecture
			SimulationEngine se = architecture.constructSimulator();
			// this add additional time at each simulation step in
			// standard simulations (useful when debugging)
			SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 0L;
			// run a simulation with the simulation beginning at 0.0 and
			// ending at 24.0
			se.doStandAloneSimulation(0.0, 24.0);
			System.exit(0);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}
