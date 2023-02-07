package fr.sorbonne_u.components.hem2022e3.equipments.solar.sil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.hem2022e3.equipments.solar.sil.events.SolarProduce;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.utils.StandardComponentLogger;
import fr.sorbonne_u.components.hem2022e3.equipments.meter.sil.ElectricMeterRTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.sil.SolarPanelStateModel.State;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.sil.events.SolarNotProduce;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

/**
 * The class <code>SolarPanelElectricityModel.java</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2022-11-16</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
//-----------------------------------------------------------------------------
@ModelExternalEvents(imported = {SolarProduce.class,
								 SolarNotProduce.class})
@ModelExportedVariable(name = "currentIntensity", type = Double.class)
//-----------------------------------------------------------------------------
public class SolarPanelElectricityModel 
extends AtomicHIOA 
implements SolarPanelOperationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** URI for a model; works when only one instance is created.			*/
	public static final String	URI = SolarPanelElectricityModel.class.
															getSimpleName();

	/** power of the scolar panel in watts.										*/
	public static double		SCOLAR_POWER = 200.0;
	/** conversion efficiency of the solar panel 								*/
	public static double 		SCOLAR_EFFICIENCY = 0.75;
	/** current state.														*/
	protected State					currentState = State.OFF;
	
	protected boolean				productionHasChanged = false;
	/** total production of the scolar panel during the simulation in kwh.		*/
	protected double				totalProduction;
	
	// -------------------------------------------------------------------------
	// HIOA model variables
	// -------------------------------------------------------------------------

	/** current intensity in amperes; intensity is power/tension.			*/
	@ExportedVariable(type = Double.class)
	protected final Value<Double>	currentIntensity = new Value<Double>(this);

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	public SolarPanelElectricityModel(
			String uri, 
			TimeUnit simulatedTimeUnit, 
			SimulatorI simulationEngine)
			throws Exception {
		super(uri, simulatedTimeUnit, simulationEngine);
		this.setLogger(new StandardLogger());
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	@Override
	public void startProduce() {
		if (this.currentState == State.OFF) {
			this.currentState = State.ON;
			this.toggleProductionHasChanged();
		}
	}

	@Override
	public void stopProduce() {
		if (this.currentState == State.ON) {
			this.currentState = State.OFF;
			this.toggleProductionHasChanged();
		}		
	}

	/**
	 * toggle the value of the state of the model telling whether the
	 * electricity prudction level has just changed or not; when it changes
	 * after receiving an external event, an immediate internal transition
	 * is triggered to update the level of electricity consumption.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	public void			toggleProductionHasChanged()
	{
		if (this.productionHasChanged) {
			this.productionHasChanged = false;
		} else {
			this.productionHasChanged = true;
		}
	}
	
	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	/**
	 * initialise the state of the model.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more preconditions.
	 * post	{@code State.OFF == this.getState()}
	 * </pre>
	 * 
	 */
	@Override
	public void			initialiseState(Time initialTime)
	{
		super.initialiseState(initialTime);

		this.currentState = State.OFF;
		this.productionHasChanged = false;

		this.toggleDebugMode();
		this.logMessage("simulation begins.\n");
	}

	@Override
	public void			initialiseVariables() throws Exception
	{
		super.initialiseVariables();
		this.currentIntensity.initialise(0.0);
	}

	@Override
	public ArrayList<EventI>	output()
	{
		return null;
	}

	@Override
	public Duration		timeAdvance()
	{
		if (this.productionHasChanged) {
			this.toggleProductionHasChanged();
			return new Duration(0.0, this.getSimulatedTimeUnit());
		} else {
			return Duration.INFINITY;
		}
	}

	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		super.userDefinedInternalTransition(elapsedTime);

		Time t = this.getCurrentStateTime();
		switch (this.currentState)
		{
			case OFF: this.currentIntensity.setNewValue(0.0, t); break;
			case ON:
				this.currentIntensity.
							setNewValue(SCOLAR_POWER * SCOLAR_EFFICIENCY, t);
		}

		// Tracing
		StringBuffer message =
				new StringBuffer("executes an internal transition ");
		message.append("with current production ");
		message.append(this.currentIntensity.getValue());
		message.append(" at ");
		message.append(this.currentIntensity.getTime());
		message.append(".\n");
		this.logMessage(message.toString());
	}

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

		Event ce = (Event) currentEvents.get(0);

		// Tracing
		StringBuffer message =
				new StringBuffer("executes an external transition on event ");
		message.append(ce.toString());
		message.append(")\n");
		this.logMessage(message.toString());

		// events have a method execute on to perform their effect on this
		// model
		ce.executeOn(this);
	}

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
							SolarPanelRTAtomicSimulatorPlugin.OWNER_RPNAME)) {
			AbstractComponent owner =
					(AbstractComponent) simParams.get(
							SolarPanelRTAtomicSimulatorPlugin.OWNER_RPNAME);
			// direct traces on the tracer of the owner component
			this.setLogger(new StandardComponentLogger(owner));
		} else if (simParams.containsKey(
							ElectricMeterRTAtomicSimulatorPlugin.OWNER_RPNAME)) {
			AbstractComponent owner =
					(AbstractComponent) simParams.get(
							ElectricMeterRTAtomicSimulatorPlugin.OWNER_RPNAME);
			// direct traces on the tracer of the owner component
			this.setLogger(new StandardComponentLogger(owner));
		}
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------

	@Override
	public SimulationReportI	getFinalReport() throws Exception
	{
		return null;
	}

}
