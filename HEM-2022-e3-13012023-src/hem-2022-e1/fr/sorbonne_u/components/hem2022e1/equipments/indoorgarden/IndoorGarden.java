package fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden;

// Copyright Jacques Malenfant, Sorbonne Universite.
//
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// new implementation of the DEVS simulation standard for Java.
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

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>IndoorGarden</code> implements the distant control unit of
 * a small indoor garden which turns a light on during the day to help growing
 * the crops.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This component is provided to show a kind of electric equipment that can
 * both be planned and suspended. However, the plan here is a bit different
 * from plans in other equipments like washing machines where a plan can be
 * postponed but then it will still be fully completed at a later time. The
 * indoor garden can postpone the beginning of its plan, but then the plan will
 * be shortened as the end of the plan will remain the same. Also, the plan is
 * meant to ne repeated each day, another difference from a plan for a
 * washing machine.
 * </p>
 * <p>
 * The indoor garden is implemented in such a way that when switched on, the
 * light is off. Switching the light on and off is done automatically through
 * the plan defining the time at which it must be switched on and the time at
 * which it must be switched off. Hence, the light will not be operative until
 * a plan is defined.
 * </p>
 * <p>
 * As the indoor garden needs an access to the current real time to operate
 * properly, a simulated mode if provided that allows to set the current time
 * arbitrarily.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// TODO
 * </pre>
 * 
 * <p>Created on : 2022-09-14</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@OfferedInterfaces(offered = {IndoorGardenCI.class})
