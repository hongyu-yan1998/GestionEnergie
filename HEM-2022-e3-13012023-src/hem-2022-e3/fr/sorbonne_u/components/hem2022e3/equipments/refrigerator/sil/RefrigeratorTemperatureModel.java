/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil;

import fr.sorbonne_u.devs_simulation.hioa.annotations.InternalVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.DerivableValue;
import fr.sorbonne_u.devs_simulation.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.Run;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.utils.StandardComponentLogger;
import fr.sorbonne_u.components.hem2022e2.HEM_ReportI;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.Refrigerator;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.DoNotRun;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.RefrigeratorEventI;

/**
 * The class <code>RefrigeratorTemperatureModel</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-03</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
//-----------------------------------------------------------------------------
@ModelExternalEvents(imported = {Run.class, DoNotRun.class})
//-----------------------------------------------------------------------------
public class RefrigeratorTemperatureModel
extends		AtomicHIOA
implements	RefrigeratorOperationI
{
	// -------------------------------------------------------------------------
	// Inner classes and types
	// -------------------------------------------------------------------------

	/**
	 * The enumeration <code>State</code> defines the states in which the
	 * refrigerator can be.
	 */
	public static enum	State {
		/** refrigerator is running.												*/
		RUNNING,
		/** refrigerator is not running.											*/
		NOT_RUNNING
	}
	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	private static final long		serialVersionUID = 1L;
	/** URI for a model; works when only one instance is created.			*/
	public static final String 		URI = RefrigeratorTemperatureModel.class.
																	getSimpleName();
	public static final double 		INITIAL_FREEZING_TEMPERATURE = 4.0;
	public static final double 		INITIAL_REFRIGERATION_TEMPERATURE = -5.0;
	
