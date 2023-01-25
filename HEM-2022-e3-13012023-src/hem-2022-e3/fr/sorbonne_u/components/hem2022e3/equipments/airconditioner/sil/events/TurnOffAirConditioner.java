/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.sil.events;

import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.sil.AirConditionerOperationI;
import fr.sorbonne_u.devs_simulation.es.events.ES_Event;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

/**
 * The class <code>TurnOffAirConditioner</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-1-2</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class TurnOffAirConditioner 
extends ES_Event 
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	
	/**
	 * create a {@code TurnOffAirConditioner} event instance.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param timeOfOccurrence	time of occurrence of the event.
	 */
	public TurnOffAirConditioner(Time timeOfOccurrence) {
		super(timeOfOccurrence, null);
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.es.events.ES_Event#hasPriorityOver(fr.sorbonne_u.devs_simulation.models.events.EventI)
	 */
	@Override
	public boolean		hasPriorityOver(EventI e)
	{
		// if many airConditioner events occur at the same time, the
		// TurnOffAirConditioner one will be executed after all others.
		return false;
	}

	/**
	 * execute this event on {@code model}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code model instanceof AirConditionerOperationI}
	 * post	{@code true}	// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.devs_simulation.models.events.Event#executeOn(fr.sorbonne_u.devs_simulation.models.AtomicModel)
	 */
	@Override
	public void executeOn(AtomicModel model) {
		assert	model instanceof AirConditionerOperationI :
				new AssertionError("Precondition violation: "
							+ "model instanceof AirConditionerOperationI");

		((AirConditionerOperationI) model).turnOff();
	}
}
