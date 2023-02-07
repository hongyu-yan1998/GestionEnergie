/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.hem;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.SolarPanelCI;
import fr.sorbonne_u.components.hem2022e3.interfaces.ProductionEquipmentControlCI;

/**
 * The class <code>SolarPanelConnector</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-2-7</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class SolarPanelConnector 
extends AbstractConnector 
implements ProductionEquipmentControlCI 
{

	@Override
	public boolean produce() throws Exception {
		if (((SolarPanelCI)this.offering).isOn()) {
			return false;
		}

		((SolarPanelCI)this.offering).startProduce();
		return true;
	}

	@Override
	public boolean stopProduce() throws Exception {
		if (!((SolarPanelCI)this.offering).isOn()) {
			return false;
		}

		((SolarPanelCI)this.offering).stopProduce();
		return true;
	}

	@Override
	public boolean on() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

}
