package fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.utils.StandardComponentLogger;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.events.BlanketEventI;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.events.DoNotHeat;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.events.SetHighTemperature;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.events.SetLowTemperature;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.events.SwitchOffBlanket;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.events.SwitchOnBlanket;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

/**
* @author Liuyi CHEN & Hongyu YAN
* @date 4 janv. 2023
* @Description 
*/
//-----------------------------------------------------------------------------
@ModelExternalEvents(
		imported = {DoNotHeat.class,
					SetHighTemperature.class,
					SetLowTemperature.class,
					SwitchOffBlanket.class,
					SwitchOnBlanket.class},
		exported = {DoNotHeat.class,
					SetHighTemperature.class,
					SetLowTemperature.class,
					SwitchOffBlanket.class,
					SwitchOnBlanket.class}
		)
//-----------------------------------------------------------------------------
public class ElectricBlanketStateModel
extends AtomicModel
implements ElectricBlanketOperationI 
{
	
	public static enum	State
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
	// Inner classes and types
	// -------------------------------------------------------------------------

	private static final long	serialVersionUID = 1L;
	/** URI for a model; works when only one instance is created.			*/
	public static final String	URI = ElectricBlanketStateModel.class.getSimpleName();

	/** current state of the hair dryer.									*/
	protected State				currentState;
	/** last received event or null if none.								*/
	protected BlanketEventI		lastReceived;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public				ElectricBlanketStateModel(
			String uri,
			TimeUnit simulatedTimeUnit,
			SimulatorI simulationEngine
			) throws Exception
		{
			super(uri, simulatedTimeUnit, simulationEngine);
			this.setLogger(new StandardLogger());
		}
	
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	@Override
	public void			switchOnBlanket()
	{
		if(this.currentState == State.OFF) {
			this.currentState = State.ON;
		}else {
			this.lastReceived = null;
		}
	}
	
	@Override
	public void			switchOffBlanket()
	{
		if(this.currentState != State.OFF) {
			this.currentState = State.OFF;
		}else {
			this.lastReceived = null;
		}
	}
	
	@Override
	public void			setHighTemperture()
	{
		if(this.currentState == State.LOWER_HEATING) {
			this.currentState = State.HIGHER_HEATING;
		}else {
			this.lastReceived = null;
		}
	}
	
	@Override
	public void			setLowTemperture()
	{
		if(this.currentState == State.HIGHER_HEATING) {
			this.currentState = State.LOWER_HEATING;
		}else {
			this.lastReceived = null;
		}
	}
	
	@Override
	public void			doNotHeat()
	{
		if(this.currentState == State.HIGHER_HEATING 
				|| this.currentState == State.LOWER_HEATING) {
			this.currentState = State.ON;
		}else {
			this.lastReceived = null;
		}
	}

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			initialiseState(Time initialTime)
	{
		this.currentState = State.OFF;
		this.lastReceived = null;
		super.initialiseState(initialTime);

		this.toggleDebugMode();
		this.logMessage("simulation begins.\n");
	}
	
	@Override
	public Duration timeAdvance() {
		if (this.lastReceived != null ) {
			return Duration.zero(this.getSimulatedTimeUnit());
		} else {
			return Duration.INFINITY;
		}
	}
	
	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI#output()
	 */
	@Override
	public ArrayList<EventI>	output()
	{
		assert	this.lastReceived != null;
		ArrayList<EventI> ret = new ArrayList<EventI>();
		ret.add(this.lastReceived);
		this.lastReceived = null;
		return ret;
	}
	
	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedExternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedExternalTransition(Duration elapsedTime)
	{
		super.userDefinedExternalTransition(elapsedTime);

		// get the vector of currently received external events
		ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
		// when this method is called, there is at least one external event,
		// and for the current hair dryer model, there must be exactly one by
		// construction.
		assert	currentEvents != null && currentEvents.size() == 1;

		this.lastReceived = (BlanketEventI) currentEvents.get(0);

		// Tracing
		StringBuffer message =
				new StringBuffer("executes an external transition on event ");
		message.append(this.lastReceived.toString());
		message.append(")\n");
		this.logMessage(message.toString());

		// events have a method execute on to perform their effect on this
		// model
		this.lastReceived.executeOn(this);
	}
	
	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#endSimulation(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			endSimulation(Time endTime) throws Exception
	{
		this.logMessage("simulation ends.\n");
		super.endSimulation(endTime);
	}
	
	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation run parameters
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.Model#setSimulationRunParameters(java.util.Map)
	 */
	@Override
	public void			setSimulationRunParameters(
		Map<String, Object> simParams
		) throws Exception
	{
		super.setSimulationRunParameters(simParams);

		// retrieve the reference to the owner component when passed as a
		// simulation run parameter
		if (simParams.containsKey(
							ElectricBlanketRTAtomicSimulatorPlugin.OWNER_RPNAME)) {
			AbstractComponent owner =
					(AbstractComponent) simParams.get(
							ElectricBlanketRTAtomicSimulatorPlugin.OWNER_RPNAME);
			// direct traces on the tracer of the owner component
			this.setLogger(new StandardComponentLogger(owner));
		}
	}
}
