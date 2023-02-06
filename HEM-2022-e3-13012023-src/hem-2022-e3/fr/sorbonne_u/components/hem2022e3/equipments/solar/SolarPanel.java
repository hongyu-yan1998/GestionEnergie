package fr.sorbonne_u.components.hem2022e3.equipments.solar;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.tools.aclocks.AcceleratedClock;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServer;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerCI;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerConnector;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerOutboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.connections.SolarPanelInboundPort;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.sil.SolarPanelCoupledModel;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.sil.SolarPanelRTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.sil.SolarPanelStateModel;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.sil.events.SolarNotProduce;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.sil.events.SolarProduce;
import fr.sorbonne_u.exceptions.PreconditionException;

/**
 * The class <code>SolarPanel.java</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2022-10-19</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
//-----------------------------------------------------------------------------
@OfferedInterfaces(offered={SolarPanelCI.class})
@RequiredInterfaces(required={ClockServerCI.class})
//-----------------------------------------------------------------------------
public class SolarPanel 
extends AbstractComponent 
implements SolarPanelImplementationI {
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** URI of the scolar panel reflection inbound port used in tests.	*/
	public static final String	REFLECTION_INBOUND_PORT_URI = "SP-RIP-URI";	
	/** URI of the scolar panel inbound port used in tests.					*/
	public static final String		INBOUND_PORT_URI_PREFIX =
												"scolar-panel-ibp";
	/** when true, methods trace their actions.								*/
	public static final boolean		VERBOSE = true;

	/** inbound port offering the <code>SolarPanelCI</code> interface.		*/
	protected SolarPanelInboundPort	sip;
	
	/** energy produced.	*/
	protected double			energy = 0;
	
	/** when true, the component executes in test mode.						*/
	protected final boolean							isUnderTest;
	/** when true, the component executes in unit test mode.				*/	
	protected final boolean							isUnitTesting;
	/** true if the component must be execute in simulated mode.			*/
	protected final boolean							isSimulated;
	/** URI of the simulation architecture to be created or the empty string
	 *  if the component does not execute as a SIL simulation.				*/
	protected final String							simArchitectureURI;
	/** simulator plug-in that holds the SIL simulator for this component.	*/
	protected SolarPanelRTAtomicSimulatorPlugin	    simulatorPlugin;
	/** acceleration factor.												*/
	protected final double							accFactor;

	/** outbound port to connect to the centralised clock server.			*/
	protected ClockServerOutboundPort				clockServerOBP;
	/** URI of the clock to be used to synchronise the test scenarios and
	 *  the simulation.														*/
	protected final String							clockURI;
	/** accelerated clock used to implement the simulation scenario.		*/
	protected AcceleratedClock						clock;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	protected SolarPanel(
			boolean isUnderTest,
			boolean isUnitTesting,
			boolean isSimulated,
			String simArchitectureURI,
			double accFactor,
			String clockURI
			) throws Exception
	{
		super(REFLECTION_INBOUND_PORT_URI, 1, 1);

		assert	!isSimulated || simArchitectureURI != null && !simArchitectureURI.isEmpty() :
				new PreconditionException(
						"!isSimulated || simArchitectureURI != null && "
						+ "!simArchitectureURI.isEmpty()");
		assert	!isSimulated || accFactor > 0.0 :
				new PreconditionException("!isSimulated || accFactor > 0.0");
		assert	!isUnderTest || clockURI != null && !clockURI.isEmpty() :
				new PreconditionException(
						"!isUnderTest || "
						+ "clockURI != null && !clockURI.isEmpty()");
		
		this.isUnderTest = isUnderTest;
		this.isUnitTesting = isUnitTesting;
		this.isSimulated = isSimulated;
		this.simArchitectureURI = simArchitectureURI;
		this.accFactor = accFactor;
		this.clockURI = clockURI;
		
		this.sip = new SolarPanelInboundPort(
				SolarPanel.INBOUND_PORT_URI_PREFIX, this);
		this.sip.publishPort();
		
		if (VERBOSE) {
			this.tracer.get().setTitle("Air conditioner component");
			this.tracer.get().setRelativePosition(3, 3);
			this.toggleTracing();		
		}
	}
	
	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	@Override
	public synchronized void start() throws ComponentStartException {
		super.start();

		try {
			if (this.isUnderTest) {
				this.clockServerOBP = new ClockServerOutboundPort(this);
				this.clockServerOBP.publishPort();
				this.doPortConnection(
						this.clockServerOBP.getPortURI(),
						ClockServer.STANDARD_INBOUNDPORT_URI,
						ClockServerConnector.class.getCanonicalName());
				if (this.isSimulated) {
					this.simulatorPlugin =
									new SolarPanelRTAtomicSimulatorPlugin();
					this.simulatorPlugin.setPluginURI(
							this.isUnitTesting ?
								SolarPanelCoupledModel.URI
							:	SolarPanelStateModel.URI);
					this.simulatorPlugin.setSimulationExecutorService(
											STANDARD_SCHEDULABLE_HANDLER_URI);
					this.simulatorPlugin.initialiseSimulationArchitecture(
							this.simArchitectureURI,
							this.accFactor,
							this.isUnitTesting);
					this.installPlugin(this.simulatorPlugin);
				}
			} else {
				throw new ComponentStartException("only test version available!");
			}
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
		this.traceMessage("start.\n");
	}
	
	@Override
	public void execute() throws Exception {
		if (this.isUnderTest) {
			this.clock = this.clockServerOBP.getClock(this.clockURI);
			assert	this.clock.getAccelerationFactor() == this.accFactor :
				new AssertionError(
						"Incompatible clock having acceleration factor "
								+ this.clock.getAccelerationFactor()
								+ "with predefined acceleration factor "
								+ this.accFactor);
		}
	}
	
	@Override
	public synchronized void finalise() throws Exception {
		this.doPortDisconnection(this.clockServerOBP.getPortURI());
		super.finalise();
	}
	
	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
		this.traceMessage("shutdown.\n");
		try {
			this.sip.unpublishPort();
			this.clockServerOBP.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
	
	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	@Override
	public void startProduce() throws Exception {
		this.traceMessage("the solar panel starts producing.\n");
		if (this.isSimulated) {
			try {
				// trigger an immediate SolarProduce event on the
				// IndoorGardenStateModel, which in turn will emit this event
				// towards the other models of the indoor garden
				// the t parameter in the lambda expression represents the
				// current simulation time to be provided by he simulator before
				// passing the event instance to the model
				this.simulatorPlugin.triggerExternalEvent(
											SolarPanelStateModel.URI,
											t -> new SolarProduce(t));
			} catch (Exception e) {
				throw new RuntimeException(e) ;
			}
		} else {
			// In a fully operational implementation of an embedded software,
			// this method would call the physical actuator switching on the
			// indoor garden. As we are not developing for an actual device,
			// nothing is done.
			if (VERBOSE) {
				System.out.print(this.clock.currentInstant()
										+ " the solar panel starts producing.\n");
			}
		}
	}
	
	@Override
	public void stopProduce() throws Exception {
		this.traceMessage("the solar panel stops producing.\n");
		if (this.isSimulated) {
			try {
				// trigger an immediate SwitchOffIndoorGarden event on the
				// IndoorGardenStateModel, which in turn will emit this event
				// towards the other models of the indoor garden
				// the t parameter in the lambda expression represents the
				// current simulation time to be provided by he simulator before
				// passing the event instance to the model
				this.simulatorPlugin.triggerExternalEvent(
											SolarPanelStateModel.URI,
											t -> new SolarNotProduce(t));
			} catch (Exception e) {
				throw new RuntimeException(e) ;
			}
		} else {
			// In a fully operational implementation of an embedded software,
			// this method would call the physical actuator switching off the
			// indoor garden. As we are not developing for an actual device,
			// nothing is done.
			if (VERBOSE) {
				System.out.print(this.clock.currentInstant()
										+ " switch the indoor garden off.\n");
			}
		}
	}

	@Override
	public double getEnergyProduction() throws Exception {
		// TODO Auto-generated method stub
		return this.energy;
	}

	@Override
	// not here
	public void setEnergyProduction(double energy) throws Exception {
		// TODO Auto-generated method stub

	}
}
