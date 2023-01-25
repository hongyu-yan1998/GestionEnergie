package fr.sorbonne_u.components.hem2022e3.equipments.heater.test;

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
import fr.sorbonne_u.components.AbstractPort;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.tools.aclocks.AcceleratedClock;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServer;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerCI;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerConnector;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerOutboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.hem2022e1.equipments.heater.HeaterCI;
import fr.sorbonne_u.components.hem2022e1.equipments.heater.HeaterConnector;
import fr.sorbonne_u.components.hem2022e1.equipments.heater.HeaterOutboundPort;
import fr.sorbonne_u.components.reflection.interfaces.ReflectionCI;
import fr.sorbonne_u.exceptions.PreconditionException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

// -----------------------------------------------------------------------------
/**
 * The class <code>ThermostatedHeaterUser</code> implements user actions in
 * a test of the thermostated heater.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This component illustrates another way to implement user behaviours, by
 * directly scheduling user actions as tasks to be executed on a schedulable
 * pool of threads. Hence, the scheduling must be aligned with the simulation
 * time, the reason why he component needs to access the same accelerated clock
 * as the one used by other components and simulators in the run. 
 * </p>
 * <p>
 * The component connects to the {@code ThermostatedHeater} and, during its
 * execution, it switches on the heater, sets its target temperature and then
 * wait for the given duration before switching off the heater.
 * </p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code heaterInboundPortURI != null && !heaterInboundPortURI.isEmpty()}
 * invariant	{@code duration > 0}
 * invariant	{@code clockURI != null && !clockURI.isEmpty()}
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2022-11-02</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@RequiredInterfaces(required={HeaterCI.class, ClockServerCI.class})
//-----------------------------------------------------------------------------
public class			ThermostatedHeaterUser
extends		AbstractComponent
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	protected static boolean			VERBOSE = true;

	/** URI of the inbound port of the {@code ThermostatedHeater}
	 *  component.															*/
	protected final String				heaterInboundPortURI;
	/** outbound port to the {@code ThermostatedHeater} component.			*/
	protected final HeaterOutboundPort	hop;
	/** duration of the heating in the test.								*/
	protected final long				duration;
	/** target temperature to be set for the heater.						*/
	protected final double				targetTemperature;

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
	protected			ThermostatedHeaterUser(
		String heaterInboundPortURI,
		long duration,
		double targetTemperature,
		String clockURI
		) throws Exception
	{
		this(AbstractPort.generatePortURI(ReflectionCI.class),
			 heaterInboundPortURI, duration, targetTemperature, clockURI);
	}

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
	 * @param reflectionInboundPortURI	URI of the inbound port offering the <code>ReflectionI</code> interface.
	 * @param heaterInboundPortURI		URI of the heater inbound port.
	 * @param duration					duration of the heating in the test.
	 * @param targetTemperature			target temperature to be set for the heater.
	 * @param clockURI					URI of the clock to be used to synchronise the test scenarios and the simulation.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			ThermostatedHeaterUser(
		String reflectionInboundPortURI,
		String heaterInboundPortURI,
		long duration,
		double targetTemperature,
		String clockURI
		) throws Exception
	{
		super(reflectionInboundPortURI, 1, 1);

		assert	heaterInboundPortURI != null && !heaterInboundPortURI.isEmpty() :
				new PreconditionException(
						"heaterInboundPortURI != null && "
						+ "!heaterInboundPortURI.isEmpty()");
		assert	duration > 0 : new PreconditionException("duration > 0");
		assert	clockURI != null && !clockURI.isEmpty() :
				new PreconditionException(
						"clockURI != null && !clockURI.isEmpty()");

		this.heaterInboundPortURI = heaterInboundPortURI;
		this.duration = duration;
		this.targetTemperature = targetTemperature;
		this.clockURI = clockURI;

		this.hop = new HeaterOutboundPort(this);
		this.hop.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Heater user component");
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
					this.hop.getPortURI(),
					this.heaterInboundPortURI,
					HeaterConnector.class.getCanonicalName());
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
	 * execute the test scenario which switches on the heater at 00:01:00 and
	 * switches it off at 23:59:00.
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

		final ThermostatedHeaterUser thu = this;
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
							thu.traceMessage("start the heater.\n");
							thu.hop.startHeater();
							thu.traceMessage("set the target temperature at "
									 		 + thu.targetTemperature + ".\n");
							thu.hop.setTargetTemperature(thu.targetTemperature);
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
							thu.traceMessage("stop the heater.\n");
							thu.hop.stopHeater();
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
		this.doPortDisconnection(this.hop.getPortURI());
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
			this.hop.unpublishPort();
			this.clockServerOBP.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
}
// -----------------------------------------------------------------------------
