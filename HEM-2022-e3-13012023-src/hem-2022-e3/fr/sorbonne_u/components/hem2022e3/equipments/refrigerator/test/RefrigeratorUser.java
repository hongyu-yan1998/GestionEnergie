/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.test;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.AbstractPort;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.tools.aclocks.AcceleratedClock;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServer;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerCI;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerConnector;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerOutboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.RefrigeratorCI;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.connections.RefrigeratorConnector;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.connections.RefrigeratorOutboundPort;
import fr.sorbonne_u.components.reflection.interfaces.ReflectionCI;
import fr.sorbonne_u.exceptions.PreconditionException;

/**
 * The class <code>RefrigeratorUser</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-04</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
//-----------------------------------------------------------------------------
@RequiredInterfaces(required={RefrigeratorCI.class, ClockServerCI.class})
//-----------------------------------------------------------------------------
public class RefrigeratorUser 
extends AbstractComponent 
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** when true, methods trace their actions.								*/
	public static final boolean		VERBOSE = true;

	/** URI of the inbound port of the {@code Refrigerator}
	 *  component.															*/
	protected final String				refrigeratorInboundPortURI;
	/** outbound port to the {@code Refrigerator} component.			*/
	protected final RefrigeratorOutboundPort	rop;
	/** duration of the running in the test.								*/
	protected final long				duration;
	/** target freezing temperature for the running.	*/
	protected double			targetFreezingTemperature;
	/** target refrigeration temperature for the running.	*/
	protected double			targetRefrigerationTemperature;
	
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
	 * create a refrigerator user component for tests.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code refrigeratorInboundPortURI != null && !refrigeratorInboundPortURI.isEmpty()}
	 * pre	{@code duration > 0}
	 * pre	{@code clockURI != null && !clockURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param refrigeratorInboundPortURI	URI of the refrigerator inbound port.
	 * @param duration				duration of the running in the test.
	 * @param targetFreezingTemperature		target temperature to be set for the refrigerator.
	 * @param targetRefrigerationTemperature		target temperature to be set for the refrigerator.
	 * @param clockURI				URI of the clock to be used to synchronise the test scenarios and the simulation.
	 * @throws Exception			<i>to do</i>.
	 */
	protected			RefrigeratorUser(
		String refrigeratorInboundPortURI,
		long duration,
		double targetFreezingTemperature,
		double targetRefrigerationTemperature,
		String clockURI
		) throws Exception
	{
		this(AbstractPort.generatePortURI(ReflectionCI.class),
			 refrigeratorInboundPortURI, duration, targetFreezingTemperature,
				targetRefrigerationTemperature, clockURI);
	}

	/**
	 * create a refrigerator user component for tests.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code refrigeratorInboundPortURI != null && !refrigeratorInboundPortURI.isEmpty()}
	 * pre	{@code duration > 0}
	 * pre	{@code clockURI != null && !clockURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param reflectionInboundPortURI	URI of the inbound port offering the <code>ReflectionI</code> interface.
	 * @param refrigeratorInboundPortURI		URI of the refrigerator inbound port.
	 * @param duration					duration of the running in the test.
	 * @param targetTemperature			target temperature to be set for the refrigerator.
	 * @param targetRefrigerationTemperature    target temperature to be set for the refrigerator.
	 * @param clockURI					URI of the clock to be used to synchronise the test scenarios and the simulation.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			RefrigeratorUser(
		String reflectionInboundPortURI,
		String refrigeratorInboundPortURI,
		long duration,
		double targetFreezingTemperature,
		double targetRefrigerationTemperature,
		String clockURI
		) throws Exception
	{
		super(reflectionInboundPortURI, 1, 1);

		assert	refrigeratorInboundPortURI != null && !refrigeratorInboundPortURI.isEmpty() :
				new PreconditionException(
						"refrigeratorInboundPortURI != null && "
						+ "!refrigeratorInboundPortURI.isEmpty()");
		assert	duration > 0 : new PreconditionException("duration > 0");
		assert	clockURI != null && !clockURI.isEmpty() :
				new PreconditionException(
						"clockURI != null && !clockURI.isEmpty()");

		this.refrigeratorInboundPortURI = refrigeratorInboundPortURI;
		this.duration = duration;
		this.targetFreezingTemperature = targetFreezingTemperature;
		this.targetRefrigerationTemperature = targetRefrigerationTemperature;
		this.clockURI = clockURI;

		this.rop = new RefrigeratorOutboundPort(this);
		this.rop.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("refrigerator user component");
			this.tracer.get().setRelativePosition(2, 2);
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

		this.traceMessage("start.\n");
		try {
			this.doPortConnection(
					this.rop.getPortURI(),
					this.refrigeratorInboundPortURI,
					RefrigeratorConnector.class.getCanonicalName());
			this.clockServerOBP = new ClockServerOutboundPort(this);
			this.clockServerOBP.publishPort();
			this.doPortConnection(
					this.clockServerOBP.getPortURI(),
					ClockServer.STANDARD_INBOUNDPORT_URI,
					ClockServerConnector.class.getCanonicalName());
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}

	/**
	 * execute the test scenario which turns on the refrigerator at 00:01:00 and
	 * turns it off at 23:59:00.
	 * 
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public synchronized void	execute() throws Exception
	{
		// get the centralised clock from the clock server.
		this.clock = this.clockServerOBP.getClock(this.clockURI);
		// wait for the Unix epoch start time to execute the actions
		Thread.sleep(this.clock.waitUntilStartInMillis());

		final RefrigeratorUser ru = this;
		// get the base instant to plan the actions
		Instant i0 = clock.getStartInstant();
		// actions are done at 00:01:00
		Instant i1 = i0.plusSeconds(10 * 60);						// 00:10:00
		// use the clock to compute the Unix epoch time delay until the
		// actions must be performed
		long d1 = clock.delayToAcceleratedInstantInNanos(i1);
		// schedule the task performing the actions after the computed delay
//		System.out.println("ThermostatedHeaterUser#execute 1 " + d1 + " " + TimeUnit.NANOSECONDS.toMillis(d1));
		this.scheduleTask(
				o -> {	try {
							ru.traceMessage("start the refrigerator.\n");
							ru.rop.startRefrigerator();
							ru.traceMessage("set the target freezing temperature at "
									 		 + ru.targetFreezingTemperature + ".\n");
							ru.rop.setTargetFreezingTemperature(ru.targetFreezingTemperature);
							ru.traceMessage("set the target refrigeration temperature at "
							 		 		 + ru.targetRefrigerationTemperature + ".\n");
							ru.rop.setTargetRefrigerationTemperature(ru.targetRefrigerationTemperature);
						} catch(Exception e) {
							e.printStackTrace();
						}
					 },
				d1,		// delay before the task executes
				TimeUnit.NANOSECONDS);
		// switching off must occur at 23:59
		Instant i2 = i0.plusSeconds((23 * 3600) + (50 * 60));		// 23:50:00
		// use the clock to compute the Unix epoch time delay until the
		// actions must be performed
		long d2 = clock.delayToAcceleratedInstantInNanos(i2);
		// schedule the task performing the actions after the computed delay
//		System.out.println("ThermostatedHeaterUser#execute 2 " + d2);
		this.scheduleTask(
				o -> {	try {
							ru.traceMessage("stop the refrigerator.\n");
							ru.rop.stopRefrigerator();
						} catch(Exception e) {
							e.printStackTrace();
						}
					 },
				d2,		// delay before the task executes
				TimeUnit.NANOSECONDS);
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void	finalise() throws Exception
	{
		this.traceMessage("finalise.\n");
		this.doPortDisconnection(this.rop.getPortURI());
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
			this.rop.unpublishPort();
			this.clockServerOBP.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
}
