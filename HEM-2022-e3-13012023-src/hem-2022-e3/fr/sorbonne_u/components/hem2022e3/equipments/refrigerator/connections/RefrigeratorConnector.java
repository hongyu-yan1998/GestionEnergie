/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.connections;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.RefrigeratorCI;

/**
 * The class <code>RefrigeratorConnector</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-03</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class RefrigeratorConnector
extends AbstractConnector 
implements RefrigeratorCI {

	@Override
	public boolean isRunning() throws Exception {
		return ((RefrigeratorCI)this.offering).isRunning();
	}

	@Override
	public void startRefrigerator() throws Exception {
		((RefrigeratorCI)this.offering).startRefrigerator();
	}

	@Override
	public void stopRefrigerator() throws Exception {
		((RefrigeratorCI)this.offering).stopRefrigerator();
	}

	@Override
	public void setTargetFreezingTemperature(double target) throws Exception {
		((RefrigeratorCI)this.offering).setTargetFreezingTemperature(target);
	}

	@Override
	public double getTargetFreezingTemperature() throws Exception {
		return ((RefrigeratorCI)this.offering).getTargetFreezingTemperature();
	}

	@Override
	public void setTargetRefrigerationTemperature(double target) throws Exception {
		((RefrigeratorCI)this.offering).setTargetRefrigerationTemperature(target);
	}

	@Override
	public double getTargetRefrigerationTemperature() throws Exception {
		return ((RefrigeratorCI)this.offering).getTargetRefrigerationTemperature();
	}

	@Override
	public double getCurrentFreezingTemperature() throws Exception {
		return ((RefrigeratorCI)this.offering).getCurrentFreezingTemperature();
	}

	@Override
	public double getCurrentRefrigerationTemperature() throws Exception {
		return ((RefrigeratorCI)this.offering).getCurrentRefrigerationTemperature();
	}

}
