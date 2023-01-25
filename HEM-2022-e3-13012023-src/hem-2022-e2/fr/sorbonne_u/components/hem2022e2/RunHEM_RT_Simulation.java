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
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTAtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTCoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.SimulationEngine;

// -----------------------------------------------------------------------------
/**
 * The class <code>RunHEM_RT_Simulation</code> creates the real time simulator
 * for the household energy management example and then runs a typical
 * simulation in real time.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This class shows how to describe, construct and then run a real time
 * simulation. By comparison with {@code RunHEM_Simulation}, differences
 * help understanding the passage from a synthetic simulation time run
 * to a real time one. Recall that real time simulations force the simulation
 * time to follow the real time, hence in a standard real time run, the
 * simulation time advance at the rhythm of the real time. However, such
 * simulation runs can become either very lengthy, for examples like the
 * household energy management where simulation runs could last several days,
 * or very short, for examples like simulating microprocessors where events
 * can occur at the nanosecond time scale. So it is also possible to keep the
 * same time structure but to accelerate or decelerate the real time by some
 * factor, here defined as {@code ACCELERATION_FACTOR}. A value greater than
 * one will accelerate the simulation while a value strictly between 0 and 1
 * will decelerate it.
 * </p>
 * <p>
 * So, notice the use of real time equivalent to the model descriptors and
 * the simulation engine attached to models, as well as the acceleration
 * factor passed as parameter through the descriptors. The same acceleration
 * factor must be imposed to all models to get time coherent simulations.
 * </p>
 * <p>
 * The interest of real time simulations will become clear when simulation
 * models will be used in SIL simulations with the actual component software
 * executing in parallel to the simulations. Time coherent exchanges will then
 * become possible between the code and the simulations as the execution
 * of code instructions will occur on the same time frame as the simulations.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code ACCELERATION_FACTOR > 0.0}
 * </pre>
 * 
 * <p>Created on : 2021-09-30</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			RunHEM_RT_Simulation
{
	/** acceleration factor for the real time simulation; with a factor 2.0,
	 *  the simulation runs two times faster than real time i.e., a run that
	 *  is supposed to take 10 seconds in real time will take 5 seconds to
	 *  execute.															*/
	protected static final double ACCELERATION_FACTOR = 600.0;

	public static void	main(String[] args)
	{
		Time.setPrintPrecision(4);
		Duration.setPrintPrecision(4);

		try {
			// map that will contain the atomic model descriptors to construct
			// the simulation architecture
			Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
																new HashMap<>();

			// atomic HIOA models require RTAtomicHIOA_Descriptor while
			// atomic models require RTAtomicModelDescriptor
			// the same acceleration factor must be used for all models

			// hair dryer models
			atomicModelDescriptors.put(
					HairDryerElectricityModel.URI,
					RTAtomicHIOA_Descriptor.create(
							HairDryerElectricityModel.class,
							HairDryerElectricityModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
							ACCELERATION_FACTOR));
			atomicModelDescriptors.put(
					HairDryerUserModel.URI,
					RTAtomicModelDescriptor.create(
							HairDryerUserModel.class,
							HairDryerUserModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
							ACCELERATION_FACTOR));

			// the heater models
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
			atomicModelDescriptors.put(
					HeaterUnitTesterModel.URI,
					RTAtomicModelDescriptor.create(
							HeaterUnitTesterModel.class,
							HeaterUnitTesterModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
							ACCELERATION_FACTOR));


			// the indoor garden models
			atomicModelDescriptors.put(
					IndoorGardenElectricityModel.URI,
					RTAtomicHIOA_Descriptor.create(
							IndoorGardenElectricityModel.class,
							IndoorGardenElectricityModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
							ACCELERATION_FACTOR));
			// for atomic model, we use an AtomicModelDescriptor
			atomicModelDescriptors.put(
					IndoorGardenTestModel.URI,
					RTAtomicModelDescriptor.create(
							IndoorGardenTestModel.class,
							IndoorGardenTestModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
							ACCELERATION_FACTOR));

			// the electric meter model
			atomicModelDescriptors.put(
					ElectricMeterElectricityModel.URI,
					RTAtomicHIOA_Descriptor.create(
							ElectricMeterElectricityModel.class,
							ElectricMeterElectricityModel.URI,
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
			bindings.put(
					new VariableSource("externalTemperature",
							   Double.class,
							   ExternalTemperatureModel.URI),
					new VariableSink[] {
							new VariableSink("externalTemperature",
											 Double.class,
											 HeaterTemperatureModel.URI)
					});

			// bindings between hair dryer and heater models to the electric
			// meter model
			bindings.put(
					new VariableSource("currentIntensity",
							   Double.class,
							   HairDryerElectricityModel.URI),
					new VariableSink[] {
							new VariableSink("currentHairDryerIntensity",
											 Double.class,
											 ElectricMeterElectricityModel.URI)
					});
			bindings.put(
					new VariableSource("currentIntensity",
							   Double.class,
							   HeaterElectricityModel.URI),
					new VariableSink[] {
							new VariableSink("currentHeaterIntensity",
											 Double.class,
											 ElectricMeterElectricityModel.URI)
					});
			// bindings from the indoor garden electricity model to the electric
			// meter model
			bindings.put(
					new VariableSource("currentIntensity",
							   Double.class,
							   IndoorGardenElectricityModel.URI),
					new VariableSink[] {
							new VariableSink("currentIndoorGardenIntensity",
											 Double.class,
											 ElectricMeterElectricityModel.URI)
					});

			// coupled model descriptor: an HIOA requires a
			// RTCoupledHIOA_Descriptor
			coupledModelDescriptors.put(
					HEM_CoupledModel.URI,
					new RTCoupledHIOA_Descriptor(
							HEM_CoupledModel.class,
							HEM_CoupledModel.URI,
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

			// run a simulation with the simulation beginning at 0.0 and
			// ending at 24.0

			// duration of the simulation in hours, the simulation time unit
			double simDuration = 24.0;
			// the real time of start of the simulation plus a 1s delay to give
			// the time to initialise all models in the architecture.
			long start = System.currentTimeMillis() + 1000L;

			se.startRTSimulation(start, 0.0, simDuration);

			// wait until the simulation ends i.e., the start delay  plus the
			// duration of the simulation in milliseconds plus another 2s delay
			// to make sure...
			Thread.sleep(1000L
						 + ((long)((simDuration*3600*1000.0)/ACCELERATION_FACTOR))
						 + 3000L);
			// Optional: simulation report
			HEM_Report r = (HEM_Report) se.getFinalReport();
			System.out.println(r.printout(""));
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}
// -----------------------------------------------------------------------------
