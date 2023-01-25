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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;

// -----------------------------------------------------------------------------
/**
 * The class <code>ThermostatedHeater</code> a thermostated heater component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code targetTemperature >= -50.0 && targetTemperature <= 50.0}
 * </pre>
 * 
 * <p>Created on : 2021-09-10</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@OfferedInterfaces(offered={HeaterCI.class})
public class			ThermostatedHeater
extends		AbstractComponent
implements	HeaterImplementationI
{
	// -------------------------------------------------------------------------
	// Inner interfaces and types
	// -------------------------------------------------------------------------

	/**
	 * The enumeration <code>HeaterState</code> describes the operation
	 * states of the heater.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Created on : 2021-09-10</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	protected static enum	HeaterState
	{
		/** heater is on.													*/
		ON,
		/** heater is off.													*/
		OFF
	}

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** URI of the hair dryer inbound port used in tests.					*/
	public static final String		INBOUND_PORT_URI =
												"HEATER-INBOUND-PORT-URI";
	/** when true, methods trace their actions.								*/
	public static final boolean		VERBOSE = true;
	/** fake current 	*/
	public static final double		FAKE_CURRENT_TEMPERATURE = 10.0;

	/** current state (on, off) of the heater.								*/
	protected HeaterState		currentState;
	/** inbound port offering the <code>HeaterCI</code> interface.			*/
	protected HeaterInboundPort	hip;
	/** target temperature for the heating.	*/
	protected double			targetTemperature;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a new thermostated heater.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code INBOUND_PORT_URI != null}
	 * pre	{@code !INBOUND_PORT_URI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 * 
	 * @throws Exception <i>to do </i>.
	 */
	protected			ThermostatedHeater() throws Exception
	{
		this(INBOUND_PORT_URI);
	}

	/**
	 * create a new thermostated heater.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code heaterInboundPortURI != null}
	 * pre	{@code !heaterInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 * 
	 * @param heaterInboundPortURI	URI of the inbound port to call the heater component.
	 * @throws Exception			<i>to do </i>.
	 */
	protected			ThermostatedHeater(
		String heaterInboundPortURI
		) throws Exception
	{
		super(1, 0);
		this.initialise(heaterInboundPortURI);
	}

	/**
	 * create a new thermostated heater.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code reflectionInboundPortURI != null}
	 * pre	{@code !reflectionInboundPortURI.isEmpty()}
	 * pre	{@code heaterInboundPortURI != null}
	 * pre	{@code !heaterInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 * 
	 * @param reflectionInboundPortURI	URI of the reflection inbound port of the component.
	 * @param heaterInboundPortURI		URI of the inbound port to call the heater component.
	 * @throws Exception				<i>to do </i>.
	 */
	protected			ThermostatedHeater(
		String reflectionInboundPortURI,
		String heaterInboundPortURI
		) throws Exception
	{
		super(reflectionInboundPortURI, 1, 0);
		this.initialise(heaterInboundPortURI);
	}

	/**
	 * create a new thermostated heater.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code heaterInboundPortURI != null}
	 * pre	{@code !heaterInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param heaterInboundPortURI	URI of the inbound port to call the heater component.
	 * @throws Exception			<i>to do </i>.
	 */
	protected void		initialise(String heaterInboundPortURI) throws Exception
	{
		assert	heaterInboundPortURI != null;
		assert	!heaterInboundPortURI.isEmpty();

		this.currentState = HeaterState.OFF;
		this.targetTemperature = 20.0;
		this.hip = new HeaterInboundPort(heaterInboundPortURI, this);
		this.hip.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Thermostated heater component");
			this.tracer.get().setRelativePosition(1, 1);
			this.toggleTracing();		
		}
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.hip.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Internal methods
	// -------------------------------------------------------------------------

	/**
	 * return true if the heater is running.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the heater is running.
	 */
	public boolean		internalIsRunning()
	{
		return this.currentState == HeaterState.ON;
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.heater.HeaterImplementationI#isRunning()
	 */
	@Override
	public boolean		isRunning() throws Exception
	{
		if (ThermostatedHeater.VERBOSE) {
			this.traceMessage("Thermostated heater returns its state: " +
											this.currentState + ".\n");
		}
		return this.internalIsRunning();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.heater.HeaterImplementationI#startHeater()
	 */
	@Override
	public void			startHeater() throws Exception
	{
		if (ThermostatedHeater.VERBOSE) {
			this.traceMessage("Thermostated heater starts.\n");
		}
		assert	!this.internalIsRunning();

		this.currentState = HeaterState.ON;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.heater.HeaterImplementationI#stopHeater()
	 */
	@Override
	public void			stopHeater() throws Exception
	{
		if (ThermostatedHeater.VERBOSE) {
			this.traceMessage("Thermostated heater stops.\n");
		}
		assert	this.internalIsRunning();

		this.currentState = HeaterState.OFF;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.heater.HeaterImplementationI#setTargetTemperature(double)
	 */
	@Override
	public void			setTargetTemperature(double target) throws Exception
	{
		if (ThermostatedHeater.VERBOSE) {
			this.traceMessage("Thermostated heater sets a new target "
										+ "temperature: " + target + ".\n");
		}

		assert	this.internalIsRunning();
		assert	target >= -50.0 && target <= 50.0;

		this.targetTemperature = target;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.heater.HeaterImplementationI#getTargetTemperature()
	 */
	@Override
	public double		getTargetTemperature() throws Exception
	{
		if (ThermostatedHeater.VERBOSE) {
			this.traceMessage("Thermostated heater returns its target"
							+ " temperature " + this.targetTemperature + ".\n");
		}

		return this.targetTemperature;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.heater.HeaterImplementationI#getCurrentTemperature()
	 */
	@Override
	public double		getCurrentTemperature() throws Exception
	{
		// Temporary implementation; would need a temperature sensor.
		double currentTemperature = FAKE_CURRENT_TEMPERATURE;
		if (ThermostatedHeater.VERBOSE) {
			this.traceMessage("Thermostated heater returns the current"
							+ " temperature " + currentTemperature + ".\n");
		}

		return  currentTemperature;
	}
}
// -----------------------------------------------------------------------------
