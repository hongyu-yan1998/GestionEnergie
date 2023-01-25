/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.connections;

import java.time.Instant;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.AirConditionerCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

/**
 * The class <code>AirConditionerOutboundPort</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-02</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class AirConditionerOutboundPort 
extends AbstractOutboundPort 
implements AirConditionerCI 
{
	private static final long serialVersionUID = 1L;

	public AirConditionerOutboundPort(ComponentI owner)
	throws Exception {
		super(AirConditionerCI.class, owner);
	}

	public AirConditionerOutboundPort(String uri, ComponentI owner)
	throws Exception {
		super(uri, AirConditionerCI.class, owner);
	}

	@Override
	public boolean isOn() throws Exception {
		return ((AirConditionerCI)this.getConnector()).isOn();
	}

	@Override
	public void on() throws Exception {
		((AirConditionerCI)this.getConnector()).on();		
	}

	@Override
	public void off() throws Exception {
		((AirConditionerCI)this.getConnector()).off();		
	}

	@Override
	public boolean isAirConditionerPlanned() throws Exception {
		return ((AirConditionerCI)this.getConnector()).isAirConditionerPlanned();
	}

	@Override
	public void plan(Instant timeOn, Instant timeOff) throws Exception {
		((AirConditionerCI)this.getConnector()).plan(timeOn, timeOff);
	}
	
	@Override
	public void cancelPlan() throws Exception {
		((AirConditionerCI)this.getConnector()).cancelPlan();		
	}

	@Override
	public Instant getPlannedAirConditionerOn() throws Exception {
		return ((AirConditionerCI)this.getConnector()).getPlannedAirConditionerOn();
	}

	@Override
	public Instant getPlannedAirConditionerOff() throws Exception {
		return ((AirConditionerCI)this.getConnector()).getPlannedAirConditionerOff();
	}

	@Override
	public boolean isDeactivated() throws Exception {
		return ((AirConditionerCI)this.getConnector()).isDeactivated();
	}

	@Override
	public void deactivate() throws Exception {
		((AirConditionerCI)this.getConnector()).deactivate();		
	}

	@Override
	public void reactivate() throws Exception {
		((AirConditionerCI)this.getConnector()).reactivate();		
	}
}
