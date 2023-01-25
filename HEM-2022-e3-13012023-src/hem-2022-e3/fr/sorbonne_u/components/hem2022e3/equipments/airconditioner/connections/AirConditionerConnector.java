/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.connections;

import java.time.Instant;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.AirConditionerCI;

/**
 * The class <code>AirConditionerConnector</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-03</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class AirConditionerConnector 
extends AbstractConnector 
implements AirConditionerCI 
{

	@Override
	public boolean isOn() throws Exception {
		return ((AirConditionerCI)this.offering).isOn();
	}

	@Override
	public void on() throws Exception {
		((AirConditionerCI)this.offering).on();
	}

	@Override
	public void off() throws Exception {
		((AirConditionerCI)this.offering).off();
	}

	@Override
	public boolean isAirConditionerPlanned() throws Exception {
		return ((AirConditionerCI)this.offering).isAirConditionerPlanned();
	}

	@Override
	public void plan(Instant timeOn, Instant timeOff) throws Exception {
		((AirConditionerCI)this.offering).plan(timeOn, timeOff);
	}

	@Override
	public void cancelPlan() throws Exception {
		((AirConditionerCI)this.offering).cancelPlan();
	}

	@Override
	public Instant getPlannedAirConditionerOn() throws Exception {
		return ((AirConditionerCI)this.offering).getPlannedAirConditionerOn();
	}

	@Override
	public Instant getPlannedAirConditionerOff() throws Exception {
		return ((AirConditionerCI)this.offering).getPlannedAirConditionerOff();
	}

	@Override
	public boolean isDeactivated() throws Exception {
		return ((AirConditionerCI)this.offering).isDeactivated();
	}

	@Override
	public void deactivate() throws Exception {
		((AirConditionerCI)this.offering).deactivate();
	}

	@Override
	public void reactivate() throws Exception {
		((AirConditionerCI)this.offering).reactivate();
	}
}
