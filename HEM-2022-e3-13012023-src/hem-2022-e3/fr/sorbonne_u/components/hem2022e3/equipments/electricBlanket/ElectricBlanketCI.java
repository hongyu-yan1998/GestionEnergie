package fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

/**
 * The component interface <code>ElectricBlanketCI</code> defines the signatures of
 * services that can be called on the electric blanket component.
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
 * @author Liuyi CHEN & Hongyu YAN
 */
public interface ElectricBlanketCI 
extends ElectricBlanketImplementationI,
		RequiredCI,
		OfferedCI 
{
	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.suspension.electricblanket.ElectricBlanketImplementationI#isRunning()
	 */
	@Override
	public boolean		isRunning() throws Exception;
	
	@Override
	public boolean		lowHeating() throws  Exception;
	
	@Override
	public boolean		highHeating() throws  Exception;

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.suspension.electricblanket.ElectricBlanketImplementationI#startBlanket()
	 */
	@Override
	public void 		startBlanket() throws Exception;

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.suspension.electricblanket.ElectricBlanketImplementationI#stopBlanket()
	 */
	@Override
	public void 		stopBlanket() throws Exception;

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.suspension.electricblanket.ElectricBlanketImplementationI#getCurrentTemperature()
	 */
	@Override
	public double 		getCurrentTemperature() throws Exception;
}
