package fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.events;

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

import fr.sorbonne_u.devs_simulation.es.events.ES_Event;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.IndoorGardenOperationI;

//------------------------------------------------------------------------------
/**
 * The class <code>SwitchOffIndoorGarden</code> implements the events that
 * switch off the indoor garden.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariants
 * </pre>
 * 
 * <p>Created on : 2022-10-04</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			SwitchOffIndoorGarden
extends		ES_Event
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a {@code SwitchOffIndoorGarden} event instance.
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
	public				SwitchOffIndoorGarden(Time timeOfOccurrence)
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
		// SwitchOnIndoorGarden < SwitchOffIndoorGarden < 
		// SwitchLightOnIndoorGarden < SwitchLightOffIndoorGarden
		if (e instanceof SwitchLightOnIndoorGarden ||
									e instanceof SwitchLightOffIndoorGarden) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * execute this event on {@code model}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code model instanceof IndoorGardenOperationI}
	 * post	{@code true}	// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.devs_simulation.models.events.Event#executeOn(fr.sorbonne_u.devs_simulation.models.AtomicModel)
	 */
	@Override
	public void executeOn(AtomicModel model) {
		assert	model instanceof IndoorGardenOperationI :
				new AssertionError("Precondition violation: "
							+ "model instanceof IndoorGardenOperationI");

		((IndoorGardenOperationI) model).switchOff();
	}
}
// -----------------------------------------------------------------------------