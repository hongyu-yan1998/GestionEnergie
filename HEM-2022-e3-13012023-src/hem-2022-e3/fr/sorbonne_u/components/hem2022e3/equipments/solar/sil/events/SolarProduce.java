package fr.sorbonne_u.components.hem2022e3.equipments.solar.sil.events;

import fr.sorbonne_u.components.hem2022e3.equipments.solar.sil.SolarPanelOperationI;
import fr.sorbonne_u.devs_simulation.es.events.ES_Event;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

/**
 * The class <code>Produce.java</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2022-11-17</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class SolarProduce 
extends ES_Event 
implements SolarPanelEventI 
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a <code>ScolarProduce</code> event.
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
	public SolarProduce(
			Time timeOfOccurrence)
	{
		super(timeOfOccurrence, null);
	}


	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	@Override
	public boolean		hasPriorityOver(EventI e)
	{
		return false;
	}

	@Override
	public void			executeOn(AtomicModel model)
	{
		assert	model instanceof SolarPanelOperationI :
			new AssertionError("Precondition violation: "
						+ "model instanceof SolarPanelOperationI");
		
		((SolarPanelOperationI) model).startProduce();
	}
}
