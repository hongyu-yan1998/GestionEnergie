package fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil;

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
import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.events.SetHighHairDryer;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.events.SetLowHairDryer;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.events.SwitchOffHairDryer;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.events.SwitchOnHairDryer;
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
import fr.sorbonne_u.devs_simulation.models.events.ReexportedEvent;

// -----------------------------------------------------------------------------
/**
 * The class <code>HairDryerRTAtomicSimulatorPlugin</code> defines the plug-in
 * that manages the SIL simulation inside the hair dryer component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The role of this plug-in is to define the local simulation architecture for
 * the {@code HairDryer} component and to implement what is required to make
 * this component participate in the SIL simulation for tests.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2021-10-04</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			HairDryerRTAtomicSimulatorPlugin
extends		RTAtomicSimulatorPlugin
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long		serialVersionUID = 1L;
	/** name used to pass the owner component reference as simulation
	 *  parameter.															*/
	public static final String		OWNER_RPNAME = "HDCRN";

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
	 * @param isUnitTesting	when true, create the unit test simulation architecture.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			initialiseSimulationArchitecture(
		String simArchURI,
		double accFactor,
		boolean isUnitTesting
		) throws Exception
	{
		Map<String,AbstractAtomicModelDescriptor>
								atomicModelDescriptors = new HashMap<>();
		Map<String,CoupledModelDescriptor>
								coupledModelDescriptors = new HashMap<>();

		atomicModelDescriptors.put(
				HairDryerStateModel.URI,
				RTAtomicModelDescriptor.create(
						HairDryerStateModel.class,
						HairDryerStateModel.URI,
						TimeUnit.HOURS,
						null,
						SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
						accFactor));

		if (isUnitTesting) {
			Map<Class<? extends EventI>,EventSink[]> imported = new HashMap<>();
			Map<Class<? extends EventI>,ReexportedEvent> reexported = null;
			Map<EventSource, EventSink[]> connections = new HashMap<>();

			// when executed as a unit test, the local simulation architecture
			// includes the hair dryer electricity model and events exported
			// by the state model are directed to the electricity model

			// as in unit test, there are two atomic models, a coupled model
			// is needed to compose them in a single simulator
			Set<String> submodels = new HashSet<String>();
			submodels.add(HairDryerStateModel.URI);
			submodels.add(HairDryerElectricityModel.URI);

			// the coupled model will transmit events (coming from the user
			// model) to the state model
			imported.put(SwitchOnHairDryer.class,
						 new EventSink[] {
								 new EventSink(HairDryerStateModel.URI,
										 	   SwitchOnHairDryer.class)
						 });
			imported.put(SwitchOffHairDryer.class,
						 new EventSink[] {
								 new EventSink(HairDryerStateModel.URI,
										 	   SwitchOffHairDryer.class)
						 });
			imported.put(SetHighHairDryer.class,
						 new EventSink[] {
								 new EventSink(HairDryerStateModel.URI,
										 	   SetHighHairDryer.class)
						 });
			imported.put(SetLowHairDryer.class,
						 new EventSink[] {
								 new EventSink(HairDryerStateModel.URI,
										 	   SetLowHairDryer.class)
						 });

			atomicModelDescriptors.put(
					HairDryerElectricityModel.URI,
					RTAtomicHIOA_Descriptor.create(
							HairDryerElectricityModel.class,
							HairDryerElectricityModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
							accFactor));

			// when the electricity model is located within the HairDryer
			// component for unit tests, the events from the state model must
			// be transmitted internally to the electricity model
			connections.put(
				new EventSource(HairDryerStateModel.URI,
								SwitchOnHairDryer.class),
				new EventSink[] {
						new EventSink(HairDryerElectricityModel.URI,
									  SwitchOnHairDryer.class)
				});
			connections.put(
				new EventSource(HairDryerStateModel.URI,
								SwitchOffHairDryer.class),
				new EventSink[] {
						new EventSink(HairDryerElectricityModel.URI,
									  SwitchOffHairDryer.class)
				});
			connections.put(
				new EventSource(HairDryerStateModel.URI,
								SetHighHairDryer.class),
				new EventSink[] {
						new EventSink(HairDryerElectricityModel.URI,
									  SetHighHairDryer.class)
				});
			connections.put(
				new EventSource(HairDryerStateModel.URI,
								SetLowHairDryer.class),
				new EventSink[] {
						new EventSink(HairDryerElectricityModel.URI,
									  SetLowHairDryer.class)
				});

			coupledModelDescriptors.put(
					HairDryerCoupledModel.URI,
					new RTCoupledModelDescriptor(
							HairDryerCoupledModel.class,
							HairDryerCoupledModel.URI,
							submodels,
							imported,
							reexported,
							connections,
							null,
							SimulationEngineCreationMode.COORDINATION_RT_ENGINE,
							accFactor));

			// this sets the architecture in the plug-in for further reference
			// and use
			this.setSimulationArchitecture(
					new RTArchitecture(
							simArchURI,
							HairDryerCoupledModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							TimeUnit.HOURS,
							accFactor));

		} else {
			// when *not% executed as a unit test, the simulation architecture
			// does not include the hair dryer electricity model and events
			// exported by the state model are simply exported by the hair
			// dryer state model; the architecture contains only one model,
			// the state model which descriptor is created above

			this.setSimulationArchitecture(
					new RTArchitecture(
							simArchURI,
							HairDryerStateModel.URI,
							atomicModelDescriptors,
							new HashMap<>(),
							TimeUnit.HOURS,
							accFactor));
		}
	}
}
// -----------------------------------------------------------------------------
