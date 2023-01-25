package fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.test;

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

import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServer;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.HairDryer;
import fr.sorbonne_u.devs_simulation.simulators.AtomicRTEngine;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;

import fr.sorbonne_u.components.AbstractComponent;

// -----------------------------------------------------------------------------
/**
 * The class <code>CVMUnitTest</code> implements a deployment for the unit
 * test of the hair dryer.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This deployment class implements in fact two different deployments. When
 * the boolean {@code isSILsimulated} in the method {@code deploy} is false,
 * the execution performs simple unit tests. When it is true, the execution uses
 * the SIL simulation, where the user is modelled by random delays between
 * actions on the hair dryer, hence producing random electricity consumptions.
 * </p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code ACC_FACTOR > 0.0}
 * invariant	{@code DELAY_TO_START_SIMULATION > 0L}
 * invariant	{@code SIMULATION_DURATION > 0.0}
 * invariant	{@code START_INSTANT != null}
 * invariant	{@code Assertions.assertDoesNotThrow(() -> Instant.parse(START_INSTANT))}
 * invariant	{@code CLOCK_URI != null && !CLOCK_URI.isEmpty()}
 * </pre>
 * 
 * <p>Created on : 2022-10-24</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			CVMUnitTest
extends		AbstractCVM
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
	public static final double		ACC_FACTOR = 4800.0;
	/** delay to start the real time simulations on every model at the
	 *  same moment (the order is delivered to the models during this
	 *  delay; this delay must be ample enough to give the time to notify
	 *  all models of their start time and to initialise them before starting,
	 *  a value that depends upon the complexity of the simulation architecture
	 *  to be traversed and the component deployment (deployments on several
	 *  JVM and even more several computers require a larger delay.			*/
	public static final long		DELAY_TO_START_SIMULATION = 3000L;
	/** duration  of the simulation.										*/
	public static final double		SIMULATION_DURATION = 24.0;
	/** start instant in test scenarios, as a string to be parsed.			*/
	public static final String		START_INSTANT = "2022-11-08T00:00:00.00Z";
	/** URI used to create the centralised clock on the {@code ClockServer}
	 *  and then to retrieve it from the participant components.		 	*/
	public static final String		CLOCK_URI = "hairdryer-clock";
	/** Unix epoch time at which the execution of the simulation and the
	 *  test scenarios must start.											*/
	public static long				EXECUTION_START;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public				CVMUnitTest() throws Exception
	{
		assert	ACC_FACTOR > 0.0;
		assert	DELAY_TO_START_SIMULATION > 0L;
		assert	SIMULATION_DURATION > 0.0;
		assert	START_INSTANT != null;
		Assertions.assertDoesNotThrow(() -> Instant.parse(START_INSTANT));
		assert	CLOCK_URI != null && !CLOCK_URI.isEmpty();
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
		// when true, the run will be SIL simulated, otherwise it will be a
		// test without simulation
		final boolean isSILsimulated = true;
		// when true, the run is a unit test, which is always the case from this
		// deployment
		final boolean isUnitTesting = true;

		// create the the component implementing the hair dryer
		AbstractComponent.createComponent(
				HairDryer.class.getCanonicalName(),
				new Object[]{isSILsimulated, isUnitTesting,
							 UnitTestSupervisor.SIM_ARCHITECTURE_URI,
							 ACC_FACTOR});

		// if SIL simulated, a supervisor component is required to start the
		// simulation and a coordinator component is needed because two
		// components use SIL simulators that must be coordinated
		if (isSILsimulated) {
			// that the actual Unix epoch start time for all components, set in
			// the centralised clock
			EXECUTION_START =
					System.currentTimeMillis() + DELAY_TO_START_SIMULATION;

			AbstractComponent.createComponent(
					ClockServer.class.getCanonicalName(),
					new Object[]{CLOCK_URI,		// create the centralised clock
								 TimeUnit.MILLISECONDS.toNanos(EXECUTION_START),
								 Instant.parse(START_INSTANT),
								 ACC_FACTOR});

			// create a user component that will switch on the hair dryer, set its
			// level and then switch it off just and cycle
			AbstractComponent.createComponent(
					HairDryerUser.class.getCanonicalName(),
					new Object[]{HairDryer.INBOUND_PORT_URI,
								 UnitTestSupervisor.SIM_ARCHITECTURE_URI,
								 ACC_FACTOR});

			AbstractComponent.createComponent(
					UnitTestCoordinator.class.getCanonicalName(),
					new Object[]{});
			AbstractComponent.createComponent(
					UnitTestSupervisor.class.getCanonicalName(),
					new Object[]{CLOCK_URI});
		} else {
			// create a unit tester component that will switch on the hair dryer,
			// set its level and then switch it off just and cycle
			AbstractComponent.createComponent(
					HairDryerUnitTester.class.getCanonicalName(),
					new Object[]{HairDryer.INBOUND_PORT_URI});
		}

		super.deploy();
	}

	// -------------------------------------------------------------------------
	// Main
	// -------------------------------------------------------------------------

	public static void	main(String[] args)
	{
		try {
			CVMUnitTest cvm = new CVMUnitTest();
			// compute the execution duration in milliseconds from the
			// simulation duration in hours and the acceleration factor
			// i.e., the simulation duration times 3600 seconds per hour
			// times 1000 milliseconds per second divided by the acceleration
			// factor
			long d = (long)(SIMULATION_DURATION*3600.0*1000.0/ACC_FACTOR);
			cvm.startStandardLifeCycle(d + DELAY_TO_START_SIMULATION
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
// -----------------------------------------------------------------------------
