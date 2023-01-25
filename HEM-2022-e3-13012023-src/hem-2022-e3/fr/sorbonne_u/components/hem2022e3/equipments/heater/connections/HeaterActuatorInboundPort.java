package fr.sorbonne_u.components.hem2022e3.equipments.heater.connections;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// new implementation of the DEVS simulation standard for Java.
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
import fr.sorbonne_u.components.hem2022e3.equipments.heater.ControlledHeaterImplementationI;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.HeaterActuatorCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>HeaterActuatorInboundPort</code> implements the inbound port
 * for the component interface {@code HeaterActuatorCI}.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code getOwner() instanceof ControlledHeaterImplementationI}
 * </pre>
 * 
 * <p>Created on : 2022-10-27</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			HeaterActuatorInboundPort
extends		AbstractInboundPort
implements	HeaterActuatorCI
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
	 * pre	{@code owner instanceof ControlledHeaterImplementationI}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param owner			component that owns this port.
	 * @throws Exception 	<i>to do</i>.
	 */
	public				HeaterActuatorInboundPort(ComponentI owner)
	throws Exception
	{
		super(HeaterActuatorCI.class, owner);

		assert	owner instanceof ControlledHeaterImplementationI :
				new PreconditionException(
						"owner instanceof ControlledHeaterImplementationI");
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
	public				HeaterActuatorInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, HeaterActuatorCI.class, owner);

		assert	owner instanceof ControlledHeaterImplementationI :
				new PreconditionException(
						"owner instanceof ControlledHeaterImplementationI");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2022e3.equipments.heater.HeaterActuatorCI#heat()
	 */
	@Override
	public void			heat() throws Exception
	{
		this.getOwner().handleRequest(
				o -> { ((ControlledHeaterImplementationI)o).heat();
					   return null;
					 });
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e3.equipments.heater.HeaterActuatorCI#doNotHeat()
	 */
	@Override
	public void			doNotHeat() throws Exception
	{
		this.getOwner().handleRequest(
				o -> { ((ControlledHeaterImplementationI)o).doNotHeat();;
					   return null;
					 });
	}
}
// -----------------------------------------------------------------------------
