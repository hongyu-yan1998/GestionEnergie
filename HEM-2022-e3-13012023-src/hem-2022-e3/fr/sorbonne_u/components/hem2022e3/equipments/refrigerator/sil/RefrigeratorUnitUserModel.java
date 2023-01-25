/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.utils.StandardComponentLogger;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.Refrigerator;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.DoNotRun;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.Run;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.StartRefrigerator;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.sil.events.StopRefrigerator;
import fr.sorbonne_u.devs_simulation.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

/**
 * The class <code>RefrigeratorUnitUserModel</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-03</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
//-----------------------------------------------------------------------------
@ModelExternalEvents(
		exported = {StartRefrigerator.class,
					StopRefrigerator.class,
					Run.class,
					DoNotRun.class})
//-----------------------------------------------------------------------------
public class RefrigeratorUnitUserModel 
extends AtomicModel 
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** URI for a model; works when only one instance is created.			*/
	public static final String	URI = RefrigeratorUnitUserModel.class.
															getSimpleName();

	/** steps in the test scenario.											*/
	protected int	step;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a <code>RefrigeratorUnitUserModel</code> instance.
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
	public RefrigeratorUnitUserModel (
			String uri, 
			TimeUnit simulatedTimeUnit, 
			SimulatorI simulationEngine)
			throws Exception 
	{
		super(uri, simulatedTimeUnit, simulationEngine);
		this.setLogger(new StandardLogger());
	}

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	@Override
	public void			initialiseState(Time initialTime)
	{
		super.initialiseState(initialTime);
		this.step = 1;
		this.toggleDebugMode();
		this.logMessage("simulation begins.\n");
	}
	
	@Override
	public ArrayList<EventI>	output()
	{
		// Simple way to implement a test scenario. Here each step generates
		// an event sent to the other models in the standard order.
		if (this.step > 0 && this.step < 5) {
			ArrayList<EventI> ret = new ArrayList<EventI>();
			switch (this.step) {
			case 1:
				this.logMessage(this.uri + " turns on the refrigerator at " +
						this.getTimeOfNextEvent() + ".\n");
				ret.add(new StartRefrigerator(this.getTimeOfNextEvent()));
				break;
			case 2:
				this.logMessage(this.uri + " starts running at " +
						this.getTimeOfNextEvent() + ".\n");
				ret.add(new Run(this.getTimeOfNextEvent()));
				break;
			case 3:
				this.logMessage(this.uri + " stops running at " +
						this.getTimeOfNextEvent() + ".\n");
				ret.add(new DoNotRun(this.getTimeOfNextEvent()));
				break;
			case 4:
				this.logMessage(this.uri + " turns ffon the refrigerator at " +
						this.getTimeOfNextEvent() + ".\n");
				ret.add(new StopRefrigerator(this.getTimeOfNextEvent()));
				break;
			}
			return ret;
		} else {
			return null;
		}
	}
	
	@Override
	public Duration		timeAdvance()
	{
		// As long as events have to be created and sent, the next internal
		// transition is set at one second later, otherwise, no more internal
		// transitions are triggered (delay = infinity).
		if (this.step < 5) {
			return new Duration(1.0, this.getSimulatedTimeUnit());
		} else {
			return Duration.INFINITY;
		}
	}

	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		super.userDefinedInternalTransition(elapsedTime);

		// advance to the next step in the scenario
		this.step++;
	}

	@Override
	public void			endSimulation(Time endTime) throws Exception
	{
		this.logMessage("simulation ends.\n");
		super.endSimulation(endTime);
	}
	
	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------

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
			this.setLogger(new StandardComponentLogger(owner));
		}
	}
	
	@Override
	public SimulationReportI	getFinalReport() throws Exception
	{
		return null;
	}
}
