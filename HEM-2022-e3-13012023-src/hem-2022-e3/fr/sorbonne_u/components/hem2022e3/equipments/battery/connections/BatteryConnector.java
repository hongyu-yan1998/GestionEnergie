package fr.sorbonne_u.components.hem2022e3.equipments.battery.connections;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.hem2022e3.equipments.battery.BatteryCI;

/**
 * The class <code>BatteryConnector.java</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2022-10-19</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class BatteryConnector 
extends AbstractConnector 
implements BatteryCI {

	@Override
	public boolean isFull() throws Exception {
		return ((BatteryCI)this.offering).isFull();
	}

	@Override
	public boolean isEmpty() throws Exception {
		return ((BatteryCI)this.offering).isEmpty();
	}

	@Override
	public void gainEnergy(double energy) throws Exception {
		((BatteryCI)this.offering).gainEnergy(energy);
	}

	@Override
	public void provideEnergy(double energy) throws Exception {
		((BatteryCI)this.offering).provideEnergy(energy);
	}

}
