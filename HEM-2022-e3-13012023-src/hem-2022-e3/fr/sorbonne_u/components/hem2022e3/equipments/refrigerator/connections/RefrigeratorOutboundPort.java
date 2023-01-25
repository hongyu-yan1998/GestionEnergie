/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.RefrigeratorCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

/**
 * The class <code>RefrigeratorOutboundPort</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-03</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class RefrigeratorOutboundPort 
extends AbstractOutboundPort 
implements RefrigeratorCI {

	private static final long serialVersionUID = 1L;

	public RefrigeratorOutboundPort(ComponentI owner)
	throws Exception {
		super(RefrigeratorCI.class, owner);
	}

	public RefrigeratorOutboundPort(String uri, ComponentI owner)
	throws Exception {
		super(uri, RefrigeratorCI.class, owner);
	}

	@Override
	public boolean isRunning() throws Exception {
		return ((RefrigeratorCI)this.getConnector()).isRunning();	}

	@Override
	public void startRefrigerator() throws Exception {
		((RefrigeratorCI)this.getConnector()).startRefrigerator();
	}

	@Override
	public void stopRefrigerator() throws Exception {
		((RefrigeratorCI)this.getConnector()).stopRefrigerator();
	}

	@Override
	public void setTargetFreezingTemperature(double target) throws Exception {
		((RefrigeratorCI)this.getConnector()).setTargetFreezingTemperature(target);
	}

	@Override
	public double getTargetFreezingTemperature() throws Exception {
		return ((RefrigeratorCI)this.getConnector()).getTargetFreezingTemperature();
	}

	@Override
	public void setTargetRefrigerationTemperature(double target) throws Exception {
		((RefrigeratorCI)this.getConnector()).setTargetRefrigerationTemperature(target);
	}

	@Override
	public double getTargetRefrigerationTemperature() throws Exception {
		return ((RefrigeratorCI)this.getConnector()).getTargetRefrigerationTemperature();
	}

	@Override
	public double getCurrentFreezingTemperature() throws Exception {
		return ((RefrigeratorCI)this.getConnector()).getCurrentFreezingTemperature();
	}

	@Override
	public double getCurrentRefrigerationTemperature() throws Exception {
		return ((RefrigeratorCI)this.getConnector()).getCurrentRefrigerationTemperature();
	}

}
