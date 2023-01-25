package fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.sil;

/**
 * The class <code>AirConditionerOperationI.java</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-1-2</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public interface AirConditionerOperationI 
{
	/**
	 * turn the air conditioner on.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	public void		turnOn();

	/**
	 * turn the air conditioner off.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	public void		turnOff();
}
