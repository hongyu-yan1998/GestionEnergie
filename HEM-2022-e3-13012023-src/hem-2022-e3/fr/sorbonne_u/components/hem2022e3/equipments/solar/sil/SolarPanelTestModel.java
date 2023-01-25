package fr.sorbonne_u.components.hem2022e3.equipments.solar.sil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.utils.StandardComponentLogger;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.sil.events.SolarNotProduce;
import fr.sorbonne_u.components.hem2022e3.equipments.solar.sil.events.SolarProduce;
import fr.sorbonne_u.devs_simulation.es.models.AtomicES_Model;
import fr.sorbonne_u.devs_simulation.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

/**
 * The class <code>ScolarPanelTestModel.java</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2022-11-17</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
//-----------------------------------------------------------------------------
@ModelExternalEvents(exported = {SolarProduce.class,
		 					     SolarNotProduce.class})
//-----------------------------------------------------------------------------
public class SolarPanelTestModel 
extends AtomicES_Model 
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** URI for an instance model; works as long as only one instance is
	 *  created.															*/
	public static final String		URI = SolarPanelTestModel.class.
																getSimpleName();

	public double daylightHours;
	public double daylightFreeHours;
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an scolar panel MIL test model instance.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more precondition.
	 * post	{@code true}	// no more postcondition.
	 * </pre>
	 *
	 * @param uri				URI of the model.
	 * @param simulatedTimeUnit	time unit used for the simulation time.
	 * @param simulationEngine	simulation engine to which the model is attached.
	 * @throws Exception		<i>to do.</i>
	 */
	public SolarPanelTestModel(
			String uri, 
			TimeUnit simulatedTimeUnit, 
			SimulatorI simulationEngine) throws Exception 
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
		
		Time t = initialTime;
		this.daylightHours = 6 + Math.random()*6 + Math.random()*6;
		Duration sunshine = new Duration(this.daylightHours, this.getSimulatedTimeUnit());
		t = t.add(sunshine);
		this.scheduleEvent(new SolarProduce(t));
		this.daylightFreeHours = 5 + Math.random()*5;
		Duration noSunshine = new Duration(this.daylightFreeHours, this.getSimulatedTimeUnit());
		t = t.add(noSunshine);
		this.scheduleEvent(new SolarNotProduce(t));
		
		this.nextTimeAdvance = this.timeAdvance();
		this.timeOfNextEvent =
				this.getCurrentStateTime().add(this.getNextTimeAdvance());
		
		this.toggleDebugMode();
		this.logMessage("simulation begins.\n");
	}

	@Override
	public ArrayList<EventI>	output()
	{
		ArrayList<EventI> ret = super.output();

		// tracing
		if (ret != null && ret.size() > 0) {
			StringBuffer sb = new StringBuffer("emits events ");
			Iterator<EventI> iter = ret.iterator();
			EventI e = iter.next();
			while (e != null) {
				sb.append(e.toString());
				if (iter.hasNext()) {
					e = iter.next();
					sb.append(", ");
				} else {
					e = null;
					sb.append('\n');
				}
			}
			this.logMessage(sb.toString());
		}

		return ret;
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
