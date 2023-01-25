package fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket;


/**
 * The interface <code>ElecBlanketImplementationI</code> defines the signatures of
 * the services implemented by a simple electric blanket .
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : 2022-12-10</p>
 * 
 * @author	Hongyu YAN & Liuyi CHEN
 */
public interface ElectricBlanketImplementationI 
{

	/**
	 * return true if the electric blanket is currently running.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				true if the electric blanket is currently running.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isRunning() throws Exception;
	
	public boolean		lowHeating() throws  Exception;
	
	public boolean		highHeating() throws  Exception;

	/**
	 * start the electric blanket.
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
	public void			startBlanket() throws Exception;

	/**
	 * stop the electric blanket.
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
	public void			stopBlanket() throws Exception;
	
	/**
	 * return the current temperature measured by the electric blanket.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isRunning()}
	 * post	{@code return >= 10.0 && return <= 40.0}
	 * </pre>
	 *
	 * @return				the current temperature measured by the thermostat.
	 * @throws Exception	<i>to do</i>.
	 */
	public double		getCurrentTemperature() throws Exception;
	
}
