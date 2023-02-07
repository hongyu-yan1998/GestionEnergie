/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.production;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.hem2022e3.interfaces.ProductionEquipmentControlCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

/**
 * The class <code>ProductionEquipmentControlOutboundPort</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-2-7</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class ProductionEquipmentControlOutboundPort
extends AbstractOutboundPort
implements ProductionEquipmentControlCI
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * create a port.
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more precondition.
	 * post	{@code true}	// no more postcondition.
	 * </pre>
	 *
	 * @param owner			component owning this port.
	 * @throws Exception
	 */
	public ProductionEquipmentControlOutboundPort(
			ComponentI owner)
			throws Exception {
		super(ProductionEquipmentControlCI.class, owner);
	}
	
	/**
	 * create a port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more precondition.
	 * post	{@code true}	// no more postcondition.
	 * </pre>
	 *
	 * @param uri			URI of the port.
	 * @param owner			component owning this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public ProductionEquipmentControlOutboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, ProductionEquipmentControlCI.class, owner);
	}

	/**
	 * create an extended port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code ProductionEquipmentControlCI.class.isAssignableFrom(implementedInterface)}
	 * post	{@code true}	// no more postcondition.
	 * </pre>
	 *
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component owning this port.
	 * @throws Exception			<i>to do</i>.
	 */
	public				ProductionEquipmentControlOutboundPort(
		Class<? extends RequiredCI> implementedInterface,
		ComponentI owner
		) throws Exception
	{
		super(implementedInterface, owner);

		assert	ProductionEquipmentControlCI.class.
										isAssignableFrom(implementedInterface);
	}

	/**
	 * create an extended port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code ProductionEquipmentControlCI.class.isAssignableFrom(implementedInterface)}
	 * post	{@code true}	// no more postcondition.
	 * </pre>
	 *
	 * @param uri					URI of the port.
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component owning this port.
	 * @throws Exception			<i>to do</i>.
	 */
	public				ProductionEquipmentControlOutboundPort(
		String uri,
		Class<? extends RequiredCI> implementedInterface,
		ComponentI owner
		) throws Exception
	{
		super(uri, implementedInterface, owner);

		assert	ProductionEquipmentControlCI.class.
										isAssignableFrom(implementedInterface);
	}
	
	@Override
	public boolean on() throws Exception {
		return ((ProductionEquipmentControlCI)this.getConnector()).on();
	}

	@Override
	public boolean produce() throws Exception {
		assert !this.on();
		boolean ret =
				((ProductionEquipmentControlCI)this.getConnector()).produce();
	    assert this.on();
		return ret;
	}

	@Override
	public boolean stopProduce() throws Exception {
		assert	this.on();
		boolean ret =
				((ProductionEquipmentControlCI)this.getConnector()).stopProduce();
		assert	!this.on();
		return ret;
	}

}