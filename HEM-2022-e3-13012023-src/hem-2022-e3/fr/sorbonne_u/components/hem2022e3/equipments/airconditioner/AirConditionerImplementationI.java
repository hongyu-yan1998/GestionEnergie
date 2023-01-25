package fr.sorbonne_u.components.hem2022e3.equipments.airconditioner;

import java.time.Instant;

/**
 * The class <code>AirconditionerImplementationI.java</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2022-12-8</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public interface AirConditionerImplementationI {
	/**
	 * return true if the air conditioner is on; otherwise false.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				true if the air conditioner is on; otherwise false.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isOn() throws Exception;
	
	/**
	 * turn the air conditioner on.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isOn()}
	 * post	{@code isOn()}
	 * post	{@code !isAirConditionerPlanned()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			on() throws Exception;
	
	/**
	 * turn the air conditioner off.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isOn()}
	 * post	{@code !isOn()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			off() throws Exception;
	
	/**
	 * return true if the air conditioner has been planned.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isOn()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				true if the air conditioner has been planned.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isAirConditionerPlanned() throws Exception;
	
	/**
	 * plan the time at which the air conditioner is turned on and turned off.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isOn()}
	 * pre	{@code !isAirConditionerPlanned()}
	 * pre	{@code timeOn != null}
	 * pre	{@code timeOff != null}
	 * post	{@code timeOff.isAfter(timeOn)}
	 * post	{@code isAirConditionerPlanned()}
	 * post	{@code !isDeactivated()}
	 * </pre>
	 *
	 * @param timeOn		time at which the air conditioner is turned on.
	 * @param timeOff		time at which the air conditioner is turned off.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			plan(Instant timeOn, Instant timeOff)
	throws Exception;
	
	/**
	 * cancel the current plan.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isOn()}
	 * pre	{@code isAirConditionerPlanned()}
	 * post	{@code !isAirConditionerPlanned()}
	 * post	{@code !isOn()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			cancelPlan() throws Exception;
	
	/**
	 * return the time at which the air conditioner is turned on.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isOn()}
	 * pre	{@code isAirConditionerPlanned()}
	 * post	{@code ret != null && ret.isBefore(getPlannedAirConditionerOff())}	
	 * </pre>
	 *
	 * @return				the time at which the air conditioner is turned on.
	 * @throws Exception	<i>to do</i>.
	 */
	public Instant	getPlannedAirConditionerOn() throws Exception;

	/**
	 * return the time at which the air conditioner is turned off.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isOn()}
	 * pre	{@code isAirConditionerPlanned()}
	 * post	{@code ret != null && ret.isAfter(getPlannedAirConditionerOn())}	
	 * </pre>
	 *
	 * @return				the time at which the air conditioner is turned off.
	 * @throws Exception	<i>to do</i>.
	 */
	public Instant	getPlannedAirConditionerOff() throws Exception;
	
	/**
	 * return true if the planned air conditioner is currently deactivated.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no postcondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				true if the planned air conditioner is currently deactivated.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isDeactivated() throws Exception;

	/**
	 * deactivate the program, turning the air conditioner off if it was on when the
	 * call is made.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isOn()}
	 * pre	{@code isAirConditionerPlanned()}
	 * pre	{@code !isDeactivated()}
	 * post	{@code isDeactivated()}
	 * post	{@code !isOn()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			deactivate() throws Exception;

	/**
	 * reactivate the program, turning the air conditioner on if it was off when the
	 * call is made.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isOn()}
	 * pre	{@code isAirConditionerPlanned()}
	 * pre	{@code isDeactivated()}
	 * post	{@code !isDeactivated()}
	 * post	{@code !currentPlanActive() || isOn()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			reactivate() throws Exception;
}