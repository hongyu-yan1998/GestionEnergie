package fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.connections;

import java.time.Instant;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.AirConditionerCI;
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.AirConditionerImplementationI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

public class AirConditionerInboundPort 
extends AbstractInboundPort 
implements AirConditionerCI 
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * create an inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner instanceof AirconditionerImplementationI}
	 * post	{@code true}	// no more postcondition.
	 * </pre>
	 *
	 * @param owner			component that owns this port.
	 * @throws Exception	<i>to do.</i>
	 */
	public AirConditionerInboundPort(ComponentI owner)
	throws Exception 
	{
		super(AirConditionerCI.class, owner);
		assert owner instanceof AirConditionerImplementationI :
			new PreconditionException("owner instanceof AirconditionerImplementationI");
	}
	
	/**
	 * create an inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner instanceof AirconditionerImplementationI}
	 * post	{@code true}	// no more postcondition.
	 * </pre>
	 *
	 * @param uri			unique identifier of the port.
	 * @param owner			component that owns this port.
	 * @throws Exception  	<i>to do.</i>
	 */
	public AirConditionerInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, AirConditionerCI.class, owner);
		assert owner instanceof AirConditionerImplementationI :
				new PreconditionException("owner instanceof AirconditionerImplementationI");
	}

	@Override
	public boolean isOn() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((AirConditionerImplementationI)o).isOn());
	}

	@Override
	public void on() throws Exception {
		this.getOwner().handleRequest(
				o -> {
					((AirConditionerImplementationI)o).on();
					return null;
				});
	}

	@Override
	public void off() throws Exception {
		this.getOwner().handleRequest(
				o -> {
					((AirConditionerImplementationI)o).off();
					return null;
				});
	}

	@Override
	public boolean isAirConditionerPlanned() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((AirConditionerImplementationI)o).isAirConditionerPlanned());
	}

	@Override
	public void plan(Instant timeOn, Instant timeOff) throws Exception {
		this.getOwner().handleRequest(
				o -> {
					((AirConditionerImplementationI)o).plan(timeOn, timeOff);
					return null;
				});
	}

	@Override
	public void cancelPlan() throws Exception {
		this.getOwner().handleRequest(
				o -> {
					((AirConditionerImplementationI)o).cancelPlan();
					return null;
				});
	}

	@Override
	public Instant getPlannedAirConditionerOn() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((AirConditionerImplementationI)o).getPlannedAirConditionerOn());
	}

	@Override
	public Instant getPlannedAirConditionerOff() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((AirConditionerImplementationI)o).getPlannedAirConditionerOff());
	}

	@Override
	public boolean isDeactivated() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((AirConditionerImplementationI)o).isDeactivated());
	}

	@Override
	public void deactivate() throws Exception {
		this.getOwner().handleRequest(
				o -> {
					((AirConditionerImplementationI)o).deactivate();
					return null;
				});
	}

	@Override
	public void reactivate() throws Exception {
		this.getOwner().handleRequest(
				o -> {
					((AirConditionerImplementationI)o).reactivate();
					return null;
				});
	}

}
