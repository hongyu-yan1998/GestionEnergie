/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.connections;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.ControlledRefrigeratorImplementationI;
import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.interfaces.DataOfferedCI.DataI;
import fr.sorbonne_u.components.ports.AbstractDataInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

/**
 * The class <code>RefrigeratorSensorDataInboundPort</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-03</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class RefrigeratorSensorDataInboundPort 
extends AbstractDataInboundPort {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create the data inbound port.
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
	public				RefrigeratorSensorDataInboundPort(ComponentI owner)
	throws Exception
	{
		super(DataOfferedCI.PullCI.class, DataOfferedCI.PushCI.class, owner);

		assert owner instanceof ControlledRefrigeratorImplementationI :
				new PreconditionException(
						"owner instanceof ControlledRefrigeratorImplementationI");
	}

	/**
	 * create the data inbound port.
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
	public				RefrigeratorSensorDataInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, DataOfferedCI.PullCI.class, DataOfferedCI.PushCI.class, owner);
		assert owner instanceof ControlledRefrigeratorImplementationI :
				new PreconditionException(
						"owner instanceof ControlledRefrigeratorImplementationI");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * return the current temperatures.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more preconditions.
	 * post	{@code true}	// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.interfaces.DataOfferedCI.PullCI#get()
	 */
	@Override
	public DataI get() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((ControlledRefrigeratorImplementationI)o).
										currentTemperaturesSensor());
	}

}
