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

import java.util.Map;
import java.util.Set;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.events.DoNotHeat;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.events.Heat;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.events.SwitchOffHeater;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.events.SwitchOnHeater;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.architectures.SimulationEngineCreationMode;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTAtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTCoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.interfaces.ModelDescriptionI;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.models.events.ReexportedEvent;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.models.time.TimeUtils;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>HeaterRTAtomicSimulatorPlugin</code> implements the
 * simulator plug-in to be used in the {@code ThermostatedHeater} component
 * xhen performing SIL simulated executions (for testing).
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
 * invariant	{@code CURRENT_ROOM_TERMPERATURE != null && !CURRENT_ROOM_TERMPERATURE.isEmpty()}
 * </pre>
 * 
 * <p>Created on : 2022-10-27</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			HeaterRTAtomicSimulatorPlugin
extends		RTAtomicSimulatorPlugin
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long	serialVersionUID = 1L;
	/** name used to pass the owner component reference as simulation
	 *  parameter.															*/
	public static final String	OWNER_RPNAME = "THCRN";
	/** name used to access the current room temperature in the
	 *  {@code HeaterTemperatureModel}.								 	*/
	public static final String	CURRENT_ROOM_TERMPERATURE = "crt";

	/** start time of the simulation in {@code LocaTime}.					*/
	protected Instant	localStartTime;
	/** start time of the simulation in simulated time.						*/
	protected Time		simulatedStartTime;

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
	 * The only model state value in the heater SIL simulators is the room
	 * temperature computed by the {@code HeaterTemperatureModel}, hence
	 * {@code modelURI} must be the URI of a {@code HeaterTemperatureModel}
	 * and {@code name} must equal {@code CURRENT_ROOM_TERMPERATURE} otherwise
	 * an assertion error will be triggered.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more preconditions.
	 * post	{@code true}	// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin#getModelStateValue(java.lang.String, java.lang.String)
	 */
	@Override
	public Object		getModelStateValue(String modelURI, String name)
	throws Exception
	{
		// The only model state value in the heater SIL simulators is the
		// room temperature computed by the HeaterTemperatureModel
		assert	modelURI != null && !modelURI.isEmpty() :
				new PreconditionException(
						"modelURI != null && !modelURI.isEmpty()");
		assert	name != null && !name.isEmpty() :
				new PreconditionException("name != null && !name.isEmpty()");

		ModelDescriptionI m = this.simulator.getDescendentModel(modelURI);
		assert	m instanceof HeaterTemperatureModel :
				new AssertionError(modelURI + " is not the URI of a "
						+ HeaterTemperatureModel.class.getSimpleName()
						+ "model!");
		
		assert	name.equals(CURRENT_ROOM_TERMPERATURE) :
				new AssertionError(name + " is an unknown variable name!");

		return ((HeaterTemperatureModel)m).getCurrentTemperature();
	}

	/**
	 * @see fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin#startRTSimulation(long, double, double)
	 */
	@Override
	public void			startRTSimulation(
		long realTimeOfStart,
		double simulationStartTime,
		double simulationDuration
		) throws Exception
	{
		// keep track of the simulation real time of start as an instance
		// of Instant to be able to convert simulated times into instants
		this.localStartTime = Instant.ofEpochMilli(realTimeOfStart);
		// also keep track of the simulated start time in order to interpret
		// correctly the simulated times against localStartTime
		this.simulatedStartTime =
				new Time(simulationStartTime,
						 this.localArchitecture.getSimulationTimeUnit());
		// actually start the real time simulation
		super.startRTSimulation(
					realTimeOfStart, simulationStartTime, simulationDuration);
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * compute the {@code Instant} corresponding to the current simulation time
	 * of the {@code HeaterTemperatureModel}, assuming that the start time and
	 * the start instant are known to interpret correctly the current simulation
	 * time as an {@code Instant}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition
	 * post	{@code ret != null}
	 * </pre>
	 *
	 * @return	the instant corresponding to the current simulation time.
	 */
	public Instant		getCurrentInstant()
	{
		try {
			HeaterTemperatureModel m =
				(HeaterTemperatureModel)
					this.simulator.getDescendentModel(HeaterTemperatureModel.URI);
			Time t = m.getCurrentStateTime();
			Duration d = t.subtract(this.simulatedStartTime);
			long dInNanos =
				TimeUtils.toNanos(d.getSimulatedDuration(), d.getTimeUnit());
			return this.localStartTime.plusNanos(dInNanos);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
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
		Map<Class<? extends EventI>,ReexportedEvent> reexported = null;
		Map<EventSource, EventSink[]> connections = new HashMap<>();
		// variable bindings between exporting and importing models
		Map<VariableSource,VariableSink[]> bindings = new HashMap<>();

		// the heater models simulating its electricity consumption, its
		// temperatures and the external temperature are atomic HIOA models
		// hence we use an AtomicHIOA_Descriptor(s)
		atomicModelDescriptors.put(
				HeaterTemperatureModel.URI,
				RTAtomicHIOA_Descriptor.create(
						HeaterTemperatureModel.class,
						HeaterTemperatureModel.URI,
						TimeUnit.HOURS,
						null,
						SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
						accFactor));
		atomicModelDescriptors.put(
				ExternalTemperatureModel.URI,
				RTAtomicHIOA_Descriptor.create(
						ExternalTemperatureModel.class,
						ExternalTemperatureModel.URI,
						TimeUnit.HOURS,
						null,
						SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
						accFactor));
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
						accFactor));

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(HeaterTemperatureModel.URI);
		submodels.add(ExternalTemperatureModel.URI);
		submodels.add(HeaterStateModel.URI);

		imported.put(
				SwitchOnHeater.class,
				new EventSink[] {
					new EventSink(HeaterStateModel.URI, SwitchOnHeater.class)
				});
		imported.put(
				SwitchOffHeater.class,
				new EventSink[] {
					new EventSink(HeaterStateModel.URI, SwitchOffHeater.class)
				});
		imported.put(
				Heat.class,
				new EventSink[] {
					new EventSink(HeaterStateModel.URI, Heat.class)
				});
		imported.put(
				DoNotHeat.class,
				new EventSink[] {
					new EventSink(HeaterStateModel.URI, DoNotHeat.class)
				});

		if (isUnitTesting) {
			// when the component performs a SIL simulated unit test, the
			// HeaterElectricityModel must be located within the
			// ThermostatedHeater component SIL simulator and the events
			// exported by the HeaterStateModel are sent to
			// HeaterElectricityModel
			atomicModelDescriptors.put(
					HeaterElectricityModel.URI,
					RTAtomicHIOA_Descriptor.create(
							HeaterElectricityModel.class,
							HeaterElectricityModel.URI,
							TimeUnit.HOURS,
							null,
							SimulationEngineCreationMode.ATOMIC_RT_ENGINE,
							accFactor));
			submodels.add(HeaterElectricityModel.URI);

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
		} else {
			// when the component performs a SIL simulated integration test, the
			// HeaterElectricityModel will be located outside the
			// ThermostatedHeater component SIL simulator and the events
			// exported by the HeaterStateModel are reexported by the
			// HeaterCoupledModel to be received by the HeaterElectricityModel
			// wherever it is located in the simulation architecture
			connections.put(
				new EventSource(HeaterStateModel.URI, Heat.class),
				new EventSink[] {
						new EventSink(HeaterTemperatureModel.URI, Heat.class)
				});
			connections.put(
				new EventSource(HeaterStateModel.URI, DoNotHeat.class),
				new EventSink[] {
						new EventSink(HeaterTemperatureModel.URI, DoNotHeat.class)
				});

			reexported = new HashMap<>();
			reexported.put(
				SwitchOnHeater.class,
				new ReexportedEvent(HeaterStateModel.URI, SwitchOnHeater.class));
			reexported.put(
				SwitchOffHeater.class,
				new ReexportedEvent(HeaterStateModel.URI, SwitchOffHeater.class));
			reexported.put(
				Heat.class,
				new ReexportedEvent(HeaterStateModel.URI, Heat.class));
			reexported.put(
				DoNotHeat.class,
				new ReexportedEvent(HeaterStateModel.URI, DoNotHeat.class));

		}

		// variable bindings between exporting and importing models
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
						imported,
						reexported,
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
						HeaterCoupledModel.URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						TimeUnit.HOURS,
						accFactor));
	}
}
// -----------------------------------------------------------------------------
