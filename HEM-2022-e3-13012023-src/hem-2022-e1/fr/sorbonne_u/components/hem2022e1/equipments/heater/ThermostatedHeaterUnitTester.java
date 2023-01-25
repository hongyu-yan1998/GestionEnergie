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
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// -----------------------------------------------------------------------------
/**
 * The class <code>ThermostatedHeaterUnitTester</code> implements a component
 * performing unit tests for the class <code>ThermostatedHeater</code> as a
 * BCM component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no invariant
 * </pre>
 * 
 * <p>Created on : 2021-09-13</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@RequiredInterfaces(required={HeaterCI.class})
public class			ThermostatedHeaterUnitTester
extends		AbstractComponent
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	protected String				heaterInboundPortURI;
	protected HeaterOutboundPort	hop;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	protected			ThermostatedHeaterUnitTester() throws Exception
	{
		this(ThermostatedHeater.INBOUND_PORT_URI);
	}

	protected			ThermostatedHeaterUnitTester(
		String heaterInboundPortURI
		) throws Exception
	{
		super(1, 0);
		this.initialise(heaterInboundPortURI);
	}

	protected			ThermostatedHeaterUnitTester(
		String reflectionInboundPortURI,
		String heaterInboundPortURI
		) throws Exception
	{
		super(reflectionInboundPortURI, 1, 0);
		this.initialise(heaterInboundPortURI);
	}

	protected void		initialise(String heaterInboundPortURI) throws Exception
	{
		this.heaterInboundPortURI = heaterInboundPortURI;
		this.hop = new HeaterOutboundPort(this);
		this.hop.publishPort();

		this.tracer.get().setTitle("Heater tester component");
		this.tracer.get().setRelativePosition(0, 1);
		this.toggleTracing();		
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	protected void		testIsRunning()
	{
		this.traceMessage("testIsRunning()...\n");
		try {
			assertEquals(false, this.hop.isRunning());
		} catch (Exception e) {
			this.traceMessage("...KO.\n" + e);
			assertTrue(false);
		}
		this.traceMessage("...done.\n");
	}

	protected void		testStartStopHeater()
	{
		this.traceMessage("testStartStopHeater()...\n");
		try {
			assertEquals(false, this.hop.isRunning());
			this.hop.startHeater();
			assertEquals(true, this.hop.isRunning());
			this.hop.stopHeater();
			assertEquals(false, this.hop.isRunning());
		} catch (Exception e) {
			this.traceMessage("...KO.\n");
			assertTrue(false);
		}
		this.traceMessage("...done.\n");
	}

	protected void		testSetGetTargetTemperature()
	{
		this.traceMessage("testSetGetTargetTemperature()...\n");
		try {
			assertEquals(false, this.hop.isRunning());
			this.hop.startHeater();
			assertEquals(true, this.hop.isRunning());
			double target = 22.0;
			this.hop.setTargetTemperature(target);
			this.hop.stopHeater();
			assertEquals(false, this.hop.isRunning());
			assertEquals(target, this.hop.getTargetTemperature());
		} catch (Exception e) {
			this.traceMessage("...KO.\n");
			assertTrue(false);
		}
		this.traceMessage("...done.\n");
	}

	protected void		testGetCurrentTemperature()
	{
		this.traceMessage("testGetCurrentTemperature()...\n");
		try {
			this.traceMessage("current temperature = " +
									this.hop.getCurrentTemperature() + "\n");
		} catch (Exception e) {
			this.traceMessage("...KO.\n");
			assertTrue(false);
		}
		this.traceMessage("...done.\n");
	}

	protected void		runnAllTests()
	{
		this.testIsRunning();
		this.testStartStopHeater();
		this.testSetGetTargetTemperature();
		this.testGetCurrentTemperature();
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public synchronized void	start() throws ComponentStartException
	{
		super.start();

		try {
			this.doPortConnection(
					this.hop.getPortURI(),
					this.heaterInboundPortURI,
					HeaterConnector.class.getCanonicalName());
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public synchronized void	execute() throws Exception
	{
		this.runnAllTests();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void	finalise() throws Exception
	{
		this.doPortDisconnection(this.hop.getPortURI());
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.hop.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
}
// -----------------------------------------------------------------------------
