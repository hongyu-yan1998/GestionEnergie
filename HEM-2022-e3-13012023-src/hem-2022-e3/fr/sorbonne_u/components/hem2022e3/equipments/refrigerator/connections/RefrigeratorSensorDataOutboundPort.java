/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.RefrigeratorController;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.RefrigeratorSensorCI.StateDataI;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.components.interfaces.DataRequiredCI.DataI;
import fr.sorbonne_u.components.ports.AbstractDataOutboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

/**
 * The class <code>RefrigeratorSensorDataOutboundPort</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-04</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class RefrigeratorSensorDataOutboundPort
extends		AbstractDataOutboundPort
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create the data outbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner instanceof RefrigeratorController}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param owner			component that owns this port.
	 * @throws Exception 	<i>to do</i>.
	 */
	public				RefrigeratorSensorDataOutboundPort(ComponentI owner)
	throws Exception
	{
		super(DataRequiredCI.PullCI.class, DataRequiredCI.PushCI.class, owner);
		assert	owner instanceof RefrigeratorController :
				new PreconditionException(
						"owner instanceof RefrigeratorController");
	}

	/**
	 * create the data outbound port.
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
	public				RefrigeratorSensorDataOutboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, DataRequiredCI.PullCI.class, DataRequiredCI.PushCI.class,
			  owner);
		assert	owner instanceof RefrigeratorController :
				new PreconditionException(
						"owner instanceof RefrigeratorController");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * receives the running state in push mode.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more preconditions.
	 * post	{@code true}	// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.interfaces.DataRequiredCI.PushCI#receive(fr.sorbonne_u.components.interfaces.DataRequiredCI.DataI)
	 */
	@Override
	public void			receive(DataI d) throws Exception
	{
		this.getOwner().handleRequest(
				o -> {	((RefrigeratorController)o).
									receiveRunningState((StateDataI) d);
						return null;
					 });
	}
}
