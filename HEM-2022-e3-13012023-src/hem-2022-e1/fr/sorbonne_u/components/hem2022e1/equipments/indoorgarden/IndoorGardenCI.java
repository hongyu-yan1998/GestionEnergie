package fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden;

import java.time.Instant;

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

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

// -----------------------------------------------------------------------------
/**
 * The component interface <code>IndoorGardenCI</code> defines the services
 * that an indoor garden component offers.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2022-09-16</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		IndoorGardenCI
extends		OfferedCI,
			RequiredCI,				// to simplify unit testing
			IndoorGardenImplementationI
{
	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#isOn()
	 */
	@Override
	public boolean		isOn() throws Exception;

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#on()
	 */
	@Override
	public void			on() throws Exception;

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#off()
	 */
	@Override
	public void			off() throws Exception;

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#isLightPlanned()
	 */
	@Override
	public boolean		isLightPlanned() throws Exception;

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#planLight(java.time.Instant, java.time.Instant)
	 */
	@Override
	public void			planLight(Instant timeOn, Instant timeOff)
	throws Exception;

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#cancelPlanLight()
	 */
	@Override
	public void			cancelPlanLight() throws Exception;

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#getPlannedLightOn()
	 */
	@Override
	public Instant		getPlannedLightOn() throws Exception;

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#getPlannedLightOff()
	 */
	@Override
	public Instant		getPlannedLightOff() throws Exception;

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#isLightOn()
	 */
	@Override
	public boolean		isLightOn() throws Exception;

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#isDeactivated()
	 */
	@Override
	public boolean		isDeactivated() throws Exception;

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#deactivate()
	 */
	@Override
	public void			deactivate() throws Exception;

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#reactivate()
	 */
	@Override
	public void			reactivate() throws Exception;

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#setCurrentTime(java.time.Instant)
	 */
	@Override
	public void			setCurrentTime(Instant current) throws Exception;

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#getCurrentTime()
	 */
	@Override
	public Instant		getCurrentTime() throws Exception;
}
// -----------------------------------------------------------------------------
