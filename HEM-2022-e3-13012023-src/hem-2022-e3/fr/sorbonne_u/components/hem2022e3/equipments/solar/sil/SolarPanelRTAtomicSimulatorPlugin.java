/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.solar.sil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.sil.events.SolarNotProduce;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.sil.events.SolarProduce;
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
 * The class <code>SolarPanelRTAtomicSimulatorPlugin</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-04</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class SolarPanelRTAtomicSimulatorPlugin 
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
			SolarPanelStateModel.URI,
			RTAtomicModelDescriptor.create(
					SolarPanelStateModel.class,
					SolarPanelStateModel.URI,
					TimeUnit.HOURS,
					null,
					SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
					accFactor));

		if (isUnitTesting) {
			// when the component performs a SIL simulated unit test, the
			// SolarPanelElectricityModel must be located within the
			// IndoorGarden component SIL simulator and then a coupled model
			// is required that must import the events and transmit them to the
			// SolarPanelStateModel
			imported.put(SolarProduce.class,
						 new EventSink[] {
								 new EventSink(SolarPanelStateModel.URI,
										 	   SolarProduce.class)
						 });
			imported.put(SolarNotProduce.class,
						 new EventSink[] {
								 new EventSink(SolarPanelStateModel.URI,
										 	   SolarNotProduce.class)
						 });

			// the indoor garden model simulating its electricity consumption,
			// an atomic HIOA model hence we use an RTAtomicHIOA_Descriptor
			atomicModelDescriptors.put(
				SolarPanelElectricityModel.URI,
				RTAtomicHIOA_Descriptor.create(
						SolarPanelElectricityModel.class,
						SolarPanelElectricityModel.URI,
						TimeUnit.HOURS,
						null,
						SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
						accFactor));

			// the set of submodels of the coupled model, given by their URIs
			Set<String> submodels = new HashSet<String>();
			submodels.add(SolarPanelStateModel.URI);
			submodels.add(SolarPanelElectricityModel.URI);

			// for unit tests, the events exported by the SolarPanelStateModel
			// are explicitly sent to SolarPanelElectricityModel, while in
			// integration tests they will be exported to the electric meter
			// component simulator, which will gather all electricity models
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
							SolarPanelCoupledModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							TimeUnit.HOURS,
							accFactor));
		} else {
			assert	!isUnitTesting;

			// simulation architecture for integration test, where only the
			// SolarPanelStateModel will appear in the IndoorGarden component
			// simulator; imported and exported events are then declared
			// directly by SolarPanelStateModel
			this.setSimulationArchitecture(
					new RTArchitecture(
							simArchURI,
							SolarPanelStateModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,	// is empty here
							TimeUnit.HOURS,
							accFactor));
		}
	}
}
