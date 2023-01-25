package fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.test;

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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.IndoorGardenCI;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.connections.IndoorGardenConnector;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.connections.IndoorGardenOutboundPort;
import fr.sorbonne_u.components.cyphy.tools.aclocks.AcceleratedClock;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServer;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerCI;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerConnector;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerOutboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;

// -----------------------------------------------------------------------------
/**
 * The class <code>IndoorGardenTester</code> implements a component
 * dedicated to the testing of the component {@code IndoorGarden}.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
 * invariant	{@code clockURI != null && !clockURI.isEmpty()}
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2022-09-19</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@RequiredInterfaces(required={IndoorGardenCI.class, ClockServerCI.class})
//-----------------------------------------------------------------------------
public class			IndoorGardenTester
extends		AbstractComponent
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	protected static boolean			VERBOSE = true;

	/** URI of the inbound port of the {@code IndoorGarden} component.		*/
	protected final String				inboundPortURI;
	/** outbound port used to connect to the {@code IndoorGarden} component.*/
	protected IndoorGardenOutboundPort	outboundPort;

	/** when true, the component executes in unit test mode.				*/
	protected final boolean				isUnitTesting;
	/** outbound port to connect to the centralised clock server.			*/
	protected ClockServerOutboundPort	clockServerOBP;
	/** URI of the clock to be used to synchronise the test scenarios and
	 *  the simulation.														*/
	protected final String				clockURI;
	/** accelerated clock governing the timing of actions in the test
	 *  scenarios.															*/
	protected AcceleratedClock			clock;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create the component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * pre	{@code clockURI != null && !clockURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param inboundPortURI	URI of the inbound port of the {@code IndoorGarden} component.
	 * @param isUnitTesting		when true, the component executes in unit test mode.
	 * @param clockURI			URI of the clock to be used to synchronise the test scenarios and the simulation.
	 */
	protected			IndoorGardenTester(
		String inboundPortURI,
		boolean isUnitTesting,
		String clockURI
		)
	{
		super(1, 1);

		assert	inboundPortURI != null && !inboundPortURI.isEmpty() :
				new PreconditionException(
						"inboundPortURI != null && !inboundPortURI.isEmpty()");
		assert	clockURI != null && !clockURI.isEmpty() :
				new PreconditionException(
						"clockURI != null && !clockURI.isEmpty()");

		this.inboundPortURI = inboundPortURI;
		this.isUnitTesting = isUnitTesting;
		this.clockURI = clockURI;

		if (VERBOSE) {
			this.tracer.get().setTitle("Indoor garden unit tester component");
			this.tracer.get().setRelativePosition(1, 3);
			this.toggleTracing();
		}
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
			this.outboundPort = new IndoorGardenOutboundPort(this);
			this.outboundPort.publishPort();
			this.doPortConnection(
					this.outboundPort.getPortURI(),
					inboundPortURI,
					IndoorGardenConnector.class.getCanonicalName());
			this.clockServerOBP = new ClockServerOutboundPort(this);
			this.clockServerOBP.publishPort();
			this.doPortConnection(
					this.clockServerOBP.getPortURI(),
					ClockServer.STANDARD_INBOUNDPORT_URI,
					ClockServerConnector.class.getCanonicalName());
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
		this.traceMessage("start.\n");
	}

	/**
	 * when unit testing, execute the {@code unitTest()} scenario, otherwise
	 * perform a standard usage scenario that simply switches on the indoor
	 * garden, plans the light on and off from 06:00 to 22:00 and then switches
	 * off the indoor garden at 23:00.
	 * 
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		// get the centralised clock from the clock server.
		this.clock = this.clockServerOBP.getClock(this.clockURI);
		// wait for the Unix epoch start time to execute the actions
		Thread.sleep(this.clock.waitUntilStartInMillis());
		
		if (this.isUnitTesting) {
			// perform the unit test scenario
			this.unitTest();
		} else {
			// perform a standard usage scenario
			final IndoorGardenOutboundPort ig = this.outboundPort;
			final IndoorGardenTester tester = this;

			// get the base instant to plan the actions
			Instant i0 = clock.getStartInstant();
			// actions are done at 04:00
			Instant i1 = i0.plusSeconds(4 * 3600);				// 4:00:00
			final Instant switchOnInstant =
									i0.plusSeconds(6 * 3600);	// 06:00:00.00
			final Instant switchOffInstant =
									i0.plusSeconds(22 * 3600);	// 22:00:00.00
			// use the clock to compute the Unix epoch time delay until the
			// actions must be performed
			long d1 = clock.delayToAcceleratedInstantInNanos(i1);
			// schedule the task performing the actions after the computed delay
			this.scheduleTask(
					o -> {	try {
								ig.on();
								tester.traceMessage("on() at " + i1 + ".\n");
								ig.plan(switchOnInstant, switchOffInstant);
								tester.traceMessage(
										"planLight() at " + i1 + ".\n");
							} catch(Exception e) {
								e.printStackTrace();
							}
						 },
					d1,		// delay before the task executes
					TimeUnit.NANOSECONDS);
			// switching off must occur at 23:00
			Instant i2 = i0.plusSeconds(23 * 3600);				// 23:00:00
			// use the clock to compute the Unix epoch time delay until the
			// actions must be performed
			long d2 = clock.delayToAcceleratedInstantInNanos(i2);
			// schedule the task performing the actions after the computed delay
			this.scheduleTask(
					o -> {	try {
								ig.off();
								tester.traceMessage("off() at " + i2 + ".\n");
							} catch(Exception e) {
								e.printStackTrace();
							}
						 },
					d2,		// delay before the task executes
					TimeUnit.NANOSECONDS);
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void	finalise() throws Exception
	{
		this.doPortDisconnection(this.outboundPort.getPortURI());
		this.doPortDisconnection(this.clockServerOBP.getPortURI());
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		this.traceMessage("shutdown.\n");
		try {
			this.outboundPort.unpublishPort();
			this.clockServerOBP.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();		
	}

	// -------------------------------------------------------------------------
	// Component internal methods
	// -------------------------------------------------------------------------

	/**
	 * when unit testing, execute the following test scenario.
	 * 
	 * <p><strong>Test scenario</strong></p>
	 * 
	 * <pre>
	 * ------------------------------------------------------------------------------------------------
	 * Time          | Action         | Description
	 * ------------------------------------------------------------------------------------------------
	 * i1 = 04:00:00 | on             | test isOn, on, off
	 * i2 = 04:30:00 | planLight      | also test isLightPlanned, isLightOn, cancelPlan
	 * i3 = 05:00:00 | planLight      | also test getPlannedLightOn, getPlannedLightOff
	 * 06:00:00      | switchLightOn  | planned switch light on
	 * i4 = 07:00:00 | deactivate     | also test isLightPlanned, deactivate, reactivate, isDeactivated
	 * 06:00:00      | switchLightOff | planned switch light of
	 * i5 = 23:00:00 | off            | test the end of the plan, isLightOn
	 * ------------------------------------------------------------------------------------------------
	 * </pre>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	protected void		unitTest() throws Exception
	{
		Instant i0 = clock.getStartInstant();
		Instant i1 = i0.plusSeconds(4 * 3600);			// 4:00:00
		Instant i2 = i0.plusSeconds(4 * 3600 + 1800);	// 4:30:00
		Instant i3 = i0.plusSeconds(5 * 3600);			// 5:00:00
		Instant i4 = i0.plusSeconds(7 * 3600);			// 7:00:00
		Instant i5 = i0.plusSeconds(23 * 3600);			// 23:00:00
		Instant i6 = i0.plusSeconds(24 * 3600);			// 24:00:00

		final IndoorGardenOutboundPort ig = this.outboundPort;
		final IndoorGardenTester tester = this;

		tester.traceMessage("Simulated unit test begins...\n");
		Assertions.assertFalse(ig.isOn());
		if (VERBOSE) tester.traceMessage("isOn false at " + i0 + ".\n");
		long d1 = clock.delayToAcceleratedInstantInNanos(i1);
		this.scheduleTask(
				o -> {	try {
							ig.on();
							tester.traceMessage("on() at " + i1 + ".\n");
							Assertions.assertTrue(ig.isOn());
							tester.traceMessage("isOn() at " + i1 + ".\n");
							ig.off();
							tester.traceMessage("off() at " + i1 + ".\n");
							Assertions.assertFalse(ig.isOn());
							tester.traceMessage("!isOn() at " + i1 + ".\n");
							ig.on();
							tester.traceMessage("isOn() at " + i1 + ".\n");
						} catch(Exception e) {
							e.printStackTrace();
						}
					 },
				d1, TimeUnit.NANOSECONDS);
		long d2 = clock.delayToAcceleratedInstantInNanos(i2);
		final Instant switchOnInstant =
							i0.plusSeconds(6 * 3600); // 06:00:00.00
		final Instant switchOffInstant =
							i0.plusSeconds(22 * 3600); // 22:00:00.00
		this.scheduleTask(
				o -> {	try {
							Assertions.assertFalse(ig.isLightPlanned());
							tester.traceMessage(
									"!isLightPlanned() at " + i2 + ".\n");
							ig.plan(switchOnInstant, switchOffInstant);
							tester.traceMessage(
									"planLight() at " + i2 + ".\n");
							Assertions.assertTrue(ig.isLightPlanned());
							tester.traceMessage(
									"isLightPlanned() at " + i2 + ".\n");
							Assertions.assertFalse(ig.isLightOn());
							tester.traceMessage(
									"!isLightOn() at " + i2 + ".\n");
							ig.cancelPlan();
							tester.traceMessage(
									"cancelPlan() at " + i2 + ".\n");
							Assertions.assertFalse(ig.isLightPlanned());
							tester.traceMessage(
									"!isLightPlanned() at " + i2 + ".\n");
						} catch(Exception e) {
							e.printStackTrace();
						}
					 },
				d2, TimeUnit.NANOSECONDS);
		long d3 = clock.delayToAcceleratedInstantInNanos(i3);
		this.scheduleTask(
				o -> {	try {
							Assertions.assertFalse(ig.isLightPlanned());
							tester.traceMessage(
									"!isLightPlanned() at " + i3 + ".\n");
							ig.plan(switchOnInstant, switchOffInstant);
							tester.traceMessage(
									"planLight() at " + i3 + ".\n");
							Assertions.assertTrue(ig.isLightPlanned());
							tester.traceMessage(
									"isLightPlanned() at " + i3 + ".\n");
							Instant i = ig.getPlannedLightOn();
							Assertions.assertEquals(switchOnInstant, i);
							tester.traceMessage(
									"getPlannedLightOn() " + i + " at "
															+ i3 + ".\n");
							i = ig.getPlannedLightOff();
							Assertions.assertEquals(switchOffInstant, i);
							tester.traceMessage(
									"getPlannedLightOff() " + i + " at "
															+ i3 + ".\n");
							Assertions.assertFalse(ig.isDeactivated());
							tester.traceMessage(
									"!isDeactivated() at " + i3 + ".\n");
							ig.deactivate();
							tester.traceMessage(
									"deactivate() at " + i3 + ".\n");
							Assertions.assertTrue(ig.isDeactivated());
							tester.traceMessage(
									"isDeactivated() at " + i3 + ".\n");
							Assertions.assertFalse(ig.isLightOn());
							tester.traceMessage(
									"!isLightOn() at " + i3 + ".\n");
							ig.reactivate();
							tester.traceMessage(
									"reactivate() at " + i3 + ".\n");
							Assertions.assertFalse(ig.isLightOn());
							tester.traceMessage(
									"!isLightOn() at " + i3 + ".\n");
						} catch(Exception e) {
							e.printStackTrace();
						}
					 },
				d3, TimeUnit.NANOSECONDS);
		long d4 = clock.delayToAcceleratedInstantInNanos(i4);
		this.scheduleTask(
				o -> {	try {
							Assertions.assertFalse(ig.isDeactivated());
							tester.traceMessage(
									"!isDeactivated() at " + i4 + ".\n");
							ig.deactivate();
							tester.traceMessage(
									"deactivate() at " + i4 + ".\n");
							Assertions.assertTrue(ig.isDeactivated());
							tester.traceMessage(
									"isDeactivated() at " + i4 + ".\n");
							Assertions.assertFalse(ig.isLightOn());
							tester.traceMessage(
									"!isLightOn() at " + i4 + ".\n");
							ig.reactivate();
							tester.traceMessage(
									"reactivate() at " + i4 + ".\n");
							Assertions.assertFalse(ig.isDeactivated());
							tester.traceMessage(
									"!isDeactivated() at " + i4 + ".\n");
							Assertions.assertTrue(ig.isLightOn());
							tester.traceMessage(
									"isLightOn() at " + i4 + ".\n");
						} catch(Exception e) {
							e.printStackTrace();
						}
					 },
				d4, TimeUnit.NANOSECONDS);
		long d5 = clock.delayToAcceleratedInstantInNanos(i5);
		this.scheduleTask(
				o -> {	try {
							Assertions.assertFalse(ig.isLightOn());
							tester.traceMessage(
									"!isLightOn() at " + i5 + ".\n");
							ig.off();
							Assertions.assertFalse(ig.isOn());
							tester.traceMessage("!isOn() at " + i5 + ".\n");
						} catch(Exception e) {
							e.printStackTrace();
						}
					 },
				d5, TimeUnit.NANOSECONDS);
		long d6 = clock.delayToAcceleratedInstantInNanos(i6);
		this.scheduleTask(
				o -> {	tester.traceMessage(
								"Simulated unit test ends at " + i6 + ".\n");
					 },
				d6, TimeUnit.NANOSECONDS);
	}
}
// -----------------------------------------------------------------------------
