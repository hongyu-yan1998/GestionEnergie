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

import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Map;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.utils.StandardComponentLogger;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.events.SwitchLightOffIndoorGarden;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.events.SwitchLightOnIndoorGarden;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.events.SwitchOffIndoorGarden;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.events.SwitchOnIndoorGarden;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

// -----------------------------------------------------------------------------
/**
 * The class <code>IndoorGardenStateModel</code> implements the DEVS simulation
 * model that tracks the state of the indoor garden with respect to its
 * electricity consumption and which propagates events that changes this state
 * to the <code>IndoorGardenElectricityModel</code>.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This model is needed only to cater for SIL simulations where state changes
 * are initiated within the {@code IndoorGarden} component but the global
 * electricity consumption of a house is computed within the
 * {@code ElectricMeter} component. Because electricity models are of HIOA type,
 * they share variables and therefore must be located within the same component.
 * So the <code>IndoorGardenElectricityModel</code> will be in the
 * {@code ElectricMeter} component but actions initiating state changes are
 * done in the {@code IndoorGarden} component. However, component methods can
 * trigger simulation events for the simulation models they hold, but not for
 * simulation models held by other components. Only simulation models can
 * exchange events with models located in other components. Hence, the
 * <code>IndoorGardenStateModel</code> plays the role of processing events
 * emitted by the software in {@code IndoorGarden} to propagate them to the
 * <code>IndoorGardenElectricityModel</code> located in the
 * {@code ElectricMeter} component.
 * </p>
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
 * invariant	{@code URI != null && !URI.isEmpty()}
 * </pre>
 * 
 * <p>Created on : 2022-11-08</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@ModelExternalEvents(
		imported = {SwitchOnIndoorGarden.class,
					SwitchOffIndoorGarden.class,
					SwitchLightOnIndoorGarden.class,
					SwitchLightOffIndoorGarden.class},
		exported = {SwitchOnIndoorGarden.class,
					SwitchOffIndoorGarden.class,
					SwitchLightOnIndoorGarden.class,
					SwitchLightOffIndoorGarden.class}
		)
//-----------------------------------------------------------------------------
public class			IndoorGardenStateModel
extends		AtomicModel
implements	IndoorGardenOperationI
{
	// -------------------------------------------------------------------------
	// Inner classes and types
	// -------------------------------------------------------------------------

	/**
	 * The enumeration <code>State</code> describes the discrete states or
	 * modes of the indoor garden simulator.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * The indoor garden can be <code>OFF</code> or on, and then it is either
	 * in <code>LIGHT_OFF</code> mode (the light is off) or in
	 * <code>LIGHT_ON</code> mode (the light is on).
	 * 
	 * <p>Created on : 2022-10-04</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static enum State {
		OFF,
		/** appliance is on and the light is on.							*/
		LIGHT_ON,			
		/** appliance is on but the light is off.							*/
		LIGHT_OFF
	}

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long	serialVersionUID = 1L;

	/** URI for an instance model; works as long as only one instance is
	 *  created.															*/
	public static final String	URI = IndoorGardenStateModel.class.
																getSimpleName();

	/** current state.														*/
	protected State				currentState = State.OFF;
	/** last received external event that must be propagated.				*/
	protected EventI			lastReceived;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an indoor garden state model instance.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more precondition.
	 * post	{@code true}	// no more postcondition.
	 * </pre>
	 *
	 * @param uri				URI of the model.
	 * @param simulatedTimeUnit	time unit used for the simulation time.
	 * @param simulationEngine	simulation engine to which the model is attached.
	 * @throws Exception		<i>to do</i>.
	 */
	public				IndoorGardenStateModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		SimulatorI simulationEngine
		) throws Exception
	{
		super(uri, simulatedTimeUnit, simulationEngine);
		this.setLogger(new StandardLogger());
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.IndoorGardenOperationI#switchOn()
	 */
	@Override
	public void			switchOn()
	{
		if (this.currentState == State.OFF) {
			this.currentState = State.LIGHT_OFF;
		} else {
			// if no state change is required, no need to propagate the event
			this.lastReceived = null;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.IndoorGardenOperationI#switchOff()
	 */
	@Override
	public void			switchOff()
	{
		if (this.currentState != State.OFF) {
			this.currentState = State.OFF;
		} else {
			// if no state change is required, no need to propagate the event
			this.lastReceived = null;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.IndoorGardenOperationI#switchLightOn()
	 */
	@Override
	public void			switchLightOn()
	{
		if (this.currentState == State.LIGHT_OFF) {
			this.currentState = State.LIGHT_ON;
		} else {
			// if no state change is required, no need to propagate the event
			this.lastReceived = null;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.IndoorGardenOperationI#switchLightOff()
	 */
	@Override
	public void			switchLightOff()
	{
		if (this.currentState == State.LIGHT_ON) {
			this.currentState = State.LIGHT_OFF;
		} else {
			// if no state change is required, no need to propagate the event
			this.lastReceived = null;
		}
	}

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			initialiseState(Time initialTime)
	{
		this.currentState = State.OFF;
		this.lastReceived = null;
		super.initialiseState(initialTime);

		this.toggleDebugMode();
		this.logMessage("simulation begins.\n");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
	 */
	@Override
	public Duration		timeAdvance()
	{
		if (this.lastReceived != null ) {
			return Duration.zero(this.getSimulatedTimeUnit());
		} else {
			return Duration.INFINITY;
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI#output()
	 */
	@Override
	public ArrayList<EventI>	output() 
	{
		ArrayList<EventI> ret = new ArrayList<EventI>();
		if (this.lastReceived != null) {
			ret.add(this.lastReceived);
			this.lastReceived = null;
		}
		return ret;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedExternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedExternalTransition(Duration elapsedTime)
	{
		super.userDefinedExternalTransition(elapsedTime);

		// get the vector of currently received external events
		ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
		// when this method is called, there is at least one external event,
		// and for the current hair dryer model, there must be exactly one by
		// construction.
		assert	currentEvents != null && currentEvents.size() == 1;

		this.lastReceived = (Event) currentEvents.get(0);

		// Tracing
		StringBuffer message =
				new StringBuffer("executes an external transition on event ");
		message.append(this.lastReceived.toString());
		message.append(")\n");
		this.logMessage(message.toString());

		// events have a method execute on to perform their effect on this
		// model
		this.lastReceived.executeOn(this);
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#endSimulation(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			endSimulation(Time endTime) throws Exception
	{
		this.logMessage("simulation ends.\n");
		super.endSimulation(endTime);
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation run parameters
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.Model#setSimulationRunParameters(java.util.Map)
	 */
	@Override
	public void			setSimulationRunParameters(
		Map<String, Object> simParams
		) throws Exception
	{
		super.setSimulationRunParameters(simParams);

		// retrieve the reference to the owner component when passed as a
		// simulation run parameter
		if (simParams.containsKey(
							IndoorGardenRTAtomicSimulatorPlugin.OWNER_RPNAME)) {
			AbstractComponent owner =
					(AbstractComponent) simParams.get(
							IndoorGardenRTAtomicSimulatorPlugin.OWNER_RPNAME);
			// direct traces on the tracer of the owner component
			this.setLogger(new StandardComponentLogger(owner));
		}
	}
}
// -----------------------------------------------------------------------------
