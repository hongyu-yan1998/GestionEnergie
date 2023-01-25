package fr.sorbonne_u.components.hem2022e2.equipments.indoorgarden.mil;

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
import fr.sorbonne_u.components.hem2022e2.equipments.indoorgarden.mil.events.SwitchLightOffIndoorGarden;
import fr.sorbonne_u.components.hem2022e2.equipments.indoorgarden.mil.events.SwitchLightOnIndoorGarden;
import fr.sorbonne_u.components.hem2022e2.equipments.indoorgarden.mil.events.SwitchOffIndoorGarden;
import fr.sorbonne_u.components.hem2022e2.equipments.indoorgarden.mil.events.SwitchOnIndoorGarden;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import java.util.ArrayList;

// -----------------------------------------------------------------------------
/**
 * The class <code>IndoorGardenElectricityModel</code> implements a simulation
 * model for the electricity consumption of an indoor garden.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The indoor garden has three states from the electricity consumption point of
 * vue which are defined by the {@code State} enumeration type:
 * </p>
 * <ol>
 * <li>{@code State.OFF}: the garden is off and its light is off.</li>
 * <li>{@code State.LIGHT_ON}: the garden is on and its light is on.</li>
 * <li>{@code State.LIGHT_OFF}: the garden is on but its light is off.</li>
 * </ol>
 * <p>
 * The model imports four event types controlling its mode:
 * </p>
 * <ol>
 * <li>{@code SwitchOnIndoorGarden}: when the indoor garden is in state OFF,
 *   these events turn it on but with the light off.</li>
 * <li>{@code SwitchOffIndoorGarden}: when the indoor garden is in state
 *   LIGHT_ON or LIGHT_OFF, these events turn it off.</li>
 * <li>{@code SwitchLightOnIndoorGarden}: when the indoor garden is in state
 *   LIGHT_OFF, these events turn the light on.</li>
 * <li>{@code SwitchLightOffIndoorGarden}: when the indoor garden is in state
 *   LIGHT_ON, these events turn the light off.</li>
 * </ol>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code getState() != State.OFF || currentIntensity.getValue() == 0.0}
 * invariant	{@code getState() != State.LIGHT_ON || currentIntensity.getValue() == LIGHT_ON_CONSUMPTION/TENSION}
 * invariant	{@code getState() != State.LIGHT_OFF || currentIntensity.getValue() == LIGHT_OFF_CONSUMPTION/TENSION}
 * </pre>
 * 
 * <p>Created on : 2022-10-04</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@ModelExternalEvents(imported = {SwitchOnIndoorGarden.class,
								 SwitchOffIndoorGarden.class,
								 SwitchLightOnIndoorGarden.class,
								 SwitchLightOffIndoorGarden.class})
@ModelExportedVariable(name = "currentIntensity", type = Double.class)
//-----------------------------------------------------------------------------
public class			IndoorGardenElectricityModel
extends		AtomicHIOA
{
	// -------------------------------------------------------------------------
	// Inner classes and types
	// -------------------------------------------------------------------------

	/**
	 * The enumeration <code>State</code> describes the discrete states or
	 * modes of the indoor garden.
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

	private static final long serialVersionUID = 1L;

	/** URI for an instance model; works as long as only one instance is
	 *  created.															*/
	public static final String		URI = IndoorGardenElectricityModel.class.
																getSimpleName();

	/** energy consumption (in Watts) of in {@code LIGHT_OFF} mode.			*/
	public static double			LIGHT_OFF_CONSUMPTION = 0.11; // Watts
	/** energy consumption (in Watts) of in {@code LIGHT_ON} mode.			*/
	public static double			LIGHT_ON_CONSUMPTION = 11.0; // Watts
	/** nominal tension (in Volts) of the hair dryer.						*/
	public static double			TENSION = 220.0; // Volts

	/** current state.														*/
	protected State					currentState = State.OFF;

	/** true when the electricity consumption of the dryer has changed
	 *  after executing an external event; the external event changes the
	 *  value of <code>currentState</code> and then an internal transition
	 *  will be triggered by putting through in this variable which will
	 *  update the variable <code>currentIntensity</code>.					*/
	protected boolean				consumptionHasChanged = false;

	// -------------------------------------------------------------------------
	// HIOA model variables
	// -------------------------------------------------------------------------

	/** current intensity in amperes; intensity is power/tension.			*/
	@ExportedVariable(type = Double.class)
	protected final Value<Double>	currentIntensity = new Value<Double>(this);

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an indoor garden electricity model instance.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param uri				URI of the model.
	 * @param simulatedTimeUnit	time unit used for the simulation time.
	 * @param simulationEngine	simulation engine to which the model is attached.
	 * @throws Exception		<i>to do</i>.
	 */
	public				IndoorGardenElectricityModel(
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
	 * set the state of the indoor garden.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code s != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param s	the new state.
	 */
	public void			setState(State s)
	{
		assert	s != null;

		this.currentState = s;
	}

	/**
	 * get the state of the indoor garden.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret != null}
	 * </pre>
	 *
	 * @return	the current state of the indoor garden.
	 */
	public State		getState()
	{
		return this.currentState;
	}

	/**
	 * toggle the value of the state of the model telling whether the
	 * electricity consumption level has just changed or not; when it changes
	 * after receiving an external event, an immediate internal transition
	 * is triggered to update the level of electricity consumption.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	public void			toggleConsumptionHasChanged()
	{
		if (this.consumptionHasChanged) {
			this.consumptionHasChanged = false;
		} else {
			this.consumptionHasChanged = true;
		}
	}

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	/**
	 * initialise the state of the model.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more preconditions.
	 * post	{@code State.OFF == this.getState()}
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			initialiseState(Time initialTime)
	{
		super.initialiseState(initialTime);

		this.currentState = State.OFF;
		this.consumptionHasChanged = false;

		this.toggleDebugMode();
		this.logMessage("simulation begins.\n");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#initialiseVariables()
	 */
	@Override
	public void			initialiseVariables() throws Exception
	{
		super.initialiseVariables();
		this.currentIntensity.initialise(0.0);
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI#output()
	 */
	@Override
	public ArrayList<EventI>	output()
	{
		// the model does not export events.
		return null;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
	 */
	@Override
	public Duration		timeAdvance()
	{
		// to trigger an internal transition after an external transition, the
		// variable consumptionHasChanged is set to true, hence when it is true
		// return a zero delay otherwise return an infinite delay (no internal
		// transition expected)
		if (this.consumptionHasChanged) {
			// after triggering the internal transition, toggle the boolean
			// to prepare for the next internal transition.
			this.toggleConsumptionHasChanged();
			return new Duration(0.0, this.getSimulatedTimeUnit());
		} else {
			return Duration.INFINITY;
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		super.userDefinedInternalTransition(elapsedTime);

		Time t = this.getCurrentStateTime();
		switch (this.currentState)
		{
			case OFF: this.currentIntensity.setNewValue(0.0, t); break;
			case LIGHT_ON:
				this.currentIntensity.
							setNewValue(LIGHT_ON_CONSUMPTION/TENSION, t);
				break;
			case LIGHT_OFF:
				this.currentIntensity.
							setNewValue(LIGHT_OFF_CONSUMPTION/TENSION, t);
		}

		// Tracing
		StringBuffer message =
				new StringBuffer("executes an internal transition ");
		message.append("with current consumption ");
		message.append(this.currentIntensity.getValue());
		message.append(" at ");
		message.append(this.currentIntensity.getTime());
		message.append(".\n");
		this.logMessage(message.toString());
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

		Event ce = (Event) currentEvents.get(0);

		// Tracing
		StringBuffer message =
				new StringBuffer("executes an external transition on event ");
		message.append(ce.toString());
		message.append(")\n");
		this.logMessage(message.toString());

		// events have a method execute on to perform their effect on this
		// model
		ce.executeOn(this);
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
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.Model#getFinalReport()
	 */
	@Override
	public SimulationReportI	getFinalReport() throws Exception
	{
		return null;
	}
}
// -----------------------------------------------------------------------------
