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
import fr.sorbonne_u.devs_simulation.architectures.ArchitectureI;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.architectures.SimulationEngineCreationMode;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTAtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTCoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.simulators.SimulationEngine;

/**
 * The class <code>RunAirConditionerUnitaryRTMILSimulation</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-02</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class RunAirConditionerUnitaryRTMILSimulation {
	/** acceleration factor for the real time simulation; with a factor 2.0,
	 *  the simulation runs two times faster than real time i.e., a run that
	 *  is supposed to take 10 seconds in real time will take 5 seconds to
	 *  execute.															*/
	protected static final double ACC_FACTOR = 4800.0;	// 24h = 18s

	public static void	main(String[] args)
	{
		try {
			// map that will contain the atomic model descriptors to construct
			// the simulation architecture
			Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
																new HashMap<>();

			// the indoor garden model simulating its electricity consumption,
			// an atomic HIOA model hence we use an RTAtomicHIOA_Descriptor
			atomicModelDescriptors.put(
				AirConditionerElectricityModel.URI,
				RTAtomicHIOA_Descriptor.create(
						AirConditionerElectricityModel.class,
						AirConditionerElectricityModel.URI,
						TimeUnit.HOURS,
						null,
						SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
						ACC_FACTOR));
			// for atomic model, we use an AtomicModelDescriptor
			atomicModelDescriptors.put(
				AirConditionerStateModel.URI,
				RTAtomicModelDescriptor.create(
						AirConditionerStateModel.class,
						AirConditionerStateModel.URI,
						TimeUnit.HOURS,
						null,
						SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
						ACC_FACTOR));
			atomicModelDescriptors.put(
				AirConditionerTestModel.URI,
				RTAtomicModelDescriptor.create(
						AirConditionerTestModel.class,
						AirConditionerTestModel.URI,
						TimeUnit.HOURS,
						null,
						SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
						ACC_FACTOR));

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
					new RTCoupledModelDescriptor(
							AirConditionerCoupledModel.class,
							AirConditionerCoupledModel.URI,
							submodels,
							null,
							null,
							connections,
							null,
							SimulationEngineCreationMode.COORDINATION_RT_ENGINE,
							ACC_FACTOR));

			// simulation architecture
			ArchitectureI architecture =
					new RTArchitecture(
							AirConditionerCoupledModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							TimeUnit.HOURS);

			// create the simulator from the simulation architecture
			SimulationEngine se = architecture.constructSimulator();

			// duration of the simulation in hours, the simulation time unit
			double simDuration = 24.0;
			// the real time of start of the simulation plus a 1s delay to give
			// the time to initialise all models in the architecture.
			long start = System.currentTimeMillis() + 100;
			se.startRTSimulation(start, 0.0, simDuration);
			// wait until the simulation ends i.e., the start delay  plus the
			// duration of the simulation in milliseconds plus another 2s delay
			// to make sure...
			Thread.sleep(
					1000L
					+ ((long)((simDuration*3600*1000.0)/ACC_FACTOR))
					+ 2000L);
			System.exit(0);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}
