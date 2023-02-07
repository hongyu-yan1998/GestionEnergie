package fr.sorbonne_u.components.hem2022e3.equipments.solar.connections;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.SolarPanelCI;

/**
 * The class <code>SolarPanelConnector.java</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2022-10-19</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class SolarPanelConnector 
extends AbstractConnector 
implements SolarPanelCI {

	@Override
	public void startProduce() throws Exception {
		((SolarPanelCI)this.offering).startProduce();
	}

	@Override
	public void stopProduce() throws Exception {
		((SolarPanelCI)this.offering).stopProduce();
	}

	@Override
	public double getEnergyProduction() throws Exception {
		return ((SolarPanelCI)this.offering).getEnergyProduction();
	}

	@Override
	public void setEnergyProduction(double energy) throws Exception {
		((SolarPanelCI)this.offering).setEnergyProduction(energy);
	}

	@Override
	public boolean isOn() throws Exception {
		return ((SolarPanelCI)this.offering).isOn();
	}

}
