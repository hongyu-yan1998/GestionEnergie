package fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.events.DoNotHeat;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.events.SetHighTemperature;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.events.SetLowTemperature;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.events.SwitchOffBlanket;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.events.SwitchOnBlanket;
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

//-----------------------------------------------------------------------------
/**
* The class <code>RunElectricBlanketUnitaryMILSimulation</code> is the main class
* used to run simulations on the models of the electric blanket in isolation.
*
* <p><strong>Description</strong></p>
* 
* <p><strong>Invariant</strong></p>
* 
* <pre>
* invariant	{@code true}	// no more invariants
* </pre>
* 
* @author Liuyi CHEN & Hongyu YAN
* @date 17 nov. 2022
*/
//-----------------------------------------------------------------------------
public class RunElectricBlanketUnitaryMILSimulation {
	public static void main(String[] args)
	{
		try {
			// map that will contain the atomic model descriptors to construct
			// the simulation architecture
			Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
																new HashMap<>();
			// the indoor garden model simulating its electricity consumption,
			// an atomic HIOA model hence we use an AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					ElectricBlanketElectricityModel.URI,
					AtomicHIOA_Descriptor.create(
							ElectricBlanketElectricityModel.class,
							ElectricBlanketElectricityModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_ENGINE));
			// for atomic model, we use an AtomicModelDescriptor
			atomicModelDescriptors.put(
					ElectricBlanketStateModel.URI,
					AtomicModelDescriptor.create(
							ElectricBlanketStateModel.class,
							ElectricBlanketStateModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_ENGINE));
			atomicModelDescriptors.put(
					ElectricBlanketTestModel.URI,
					AtomicModelDescriptor.create(
							ElectricBlanketTestModel.class,
							ElectricBlanketTestModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_ENGINE));
			
			// map that will contain the coupled model descriptors to construct
			// the simulation architecture
			Map<String,CoupledModelDescriptor> coupledModelDescriptors =
																new HashMap<>();

			// the set of submodels of the coupled model, given by their URIs
			Set<String> submodels = new HashSet<String>();
			submodels.add(ElectricBlanketElectricityModel.URI);
			submodels.add(ElectricBlanketStateModel.URI);
			submodels.add(ElectricBlanketTestModel.URI);
			
			// event exchanging connections between exporting and importing
			// models
			Map<EventSource,EventSink[]> connections =
										new HashMap<EventSource,EventSink[]>();

			//ElectricBlanketTestModel -->ElectricBlanketStateModel
			connections.put(
					new EventSource(ElectricBlanketTestModel.URI,
									SwitchOnBlanket.class),
					new EventSink[] {
							new EventSink(ElectricBlanketStateModel.URI,
									SwitchOnBlanket.class)
					});
			connections.put(
					new EventSource(ElectricBlanketTestModel.URI,
									SwitchOffBlanket.class),
					new EventSink[] {
							new EventSink(ElectricBlanketStateModel.URI,
									SwitchOffBlanket.class)
					});
			connections.put(
					new EventSource(ElectricBlanketTestModel.URI,
									SetHighTemperature.class),
					new EventSink[] {
							new EventSink(ElectricBlanketStateModel.URI,
							SetHighTemperature.class)
					});
			connections.put(
					new EventSource(ElectricBlanketTestModel.URI,
									SetLowTemperature.class),
					new EventSink[] {
							new EventSink(ElectricBlanketStateModel.URI,
							SetLowTemperature.class)
					});
			connections.put(
					new EventSource(ElectricBlanketTestModel.URI,
									DoNotHeat.class),
					new EventSink[] {
							new EventSink(ElectricBlanketStateModel.URI,
									DoNotHeat.class)
					});

			//ElectricBlanketStateModel --> ElectricBlanketElectricityModel
			connections.put(
					new EventSource(ElectricBlanketStateModel.URI,
							SwitchOnBlanket.class),
					new EventSink[] {
							new EventSink(ElectricBlanketElectricityModel.URI,
							SwitchOnBlanket.class)
					});
			connections.put(
				new EventSource(ElectricBlanketStateModel.URI,
						SwitchOffBlanket.class),
				new EventSink[] {
						new EventSink(ElectricBlanketElectricityModel.URI,
						SwitchOffBlanket.class)
				});
			connections.put(
				new EventSource(ElectricBlanketStateModel.URI,
						SetHighTemperature.class),
				new EventSink[] {
						new EventSink(ElectricBlanketElectricityModel.URI,
						SetHighTemperature.class)
				});
			connections.put(
				new EventSource(ElectricBlanketStateModel.URI,
						SetLowTemperature.class),
				new EventSink[] {
						new EventSink(ElectricBlanketElectricityModel.URI,
						SetLowTemperature.class)
				});
			connections.put(
				new EventSource(ElectricBlanketStateModel.URI,
						DoNotHeat.class),
				new EventSink[] {
						new EventSink(ElectricBlanketElectricityModel.URI,
						DoNotHeat.class)
				});
			
			// coupled model descriptor
			coupledModelDescriptors.put(
					ElectricBlanketCoupledModel.URI,
					new CoupledModelDescriptor(
							ElectricBlanketCoupledModel.class,
							ElectricBlanketCoupledModel.URI,
							submodels,
							null,
							null,
							connections,
							null,
							SimulationEngineCreationMode.COORDINATION_ENGINE
					));
			// simulation architecture
			ArchitectureI architecture =
					new Architecture(
							ElectricBlanketCoupledModel.URI,
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
