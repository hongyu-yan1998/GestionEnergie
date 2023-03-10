package fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.sil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.sil.events.TurnOffAirConditioner;
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.sil.events.TurnOnAirConditioner;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.architectures.SimulationEngineCreationMode;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTAtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTCoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;

/**
 * The class <code>AirConditionerRTAtomicSimulatorPlugin</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-1-1</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class AirConditionerRTAtomicSimulatorPlugin 
extends RTAtomicSimulatorPlugin 
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	
	/** name used to pass the air conditioner owner component reference as
	 *  simulation parameter.												*/
	public static final String	OWNER_RPNAME = "ACCRN";
	
	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	@Override
	public void			setSimulationRunParameters(
		Map<String, Object> simParams
		) throws Exception
	{
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
	 * @param isUnitTesting	when true, create the unit test simulation architecture.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			initialiseSimulationArchitecture(
		String simArchURI,
		double accFactor,
		boolean isUnitTesting
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
		Map<EventSource, EventSink[]> connections = new HashMap<>();

		// for atomic model, we use an AtomicModelDescriptor
		atomicModelDescriptors.put(
			AirConditionerStateModel.URI,
			RTAtomicModelDescriptor.create(
					AirConditionerStateModel.class,
					AirConditionerStateModel.URI,
					TimeUnit.HOURS,
					null,
					SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
					accFactor));

		if (isUnitTesting) {
			// when the component performs a SIL simulated unit test, the
			// AirConditionerElectricityModel must be located within the
			// IndoorGarden component SIL simulator and then a coupled model
			// is required that must import the events and transmit them to the
			// AirConditionerStateModel
			imported.put(TurnOnAirConditioner.class,
						 new EventSink[] {
								 new EventSink(AirConditionerStateModel.URI,
										 	   TurnOnAirConditioner.class)
						 });
			imported.put(TurnOffAirConditioner.class,
						 new EventSink[] {
								 new EventSink(AirConditionerStateModel.URI,
										 	   TurnOffAirConditioner.class)
						 });

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
						accFactor));

			// the set of submodels of the coupled model, given by their URIs
			Set<String> submodels = new HashSet<String>();
			submodels.add(AirConditionerStateModel.URI);
			submodels.add(AirConditionerElectricityModel.URI);

			// for unit tests, the events exported by the AirConditionerStateModel
			// are explicitly sent to AirConditionerElectricityModel, while in
			// integration tests they will be exported to the electric meter
			// component simulator, which will gather all electricity models
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
							imported,
							null,
							connections,
							null,
							SimulationEngineCreationMode.COORDINATION_RT_ENGINE,
							accFactor));

			// simulation architecture
			this.setSimulationArchitecture(
					new RTArchitecture(
							simArchURI,
							AirConditionerCoupledModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							TimeUnit.HOURS,
							accFactor));
		} else {
			assert	!isUnitTesting;

			// simulation architecture for integration test, where only the
			// AirConditionerStateModel will appear in the IndoorGarden component
			// simulator; imported and exported events are then declared
			// directly by AirConditionerStateModel
			this.setSimulationArchitecture(
					new RTArchitecture(
							simArchURI,
							AirConditionerStateModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,	// is empty here
							TimeUnit.HOURS,
							accFactor));
		}
	}
}
