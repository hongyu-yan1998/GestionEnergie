package fr.sorbonne_u.components.hem2022e3.equipments.solar;
/**
 * The class <code>SolarPanelImplementationsI.java</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2022-10-19</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public interface SolarPanelImplementationI {
	public boolean		isOn() throws Exception;
	
	public void startProduce() throws Exception;
	
	public void stopProduce() throws Exception;
	
	public double getEnergyProduction() throws Exception;
	
	public void setEnergyProduction(double energy) throws Exception;
}
