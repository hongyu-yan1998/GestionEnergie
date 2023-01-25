package fr.sorbonne_u.components.hem2022e3.equipments.battery.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.hem2022e3.equipments.battery.BatteryCI;
import fr.sorbonne_u.components.hem2022e3.equipments.battery.BatteryImplementationI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;

/**
 * The class <code>BatteryInboundPort.java</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2022-10-19</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class BatteryInboundPort 
extends AbstractInboundPort 
implements BatteryCI {

	private static final long serialVersionUID = 1L;

	public BatteryInboundPort(ComponentI owner) throws Exception {
		super(BatteryCI.class, owner);
	}

	public BatteryInboundPort(String uri, ComponentI owner)
			throws Exception {
		super(uri, BatteryCI.class, owner);
	}

	@Override
	public boolean isFull() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((BatteryImplementationI)o).isFull());
	}

	@Override
	public boolean isEmpty() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((BatteryImplementationI)o).isEmpty());
	}

	@Override
	public void gainEnergy(double energy) throws Exception {
		this.getOwner().handleRequest(
				o -> {	((BatteryImplementationI)o).gainEnergy(energy);
						return null;
					 });
	}

	@Override
	public void provideEnergy(double energy) throws Exception {
		this.getOwner().handleRequest(
				o -> {	((BatteryImplementationI)o).provideEnergy(energy);
						return null;
					 });
	}

}
