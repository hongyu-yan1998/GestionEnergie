/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.sil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.sil.events.TurnOnAirConditioner;
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.sil.events.TurnOffAirConditioner;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.utils.StandardComponentLogger;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

/**
 * The class <code>AirConditionerStateModel</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-1-1</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
//-----------------------------------------------------------------------------
@ModelExternalEvents(
		imported = {TurnOnAirConditioner.class,
					TurnOffAirConditioner.class},
		exported = {TurnOnAirConditioner.class,
					TurnOffAirConditioner.class}
		)
//-----------------------------------------------------------------------------
public class AirConditionerStateModel
extends AtomicModel
implements AirConditionerOperationI
{
	// -------------------------------------------------------------------------
	// Inner classes and types
	// -------------------------------------------------------------------------

	/**
	 * The enumeration <code>State</code> describes the discrete states or
	 * modes of the air conditioner simulator.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * The air conditioner can be <code>OFF</code> or ON
	 * 
	 * 
	 */
	public static enum State {
		OFF,
		ON
	}
	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	
	/** URI for an instance model; works as long as only one instance is
	 *  created.															*/
	public static final String	URI = AirConditionerStateModel.class.
																getSimpleName();

	/** current state.														*/
	protected State				currentState = State.OFF;
	/** last received external event that must be propagated.				*/
	protected EventI			lastReceived;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an air conditioner state model instance.
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
	public AirConditionerStateModel(
			String uri, 
			TimeUnit simulatedTimeUnit, 
			SimulatorI simulationEngine)
			throws Exception 
	{
		super(uri, simulatedTimeUnit, simulationEngine);
		this.setLogger(new StandardLogger());
	}
	
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------
	
	@Override
	public void turnOn() {
		if (this.currentState == State.OFF) {
			this.currentState = State.ON;
		} else {
			// if no state change is required, no need to propagate the event
			this.lastReceived = null;
		}
	}

	@Override
	public void turnOff() {
		if (this.currentState == State.ON) {
			this.currentState = State.OFF;
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
							AirConditionerRTAtomicSimulatorPlugin.OWNER_RPNAME)) {
			AbstractComponent owner =
					(AbstractComponent) simParams.get(
							AirConditionerRTAtomicSimulatorPlugin.OWNER_RPNAME);
			// direct traces on the tracer of the owner component
			this.setLogger(new StandardComponentLogger(owner));
		}
	}
}
