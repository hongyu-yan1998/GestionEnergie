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
import fr.sorbonne_u.components.hem2022e3.equipments.heater.ThermostatedHeaterController;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.HeaterSensorCI.StateDataI;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.components.interfaces.DataRequiredCI.DataI;
import fr.sorbonne_u.components.ports.AbstractDataOutboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>HeaterSensorDataOutboundPort</code> implements the date
 * outbound port for the component interface {@code DataRequiredCI} extended by
 * the component interface {@code HeaterSensorCI}.
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
 * invariant	{@code getOwner() instanceof ThermostatedHeaterController}
 * </pre>
 * 
 * <p>Created on : 2022-10-28</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			HeaterSensorDataOutboundPort
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
	 * pre	{@code owner instanceof ThermostatedHeaterController}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param owner			component that owns this port.
	 * @throws Exception 	<i>to do</i>.
	 */
	public				HeaterSensorDataOutboundPort(ComponentI owner)
	throws Exception
	{
		super(DataRequiredCI.PullCI.class, DataRequiredCI.PushCI.class, owner);
		assert	owner instanceof ThermostatedHeaterController :
				new PreconditionException(
						"owner instanceof ThermostatedHeaterController");
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
	public				HeaterSensorDataOutboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, DataRequiredCI.PullCI.class, DataRequiredCI.PushCI.class,
			  owner);
		assert	owner instanceof ThermostatedHeaterController :
				new PreconditionException(
						"owner instanceof ThermostatedHeaterController");
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
				o -> {	((ThermostatedHeaterController)o).
									receiveRunningState((StateDataI) d);
						return null;
					 });
	}
}
// -----------------------------------------------------------------------------
