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
import fr.sorbonne_u.components.hem2022e3.equipments.heater.ThermostatedHeater;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

// -----------------------------------------------------------------------------
/**
 * The class <code>ExternalTemperatureModel</code> defines a simulation model
 * for the environment, namely the external temperature of the house.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The model makes the temperature vary over some period representing typically
 * a day. The variation is taken as a cosine between {@code Math.PI} and
 * {@code 3*Math.PI}. The cosine (plus 1 and divided by 2 to vary between 0 and
 * 1) is taken as a coefficient applied to the maximal variation over a day and
 * then added to the minimal temperature to get the current temperature.
 * </p>
 * <p>
 * The model reevaluates the external temperature at some predefined rate i.e.,
 * the evaluation step. It exports this temperature in a variable called
 * {@code externalTemperature}.
 * </p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code STEP > 0.0}
 * invariant	{@code evaluationStep.greaterThan(Duration.zero(getSimulatedTimeUnit()))}
 * invariant	{@code cycleTime >= 0.0 && cycleTime <= PERIOD}
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code MAX_EXTERNAL_TEMPERATURE > MIN_EXTERNAL_TEMPERATURE}
 * invariant	{@code PERIOD > 0.0}
 * </pre>
 * 
 * <p>Created on : 2021-09-23</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@ModelExportedVariable(name = "externalTemperature", type = Double.class)
//-----------------------------------------------------------------------------
public class			ExternalTemperatureModel
extends		AtomicHIOA
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** URI for a model; works when only one instance is created.			*/
	public static final String		URI = ExternalTemperatureModel.class.
															getSimpleName();

	/** minimal external temperature.										*/
	public static final double		MIN_EXTERNAL_TEMPERATURE = -5.0;
	/** maximal external temperature.										*/
	public static final double		MAX_EXTERNAL_TEMPERATURE = 15.0;
	/** period of the temperature variation cycle (day); the cycle begins
	 *  at the minimal temperature and ends at the same temperature.		*/
	public static final double		PERIOD = 24.0;

	/** evaluation step for the equation (assumed in hours).				*/
	protected static final double	STEP = 60.0/3600.0;	// 60 seconds
	/** evaluation step as a duration, including the time unit.				*/
	protected final Duration		evaluationStep;

	protected double				cycleTime;

	// -------------------------------------------------------------------------
	// HIOA model variables
	// -------------------------------------------------------------------------

	/** current external temperature in Celsius.							*/
	@ExportedVariable(type = Double.class)
	protected final Value<Double>	externalTemperature =
												new Value<Double>(this);

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an external temperature MIL model instance.
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
	public				ExternalTemperatureModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		SimulatorI simulationEngine
		) throws Exception
	{
		super(uri, simulatedTimeUnit, simulationEngine);
		this.evaluationStep = new Duration(STEP, this.getSimulatedTimeUnit());
		this.setLogger(new StandardLogger());
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
		super.initialiseState(initialTime);

		this.cycleTime = 0.0;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#initialiseVariables()
	 */
	@Override
	public void			initialiseVariables() throws Exception
	{
		super.initialiseVariables();

		this.externalTemperature.initialise(MIN_EXTERNAL_TEMPERATURE);

		this.toggleDebugMode();
		this.logMessage("simulation begins.\n");
		StringBuffer message =
				new StringBuffer("current external temperature: ");
		message.append(this.externalTemperature.getValue());
		message.append(" at ");
		message.append(this.getCurrentStateTime());
		message.append("\n");
		this.logMessage(message.toString());
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI#output()
	 */
	@Override
	public ArrayList<EventI>	output()
	{
		// the model does not export any event.
		return null;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
	 */
	@Override
	public Duration		timeAdvance()
	{
		// the model makes an internal transition every evaluation step
		// duration
		return this.evaluationStep;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		super.userDefinedInternalTransition(elapsedTime);

		// compute the current time in the cycle
		this.cycleTime += elapsedTime.getSimulatedDuration();
		if (this.cycleTime > PERIOD) {
			this.cycleTime -= PERIOD;
		}
		// compute the new temperature
		double c = Math.cos((1.0 + this.cycleTime/(PERIOD/2.0))*Math.PI);
		double newTemp =
				MIN_EXTERNAL_TEMPERATURE +
					(MAX_EXTERNAL_TEMPERATURE - MIN_EXTERNAL_TEMPERATURE)*
																((1.0 + c)/2.0);
		this.externalTemperature.setNewValue(newTemp,
											 this.getCurrentStateTime());

		// Tracing
		StringBuffer message =
				new StringBuffer("external temperature: ");
		message.append(this.externalTemperature.getValue());
		message.append(" at ");
		message.append(this.getCurrentStateTime());
		message.append("\n");
		this.logMessage(message.toString());
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
	 * @see fr.sorbonne_u.devs_simulation.models.Model#getFinalReport()
	 */
	@Override
	public SimulationReportI	getFinalReport() throws Exception
	{
		// no report
		return null;
	}
}
// -----------------------------------------------------------------------------
