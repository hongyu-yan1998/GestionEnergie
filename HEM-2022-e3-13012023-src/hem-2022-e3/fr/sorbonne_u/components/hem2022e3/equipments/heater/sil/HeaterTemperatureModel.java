package fr.sorbonne_u.components.hem2022e3.equipments.heater.sil;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a basic
// household management systems as an example of a cyber-physical system.
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

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.utils.StandardComponentLogger;
import fr.sorbonne_u.components.hem2022e2.HEM_ReportI;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.ThermostatedHeater;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.events.DoNotHeat;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.events.Heat;
import fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.events.HeaterEventI;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.InternalVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.DerivableValue;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;
import fr.sorbonne_u.devs_simulation.utils.Pair;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

// -----------------------------------------------------------------------------
/**
 * The class <code>HeaterTemperatureModel</code> defines a simulation model
 * for the temperature inside a room equipped with a heater.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The model is implemented as an atomic HIOA model with differential
 * equation. The differential equation defines the temperature variation
 * over time. It uses a very simple mathematical model where the derivative
 * is proportional to the difference between the current temperature and the
 * temperature that influences the current one. In fact, there are two
 * temperatures that influences the current temperature of the room:
 * </p>
 * <ol>
 * <li>the temperature outside the house (room) where the coefficient
 *   applied to the difference between the outside temperature and the
 *   current temperature models the thermal insulation of the walls
 *   ({@code INSULATION_TRANSFER_CONSTANT});</li>
 * <li>the temperature of the heater when it heats where the coefficient
 *   applied to the difference between the heater temperature
 *   ({@code STANDARD_HEATING_TEMP}) and the current temperature models the
 *   heat diffusion over the house (room)
 *   ({@code HEATING_TRANSFER_CONSTANT}).</li>
 * </ol>
 * <p>
 * The resulting differential equation is integrated using the Euler method
 * with a predefined integration step. The initial state of the model is
 * a state not heating and the initial temperature given by
 * {@code INITIAL_TEMPERATURE}.
 * </p>
 * <p>
 * Whether the current temperature evolves under the influence of the outside
 * temperature only or also the heating temperature depends upon the state,
 * which in turn is modified through the reception of imported events
 * {@code Heat} and {@code DoNotHeat}. The external temperature is imported
 * from another model simulating the environment. The current temperature is
 * exported to be used by other models.
 * </p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code STEP > 0.0}
 * invariant	{@code INSULATION_TRANSFER_CONSTANT > 0.0}
 * invariant	{@code HEATING_TRANSFER_CONSTANT > 0.0}
 * invariant	{@code STANDARD_HEATING_TEMP > 0.0}
 * invariant	{@code INSULATION_TRANSFER_CONSTANT > 0.0}
 * invariant	{@code integrationStep.greaterThan(Duration.zero(getSimulatedTimeUnit()))}
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code URI != null && !URI.isEmpty()}
 * </pre>
 * 
 * <p>Created on : 2021-09-23</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
// -----------------------------------------------------------------------------
@ModelExternalEvents(imported = {Heat.class, DoNotHeat.class})
@ModelImportedVariable(name = "externalTemperature", type = Double.class)
// -----------------------------------------------------------------------------
public class			HeaterTemperatureModel
extends		AtomicHIOA
implements	HeaterOperationI
{
	// -------------------------------------------------------------------------
	// Inner classes and types
	// -------------------------------------------------------------------------

	/**
	 * The enumeration <code>State</code> defines the states in which the
	 * heater can be.
	 *
	 * <p>Created on : 2021-09-24</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static enum	State {
		/** heater is heating.												*/
		HEATING,
		/** heater is not heating.											*/
		NOT_HEATING
	}

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long		serialVersionUID = 1L;
	/** URI for a model; works when only one instance is created.			*/
	public static final String		URI = HeaterTemperatureModel.class.
															getSimpleName();
	public static final double		INITIAL_TEMPERATURE = 19.5;

	/** wall insulation heat transfer constant in the differential equation.*/
	protected final double 			INSULATION_TRANSFER_CONSTANT = 35.0;
	/** heating transfer constant in the differential equation.				*/
	protected final double			HEATING_TRANSFER_CONSTANT = 150.0;
	/** temperature of the heating plate in the heater.						*/
	protected final double			STANDARD_HEATING_TEMP = 300.0;
	/** integration step for the differential equation(assumed in hours).	*/
	protected static final double	STEP = 60.0/3600.0;	// 60 seconds
	/** integration step as a duration, including the time unit.			*/
	protected final Duration		integrationStep;

	/** current external temperature in Celsius.							*/
	/** current state of the heater.										*/
	protected State					currentState;

	/** accumulator to compute the mean external temperature for the
	 *  simulation report.													*/
	protected double				temperatureAcc;
	/** the simulation time of start used to compute the mean temperature.	*/
	protected Time					start;
	/** the mean temperature over the simulation duration for the simulation
	 *  report.																*/
	protected double				meanTemperature;

	// -------------------------------------------------------------------------
	// HIOA model variables
	// -------------------------------------------------------------------------

	/** outdoor temperature.											 	*/
	@ImportedVariable(type = Double.class)
	protected Value<Double>					externalTemperature;
	/** current temperature in the room.									*/
	@InternalVariable(type = Double.class)
	protected final DerivableValue<Double>	currentTemperature =
											new DerivableValue<Double>(this);

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a <code>HeaterTemperatureModel</code> instance.
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
	public				HeaterTemperatureModel(
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

	/**
	 * @see fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.HeaterOperationI#heat()
	 */
	@Override
	public void			heat()
	{
		this.currentState = State.HEATING;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e3.equipments.heater.sil.HeaterOperationI#doNotHeat()
	 */
	@Override
	public void			doNotHeat()
	{
		this.currentState = State.NOT_HEATING;
	}

	/**
	 * return the current temperature as computed by the model.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the current temperature as computed by the model.
	 */
	public double		getCurrentTemperature()
	{
		return this.currentTemperature.evaluateAt(this.getCurrentStateTime());
	}

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			initialiseState(Time initialTime)
	{
		this.temperatureAcc = 0.0;
		this.start = initialTime;
		this.currentState = State.NOT_HEATING;

		this.toggleDebugMode();
		this.logMessage("simulation begins.\n");

		super.initialiseState(initialTime);
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.interfaces.VariableInitialisationI#useFixpointInitialiseVariables()
	 */
	@Override
	public boolean		useFixpointInitialiseVariables()
	{
		return true;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.interfaces.VariableInitialisationI#fixpointInitialiseVariables()
	 */
	@Override
	public Pair<Integer, Integer>	fixpointInitialiseVariables()
	throws Exception
	{
		int justInitialised = 0;
		int notInitialisedYet = 0;

		if (!this.currentTemperature.isInitialised() &&
									this.externalTemperature.isInitialised()) {
			// compute the derivative given the initial temperature of the room
			double derivative = this.computeDerivatives(INITIAL_TEMPERATURE);
			// initialise the model exported variable
			this.currentTemperature.initialise(INITIAL_TEMPERATURE, derivative);
			// one model variable has been initialised (over one existing)
			justInitialised++;
		} else if (!this.currentTemperature.isInitialised()) {
			// one model variable has *not* been initialised (over one existing)
			notInitialisedYet++;
		}
		// return the number initialised and the number not initialised; the
		// method will be called until it returns the pair (1, 0) or (0, 0)
		// hence stopping the fix point computation
		return new Pair<>(justInitialised, notInitialisedYet);
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI#output()
	 */
	@Override
	public ArrayList<EventI>	output()
	{
		return null;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
	 */
	@Override
	public Duration		timeAdvance()
	{
		return this.integrationStep;
	}

	/**
	 * compute the current derivative of the room temperature.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param current	current temperature of the room.
	 * @return			the current derivative.
	 */
	protected double	computeDerivatives(Double current)
	{
		double currentTempDerivative = 0.0;
		if (this.currentState == State.HEATING) {
			// the heating contribution: temperature difference between the
			// heating temperature and the room temperature divided by the
			// heat transfer constant takinbg into account the size of the
			// room
			currentTempDerivative =
					(STANDARD_HEATING_TEMP - current)/
												HEATING_TRANSFER_CONSTANT;
		}
		// the cooling contribution: difference between the external temperature
		// and the temperature of the room divided by the insulation transfer
		// constant taking into account the surface of the walls.
		Time t = this.getCurrentStateTime();
		currentTempDerivative +=
				(this.externalTemperature.evaluateAt(t) - current)/
												INSULATION_TRANSFER_CONSTANT;
		return currentTempDerivative;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		Time t = this.currentTemperature.getTime();
		double oldTemp = this.currentTemperature.evaluateAt(t);
		// accumulate the temperature*time to compute the mean temperature
		this.temperatureAcc += oldTemp * elapsedTime.getSimulatedDuration();

		// update the room temperature using the Euler integration of the
		// differential equation
		double oldDerivative = this.currentTemperature.getFirstDerivative();
		double newTemp = oldTemp + oldDerivative*STEP;
		double newDerivative = this.computeDerivatives(newTemp);
		this.currentTemperature.setNewValue(newTemp,
											newDerivative,
											this.getCurrentStateTime());

		// Tracing
		String mark = (this.currentState == State.HEATING ? " (h)" : " (-)");
		StringBuffer message = new StringBuffer(mark);
		message.append(" room temperature: ");
		message.append(this.currentTemperature.getValue());
		message.append(" at ");
		message.append(this.currentTemperature.getTime());
		message.append('\n');
		this.logMessage(message.toString());

		super.userDefinedInternalTransition(elapsedTime);
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedExternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedExternalTransition(Duration elapsedTime)
	{
		// get the vector of current external events
		ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
		// when this method is called, there is at least one external event,
		// and for the heater model, there will be exactly one by
		// construction.
		assert	currentEvents != null && currentEvents.size() == 1;

		Event ce = (Event) currentEvents.get(0);
		assert	ce instanceof HeaterEventI;
		assert	ce instanceof Heat || ce instanceof DoNotHeat;

		StringBuffer sb = new StringBuffer("executing the external event: ");
		sb.append(ce.eventAsString());
		sb.append(".\n");
		this.logMessage(sb.toString());

		// the next call will update the current state of the heater and if
		// this state has changed, it will toggle the boolean
		// consumptionHasChanged, which in turn will trigger an immediate
		// internal transition to update the current intensity of the
		// heater electricity consumption.
		ce.executeOn(this);

		super.userDefinedExternalTransition(elapsedTime);
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#endSimulation(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			endSimulation(Time endTime) throws Exception
	{
		this.meanTemperature =
				this.temperatureAcc/
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
		if (simParams.containsKey(HeaterRTAtomicSimulatorPlugin.OWNER_RPNAME)) {
			AbstractComponent owner =
					(ThermostatedHeater) simParams.get(
								HeaterRTAtomicSimulatorPlugin.OWNER_RPNAME);
			// direct traces on the tracer of the owner component
			this.setLogger(new StandardComponentLogger(owner));
		}
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------

	/**
	 * The class <code>HeaterTemperatureReport</code> implements the
	 * simulation report for the <code>HeaterTemperatureModel</code>.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code true}
	 * </pre>
	 * 
	 * <p>Created on : 2021-10-01</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static class		HeaterTemperatureReport
	implements	SimulationReportI, HEM_ReportI
	{
		private static final long serialVersionUID = 1L;
		protected String	modelURI;
		protected double	meanTemperature;

		public			HeaterTemperatureReport(
			String modelURI,
			double meanTemperature
			)
		{
			super();
			this.modelURI = modelURI;
			this.meanTemperature = meanTemperature;
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
			ret.append("mean temperature = ");
			ret.append(this.meanTemperature);
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
		return new HeaterTemperatureReport(URI, this.meanTemperature);
	}
}
// -----------------------------------------------------------------------------
