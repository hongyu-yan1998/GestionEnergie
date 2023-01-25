package fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.ElectricBlanketCI;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.ElectricBlanketImplementationI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;

/**
* @author Liuyi CHEN & Hongyu YAN
* @date 18 oct. 2022
* @Description 
*/
public class ElectricBlanketInboundPort 
extends AbstractInboundPort 
implements ElectricBlanketCI
{
	private static final long serialVersionUID = 1L;
	
	public				ElectricBlanketInboundPort(ComponentI owner) throws Exception
	{
		super(ElectricBlanketCI.class, owner);
	}

	public				ElectricBlanketInboundPort(String uri, ComponentI owner)
	throws Exception
	{
		super(uri, ElectricBlanketCI.class, owner);
	}

	@Override
	public boolean lowHeating() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((ElectricBlanketImplementationI)o).lowHeating());
	}

	@Override
	public boolean highHeating() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((ElectricBlanketImplementationI)o).highHeating());
	}

	@Override
	public boolean isRunning() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((ElectricBlanketImplementationI)o).isRunning());
	}

	@Override
	public void startBlanket() throws Exception {
		this.getOwner().handleRequest(
				o -> {	((ElectricBlanketImplementationI)o).startBlanket();
						return null;
					 });

	}

	@Override
	public void stopBlanket() throws Exception {
		this.getOwner().handleRequest(
				o -> {	((ElectricBlanketImplementationI)o).stopBlanket();
						return null;
					 });
	}

	@Override
	public double getCurrentTemperature() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((ElectricBlanketImplementationI)o).getCurrentTemperature());
	}

}
