/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.hem;

import java.time.Duration;
import java.time.Instant;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.hem2022.interfaces.SuspensionAndPlanningEquipmentControlCI;
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.AirConditionerCI;
/**
 * The class <code>AirConditionerConnector</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-1-11</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class AirConditionerConnector 
extends AbstractConnector
implements SuspensionAndPlanningEquipmentControlCI
{
	protected boolean	isSuspended = false;

	@Override
	public boolean		on() throws Exception
	{
		return ((AirConditionerCI)this.offering).isOn();
	}

	@Override
	public boolean		switchOn() throws Exception
	{
		if (((AirConditionerCI)this.offering).isOn()) {
			return false;
		}

		((AirConditionerCI)this.offering).on();
		return true;
	}

	@Override
	public boolean		switchOff() throws Exception
	{
		if (!((AirConditionerCI)this.offering).isOn()) {
			return false;
		}

		((AirConditionerCI)this.offering).off();
		return true;
	}

	@Override
	public int			maxMode() throws Exception
	{
		// Only one mode in air conditioner, so 1 becomes the sole "mode".
		return 1;
	}

	@Override
	public boolean		upMode() throws Exception
	{
		return false;
	}


	@Override
	public boolean		downMode() throws Exception
	{
		return false;
	}

	@Override
	public boolean		setMode(int modeIndex) throws Exception
	{
		return false;
	}

	@Override
	public int			currentMode() throws Exception
	{
		// Only one mode in air conditioner, so 1 becomes the sole "mode".
		return 1;
	}

	@Override
	public boolean		suspended() throws Exception
	{
		return this.isSuspended;
	}

	@Override
	public boolean		suspend() throws Exception
	{
		if (this.isSuspended) {
			return false;
		}

		if (((AirConditionerCI)this.offering).isAirConditionerPlanned()) {
			((AirConditionerCI)this.offering).deactivate();
		}
		this.isSuspended = true;
		return true;			
	}

	@Override
	public boolean		resume() throws Exception
	{
		if (!this.isSuspended) {
			return false;
		}

		if (((AirConditionerCI)this.offering).isAirConditionerPlanned()) {
			((AirConditionerCI)this.offering).reactivate();
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

	@Override
	public double		emergency() throws Exception
	{
		if (!((AirConditionerCI)this.offering).isAirConditionerPlanned()) {
			return 0.0;
		}

		if (((AirConditionerCI)this.offering).isDeactivated()) {
			return 0.3;
		} else {
			return 0;			
		}
	}

	@Override
	public boolean			hasPlan() throws Exception
	{
		return ((AirConditionerCI)this.offering).isAirConditionerPlanned();
	}


	@Override
	public boolean			isPlanRunning() throws Exception
	{
		if (((AirConditionerCI)this.offering).isAirConditionerPlanned()) {
			return ((AirConditionerCI)this.offering).isDeactivated();
		} else {
			return false;
		}
	}
	
	@Override
	public Instant		startTime() throws Exception
	{
		if (!((AirConditionerCI)this.offering).isAirConditionerPlanned()) {
			return null;
		}

		return ((AirConditionerCI)this.offering).getPlannedAirConditionerOn();
	}

	@Override
	public Duration			duration() throws Exception
	{
		if (!((AirConditionerCI)this.offering).isAirConditionerPlanned()) {
			return null;
		}

		return this.computeDuration(
					((AirConditionerCI)this.offering).getPlannedAirConditionerOn(),
					((AirConditionerCI)this.offering).getPlannedAirConditionerOff());
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.PlanningEquipmentControlCI#deadline()
	 */
	@Override
	public Instant		deadline() throws Exception
	{
		if (!((AirConditionerCI)this.offering).isAirConditionerPlanned()) {
			return null;
		}

		Instant s = ((AirConditionerCI)this.offering).getPlannedAirConditionerOn();
		Duration d = this.computeDuration(
						s,
						((AirConditionerCI)this.offering).getPlannedAirConditionerOff());
		d = d.dividedBy(2L);
		return s.plus(d);
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.PlanningEquipmentControlCI#postpone(java.time.Duration)
	 */
	@Override
	public boolean			postpone(Duration d) throws Exception
	{
		if (!((AirConditionerCI)this.offering).isAirConditionerPlanned() ||
			isPlanRunning() ||
			((AirConditionerCI)this.offering).isDeactivated()) {
			return false;
		}

		Instant timeOn =
				((AirConditionerCI)this.offering).getPlannedAirConditionerOn();
		Instant timeOff =
				((AirConditionerCI)this.offering).getPlannedAirConditionerOff();

		if (d.isNegative()) {
			((AirConditionerCI)this.offering).cancelPlan();
			((AirConditionerCI)this.offering).
								plan(timeOn.plus(d), timeOff.plus(d));
			return true;
		} else {
			Duration planned = this.computeDuration(timeOn, timeOff);
			if (d.compareTo(planned) < 0) {
				((AirConditionerCI)this.offering).cancelPlan();
				((AirConditionerCI)this.offering).
								plan(timeOn.plus(d), timeOff.plus(d));
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public boolean			cancel() throws Exception
	{
		if (((AirConditionerCI)this.offering).isAirConditionerPlanned()) {
			((AirConditionerCI)this.offering).cancelPlan();
			return true;
		} else {
			return false;
		}
	}
}
