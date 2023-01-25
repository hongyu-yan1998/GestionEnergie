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
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.HairDryer;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.HairDryerCoupledModel;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.HairDryerStateModel;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.HairDryerUnitTestCoupledModel;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.HairDryerUserModel;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.events.SetHighHairDryer;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.events.SetLowHairDryer;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.events.SwitchOffHairDryer;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.events.SwitchOnHairDryer;
import fr.sorbonne_u.devs_simulation.architectures.SimulationEngineCreationMode;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>UnitTestSupervisor</code> implements a supervisor component
 * for SIL simulated tests of the hair dryer.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code SUPERVISOR_PLUGIN_URI != null && !SUPERVISOR_PLUGIN_URI.isEmpty()}
 * invariant	{@code clockURI != null && !clockURI.isEmpty()}
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code SIM_ARCHITECTURE_URI != null && !SIM_ARCHITECTURE_URI.isEmpty()}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2022-10-25</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
// -----------------------------------------------------------------------------
@RequiredInterfaces(required={ClockServerCI.class})
// -----------------------------------------------------------------------------
public class			UnitTestSupervisor
extends		AbstractCyPhyComponent
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** simulation architectures can have URI to name them; this is the
	 *  URI used in this example.											*/
	public static final String		SIM_ARCHITECTURE_URI = "global";
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
	 * post	{@code true}	// no more postcondition.
	 * </pre>
	 *
	 * @param clockURI	URI of the clock to be used to synchronise the test scenarios and the simulation.
	 */
	protected			UnitTestSupervisor(String clockURI)
	{
		super(1, 0);

		assert	clockURI != null && !clockURI.isEmpty() :
				new PreconditionException(
						"clockURI != null && !clockURI.isEmpty()");

		this.clockURI = clockURI;

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
	 * create the simulation architecture for a SIL simulated unit test of the
	 * hair dryer.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret != null}
	 * </pre>
	 *
	 * @return				the global simulation architecture.
	 * @throws Exception	<i>to do</i>.
	 */
	@SuppressWarnings("unchecked")
	protected ComponentModelArchitecture	createArchitecture()
	throws Exception
	{
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		// Descriptors for atomic models included in components must define
		// simulation models specific information plus the reflection inbound
		// port URI of the component holding them in order to connect this
		// component inside the component-based simulation architecture.

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

		atomicModelDescriptors.put(
				HairDryerCoupledModel.URI,
				RTComponentAtomicModelDescriptor.create(
						HairDryerStateModel.URI,
						new Class[]{
							SwitchOnHairDryer.class,SwitchOffHairDryer.class,
							SetLowHairDryer.class,SetHighHairDryer.class},
						new Class[]{},
						TimeUnit.HOURS,
						HairDryer.REFLECTION_INBOUND_PORT_URI));


		Set<String> submodels = new HashSet<String>();
		submodels.add(HairDryerUserModel.URI);
		submodels.add(HairDryerCoupledModel.URI);

		Map<EventSource,EventSink[]> connections = new HashMap<>();
		connections.put(
				new EventSource(HairDryerUserModel.URI,
								SwitchOnHairDryer.class),
				new EventSink[] {
						new EventSink(HairDryerCoupledModel.URI,
									  SwitchOnHairDryer.class)
				});
		connections.put(
				new EventSource(HairDryerUserModel.URI,
								SwitchOffHairDryer.class),
				new EventSink[] {
						new EventSink(HairDryerCoupledModel.URI,
									  SwitchOffHairDryer.class)
				});
		connections.put(
				new EventSource(HairDryerUserModel.URI,
								SetLowHairDryer.class),
				new EventSink[] {
						new EventSink(HairDryerCoupledModel.URI,
									  SetLowHairDryer.class)
				});
		connections.put(
				new EventSource(HairDryerUserModel.URI,
								SetHighHairDryer.class),
				new EventSink[] {
						new EventSink(HairDryerCoupledModel.URI,
									  SetHighHairDryer.class)
				});

		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
														new HashMap<>();

		coupledModelDescriptors.put(
				HairDryerUnitTestCoupledModel.URI,
				RTComponentCoupledModelDescriptor.create(
						HairDryerUnitTestCoupledModel.class,
						HairDryerUnitTestCoupledModel.URI,
						submodels,
						null,
						null,
						connections,
						null,
						SimulationEngineCreationMode.COORDINATION_RT_ENGINE,
						null,
						UnitTestCoordinator.REFLECTION_INBOUND_PORT_URI,
						RTCoordinatorPlugin.class,
						null,
						CVMUnitTest.ACC_FACTOR));

		// SIL simulations must be executed under the real time modality to
		// be able to intertwine simulation events processing and code
		// execution under the same time line.
		RTComponentModelArchitecture arch =
				new RTComponentModelArchitecture(
						SIM_ARCHITECTURE_URI,
						HairDryerUnitTestCoupledModel.URI,
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
		this.sp.createSimulator();
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

		long realTimeOfStart =
				TimeUnit.NANOSECONDS.toMillis(clock.getStartEpochNanos());
		this.sp.startRTSimulation(realTimeOfStart, 0.0,
								  CVMUnitTest.SIMULATION_DURATION);
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
