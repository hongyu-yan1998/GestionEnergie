/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.interfaces;

import fr.sorbonne_u.components.interfaces.RequiredCI;

/**
 * The class <code>ProductionEquipmentControlCI</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-2-7</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public interface ProductionEquipmentControlCI 
extends RequiredCI 
{	
	/**
	 * return true if the equipment is on or false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				true if the equipment is on or false otherwise.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		on() throws Exception;
	
	/**
	 * the equipment start producing, returning true if the operation succeeded or
	 * false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !on()}
	 * post	{@code on()}
	 * </pre>
	 *
	 * @return				true if the operation succeeded or false otherwise.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		produce() throws Exception;

	/**
	 * the equipment stop producing, returning true if the operation succeeded or
	 * false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code on()}
	 * post	{@code !on()}
	 * </pre>
	 *
	 * @return				true if the operation succeeded or false otherwise.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		stopProduce() throws Exception;
}
