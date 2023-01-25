/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.test;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;

import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.AirConditionerCI;
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.connections.AirConditionerConnector;
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.connections.AirConditionerOutboundPort;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.tools.aclocks.AcceleratedClock;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServer;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerCI;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerConnector;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerOutboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.exceptions.PreconditionException;

/**
 * The class <code>AirConditionerTester</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-02</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
//-----------------------------------------------------------------------------
@RequiredInterfaces(required={AirConditionerCI.class, ClockServerCI.class})
//-----------------------------------------------------------------------------
public class AirConditionerTester 
extends AbstractComponent {
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	protected static boolean			VERBOSE = true;

	/** URI of the inbound port of the {@code AirConditioner} component.		*/
	protected final String				inboundPortURI;
	/** outbound port used to connect to the {@code AirConditioner} component.*/
	protected AirConditionerOutboundPort	outboundPort;

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
	 * @param inboundPortURI	URI of the inbound port of the {@code AirConditioner} component.
	 * @param isUnitTesting		when true, the component executes in unit test mode.
	 * @param clockURI			URI of the clock to be used to synchronise the test scenarios and the simulation.
	 */
	protected			AirConditionerTester(
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
			this.tracer.get().setTitle("Air conditioner unit tester component");
			this.tracer.get().setRelativePosition(3, 1);
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
			this.outboundPort = new AirConditionerOutboundPort(this);
			this.outboundPort.publishPort();
			this.doPortConnection(
					this.outboundPort.getPortURI(),
					inboundPortURI,
					AirConditionerConnector.class.getCanonicalName());
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
	 * perform a standard usage scenario that simply turns on the air
	 * conditioner, plans the air conditioner on and off from 06:00 to 22:00.
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
			final AirConditionerOutboundPort ac = this.outboundPort;
			final AirConditionerTester tester = this;

			// get the base instant to plan the actions
			Instant i0 = clock.getStartInstant();
			// actions are done at 04:00
			Instant i1 = i0.plusSeconds(4 * 3600);				// 4:00:00
			final Instant turnOnInstant =
									i0.plusSeconds(6 * 3600);	// 06:00:00.00
			final Instant turnOffInstant =
									i0.plusSeconds(22 * 3600);	// 22:00:00.00

			// use the clock to compute the Unix epoch time delay until the
			// actions must be performed
			long d1 = clock.delayToAcceleratedInstantInNanos(i1);
			// schedule the task performing the actions after the computed delay
			this.scheduleTask(
					o -> {	try {
								//  ac.on();
								// tester.traceMessage("on() at " + i1 + ".\n");
								ac.plan(turnOnInstant, turnOffInstant);
								tester.traceMessage(
										"plan() at " + i1 + ".\n");
							} catch(Exception e) {
								e.printStackTrace();
							}
						 },
					d1,		// delay before the task executes
					TimeUnit.NANOSECONDS);
//			long d2 = clock.delayToAcceleratedInstantInNanos(turnOffInstant);
//			// schedule the task performing the actions after the computed delay
//			this.scheduleTask(
//					o -> {	try {
//								ac.off();
//								tester.traceMessage("off() at " + turnOffInstant + ".\n");
//							} catch(Exception e) {
//								e.printStackTrace();
//							}
//						 },
//					d2,		// delay before the task executes
//					TimeUnit.NANOSECONDS);
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
	 * i1 = 04:30:00 | plan           | test isAirConditionerPlanned, isOn, cancelPlan
	 * i2 = 05:00:00 | plan           | also test getPlannedAirConditionerOn, getPlannedAirConditionerOff
	 * 06:00:00      | turnOn         | planned turn air conditioner on
	 * i3 = 07:00:00 | deactivate     | also test isAirConditionerPlanned, deactivate, reactivate, isDeactivated
	 * 22:00:00      | turnOff        | planned turn air conditioner off
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
		Instant i2 = i0.plusSeconds(4 * 3600 + 1800);	// 4:30:00
		Instant i3 = i0.plusSeconds(5 * 3600);			// 5:00:00
		Instant i4 = i0.plusSeconds(7 * 3600);			// 7:00:00
		Instant i6 = i0.plusSeconds(24 * 3600);			// 24:00:00

		final AirConditionerOutboundPort ac = this.outboundPort;
		final AirConditionerTester tester = this;

		tester.traceMessage("Simulated unit test begins...\n");
		Assertions.assertFalse(ac.isOn());
		if (VERBOSE) tester.traceMessage("isOn false at " + i0 + ".\n");
		
		long d2 = clock.delayToAcceleratedInstantInNanos(i2);
		final Instant turnOnInstant =
							i0.plusSeconds(6 * 3600); // 06:00:00.00
		final Instant turnOffInstant =
							i0.plusSeconds(22 * 3600); // 22:00:00.00
		this.scheduleTask(
				o -> {	try {
							Assertions.assertFalse(ac.isAirConditionerPlanned());
							tester.traceMessage(
									"!isAirConditionerPlanned() at " + i2 + ".\n");
							ac.plan(turnOnInstant, turnOffInstant);
							tester.traceMessage(
									"plan() at " + i2 + ".\n");
							Assertions.assertTrue(ac.isAirConditionerPlanned());
							tester.traceMessage(
									"plan() at " + i2 + ".\n");
							Assertions.assertFalse(ac.isOn());
							tester.traceMessage(
									"!isOn() at " + i2 + ".\n");
							ac.cancelPlan();
							tester.traceMessage(
									"cancelPlan() at " + i2 + ".\n");
							Assertions.assertFalse(ac.isAirConditionerPlanned());
							tester.traceMessage(
									"!isAirConditionerPlanned() at " + i2 + ".\n");
						} catch(Exception e) {
							e.printStackTrace();
						}
					 },
				d2, TimeUnit.NANOSECONDS);
		long d3 = clock.delayToAcceleratedInstantInNanos(i3);
		this.scheduleTask(
				o -> {	try {
							Assertions.assertFalse(ac.isAirConditionerPlanned());
							tester.traceMessage(
									"!isAirConditionerPlanned() at " + i3 + ".\n");
							ac.plan(turnOnInstant, turnOffInstant);
							tester.traceMessage(
									"plan() at " + i3 + ".\n");
							Assertions.assertTrue(ac.isAirConditionerPlanned());
							tester.traceMessage(
									"isAirConditionerPlanned() at " + i3 + ".\n");
							Instant i = ac.getPlannedAirConditionerOn();
							Assertions.assertEquals(turnOnInstant, i);
							tester.traceMessage(
									"getPlannedAirConditionerOn() " + i + " at "
															+ i3 + ".\n");
							i = ac.getPlannedAirConditionerOff();
							Assertions.assertEquals(turnOffInstant, i);
							tester.traceMessage(
									"getPlannedAirConditionerOff() " + i + " at "
															+ i3 + ".\n");
							Assertions.assertFalse(ac.isDeactivated());
							tester.traceMessage(
									"!isDeactivated() at " + i3 + ".\n");
							ac.deactivate();
							tester.traceMessage(
									"deactivate() at " + i3 + ".\n");
							Assertions.assertTrue(ac.isDeactivated());
							tester.traceMessage(
									"isDeactivated() at " + i3 + ".\n");
							Assertions.assertFalse(ac.isOn());
							tester.traceMessage(
									"!isOn() at " + i3 + ".\n");
							ac.reactivate();
							tester.traceMessage(
									"reactivate() at " + i3 + ".\n");
							Assertions.assertFalse(ac.isOn());
							tester.traceMessage(
									"!isOn() at " + i3 + ".\n");
						} catch(Exception e) {
							e.printStackTrace();
						}
					 },
				d3, TimeUnit.NANOSECONDS);
		long d4 = clock.delayToAcceleratedInstantInNanos(i4);
		this.scheduleTask(
				o -> {	try {
							Assertions.assertFalse(ac.isDeactivated());
							tester.traceMessage(
									"!isDeactivated() at " + i4 + ".\n");
							ac.deactivate();
							tester.traceMessage(
									"deactivate() at " + i4 + ".\n");
							Assertions.assertTrue(ac.isDeactivated());
							tester.traceMessage(
									"isDeactivated() at " + i4 + ".\n");
							Assertions.assertFalse(ac.isOn());
							tester.traceMessage(
									"!isOn() at " + i4 + ".\n");
							ac.reactivate();
							tester.traceMessage(
									"reactivate() at " + i4 + ".\n");
							Assertions.assertFalse(ac.isDeactivated());
							tester.traceMessage(
									"!isDeactivated() at " + i4 + ".\n");
							Assertions.assertTrue(ac.isOn());
							tester.traceMessage(
									"isOn() at " + i4 + ".\n");
						} catch(Exception e) {
							e.printStackTrace();
						}
					 },
				d4, TimeUnit.NANOSECONDS);
		long d6 = clock.delayToAcceleratedInstantInNanos(i6);
		this.scheduleTask(
				o -> {	tester.traceMessage(
								"Simulated unit test ends at " + i6 + ".\n");
					 },
				d6, TimeUnit.NANOSECONDS);
	}
}