//	/** Refrigerator insulation heat transfer constant in the differential equation.*/
//	protected final double 			INSULATION_TRANSFER_CONSTANT = 5000.0;
	/** cooling transfer constant in the differential equation.				*/
	protected final double			FREEZING_TRANSFER_CONSTANT = 5000.0;
	/** temperature of the cooling plate in the refrigerator.						*/
	protected final double			STANDARD_FREEZING_TEMP = 300.0;
	/** cooling transfer constant in the differential equation.				*/
	protected final double			REFRIGERATION_TRANSFER_CONSTANT = 4000.0;
	/** temperature of the cooling plate in the refrigerator.						*/
	protected final double			STANDARD_REFRIGERATION_TEMP = 300.0;
	/** integration step for the differential equation(assumed in seconds).	*/
	protected static final double	STEP = 60.0/3600.0;
	/** integration step as a duration, including the time unit.			*/
	protected final Duration		integrationStep;

	/** current state of the refrigerator.										*/
	protected State					currentState;

	/** accumulator to compute the mean external temperature for the
	 *  simulation report.													*/
	protected double				freezingTemperatureAcc;
	protected double				refrigerationTemperatureAcc;
	/** the simulation time of start used to compute the mean temperature.	*/
	protected Time					start;
	/** the mean freezing temperature over the simulation duration for the simulation
	 *  report.																*/
	protected double				meanFreezingTemperature;
	/** the mean refrigeration temperature over the simulation duration for the simulation
	 *  report.																*/
	protected double				meanRefrigerationTemperature;
	
	// -------------------------------------------------------------------------
	// HIOA model variables
	// -------------------------------------------------------------------------
	
	/** current freezing temperature.									*/
	@InternalVariable(type = Double.class)
	protected final DerivableValue<Double>	currentFreezingTemperature =
											new DerivableValue<Double>(this);
	/** current refrigeration temperature.									*/
	@InternalVariable(type = Double.class)
	protected final DerivableValue<Double>	currentRefrigerationTemperature =
											new DerivableValue<Double>(this);

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a <code>RefrigeratorTemperatureModel</code> instance.
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
	public RefrigeratorTemperatureModel(
			String uri, 
			TimeUnit simulatedTimeUnit, 
			SimulatorI simulationEngine
			) throws Exception 
	{
		super(uri, simulatedTimeUnit, simulationEngine);
		this.integrationStep = new Duration(STEP, simulatedTimeUnit);
		this.setLogger(new StandardLogger());
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	@Override
	public void run() {
		this.currentState = State.RUNNING;
	}

	@Override
	public void doNotRun() {
		this.currentState = State.NOT_RUNNING;
	}
	
	/**
	 * return the current Freezing temperature as computed by the model.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the current Freezing temperature as computed by the model.
	 */
	public double		getCurrentFreezingTemperature()
	{
		return this.currentFreezingTemperature.evaluateAt(this.getCurrentStateTime());
	}
	
	/**
	 * return the current Refrigeration temperature as computed by the model.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the current Refrigeration temperature as computed by the model.
	 */
	public double		getCurrentRefrigerationTemperature()
	{
		return this.currentRefrigerationTemperature.evaluateAt(this.getCurrentStateTime());
	}
	
	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------
	@Override
	public void			initialiseState(Time initialTime)
	{
		this.freezingTemperatureAcc = 0.0;
		this.refrigerationTemperatureAcc = 0.0;
		this.start = initialTime;
		this.currentState = State.NOT_RUNNING;

		this.toggleDebugMode();
		this.logMessage("simulation begins.\n");

		super.initialiseState(initialTime);
	}

	@Override
	public boolean		useFixpointInitialiseVariables()
	{
		return true;
	}

	@Override
	public ArrayList<EventI>	output()
	{
		return null;
	}

	@Override
	public Duration		timeAdvance()
	{
		return this.integrationStep;
	}

	protected double	computeFreezingDerivatives(Double current)
	{
		double currentTempDerivative = 0.0;
		if (this.currentState == State.RUNNING) {
			// the cooling contribution: temperature difference between the
			// cooling temperature and the refrigerator temperature divided by the
			// heat transfer constant takinbg into account the size of the
			// room
			currentTempDerivative = 
					(STANDARD_FREEZING_TEMP - current)/
					FREEZING_TRANSFER_CONSTANT;
		} else {
			currentTempDerivative = 0.2;
		}
		return currentTempDerivative;
	}
	
	protected double	computeRefrigerationDerivatives(Double current)
	{
		double currentTempDerivative = 0.0;
		if (this.currentState == State.RUNNING) {
			// the cooling contribution: temperature difference between the
			// cooling temperature and the refrigerator temperature divided by the
			// heat transfer constant takinbg into account the size of the
			// room
			currentTempDerivative = 
					(STANDARD_REFRIGERATION_TEMP - current)/
					REFRIGERATION_TRANSFER_CONSTANT;
		} else {
			currentTempDerivative = 0.3;
		}
		return currentTempDerivative;
	}
	
	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
//		Time ft = this.currentFreezingTemperature.getTime();
//		double oldFreezingTemp = this.currentFreezingTemperature.evaluateAt(ft);
//		// accumulate the temperature*time to compute the mean temperature
//		this.freezingTemperatureAcc += oldFreezingTemp * elapsedTime.getSimulatedDuration();
//		
//		Time rt = this.currentRefrigerationTemperature.getTime();
//		double oldRefrigerationTemp = this.currentRefrigerationTemperature.evaluateAt(rt);
//		// accumulate the temperature*time to compute the mean temperature
//		this.refrigerationTemperatureAcc += oldRefrigerationTemp * elapsedTime.getSimulatedDuration();
//
//		// update the temperature using the Euler integration of the
//		// differential equation
//		double oldFreezingDerivative = this.currentFreezingTemperature.getFirstDerivative();
//		double newFreezingTemp = oldFreezingTemp + oldFreezingDerivative*STEP;
//		double newFreezingDerivative = this.computeFreezingDerivatives(newFreezingTemp);
//		this.currentFreezingTemperature.setNewValue(newFreezingTemp,
//											newFreezingDerivative,
//											this.getCurrentStateTime());
//		
//		double oldRefrigerationDerivative = this.currentRefrigerationTemperature.getFirstDerivative();
//		double newRefrigerationTemp = oldRefrigerationTemp + oldRefrigerationDerivative*STEP;
//		double newRefrigerationDerivative = this.computeRefrigerationDerivatives(newRefrigerationTemp);
//		this.currentRefrigerationTemperature.setNewValue(newRefrigerationTemp,
//											newRefrigerationDerivative,
//											this.getCurrentStateTime());
		
		if (this.currentState == State.RUNNING) {


		} else {

		}
		
		// Tracing
		String mark = this.currentState == State.RUNNING ? " (h)" : " (-)";
		StringBuffer message = new StringBuffer();
		message.append(this.currentFreezingTemperature.getTime().getSimulatedTime());
		message.append(mark);
		message.append(" : ");
		message.append(this.currentFreezingTemperature.getValue());
		message.append(" | ");
		message.append(this.currentRefrigerationTemperature.getTime().getSimulatedTime());
		message.append(mark);
		message.append(" : ");
		message.append(this.currentRefrigerationTemperature.getValue());
		message.append('\n');
		this.logMessage(message.toString());

		super.userDefinedInternalTransition(elapsedTime);
	}

	@Override
	public void			userDefinedExternalTransition(Duration elapsedTime)
	{
		// get the vector of current external events
		ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
		// when this method is called, there is at least one external event,
		// and for the refrigerator model, there will be exactly one by
		// construction.
		assert	currentEvents != null && currentEvents.size() == 1;

		Event ce = (Event) currentEvents.get(0);
		assert	ce instanceof RefrigeratorEventI;
		assert	ce instanceof Run || ce instanceof DoNotRun;

		StringBuffer sb = new StringBuffer("executing the external event: ");
		sb.append(ce.eventAsString());
		sb.append(".\n");
		this.logMessage(sb.toString());

		// the next call will update the current state of the refrigerator and if
		// this state has changed, it will toggle the boolean
		// consumptionHasChanged, which in turn will trigger an immediate
		// internal transition to update the current intensity of the
		// refrigerator electricity consumption.
		ce.executeOn(this);

		super.userDefinedExternalTransition(elapsedTime);
	}

	@Override
	public void			endSimulation(Time endTime) throws Exception
	{
		this.meanFreezingTemperature =
				this.freezingTemperatureAcc/
						endTime.subtract(this.start).getSimulatedDuration();
		this.meanRefrigerationTemperature =
				this.meanRefrigerationTemperature/
						endTime.subtract(this.start).getSimulatedDuration();

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
		if (simParams.containsKey(RefrigeratorRTAtomicSimulatorPlugin.OWNER_RPNAME)) {
			AbstractComponent owner =
					(Refrigerator) simParams.get(
							RefrigeratorRTAtomicSimulatorPlugin.OWNER_RPNAME);
			// direct traces on the tracer of the owner component
			this.setLogger(new StandardComponentLogger(owner));
		}
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------

	/**
	 * The class <code>RefrigeratorTemperatureReport</code> implements the
	 * simulation report for the <code>RefrigeratorTemperatureModel</code>.
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
	public static class		RefrigeratorTemperatureReport
	implements	SimulationReportI, HEM_ReportI
	{
		private static final long serialVersionUID = 1L;
		protected String	modelURI;
		protected double	meanFreezingTemperature;										
		protected double	meanRefrigerationTemperature;

		public			RefrigeratorTemperatureReport(
			String modelURI,
			double meanFreezingTemperature,
			double meanRefrigerationTemperature
			)
		{
			super();
			this.modelURI = modelURI;
			this.meanFreezingTemperature = meanFreezingTemperature;
			this.meanRefrigerationTemperature = meanRefrigerationTemperature;
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
			ret.append("mean refrigeration temperature = ");
			ret.append(this.meanRefrigerationTemperature);
			ret.append('|');
			ret.append("mean freezing temperature = ");
			ret.append(this.meanFreezingTemperature);
			ret.append(".\n");
			ret.append(indent);
			ret.append("---\n");
			return ret.toString();
		}
	}

	@Override
	public SimulationReportI	getFinalReport() throws Exception
	{
		return new RefrigeratorTemperatureReport(URI, 
				this.meanFreezingTemperature,
				this.meanRefrigerationTemperature);
	}
}
