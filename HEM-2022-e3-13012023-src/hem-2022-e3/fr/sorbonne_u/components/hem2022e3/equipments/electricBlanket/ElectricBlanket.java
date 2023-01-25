package fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket;


import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.tools.aclocks.AcceleratedClock;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServer;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerCI;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerConnector;
import fr.sorbonne_u.components.cyphy.tools.aclocks.ClockServerOutboundPort;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.connections.ElectricBlanketInboundPort;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.ElectricBlanketCoupledModel;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.ElectricBlanketRTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.ElectricBlanketStateModel;
import fr.sorbonne_u.exceptions.PreconditionException;

//-----------------------------------------------------------------------------
/**
* The class <code>ElectricBlanket</code> an electric blanket component.
*
* <p><strong>Description</strong></p>
* 
* <p><strong>Invariant</strong></p>
* 
* <p>Created on : 2022-10-12</p>
* 
* @author Liuyi CHEN & Hongyu YAN
*/
//-----------------------------------------------------------------------------
@OfferedInterfaces(offered={ElectricBlanketCI.class})
@RequiredInterfaces(required={ClockServerCI.class})
//-----------------------------------------------------------------------------
public class ElectricBlanket 
extends AbstractCyPhyComponent 
implements ElectricBlanketImplementationI 
{

	// -------------------------------------------------------------------------
	// Inner interfaces and types
	// -------------------------------------------------------------------------

	/**
	 * The enumeration <code>ElectricBlanketState</code> describes the operation
	 * states of the electric blanket.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Created on : 2022-10-12</p>
	 * 
	 * @author	Liuyi CHEN & Hongyu YAN
	 */
	protected static enum	BlanketState
	{
		/** The electric blanket is on but not heating.						*/
		ON,
		/** The electric blanket on and heating with high temperature.		*/
		HIGHER_HEATING,
		/** The electric blanket on and heating with low temperature.		*/
		LOWER_HEATING,
		/** The electric blanket is off.									*/
		OFF
	}
	

	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	/** URI of the electric blanket reflection inbound port used in tests.			*/
	public static final String		REFLECTION_INBOUND_PORT_URI = "EB-RIP-URI";
	/** URI of the electric blanket inbound port used in tests.					*/
	public static final String		INBOUND_PORT_URI =
												"ELECTRIC-BLANKET-INBOUND-PORT-URI";
	/** when true, methods trace their actions.								*/
	public static final boolean		VERBOSE = true;
	/** fake current 	*/
	public static final double		FAKE_CURRENT_TEMPERATURE = 5.0;
	/** The maximum temperature that can be reached by an electric blanket */
	public static final double		MAX_TEMPERATURE = 40.0;
	/** The minimum temperature that can be reached by an electric blanket */
	public static final double		MIN_TEMPERATURE = 10.0;
	
	/** current state (on, off) of the electric blanket.								*/
	protected BlanketState								currentState;
	protected double									targetTemperature;
	/** inbound port offering the <code>ElectricBlanketCI</code> interface.			*/
	protected ElectricBlanketInboundPort				inboundPort;
	
	// SIL simulation
	/** URI of the pool of threads used to execute the simulator.			*/
	protected static final String						SIM_THREAD_POOL_URI =
															"SIMULATION-POOL";
	/** true if the component executes as a SIL simulation, false otherwise.*/
	protected final boolean								isSimulated;
	/** when true, the execution is a test.									*/
	protected final boolean								isUnderTest;
	/** when true, the execution is a unit test, otherwise an it is an
	 *  integration test.													*/
	protected final boolean								isUnitTest;
	/** URI of the simulation architecture to be created or the empty string
	 *  if the component does not execute as a SIL simulation.				*/
	protected final String								simArchitectureURI;
	/** simulator plug-in that holds the SIL simulator for this component.	*/
	protected ElectricBlanketRTAtomicSimulatorPlugin 	simulatorPlugin;
	/** acceleration factor to be used when running the real time
	 *  simulation.															*/
	protected final double								accFactor;
	/** outbound port to connect to the centralised clock server.			*/
	protected ClockServerOutboundPort					clockServerOBP;
	/** URI of the clock to be used to synchronise the test scenarios and
	 *  the simulation.														*/
	protected final String								clockURI;
	/** accelerated clock used to implement the simulation scenario.		*/
	protected AcceleratedClock							clock;
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	
	/**
	 * create a new electric blanket.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code electricBlanketIBPURI != null}
	 * pre	{@code !electricBlanketIBPURI.isEmpty()}
	 * post	true		// no postcondition.
	 * </pre>
	 * 
	 * @param electricBlanketIBPURI	URI of the inbound port to call the electric blanket component.
	 * @throws Exception			<i>to do </i>.
	 */
	protected			ElectricBlanket(
		boolean isUnderTest,
		boolean isUnitTest,
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
	this.isUnitTest = isUnitTest;
	this.isSimulated = isSimulated;
	this.simArchitectureURI = simArchitectureURI;
	this.accFactor = accFactor;
	this.clockURI = clockURI;

	this.currentState = BlanketState.OFF;
	this.inboundPort = new ElectricBlanketInboundPort(
								ElectricBlanket.INBOUND_PORT_URI, this);
	this.inboundPort.publishPort();

	if (VERBOSE) {
		this.tracer.get().setTitle("Indoor garden component");
		this.tracer.get().setRelativePosition(0, 3);
		this.toggleTracing();		
	}

	//assert	!this.();
	}
	
	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	@Override
	public synchronized void start() throws ComponentStartException 
	{
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
									new ElectricBlanketRTAtomicSimulatorPlugin();
					this.simulatorPlugin.setPluginURI(
							this.isUnitTest ?
								ElectricBlanketCoupledModel.URI
							:	ElectricBlanketStateModel.URI);
					this.simulatorPlugin.setSimulationExecutorService(
											STANDARD_SCHEDULABLE_HANDLER_URI);
					this.simulatorPlugin.initialiseSimulationArchitecture(
							this.simArchitectureURI,
							this.accFactor,
							this.isUnitTest);
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
	
	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
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
	
	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void	finalise() throws Exception
	{
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
			this.inboundPort.unpublishPort();
			this.clockServerOBP.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
	
	// -------------------------------------------------------------------------
	// Internal methods
	// -------------------------------------------------------------------------
	
	/**
	 * return true if the blanket is running.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true		// no precondition.
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @return	true if the blanket is running.
	 */
	public boolean		internalIsRunning()
	{
		return this.currentState == BlanketState.ON ||
				this.currentState == BlanketState.LOWER_HEATING ||
				this.currentState == BlanketState.HIGHER_HEATING ;
	}
	

	// -------------------------------------------------------------------------
	// Component services implementation !!! A modifier 
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.suspension.electricblanket.ElectricBlanketImplementationI#isRunning()
	 */
	@Override
	public boolean isRunning() throws Exception {
		if (ElectricBlanket.VERBOSE) {
			this.traceMessage("Electric blanket returns its state: " +
											this.currentState + ".\n");
		}
		return this.internalIsRunning();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.suspension.electricblanket.ElectricBlanketImplementationI#startBlanket()
	 */
	@Override
	public void startBlanket() throws Exception {
		if (ElectricBlanket.VERBOSE) {
			this.traceMessage("Electric blanket starts.\n");
		}
		assert	!this.internalIsRunning();

		this.currentState = BlanketState.ON;	
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.suspension.electricblanket.ElectricBlanketImplementationI#stopBlanket()
	 */
	@Override
	public void stopBlanket() throws Exception {
		if (ElectricBlanket.VERBOSE) {
			this.traceMessage("Electric blanket stops.\n");
		}
		assert	this.internalIsRunning();

		this.currentState = BlanketState.OFF;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.suspension.electricblanket.ElectricBlanketImplementationI#getCurrentTemperature()
	 */
	@Override
	public double getCurrentTemperature() throws Exception {
		// Temporary implementation; would need a temperature sensor.
		double currentTemperature = FAKE_CURRENT_TEMPERATURE;
		if (ElectricBlanket.VERBOSE) {
			this.traceMessage("Electric blanket returns the current"
							+ " temperature " + currentTemperature + ".\n");
		}

		return  currentTemperature;
	}

	@Override
	public boolean lowHeating() throws Exception {
		return this.currentState == BlanketState.LOWER_HEATING;
	}

	@Override
	public boolean highHeating() throws Exception {
		return this.currentState == BlanketState.HIGHER_HEATING;
	}

}
//-----------------------------------------------------------------------------
