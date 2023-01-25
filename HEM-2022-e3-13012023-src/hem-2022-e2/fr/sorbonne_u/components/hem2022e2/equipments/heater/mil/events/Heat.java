package fr.sorbonne_u.components.hem2022e2.equipments.heater.mil.events;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a basic
// household management systems as an example of a cyber-physical system.
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

import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.components.hem2022e2.equipments.heater.mil.HeaterElectricityModel;
import fr.sorbonne_u.components.hem2022e2.equipments.heater.mil.HeaterTemperatureModel;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;

// -----------------------------------------------------------------------------
/**
 * The class <code>Heat</code> defines the simulation event of the heater
 * starting to heat.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2021-09-21</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			Heat
extends		Event
implements	HeaterEventI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a <code>Heat</code> event.
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
	public				Heat(
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
		// if many heater events occur at the same time, the Heat one will be
		// executed after SwitchOnHeater and DoNotHeat ones but before
		// SwitchOffHeater.
		if (e instanceof SwitchOnHeater || e instanceof DoNotHeat) {
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
		// the Heat event can be executed either on the heater electricity
		// or temperature models 
		assert	model instanceof HeaterElectricityModel ||
									model instanceof HeaterTemperatureModel;

		if (model instanceof HeaterElectricityModel) {
			HeaterElectricityModel heater = (HeaterElectricityModel)model;
			assert	heater.getState() == HeaterElectricityModel.State.ON;
			heater.setState(HeaterElectricityModel.State.HEATING);
		} else if (model instanceof HeaterTemperatureModel) {
			HeaterTemperatureModel heaterTemperature =
											(HeaterTemperatureModel)model;
			heaterTemperature.setState(HeaterTemperatureModel.State.HEATING);
		}
	}
}
// -----------------------------------------------------------------------------
