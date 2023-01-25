/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.refrigerator;

import fr.sorbonne_u.components.hem2022e3.equipments.heater.HeaterSensorCI.SensorDataI;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.Refrigerator.RefrigeratorState;
import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;

/**
 * The class <code>RefrigeratorSensorCI</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-03</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public interface RefrigeratorSensorCI 
extends DataRequiredCI, 
		DataOfferedCI 
{
	// -------------------------------------------------------------------------
	// Inner interfaces and types
	// -------------------------------------------------------------------------

	/**
	 * The interface <code>TemperatureDataI</code> declares the methods to
	 * access the target and the current temperatures as sensed by the
	 * refrigerator.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Black-box Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code true}	// no more invariant
	 * </pre>
	 * 
	 */
	public interface	TemperatureDataI
	extends		SensorDataI
	{
		/**
		 * return the current target temperature.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code true}	// no precondition.
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @return	the current target temperature.
		 */
		public double	getTargetFreezingTemperature();
		
		public double	getTargetRefrigerationTemperature();

		/**
		 * return the current temperature.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code true}	// no precondition.
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @return	the current temperature.
		 */
		public double	getCurrentFreezingTemperature();
		
		public double	getCurrentRefrigerationTemperature();
	}

	/**
	 * The interface <code>BooleanDataI</code> declares the methods to access
	 * a boolean value in a boolean sensor data.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Black-box Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code getValue() != null}
	 * </pre>
	 */
	public static interface	StateDataI
	extends		SensorDataI
	{
		/**
		 * return the value held in the state sensor data object.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code true}	// no precondition.
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @return	the value held in the state sensor data object.
		 */
		public RefrigeratorState	getState();
	}
}
