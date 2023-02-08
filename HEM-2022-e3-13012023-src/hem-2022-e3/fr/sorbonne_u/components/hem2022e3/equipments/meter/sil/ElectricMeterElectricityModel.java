package fr.sorbonne_u.components.hem2022e3.equipments.meter.sil;

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
import fr.sorbonne_u.components.hem2022e2.utils.Electricity;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.InternalVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;
import fr.sorbonne_u.devs_simulation.utils.Pair;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

// -----------------------------------------------------------------------------
/**
 * The class <code>ElectricMeterElectricityModel</code> defines the simulation
 * model for the electric meter electricity consumption.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This model is an HIOA model that imports variables, hence shows how this kind
 * of models are programmed.
 * </p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code STEP > 0.0}
 * invariant	{@code evaluationStep.greaterThan(Duration.zero(getSimulatedTimeUnit()))}
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code URI != null && !URI.isEmpty()}
 * invariant	{@code TENSION > 0.0}
 * </pre>
 * 
 * <p>Created on : 2021-09-24</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
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
//-----------------------------------------------------------------------------
public class			ElectricMeterElectricityModel
extends		AtomicHIOA
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** URI for a model; works when only one instance is created.			*/
	public static final String		URI = ElectricMeterElectricityModel.class.
																getSimpleName();
	/** tension of electric circuit for appliances in volts.			 	*/
	public static final double		TENSION = 220.0;

	/** evaluation step for the equation (assumed in hours).				*/
	protected static final double	STEP = 60.0/3600.0;	// 60 seconds
	/** evaluation step as a duration, including the time unit.				*/
	protected final Duration		evaluationStep;

	/** final report of the simulation run.									*/
	protected ElectricMeterElectricityReport	finalReport;

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

	/** current total intensity of the consumption equipments in amperes.	*/
	@InternalVariable(type = Double.class)
	protected final Value<Double>	currentConsIntensity =
												new Value<Double>(this);
	/** current total intensity of the production equipments in amperes.	*/
	@InternalVariable(type = Double.class)
	protected final Value<Double>	currentProdIntensity =
												new Value<Double>(this);
	/** current total consumption of the house in kwh.						*/
	@InternalVariable(type = Double.class)
	protected final Value<Double>	currentConsumption =
												new Value<Double>(this);
	
	/** current total production of the house in kwh.						*/
	@InternalVariable(type = Double.class)
	protected final Value<Double>	currentProduction =
												new Value<Double>(this);

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
	public				ElectricMeterElectricityModel(
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
	 * Calculate the total electricity consumption at the current time
	 * */
	public double computeTotalConsumption() {
		return this.currentConsumption.evaluateAt(this.getCurrentStateTime());
	}
	
	/**
	 * Calculate the total electricity production at the current time
	 * */
	public double computeTotalProduction() {
		return this.currentProduction.evaluateAt(this.getCurrentStateTime());
	}
	
	
	/**
	 * update the total electricity production in kwh given the current
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
	protected void		updateProduction(Duration d)
	{
		double c = this.currentProduction.getValue();
		c += Electricity.computeConsumption(
								d, TENSION*this.currentProdIntensity.getValue());
		Time t = this.currentProduction.getTime().add(d);
		this.currentProduction.setNewValue(c, t);
	}
	
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
	protected double	computeTotalProductionIntensity()
	{
		// simple sum of all incoming intensities
		double i = this.currentSolarPanelIntensity.getValue();
		return i;
	}
	
	/**
	 * update the total electricity consumption in kwh given the current
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
	protected void		updateConsumption(Duration d)
	{
		double c = this.currentConsumption.getValue();
		c += Electricity.computeConsumption(
								d, TENSION*this.currentConsIntensity.getValue());
		Time t = this.currentConsumption.getTime().add(d);
		this.currentConsumption.setNewValue(c, t);
	}

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
	protected double		computeTotalConsumptionIntensity()
	{
		// simple sum of all incoming intensities
		double i = this.currentHairDryerIntensity.getValue() +
				   this.currentHeaterIntensity.getValue() +
				   this.currentIndoorGardenIntensity.getValue() +
				   this.currentAirConditionerIntensity.getValue() +
				   this.currentRefrigeratorIntensity.getValue();
		// Tracing
//		if (this.currentIntensity.isInitialised()) {
//			StringBuffer message = new StringBuffer("current total consumption: ");
//			message.append(this.currentIntensity.getValue());
//			message.append(" at ");
//			message.append(this.getCurrentStateTime());
//			message.append('\n');
//			this.logMessage(message.toString());
//		}

		return i;
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

		if (!this.currentConsIntensity.isInitialised() &&
							this.currentHairDryerIntensity.isInitialised() &&
							this.currentHeaterIntensity.isInitialised() &&
							this.currentIndoorGardenIntensity.isInitialised() &&
							this.currentAirConditionerIntensity.isInitialised() &&
							this.currentRefrigeratorIntensity.isInitialised()) {
			double ic = this.computeTotalConsumptionIntensity();
			this.currentConsIntensity.initialise(ic);
			this.currentConsumption.initialise(0.0);
			justInitialised += 2;
		} else if (!this.currentConsIntensity.isInitialised()) {
			notInitialisedYet += 2;
		}
		
		if(!this.currentProdIntensity.isInitialised() && 
							this.currentSolarPanelIntensity.isInitialised()) {
			double ip = this.computeTotalProductionIntensity();
			this.currentProdIntensity.initialise(ip);
			this.currentProduction.initialise(0.0);
			justInitialised += 2;
		} else if (!this.currentProdIntensity.isInitialised()) {
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

		// update the current consumption since the last consumption update.
		// must be done before recomputing the instantaneous intensity.
		this.updateConsumption(elapsedTime);
		this.updateProduction(elapsedTime);
		// recompute the current total intensity of consumption
		double old_cons = this.currentConsIntensity.getValue();
		double ic = this.computeTotalConsumptionIntensity();
		this.currentConsIntensity.setNewValue(ic, this.getCurrentStateTime());
		if (Math.abs(ic - old_cons) > 0.00000000000000001) {
			this.logMessage("new electricity consumption: " +
							this.currentConsIntensity + " amperes.\n");
		}
		// recompute the current total intensity of production
		double old_prod = this.currentProdIntensity.getValue();
		double ip = this.computeTotalProductionIntensity();
		this.currentProdIntensity.setNewValue(ic, this.getCurrentStateTime());
		if (Math.abs(ip - old_prod) > 0.00000000000000001) {
			this.logMessage("new electricity production: " +
							this.currentProdIntensity + " amperes.\n");
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#endSimulation(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			endSimulation(Time endTime) throws Exception
	{
		this.updateConsumption(
						endTime.subtract(this.currentConsumption.getTime()));
		this.updateProduction(
						endTime.subtract(this.currentProduction.getTime()));
		// must capture the current consumption before the finalisation
		// reinitialise the internal model variable.
		this.finalReport = new ElectricMeterElectricityReport(
											URI,
											this.currentConsumption.getValue(),
											this.currentProduction.getValue());

		this.logMessage("simulation ends.\n");
		super.endSimulation(endTime);
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------

	/**
	 * The class <code>ElectricMeterElectricityReport</code> implements the
	 * simulation report for the <code>ElectricMeterElectricityModel</code>.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code true}	// no invariant
	 * </pre>
	 * 
	 * <p>Created on : 2021-10-01</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static class		ElectricMeterElectricityReport
	implements	SimulationReportI, HEM_ReportI
	{
		private static final long serialVersionUID = 1L;
		protected String	modelURI;
		protected double	totalConsumption; // in kwh
		protected double	totalProduction; // in kwh

		public			ElectricMeterElectricityReport(
			String modelURI,
			double totalConsumption,
			double totalProduction
			)
		{
			super();
			this.modelURI = modelURI;
			this.totalConsumption = totalConsumption;
			this.totalProduction = totalProduction;
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
			ret.append('|');
			ret.append("total production in kwh = ");
			ret.append(this.totalProduction);
			ret.append(".\n");
			ret.append(indent);
			ret.append("---\n");
			return ret.toString();
		}		
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
	 * @see fr.sorbonne_u.devs_simulation.models.Model#getFinalReport()
	 */
	@Override
	public SimulationReportI	getFinalReport() throws Exception
	{
		return this.finalReport;
	}
}
// -----------------------------------------------------------------------------
