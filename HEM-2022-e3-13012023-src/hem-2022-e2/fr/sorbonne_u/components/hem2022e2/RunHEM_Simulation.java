package fr.sorbonne_u.components.hem2022e2;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a basic
// household management systems as an example of a cyber-physical system.
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

import fr.sorbonne_u.components.hem2022e2.HEM_CoupledModel.HEM_Report;
import fr.sorbonne_u.components.hem2022e2.equipments.hairdryer.mil.HairDryerElectricityModel;
import fr.sorbonne_u.components.hem2022e2.equipments.hairdryer.mil.HairDryerUserModel;
import fr.sorbonne_u.components.hem2022e2.equipments.hairdryer.mil.events.SetHighHairDryer;
import fr.sorbonne_u.components.hem2022e2.equipments.hairdryer.mil.events.SetLowHairDryer;
import fr.sorbonne_u.components.hem2022e2.equipments.hairdryer.mil.events.SwitchOffHairDryer;
import fr.sorbonne_u.components.hem2022e2.equipments.hairdryer.mil.events.SwitchOnHairDryer;
import fr.sorbonne_u.components.hem2022e2.equipments.heater.mil.ExternalTemperatureModel;
import fr.sorbonne_u.components.hem2022e2.equipments.heater.mil.HeaterElectricityModel;
import fr.sorbonne_u.components.hem2022e2.equipments.heater.mil.HeaterTemperatureModel;
import fr.sorbonne_u.components.hem2022e2.equipments.heater.mil.HeaterUnitTesterModel;
import fr.sorbonne_u.components.hem2022e2.equipments.heater.mil.events.DoNotHeat;
import fr.sorbonne_u.components.hem2022e2.equipments.heater.mil.events.Heat;
import fr.sorbonne_u.components.hem2022e2.equipments.heater.mil.events.SwitchOffHeater;
import fr.sorbonne_u.components.hem2022e2.equipments.heater.mil.events.SwitchOnHeater;
import fr.sorbonne_u.components.hem2022e2.equipments.indoorgarden.mil.IndoorGardenElectricityModel;
import fr.sorbonne_u.components.hem2022e2.equipments.indoorgarden.mil.IndoorGardenTestModel;
import fr.sorbonne_u.components.hem2022e2.equipments.indoorgarden.mil.events.SwitchLightOffIndoorGarden;
import fr.sorbonne_u.components.hem2022e2.equipments.indoorgarden.mil.events.SwitchLightOnIndoorGarden;
import fr.sorbonne_u.components.hem2022e2.equipments.indoorgarden.mil.events.SwitchOffIndoorGarden;
import fr.sorbonne_u.components.hem2022e2.equipments.indoorgarden.mil.events.SwitchOnIndoorGarden;
import fr.sorbonne_u.components.hem2022e2.equipments.meter.mil.ElectricMeterElectricityModel;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.architectures.ArchitectureI;
import fr.sorbonne_u.devs_simulation.architectures.SimulationEngineCreationMode;
import fr.sorbonne_u.devs_simulation.hioa.architectures.AtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.CoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.SimulationEngine;

