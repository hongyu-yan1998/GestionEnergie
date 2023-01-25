package fr.sorbonne_u.components.hem2022e1.equipments.hem;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a basic
// household management systems as an example of a cyber-physical system.
//
// This software is governed by the CeCILL-C license under French law and
// abiding by the rules of distribution of free software.  You can use,
// modify and/ or redistribute the software under the terms of the
// CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
// URL "http://www.cecill.info".
//
// As a counterpart to the access to the source code and  rights to copy,
// modify and redistribute granted by the license, users are provided only
// with a limited warranty  and the software's author,  the holder of the
// economic rights,  and the successive licensors  have only  limited
// liability. 
//
// In this respect, the user's attention is drawn to the risks associated
// with loading,  using,  modifying and/or developing or reproducing the
// software by the user in light of its specific status of free software,
// that may mean  that it is complicated to manipulate,  and  that  also
// therefore means  that it is reserved for developers  and  experienced
// professionals having in-depth computer knowledge. Users are therefore
// encouraged to load and test the software's suitability as regards their
// requirements in conditions enabling the security of their systems and/or 
// data to be ensured and,  more generally, to use and operate it in the 
// same conditions as regards security. 
//
// The fact that you are presently reading this means that you have had
// knowledge of the CeCILL-C license and that you accept its terms.

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

// -----------------------------------------------------------------------------
/**
 * The class <code>StandardEquipmentControlOutboundPort</code> implements an
 * outbound port for the {@code StandardEquipmentControlCI} component
 * interface.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariants
 * </pre>
 * 
 * <p>Created on : 2021-09-09</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			StandardEquipmentControlOutboundPort
extends		AbstractOutboundPort
implements	StandardEquipmentControlCI
{
	private static final long serialVersionUID = 1L;

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
	 * @param owner			component owning this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				StandardEquipmentControlOutboundPort(
		ComponentI owner
		) throws Exception
	{
		super(StandardEquipmentControlCI.class, owner);
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
	public StandardEquipmentControlOutboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, StandardEquipmentControlCI.class, owner);
	}

	/**
	 * create an extended port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code StandardEquipmentControlCI.class.isAssignableFrom(implementedInterface)}
	 * post	{@code true}	// no more postcondition.
	 * </pre>
	 *
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component owning this port.
	 * @throws Exception			<i>to do</i>.
	 */
	public				StandardEquipmentControlOutboundPort(
		Class<? extends RequiredCI> implementedInterface,
		ComponentI owner
		) throws Exception
	{
		super(implementedInterface, owner);

		assert	StandardEquipmentControlCI.class.
										isAssignableFrom(implementedInterface);
	}

	/**
	 * create an extended port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code StandardEquipmentControlCI.class.isAssignableFrom(implementedInterface)}
	 * post	{@code true}	// no more postcondition.
	 * </pre>
	 *
	 * @param uri					URI of the port.
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component owning this port.
	 * @throws Exception			<i>to do</i>.
	 */
	public				StandardEquipmentControlOutboundPort(
		String uri,
		Class<? extends RequiredCI> implementedInterface,
		ComponentI owner
		) throws Exception
	{
		super(uri, implementedInterface, owner);

		assert	StandardEquipmentControlCI.class.
										isAssignableFrom(implementedInterface);
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI#on()
	 */
	@Override
	public boolean		on() throws Exception
	{
		return ((StandardEquipmentControlCI)this.getConnector()).on();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI#switchOn()
	 */
	@Override
	public boolean		switchOn() throws Exception
	{
		assert	!this.on();
		boolean ret =
				((StandardEquipmentControlCI)this.getConnector()).switchOn();
		assert	this.on();
		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI#switchOff()
	 */
	@Override
	public boolean		switchOff() throws Exception
	{
		assert	this.on();
		boolean ret =
				((StandardEquipmentControlCI)this.getConnector()).switchOff();
		assert	!this.on();
		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI#maxMode()
	 */
	@Override
	public int			maxMode() throws Exception
	{
		int ret = ((StandardEquipmentControlCI)this.getConnector()).maxMode();
		assert ret > 0;
		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI#upMode()
	 */
	@Override
	public boolean		upMode() throws Exception
	{
		assert	this.on();
		int oldMode = this.currentMode();
		assert	oldMode < this.maxMode();
		boolean ret =
				((StandardEquipmentControlCI)this.getConnector()).upMode();
		assert	this.currentMode() > oldMode;
		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI#downMode()
	 */
	@Override
	public boolean		downMode() throws Exception
	{
		assert	this.on();
		int oldMode = this.currentMode();
		assert	oldMode > 1;
		boolean ret =
				((StandardEquipmentControlCI)this.getConnector()).downMode();
		assert	this.currentMode() < oldMode;
		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI#setMode(int)
	 */
	@Override
	public boolean		setMode(int modeIndex) throws Exception
	{
		assert	this.on();
		assert	modeIndex > 0 && modeIndex <= this.maxMode();
		boolean ret =
				((StandardEquipmentControlCI)this.getConnector()).
															setMode(modeIndex);
		assert	this.currentMode() == modeIndex;
		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI#currentMode()
	 */
	@Override
	public int			currentMode() throws Exception
	{
		assert	this.on();
		int ret = ((StandardEquipmentControlCI)this.getConnector()).
																currentMode();
		assert	ret > 0 && ret <= this.maxMode();
		return ret;
	}
}
// -----------------------------------------------------------------------------
