package fr.sorbonne_u.components.hem2022e3.equipments.hem;

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

import java.time.Duration;
import java.time.Instant;
import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.hem2022.interfaces.SuspensionAndPlanningEquipmentControlCI;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.IndoorGardenCI;

//------------------------------------------------------------------------------
/**
 * The class <code>IndoorGardenConnector</code> implements a connector that
 * bridges the gap between the required component interface
 * {@code SuspensionAndPlanningEquipmentControlCI} and the offered component
 * interface {@code IndoorGardenCI}.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariants
 * </pre>
 * 
 * <p>Created on : 2022-09-16</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			IndoorGardenConnector
extends		AbstractConnector
implements	SuspensionAndPlanningEquipmentControlCI
{
	protected boolean	isSuspended = false;

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI#on()
	 */
	@Override
	public boolean		on() throws Exception
	{
		return ((IndoorGardenCI)this.offering).isOn();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI#switchOn()
	 */
	@Override
	public boolean		switchOn() throws Exception
	{
		if (((IndoorGardenCI)this.offering).isOn()) {
			return false;
		}

		((IndoorGardenCI)this.offering).on();
		return true;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI#switchOff()
	 */
	@Override
	public boolean		switchOff() throws Exception
	{
		if (!((IndoorGardenCI)this.offering).isOn()) {
			return false;
		}

		((IndoorGardenCI)this.offering).off();
		return true;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI#maxMode()
	 */
	@Override
	public int			maxMode() throws Exception
	{
		// Only one mode in heater, so 1 becomes the sole "mode".
		return 1;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI#upMode()
	 */
	@Override
	public boolean		upMode() throws Exception
	{
		return false;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI#downMode()
	 */
	@Override
	public boolean		downMode() throws Exception
	{
		return false;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI#setMode(int)
	 */
	@Override
	public boolean		setMode(int modeIndex) throws Exception
	{
		return false;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI#currentMode()
	 */
	@Override
	public int			currentMode() throws Exception
	{
		// Only one mode in heater, so 1 becomes the sole "mode".
		return 1;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.SuspensionEquipmentControlCI#suspended()
	 */
	@Override
	public boolean		suspended() throws Exception
	{
		if (!((IndoorGardenCI)this.offering).isOn()) {
			return false;
		}

		return this.isSuspended;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.SuspensionEquipmentControlCI#suspend()
	 */
	@Override
	public boolean		suspend() throws Exception
	{
		if (!((IndoorGardenCI)this.offering).isOn() || this.isSuspended) {
			return false;
		}

		if (((IndoorGardenCI)this.offering).isLightPlanned()) {
			((IndoorGardenCI)this.offering).deactivate();
		}
		this.isSuspended = true;
		return true;			
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.SuspensionEquipmentControlCI#resume()
	 */
	@Override
	public boolean		resume() throws Exception
	{
		if (!((IndoorGardenCI)this.offering).isOn() || !this.isSuspended) {
			return false;
		}

		if (((IndoorGardenCI)this.offering).isLightPlanned()) {
			((IndoorGardenCI)this.offering).reactivate();
		}
		this.isSuspended = false;
		return true;
	}

	/**
	 * compute the duration between two {@code LocalTime}, including the case
	 * when the start time is after the end time, hence spanning over midnight.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param start			beginning of the time interval.
	 * @param end			end of the time interval.
	 * @return				the duration between {@code start} and {@code end}.
	 * @throws Exception	<i>to do</i>.
	 */
	protected Duration		computeDuration(Instant start, Instant end)
	throws Exception
	{
		assert	start != null;
		assert	end != null;
		return Duration.between(start, end);
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.SuspensionEquipmentControlCI#emergency()
	 */
	@Override
	public double		emergency() throws Exception
	{
		if (!((IndoorGardenCI)this.offering).isOn() ||
				!((IndoorGardenCI)this.offering).isLightPlanned()) {
			return 0.0;
		}

		if (((IndoorGardenCI)this.offering).isDeactivated()) {
			// emergency of the indoor garden is always low...
			return 0.1;
		} else {
			return 0;			
		}
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.PlanningEquipmentControlCI#hasPlan()
	 */
	@Override
	public boolean			hasPlan() throws Exception
	{
		if (!((IndoorGardenCI)this.offering).isOn()) {
			return false;
		}

		return ((IndoorGardenCI)this.offering).isLightPlanned();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.PlanningEquipmentControlCI#isPlanRunning()
	 */
	@Override
	public boolean			isPlanRunning() throws Exception
	{
		if (!((IndoorGardenCI)this.offering).isOn()) {
			return false;
		}

		if (((IndoorGardenCI)this.offering).isLightPlanned()) {
			return !((IndoorGardenCI)this.offering).isDeactivated();
		} else {
			return false;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.PlanningEquipmentControlCI#startTime()
	 */
	@Override
	public Instant			startTime() throws Exception
	{
		if (!((IndoorGardenCI)this.offering).isOn() ||
						!((IndoorGardenCI)this.offering).isLightPlanned()) {
			return null;
		}

		return ((IndoorGardenCI)this.offering).getPlannedLightOn();
	}


	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.PlanningEquipmentControlCI#duration()
	 */
	@Override
	public Duration			duration() throws Exception
	{
		if (!((IndoorGardenCI)this.offering).isOn() ||
						!((IndoorGardenCI)this.offering).isLightPlanned()) {
			return null;
		}

		return this.computeDuration(
					((IndoorGardenCI)this.offering).getPlannedLightOn(),
					((IndoorGardenCI)this.offering).getPlannedLightOff());
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.PlanningEquipmentControlCI#deadline()
	 */
	@Override
	public Instant			deadline() throws Exception
	{
		if (!((IndoorGardenCI)this.offering).isOn() ||
						!((IndoorGardenCI)this.offering).isLightPlanned()) {
			return null;
		}

		Instant s = ((IndoorGardenCI)this.offering).getPlannedLightOn();
		Duration d = this.computeDuration(
						s,
						((IndoorGardenCI)this.offering).getPlannedLightOff());
		d = d.dividedBy(2L);
		return s.plus(d);
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.PlanningEquipmentControlCI#postpone(java.time.Duration)
	 */
	@Override
	public boolean			postpone(Duration d) throws Exception
	{
		if (!((IndoorGardenCI)this.offering).isOn() ||
				!((IndoorGardenCI)this.offering).isLightPlanned() ||
					isPlanRunning() ||
							((IndoorGardenCI)this.offering).isDeactivated()) {
			return false;
		}

		Instant timeOn =
				((IndoorGardenCI)this.offering).getPlannedLightOn();
		Instant timeOff =
				((IndoorGardenCI)this.offering).getPlannedLightOff();

		if (d.isNegative()) {
			((IndoorGardenCI)this.offering).cancelPlan();
			((IndoorGardenCI)this.offering).
								plan(timeOn.plus(d), timeOff.plus(d));
			return true;
		} else {
			Duration planned = this.computeDuration(timeOn, timeOff);
			if (d.compareTo(planned) < 0) {
				((IndoorGardenCI)this.offering).cancelPlan();
				((IndoorGardenCI)this.offering).
								plan(timeOn.plus(d), timeOff.plus(d));
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.PlanningEquipmentControlCI#cancel()
	 */
	@Override
	public boolean			cancel() throws Exception
	{
		if (!((IndoorGardenCI)this.offering).isOn() ) {
			return false;
		}
		
		if (((IndoorGardenCI)this.offering).isLightPlanned()) {
			((IndoorGardenCI)this.offering).cancelPlan();
			return true;
		} else {
			return false;
		}
	}
}
// -----------------------------------------------------------------------------
