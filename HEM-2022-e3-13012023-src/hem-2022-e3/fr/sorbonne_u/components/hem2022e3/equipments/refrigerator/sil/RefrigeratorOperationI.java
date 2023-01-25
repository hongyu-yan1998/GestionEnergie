/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil;

/**
 * The interface <code>RefrigeratorOperationI</code> declares operations that can be
 * executed on a simulation model used in the refrigerator SIL simulation.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-03</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public interface RefrigeratorOperationI 
{

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
	default void			startRefrigerator() { /* by default, do nothing. */ }

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
	default void			stopRefrigerator() { /* by default, do nothing. */ }
	
	/**
	 * make the refrigerator start running; this internal method is
	 * meant to be executed by the refrigerator when the internal temperature
	 * comes over the maximum target temperature.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	default void		run()			{ /* by default, do nothing. */ }

	/**
	 * make the refrigerator stop running; this internal method is
	 * meant to be executed by the refrigerator when the internal temperature
	 * is below the minimum target temperature after a period of running.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	default void		doNotRun()		{ /* by default, do nothing. */ }
}
