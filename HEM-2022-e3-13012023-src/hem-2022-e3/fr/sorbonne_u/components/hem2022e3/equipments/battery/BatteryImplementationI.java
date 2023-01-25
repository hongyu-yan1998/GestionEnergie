package fr.sorbonne_u.components.hem2022e3.equipments.battery;
/**
 * The class <code>BatteryImplementationI.java</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2022-10-19</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public interface BatteryImplementationI {

	// -------------------------------------------------------------------------
	// Component services signatures
	// -------------------------------------------------------------------------

	/**
	 * return true if the battery is fully charged.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				return true if the battery is fully charged.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean isFull() throws Exception;
	
	/**
	 * return true if the battery is dead.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return				return true if the battery is dead.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean isEmpty() throws Exception;
	
	/**
	 * charge the battery.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	!isFull()		// it can gain energy.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void gainEnergy(double energy) throws Exception;
	
	/**
	 * Discharge the battery.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	!isEmpty()		// it can provide energy.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void provideEnergy(double energy) throws Exception;
}