// -----------------------------------------------------------------------------
/**
 * The class <code>RunHEM_Simulation</code> creates the simulator for the
 * household energy management example and then runs a typical simulation.
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
 * {@code doStandAloneSimulation}. Notice the use of the method
 * {@code setSimulationRunParameters} to initialise some parameters of
 * the simulation defined in the different models. This method is implemented
 * to traverse all of the models, hence each one can get its own parameters by
 * carefully defining unique names for them. Also, it shows how to get the
 * simulation reports from the models after the simulation run.
 * </p>
 * <p>
 * The descriptors and maps can be viewed as kinds of nodes in the abstract
 * syntax tree of an architectural language that does not have a concrete
 * syntax yet.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no invariant
 * </pre>
 * 
 * <p>Created on : 2021-09-24</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			RunHEM_Simulation
{
	public static void	main(String[] args)
	{
		Time.setPrintPrecision(4);
		Duration.setPrintPrecision(4);

		try {
			// map that will contain the atomic model descriptors to construct
			// the simulation architecture
			Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
																new HashMap<>();

			// atomic HIOA models require AtomicHIOA_Descriptor while
			// atomic models require AtomicModelDescriptor

			// hair dryer models
			atomicModelDescriptors.put(
					HairDryerElectricityModel.URI,
					AtomicHIOA_Descriptor.create(
							HairDryerElectricityModel.class,
							HairDryerElectricityModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_ENGINE));
			atomicModelDescriptors.put(
					HairDryerUserModel.URI,
					AtomicModelDescriptor.create(
							HairDryerUserModel.class,
							HairDryerUserModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_ENGINE));

			// the heater models
			atomicModelDescriptors.put(
					HeaterElectricityModel.URI,
					AtomicHIOA_Descriptor.create(
							HeaterElectricityModel.class,
							HeaterElectricityModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_ENGINE));
			atomicModelDescriptors.put(
					HeaterTemperatureModel.URI,
					AtomicHIOA_Descriptor.create(
							HeaterTemperatureModel.class,
							HeaterTemperatureModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_ENGINE));
			atomicModelDescriptors.put(
					ExternalTemperatureModel.URI,
					AtomicHIOA_Descriptor.create(
							ExternalTemperatureModel.class,
							ExternalTemperatureModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_ENGINE));
			atomicModelDescriptors.put(
					HeaterUnitTesterModel.URI,
					AtomicModelDescriptor.create(
							HeaterUnitTesterModel.class,
							HeaterUnitTesterModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_ENGINE));

			// the indoor garden models
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
					IndoorGardenTestModel.URI,
					AtomicModelDescriptor.create(
							IndoorGardenTestModel.class,
							IndoorGardenTestModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_ENGINE));

			// the electric meter model
			atomicModelDescriptors.put(
					ElectricMeterElectricityModel.URI,
					AtomicHIOA_Descriptor.create(
							ElectricMeterElectricityModel.class,
							ElectricMeterElectricityModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_ENGINE));

			// map that will contain the coupled model descriptors to construct
			// the simulation architecture
			Map<String,CoupledModelDescriptor> coupledModelDescriptors =
																new HashMap<>();

			// the set of submodels of the coupled model, given by their URIs
			Set<String> submodels = new HashSet<String>();
			submodels.add(HairDryerElectricityModel.URI);
			submodels.add(HairDryerUserModel.URI);
			submodels.add(HeaterElectricityModel.URI);
			submodels.add(HeaterTemperatureModel.URI);
			submodels.add(ExternalTemperatureModel.URI);
			submodels.add(HeaterUnitTesterModel.URI);
			submodels.add(IndoorGardenElectricityModel.URI);
			submodels.add(IndoorGardenTestModel.URI);
			submodels.add(ElectricMeterElectricityModel.URI);

			// event exchanging connections between exporting and importing
			// models
			Map<EventSource,EventSink[]> connections =
										new HashMap<EventSource,EventSink[]>();

			connections.put(
					new EventSource(HairDryerUserModel.URI, SwitchOnHairDryer.class),
					new EventSink[] {
							new EventSink(HairDryerElectricityModel.URI,
										  SwitchOnHairDryer.class)
					});
			connections.put(
					new EventSource(HairDryerUserModel.URI, SwitchOffHairDryer.class),
					new EventSink[] {
							new EventSink(HairDryerElectricityModel.URI,
										  SwitchOffHairDryer.class)
					});
			connections.put(
					new EventSource(HairDryerUserModel.URI, SetHighHairDryer.class),
					new EventSink[] {
							new EventSink(HairDryerElectricityModel.URI,
										  SetHighHairDryer.class)
					});
			connections.put(
					new EventSource(HairDryerUserModel.URI, SetLowHairDryer.class),
					new EventSink[] {
							new EventSink(HairDryerElectricityModel.URI,
										  SetLowHairDryer.class)
					});

			connections.put(
					new EventSource(HeaterUnitTesterModel.URI,
									SwitchOnHeater.class),
					new EventSink[] {
							new EventSink(HeaterElectricityModel.URI,
										  SwitchOnHeater.class)
					});
			connections.put(
					new EventSource(HeaterUnitTesterModel.URI,
									SwitchOffHeater.class),
					new EventSink[] {
							new EventSink(HeaterElectricityModel.URI,
										  SwitchOffHeater.class)
					});
			connections.put(
					new EventSource(HeaterUnitTesterModel.URI, Heat.class),
					new EventSink[] {
							new EventSink(HeaterElectricityModel.URI,
										  Heat.class),
							new EventSink(HeaterTemperatureModel.URI,
										  Heat.class)
					});
			connections.put(
					new EventSource(HeaterUnitTesterModel.URI, DoNotHeat.class),
					new EventSink[] {
							new EventSink(HeaterElectricityModel.URI,
										  DoNotHeat.class),
							new EventSink(HeaterTemperatureModel.URI,
										  DoNotHeat.class)
					});
			connections.put(
					new EventSource(IndoorGardenTestModel.URI,
									SwitchOnIndoorGarden.class),
					new EventSink[] {
							new EventSink(IndoorGardenElectricityModel.URI,
										  SwitchOnIndoorGarden.class)
					});
			connections.put(
					new EventSource(IndoorGardenTestModel.URI,
									SwitchOffIndoorGarden.class),
					new EventSink[] {
							new EventSink(IndoorGardenElectricityModel.URI,
										  SwitchOffIndoorGarden.class)
					});
			connections.put(
					new EventSource(IndoorGardenTestModel.URI,
									SwitchLightOnIndoorGarden.class),
					new EventSink[] {
							new EventSink(IndoorGardenElectricityModel.URI,
										  SwitchLightOnIndoorGarden.class)
					});
			connections.put(
					new EventSource(IndoorGardenTestModel.URI,
									SwitchLightOffIndoorGarden.class),
					new EventSink[] {
							new EventSink(IndoorGardenElectricityModel.URI,
										  SwitchLightOffIndoorGarden.class)
					});

			// variable bindings between exporting and importing models
			Map<VariableSource,VariableSink[]> bindings =
					new HashMap<VariableSource,VariableSink[]>();

			// bindings among heater models
			VariableSource source1 =
					new VariableSource("externalTemperature",
									   Double.class,
									   ExternalTemperatureModel.URI);
			VariableSink[] sinks1 =
					new VariableSink[] {
							new VariableSink("externalTemperature",
											 Double.class,
											 HeaterTemperatureModel.URI)
					};
			bindings.put(source1, sinks1);

			// bindings between hair dryer and heater models to the electric
			// meter model
			VariableSource source2 =
					new VariableSource("currentIntensity",
									   Double.class,
									   HairDryerElectricityModel.URI);
			VariableSink[] sinks2 =
					new VariableSink[] {
							new VariableSink("currentHairDryerIntensity",
											 Double.class,
											 ElectricMeterElectricityModel.URI)
					};
			VariableSource source3 =
					new VariableSource("currentIntensity",
									   Double.class,
									   HeaterElectricityModel.URI);
			VariableSink[] sinks3 =
					new VariableSink[] {
							new VariableSink("currentHeaterIntensity",
											 Double.class,
											 ElectricMeterElectricityModel.URI)
					};
			bindings.put(source2, sinks2);
			bindings.put(source3, sinks3);

			// bindings from the indoor garden electricity model to the electric
			// meter model
			VariableSource source4 =
					new VariableSource("currentIntensity",
									   Double.class,
									   IndoorGardenElectricityModel.URI);
			VariableSink[] sinks4 =
					new VariableSink[] {
							new VariableSink("currentIndoorGardenIntensity",
											 Double.class,
											 ElectricMeterElectricityModel.URI)
					};
			bindings.put(source4, sinks4);


			// coupled model descriptor: an HIOA requires a
			// CoupledHIOA_Descriptor
			coupledModelDescriptors.put(
					HEM_CoupledModel.URI,
					new CoupledHIOA_Descriptor(
							HEM_CoupledModel.class,
							HEM_CoupledModel.URI,
							submodels,
							null,
							null,
							connections,
							null,
							SimulationEngineCreationMode.COORDINATION_ENGINE,
							null,
							null,
							bindings));

			// simulation architecture
			ArchitectureI architecture =
					new Architecture(
							HEM_CoupledModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							TimeUnit.HOURS);

			// create the simulator from the simulation architecture
			SimulationEngine se = architecture.constructSimulator();

			// Optional: how to use simulation run parameters to modify
			// the behaviour of the models from runs to runs
			Map<String, Object> simParams = new HashMap<String, Object>();
			simParams.put(
				ModelI.createRunParameterName(
					HairDryerElectricityModel.URI,
					HairDryerElectricityModel.LOW_MODE_CONSUMPTION_RPNAME),
				1320.0);
			simParams.put(
				ModelI.createRunParameterName(
					HairDryerElectricityModel.URI,
					HairDryerElectricityModel.HIGH_MODE_CONSUMPTION_RPNAME),
				2200.0);
			simParams.put(ModelI.createRunParameterName(
								HairDryerUserModel.URI,
								HairDryerUserModel.MEAN_STEP_RPNAME),
						  0.05);
			simParams.put(ModelI.createRunParameterName(
								HairDryerUserModel.URI,
								HairDryerUserModel.MEAN_DELAY_RPNAME),
						  2.0);
			simParams.put(
					ModelI.createRunParameterName(
							HeaterElectricityModel.URI,
							HeaterElectricityModel.NOT_HEATING_POWER_RUNPNAME),
					0.0);
			simParams.put(
					ModelI.createRunParameterName(
							HeaterElectricityModel.URI,
							HeaterElectricityModel.HEATING_POWER_RUNPNAME),
					4400.0);
			se.setSimulationRunParameters(simParams);

			// this add additional time at each simulation step in
			// standard simulations (useful for debugging)
			SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 0L;
			// run a simulation with the simulation beginning at 0.0 and
			// ending at 24.0 hours
			se.doStandAloneSimulation(0.0, 24.0);

			// Optional: simulation report
			HEM_Report r = (HEM_Report) se.getFinalReport();
			System.out.println(r.printout(""));
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}
// -----------------------------------------------------------------------------
