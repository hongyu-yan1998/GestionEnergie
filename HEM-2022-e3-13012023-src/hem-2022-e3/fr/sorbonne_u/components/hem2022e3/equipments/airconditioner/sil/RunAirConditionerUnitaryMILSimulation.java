/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.sil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.sil.events.TurnOffAirConditioner;
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.sil.events.TurnOnAirConditioner;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.architectures.ArchitectureI;
import fr.sorbonne_u.devs_simulation.architectures.SimulationEngineCreationMode;
import fr.sorbonne_u.devs_simulation.hioa.architectures.AtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.simulators.SimulationEngine;

/**
 * The class <code>RunAirConditionerUnitaryMILSimulation</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-02</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class RunAirConditionerUnitaryMILSimulation {
	public static void	main(String[] args)
	{
		try {
			// map that will contain the atomic model descriptors to construct
			// the simulation architecture
			Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
																new HashMap<>();

			// the air conditioner model simulating its electricity consumption,
			// an atomic HIOA model hence we use an AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					AirConditionerElectricityModel.URI,
					AtomicHIOA_Descriptor.create(
							AirConditionerElectricityModel.class,
							AirConditionerElectricityModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_ENGINE));
			// for atomic model, we use an AtomicModelDescriptor
			atomicModelDescriptors.put(
					AirConditionerStateModel.URI,
					AtomicModelDescriptor.create(
							AirConditionerStateModel.class,
							AirConditionerStateModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_ENGINE));
			atomicModelDescriptors.put(
					AirConditionerTestModel.URI,
					AtomicModelDescriptor.create(
							AirConditionerTestModel.class,
							AirConditionerTestModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_ENGINE));


			// map that will contain the coupled model descriptors to construct
			// the simulation architecture
			Map<String,CoupledModelDescriptor> coupledModelDescriptors =
																new HashMap<>();

			// the set of submodels of the coupled model, given by their URIs
			Set<String> submodels = new HashSet<String>();
			submodels.add(AirConditionerElectricityModel.URI);
			submodels.add(AirConditionerStateModel.URI);
			submodels.add(AirConditionerTestModel.URI);

			// event exchanging connections between exporting and importing
			// models
			Map<EventSource,EventSink[]> connections =
										new HashMap<EventSource,EventSink[]>();

			connections.put(
					new EventSource(AirConditionerTestModel.URI,
									TurnOnAirConditioner.class),
					new EventSink[] {
							new EventSink(AirConditionerStateModel.URI,
									TurnOnAirConditioner.class)
					});
			connections.put(
					new EventSource(AirConditionerTestModel.URI,
									TurnOffAirConditioner.class),
					new EventSink[] {
							new EventSink(AirConditionerStateModel.URI,
									TurnOffAirConditioner.class)
					});
			
			connections.put(
					new EventSource(AirConditionerStateModel.URI,
									TurnOnAirConditioner.class),
					new EventSink[] {
							new EventSink(AirConditionerElectricityModel.URI,
									TurnOnAirConditioner.class)
					});
			connections.put(
					new EventSource(AirConditionerStateModel.URI,
									TurnOffAirConditioner.class),
					new EventSink[] {
							new EventSink(AirConditionerElectricityModel.URI,
									TurnOffAirConditioner.class)
					});

			// coupled model descriptor
			coupledModelDescriptors.put(
					AirConditionerCoupledModel.URI,
					new CoupledModelDescriptor(
							AirConditionerCoupledModel.class,
							AirConditionerCoupledModel.URI,
							submodels,
							null,
							null,
							connections,
							null,
							SimulationEngineCreationMode.COORDINATION_ENGINE));

			// simulation architecture
			ArchitectureI architecture =
					new Architecture(
							AirConditionerCoupledModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							TimeUnit.HOURS);

			// create the simulator from the simulation architecture
			SimulationEngine se = architecture.constructSimulator();
			// this add additional time at each simulation step in
			// standard simulations (useful when debugging)
			SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 0L;
			// run a simulation with the simulation beginning at 0.0 and
			// ending at 10.0
			se.doStandAloneSimulation(0.0, 24.0);
			System.exit(0);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}
