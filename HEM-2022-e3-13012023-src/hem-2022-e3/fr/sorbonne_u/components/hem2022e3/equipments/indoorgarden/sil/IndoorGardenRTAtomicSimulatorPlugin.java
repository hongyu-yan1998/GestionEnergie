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
import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.events.SwitchLightOffIndoorGarden;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.events.SwitchLightOnIndoorGarden;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.events.SwitchOffIndoorGarden;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.events.SwitchOnIndoorGarden;
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

// -----------------------------------------------------------------------------
/**
 * The class <code>IndoorGardenRTAtomicSimulatorPlugin</code> implements the
 * simulation plug-in associated with the {@code IndoorGarden} component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code OWNER_RPNAME != null && !OWNER_RPNAME.isEmpty()}
 * </pre>
 * 
 * <p>Created on : 2022-11-08</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			IndoorGardenRTAtomicSimulatorPlugin
extends		RTAtomicSimulatorPlugin
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	/** name used to pass the indoor garden owner component reference as
	 *  simulation parameter.												*/
	public static final String	OWNER_RPNAME = "IGCRN";

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
			IndoorGardenStateModel.URI,
			RTAtomicModelDescriptor.create(
					IndoorGardenStateModel.class,
					IndoorGardenStateModel.URI,
					TimeUnit.HOURS,
					null,
					SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
					accFactor));

		if (isUnitTesting) {
			// when the component performs a SIL simulated unit test, the
			// IndoorGardenElectricityModel must be located within the
			// IndoorGarden component SIL simulator and then a coupled model
			// is required that must import the events and transmit them to the
			// IndoorGardenStateModel
			imported.put(SwitchOnIndoorGarden.class,
						 new EventSink[] {
								 new EventSink(IndoorGardenStateModel.URI,
										 	   SwitchOnIndoorGarden.class)
						 });
			imported.put(SwitchOffIndoorGarden.class,
						 new EventSink[] {
								 new EventSink(IndoorGardenStateModel.URI,
										 	   SwitchOffIndoorGarden.class)
						 });
			imported.put(SwitchLightOnIndoorGarden.class,
						 new EventSink[] {
								 new EventSink(IndoorGardenStateModel.URI,
										 	   SwitchLightOnIndoorGarden.class)
						 });
			imported.put(SwitchLightOffIndoorGarden.class,
						 new EventSink[] {
								 new EventSink(IndoorGardenStateModel.URI,
										 	   SwitchLightOffIndoorGarden.class)
						 });

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
						accFactor));

			// the set of submodels of the coupled model, given by their URIs
			Set<String> submodels = new HashSet<String>();
			submodels.add(IndoorGardenStateModel.URI);
			submodels.add(IndoorGardenElectricityModel.URI);

			// for unit tests, the events exported by the IndoorGardenStateModel
			// are explicitly sent to IndoorGardenElectricityModel, while in
			// integration tests they will be exported to the electric meter
			// component simulator, which will gather all electricity models
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
							IndoorGardenCoupledModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							TimeUnit.HOURS,
							accFactor));
		} else {
			assert	!isUnitTesting;

			// simulation architecture for integration test, where only the
			// IndoorGardenStateModel will appear in the IndoorGarden component
			// simulator; imported and exported events are then declared
			// directly by IndoorGardenStateModel
			this.setSimulationArchitecture(
					new RTArchitecture(
							simArchURI,
							IndoorGardenStateModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,	// is empty here
							TimeUnit.HOURS,
							accFactor));
		}
	}
}
// -----------------------------------------------------------------------------
