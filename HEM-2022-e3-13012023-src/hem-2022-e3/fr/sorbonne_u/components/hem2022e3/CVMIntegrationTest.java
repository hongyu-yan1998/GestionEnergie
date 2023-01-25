package fr.sorbonne_u.components.hem2022e3;

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
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.AirConditioner;
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.test.AirConditionerTester;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.HairDryer;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.test.HairDryerUser;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.ThermostatedHeater;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.ThermostatedHeaterController;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.test.ThermostatedHeaterUser;
import fr.sorbonne_u.components.hem2022e3.equipments.hem.HEM;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.IndoorGarden;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.test.IndoorGardenTester;
import fr.sorbonne_u.components.hem2022e3.equipments.meter.ElectricMeter;
import fr.sorbonne_u.devs_simulation.simulators.AtomicRTEngine;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.AbstractComponent;

// -----------------------------------------------------------------------------
/**
 * The class <code>CVMIntegrationTest</code> implements a deployment of
 * components performing an integration test for all of them together using
 * a SIL simulation to provide inputs to the sensors of components requiring
 * them.
 *
 * <p><strong>Description</strong></p>
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
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2022-10-24</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			CVMIntegrationTest
extends		AbstractCVM
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** when true, the run will be SIL simulated, otherwise it will be
	 *  a test without simulation.											*/
	public static final boolean IS_SIMULATED = true;
	/** when true, the run is a test, always the case for this deployment.	*/
	public static final boolean IS_UNDER_TEST = true;
	/** when true, the run is a unit test, always false for this deployment.	*/
	public static final boolean IS_UNIT_TEST = false;
	/** acceleration factor for the real time execution; it controls how fast
	 *  the simulation will run but keeping the same time structure as a real
	 *  time simulation running exactly at the pace of physical time; currently,
	 *  because of implementation constraints revolving around the precision
	 *  of the Java thread scheduler, this factor must be chosen in such a way
	 *  that intervals between simulation transitions or the execution of pieces
	 *  of code do not fall under 10 milliseconds approximately.			*/
	public static final double	ACC_FACTOR = IS_SIMULATED ? 300.0 : 4800;
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
	public static final String	CLOCK_URI = "integration-test--clock";

	/** target temperature for the run.										*/
	protected static double		TARGET_TEMPERATURE = 19.0;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public CVMIntegrationTest() throws Exception {
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.cvm.AbstractCVM#deploy()
	 */
	@Override
	public void			deploy() throws Exception
	{
		// that the actual Unix epoch start time for all components, set in
		// the centralised clock
		EXECUTION_START =
				System.currentTimeMillis() + DELAY_TO_START_SIMULATION;

		// Create a clock server with a unique accelerated clock used to
		// synchronise the start of the simulation and of the test scenarios
		// programmed within components
		AbstractComponent.createComponent(
				ClockServer.class.getCanonicalName(),
				// create the global accelerated clock
				new Object[]{CLOCK_URI,		// URI of the clock to retrieve it
											// start time in Unix epoch time
							 TimeUnit.MILLISECONDS.toNanos(EXECUTION_START),
							 				// the start instant synchronised
							 				// with the start time
							 Instant.parse(START_INSTANT),
							 ACC_FACTOR});	// the acceleration factor to evolve
											// time and instant after the start

		// ---------------------------------------------------------------------
		// Hair dryer components
		// ---------------------------------------------------------------------

		// the hair dryer component that holds its own simulation, hence
		// needing the acceleration factor for initialisation
		AbstractComponent.createComponent(
				HairDryer.class.getCanonicalName(),
				new Object[]{IS_SIMULATED,
							 IS_UNIT_TEST,
							 IntegrationTestSupervisor.SIM_ARCHITECTURE_URI,
							 ACC_FACTOR});
		// the user component implementing some usage scenario as an
		// event-driven simulation
		AbstractComponent.createComponent(
				HairDryerUser.class.getCanonicalName(),
				new Object[]{HairDryer.INBOUND_PORT_URI,
							 IntegrationTestSupervisor.SIM_ARCHITECTURE_URI,
							 ACC_FACTOR});

		// ---------------------------------------------------------------------
		// Thermostated heater components
		// ---------------------------------------------------------------------

		// the heater component that holds its own simulation, hence
		// needing the acceleration factor for initialisation, but also needs
		// the clock to timestamp its sensor data
		AbstractComponent.createComponent(
				ThermostatedHeater.class.getCanonicalName(),
				new Object[]{IS_UNDER_TEST,
							 IS_UNIT_TEST,
							 IS_SIMULATED,
							 IntegrationTestSupervisor.SIM_ARCHITECTURE_URI,
							 ACC_FACTOR,
							 CLOCK_URI});
		// the heater controller component that needs the clock to compute its
		// accelerated control period when executed in test mode
		AbstractComponent.createComponent(
				ThermostatedHeaterController.class.getCanonicalName(),
				new Object[]{
						ThermostatedHeater.SENSOR_INBOUND_PORT_URI,
						ThermostatedHeater.ACTUATOR_INBOUND_PORT_URI,
						ThermostatedHeaterController.STANDARD_HYSTERESIS,
						ThermostatedHeaterController.STANDARD_CONTROL_PERIOD,
						IS_UNDER_TEST,
						CLOCK_URI});
		// user component that will switch on the heater, set its target
		// temperature and then switch it off just before the end of the
		// execution; it needs the clock to schedule its test scenario actions
		AbstractComponent.createComponent(
				ThermostatedHeaterUser.class.getCanonicalName(),
				new Object[]{ThermostatedHeater.INBOUND_PORT_URI,
							 EXECUTION_DURATION,
							 TARGET_TEMPERATURE,
							 CLOCK_URI});

		// ---------------------------------------------------------------------
		// Indoor garden components
		// ---------------------------------------------------------------------

		// the indoor garden component that holds its own simulation, hence
		// needing the acceleration factor for initialisation, but also needs
		// the clock to manage correctly its planning for switching light on
		// and off
		AbstractComponent.createComponent(
				IndoorGarden.class.getCanonicalName(),
				new Object[]{IS_UNDER_TEST,
							 IS_UNIT_TEST,
							 IS_SIMULATED,
							 IntegrationTestSupervisor.SIM_ARCHITECTURE_URI,
							 ACC_FACTOR,
							 CLOCK_URI});
		// tester component for the indoor garden that needs the clock to
		// schedule its test scenario actions
		AbstractComponent.createComponent(
				IndoorGardenTester.class.getCanonicalName(),
				new Object[]{IndoorGarden.INBOUND_PORT_URI_PREFIX,
							 IS_UNIT_TEST,
							 CLOCK_URI});
		
		// ---------------------------------------------------------------------
		// Air conditioner components
		// ---------------------------------------------------------------------
		// the air conditioner component that holds its own simulation, hence
		// needing the acceleration factor for initialisation, but also needs
		// the clock to manage correctly its planning for turning it on
		// and off
		AbstractComponent.createComponent(
				AirConditioner.class.getCanonicalName(),
				new Object[]{IS_UNDER_TEST,
							 IS_UNIT_TEST,
							 IS_SIMULATED,
							 IntegrationTestSupervisor.SIM_ARCHITECTURE_URI,
							 ACC_FACTOR,
							 CLOCK_URI});
		// tester component for the air conditioner that needs the clock to
		// schedule its test scenario actions
		AbstractComponent.createComponent(
				AirConditionerTester.class.getCanonicalName(),
				new Object[]{AirConditioner.INBOUND_PORT_URI_PREFIX,
							 IS_UNIT_TEST,
							 CLOCK_URI});

		// ---------------------------------------------------------------------
		// Electric meter and HEM components
		// ---------------------------------------------------------------------

		// the electric meter component that holds its own simulation, hence
		// needing the acceleration factor for initialisation, but does not
		// needs the clock as it performs no actions in test scenarios.
		AbstractComponent.createComponent(
				ElectricMeter.class.getCanonicalName(),
				new Object[]{ElectricMeter.REFLECTION_INBOUND_PORT_URI,
							 ElectricMeter.ELECTRIC_METER_INBOUND_PORT_URI,
							 IS_UNDER_TEST, IS_SIMULATED,
							 IntegrationTestSupervisor.SIM_ARCHITECTURE_URI,
							 ACC_FACTOR});
		// the household energy manager component that does not hold a simulator
		// but needs the clock to perform its actions in test scenarios.
		AbstractComponent.createComponent(
				HEM.class.getCanonicalName(),
				new Object[]{IS_UNDER_TEST, CLOCK_URI});

		// ---------------------------------------------------------------------
		// SIL simulation components
		// ---------------------------------------------------------------------

		// a simulation coordinator component to help in managing the DEVS
		// simulation among the different components
		AbstractComponent.createComponent(
				IntegrationTestCoordinator.class.getCanonicalName(),
				new Object[]{});
		// a simulation supervisor component to compose the simulators and
		// manage the simulation among the different components; it needs the
		// clock and the acceleration factor both for initialisation of the
		// simulators and the start of the simulation at the start time given
		// by the clock
		AbstractComponent.createComponent(
				IntegrationTestSupervisor.class.getCanonicalName(),
				new Object[]{CLOCK_URI, ACC_FACTOR});

		super.deploy();
	}

	// -------------------------------------------------------------------------
	// Main
	// -------------------------------------------------------------------------

	public static void	main(String[] args)
	{
		try {
			CVMIntegrationTest cvm = new CVMIntegrationTest();
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
			cvm.startStandardLifeCycle(EXECUTION_DURATION +
										DELAY_TO_START_SIMULATION +
											AtomicRTEngine.END_TIME_TOLERANCE +
												1000L);
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
