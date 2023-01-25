package fr.sorbonne_u.components.hem2022e3.equipments.solar.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.SolarPanelCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

/**
 * The class <code>SolarPanelOutboundPort.java</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2022-01-03</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class SolarPanelOutboundPort 
extends AbstractOutboundPort 
implements SolarPanelCI 
{

	private static final long serialVersionUID = 1L;

	public SolarPanelOutboundPort(ComponentI owner) throws Exception {
		super(SolarPanelCI.class, owner);
	}

	public SolarPanelOutboundPort(String uri, ComponentI owner)
			throws Exception {
		super(uri, SolarPanelCI.class, owner);
	}

	@Override
	public void startProduce() throws Exception {
		((SolarPanelCI)this.getConnector()).startProduce();	
	}

	@Override
	public void stopProduce() throws Exception {
		((SolarPanelCI)this.getConnector()).stopProduce();	
	}

	@Override
	public double getEnergyProduction() throws Exception {
		return ((SolarPanelCI)this.getConnector()).getEnergyProduction();	
	}

	@Override
	public void setEnergyProduction(double energy) throws Exception {
		((SolarPanelCI)this.getConnector()).setEnergyProduction(energy);	
	}

}
