package fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.events.SwitchLightOffIndoorGarden;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.events.SwitchLightOnIndoorGarden;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.events.SwitchOffIndoorGarden;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.events.SwitchOnIndoorGarden;
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

// -----------------------------------------------------------------------------
/**
 * The class <code>RunIndoorGardenUnitaryRTSimulation</code> is the main class
 * used to run real time simulations on the models of the indoor garden in
 * isolation.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code ACC_FACTOR > 0.0}
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// TODO	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2022-11-08</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			RunIndoorGardenUnitaryRTMILSimulation
{
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
				IndoorGardenElectricityModel.URI,
				RTAtomicHIOA_Descriptor.create(
						IndoorGardenElectricityModel.class,
						IndoorGardenElectricityModel.URI,
						TimeUnit.HOURS,
						null,
						SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
						ACC_FACTOR));
			// for atomic model, we use an AtomicModelDescriptor
			atomicModelDescriptors.put(
				IndoorGardenStateModel.URI,
				RTAtomicModelDescriptor.create(
						IndoorGardenStateModel.class,
						IndoorGardenStateModel.URI,
						TimeUnit.HOURS,
						null,
						SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
						ACC_FACTOR));
			atomicModelDescriptors.put(
				IndoorGardenTestModel.URI,
				RTAtomicModelDescriptor.create(
						IndoorGardenTestModel.class,
						IndoorGardenTestModel.URI,
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
			submodels.add(IndoorGardenElectricityModel.URI);
			submodels.add(IndoorGardenStateModel.URI);
			submodels.add(IndoorGardenTestModel.URI);

			// event exchanging connections between exporting and importing
			// models
			Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();

			connections.put(
					new EventSource(IndoorGardenTestModel.URI,
									SwitchOnIndoorGarden.class),
					new EventSink[] {
							new EventSink(IndoorGardenStateModel.URI,
										  SwitchOnIndoorGarden.class)
							});
			connections.put(
					new EventSource(IndoorGardenTestModel.URI,
									SwitchOffIndoorGarden.class),
					new EventSink[] {
							new EventSink(IndoorGardenStateModel.URI,
										  SwitchOffIndoorGarden.class)
					});
			connections.put(
					new EventSource(IndoorGardenTestModel.URI,
									SwitchLightOnIndoorGarden.class),
					new EventSink[] {
							new EventSink(IndoorGardenStateModel.URI,
										  SwitchLightOnIndoorGarden.class)
					});
			connections.put(
					new EventSource(IndoorGardenTestModel.URI,
									SwitchLightOffIndoorGarden.class),
					new EventSink[] {
							new EventSink(IndoorGardenStateModel.URI,
										  SwitchLightOffIndoorGarden.class)
					});

			connections.put(
					new EventSource(IndoorGardenStateModel.URI,
									SwitchOnIndoorGarden.class),
					new EventSink[] {
							new EventSink(IndoorGardenElectricityModel.URI,
										  SwitchOnIndoorGarden.class)
					});
			connections.put(
					new EventSource(IndoorGardenStateModel.URI,
									SwitchOffIndoorGarden.class),
					new EventSink[] {
							new EventSink(IndoorGardenElectricityModel.URI,
										  SwitchOffIndoorGarden.class)
					});
			connections.put(
					new EventSource(IndoorGardenStateModel.URI,
									SwitchLightOnIndoorGarden.class),
					new EventSink[] {
							new EventSink(IndoorGardenElectricityModel.URI,
										  SwitchLightOnIndoorGarden.class)
					});
			connections.put(
					new EventSource(IndoorGardenStateModel.URI,
									SwitchLightOffIndoorGarden.class),
					new EventSink[] {
							new EventSink(IndoorGardenElectricityModel.URI,
										  SwitchLightOffIndoorGarden.class)
					});

			// coupled model descriptor
			coupledModelDescriptors.put(
					IndoorGardenCoupledModel.URI,
					new RTCoupledModelDescriptor(
							IndoorGardenCoupledModel.class,
							IndoorGardenCoupledModel.URI,
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
							IndoorGardenCoupledModel.URI,
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
// -----------------------------------------------------------------------------
