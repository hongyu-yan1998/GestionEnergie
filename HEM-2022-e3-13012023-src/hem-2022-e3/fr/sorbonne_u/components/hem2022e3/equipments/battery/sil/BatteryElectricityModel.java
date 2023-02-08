/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.battery.sil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.utils.StandardComponentLogger;
import fr.sorbonne_u.components.hem2022e2.utils.Electricity;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.InternalVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;
import fr.sorbonne_u.devs_simulation.utils.Pair;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

/**
 * The class <code>BatteryElectricityModel</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 08/02/2023</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
//-----------------------------------------------------------------------------
@ModelImportedVariable(name = "currentHeaterIntensity",
					   type = Double.class)
@ModelImportedVariable(name = "currentHairDryerIntensity",
					   type = Double.class)
@ModelImportedVariable(name = "currentIndoorGardenIntensity",
					   type = Double.class)
@ModelImportedVariable(name = "currentAirConditionerIntensity",
					   type = Double.class)
@ModelImportedVariable(name = "currentRefrigeratorIntensity",
					   type = Double.class)
@ModelImportedVariable(name = "currentSolarPanelIntensity",
					   type = Double.class)
@ModelExportedVariable(name = "currentStockEnergy",
					   type = Double.class)
//-----------------------------------------------------------------------------
public class BatteryElectricityModel 
extends AtomicHIOA {
	
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** URI for a model; works when only one instance is created.			*/
	public static final String		URI = BatteryElectricityModel.class.
																getSimpleName();
	/** tension of electric circuit for appliances in volts.			 	*/
	public static final double		TENSION = 220.0;

	/** evaluation step for the equation (assumed in hours).				*/
	protected static final double	STEP = 60.0/3600.0;	// 60 seconds
	/** evaluation step as a duration, including the time unit.				*/
	protected final Duration		evaluationStep;
	/** total storage capacity												*/
	protected static final double	CAPACITY = 7000.0;	

	// -------------------------------------------------------------------------
	// HIOA model variables
	// -------------------------------------------------------------------------

	/** current intensity of the heater in amperes.							*/
	@ImportedVariable(type = Double.class)
	protected Value<Double>			currentHeaterIntensity;
	/** current intensity of the heater in amperes.							*/
	@ImportedVariable(type = Double.class)
	protected Value<Double>			currentHairDryerIntensity;
	/** current intensity of the indoor garden in amperes.					*/
	@ImportedVariable(type = Double.class)
	protected Value<Double>			currentIndoorGardenIntensity;
	/** current intensity of the air conditioner in amperes.				*/
	@ImportedVariable(type = Double.class)
	protected Value<Double>			currentAirConditionerIntensity;
	/** current intensity of the refrigerator in amperes.					*/
	@ImportedVariable(type = Double.class)
	protected Value<Double>			currentRefrigeratorIntensity;
	/** current intensity of the solar panel in amperes.					*/
	@ImportedVariable(type = Double.class)
	protected Value<Double>			currentSolarPanelIntensity;

	/** current total intensity in amperes.									*/
	@InternalVariable(type = Double.class)
	protected final Value<Double>	currentIntensity =
												new Value<Double>(this);
	
	/** current energy storage in the battery 								*/
	@ExportedVariable(type = Double.class)
	protected Value<Double>			currentStorage;

	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an <code>ElectricMeterElectricityModel</code> instance.
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
	public				BatteryElectricityModel(
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
	// Methods
	// -------------------------------------------------------------------------
	
	/**
	 * compute the current total intensity.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return the current total intensity of electric consumption.
	 */
	protected double	computeTotalIntensity()
	{
		// simple sum of all incoming intensities
		double i = this.currentSolarPanelIntensity.getValue() -
				  (this.currentHairDryerIntensity.getValue() +
				   this.currentHeaterIntensity.getValue() +
				   this.currentIndoorGardenIntensity.getValue() +
				   this.currentAirConditionerIntensity.getValue() +
				   this.currentRefrigeratorIntensity.getValue());
		return i;
	}
	
	/**
	 * update the total electricity storage in kwh given the current
	 * intensity has been constant for the duration {@code d}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code d != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param d	duration for which the intensity has been maintained.
	 */
	protected void		updateStorage(Duration d)
	{
		double c = this.currentStorage.getValue();
		c += Electricity.computeConsumption(
								d, TENSION*this.currentIntensity.getValue());
		if(c <= CAPACITY) {
			Time t = this.currentStorage.getTime().add(d);
			this.currentStorage.setNewValue(c, t);
		}
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

		this.toggleDebugMode();
		this.logMessage("simulation begins.\n");
	}
	
	/**
	 * @see fr.sorbonne_u.devs_simulation.interfaces.VariableInitialisationI#useFixpointInitialiseVariables()
	 */
	@Override
	public boolean		useFixpointInitialiseVariables() throws Exception
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

		if (!this.currentIntensity.isInitialised() &&
							this.currentHairDryerIntensity.isInitialised() &&
							this.currentHeaterIntensity.isInitialised() &&
							this.currentIndoorGardenIntensity.isInitialised() &&
							this.currentAirConditionerIntensity.isInitialised() &&
							this.currentRefrigeratorIntensity.isInitialised() &&
							this.currentSolarPanelIntensity.isInitialised()) {
			double ic = this.computeTotalIntensity();
			this.currentIntensity.initialise(ic);
			this.currentStorage.initialise(0.0);
			justInitialised += 2;
		} else if (!this.currentIntensity.isInitialised()) {
			notInitialisedYet += 2;
		}
		
		return new Pair<>(justInitialised, notInitialisedYet);
	}
	
	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI#output()
	 */
	@Override
	public ArrayList<EventI>	output()
	{
		// The model does not export any event.
		return null;
	}
	
	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
	 */
	@Override
	public Duration		timeAdvance()
	{
		// trigger a new internal transition at each evaluation step duration
		return this.evaluationStep;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		super.userDefinedInternalTransition(elapsedTime);

		// update the current consumption since the last storage update.
		// must be done before recomputing the instantaneous intensity.
		this.updateStorage(elapsedTime);
		double old_cons = this.currentIntensity.getValue();
		double ic = this.computeTotalIntensity();
		this.currentIntensity.setNewValue(ic, this.getCurrentStateTime());
		if (Math.abs(ic - old_cons) > 0.00000000000000001) {
			this.logMessage("new electricity consumption: " +
							this.currentIntensity + " amperes.\n");
		}
	}
	
	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#endSimulation(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			endSimulation(Time endTime) throws Exception
	{
		this.updateStorage(
						endTime.subtract(this.currentStorage.getTime()));
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
						BatteryRTAtomicSimulatorPlugin.OWNER_RPNAME)) {
			AbstractComponent owner =
					(AbstractComponent) simParams.get(
							BatteryRTAtomicSimulatorPlugin.OWNER_RPNAME);
			// direct traces on the tracer of the owner component
			this.setLogger(new StandardComponentLogger(owner));
		}
	}

}
