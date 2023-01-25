package fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.events;

import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.ElectricBlanketOperationI;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

/**
 * The class <code>DoNotHeat</code> defines the simulation event of the
 * blanket stopping to heat.
 * 
* @author Liuyi CHEN & Hongyu YAN
* @date 16 nov. 2022
* @Description 
*/
public class DoNotHeat 
extends Event 
implements BlanketEventI 
{

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	
	/**
	 * create a <code>DoNotHeat</code> event.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code timeOfOccurrence != null}
	 * post	{@code this.getTimeOfOccurrence().equals(timeOfOccurrence)}
	 * post	{@code this.getEventInformation.equals(content)}
	 * </pre>
	 *
	 * @param timeOfOccurrence	time of occurrence of the event.
	 */
	public				DoNotHeat(
		Time timeOfOccurrence
		)
	{
		super(timeOfOccurrence, null);
	}
	
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.events.Event#hasPriorityOver(fr.sorbonne_u.devs_simulation.models.events.EventI)
	 */
	@Override
	public boolean		hasPriorityOver(EventI e)
	{
		// SwitchOffBlanket < SetHighTemperature
		// SetLowTemperature < DoNotHeat < SwitchOnBlanket
		if (e instanceof SwitchOnBlanket) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * @see fr.sorbonne_u.devs_simulation.models.events.Event#executeOn(fr.sorbonne_u.devs_simulation.models.AtomicModel)
	 */
	@Override
	public void			executeOn(AtomicModel model)
	{
		// the DoNotHeat event can be executed either on the heater electricity
		// or temperature models 
		assert	model instanceof ElectricBlanketOperationI;
		
		((ElectricBlanketOperationI)model).doNotHeat();
	}
}
