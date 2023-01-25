/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.refrigerator;

/**
 * The class <code>RefrigeratorImplementationI</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-03</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public interface RefrigeratorImplementationI 
{
	// -------------------------------------------------------------------------
	// Component services signatures
	// -------------------------------------------------------------------------

	/**
	 * return true if the refrigerator is currently running.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				true if the refrigerator is currently running.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isRunning() throws Exception;

	/**
	 * start the refrigerator.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isRunning()}
	 * post	{@code isRunning()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			startRefrigerator() throws Exception;

	/**
	 * stop the refrigerator.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isRunning()}
	 * post	{@code !isRunning()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			stopRefrigerator() throws Exception;

	/**
	 * set the target temperature for the thermostat controlling the refrigerator.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isRunning()}
	 * pre	{@code target >= -26.0 && target <= -16.0}
	 * post	{@code target == getTargetTemperature()}
	 * </pre>
	 *
	 * @param target		the new target temperature.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			setTargetFreezingTemperature(double target)
	throws Exception;

	/**
	 * 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isRunning()}
	 * post	{@code return >= -26.0 && return <= -16.0}
	 * </pre>
	 *
	 * @return				the current target temperature.
	 * @throws Exception	<i>to do</i>.
	 */
	public double		getTargetFreezingTemperature() throws Exception;
	
	/**
	 * set the target temperature for the thermostat controlling the refrigerator.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isRunning()}
	 * pre	{@code target >= 1.0 && target <= 10.0}
	 * post	{@code target == getTargetTemperature()}
	 * </pre>
	 *
	 * @param target		the new target temperature.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			setTargetRefrigerationTemperature(double target)
	throws Exception;

	/**
	 * 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isRunning()}
	 * post	{@code return >= 1.0 && return <= 10.0}
	 * </pre>
	 *
	 * @return				the current target temperature.
	 * @throws Exception	<i>to do</i>.
	 */
	public double		getTargetRefrigerationTemperature() throws Exception;

	/**
	 * return the current temperature measured by the thermostat.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isRunning()}
	 * post	{@code return >= -26.0 && return <= -16.0}
	 * </pre>
	 *
	 * @return				the current temperature measured by the thermostat.
	 * @throws Exception	<i>to do</i>.
	 */
	public double		getCurrentFreezingTemperature() throws Exception;
	
	/**
	 * return the current temperature measured by the thermostat.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isRunning()}
	 * post	{@code return >= 1.0 && return <= 10.0}
	 * </pre>
	 *
	 * @return				the current temperature measured by the thermostat.
	 * @throws Exception	<i>to do</i>.
	 */
	public double		getCurrentRefrigerationTemperature() throws Exception;
}
