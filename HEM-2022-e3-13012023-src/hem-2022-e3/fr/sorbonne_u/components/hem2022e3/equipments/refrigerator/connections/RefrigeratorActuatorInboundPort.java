/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.ControlledRefrigeratorImplementationI;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.RefrigeratorActuatorCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

/**
 * The class <code>RefrigeratorActuatorInboundPort</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-03</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class RefrigeratorActuatorInboundPort 
extends AbstractInboundPort 
implements RefrigeratorActuatorCI 
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner instanceof ControlledRefrigeratorImplementationI}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param owner			component that owns this port.
	 * @throws Exception 	<i>to do</i>.
	 */
	public				RefrigeratorActuatorInboundPort(ComponentI owner)
	throws Exception
	{
		super(RefrigeratorActuatorCI.class, owner);

		assert	owner instanceof ControlledRefrigeratorImplementationI :
				new PreconditionException(
						"owner instanceof ControlledRefrigeratorImplementationI");
	}

	/**
	 * create an inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param uri			unique identifier of the port.
	 * @param owner			component that owns this port.
	 * @throws Exception 	<i>to do</i>.
	 */
	public				RefrigeratorActuatorInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, RefrigeratorActuatorCI.class, owner);

		assert	owner instanceof ControlledRefrigeratorImplementationI :
				new PreconditionException(
						"owner instanceof ControlledRefrigeratorImplementationI");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	@Override
	public void run() throws Exception {
		this.getOwner().handleRequest(
				o -> { ((ControlledRefrigeratorImplementationI)o).run();
					   return null;
					 });
	}

	@Override
	public void doNotRun() throws Exception {
		this.getOwner().handleRequest(
				o -> { ((ControlledRefrigeratorImplementationI)o).doNotRun();
					   return null;
					 });
	}

}
