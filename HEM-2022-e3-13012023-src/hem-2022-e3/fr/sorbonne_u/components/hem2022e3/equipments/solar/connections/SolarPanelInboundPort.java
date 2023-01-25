package fr.sorbonne_u.components.hem2022e3.equipments.solar.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.SolarPanelCI;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.SolarPanelImplementationI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;

/**
 * The class <code>SolarPanelInboundPort.java</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2022-10-19</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class SolarPanelInboundPort 
extends AbstractInboundPort
implements SolarPanelCI
{
	private static final long serialVersionUID = 1L;

	public SolarPanelInboundPort(ComponentI owner) throws Exception {
		super(SolarPanelCI.class, owner);
	}

	public SolarPanelInboundPort(String uri, ComponentI owner)
			throws Exception {
		super(uri, SolarPanelCI.class, owner);
	}

	@Override
	public void startProduce() throws Exception {
		this.getOwner().handleRequest(
				o -> {	((SolarPanelImplementationI)o).startProduce();
				return null;
			 });		
	}

	@Override
	public void stopProduce() throws Exception {
		this.getOwner().handleRequest(
				o -> {	((SolarPanelImplementationI)o).stopProduce();
				return null;
			 });			
	}

	@Override
	public double getEnergyProduction() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((SolarPanelImplementationI)o).getEnergyProduction());
	}

	@Override
	public void setEnergyProduction(double energy) throws Exception {
		this.getOwner().handleRequest(
				o -> {	((SolarPanelImplementationI)o).setEnergyProduction(energy);
				return null;
			 });			
	}

}
