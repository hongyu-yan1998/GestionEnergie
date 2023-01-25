package fr.sorbonne_u.components.hem2022e3.equipments.heater.sil;

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
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.events.DoNotHeat;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.events.Heat;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.events.SwitchOffHeater;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.events.SwitchOnHeater;
import fr.sorbonne_u.devs_simulation.architectures.ArchitectureI;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.architectures.SimulationEngineCreationMode;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTAtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTCoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.simulators.SimulationEngine;

// -----------------------------------------------------------------------------
/**
 * The class <code>RunHeaterUnitaryRTSimulation</code> creates a simulator
 * for the heater and then runs a typical real time simulation.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This class shows how to use simulation model descriptors to create the
 * description of a simulation architecture and then create an instance of this
 * architecture by instantiating and connecting the models. Note how models
 * are described by atomic model descriptors and coupled model descriptors and
 * then the connections between coupled models and their submodels as well as
 * exported events and variables to imported ones are described by different
 * maps. In this example, only connections of events and bindings of variables
 * between models within this architecture are necessary, but when creating
 * coupled models, they can also import and export events and variables
 * consumed and produced by their submodels.
 * </p>
 * <p>
 * The architecture object is the root of this description and it provides
 * the method {@code constructSimulator} that instantiate the models and
 * connect them. This method returns the reference on the simulator attached
 * to the root coupled model in the architecture instance, which is then used
 * to perform simulation runs by calling the method
 * {@code doStandAloneSimulation}.
 * </p>
 * <p>
 * The descriptors and maps can be viewed as kinds of nodes in the abstract
 * syntax tree of an architectural language that does not have a concrete
 * syntax yet.
 * </p>
 * 
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code ACCELERATION_FACTOR > 0.0}
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no invariant
 * </pre>
 * 
 * <p>Created on : 2022-10-27</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			RunHeaterUnitaryRTSimulation
{
	/** acceleration factor for the real time simulation; with a factor 2.0,
	 *  the simulation runs two times faster than real time i.e., a run that
	 *  is supposed to take 10 seconds in real time will take 5 seconds to
	 *  execute.															*/
	protected static final double ACCELERATION_FACTOR = 600.0;

	public static void main(String[] args)
	{
		try {
			// map that will contain the atomic model descriptors to construct
			// the simulation architecture
			Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

			// the heater models simulating its electricity consumption, its
			// temperatures and the external temperature are atomic HIOA models
			// hence we use an AtomicHIOA_Descriptor(s)
			atomicModelDescriptors.put(
					HeaterElectricityModel.URI,
					RTAtomicHIOA_Descriptor.create(
							HeaterElectricityModel.class,
							HeaterElectricityModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
							ACCELERATION_FACTOR));
			atomicModelDescriptors.put(
					HeaterTemperatureModel.URI,
					RTAtomicHIOA_Descriptor.create(
							HeaterTemperatureModel.class,
							HeaterTemperatureModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
							ACCELERATION_FACTOR));
			atomicModelDescriptors.put(
					ExternalTemperatureModel.URI,
					RTAtomicHIOA_Descriptor.create(
							ExternalTemperatureModel.class,
							ExternalTemperatureModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
							ACCELERATION_FACTOR));
			// the heater state and unit tester models only exchanges event, an
			// atomic model hence we use an AtomicModelDescriptor
			atomicModelDescriptors.put(
					HeaterStateModel.URI,
					RTAtomicModelDescriptor.create(
							HeaterStateModel.class,
							HeaterStateModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
							ACCELERATION_FACTOR));
			atomicModelDescriptors.put(
					HeaterUnitUserModel.URI,
					RTAtomicModelDescriptor.create(
							HeaterUnitUserModel.class,
							HeaterUnitUserModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
							ACCELERATION_FACTOR));

			// map that will contain the coupled model descriptors to construct
			// the simulation architecture
			Map<String,CoupledModelDescriptor> coupledModelDescriptors =
																new HashMap<>();

			// the set of submodels of the coupled model, given by their URIs
			Set<String> submodels = new HashSet<String>();
			submodels.add(HeaterElectricityModel.URI);
			submodels.add(HeaterTemperatureModel.URI);
			submodels.add(ExternalTemperatureModel.URI);
			submodels.add(HeaterStateModel.URI);
			submodels.add(HeaterUnitUserModel.URI);
			
			// event exchanging connections between exporting and importing
			// models
			Map<EventSource,EventSink[]> connections = new HashMap<>();

			connections.put(
				new EventSource(HeaterUnitUserModel.URI, SwitchOnHeater.class),
				new EventSink[] {
					new EventSink(HeaterStateModel.URI, SwitchOnHeater.class)
				});
			connections.put(
				new EventSource(HeaterUnitUserModel.URI, SwitchOffHeater.class),
				new EventSink[] {
					new EventSink(HeaterStateModel.URI, SwitchOffHeater.class)
				});
			connections.put(
				new EventSource(HeaterUnitUserModel.URI, Heat.class),
				new EventSink[] {
					new EventSink(HeaterStateModel.URI, Heat.class)
				});
			connections.put(
				new EventSource(HeaterUnitUserModel.URI, DoNotHeat.class),
				new EventSink[] {
					new EventSink(HeaterStateModel.URI, DoNotHeat.class)
				});

			connections.put(
				new EventSource(HeaterStateModel.URI, SwitchOnHeater.class),
				new EventSink[] {
					new EventSink(HeaterElectricityModel.URI,
								  SwitchOnHeater.class)
				});
			connections.put(
				new EventSource(HeaterStateModel.URI, SwitchOffHeater.class),
				new EventSink[] {
					new EventSink(HeaterElectricityModel.URI,
								  SwitchOffHeater.class)
				});
			connections.put(
				new EventSource(HeaterStateModel.URI, Heat.class),
				new EventSink[] {
					new EventSink(HeaterElectricityModel.URI, Heat.class),
					new EventSink(HeaterTemperatureModel.URI, Heat.class)
				});
			connections.put(
				new EventSource(HeaterStateModel.URI, DoNotHeat.class),
				new EventSink[] {
					new EventSink(HeaterElectricityModel.URI, DoNotHeat.class),
					new EventSink(HeaterTemperatureModel.URI, DoNotHeat.class)
				});

			// variable bindings between exporting and importing models
			Map<VariableSource,VariableSink[]> bindings =
								new HashMap<VariableSource,VariableSink[]>();

			bindings.put(
				new VariableSource("externalTemperature", Double.class,
								   ExternalTemperatureModel.URI),
				new VariableSink[] {
					new VariableSink("externalTemperature", Double.class,
									 HeaterTemperatureModel.URI)
				});

			// coupled model descriptor
			coupledModelDescriptors.put(
					HeaterCoupledModel.URI,
					new RTCoupledHIOA_Descriptor(
							HeaterCoupledModel.class,
							HeaterCoupledModel.URI,
							submodels,
							null,
							null,
							connections,
							null,
							SimulationEngineCreationMode.COORDINATION_RT_ENGINE,
							null,
							null,
							bindings,
							ACCELERATION_FACTOR));

			// simulation architecture
			ArchitectureI architecture =
					new RTArchitecture(
							HeaterCoupledModel.URI,
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
					+ ((long)((simDuration*3600*1000.0)/ACCELERATION_FACTOR))
					+ 2000L);
			System.exit(0);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}
// -----------------------------------------------------------------------------
