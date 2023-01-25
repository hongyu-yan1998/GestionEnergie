package fr.sorbonne_u.components.hem2022e3.equipments.battery.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.hem2022e3.equipments.battery.BatteryCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

/**
 * The class <code>BatteryOutboundPort.java</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2022-10-19</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class BatteryOutboundPort 
extends AbstractOutboundPort 
implements BatteryCI 
{
	private static final long serialVersionUID = 1L;

	public BatteryOutboundPort(ComponentI owner) throws Exception {
		super(BatteryCI.class, owner);
	}

	public BatteryOutboundPort(String uri, ComponentI owner)
			throws Exception {
		super(uri, BatteryCI.class, owner);
	}

	@Override
	public boolean isFull() throws Exception {
		return ((BatteryCI)this.getConnector()).isFull();
	}

	@Override
	public boolean isEmpty() throws Exception {
		return ((BatteryCI)this.getConnector()).isEmpty();
	}

	@Override
	public void gainEnergy(double energy) throws Exception {
		((BatteryCI)this.getConnector()).gainEnergy(energy);
	}

	@Override
	public void provideEnergy(double energy) throws Exception {
		((BatteryCI)this.getConnector()).provideEnergy(energy);
	}

}