public class			IndoorGarden
extends		AbstractComponent
implements	IndoorGardenImplementationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** a convenient string to be used as inbound port URI prefix.			*/
	public static final String	INBOUND_PORT_URI_PREFIX = "indoor-garden-ibp";
	/** when true, all methods trace their actions.								*/
	public static final boolean		DEBUG = false;
	/** when true, methods trace their actions.								*/
	public static final boolean		VERBOSE = true;

	/** the inbound port of the component.									*/
	protected IndoorGardenInboundPort	inboundPort;

	/** if true, the indoor garden is on, otherwise it is off.				*/
	protected boolean		on;
	/** if true, the indoor garden light is on, otherwise it is off.		*/
	protected boolean		lightOn;
	/** if true, the indoor garden is deactivated, otherwise it is
	 *  activated.															*/
	protected boolean		deactivated;
	/** if the lighting plan has been defined, the time at which the light
	 *  must be switched on.												*/
	protected Instant		timeToSwitchLightOn;
	/** if the lighting plan has been defined, the time at which the light
	 *  must be switched off.												*/
	protected Instant		timeToSwitchLightOff;

	/** true if the component must be execute in simulated mode.			*/
	protected boolean		isSimulated;
	/** for the time being, represents the current time to test the
	 *  component.															*/
	protected Instant		simulatedNow;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a component instance.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code !isOn()}
	 * </pre>
	 * 
	 * @param isSimulated	if true, execute the component in simulated mode.
	 * @throws Exception	<i>to do</i>.
	 */
	protected			IndoorGarden(boolean isSimulated) throws Exception
	{
		super(1, 1);

		this.on = false;
		this.isSimulated = isSimulated;
		this.lightOn = false;
		this.deactivated = false;

		this.inboundPort = new IndoorGardenInboundPort(
									IndoorGarden.INBOUND_PORT_URI_PREFIX, this);
		this.inboundPort.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Indoor garden component");
			this.tracer.get().setRelativePosition(1, 2);
			this.toggleTracing();		
		}

		assert	!this.isOn();
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.inboundPort.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#isOn()
	 */
	@Override
	public boolean		isOn() throws Exception
	{
		if (DEBUG) {
			this.traceMessage("Indoor garden is on ? " + this.on + ".\n");
		}

		return this.on;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#on()
	 */
	@Override
	public void			on() throws Exception
	{
		assert	!isOn() : new PreconditionException("!isOn()");

		if (DEBUG || VERBOSE) {
			this.traceMessage("Indoor garden is switched on.\n");
		}

		this.on = true;
		this.lightOn = false;
		this.deactivated = false;
		this.timeToSwitchLightOn = null;
		this.timeToSwitchLightOff = null;

		assert	isOn() : new PostconditionException("isOn()");
		assert	!isLightOn() : new PostconditionException("!isLightOn()");
		assert	!isLightPlanned() :
				new PostconditionException("!isLightPlanned()");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#off()
	 */
	@Override
	public void			off() throws Exception
	{
		assert isOn() : new PreconditionException("isOn()");

		if (DEBUG || VERBOSE) {
			this.traceMessage("Indoor garden is switched off.\n");
		}

		this.on = false;
		this.lightOn = false;
		this.deactivated = false;
		this.timeToSwitchLightOn = null;
		this.timeToSwitchLightOff = null;

		assert !isOn() : new PostconditionException("!isOn()");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#isLightPlanned()
	 */
	@Override
	public boolean		isLightPlanned() throws Exception
	{
		assert	isOn() : new PreconditionException("isOn()");

		if (DEBUG) {
			this.traceMessage("Indoor garden is light planned " + 
							  (this.timeToSwitchLightOn != null &&
									this.timeToSwitchLightOff != null) + ".\n");
		}

		return this.timeToSwitchLightOn != null &&
									this.timeToSwitchLightOff != null;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#planLight(java.time.Instant, java.time.Instant)
	 */
	@Override
	public void			planLight(Instant timeOn, Instant timeOff)
	throws Exception
	{
		assert	isOn() : new PreconditionException("isOn()");
		assert	!isLightPlanned() :
				new PreconditionException("!isLightPlanned()");
		assert	timeOn != null : new PreconditionException("timeOn != null");
		assert	timeOff != null : new PreconditionException("timeOff != null");
		assert	timeOn.isBefore(timeOff) :
				new PreconditionException("timeOff != null");

		if (DEBUG || VERBOSE) {
			this.traceMessage("Indoor garden plans light from " + timeOn +
							  " to " + timeOff + " at " + this.getCurrentTime()
							  + ".\n");
		}

		this.timeToSwitchLightOn = timeOn;
		this.timeToSwitchLightOff = timeOff;

		if (this.currentPlanActive()) {
			this.switchLightOn();
		}
		if (!this.isSimulated) {
			this.scheduleProgram();
		}

		assert	isLightPlanned() :
				new PostconditionException("isLightPlanned()");
		assert	!isDeactivated() :
				new PostconditionException("!isDeactivated()");
		assert	!currentlyActive(timeOn, timeOff) || isLightOn() :
				new PostconditionException(
						"!currentlyActive(timeOn, timeOff)  || isLightOn()");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#cancelPlanLight()
	 */
	@Override
	public void			cancelPlanLight() throws Exception
	{
		assert	isOn() : new PreconditionException("isOn()");
		assert	isLightPlanned() :
				new PreconditionException("isLightPlanned()");

		if (VERBOSE) {
			this.traceMessage("Indoor garden cancels plan at " +
											this.getCurrentTime() + ".\n");
		}

		if (this.isLightOn()) {
			this.switchLightOff();
		}
		this.deactivated = false;
		this.timeToSwitchLightOn = null;
		this.timeToSwitchLightOff = null;

		if (!this.isSimulated) {
			this.cancelSchedule();
		}

		assert	!isLightPlanned() :
				new PostconditionException("!isLightPlanned()");
		assert	!isLightOn() : new PostconditionException("!isLightOn()");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#getPlannedLightOn()
	 */
	@Override
	public Instant		getPlannedLightOn() throws Exception
	{
		assert	isOn() : new PreconditionException("isOn()");
		assert	isLightPlanned() :
				new PreconditionException("isLightPlanned()");

		if (VERBOSE) {
			this.traceMessage("Indoor garden returns planned light on time: " +
							  this.timeToSwitchLightOn + ".\n");
		}

		return this.timeToSwitchLightOn;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#getPlannedLightOff()
	 */
	@Override
	public Instant		getPlannedLightOff() throws Exception
	{
		assert	isOn() : new PreconditionException("isOn()");
		assert	isLightPlanned() :
				new PreconditionException("isLightPlanned()");

		if (VERBOSE) {
			this.traceMessage("Indoor garden returns planned light off time: " +
							  this.timeToSwitchLightOff + ".\n");
		}

		return this.timeToSwitchLightOff;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#isLightOn()
	 */
	@Override
	public boolean		isLightOn() throws Exception
	{
		assert	isOn() : new PreconditionException("isOn()");

		if (DEBUG) {
			this.traceMessage("Indoor garden is light on ? " + this.lightOn
							  + ".\n");
		}

		return this.lightOn;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#isDeactivated()
	 */
	@Override
	public boolean		isDeactivated() throws Exception
	{
		assert isOn() : new PreconditionException("isOn()");
		assert isLightPlanned() : new PreconditionException("isLightPlanned()");

		if (DEBUG) {
			this.traceMessage("Indoor garden is deactivated? " +
							  this.deactivated + ".\n");
		}

		return this.deactivated;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#deactivate()
	 */
	@Override
	public void			deactivate() throws Exception
	{
		assert	isOn() : new PreconditionException("isOn()");
		assert	isLightPlanned() : new PreconditionException("isLightPlanned()");
		assert	!isDeactivated() : new PreconditionException("!isDeactivated()");

		if (DEBUG || VERBOSE) {
			this.traceMessage("Indoor garden is deactivated at " +
							  this.getCurrentTime() + ".\n");
		}

		this.deactivated = true;
		if (this.isLightOn()) {
			this.switchLightOff();
		}

		assert	isDeactivated() : new PostconditionException("isDeactivated()");
		assert	!isLightOn() : new PostconditionException("!isLightOn()");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#reactivate()
	 */
	@Override
	public void			reactivate() throws Exception
	{
		assert	isOn() : new PreconditionException("isOn()");
		assert	isLightPlanned() : new PreconditionException("isLightPlanned()");
		assert	isDeactivated() : new PreconditionException("isDeactivated()");

		if (DEBUG || VERBOSE) {
			this.traceMessage("Indoor garden is reactivated at " +
							  this.getCurrentTime() + ".\n");
		}

		this.deactivated = false;
		if (this.currentPlanActive()) {
			this.switchLightOn();
		}

		assert	!isDeactivated() : new PostconditionException("!isDeactivated()");
		assert	!currentPlanActive() || isLightOn() :
				new PostconditionException("!currentPlanActive() || isLightOn()");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#setCurrentTime(java.time.Instant)
	 */
	@Override
	public void			setCurrentTime(Instant current) throws Exception
	{
		if (DEBUG || VERBOSE) {
			this.traceMessage("Indoor garden set simulated current time to " +
							  current + ".\n");
		}

		if (this.isSimulated) {
			this.simulatedNow = current;
			if (this.on && this.isLightPlanned() && !this.deactivated) {
				if (this.currentPlanActive() && !this.isLightOn()) {
					this.switchLightOn();
				}
				if (!this.currentPlanActive() && this.isLightOn()) {
					this.switchLightOff();
				}
			}
		}
	}

	/**
	 * @see fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenImplementationI#getCurrentTime()
	 */
	public Instant		getCurrentTime()
	{
		Instant ret = null;
		if (this.isSimulated) {
			ret = this.simulatedNow;
		} else {
			ret = Instant.now();
		}
		if (DEBUG) {
			this.traceMessage("Indoor garden returns the current time: " +
							  ret + ".\n");
		}

		return ret;
	}

	// -------------------------------------------------------------------------
	// Component methods
	// -------------------------------------------------------------------------

	/**
	 * return true if the current time is between {@code timeOn} and
	 * {@code timeOff}.
	 *
	 * <p>
	 * As the component works with {@code LocalTime}, times are on a 24H/day.
	 * Hence, if {@code timeOff} comes after {@code timeOn}, then the current
	 * time must really be in between. If {@code timeOn} comes after
	 * {@code timeOff}, then we assume that the lighting begins on one day and
	 * ends on the next day. Thus, in this latter case, the current time must be
	 * either after {@code timeOn} or before {@code timeOff}.
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code timeOn != null}
	 * pre	{@code timeOff != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param timeOn	beginning of the lighting.
	 * @param timeOff	end of the lighting.
	 * @return			true if the current time is between {@code timeOn} and {@code timeOff}.
	 */
	public boolean		currentlyActive(
		Instant timeOn,
		Instant timeOff
		)
	{
		assert	timeOn != null : new PreconditionException("timeOn != null");
		assert	timeOff != null : new PreconditionException("timeOff != null");

		Instant current = this.getCurrentTime();
		return current.equals(timeOn) || current.equals(timeOff) ||
						current.isAfter(timeOn) && current.isBefore(timeOff);
	}

	/**
	 * return true if the current plan is active now.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isLightPlanned()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				true if the current plan is active now.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		currentPlanActive() throws Exception
	{
		assert	isLightPlanned() : new PreconditionException("isLightPlanned()");
		return currentlyActive(this.timeToSwitchLightOn,
											    this.timeToSwitchLightOff);
	}

	/**
	 * switch the light on for actual hardware (actuator).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isLightOn()}
	 * post	{@code isLightOn()}
	 * </pre>
	 *
	 */
	protected void		switchLightOn()
	{
		try {
			assert	!isLightOn() : new PreconditionException("!isLightOn()");
			this.lightOn = true;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

	/**
	 * switch the light off for actual hardware (actuator).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isLightOn()}
	 * post	{@code !isLightOn()}
	 * </pre>
	 *
	 */
	protected void		switchLightOff()
	{
		try {
			assert	isLightOn() : new PreconditionException("isLightOn()");
			this.lightOn = false;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

	/** future object providing a handle on the task that switches on
	 *  the light.															*/
	protected Future<?>	switchOnTask;
	/** future object providing a handle on the task that switches off
	 *  the light.															*/
	protected Future<?>	switchOffTask;

	/**
	 * return true if the plan has been scheduled, otherwise false.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the plan has been scheduled, otherwise false.
	 */
	protected boolean	isScheduled()
	{
		return this.switchOnTask != null || this.switchOffTask != null;
	}

	/**
	 * schedule the next program actions (switching the light on then off).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !isScheduled()}
	 * post	{@code isScheduled()}
	 * </pre>
	 * 
	 * @throws Exception <i>to do</i>.
	 */
	@SuppressWarnings("rawtypes")
	protected void		scheduleProgram() throws Exception
	{
		assert	!this.isScheduled() :
			new PreconditionException("!isScheduled()");

		Instant now = this.getCurrentTime();
		if (!this.currentPlanActive()) {
			if (!this.isSimulated) {
				Duration d = Duration.between(now, timeToSwitchLightOn);
				this.switchOnTask = 
					this.scheduleTaskOnComponent(
							new AbstractTask() {
								@Override
								public void run() {
									try {
										if (!((IndoorGarden)this.getTaskOwner()).
												isDeactivated()) {
											((IndoorGarden)this.getTaskOwner()).
											switchLightOn();
										}
									} catch (Exception e) {
										throw new RuntimeException(e) ;
									}
								}
							},
							d.toMillis(),
							TimeUnit.MILLISECONDS);
			} else {
				this.switchOnTask =
					new Future() {
						@Override
						public boolean cancel(boolean mayInterruptIfRunning) {
							return true;
						}
						@Override
						public boolean isCancelled() { return false; }
						@Override
						public boolean isDone() { return false; }
						@Override
						public Object get()
						throws InterruptedException, ExecutionException
						{ return null; }
						@Override
						public Object get(long timeout, TimeUnit unit)
						throws InterruptedException, ExecutionException,
						TimeoutException
						{ return null; }
					};
			}
		}
		Duration d = Duration.between(now, this.timeToSwitchLightOff);
		if (!this.isSimulated) {
			this.switchOffTask =
				this.scheduleTaskOnComponent(
						new AbstractTask() {
							@Override
							public void run() {
								try {
									if (!((IndoorGarden)this.getTaskOwner()).
											isDeactivated()) {
										((IndoorGarden)this.getTaskOwner()).
										switchLightOff();
									}
								} catch (Exception e) {
									throw new RuntimeException(e) ;
								}
							}
						},
						d.toMillis(),
						TimeUnit.MILLISECONDS);
		} else {
			this.switchOffTask =
				new Future() {
					@Override
					public boolean cancel(boolean mayInterruptIfRunning) {
						return true;
					}
					@Override
					public boolean isCancelled() { return false; }
					@Override
					public boolean isDone() { return false; }
					@Override
					public Object get()
					throws InterruptedException, ExecutionException
					{ return null; }
					@Override
					public Object get(long timeout, TimeUnit unit)
					throws InterruptedException, ExecutionException,
					TimeoutException
					{ return null; }
				};
		}

		assert	this.isScheduled() :
			new PostconditionException("isScheduled()");
	}

	/**
	 * cancel the scheduled actions if any.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code isScheduled()}
	 * post	{@code !isScheduled()}
	 * </pre>
	 * 
	 * @throws Exception <i>to do</i>.
	 */
	protected void		cancelSchedule() throws Exception
	{
		assert	this.isScheduled() :
				new PreconditionException("isScheduled()");

		if (!this.isSimulated) {
			if (this.switchOnTask != null && !this.switchOnTask.isCancelled()) {
				this.switchOnTask.cancel(false);
				this.switchOnTask = null;
			}
			if (this.switchOffTask != null && !this.switchOffTask.isCancelled()) {
				this.switchOffTask.cancel(false);
				this.switchOffTask = null;
			}
		} else {
			this.switchOnTask = null;
			this.switchOffTask = null;
		}

		assert	!this.isScheduled() :
				new PostconditionException("!isScheduled()");
	}
}
// -----------------------------------------------------------------------------
