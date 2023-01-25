package fr.sorbonne_u.components.hem2022e3.equipments.battery;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

/**
 * The class <code>BatteryCI.java</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2022-10-19</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public interface BatteryCI 
extends BatteryImplementationI, 
		RequiredCI, 
		OfferedCI 
{

	@Override
	public boolean isFull() throws Exception;

	@Override
	public boolean isEmpty() throws Exception;

	@Override
	public void gainEnergy(double energy) throws Exception;

	@Override
	public void provideEnergy(double energy) throws Exception;

}
