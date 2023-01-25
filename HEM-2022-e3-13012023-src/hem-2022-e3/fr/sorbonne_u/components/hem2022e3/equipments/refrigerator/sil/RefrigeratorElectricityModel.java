/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.utils.StandardComponentLogger;
import fr.sorbonne_u.components.hem2022e2.HEM_ReportI;
import fr.sorbonne_u.components.hem2022e2.utils.Electricity;
import fr.sorbonne_u.components.hem2022e3.equipments.meter.sil.ElectricMeterRTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.DoNotRun;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.RefrigeratorEventI;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.Run;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.StartRefrigerator;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.StopRefrigerator;
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
 * The class <code>RefrigeratorElectricityModel</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-03</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
//-----------------------------------------------------------------------------
@ModelExternalEvents(
		imported = {StartRefrigerator.class,
					StopRefrigerator.class,
					Run.class,
					DoNotRun.class})
@ModelExportedVariable(name = "currentIntensity", type = Double.class)
//-----------------------------------------------------------------------------
public class RefrigeratorElectricityModel 
extends AtomicHIOA 
implements RefrigeratorOperationI 
{
	// -------------------------------------------------------------------------
	// Inner classes and types
	// -------------------------------------------------------------------------

	/**
	 * The enumeration <code>State</code> defines the state in which the
	 * refrigerator can be.
	 */
	public static enum	State {
		/** refrigerator is on but not running.									*/
		ON,
		/** refrigerator is on and running.										*/
		RUNNING,
		/** refrigerator is off.													*/
		OFF
	}
	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	private static final long serialVersionUID = 1L;
	/** URI for a model; works when only one instance is created.			*/
	public static final String	URI = RefrigeratorElectricityModel.class.
															getSimpleName();

	/** power of the refrigerator in watts.										*/
	public static double		NOT_RUNNING_POWER = 22.0;
	/** power of the refrigerator in watts.										*/
	public static double		RUNNING_POWER = 190.0;
	/** nominal tension (in Volts) of the refrigerator.							*/
	public static double		TENSION = 220.0;

	/** current state of the refrigerator.										*/
	protected State					currentState = State.OFF;
	/** true when the electricity consumption of the refrigerator has changed
	 *  after executing an external event; the external event changes the
	 *  value of <code>currentState</code> and then an internal transition
	 *  will be triggered by putting through in this variable which will
	 *  update the variable <code>currentIntensity</code>.					*/
	protected boolean				consumptionHasChanged = false;

	/** total consumption of the refrigerator during the simulation in kwh.		*/
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
	 * create a refrigerator MIL model instance.
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
	public RefrigeratorElectricityModel(
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
	public void startRefrigerator() {
		if (this.currentState == State.OFF) {
			this.currentState = State.ON;
			this.consumptionHasChanged = true;
		}
	}

	@Override
	public void stopRefrigerator() {
		if (this.currentState == State.ON ||
				this.currentState == State.RUNNING) {
			this.currentState = State.OFF;
			this.consumptionHasChanged = true;
		}
	}

	@Override
	public void run() {
		if (this.currentState == State.ON) {
			this.currentState = State.RUNNING;
			this.consumptionHasChanged = true;
		}
	}

	@Override
	public void doNotRun() {
		if (this.currentState == State.RUNNING) {
			this.currentState = State.ON;
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

		this.currentState = State.OFF;
		this.consumptionHasChanged = false;
		this.totalConsumption = 0.0;

		this.toggleDebugMode();
		this.logMessage("simulation begins.\n");
	}

	@Override
	public void			initialiseVariables() throws Exception
	{
		super.initialiseVariables();

		// initially, the refrigerator is off, so its consumption is zero.
		this.currentIntensity.initialise(0.0);

		StringBuffer sb = new StringBuffer("new consumption: ");
		sb.append(this.currentIntensity.getValue());
		sb.append(" amperes at ");
		sb.append(this.currentIntensity.getTime());
		sb.append(" seconds.\n");
		this.logMessage(sb.toString());
	}

	@Override
	public ArrayList<EventI>	output()
	{
		return null;
	}

	@Override
	public Duration		timeAdvance()
	{
		if (this.consumptionHasChanged) {
			// When the consumption has changed, an immediate (delay = 0.0)
			// internal transition must be made to update the electricity
			// consumption.
			this.consumptionHasChanged = false;
			return Duration.zero(this.getSimulatedTimeUnit());
		} else {
			// As long as the state does not change, no internal transition
			// is made (delay = infinity).
			return Duration.INFINITY;
		}
	}

	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		super.userDefinedInternalTransition(elapsedTime);

		Time t = this.getCurrentStateTime();
		if (this.currentState == State.ON) {
			this.currentIntensity.setNewValue(
					RefrigeratorElectricityModel.NOT_RUNNING_POWER/
					RefrigeratorElectricityModel.TENSION,
					t);
		} else if (this.currentState == State.RUNNING) {
			this.currentIntensity.setNewValue(
					RefrigeratorElectricityModel.RUNNING_POWER/
					RefrigeratorElectricityModel.TENSION,
					t);
		} else {
			assert	this.currentState == State.OFF;
			this.currentIntensity.setNewValue(0.0, t);
		}

		StringBuffer sb = new StringBuffer("new consumption: ");
		sb.append(this.currentIntensity.getValue());
		sb.append(" amperes at ");
		sb.append(this.currentIntensity.getTime());
		sb.append(" seconds.\n");
		this.logMessage(sb.toString());
	}

	@Override
	public void userDefinedExternalTransition(Duration elapsedTime)
	{
		super.userDefinedExternalTransition(elapsedTime);
		
		// get the vector of current external events
		ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
		// when this method is called, there is at least one external event,
		// and for the refrigerator model, there will be exactly one by
		// construction.
		assert	currentEvents != null && currentEvents.size() == 1;

		Event ce = (Event) currentEvents.get(0);
		assert	ce instanceof RefrigeratorEventI;

		// compute the total consumption for the simulation report.
		this.totalConsumption +=
				Electricity.computeConsumption(
									elapsedTime,
									TENSION*this.currentIntensity.getValue());

		StringBuffer sb = new StringBuffer("execute the external event: ");
		sb.append(ce.eventAsString());
		sb.append(".\n");
		this.logMessage(sb.toString());

		// the next call will update the current state of the refrigerator and if
		// this state has changed, it put the boolean consumptionHasChanged
		// at true, which in turn will trigger an immediate internal transition
		// to update the current intensity of the refrigerator electricity
		// consumption.
		ce.executeOn(this);
	}

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

	/** power of the refrigerator in watts.										*/
	public static final String	NOT_RUNNING_POWER_RUNPNAME = "NOT_RUNNING_POWER";
	/** power of the refrigerator in watts.										*/
	public static final String	RUNNING_POWER_RUNPNAME = "RUNNING_POWER";
	/** nominal tension (in Volts) of the refrigerator.							*/
	public static final String	TENSION_RUNPNAME = "TENSION";

	@Override
	public void			setSimulationRunParameters(
		Map<String, Object> simParams
		) throws Exception
	{
		super.setSimulationRunParameters(simParams);

		String notRunningName =
			ModelI.createRunParameterName(getURI(), NOT_RUNNING_POWER_RUNPNAME);
		if (simParams.containsKey(notRunningName)) {
			NOT_RUNNING_POWER = (double) simParams.get(notRunningName);
		}
		String runningName =
			ModelI.createRunParameterName(getURI(), RUNNING_POWER_RUNPNAME);
		if (simParams.containsKey(runningName)) {
			RUNNING_POWER = (double) simParams.get(runningName);
		}
		String tensionName =
			ModelI.createRunParameterName(getURI(), TENSION_RUNPNAME);
		if (simParams.containsKey(tensionName)) {
			TENSION = (double) simParams.get(tensionName);
		}

		// retrieve the reference to the owner component when passed as a
		// simulation run parameter
		if (simParams.containsKey(RefrigeratorRTAtomicSimulatorPlugin.OWNER_RPNAME)) {
			AbstractComponent owner =
					(AbstractComponent) simParams.get(
							RefrigeratorRTAtomicSimulatorPlugin.OWNER_RPNAME);
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
	 * The class <code>RefrigeratorElectricityReport</code> implements the
	 * simulation report for the <code>RefrigeratorElectricityModel</code>.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code true}
	 * </pre>
	 * 
	 */
	public static class		RefrigeratorElectricityReport
	implements	SimulationReportI, HEM_ReportI
	{
		private static final long serialVersionUID = 1L;
		protected String	modelURI;
		protected double	totalConsumption; // in kwh


		public			RefrigeratorElectricityReport(
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

	@Override
	public SimulationReportI	getFinalReport() throws Exception
	{
		return new RefrigeratorElectricityReport(URI, this.totalConsumption);
	}
}
