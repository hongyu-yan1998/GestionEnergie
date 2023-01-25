package fr.sorbonne_u.components.hem2022e3.equipments.solar;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

/**
 * The class <code>SolarPanelCI.java</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2022-10-19</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public interface SolarPanelCI 
extends OfferedCI, 
		RequiredCI, 
		SolarPanelImplementationI 
{

	@Override
	public void startProduce() throws Exception; 

	@Override
	public void stopProduce() throws Exception;

	@Override
	public double getEnergyProduction() throws Exception;

	@Override
	public void setEnergyProduction(double energy) throws Exception;

}
