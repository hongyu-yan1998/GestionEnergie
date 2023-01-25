package fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil;

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

import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.random.RandomDataGenerator;
import fr.sorbonne_u.components.cyphy.plugins.devs.utils.StandardComponentLogger;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.events.SetHighHairDryer;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.events.SetLowHairDryer;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.events.SwitchOffHairDryer;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.events.SwitchOnHairDryer;
import fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.test.HairDryerUser;
import fr.sorbonne_u.devs_simulation.es.events.ES_EventI;
import fr.sorbonne_u.devs_simulation.es.models.AtomicES_Model;
import fr.sorbonne_u.devs_simulation.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import java.util.ArrayList;

// -----------------------------------------------------------------------------
/**
 * The class <code>HairDryerUserModel</code> defines a very simple user
 * model for the hair dryer.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This model is meant to illustrate how to program SIL models simulating user
 * actions by having events executing a call to an owner component.
 * </p>
 * <p>
 * Here, we use an event scheduling atomic model to schedule events at random
 * time intervals in a predefined cycle to test all of the different modes in
 * the hair dryer. Note that the events are indeed subclasses of event
 * scheduling events {@code ES_Event}. Hence, this example also shows how to
 * program this type of event scheduling simulation models.
 * </p>
 * <p>
 * Event scheduling models are constructed around an event list that contains
 * events scheduled to be executed at future time in the simulation time. The
 * class {@code AtomicES_Model} hence defines the main methods to execute
 * the transitions. Internal transitions simply occurs at the time of the next
 * event in the event list and then, if the event is internal, execute that
 * event on the model (by calling the method {@code executeOn} defined on the
 * event. If the next event is external, it must be emitted towards other
 * models. This is performed by the method {@code AtomicES_Model#output}
 * </p>
 * <p>
 * Note that the model here is a bit more flexible hence complicated. As we
 * wanted to be able to execute it in a MIL way, the model switch from a MIL
 * to a SIL execution depending on the value of the {@code owner} variable.
 * If {@code owner} is set, then the model executes the events as internal ones
 * which calls the methods {@code turnOn}, {@code turnOff}, {@code setHigh} and
 * {@code setLow} through their own method {@code executeOn} called by the
 * internal transition of {@code AtomicES_Model}. However, if {@code owner} is
 * not set, then the events are exported towards the {@code HairDryerStateModel}
 * as we would do in a MIL simulation. This switching behaviour is implemented
 * by the {@code output} method and by the methods {@code turnOn},
 * {@code turnOff}, {@code setHigh} and {@code setLow} that call the
 * {@code owner} only when it is not null.
 * </p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code STEP_MEAN_DURATION > 0.0}
 * invariant	{@code DELAY_MEAN_DURATION > 0.0}
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2021-09-20</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@ModelExternalEvents(exported = {SwitchOnHairDryer.class,
								 SwitchOffHairDryer.class,
								 SetLowHairDryer.class,
								 SetHighHairDryer.class})
//-----------------------------------------------------------------------------
public class			HairDryerUserModel
extends		AtomicES_Model
implements	HairDryerOperationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long		serialVersionUID = 1L;
	/** URI for an instance model; works as long as only one instance is
	 *  created.															*/
	public static final String		URI = HairDryerUserModel.class.
																getSimpleName();

	/** time interval between event outputs in hours.						*/
	protected static double			STEP_MEAN_DURATION = 5.0/60.0; // 5 minutes
	/** time interval between hair dryer usages in hours.					*/
	protected static double			DELAY_MEAN_DURATION = 4.0;

	/**	the random number generator from common math library.				*/
	protected final RandomDataGenerator	rg ;
	/** component that owns this model.										*/
	protected HairDryerUser			owner;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a hair dryer user MIL model instance.
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
	public				HairDryerUserModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		SimulatorI simulationEngine
		) throws Exception
	{
		super(uri, simulatedTimeUnit, simulationEngine);
		this.rg = new RandomDataGenerator();
		// set the logger to a standard simulation logger
		this.setLogger(new StandardLogger());
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.HairDryerOperationI#turnOn()
	 */
	@Override
	public void			turnOn()
	{
		this.logMessage(this.uri + " turns on the hair dryer.\n");
		try {
			if (this.owner != null) {
				this.owner.turnOn();
			}
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.HairDryerOperationI#turnOff()
	 */
	@Override
	public void			turnOff()
	{
		this.logMessage(this.uri + " turns off the hair dryer.\n");
		try {
			if (this.owner != null) {
				this.owner.turnOff();
			}
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.HairDryerOperationI#setHigh()
	 */
	@Override
	public void			setHigh()
	{
		this.logMessage(this.uri + " sets the hair dryer HIGH.\n");
		try {
			if (this.owner != null) {
				this.owner.setHigh();
			}
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e3.equipments.hairdryer.sil.HairDryerOperationI#setLow()
	 */
	@Override
	public void			setLow()
	{
		this.logMessage(this.uri + " sets the hair dryer LOW.\n");
		try {
			if (this.owner != null) {
				this.owner.setLow();
			}
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

	/**
	 * generate the next event in the test scenario; current implementation
	 * cycles through {@code SwitchOnHairDryer}, {@code SetHighHairDryer},
	 * {@code SetLowHairDryer} and {@code SwitchOffHairDryer} in this order
	 * at a random time interval following a gaussian distribution with
	 * mean {@code STEP_MEAN_DURATION} and standard deviation
	 * {@code STEP_MEAN_DURATION/2.0}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code eventList.peek() != null}
	 * </pre>
	 *
	 * @param current	current event being executed.
	 */
	protected void		generateNextEvent(ES_EventI current)
	{
		// compute the next event type given the current event
		ES_EventI nextEvent = null;
		if (current instanceof SwitchOffHairDryer) {
			Time t1 = this.computeTimeOfNextUsage(current.getTimeOfOccurrence());
			nextEvent = new SwitchOnHairDryer(t1);
		} else {
			// compute the time of occurrence for the next event
			Time t2 = this.computeTimeOfNextEvent(current.getTimeOfOccurrence());
			if (current instanceof SwitchOnHairDryer) {
				nextEvent = new SetHighHairDryer(t2);
			} else if (current instanceof SetHighHairDryer) {
				nextEvent = new SetLowHairDryer(t2);
			} else if (current instanceof SetLowHairDryer) {
				nextEvent = new SwitchOffHairDryer(t2);
			}
		}
		// schedule the event to be executed by this model
		this.scheduleEvent(nextEvent);
	}

	/**
	 * compute the time of the next event, adding a random delay to
	 * {@code from}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param from	time from which a delay will be added to get the time of the next event.
	 * @return		the time of the next event, adding a random delay to {@code from}.
	 */
	protected Time		computeTimeOfNextEvent(Time from)
	{
		// generate randomly the next time interval but force it to be
		// greater than 0 by returning at least 0.1 
		double delay = Math.max(this.rg.nextGaussian(STEP_MEAN_DURATION,
													 STEP_MEAN_DURATION/2.0),
								0.1);
		// compute the new time by adding the delay to from
		Time t = from.add(new Duration(delay, this.getSimulatedTimeUnit()));
		return t;
	}

	/**
	 * compute the time of the next hair dryer usage, adding a random delay to
	 * {@code from}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code from != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param from	time from which a delay will be added to get the time of the next event.
	 * @return		the time of the next event, adding a random delay to {@code from}.
	 */
	protected Time		computeTimeOfNextUsage(Time from)
	{
		assert	from != null;

		// generate randomly the next time interval but force it to be
		// greater than 0 by returning at least 0.1 
		double delay = Math.max(this.rg.nextGaussian(DELAY_MEAN_DURATION,
													 DELAY_MEAN_DURATION/10.0),
								0.1);
		// compute the new time by adding the delay to from
		Time t = from.add(new Duration(delay, this.getSimulatedTimeUnit()));
		return t;
	}

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			initialiseState(Time initialTime)
	{
		super.initialiseState(initialTime);

		this.rg.reSeedSecure();

		// compute the time of occurrence for the first event
		Time t = this.computeTimeOfNextUsage(this.getCurrentStateTime());
		// schedule the first event
		this.scheduleEvent(new SwitchOnHairDryer(t));
		// re-initialisation of the time of occurrence of the next event
		// required here after adding a new event in the schedule.
		this.nextTimeAdvance = this.timeAdvance();
		this.timeOfNextEvent =
				this.getCurrentStateTime().add(this.getNextTimeAdvance());

		this.toggleDebugMode();
		this.logMessage("simulation begins.\n");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.es.models.AtomicES_Model#output()
	 */
	@Override
	public ArrayList<EventI>	output()
	{
		ArrayList<EventI> ret = null;
		ES_EventI e = this.eventList.peek();
		if (this.owner == null) {
			// when the owner is null, it means that we are in a MIL simulation
			// hence this model must export the next event towards the
			// HairDryerStateModel, otherwise we are in a SIL simulation and
			// this model does not export events but rather force the owner
			// to call the HairDryer component through the execution of events
			// as internal by internalTransition
			ret = super.output();
		}
		if (e != null) {
			// tracing
			StringBuffer sb = new StringBuffer(this.uri);
			sb.append(" executes the event ");
			sb.append(e.eventAsString());
			sb.append(".\n");
			this.logMessage(sb.toString());

			this.generateNextEvent(e);
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

	/** run parameter name for {@code STEP_MEAN_DURATION}.					*/
	public static final String		MEAN_STEP_RPNAME = "STEP_MEAN_DURATION";
	/** run parameter name for {@code STEP_MEAN_DURATION}.					*/
	public static final String		MEAN_DELAY_RPNAME = "STEP_MEAN_DURATION";

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.Model#setSimulationRunParameters(java.util.Map)
	 */
	@Override
	public void			setSimulationRunParameters(
		Map<String, Object> simParams
		) throws Exception
	{
		super.setSimulationRunParameters(simParams);

		String stepName =
				ModelI.createRunParameterName(getURI(), MEAN_STEP_RPNAME);
		if (simParams.containsKey(stepName)) {
			STEP_MEAN_DURATION = (double) simParams.get(stepName);
		}
		String delayName =
				ModelI.createRunParameterName(getURI(), MEAN_DELAY_RPNAME);
		if (simParams.containsKey(delayName)) {
			DELAY_MEAN_DURATION = (double) simParams.get(delayName);
		}

		if (simParams.containsKey(
						HairDryerUserRTAtomicSimulatorPlugin.OWNER_RPNAME)) {
			this.owner = (HairDryerUser) simParams.get(
							HairDryerUserRTAtomicSimulatorPlugin.OWNER_RPNAME);
			// set the logger to one directing logs to the owner component logger
			this.setLogger(new StandardComponentLogger(this.owner));
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
		return null;
	}
}
// -----------------------------------------------------------------------------
