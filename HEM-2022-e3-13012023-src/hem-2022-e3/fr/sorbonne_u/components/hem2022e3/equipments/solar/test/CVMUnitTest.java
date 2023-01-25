/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.solar.test;

import fr.sorbonne_u.components.cvm.AbstractCVM;

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

	/** when true, the run will be SIL simulated, otherwise it will be
	 *  a test without simulation.											*/
	public static final boolean IS_SIMULATED = true;
	/** when true, the run is a test, which is always the case for this
	 *  deployment.															*/
	public static final boolean IS_UNDER_TEST = true;
	/** when true, the run is a unit test, which is always the case for
	 *  this deployment.													*/
	public static final boolean IS_UNIT_TESTING = true;
	/** acceleration factor for the real time execution; it controls how fast
	 *  the simulation will run but keeping the same time structure as a real
	 *  time simulation running exactly at the pace of physical time; currently,
	 *  because of implementation constraints revolving around the precision
	 *  of the Java thread scheduler, this factor must be chosen in such a way
	 *  that intervals between simulation transitions or the execution of pieces
	 *  of code do not fall under 10 milliseconds approximately.			*/
	public static final double	ACC_FACTOR = IS_SIMULATED ? 600.0 : 4800;
	/** delay to start the real time simulations on every model at the
	 *  same moment (the order is delivered to the models during this
	 *  delay; this delay must be ample enough to give the time to notify
	 *  all models of their start time and to initialise them before starting,
	 *  a value that depends upon the complexity of the simulation architecture
	 *  to be traversed and the component deployment (deployments on several
	 *  JVM and even more several computers require a larger delay.			*/
	public static final long	DELAY_TO_START_SIMULATION = 3000L;
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
	public static final String	CLOCK_URI = "refrigerator-clock";


	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public CVMUnitTest() throws Exception
	{
		
	}

}
