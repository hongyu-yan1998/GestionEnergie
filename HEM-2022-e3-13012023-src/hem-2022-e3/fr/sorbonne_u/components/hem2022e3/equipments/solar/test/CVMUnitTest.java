/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.solar.test;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServer;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.SolarPanel;
import fr.sorbonne_u.devs_simulation.simulators.AtomicRTEngine;

/**
 * The class <code>CVMUnitTest</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-04</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class CVMUnitTest 
extends AbstractCVM 
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** acceleration factor for the real time execution; it controls how fast
	 *  the simulation will run but keeping the same time structure as a real
	 *  time simulation running exactly at the pace of physical time; currently,
	 *  because of implementation constraints revolving around the precision
	 *  of the Java thread scheduler, this factor must be chosen in such a way
	 *  that intervals between simulation transitions or the execution of pieces
	 *  of code do not fall under 10 milliseconds approximately.			*/
	public static final double	ACC_FACTOR = 2400.0;	// 24h = 36s
	/** delay to start the real time simulations on every model at the
	 *  same moment (the order is delivered to the models during this
	 *  delay; this delay must be ample enough to give the time to notify
	 *  all models of their start time and to initialise them before starting,
	 *  a value that depends upon the complexity of the simulation architecture
	 *  to be traversed and the component deployment (deployments on several
	 *  JVM and even more several computers require a larger delay.			*/
	public static final long	DELAY_TO_START_SIMULATION = 5000L;
	/** duration  of the simulation in hours.								*/
	public static final double	SIMULATION_DURATION = 24.0;

	/** start instant in test scenarios, as a string to be parsed.			*/
	public static final String	START_INSTANT = "2022-11-08T00:00:00.00Z";
	/** Unix epoch time at which the execution of the simulation and the
	 *  test scenarios must start.											*/
	public static long			EXECUTION_START;
	/** duration of the simulation execution, computed from the simulation
	 *  duration and the acceleration factor.								*/
	protected static long		EXECUTION_DURATION;
	/** URI used to create the centralised clock on the {@code ClockServer}
	 *  and then to retrieve it from the participant components.		 	*/
	public static final String	CLOCK_URI = "solar-panel-clock";


	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public CVMUnitTest() throws Exception
	{
		
	}

	// -------------------------------------------------------------------------
	// Component virtual machine life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.cvm.AbstractCVM#deploy()
	 */
	@Override
	public void			deploy() throws Exception
	{
		boolean isUnderTest = true;
		boolean isUnitTesting = true;
		boolean isSimulated = true;
		String simArchitectureURI = "unit-test";

		// that the actual Unix epoch start time for all components, set in
		// the centralised clock
		EXECUTION_START =
				System.currentTimeMillis() + DELAY_TO_START_SIMULATION;

		// the accelerated clock and the clock server used to synchronise
		// simulations and test scenarios
		AbstractComponent.createComponent(
				ClockServer.class.getCanonicalName(),
				new Object[]{CLOCK_URI,		// create the centralised clock
							 TimeUnit.MILLISECONDS.toNanos(EXECUTION_START),
							 Instant.parse(START_INSTANT),
							 ACC_FACTOR});

		AbstractComponent.createComponent(
				SolarPanel.class.getCanonicalName(),
				new Object[]{isUnderTest, isUnitTesting, isSimulated,
							 simArchitectureURI, ACC_FACTOR, CLOCK_URI});

		AbstractComponent.createComponent(
				SolarPanelTester.class.getCanonicalName(),
				new Object[]{SolarPanel.INBOUND_PORT_URI_PREFIX,
							 CLOCK_URI});

		if (isSimulated) {
			AbstractComponent.createComponent(
					UnitTestSupervisor.class.getCanonicalName(),
					new Object[]{CLOCK_URI});
		}

		super.deploy();
	}

	// -------------------------------------------------------------------------
	// Main
	// -------------------------------------------------------------------------

	public static void	main(String[] args)
	{
		try {
			ClockServer.VERBOSE = true;
			CVMUnitTest cvm = new CVMUnitTest();
			// compute the execution duration in milliseconds from the
			// simulation duration in hours and the acceleration factor
			// i.e., the simulation duration times 3600 seconds per hour
			// times 1000 milliseconds per second divided by the acceleration
			// factor
			EXECUTION_DURATION =
					(long)(SIMULATION_DURATION*3600.0*1000.0/ACC_FACTOR);
			// start the execution for the above duration, adding the delay to
			// start the simulation, the tolerance used by the simulation
			// engine and another second at the end to make sure that the end of
			// components execution happens after  the end of the simulation and
			// the execution of the control
			cvm.startStandardLifeCycle(EXECUTION_DURATION
										+ DELAY_TO_START_SIMULATION
											+ AtomicRTEngine.END_TIME_TOLERANCE
												+ 1000L);
			// delay to look at the results before closing the trace windows
			Thread.sleep(10000L);
			// force the exit
			System.exit(0);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
	
}
