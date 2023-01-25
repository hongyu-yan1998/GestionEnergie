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

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.hem2022.interfaces.SuspensionEquipmentControlCI;
import fr.sorbonne_u.components.hem2022e1.equipments.heater.HeaterCI;

// -----------------------------------------------------------------------------
/**
 * The class <code>HeaterConnector</code> manually implements a connector
 * bridging the gap between the given generic component interface
 * {@code SuspensionEquipmentControlCI}and the actual component interface
 * offered by the thermostated heater component ({@code HeaterCI}).
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The code given here illustrates how a connector can be used to implement
 * a required interface given some offered interface that is different.
 * The objective is to be able to automatically generate such a connector
 * at run-time from an XML descriptor of the required adjustments.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2021-09-13</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			HeaterConnector
extends		AbstractConnector
implements	SuspensionEquipmentControlCI
{
	/** the minimum admissible temperature from which the heater should
	 *  be resumed in priority after being suspended to save energy.		*/
	protected static final double	MIN_ADMISSIBLE_TEMP = 12.0;
	/** the maximal admissible difference between the target and the
	 *  current temperature from which the heater should be resumed in
	 *  priority after being suspended to save energy.						*/
	protected static final double	MAX_ADMISSIBLE_DELTA = 10.0;
	/** true if the heater has been suspended, false otherwise.				*/
	protected boolean	isSuspended;

	public				HeaterConnector()
	{
		super();
		this.isSuspended = false;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI#on()
	 */
	@Override
	public boolean		on() throws Exception
	{
		return this.isSuspended || ((HeaterCI)this.offering).isRunning();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI#switchOn()
	 */
	@Override
	public boolean		switchOn() throws Exception
	{
		((HeaterCI)this.offering).startHeater();
		return true;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI#switchOff()
	 */
	@Override
	public boolean		switchOff() throws Exception
	{
		((HeaterCI)this.offering).stopHeater();
		return true;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI#maxMode()
	 */
	@Override
	public int			maxMode() throws Exception
	{
		// Only one mode in heater, so 1 becomes the sole "mode".
		return 1;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI#upMode()
	 */
	@Override
	public boolean		upMode() throws Exception
	{
		return false;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI#downMode()
	 */
	@Override
	public boolean		downMode() throws Exception
	{
		return false;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI#setMode(int)
	 */
	@Override
	public boolean		setMode(int modeIndex) throws Exception
	{
		return true;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.StandardEquipmentControlCI#currentMode()
	 */
	@Override
	public int			currentMode() throws Exception
	{
		// Only one mode in heater, so 1 becomes the sole "mode".
		return 1;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.SuspensionEquipmentControlCI#suspended()
	 */
	@Override
	public boolean		suspended() throws Exception
	{
		return this.isSuspended;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.SuspensionEquipmentControlCI#suspend()
	 */
	@Override
	public boolean		suspend() throws Exception
	{
		((HeaterCI)this.offering).stopHeater();
		this.isSuspended = true;
		return true;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.SuspensionEquipmentControlCI#resume()
	 */
	@Override
	public boolean		resume() throws Exception
	{
		((HeaterCI)this.offering).startHeater();
		this.isSuspended = false;
		return true;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022.interfaces.SuspensionEquipmentControlCI#emergency()
	 */
	@Override
	public double		emergency() throws Exception
	{
		double currentTemperature =
					((HeaterCI)this.offering).getCurrentTemperature();
		double targetTemperature =
					((HeaterCI)this.offering).getTargetTemperature();
		double delta = Math.abs(targetTemperature - currentTemperature);
		if (currentTemperature < HeaterConnector.MIN_ADMISSIBLE_TEMP ||
							delta >= HeaterConnector.MAX_ADMISSIBLE_DELTA) {
			return 1.0;
		} else {
			return delta/HeaterConnector.MAX_ADMISSIBLE_DELTA;
		}
	}
}
// -----------------------------------------------------------------------------
