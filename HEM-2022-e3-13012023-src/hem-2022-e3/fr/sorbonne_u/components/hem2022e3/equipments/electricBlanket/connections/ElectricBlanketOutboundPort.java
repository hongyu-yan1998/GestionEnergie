package fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.ElectricBlanketCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

/**
* @author Liuyi CHEN & Hongyu YAN
* @date 4 janv. 2023
* @Description 
*/
public class ElectricBlanketOutboundPort 
extends AbstractOutboundPort 
implements ElectricBlanketCI 
{

	private static final long serialVersionUID = 1L;
	
	public ElectricBlanketOutboundPort(ComponentI owner)
	throws Exception
	{
		super(ElectricBlanketCI.class, owner);
	}
	
	public ElectricBlanketOutboundPort(
			String uri,
			ComponentI owner
			)
	throws Exception
	{
		super(uri, ElectricBlanketCI.class, owner);
	}
	
	@Override
	public boolean isRunning() throws Exception {
		return ((ElectricBlanketCI)this.getConnector()).isRunning();
	}

	@Override
	public boolean lowHeating() throws Exception {
		return ((ElectricBlanketCI)this.getConnector()).lowHeating();
	}

	@Override
	public boolean highHeating() throws Exception {
		return ((ElectricBlanketCI)this.getConnector()).highHeating();
	}

	@Override
	public void startBlanket() throws Exception {
		((ElectricBlanketCI)this.getConnector()).startBlanket();
	}

	@Override
	public void stopBlanket() throws Exception {
		((ElectricBlanketCI)this.getConnector()).stopBlanket();
	}

	@Override
	public double getCurrentTemperature() throws Exception {
		return ((ElectricBlanketCI)this.getConnector()).getCurrentTemperature();
	}

}
