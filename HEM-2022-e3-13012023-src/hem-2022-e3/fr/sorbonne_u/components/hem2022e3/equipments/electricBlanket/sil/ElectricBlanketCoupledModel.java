package fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.devs_simulation.interfaces.ModelDescriptionI;
import fr.sorbonne_u.devs_simulation.models.CoupledModel;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.models.events.ReexportedEvent;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;

/**
* The class <code>BlanketCoupledModel</code> defines a simple coupled
* model used to assemble the models defined for the electric blanket in order to
* execute unit tests on the electric blanket simulator.
*
* <p><strong>Description</strong></p>
* 
* <p><strong>Invariant</strong></p>
* @author Liuyi CHEN & Hongyu YAN
* @date 9 nov. 2022
*/
public class ElectricBlanketCoupledModel 
extends CoupledModel 
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** URI for a model; works when only one instance is created.			*/
	public static final String	URI = ElectricBlanketCoupledModel.class.
																getSimpleName();
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------
	/**
	 *creating the coupled model with event exchanges only.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more precondition.
	 * post	{@code true}	// no more postcondition.
	 * </pre>
	 *
	 * @param uri				URI of the coupled model to be created.
	 * @param simulatedTimeUnit	time unit used in the simulation by the model.
	 * @param simulationEngine	simulation engine enacting the model.
	 * @param submodels			array of submodels of the new coupled model.
	 * @param imported			map from imported event types to submodels consuming them.
	 * @param reexported		map from event types exported by submodels that are reexported by this coupled model.
	 * @param connections		map connecting event sources to arrays of event sinks among submodels.
	 * @throws Exception		<i>to do</i>.
	 */
	public				ElectricBlanketCoupledModel(
			String uri,
			TimeUnit simulatedTimeUnit,
			SimulatorI simulationEngine,
			ModelDescriptionI[] submodels,
			Map<Class<? extends EventI>, EventSink[]> imported,
			Map<Class<? extends EventI>, ReexportedEvent> reexported,
			Map<EventSource, EventSink[]> connections
			) throws Exception
		{
			super(uri, simulatedTimeUnit, simulationEngine, submodels,
				  imported, reexported, connections);
		}
}
