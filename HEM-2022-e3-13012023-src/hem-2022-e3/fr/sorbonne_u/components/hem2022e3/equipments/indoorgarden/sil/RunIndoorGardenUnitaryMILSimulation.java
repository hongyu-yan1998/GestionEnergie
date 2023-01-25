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

// -----------------------------------------------------------------------------
/**
 * The class <code>RunIndoorGardenUnitarySimulation</code> is the main class
 * used to run simulations on the models of the indoor garden in isolation.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no invariant
 * </pre>
 * 
 * <p>Created on : 2022-10-04</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			RunIndoorGardenUnitaryMILSimulation
{
	public static void	main(String[] args)
	{
		try {
			// map that will contain the atomic model descriptors to construct
			// the simulation architecture
			Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
																new HashMap<>();

			// the indoor garden model simulating its electricity consumption,
			// an atomic HIOA model hence we use an AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					IndoorGardenElectricityModel.URI,
					AtomicHIOA_Descriptor.create(
							IndoorGardenElectricityModel.class,
							IndoorGardenElectricityModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_ENGINE));
			// for atomic model, we use an AtomicModelDescriptor
			atomicModelDescriptors.put(
					IndoorGardenStateModel.URI,
					AtomicModelDescriptor.create(
							IndoorGardenStateModel.class,
							IndoorGardenStateModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_ENGINE));
			atomicModelDescriptors.put(
					IndoorGardenTestModel.URI,
					AtomicModelDescriptor.create(
							IndoorGardenTestModel.class,
							IndoorGardenTestModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_ENGINE));


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
					new CoupledModelDescriptor(
							IndoorGardenCoupledModel.class,
							IndoorGardenCoupledModel.URI,
							submodels,
							null,
							null,
							connections,
							null,
							SimulationEngineCreationMode.COORDINATION_ENGINE));

			// simulation architecture
			ArchitectureI architecture =
					new Architecture(
							IndoorGardenCoupledModel.URI,
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
// -----------------------------------------------------------------------------
