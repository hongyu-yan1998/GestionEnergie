package fr.sorbonne_u.components.hem2022e3.equipments.airconditioner;

import java.time.Instant;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

/**
 * The component interface <code>AirconditionerCI</code> defines the services
 * that an air conditioner component offers.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2022-12-8</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public interface AirConditionerCI 
extends OfferedCI, 
		RequiredCI,
		AirConditionerImplementationI 
{

	@Override
	public boolean isOn() throws Exception;

	@Override
	public void on() throws Exception;

	@Override
	public void off() throws Exception;

	@Override
	public boolean isAirConditionerPlanned() throws Exception;

	@Override
	public void plan(Instant timeOn, Instant timeOff) throws Exception;
	
	@Override
	public void cancelPlan() throws Exception;

	@Override
	public Instant getPlannedAirConditionerOn() throws Exception;

	@Override
	public Instant getPlannedAirConditionerOff() throws Exception;

	@Override
	public boolean isDeactivated() throws Exception;

	@Override
	public void deactivate() throws Exception;

	@Override
	public void reactivate() throws Exception;

}
