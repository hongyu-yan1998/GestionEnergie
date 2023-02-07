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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.RTCoordinatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.SupervisorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.ComponentModelArchitecture;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.RTComponentAtomicModelDescriptor;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.RTComponentCoupledModelDescriptor;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.RTComponentModelArchitecture;
import fr.sorbonne_u.components.cyphy.tools.aclocks.AcceleratedClock;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServer;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerCI;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerConnector;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerOutboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.AirConditioner;
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.sil.AirConditionerStateModel;
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.sil.events.TurnOffAirConditioner;
import fr.sorbonne_u.components.hem2022e3.equipments.airconditioner.sil.events.TurnOnAirConditioner;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.HairDryer;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.HairDryerStateModel;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.HairDryerUserModel;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.events.SetHighHairDryer;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.events.SetLowHairDryer;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.events.SwitchOffHairDryer;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.events.SwitchOnHairDryer;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.test.HairDryerUser;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.ThermostatedHeater;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.HeaterCoupledModel;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.events.DoNotHeat;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.events.Heat;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.events.SwitchOffHeater;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.events.SwitchOnHeater;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.IndoorGarden;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.IndoorGardenStateModel;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.events.SwitchLightOffIndoorGarden;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.events.SwitchLightOnIndoorGarden;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.events.SwitchOffIndoorGarden;
import fr.sorbonne_u.components.hem2022e3.equipments.indoorgarden.sil.events.SwitchOnIndoorGarden;
import fr.sorbonne_u.components.hem2022e3.equipments.meter.ElectricMeter;
import fr.sorbonne_u.components.hem2022e3.equipments.meter.sil.ElectricMeterCoupledModel;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.SolarPanel;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.sil.SolarPanelStateModel;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.sil.events.SolarNotProduce;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.sil.events.SolarProduce;
import fr.sorbonne_u.devs_simulation.architectures.SimulationEngineCreationMode;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>IntegrationTestSupervisor</code> implements a supervisor
 * component for SIL simulated tests of the Household Energy Manager.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The supervisor component has two major roles: define the simulation
 * architecture connecting simulation models defined by the components y
 * the events that they exchange and start the simulation at the right time.
 * </p>
 * <p>
 * The method {@code createArchitecture} creates the high level simulation
 * architecture (tree of simulation models) among the components. In this
 * method, thanks to DEVS, the simulation models of each component can be seen
 * as an atomic model at the leaves of this overall architecture, hiding the
 * details of component simulators within these components.
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
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2022-10-25</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
// -----------------------------------------------------------------------------
@RequiredInterfaces(required={ClockServerCI.class})
// -----------------------------------------------------------------------------
public class			IntegrationTestSupervisor
extends		AbstractCyPhyComponent
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** simulation architectures can have URI to name them; this is the
	 *  URI used in this example.											*/
	public static final String		SIM_ARCHITECTURE_URI =
											"household-energy-manager-test";
	/** URI of the supervisor plug-in.										*/
	protected static final String	SUPERVISOR_PLUGIN_URI = "supervisor-uri";
	/** when true, methods trace their actions.								*/
	public static final boolean		VERBOSE = true;

	/** the supervisor plug-in attached to this component.					*/
	protected SupervisorPlugin			sp;
	/** outbound port to connect to the centralised clock server.			*/
	protected ClockServerOutboundPort	clockServerOBP;
	/** URI of the clock to be used to synchronise the test scenarios and
	 *  the simulation.														*/
	protected final String				clockURI;
	protected final double				accFactor;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create the supervisor component for the hair dryer SIL unit test..
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code clockURI != null && !clockURI.isEmpty()}
	 * pre	{@code accFactor > 0.0}
	 * post	{@code true}	// no more postcondition.
	 * </pre>
	 *
	 * @param clockURI		URI of the clock to be used to synchronise the test scenarios and the simulation.
	 * @param accFactor		acceleration factor used in timing actions.
	 */
	protected			IntegrationTestSupervisor(
		String clockURI,
		double accFactor
		)
	{
		super(1, 0);

		assert	clockURI != null && !clockURI.isEmpty() :
				new PreconditionException(
						"clockURI != null && !clockURI.isEmpty()");
		assert	accFactor > 0.0 : new PreconditionException("accFactor > 0.0");

		this.clockURI = clockURI;
		this.accFactor = accFactor;

		try {
			this.sp = new SupervisorPlugin(this.createArchitecture());
			this.sp.setPluginURI(SUPERVISOR_PLUGIN_URI);
			this.installPlugin(this.sp);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}

		if (VERBOSE) {
			this.tracer.get().setTitle("Supervisor component");
			this.tracer.get().setRelativePosition(0, 0);
			this.toggleTracing();
		}
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * create the simulation architecture for a SIL simulated integration test
	 * of the overall household energy management application.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret != null}
	 * </pre>
	 *
	 * @return				the unit test simulation architecture.
	 * @throws Exception	<i>to do</i>.
	 */
	@SuppressWarnings("unchecked")
	protected ComponentModelArchitecture	createArchitecture()
	throws Exception
	{
		// set of submodels of the unique coupled model that will compose
		// all of the component models
		Set<String> submodels = new HashSet<String>();
		// map that will contain the atomic model descriptors to construct
		// the simulation architecture
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();
		// connections among component models for exchanging events
		Map<EventSource,EventSink[]> connections = new HashMap<>();
		// map that will contain the (single here) coupled model descriptor to
		// construct the overall simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		// ---------------------------------------------------------------------
		// Hair dryer models
		// ---------------------------------------------------------------------

		// the model associated to the HairDryerUser component
		atomicModelDescriptors.put(
				HairDryerUserModel.URI,
				RTComponentAtomicModelDescriptor.create(
					HairDryerUserModel.URI,
					new Class[]{},
					new Class[]{
							SwitchOnHairDryer.class,SwitchOffHairDryer.class,
							SetLowHairDryer.class,SetHighHairDryer.class},
					TimeUnit.HOURS,
					HairDryerUser.REFLECTION_INBOUND_PORT_URI));
		// add it to the submodels
		submodels.add(HairDryerUserModel.URI);
		// the model associated to the HairDryer component
		atomicModelDescriptors.put(
				HairDryerStateModel.URI,
				RTComponentAtomicModelDescriptor.create(
					HairDryerStateModel.URI,
					new Class[]{
							SwitchOnHairDryer.class,SwitchOffHairDryer.class,
							SetLowHairDryer.class,SetHighHairDryer.class},
					new Class[]{
							SwitchOnHairDryer.class,SwitchOffHairDryer.class,
							SetLowHairDryer.class,SetHighHairDryer.class},
					TimeUnit.HOURS,
					HairDryer.REFLECTION_INBOUND_PORT_URI));
		// add it to the submodels
		submodels.add(HairDryerStateModel.URI);

		// connections exchanging events from the HairDryerUser component to
		// the HairDryer component
		connections.put(
				new EventSource(HairDryerUserModel.URI,
								SwitchOnHairDryer.class),
				new EventSink[] {
						new EventSink(HairDryerStateModel.URI,
									  SwitchOnHairDryer.class)
				});
		connections.put(
				new EventSource(HairDryerUserModel.URI,
								SwitchOffHairDryer.class),
				new EventSink[] {
						new EventSink(HairDryerStateModel.URI,
									  SwitchOffHairDryer.class)
				});
		connections.put(
				new EventSource(HairDryerUserModel.URI,
								SetLowHairDryer.class),
				new EventSink[] {
						new EventSink(HairDryerStateModel.URI,
									  SetLowHairDryer.class)
				});
		connections.put(
				new EventSource(HairDryerUserModel.URI,
								SetHighHairDryer.class),
				new EventSink[] {
						new EventSink(HairDryerStateModel.URI,
									  SetHighHairDryer.class)
				});

		// ---------------------------------------------------------------------
		// Heater models
		// ---------------------------------------------------------------------

		// the model associated to the ThermostatedHeater component
		atomicModelDescriptors.put(
				HeaterCoupledModel.URI,
				RTComponentAtomicModelDescriptor.create(
						HeaterCoupledModel.URI,
						new Class[]{},
						new Class[]{
								SwitchOnHeater.class, SwitchOffHeater.class,
								Heat.class, DoNotHeat.class},
						TimeUnit.HOURS,
						ThermostatedHeater.REFLECTION_INBOUND_PORT_URI));
		// add it to the submodels
		submodels.add(HeaterCoupledModel.URI);

		// ---------------------------------------------------------------------
		// Indoor garden models
		// ---------------------------------------------------------------------

		// the model associated to the IndoorGarden component
		atomicModelDescriptors.put(
				IndoorGardenStateModel.URI,
				RTComponentAtomicModelDescriptor.create(
						IndoorGardenStateModel.URI,
						new Class[]{},
						new Class[]{SwitchOnIndoorGarden.class,
									SwitchOffIndoorGarden.class,
									SwitchLightOnIndoorGarden.class,
									SwitchLightOffIndoorGarden.class},
						TimeUnit.HOURS,
						IndoorGarden.REFLECTION_INBOUND_PORT_URI));
		// add it to the submodels
		submodels.add(IndoorGardenStateModel.URI);
		
		// ---------------------------------------------------------------------
		// Air conditioner models
		// ---------------------------------------------------------------------

		// the model associated to the AirConditioner component
		atomicModelDescriptors.put(
				AirConditionerStateModel.URI,
				RTComponentAtomicModelDescriptor.create(
						AirConditionerStateModel.URI,
						new Class[]{},
						new Class[]{TurnOnAirConditioner.class,
									TurnOffAirConditioner.class},
						TimeUnit.HOURS,
						AirConditioner.REFLECTION_INBOUND_PORT_URI));
		// add it to the submodels
		submodels.add(AirConditionerStateModel.URI);
		
		// ---------------------------------------------------------------------
		// Solar panel models
		// ---------------------------------------------------------------------

		// the model associated to the AirConditioner component
		atomicModelDescriptors.put(
				SolarPanelStateModel.URI,
				RTComponentAtomicModelDescriptor.create(
						SolarPanelStateModel.URI,
						new Class[]{},
						new Class[]{SolarProduce.class,
									SolarNotProduce.class},
						TimeUnit.HOURS,
						SolarPanel.REFLECTION_INBOUND_PORT_URI));
		// add it to the submodels
		submodels.add(SolarPanelStateModel.URI);

		// ---------------------------------------------------------------------
		// Electric meter models
		// ---------------------------------------------------------------------

		// the model associated to the ElectricMeter component
		atomicModelDescriptors.put(
				ElectricMeterCoupledModel.URI,
				RTComponentAtomicModelDescriptor.create(
					ElectricMeterCoupledModel.URI,
					new Class[]{
							SwitchOnHairDryer.class, SwitchOffHairDryer.class,
							SetLowHairDryer.class, SetHighHairDryer.class,
							SwitchOnHeater.class, SwitchOffHeater.class,
							Heat.class, DoNotHeat.class,
							SwitchOnIndoorGarden.class,
							SwitchOffIndoorGarden.class,
							SwitchLightOnIndoorGarden.class,
							SwitchLightOffIndoorGarden.class,
							TurnOnAirConditioner.class,
							TurnOffAirConditioner.class,
							SolarProduce.class,
							SolarNotProduce.class},
					new Class[]{},
					TimeUnit.HOURS,
					ElectricMeter.REFLECTION_INBOUND_PORT_URI));
		// add it to the submodels
		submodels.add(ElectricMeterCoupledModel.URI);

		// connections exchanging events from the HairDryer component to
		// the ElectricMeter component
		connections.put(
			new EventSource(HairDryerStateModel.URI, SwitchOnHairDryer.class), 
			new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
								  SwitchOnHairDryer.class)
			});
		connections.put(
			new EventSource(HairDryerStateModel.URI, SwitchOffHairDryer.class), 
			new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
								  SwitchOffHairDryer.class)
			});
		connections.put(
			new EventSource(HairDryerStateModel.URI, SetLowHairDryer.class), 
			new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
								  SetLowHairDryer.class)
			});
		connections.put(
			new EventSource(HairDryerStateModel.URI, SetHighHairDryer.class), 
			new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
								  SetHighHairDryer.class)
			});

		// connections exchanging events from the ThermostatedHeater component
		// to the ElectricMeter component
		connections.put(
			new EventSource(HeaterCoupledModel.URI, SwitchOnHeater.class), 
			new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
								  SwitchOnHeater.class)
			});
		connections.put(
			new EventSource(HeaterCoupledModel.URI, SwitchOffHeater.class), 
			new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
								  SwitchOffHeater.class)
			});
		connections.put(
			new EventSource(HeaterCoupledModel.URI, Heat.class), 
			new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI, Heat.class)
			});
		connections.put(
			new EventSource(HeaterCoupledModel.URI, DoNotHeat.class), 
			new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
								  DoNotHeat.class)
			});

		// connections exchanging events from the IndoorGarden component to
		// the ElectricMeter component
		connections.put(
			new EventSource(IndoorGardenStateModel.URI,
							SwitchOnIndoorGarden.class), 
			new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
								  SwitchOnIndoorGarden.class)
			});
		connections.put(
			new EventSource(IndoorGardenStateModel.URI,
							SwitchOffIndoorGarden.class), 
			new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
								  SwitchOffIndoorGarden.class)
			});
		connections.put(
			new EventSource(IndoorGardenStateModel.URI,
							SwitchLightOnIndoorGarden.class), 
			new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
								  SwitchLightOnIndoorGarden.class)
			});
		connections.put(
			new EventSource(IndoorGardenStateModel.URI,
							SwitchLightOffIndoorGarden.class), 
			new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
								  SwitchLightOffIndoorGarden.class)
			});
	
		// connections exchanging events from the AirConditioner component to
		// the ElectricMeter component
		connections.put(
			new EventSource(AirConditionerStateModel.URI,
							TurnOnAirConditioner.class), 
			new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
								  TurnOnAirConditioner.class)
			});
		connections.put(
			new EventSource(AirConditionerStateModel.URI,
						    TurnOffAirConditioner.class), 
			new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
								  TurnOffAirConditioner.class)
			});
		
		// connections exchanging events from the SolarPanel component to
		// the ElectricMeter component
		connections.put(
			new EventSource(SolarPanelStateModel.URI,
							SolarProduce.class), 
			new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
							SolarProduce.class)
			});
		connections.put(
			new EventSource(SolarPanelStateModel.URI,
							SolarNotProduce.class), 
			new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
							SolarNotProduce.class)
			});
		
		// ---------------------------------------------------------------------
		// Coupled model
		// ---------------------------------------------------------------------

		coupledModelDescriptors.put(
				HEMCoupledModel.URI,
				RTComponentCoupledModelDescriptor.create(
						HEMCoupledModel.class,
						HEMCoupledModel.URI,
						submodels,
						null,
						null,
						connections,
						null,
						SimulationEngineCreationMode.COORDINATION_RT_ENGINE,
						null,
						IntegrationTestCoordinator.REFLECTION_INBOUND_PORT_URI,
						RTCoordinatorPlugin.class,
						null,
						this.accFactor));

		// SIL simulations must be executed under the real time modality to
		// be able to intertwine simulation events processing and code
		// execution under the same time line.
		RTComponentModelArchitecture arch =
				new RTComponentModelArchitecture(
						SIM_ARCHITECTURE_URI,
						HEMCoupledModel.URI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						TimeUnit.HOURS);

		return arch;
	}

	// -------------------------------------------------------------------------
	// Component life-cycle.
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public synchronized void	start() throws ComponentStartException
	{
		super.start();
		this.traceMessage("start.\n");
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		// create the simulator from the simulation architecture
		this.sp.createSimulator();
		// force the setting of simulation run parameters, as simulation
		// plug-ins in components can add some during the process
		this.sp.setSimulationRunParameters(new HashMap<String, Object>());
		// start a real time simulation with a delay before the
		// actual start of the simulation
		this.clockServerOBP = new ClockServerOutboundPort(this);
		this.clockServerOBP.publishPort();
		this.doPortConnection(
				this.clockServerOBP.getPortURI(),
				ClockServer.STANDARD_INBOUNDPORT_URI,
				ClockServerConnector.class.getCanonicalName());
		AcceleratedClock clock =
				this.clockServerOBP.getClock(this.clockURI);
		this.doPortDisconnection(this.clockServerOBP.getPortURI());
		this.clockServerOBP.unpublishPort();

		assert	this.accFactor == clock.getAccelerationFactor() :
				new AssertionError(
						"acceleration factor given at creation("
						+ this.accFactor
						+ ") is not equal to the one of the clock "
						+ this.clockURI + " (" + clock.getAccelerationFactor()
						+ ")");

		long realTimeOfStart =
				TimeUnit.NANOSECONDS.toMillis(clock.getStartEpochNanos());
		this.sp.startRTSimulation(realTimeOfStart, 0.0,
								  CVMIntegrationTest.SIMULATION_DURATION);
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		this.traceMessage("shutdown.\n");
		super.shutdown();
	}
}
// -----------------------------------------------------------------------------
