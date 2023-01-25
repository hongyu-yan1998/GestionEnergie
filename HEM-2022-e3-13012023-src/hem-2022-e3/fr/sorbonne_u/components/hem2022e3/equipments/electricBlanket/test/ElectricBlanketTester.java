package fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.test;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.tools.aclocks.AcceleratedClock;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServer;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerCI;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerConnector;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerOutboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.ElectricBlanketCI;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.connections.ElectricBlanketConnector;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.connections.ElectricBlanketOutboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

/**
* @author Liuyi CHEN & Hongyu YAN
* @date 4 janv. 2023
* @Description 
*/
//-----------------------------------------------------------------------------
@RequiredInterfaces(required={ElectricBlanketCI.class, ClockServerCI.class})
//-----------------------------------------------------------------------------
public class ElectricBlanketTester
extends AbstractComponent 
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	protected static boolean			VERBOSE = true;

	/** URI of the inbound port of the {@code IndoorGarden} component.		*/
	protected final String				inboundPortURI;
	/** outbound port used to connect to the {@code IndoorGarden} component.*/
	protected ElectricBlanketOutboundPort	outboundPort;

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
	 * create a heater user component for tests.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code heaterInboundPortURI != null && !heaterInboundPortURI.isEmpty()}
	 * pre	{@code duration > 0}
	 * pre	{@code clockURI != null && !clockURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param heaterInboundPortURI	URI of the heater inbound port.
	 * @param duration				duration of the heating in the test.
	 * @param targetTemperature		target temperature to be set for the heater.
	 * @param clockURI				URI of the clock to be used to synchronise the test scenarios and the simulation.
	 * @throws Exception			<i>to do</i>.
	 */
	protected			ElectricBlanketTester(
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
				this.tracer.get().setTitle("electric blanket unit tester component");
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
			this.outboundPort = new ElectricBlanketOutboundPort(this);
			this.outboundPort.publishPort();
			this.doPortConnection(
					this.outboundPort.getPortURI(),
					inboundPortURI,
					ElectricBlanketConnector.class.getCanonicalName());
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
			final ElectricBlanketOutboundPort ig = this.outboundPort;
			final ElectricBlanketTester tester = this;

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
								ig.isRunning();
								tester.traceMessage("on() at " + i1 + ".\n");
								ig.startBlanket();
								tester.traceMessage(
										"startBlanket() at " + i1 + ".\n");
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
								ig.stopBlanket();
								tester.traceMessage("stopBlanket() at " + i2 + ".\n");
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

	protected void		unitTest() throws Exception
	{
		Instant i0 = clock.getStartInstant();
		Instant i1 = i0.plusSeconds(4 * 3600);			// 4:00:00
		Instant i2 = i0.plusSeconds(4 * 3600 + 1800);	// 4:30:00
		Instant i3 = i0.plusSeconds(5 * 3600);			// 5:00:00
		Instant i4 = i0.plusSeconds(7 * 3600);			// 7:00:00
		Instant i5 = i0.plusSeconds(23 * 3600);			// 23:00:00
		Instant i6 = i0.plusSeconds(24 * 3600);			// 24:00:00

		final ElectricBlanketOutboundPort ig = this.outboundPort;
		final ElectricBlanketTester tester = this;

		tester.traceMessage("Simulated unit test begins...\n");
		Assertions.assertFalse(ig.isRunning());
		if (VERBOSE) tester.traceMessage("isOn false at " + i0 + ".\n");
		long d1 = clock.delayToAcceleratedInstantInNanos(i1);
		this.scheduleTask(
				o -> {	try {
							Assertions.assertTrue(ig.isRunning());
							tester.traceMessage("isRunning() at " + i1 + ".\n");
							ig.stopBlanket();
							tester.traceMessage("stopBlanket() at " + i1 + ".\n");
							Assertions.assertFalse(ig.isRunning());
							tester.traceMessage("!isRunning() at " + i1 + ".\n");
							ig.isRunning();
							tester.traceMessage("isRunning() at " + i1 + ".\n");
						} catch(Exception e) {
							e.printStackTrace();
						}
					 },
				d1, TimeUnit.NANOSECONDS);
		long d2 = clock.delayToAcceleratedInstantInNanos(i2);
		this.scheduleTask(
				o -> {	try {
						//a completer
						} catch(Exception e) {
							e.printStackTrace();
						}
					 },
				d2, TimeUnit.NANOSECONDS);
		long d3 = clock.delayToAcceleratedInstantInNanos(i3);
		this.scheduleTask(
				o -> {	try {
					//a completer
						} catch(Exception e) {
							e.printStackTrace();
						}
					 },
				d3, TimeUnit.NANOSECONDS);
		long d4 = clock.delayToAcceleratedInstantInNanos(i4);
		this.scheduleTask(
				o -> {	try {
							//ac
						} catch(Exception e) {
							e.printStackTrace();
						}
					 },
				d4, TimeUnit.NANOSECONDS);
		long d5 = clock.delayToAcceleratedInstantInNanos(i5);
		this.scheduleTask(
				o -> {	try {
							//ac
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
