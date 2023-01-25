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
 * The class <code>startRefrigerator</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023Äê1ÔÂ3ÈÕ</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class StartRefrigerator
extends ES_Event 
implements RefrigeratorEventI 
{

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a <code>startRefrigerator</code> event.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code timeOfOccurrence != null}
	 * post	{@code this.getTimeOfOccurrence().equals(timeOfOccurrence)}
	 * post	{@code this.getEventInformation.equals(content)}
	 * </pre>
	 *  
	 * @param timeOfOccurrence time of occurrence of the event.
	 */
	public StartRefrigerator(Time timeOfOccurrence) {
		super(timeOfOccurrence,null);
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	@Override
	public boolean hasPriorityOver(EventI e) {
		// if many refrigerator events occur at the same time, the
		// startRefrigerator one will be executed first.
		return true;
	}

	@Override
	public void executeOn(AtomicModel model) {
		assert model instanceof RefrigeratorOperationI;
		
		((RefrigeratorOperationI)model).startRefrigerator();
	}

}

