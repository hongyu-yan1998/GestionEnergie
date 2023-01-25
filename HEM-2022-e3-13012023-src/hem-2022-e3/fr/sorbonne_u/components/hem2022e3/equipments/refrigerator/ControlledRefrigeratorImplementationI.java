/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.refrigerator;

import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.RefrigeratorSensorCI.TemperatureDataI;

/**
 * The class <code>ControlledRefrigeratorImplementationI</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-03</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public interface ControlledRefrigeratorImplementationI 
extends RefrigeratorImplementationI 
{
	/**
	 * trigger a push of the refrigerator state towards the sensor reader.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			refrigeratorStateSensor() throws Exception;

	/**
	 * return the current and target temperatures to the sensor reader.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret != null}
	 * </pre>
	 *
	 * @return				the current and target temperatures.
	 * @throws Exception	<i>to do</i>.
	 */
	public TemperatureDataI	currentTemperaturesSensor() throws Exception;

	/**
	 * turn on the refrigerator.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			run() throws Exception;

	/**
	 * turn off the refrigerator.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			doNotRun() throws Exception;
}
