package fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.connections;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.ElectricBlanketCI;

/**
* @author Liuyi CHEN & Hongyu YAN
* @date 4 janv. 2023
* @Description 
*/
public class ElectricBlanketConnector 
extends AbstractConnector 
implements ElectricBlanketCI 
{

	@Override
	public boolean isRunning() throws Exception {
		return ((ElectricBlanketCI)this.offering).isRunning();
	}

	@Override
	public boolean lowHeating() throws Exception {
		return ((ElectricBlanketCI)this.offering).lowHeating();
	}

	@Override
	public boolean highHeating() throws Exception {
		return ((ElectricBlanketCI)this.offering).highHeating();
	}

	@Override
	public void startBlanket() throws Exception {
		((ElectricBlanketCI)this.offering).startBlanket();
	}

	@Override
	public void stopBlanket() throws Exception {
		((ElectricBlanketCI)this.offering).stopBlanket();
	}

	@Override
	public double getCurrentTemperature() throws Exception {
		return ((ElectricBlanketCI)this.offering).getCurrentTemperature();
	}

}
