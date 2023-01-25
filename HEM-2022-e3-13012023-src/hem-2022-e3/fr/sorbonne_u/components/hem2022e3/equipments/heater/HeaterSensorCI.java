package fr.sorbonne_u.components.hem2022e3.equipments.heater;

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

import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.ThermostatedHeater.HeaterState;
import java.time.Instant;

// -----------------------------------------------------------------------------
/**
 * The component interface <code>HeaterSensorCI</code> declares the interfaces
 * that sensor data exchanged through it must implement.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This component interface extends BCM4Java data interfaces. These interfaces
 * are meant to implement more simply the communication of data among two
 * components. The other interesting point about data interfaces is that they
 * allow two way communication <i>i.e.</i>, data can be either pushed by the
 * emitter towards the receiver or the receiver can pull the data from the
 * emitter. In the case of the heater components, the push mode will fit the
 * requirement for notifying the controller when the heater is switched on and
 * off, so that the controller can begin or stop its closed-loop control of the
 * temperature and the heating. On the other hand, when the controller needs
 * the current and target temperatures, it will use the pull mode.
 * </p>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2022-10-27</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		HeaterSensorCI
extends		DataRequiredCI,
			DataOfferedCI
{
	// -------------------------------------------------------------------------
	// Inner interfaces and types
	// -------------------------------------------------------------------------

	/**
	 * The interface <code>SensorDataI</code> declares the methods common
	 * to all sensor data exchanged between the controlled and the controller
	 * components.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * Sensor data should always be tagged with their time of measurement.
	 * Here, the time will not be of use but this defends the idea in general.
	 * </p>
	 * 
	 * <p><strong>Black-box Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code getTime() != null}
	 * </pre>
	 * 
	 * <p>Created on : 2022-10-28</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static interface	SensorDataI
	extends		DataRequiredCI.DataI,
				DataOfferedCI.DataI
	{
		/**
		 * return	the time at which the data has been measured.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code true}	// no precondition.
		 * post	{@code ret != null}
		 * </pre>
		 *
		 * @return	the time instant at which the data has been measured.
		 */
		public Instant	getTime();
	}

	/**
	 * The interface <code>TemperatureDataI</code> declares the methods to
	 * access the target and the current temperatures as sensed by the
	 * heater.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Black-box Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code true}	// no more invariant
	 * </pre>
	 * 
	 * <p>Created on : 2022-10-27</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
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
		public double	getTarget();

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
		public double	getCurrent();
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
	 * 
	 * <p>Created on : 2022-10-28</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
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
		public HeaterState	getState();
	}
}
// -----------------------------------------------------------------------------
