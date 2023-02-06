/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.solar.sil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.sil.events.SolarNotProduce;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.sil.events.SolarProduce;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.architectures.ArchitectureI;
import fr.sorbonne_u.devs_simulation.architectures.SimulationEngineCreationMode;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTAtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTCoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.simulators.SimulationEngine;

/**
 * The class <code>RunScolarPanelUnitaryRTMILSimulation</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-04</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class RunScolarPanelUnitaryRTMILSimulation {
	
	protected static final double ACC_FACTOR = 4800.0;	// 24h = 18s
	
	public static void	main(String[] args)
	{
		try {
			// map that will contain the atomic model descriptors to construct
			// the simulation architecture
			Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

			// the scolar panel models simulating its electricity consumption, its
			// temperatures and the external temperature are atomic HIOA models
			// hence we use an RTAtomicHIOA_Descriptor(s)
			atomicModelDescriptors.put(
					SolarPanelElectricityModel.URI,
					RTAtomicHIOA_Descriptor.create(
							SolarPanelElectricityModel.class,
							SolarPanelElectricityModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
							ACC_FACTOR));
			// for atomic model, we use an AtomicModelDescriptor
			atomicModelDescriptors.put(
					SolarPanelStateModel.URI,
					RTAtomicModelDescriptor.create(
							SolarPanelStateModel.class,
							SolarPanelStateModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
							ACC_FACTOR));
			atomicModelDescriptors.put(
					SolarPanelTestModel.URI,
					RTAtomicModelDescriptor.create(
							SolarPanelTestModel.class,
							SolarPanelTestModel.URI,
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
			submodels.add(SolarPanelElectricityModel.URI);
			submodels.add(SolarPanelStateModel.URI);
			submodels.add(SolarPanelTestModel.URI);
			
			// event exchanging connections between exporting and importing
			// models
			Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();

			connections.put(
					new EventSource(SolarPanelTestModel.URI,
									SolarProduce.class),
					new EventSink[] {
							new EventSink(SolarPanelStateModel.URI,
									SolarProduce.class)
					});
			connections.put(
					new EventSource(SolarPanelTestModel.URI,
									SolarNotProduce.class),
					new EventSink[] {
							new EventSink(SolarPanelStateModel.URI,
									SolarNotProduce.class)
					});
			connections.put(
					new EventSource(SolarPanelStateModel.URI,
									SolarProduce.class),
					new EventSink[] {
							new EventSink(SolarPanelElectricityModel.URI,
									SolarProduce.class)
					});
			connections.put(
					new EventSource(SolarPanelStateModel.URI,
									SolarNotProduce.class),
					new EventSink[] {
							new EventSink(SolarPanelElectricityModel.URI,
									SolarNotProduce.class)
					});

			// coupled model descriptor
			coupledModelDescriptors.put(
					SolarPanelCoupledModel.URI,
					new RTCoupledModelDescriptor(
							SolarPanelCoupledModel.class,
							SolarPanelCoupledModel.URI,
							submodels,
							null,
							null,
							connections,
							null,
							SimulationEngineCreationMode.COORDINATION_RT_ENGINE,
							ACC_FACTOR));

			// simulation architecture
			ArchitectureI architecture =
					new Architecture(
							SolarPanelCoupledModel.URI,
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
