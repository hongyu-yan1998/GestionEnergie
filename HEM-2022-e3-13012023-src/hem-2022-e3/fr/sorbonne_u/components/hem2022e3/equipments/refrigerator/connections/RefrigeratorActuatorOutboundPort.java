/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.RefrigeratorActuatorCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

/**
 * The class <code>RefrigeratorActuatorOutboundPort</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-04</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class RefrigeratorActuatorOutboundPort 
extends		AbstractOutboundPort
implements	RefrigeratorActuatorCI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an outbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more precondition.
	 * post	{@code true}	// no more postcondition.
	 * </pre>
	 *
	 * @param owner			component that owns this port.
	 * @throws Exception 	<i>to do</i>.
	 */
	public				RefrigeratorActuatorOutboundPort(ComponentI owner)
	throws Exception
	{
		super(RefrigeratorActuatorCI.class, owner);
	}

	/**
	 * create an outbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more precondition.
	 * post	{@code true}	// no more postcondition.
	 * </pre>
	 *
	 * @param uri			unique identifier of the port.
	 * @param owner			component that owns this port.
	 * @throws Exception 	<i>to do</i>.
	 */
	public				RefrigeratorActuatorOutboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, RefrigeratorActuatorCI.class, owner);
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	@Override
	public void run() throws Exception {
		((RefrigeratorActuatorCI)this.getConnector()).run();
	}

	@Override
	public void doNotRun() throws Exception {
		((RefrigeratorActuatorCI)this.getConnector()).doNotRun();
	}
}
