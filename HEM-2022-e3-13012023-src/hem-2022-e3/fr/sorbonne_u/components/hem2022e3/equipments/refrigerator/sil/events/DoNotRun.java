/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events;

import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.RefrigeratorOperationI;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

/**
 * The class <code>DoNotRun</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-03</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class DoNotRun
extends Event 
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
	 * create a <code>DoNotRun</code> event.
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
	public DoNotRun(Time timeOfOccurrence) {
		super(timeOfOccurrence, null);
	}
	
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	@Override
	public boolean hasPriorityOver(EventI e) {
		// if many refrigerator events occur at the same time, the DoNotRun one 
		// will be first except for startRefrigerator ones.
		if (e instanceof StartRefrigerator) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void executeOn(AtomicModel model) {
		assert model instanceof RefrigeratorOperationI;
		
		((RefrigeratorOperationI)model).doNotRun();
	}
}
