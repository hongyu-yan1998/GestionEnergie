package fr.sorbonne_u.components.hem2022e3.equipments.meter.sil;

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

import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.sil.AirConditionerElectricityModel;
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.sil.events.TurnOffAirConditioner;
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.sil.events.TurnOnAirConditioner;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.HairDryerElectricityModel;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.events.SetHighHairDryer;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.events.SetLowHairDryer;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.events.SwitchOffHairDryer;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.events.SwitchOnHairDryer;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.HeaterElectricityModel;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.events.DoNotHeat;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.events.Heat;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.events.SwitchOffHeater;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.events.SwitchOnHeater;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.IndoorGardenElectricityModel;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.events.SwitchLightOffIndoorGarden;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.events.SwitchLightOnIndoorGarden;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.events.SwitchOffIndoorGarden;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.events.SwitchOnIndoorGarden;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.RefrigeratorElectricityModel;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.DoNotRun;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.Run;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.StartRefrigerator;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.StopRefrigerator;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.sil.SolarPanelElectricityModel;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.sil.events.SolarNotProduce;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.sil.events.SolarProduce;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.architectures.SimulationEngineCreationMode;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTAtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTCoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

// -----------------------------------------------------------------------------
/**
 * The class <code>ElectricMeterRTAtomicSimulatorPlugin</code>
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// TODO	// no more invariant
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code OWNER_RPNAME != null && !OWNER_RPNAME.isEmpty()}
 * </pre>
 * 
 * <p>Created on : 2022-11-21</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			ElectricMeterRTAtomicSimulatorPlugin
extends		RTAtomicSimulatorPlugin
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** name used to pass the owner component reference as simulation
	 *  parameter.															*/
	public static final String	OWNER_RPNAME = "EMCRN";

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.cyphy.plugins.devs.AbstractSimulatorPlugin#setSimulationRunParameters(java.util.Map)
	 */
	@Override
	public void			setSimulationRunParameters(
		Map<String, Object> simParams
		) throws Exception
	{
		
		System.out.println("EMPlugin"+getURI());
		// initialise the simulation parameter giving the reference to the
		// owner component before passing the parameters to the simulation
		// models
		simParams.put(OWNER_RPNAME, this.getOwner());

		// this will pass the parameters to the simulation models that will
		// then be able to get their own parameters.
		super.setSimulationRunParameters(simParams);

		// remove the value so that the reference may not exit the context of
		// the component
		simParams.remove(OWNER_RPNAME);
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------
	
	
	/**
	 * create and set the simulation architecture internal to this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code simArchURI != null && !simArchURIisEmpty()}
	 * pre	{@code accFactor > 0.0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param simArchURI	URI of the simulation architecture to be created.
	 * @param accFactor		acceleration factor used in the real time simulation.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			initialiseSimulationArchitecture(
		String simArchURI,
		double accFactor
		) throws Exception
	{
		// map that will contain the atomic model descriptors to construct
		// the simulation architecture
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();
		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();
		Map<Class<? extends EventI>,EventSink[]> imported = new HashMap<>();
		// event exchanging connections between exporting and importing
		// models
		Map<EventSource, EventSink[]> connections = new HashMap<>();
		// variable bindings between exporting and importing models
		Map<VariableSource,VariableSink[]> bindings =
								new HashMap<VariableSource,VariableSink[]>();

		atomicModelDescriptors.put(
				HairDryerElectricityModel.URI,
				RTAtomicHIOA_Descriptor.create(
						HairDryerElectricityModel.class,
						HairDryerElectricityModel.URI,
						TimeUnit.HOURS,
						null,
						SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
						accFactor));
		atomicModelDescriptors.put(
				HeaterElectricityModel.URI,
				RTAtomicHIOA_Descriptor.create(
						HeaterElectricityModel.class,
						HeaterElectricityModel.URI,
						TimeUnit.HOURS,
						null,
						SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
						accFactor));
		atomicModelDescriptors.put(
				IndoorGardenElectricityModel.URI,
				RTAtomicHIOA_Descriptor.create(
						IndoorGardenElectricityModel.class,
						IndoorGardenElectricityModel.URI,
						TimeUnit.HOURS,
						null,
						SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
						accFactor));
		
		// Air Conditioner
		atomicModelDescriptors.put(
				AirConditionerElectricityModel.URI,
				RTAtomicHIOA_Descriptor.create(
						AirConditionerElectricityModel.class,
						AirConditionerElectricityModel.URI,
						TimeUnit.HOURS,
						null,
						SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
						accFactor));
		// Refrigerator
		atomicModelDescriptors.put(
				RefrigeratorElectricityModel.URI,
				RTAtomicHIOA_Descriptor.create(
						RefrigeratorElectricityModel.class,
						RefrigeratorElectricityModel.URI,
						TimeUnit.HOURS,
						null,
						SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
						accFactor));
		
		// Solar Panel
		atomicModelDescriptors.put(
				SolarPanelElectricityModel.URI,
				RTAtomicHIOA_Descriptor.create(
						SolarPanelElectricityModel.class,
						SolarPanelElectricityModel.URI,
						TimeUnit.HOURS,
						null,
						SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
						accFactor));
		
		atomicModelDescriptors.put(
				ElectricMeterElectricityModel.URI,
				RTAtomicHIOA_Descriptor.create(
						ElectricMeterElectricityModel.class,
						ElectricMeterElectricityModel.URI,
						TimeUnit.HOURS,
						null,
						SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
						accFactor));

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(HairDryerElectricityModel.URI);
		submodels.add(HeaterElectricityModel.URI);
		submodels.add(IndoorGardenElectricityModel.URI);
		submodels.add(ElectricMeterElectricityModel.URI);
		submodels.add(AirConditionerElectricityModel.URI);
		submodels.add(RefrigeratorElectricityModel.URI);
		submodels.add(SolarPanelElectricityModel.URI);


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
		bindings.put(
				new VariableSource("currentIntensity",
						   Double.class,
						   IndoorGardenElectricityModel.URI),
				new VariableSink[] {
						new VariableSink("currentIndoorGardenIntensity",
										 Double.class,
										 ElectricMeterElectricityModel.URI)
				});
		
		// Air Conditioner
		bindings.put(
				new VariableSource("currentIntensity",
						   Double.class,
						   AirConditionerElectricityModel.URI),
				new VariableSink[] {
						new VariableSink("currentAirConditionerIntensity",
										 Double.class,
										 ElectricMeterElectricityModel.URI)
				});
		
		// Refrigerator
		bindings.put(
				new VariableSource("currentIntensity",
						   Double.class,
						   RefrigeratorElectricityModel.URI),
				new VariableSink[] {
						new VariableSink("currentRefrigeratorIntensity",
										 Double.class,
										 ElectricMeterElectricityModel.URI)
				});
		
		// Solar Panel
		bindings.put(
				new VariableSource("currentIntensity",
						   Double.class,
						   SolarPanelElectricityModel.URI),
				new VariableSink[] {
						new VariableSink("currentSolarPanelIntensity",
										 Double.class,
										 ElectricMeterElectricityModel.URI)
				});

		imported.put(SwitchOnHairDryer.class,
					 new EventSink[] {
							 new EventSink(HairDryerElectricityModel.URI,
									 	   SwitchOnHairDryer.class)
					 });
		imported.put(SwitchOffHairDryer.class,
					 new EventSink[] {
							 new EventSink(HairDryerElectricityModel.URI,
									 	   SwitchOffHairDryer.class)
					 });
		imported.put(SetLowHairDryer.class,
					 new EventSink[] {
							 new EventSink(HairDryerElectricityModel.URI,
									 	   SetLowHairDryer.class)
					 });
		imported.put(SetHighHairDryer.class,
					 new EventSink[] {
							 new EventSink(HairDryerElectricityModel.URI,
									 	   SetHighHairDryer.class)
					 });

		imported.put(SwitchOnHeater.class,
					 new EventSink[] {
							 new EventSink(HeaterElectricityModel.URI,
									 	   SwitchOnHeater.class)
					 });
		imported.put(SwitchOffHeater.class,
					 new EventSink[] {
							 new EventSink(HeaterElectricityModel.URI,
									 	   SwitchOffHeater.class)
					 });
		imported.put(Heat.class,
					 new EventSink[] {
							 new EventSink(HeaterElectricityModel.URI,
									 	   Heat.class)
					 });
		imported.put(DoNotHeat.class,
					 new EventSink[] {
							 new EventSink(HeaterElectricityModel.URI,
									 	   DoNotHeat.class)
					 });

		imported.put(SwitchOnIndoorGarden.class,
					 new EventSink[] {
							 new EventSink(IndoorGardenElectricityModel.URI,
									 	   SwitchOnIndoorGarden.class)
					 });
		imported.put(SwitchOffIndoorGarden.class,
					 new EventSink[] {
							 new EventSink(IndoorGardenElectricityModel.URI,
									 	   SwitchOffIndoorGarden.class)
					 });
		imported.put(SwitchLightOnIndoorGarden.class,
					 new EventSink[] {
							 new EventSink(IndoorGardenElectricityModel.URI,
									 	   SwitchLightOnIndoorGarden.class)
					 });
		imported.put(SwitchLightOffIndoorGarden.class,
					 new EventSink[] {
							 new EventSink(IndoorGardenElectricityModel.URI,
									 	   SwitchLightOffIndoorGarden.class)
					 });
		
		// Air Conditioner
		imported.put(TurnOnAirConditioner.class,
				 new EventSink[] {
						 new EventSink(AirConditionerElectricityModel.URI,
								 	   TurnOnAirConditioner.class)
				 });
		imported.put(TurnOffAirConditioner.class,
				 new EventSink[] {
						 new EventSink(AirConditionerElectricityModel.URI,
								 	   TurnOffAirConditioner.class)
				 });
		
		// Refrigerator
		imported.put(StartRefrigerator.class,
				 new EventSink[] {
						 new EventSink(RefrigeratorElectricityModel.URI,
								 		StartRefrigerator.class)
				 });
		imported.put(StopRefrigerator.class,
				 new EventSink[] {
						 new EventSink(RefrigeratorElectricityModel.URI,
								 		StopRefrigerator.class)
				 });
		imported.put(Run.class,
				 new EventSink[] {
						 new EventSink(RefrigeratorElectricityModel.URI,
								 		Run.class)
				 });
		imported.put(DoNotRun.class,
				 new EventSink[] {
						 new EventSink(RefrigeratorElectricityModel.URI,
								 		DoNotRun.class)
				 });
		
		// Solar Panel
		bindings.put(
				new VariableSource("currentIntensity",
						   Double.class,
						   SolarPanelElectricityModel.URI),
				new VariableSink[] {
						new VariableSink("currentSolarPanelIntensity",
										 Double.class,
										 ElectricMeterElectricityModel.URI)
				});
		
		imported.put(SolarProduce.class,
				 new EventSink[] {
						 new EventSink(SolarPanelElectricityModel.URI,
								 SolarProduce.class)
				 });
		imported.put(SolarNotProduce.class,
				 new EventSink[] {
						 new EventSink(SolarPanelElectricityModel.URI,
								 SolarNotProduce.class)
				 });

		// coupled model descriptor
		coupledModelDescriptors.put(
				ElectricMeterCoupledModel.URI,
				new RTCoupledHIOA_Descriptor(
						ElectricMeterCoupledModel.class,
						ElectricMeterCoupledModel.URI,
						submodels,
						imported,
						null,
						connections,
						null,
						SimulationEngineCreationMode.COORDINATION_RT_ENGINE,
						null,
						null,
						bindings,
						accFactor));

		// simulation architecture
		this.setSimulationArchitecture(
				new RTArchitecture(
						simArchURI,
						ElectricMeterCoupledModel.URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						TimeUnit.HOURS,
						accFactor));

	}
}
// -----------------------------------------------------------------------------
