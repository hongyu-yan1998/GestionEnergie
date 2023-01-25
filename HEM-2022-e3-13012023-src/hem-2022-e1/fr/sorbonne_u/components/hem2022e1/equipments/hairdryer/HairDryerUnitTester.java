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
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerImplementationI.HairDryerMode;
import fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerImplementationI.HairDryerState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.concurrent.ExecutionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>HairDryerUnitTester</code> implements a component performing
 * unit tests for the class <code>HairDryer</code> as a BCM component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no invariant
 * </pre>
 * 
 * <p>Created on : 2021-09-09</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@RequiredInterfaces(required= {HairDryerCI.class})
public class			HairDryerUnitTester
extends		AbstractComponent
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	protected HairDryerOutboundPort	hdop;
	protected String				hairDryerInboundPortURI;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	protected			HairDryerUnitTester() throws Exception
	{
		this(HairDryer.INBOUND_PORT_URI);
	}

	protected			HairDryerUnitTester(String hairDryerInboundPortURI)
	throws Exception
	{
		super(1, 0);

		this.initialise(hairDryerInboundPortURI);
	}

	protected			HairDryerUnitTester(
		String hairDryerInboundPortURI,
		String reflectionInboundPortURI
		) throws Exception
	{
		super(reflectionInboundPortURI, 1, 0);

		this.initialise(hairDryerInboundPortURI);
	}

	protected void		initialise(String hairDryerInboundPortURI)
	throws Exception
	{
		this.hairDryerInboundPortURI = hairDryerInboundPortURI;
		this.hdop = new HairDryerOutboundPort(this);
		this.hdop.publishPort();

		this.tracer.get().setTitle("Hair dryer tester component");
		this.tracer.get().setRelativePosition(0, 0);
		this.toggleTracing();		
	}

	// -------------------------------------------------------------------------
	// Component internal methods
	// -------------------------------------------------------------------------

	public void			testGetState()
	{
		this.logMessage("testGetState()... ");
		try {
			assertEquals(HairDryerState.OFF, this.hdop.getState());
		} catch (Exception e) {
			this.logMessage("...KO.");
			assertTrue(false);
		}
		this.logMessage("...done.");
	}

	public void			testGetMode()
	{
		this.logMessage("testGetMode()... ");
		try {
			assertEquals(HairDryerMode.LOW, this.hdop.getMode());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	}

	public void			testTurnOnOff()
	{
		this.logMessage("testTurnOnOff()... ");
		try {
			this.hdop.turnOn();
			assertEquals(HairDryerState.ON, this.hdop.getState());
			assertEquals(HairDryerMode.LOW, this.hdop.getMode());
		} catch (Exception e) {
			assertTrue(false);
		}
		try {
			assertThrows(ExecutionException.class,
						 () -> this.hdop.turnOn());
		} catch (Exception e) {
			assertTrue(false);
		}
		try {
			this.hdop.turnOff();
			assertEquals(HairDryerState.OFF, this.hdop.getState());
		} catch (Exception e) {
			assertTrue(false);
		}
		try {
			assertThrows(ExecutionException.class,
						 () -> this.hdop.turnOff());
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	}

	public void			testSetLowHigh()
	{
		this.logMessage("testSetLowHigh()... ");
		try {
			this.hdop.turnOn();
			this.hdop.setHigh();
			assertEquals(HairDryerState.ON, this.hdop.getState());
			assertEquals(HairDryerMode.HIGH, this.hdop.getMode());
		} catch (Exception e) {
			assertTrue(false);
		}
		try {
			assertThrows(ExecutionException.class,
						 () -> this.hdop.setHigh());
		} catch (Exception e) {
			assertTrue(false);
		}
		try {
			this.hdop.setLow();
			assertEquals(HairDryerState.ON, this.hdop.getState());
			assertEquals(HairDryerMode.LOW, this.hdop.getMode());
		} catch (Exception e) {
			assertTrue(false);
		}
		try {
			assertThrows(ExecutionException.class,
						 () -> this.hdop.setLow());
		} catch (Exception e) {
			assertTrue(false);
		}
		try {
			this.hdop.turnOff();
		} catch (Exception e) {
			assertTrue(false);
		}
		this.logMessage("...done.");
	}

	protected void			runAllTests()
	{
		this.testGetState();
		this.testGetMode();
		this.testTurnOnOff();
		this.testSetLowHigh();
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public synchronized void	start()
	throws ComponentStartException
	{
		super.start();

		try {
			this.doPortConnection(
							this.hdop.getPortURI(),
							hairDryerInboundPortURI,
							HairDryerConnector.class.getCanonicalName());
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public synchronized void execute() throws Exception
	{
		this.runAllTests();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void	finalise() throws Exception
	{
		this.doPortDisconnection(this.hdop.getPortURI());
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.hdop.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
}
// -----------------------------------------------------------------------------
