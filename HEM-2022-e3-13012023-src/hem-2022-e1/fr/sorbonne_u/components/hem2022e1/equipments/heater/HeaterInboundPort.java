package fr.sorbonne_u.components.hem2022e1.equipments.heater;

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
import fr.sorbonne_u.components.ports.AbstractInboundPort;

// -----------------------------------------------------------------------------
/**
 * The class <code>HeaterInboundPort</code> implements an inbound port for the
 * {@code HeaterCI} component interface.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2021-09-10</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			HeaterInboundPort
extends		AbstractInboundPort
implements	HeaterCI
{
	private static final long serialVersionUID = 1L;

	public				HeaterInboundPort(ComponentI owner) throws Exception
	{
		super(HeaterCI.class, owner);
	}

	public				HeaterInboundPort(String uri, ComponentI owner)
	throws Exception
	{
		super(uri, HeaterCI.class, owner);
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.heater.HeaterCI#isRunning()
	 */
	@Override
	public boolean		isRunning() throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((HeaterImplementationI)o).isRunning());
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.heater.HeaterCI#startHeater()
	 */
	@Override
	public void			startHeater() throws Exception
	{
		this.getOwner().handleRequest(
				o -> {	((HeaterImplementationI)o).startHeater();
						return null;
					 });
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.heater.HeaterCI#stopHeater()
	 */
	@Override
	public void			stopHeater() throws Exception
	{
		this.getOwner().handleRequest(
				o -> {	((HeaterImplementationI)o).stopHeater();
						return null;
					 });
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.heater.HeaterCI#setTargetTemperature(double)
	 */
	@Override
	public void			setTargetTemperature(double target) throws Exception
	{
		this.getOwner().handleRequest(
				o -> {	((HeaterImplementationI)o).setTargetTemperature(target);
						return null;
					 });
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.heater.HeaterCI#getTargetTemperature()
	 */
	@Override
	public double		getTargetTemperature() throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((HeaterImplementationI)o).getTargetTemperature());
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.heater.HeaterCI#getCurrentTemperature()
	 */
	@Override
	public double		getCurrentTemperature() throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((HeaterImplementationI)o).getCurrentTemperature());
	}
}
// -----------------------------------------------------------------------------
