package fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.utils.StandardComponentLogger;
import fr.sorbonne_u.components.hem2022e2.HEM_ReportI;
import fr.sorbonne_u.components.hem2022e2.utils.Electricity;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.events.BlanketEventI;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.events.DoNotHeat;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.events.SetHighTemperature;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.events.SetLowTemperature;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.events.SwitchOffBlanket;
import fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil.events.SwitchOnBlanket;
import fr.sorbonne_u.components.hem2022e3.equipments.meter.sil.ElectricMeterRTAtomicSimulatorPlugin;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

/**
* @author Liuyi CHEN & Hongyu YAN
* @date 15 nov. 2022
* @Description 
*/
//-----------------------------------------------------------------------------
@ModelExternalEvents(imported = {SetHighTemperature.class,
								 SetLowTemperature.class,
								 SwitchOnBlanket.class,
								 SwitchOffBlanket.class,
								 DoNotHeat.class})
@ModelExportedVariable(name = "currentIntensity", type = Double.class)
//-----------------------------------------------------------------------------
public class ElectricBlanketElectricityModel 
extends AtomicHIOA
implements ElectricBlanketOperationI
{
	// -------------------------------------------------------------------------
	// Inner classes and types
	// -------------------------------------------------------------------------

	public static enum	BlanketState {
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
	
	private static final long		serialVersionUID = 1L;
	/** URI for a model; works when only one instance is created.			*/
	public static final String		URI = ElectricBlanketElectricityModel.class.
															getSimpleName();
	/** power of the electric blanket in watts.								*/
	public static double			NOT_HEATING_POWER = 10.0;
	/** power of the electric blanket in watts.										*/
	public static double			HEATING_LOWPOWER = 40.0;
	public static double			HEATING_HIGHPOWER = 60.0;
	/** nominal tension (in Volts) of the electric blanket.					*/
	public static double			TENSION = 220.0;
	
	/** current state of ths electric blanket.								*/
	protected BlanketState			currentState = BlanketState.OFF;

	/**
	 * true when the electricity consumption of the electric blanket has changed
	 * after executing an external event; the external event changes the value of
	 * <code>currentState</code> and then an internal transition will be triggered
	 * by putting through in this variable which will update the variable
	 * <code>currentIntensity</code>.
	 */
	protected boolean				consumptionHasChanged = false;
	/** total consumption of the electric blanket during the simulation in kwh.		*/
	protected double				totalConsumption;
	
	// -------------------------------------------------------------------------
	// HIOA model variables
	// -------------------------------------------------------------------------

	/** current intensity in amperes; intensity is power/tension.			*/
	@ExportedVariable(type = Double.class)
	protected final Value<Double>	currentIntensity = new Value<Double>(this);
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a electric blanket MIL model instance.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more precondition
	 * post	{@code true}	// no more postcondition
	 * </pre>
	 *
	 * @param uri				URI of the model.
	 * @param simulatedTimeUnit	time unit used for the simulation time.
	 * @param simulationEngine	simulation engine to which the model is attached.
	 * @throws Exception		<i>to do</i>.
	 */
	public				ElectricBlanketElectricityModel(
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
	public void			switchOnBlanket() {
		if(this.currentState == BlanketState.OFF) {
			this.currentState = BlanketState.ON;
			this.toggleConsumptionHasChanged();
		}
	}
	
	@Override
	public void			switchOffBlanket()
	{
		if(this.currentState != BlanketState.OFF) {
			this.currentState = BlanketState.OFF;
			this.toggleConsumptionHasChanged();
		}
	}
	
	@Override
	public void			setHighTemperture()
	{
		if(this.currentState == BlanketState.LOWER_HEATING) {
			this.currentState = BlanketState.HIGHER_HEATING;
			this.toggleConsumptionHasChanged();
		}
	}
	
	@Override
	public void			setLowTemperture()
	{
		if(this.currentState == BlanketState.HIGHER_HEATING) {
			this.currentState = BlanketState.LOWER_HEATING;
			this.toggleConsumptionHasChanged();
		}
	}
	
	@Override
	public void			doNotHeat()
	{
		if(this.currentState == BlanketState.HIGHER_HEATING 
				|| this.currentState == BlanketState.LOWER_HEATING) {
			this.currentState = BlanketState.ON;
			this.toggleConsumptionHasChanged();
		}
	}
	
	public void			toggleConsumptionHasChanged()
	{
		if (this.consumptionHasChanged) {
			this.consumptionHasChanged = false;
		} else {
			this.consumptionHasChanged = true;
		}
	}
	
	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	@Override
	public void			initialiseState(Time initialTime)
	{
		super.initialiseState(initialTime);

		this.currentState = BlanketState.OFF;
		this.consumptionHasChanged = false;

		this.toggleDebugMode();
		this.logMessage("simulation begins.\n");
	}
	
	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#initialiseVariables()
	 */
	@Override
	public void			initialiseVariables() throws Exception
	{
		super.initialiseVariables();
		this.currentIntensity.initialise(0.0);
	}
	
	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI#output()
	 */
	@Override
	public ArrayList<EventI>	output()
	{
		// the model does not export events.
		return null;
	}
	
	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
	 */
	@Override
	public Duration		timeAdvance()
	{
		// to trigger an internal transition after an external transition, the
		// variable consumptionHasChanged is set to true, hence when it is true
		// return a zero delay otherwise return an infinite delay (no internal
		// transition expected)
		if (this.consumptionHasChanged) {
			// after triggering the internal transition, toggle the boolean
			// to prepare for the next internal transition.
			this.toggleConsumptionHasChanged();
			return new Duration(0.0, this.getSimulatedTimeUnit());
		} else {
			return Duration.INFINITY;
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		super.userDefinedInternalTransition(elapsedTime);

		Time t = this.getCurrentStateTime();
		switch (this.currentState)
		{
		case ON: this.currentIntensity.
						setNewValue(NOT_HEATING_POWER/TENSION, t);
				 break;
		case HIGHER_HEATING:
				 this.currentIntensity.
						setNewValue(HEATING_HIGHPOWER/TENSION, t);
				 break;
		case LOWER_HEATING:
			 	this.currentIntensity.
			 			setNewValue(HEATING_LOWPOWER/TENSION, t);
			 	break;
		case OFF:
				 this.currentIntensity.
						setNewValue(0.0, t);
				 break;
		}
		
		// Tracing
		StringBuffer sb = new StringBuffer("new consumption: ");
		sb.append(this.currentIntensity.getValue());
		sb.append(" amperes at ");
		sb.append(this.currentIntensity.getTime());
		sb.append(" seconds.\n");
		this.logMessage(sb.toString());
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

		Event ce = (Event) currentEvents.get(0);
		assert ce instanceof BlanketEventI;
		
		// compute the total consumption for the simulation report.
		this.totalConsumption +=
				Electricity.computeConsumption(
									elapsedTime,
									TENSION*this.currentIntensity.getValue());


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
	
	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#endSimulation(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			endSimulation(Time endTime) throws Exception
	{
		Duration d = endTime.subtract(this.getCurrentStateTime());
		this.totalConsumption +=
				Electricity.computeConsumption(
									d,
									TENSION*this.currentIntensity.getValue());

		this.logMessage("simulation ends.\n");
		super.endSimulation(endTime);
	}
	
	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation run parameters
	// -------------------------------------------------------------------------

	/** power of the electric blanket in watts.										*/
	public static final String	NOT_HEATING_POWER_RUNPNAME = "NOT_HEATING_POWER";
	/** power of the electric blanket in watts.										*/
	public static final String	LOWER_HEATING_POWER_RUNPNAME = "LOWER_HEATING_POWER";
	public static final String	HIGHER_HEATING_POWER_RUNPNAME = "HIGHER_HEATING_POWER";
	/** nominal tension (in Volts) of the blanket.							*/
	public static final String	TENSION_RUNPNAME = "TENSION";
	
	/**
	 * @see fr.sorbonne_u.devs_simulation.models.Model#setSimulationRunParameters(java.util.Map)
	 */
	@Override
	public void			setSimulationRunParameters(
		Map<String, Object> simParams
		) throws Exception
	{
		super.setSimulationRunParameters(simParams);
		
		String notHeatingName =
				ModelI.createRunParameterName(getURI(), NOT_HEATING_POWER_RUNPNAME);
		if(simParams.containsKey(notHeatingName)) {
			NOT_HEATING_POWER = (double) simParams.get(notHeatingName);
		}
		
		String lowHeatingName =
				ModelI.createRunParameterName(getURI(), LOWER_HEATING_POWER_RUNPNAME);
		if (simParams.containsKey(lowHeatingName)) {
			HEATING_LOWPOWER = (double) simParams.get(lowHeatingName);
		}
		
		String highHeatingName =
				ModelI.createRunParameterName(getURI(), HIGHER_HEATING_POWER_RUNPNAME);
		if (simParams.containsKey(highHeatingName)) {
			HEATING_HIGHPOWER = (double) simParams.get(highHeatingName);
		}
		
		String tensionName =
			ModelI.createRunParameterName(getURI(), TENSION_RUNPNAME);
		if (simParams.containsKey(tensionName)) {
			TENSION = (double) simParams.get(tensionName);
		}
		
		// retrieve the reference to the owner component when passed as a
		// simulation run parameter
		if (simParams.containsKey(ElectricBlanketRTAtomicSimulatorPlugin.OWNER_RPNAME)) {
			AbstractComponent owner =
					(AbstractComponent) simParams.get(
							ElectricBlanketRTAtomicSimulatorPlugin.OWNER_RPNAME);
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

	/**
	 * The class <code>BlanketElectricityReport</code> implements the
	 * simulation report for the <code>blanketElectricityModel</code>.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 */
	public static class		BlanketElectricityReport
	implements SimulationReportI, HEM_ReportI
	{
		private static final long serialVersionUID = 1L;
		protected String	modelURI;
		protected double	totalConsumption; // in kwh
		
		public			BlanketElectricityReport(
			String modelURI,
			double totalConsumption
			)
		{
			super();
			this.modelURI = modelURI;
			this.totalConsumption = totalConsumption;
		}
		
		@Override
		public String	getModelURI()
		{
			return this.modelURI;
		}
		
		@Override
		public String	printout(String indent)
		{
			StringBuffer ret = new StringBuffer(indent);
			ret.append("---\n");
			ret.append(indent);
			ret.append('|');
			ret.append(this.modelURI);
			ret.append(" report\n");
			ret.append(indent);
			ret.append('|');
			ret.append("total consumption in kwh = ");
			ret.append(this.totalConsumption);
			ret.append(".\n");
			ret.append(indent);
			ret.append("---\n");
			return ret.toString();
		}
	}
	
	/**
	 * @see fr.sorbonne_u.devs_simulation.models.Model#getFinalReport()
	 */
	@Override
	public SimulationReportI	getFinalReport() throws Exception
	{
		return new BlanketElectricityReport(URI, this.totalConsumption);
	}

}
