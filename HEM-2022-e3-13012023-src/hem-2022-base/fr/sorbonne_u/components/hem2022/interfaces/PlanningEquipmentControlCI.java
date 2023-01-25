package fr.sorbonne_u.components.hem2022.interfaces;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide an
// example of a cyber-physical system.
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

// -----------------------------------------------------------------------------
/**
 * The component interface <code>PlanningEquipmentControlCI</code> defines the
 * operations that a controller can perform on an equipment which can have
 * planned programs.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code !hasPlan() || (deadline().compareTo(startTime().plus(duration())}
 * </pre>
 * 
 * <p>Created on : 2021-09-09</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		PlanningEquipmentControlCI
extends		StandardEquipmentControlCI
{
	/**
	 * return true if the equipment currently has a planned program.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the equipment currently has a planned program.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		hasPlan() throws Exception;

	/**
	 * return true if the plan has begun and is still running.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				true if the plan has begun and is still running.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		isPlanRunning() throws Exception;

	/**
	 * return the time at which the current planned program will start or null
	 * if none has been planned.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hasPlan()}
	 * post	{@code return != null && LocalTime.now().compareTo(return) <= 0}
	 * </pre>
	 *
	 * @return				the time at which the current planned program will start.
	 * @throws Exception	<i>to do</i>.
	 */
	public Instant	startTime() throws Exception;

	/**
	 * return the time at which the current planned program will start or null
	 * if none has been planned.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hasPlan()}
	 * post	{@code return != null && !return.isZero() && !return.isNegative()}
	 * </pre>
	 *
	 * @return				the duration of the current planned program.
	 * @throws Exception	<i>to do</i>.
	 */
	public Duration		duration() throws Exception;

	/**
	 * return the time at which the current planned program must finish or null
	 * if none has been planned.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hasPlan()}
	 * post	{@code return != null && LocalTime.now().compareTo(return) <= 0}
	 * </pre>
	 *
	 * @return				the time at which the current planned program must finish.
	 * @throws Exception	<i>to do</i>.
	 */
	public Instant	deadline() throws Exception;

	/**
	 * postpone (resp. advance if {@code d.isNegative()}) the start time of the
	 * planned program by the given duration, returning true if the operation
	 * succeeded or false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hasPlan()}
	 * pre	{@code !d.isZero()}
	 * post	{@code !return || deadline().compareTo(deadline()@pre.plus(d)) == 0}
	 * </pre>
	 *
	 * @param d	duration by which the program must be postponed (or advance if negative).
	 * @return	true if the operation succeeded or false otherwise.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		postpone(Duration d) throws Exception;

	/**
	 * cancel the planned program, returning true if the operation succeeded
	 * or false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hasPlan()}
	 * post	{@code !return || !hasPlan()}
	 * </pre>
	 *
	 * @return	true if the operation succeeded or false otherwise.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		cancel() throws Exception;
}
// -----------------------------------------------------------------------------
