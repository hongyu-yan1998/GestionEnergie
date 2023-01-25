package fr.sorbonne_u.components.hem2022e1.equipments.hairdryer;

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
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>HairDryer</code> implements the hair dryer component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The hair dryer is an uncontrollable appliance, hence it does not connect
 * with the household energy manager. However, it will connect later to the
 * electric panel to take its (simulated) electricity consumption into account.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code currentState != null}
 * invariant	{@code currentMode != null}
 * </pre>
 * 
 * <p>Created on : 2021-09-09</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@OfferedInterfaces(offered={HairDryerCI.class})
public class			HairDryer
extends		AbstractComponent
implements	HairDryerImplementationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** URI of the hair dryer inbound port used in tests.					*/
	public static final String			INBOUND_PORT_URI =
												"HAIR-DRYER-INBOUND-PORT-URI";
	/** when true, methods trace their actions.								*/
	public static final boolean			VERBOSE = true;
	public static final HairDryerState	INITIAL_STATE = HairDryerState.OFF;
	public static final HairDryerMode	INITIAL_MODE = HairDryerMode.LOW;

	/** current state (on, off) of the hair dryer.							*/
	protected HairDryerState		currentState;
	/** current mode of operation (low, high) of the hair dryer.			*/
	protected HairDryerMode			currentMode;

	/** inbound port offering the <code>HairDryerCI</code> interface.		*/
	protected HairDryerInboundPort	hdip;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a hair dryer component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code INBOUND_PORT_URI != null}
	 * pre	{@code !INBOUND_PORT_URI.isEmpty()}
	 * post	{@code getState() == HairDryerState.OFF}
	 * post	{@code getMode() == HairDryerMode.LOW}
	 * </pre>
	 * 
	 * @throws Exception	<i>to do</i>.
	 */
	protected			HairDryer()
	throws Exception
	{
		super(1, 0);
		this.initialise(INBOUND_PORT_URI);
	}

	/**
	 * create a hair dryer component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hairDryerInboundPortURI != null}
	 * pre	{@code !hairDryerInboundPortURI.isEmpty()}
	 * post	{@code getState() == HairDryerState.OFF}
	 * post	{@code getMode() == HairDryerMode.LOW}
	 * </pre>
	 * 
	 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			HairDryer(String hairDryerInboundPortURI)
	throws Exception
	{
		super(1, 0);
		this.initialise(hairDryerInboundPortURI);
	}

	/**
	 * create a hair dryer component with the given reflection innbound port
	 * URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hairDryerInboundPortURI != null}
	 * pre	{@code !hairDryerInboundPortURI.isEmpty()}
	 * pre	{@code reflectionInboundPortURI != null}
	 * post	{@code getState() == HairDryerState.OFF}
	 * post	{@code getMode() == HairDryerMode.LOW}
	 * </pre>
	 *
	 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port.
	 * @param reflectionInboundPortURI	URI of the reflection innbound port of the component.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			HairDryer(
		String hairDryerInboundPortURI,
		String reflectionInboundPortURI) throws Exception
	{
		super(reflectionInboundPortURI, 1, 0);
		this.initialise(hairDryerInboundPortURI);
	}

	/**
	 * initialise the hair dryer component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hairDryerInboundPortURI != null}
	 * pre	{@code !hairDryerInboundPortURI.isEmpty()}
	 * post	{@code getState() == HairDryerState.OFF}
	 * post	{@code getMode() == HairDryerMode.LOW}
	 * </pre>
	 * 
	 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port.
	 * @throws Exception				<i>to do</i>.
	 */
	protected void		initialise(String hairDryerInboundPortURI)
	throws Exception
	{
		assert	hairDryerInboundPortURI != null :
					new PreconditionException(
										"hairDryerInboundPortURI != null");
		assert	!hairDryerInboundPortURI.isEmpty() :
					new PreconditionException(
										"!hairDryerInboundPortURI.isEmpty()");

		this.currentState = INITIAL_STATE;
		this.currentMode = INITIAL_MODE;
		this.hdip = new HairDryerInboundPort(hairDryerInboundPortURI, this);
		this.hdip.publishPort();

		if (HairDryer.VERBOSE) {
			this.tracer.get().setTitle("Hair dryer component");
			this.tracer.get().setRelativePosition(1, 0);
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
			this.hdip.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerImplementationI#getState()
	 */
	@Override
	public HairDryerState	getState() throws Exception
	{
		if (HairDryer.VERBOSE) {
			this.traceMessage("Hair dryer returns its state : " +
													this.currentState + ".\n");
		}

		return this.currentState;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerImplementationI#getMode()
	 */
	@Override
	public HairDryerMode	getMode() throws Exception
	{
		if (HairDryer.VERBOSE) {
			this.traceMessage("Hair dryer returns its mode : " +
													this.currentMode + ".\n");
		}

		return this.currentMode;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerImplementationI#turnOn()
	 */
	@Override
	public void			turnOn() throws Exception
	{
		if (HairDryer.VERBOSE) {
			this.traceMessage("Hair dryer is turned on.\n");
		}

		assert	this.getState() == HairDryerState.OFF :
				new PreconditionException("getState() == HairDryerState.OFF");

		this.currentState = HairDryerState.ON;
		this.currentMode = HairDryerMode.LOW;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerImplementationI#turnOff()
	 */
	@Override
	public void			turnOff() throws Exception
	{
		if (HairDryer.VERBOSE) {
			this.traceMessage("Hair dryer is turned off.\n");
		}

		assert	this.getState() == HairDryerState.ON :
				new PreconditionException("getState() == HairDryerState.ON");

		this.currentState = HairDryerState.OFF;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerImplementationI#setHigh()
	 */
	@Override
	public void			setHigh() throws Exception
	{
		if (HairDryer.VERBOSE) {
			this.traceMessage("Hair dryer is set high.\n");
		}

		assert	this.getState() == HairDryerState.ON :
				new PreconditionException("getState() == HairDryerState.ON");
		assert	this.getMode() == HairDryerMode.LOW :
				new PreconditionException("getMode() == HairDryerMode.LOW");

		this.currentMode = HairDryerMode.HIGH;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerImplementationI#setLow()
	 */
	@Override
	public void			setLow() throws Exception
	{
		if (HairDryer.VERBOSE) {
			this.traceMessage("Hair dryer is set low.\n");
		}

		assert	this.getState() == HairDryerState.ON :
				new PreconditionException("getState() == HairDryerState.ON");
		assert	this.getMode() == HairDryerMode.HIGH :
				new PreconditionException("getMode() == HairDryerMode.HIGH");

		this.currentMode = HairDryerMode.LOW;
	}
}
// -----------------------------------------------------------------------------
