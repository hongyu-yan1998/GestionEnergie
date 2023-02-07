/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.solar.test;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.tools.aclocks.AcceleratedClock;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServer;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerCI;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerConnector;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerOutboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.SolarPanelCI;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.connections.SolarPanelConnector;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.connections.SolarPanelOutboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

/**
 * The class <code>SolarPanelTester</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-1-7</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
//-----------------------------------------------------------------------------
@RequiredInterfaces(required={SolarPanelCI.class, ClockServerCI.class})
//-----------------------------------------------------------------------------
public class SolarPanelTester 
extends AbstractComponent {
	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	protected static boolean			VERBOSE = true;

	/** URI of the inbound port of the {@code SolarPanel} component.		*/
	protected final String				inboundPortURI;
	/** outbound port used to connect to the {@code SolarPanel} component.*/
	protected SolarPanelOutboundPort	outboundPort;

	/** when true, the component executes in unit test mode.				*/
//	protected final boolean				isUnitTesting;
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
	 * @param inboundPortURI	URI of the inbound port of the {@code SolarPanel} component.
	 * @param isUnitTesting		when true, the component executes in unit test mode.
	 * @param clockURI			URI of the clock to be used to synchronise the test scenarios and the simulation.
	 */
	protected			SolarPanelTester(
		String inboundPortURI,
//		boolean isUnitTesting,
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
//		this.isUnitTesting = isUnitTesting;
		this.clockURI = clockURI;

		if (VERBOSE) {
			this.tracer.get().setTitle("Solar panel unit tester component");
			this.tracer.get().setRelativePosition(3, 3);
			this.toggleTracing();
		}
	}
	
	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	@Override
	public synchronized void	start() throws ComponentStartException
	{
		super.start();

		try {
			this.outboundPort = new SolarPanelOutboundPort(this);
			this.outboundPort.publishPort();
			this.doPortConnection(
					this.outboundPort.getPortURI(),
					inboundPortURI,
					SolarPanelConnector.class.getCanonicalName());
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
		

		// perform a standard usage scenario
		final SolarPanelOutboundPort sp = this.outboundPort;
		final SolarPanelTester tester = this;

		// get the base instant to plan the actions
		Instant i0 = clock.getStartInstant();
		// actions are done at 04:00
		Instant i1 = i0.plusSeconds(6 * 3600);				// 6:00:00
		// use the clock to compute the Unix epoch time delay until the
		// actions must be performed
		long d1 = clock.delayToAcceleratedInstantInNanos(i1);
		// schedule the task performing the actions after the computed delay
		this.scheduleTask(
				o -> {	try {
							sp.startProduce();
							tester.traceMessage("startProduce() at " + i1 + ".\n");
						} catch(Exception e) {
							e.printStackTrace();
						}
					 },
				d1,		// delay before the task executes
				TimeUnit.NANOSECONDS);
		// switching off must occur at 23:00
		Instant i2 = i0.plusSeconds(18 * 3600);				// 18:00:00
		// use the clock to compute the Unix epoch time delay until the
		// actions must be performed
		long d2 = clock.delayToAcceleratedInstantInNanos(i2);
		// schedule the task performing the actions after the computed delay
		this.scheduleTask(
				o -> {	try {
							sp.stopProduce();
							tester.traceMessage("stopProduce() at " + i2 + ".\n");
						} catch(Exception e) {
							e.printStackTrace();
						}
					 },
				d2,		// delay before the task executes
				TimeUnit.NANOSECONDS);
		
	}

	@Override
	public synchronized void	finalise() throws Exception
	{
		this.doPortDisconnection(this.outboundPort.getPortURI());
		this.doPortDisconnection(this.clockServerOBP.getPortURI());
		super.finalise();
	}

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

}
