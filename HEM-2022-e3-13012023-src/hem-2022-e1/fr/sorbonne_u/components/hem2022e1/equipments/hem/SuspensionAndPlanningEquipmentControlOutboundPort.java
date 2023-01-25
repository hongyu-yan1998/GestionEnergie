package fr.sorbonne_u.components.hem2022e1.equipments.hem;

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

import java.time.Duration;
import java.time.Instant;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.hem2022.interfaces.SuspensionAndPlanningEquipmentControlCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>SuspensionAndPlanningEquipmentControlOutboundPort</code>
 * implements an outbound port for the
 * {@code SuspensionAndPlanningEquipmentControlCI} component interface.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariants
 * </pre>
 * 
 * <p>Created on : 2022-09-19</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			SuspensionAndPlanningEquipmentControlOutboundPort
extends		SuspensionEquipmentControlOutboundPort
implements	SuspensionAndPlanningEquipmentControlCI
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
	public				SuspensionAndPlanningEquipmentControlOutboundPort(
		ComponentI owner
		) throws Exception
	{
		super(SuspensionAndPlanningEquipmentControlCI.class, owner);
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
	public SuspensionAndPlanningEquipmentControlOutboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, SuspensionAndPlanningEquipmentControlCI.class, owner);
	}

	/**
	 * create an extended port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code SuspensionAndPlanningEquipmentControlCI.class.isAssignableFrom(implementedInterface)}
	 * post	{@code true}	// no more postcondition.
	 * </pre>
	 *
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component owning this port.
	 * @throws Exception			<i>to do</i>.
	 */
	public				SuspensionAndPlanningEquipmentControlOutboundPort(
		Class<? extends RequiredCI> implementedInterface,
		ComponentI owner
		) throws Exception
	{
		super(implementedInterface, owner);
		assert	SuspensionAndPlanningEquipmentControlCI.class.isAssignableFrom(implementedInterface) :
				new PreconditionException(
						"SuspensionAndPlanningEquipmentControlCI.class."
						+ "isAssignableFrom(implementedInterface)");
	}

	/**
	 * create an extended port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code SuspensionAndPlanningEquipmentControlCI.class.isAssignableFrom(implementedInterface)}
	 * post	{@code true}	// no more postcondition.
	 * </pre>
	 *
	 * @param uri					URI of the port.
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component owning this port.
	 * @throws Exception			<i>to do</i>.
	 */
	public				SuspensionAndPlanningEquipmentControlOutboundPort(
		String uri,
		Class<? extends RequiredCI> implementedInterface,
		ComponentI owner
		) throws Exception
	{
		super(uri, implementedInterface, owner);
		assert	SuspensionAndPlanningEquipmentControlCI.class.isAssignableFrom(implementedInterface) :
				new PreconditionException(
						"SuspensionAndPlanningEquipmentControlCI.class."
						+ "isAssignableFrom(implementedInterface)");
	}


	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.PlanningEquipmentControlCI#hasPlan()
	 */
	@Override
	public boolean		hasPlan() throws Exception
	{
		return ((SuspensionAndPlanningEquipmentControlCI)this.getConnector()).
																hasPlan();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.PlanningEquipmentControlCI#isPlanRunning()
	 */
	@Override
	public boolean		isPlanRunning() throws Exception
	{
		return ((SuspensionAndPlanningEquipmentControlCI)this.getConnector()).
																isPlanRunning();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.PlanningEquipmentControlCI#startTime()
	 */
	@Override
	public Instant		startTime() throws Exception
	{
		return ((SuspensionAndPlanningEquipmentControlCI)this.getConnector()).
																startTime();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.PlanningEquipmentControlCI#duration()
	 */
	@Override
	public Duration		duration() throws Exception
	{
		return ((SuspensionAndPlanningEquipmentControlCI)this.getConnector()).
																duration();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.PlanningEquipmentControlCI#deadline()
	 */
	@Override
	public Instant		deadline() throws Exception
	{
		return ((SuspensionAndPlanningEquipmentControlCI)this.getConnector()).
																deadline();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.PlanningEquipmentControlCI#postpone(java.time.Duration)
	 */
	@Override
	public boolean		postpone(Duration d) throws Exception
	{
		return ((SuspensionAndPlanningEquipmentControlCI)this.getConnector()).
																postpone(d);
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.PlanningEquipmentControlCI#cancel()
	 */
	@Override
	public boolean		cancel() throws Exception
	{
		return ((SuspensionAndPlanningEquipmentControlCI)this.getConnector()).
																cancel();
	}
}
// -----------------------------------------------------------------------------
