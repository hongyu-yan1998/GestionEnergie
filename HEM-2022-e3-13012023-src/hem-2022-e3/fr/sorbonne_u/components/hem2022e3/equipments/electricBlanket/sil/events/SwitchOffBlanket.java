package fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.events;

import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.ElectricBlanketOperationI;
import fr.sorbonne_u.devs_simulation.es.events.ES_Event;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

/**
* @author Liuyi CHEN & Hongyu YAN
* @date 16 nov. 2022
* @Description 
*/
public class 	SwitchOffBlanket 
extends		ES_Event
implements BlanketEventI
{
	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * create a <code>SwtichOffBlanket</code> event.
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
	public SwitchOffBlanket(
			Time timeOfOccurrence
			) 
	{
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
		// if many blanket events occur at the same time, the
		// SwtichOffBlanket one will be executed after all others.
		return false;
	}
	
	/**
	 * @see fr.sorbonne_u.devs_simulation.models.events.Event#executeOn(fr.sorbonne_u.devs_simulation.models.AtomicModel)
	 */
	@Override
	public void			executeOn(AtomicModel model)
	{
		assert	model instanceof ElectricBlanketOperationI;
		
		((ElectricBlanketOperationI)model).switchOffBlanket();
	}
}
