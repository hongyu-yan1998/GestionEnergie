/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.RefrigeratorCI;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.RefrigeratorImplementationI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;

/**
 * The class <code>RefrigeratorInboundPort</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-03</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class RefrigeratorInboundPort 
extends AbstractInboundPort 
implements RefrigeratorCI 
{
	private static final long serialVersionUID = 1L;

	public RefrigeratorInboundPort(ComponentI owner) throws Exception {
		super(RefrigeratorCI.class, owner);
	}

	public RefrigeratorInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, RefrigeratorCI.class, owner);
	}

	@Override
	public boolean isRunning() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((RefrigeratorImplementationI)o).isRunning());
	}

	@Override
	public void startRefrigerator() throws Exception {
		this.getOwner().handleRequest(
				o -> {	((RefrigeratorImplementationI)o).startRefrigerator();
						return null;
					 });
	}

	@Override
	public void stopRefrigerator() throws Exception {
		this.getOwner().handleRequest(
				o -> {	((RefrigeratorImplementationI)o).stopRefrigerator();
						return null;
					 });
	}

	@Override
	public void setTargetFreezingTemperature(double target) throws Exception {
		this.getOwner().handleRequest(
				o -> {	((RefrigeratorImplementationI)o).setTargetFreezingTemperature(target);
						return null;
					 });
	}

	@Override
	public double getTargetFreezingTemperature() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((RefrigeratorImplementationI)o).getTargetFreezingTemperature());
	}

	@Override
	public void setTargetRefrigerationTemperature(double target) throws Exception {
		this.getOwner().handleRequest(
				o -> {	((RefrigeratorImplementationI)o).setTargetRefrigerationTemperature(target);
						return null;
					 });
	}

	@Override
	public double getTargetRefrigerationTemperature() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((RefrigeratorImplementationI)o).getTargetRefrigerationTemperature());
	}

	@Override
	public double getCurrentFreezingTemperature() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((RefrigeratorImplementationI)o).getCurrentFreezingTemperature());
	}

	@Override
	public double getCurrentRefrigerationTemperature() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((RefrigeratorImplementationI)o).getCurrentRefrigerationTemperature());
	}
	
}
