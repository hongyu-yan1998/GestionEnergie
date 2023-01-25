/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events;

import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.RefrigeratorOperationI;
import fr.sorbonne_u.devs_simulation.es.events.ES_Event;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

/**
 * The class <code>stopRefrigerator</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-03</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class StopRefrigerator
extends ES_Event 
implements RefrigeratorEventI 
{

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	
	/**
	 * create a <code>stopRefrigerator</code> event.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code timeOfOccurrence != null}
	 * post	{@code this.getTimeOfOccurrence().equals(timeOfOccurrence)}
	 * </pre>
	 *
	 * @param timeOfOccurrence	time of occurrence of the event.
	 */
	public StopRefrigerator(Time timeOfOccurrence) {
		super(timeOfOccurrence, null);
	}
	
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	@Override
	public boolean hasPriorityOver(EventI e) {
		// if many refrigerator events occur at the same time, the
		// stopRefrigerator one will be executed after all others.
		return false;
	}

	@Override
	public void executeOn(AtomicModel model) {
		assert model instanceof RefrigeratorOperationI;
		
		((RefrigeratorOperationI)model).stopRefrigerator();
	}
}

