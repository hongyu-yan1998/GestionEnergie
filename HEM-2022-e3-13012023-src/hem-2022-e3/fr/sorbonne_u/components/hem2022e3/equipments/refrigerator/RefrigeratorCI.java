/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.refrigerator;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

/**
 * The class <code>RefrigeratorCI</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-03</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public interface RefrigeratorCI 
extends RefrigeratorImplementationI, 
		OfferedCI, 
		RequiredCI 
{
	@Override
	public boolean isRunning() throws Exception;

	@Override
	public void startRefrigerator() throws Exception;

	@Override
	public void stopRefrigerator() throws Exception;

	@Override
	public void setTargetFreezingTemperature(double target) throws Exception;

	@Override
	public double getTargetFreezingTemperature() throws Exception;

	@Override
	public void setTargetRefrigerationTemperature(double target) throws Exception;

	@Override
	public double getTargetRefrigerationTemperature() throws Exception;

	@Override
	public double getCurrentFreezingTemperature() throws Exception;

	@Override
	public double getCurrentRefrigerationTemperature() throws Exception;

}
