package fr.sorbonne_u.components.hem2022e3.equipments.battery;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.hem2022e3.equipments.battery.connections.BatteryInboundPort;

/**
 * The class <code>Battery.java</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2022-10-19</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
@OfferedInterfaces(offered={BatteryCI.class})
public class Battery 
extends AbstractComponent 
implements BatteryImplementationI {
	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** URI of the hair dryer inbound port used in tests.					*/
	public static final String		INBOUND_PORT_URI =
												"BATTERY-INBOUND-PORT-URI";
	/** when true, methods trace their actions.								*/
	public static final boolean		VERBOSE = true;

	/** inbound port offering the <code>BatteryCI</code> interface.			*/
	protected BatteryInboundPort	bip;
	/** max stored energy.	*/
	protected double			maxEnergy = 100;
	/** current stored energy.	*/
	protected double		currentEnergy = 0;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------


	protected Battery() throws Exception {
		this(INBOUND_PORT_URI);
	}

	protected Battery(
			String batteryInboundPortURI) throws Exception
	{
		super(1, 0);
		this.initialise(batteryInboundPortURI);
	}
	
	protected Battery(
			String reflectionInboundPortURI, 
			String batteryrInboundPortURI) throws Exception
	{
		super(reflectionInboundPortURI, 1, 0);
		this.initialise(batteryrInboundPortURI);
	}
	
	/**
	 * create a new battery.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code batteryrInboundPortURI != null}
	 * pre	{@code !batteryrInboundPortURI.isEmpty()}
	 * post	true		// no postcondition.
	 * </pre>
	 *
	 * @param batteryrInboundPortURI	URI of the inbound port to call the battery component.
	 * @throws Exception			<i>to do </i>.
	 */
	protected void		initialise(String batteryrInboundPortURI) throws Exception
	{
		assert	batteryrInboundPortURI != null;
		assert	!batteryrInboundPortURI.isEmpty();

		this.bip = new BatteryInboundPort(batteryrInboundPortURI, this);
		this.bip.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Battery component");
			this.tracer.get().setRelativePosition(1, 1);
			this.toggleTracing();		
		}
	}
	// -------------------------------------------------------------------------
	// Internal methods
	// -------------------------------------------------------------------------

	public double getMaxEnergy() {
		return maxEnergy;
	}

	public void setMaxEnergy(double maxEnergy) {
		this.maxEnergy = maxEnergy;
	}
	
	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------
	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
		try {
			this.bip.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	@Override
	public boolean isFull() throws Exception {
		return this.currentEnergy == this.maxEnergy;
	}

	@Override
	public boolean isEmpty() throws Exception {
		return this.currentEnergy == 0;
	}

	@Override
	public void gainEnergy(double energy) throws Exception {
		assert !this.isFull();
		double sum = this.currentEnergy + energy;
		if(sum >= this.maxEnergy) {
			this.currentEnergy = this.maxEnergy;
			assert this.isFull();
		} else {
			this.currentEnergy = sum;
		}
	}

	@Override
	public void provideEnergy(double energy) throws Exception {
		assert !this.isEmpty();
		double diff = this.currentEnergy - energy;
		if(diff > 0) {
			this.currentEnergy = diff;
		} else {
			this.currentEnergy = 0;
			assert this.isEmpty();
		}
	}

}
