package fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden;

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

import java.time.Instant;

// -----------------------------------------------------------------------------
/**
 * The interface <code>IndoorGardenImplementationI</code> defines the methods
 * that an indoor garden must implement for its services.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no invariant
 * </pre>
 * 
 * <p>Created on : 2022-09-14</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		IndoorGardenImplementationI
{
	/**
	 * return true if the indoor garden is on; otherwise false.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				true if the indoor garden is on; otherwise false.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isOn() throws Exception;

	/**
	 * switch the indoor garden on.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isOn()}
	 * post	{@code isOn()}
	 * post	{@code !isLightOn()}
	 * post	{@code !isLightPlanned()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			on() throws Exception;

	/**
	 * switch the indoor garden off.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isOn()}
	 * post	{@code !isOn()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			off() throws Exception;

	/**
	 * return true if the indoor garden has been planned.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				true if the indoor garden has been planned.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isLightPlanned() throws Exception;

	/**
	 * plan the time at which the light is switched on and switched off.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isOn()}
	 * pre	{@code !isLightPlanned()}
	 * pre	{@code timeOn != null}
	 * pre	{@code timeOff != null}
	 * pre	{@code timeOff.isAfter(timeOn)}
	 * post	{@code isLightPlanned()}
	 * post	{@code !isDeactivated()}
	 * </pre>
	 *
	 * @param timeOn		time at which the light is switched on.
	 * @param timeOff		time at which the light is switched off.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			plan(Instant timeOn, Instant timeOff)
	throws Exception;

	/**
	 * cancel the current plan.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isOn()}
	 * pre	{@code isLightPlanned()}
	 * post	{@code !isLightPlanned()}
	 * post	{@code !isLightOn()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			cancelPlan() throws Exception;

	/**
	 * return the time at which the light is switched on.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isOn()}
	 * pre	{@code isLightPlanned()}
	 * post	{@code ret != null && ret.isBefore(getPlannedLightOff())}
	 * </pre>
	 *
	 * @return				the time at which the light is switched on.
	 * @throws Exception	<i>to do</i>.
	 */
	public Instant		getPlannedLightOn() throws Exception;

	/**
	 * return the time at which the light is switched off.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isOn()}
	 * pre	{@code isLightPlanned()}
	 * post	{@code ret != null && ret.isAfter(getPlannedLightOn())}
	 * </pre>
	 *
	 * @return				the time at which the light is switched off.
	 * @throws Exception	<i>to do</i>.
	 */
	public Instant	getPlannedLightOff() throws Exception;

	/**
	 * return true if the light should be currently on unless currently
	 * deactivated.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				true if the light should be currently on unless currently deactivated.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isLightOn() throws Exception;

	/**
	 * return true if the planned lighting is currently deactivated.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				true if the planned lighting is currently deactivated.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isDeactivated() throws Exception;

	/**
	 * deactivate the program, switching the light off if it was on when the
	 * call is made.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isOn()}
	 * pre	{@code isLightPlanned()}
	 * pre	{@code !isDeactivated()}
	 * post	{@code isDeactivated()}
	 * post	{@code !isLightOn()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			deactivate() throws Exception;

	/**
	 * deactivate the program, switching the light off if it was on when the
	 * call is made.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isOn()}
	 * pre	{@code isLightPlanned()}
	 * pre	{@code isDeactivated()}
	 * post	{@code !isDeactivated()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			reactivate() throws Exception;
}
// -----------------------------------------------------------------------------
