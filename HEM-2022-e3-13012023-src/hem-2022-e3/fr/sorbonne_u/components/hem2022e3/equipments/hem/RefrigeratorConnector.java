package fr.sorbonne_u.components.hem2022e3.equipments.hem;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.hem2022.interfaces.SuspensionEquipmentControlCI;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.RefrigeratorCI;
/**
 * The class <code>RefrigeratorConnector.java</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2022-10-18</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class RefrigeratorConnector 
extends AbstractConnector 
implements SuspensionEquipmentControlCI {
	/** the minimum admissible temperature from which the refrigerator should
	 *  be resumed in priority after being suspended to save energy.		*/
	protected static final double	MAX_ADMISSIBLE_FREEZING_TEMP = -16.0;
	
	protected static final double	MAX_ADMISSIBLE_REFRIGERATION_TEMP = 10.0;
	/** the maximal admissible difference between the target and the
	 *  current temperature from which the refrigerator should be resumed in
	 *  priority after being suspended to save energy.						*/
	protected static final double	MAX_ADMISSIBLE_DELTA = 5.0;
	/** true if the heater has been suspended, false otherwise.				*/
	protected boolean	isSuspended;
	
	public RefrigeratorConnector() {
		super();
		this.isSuspended = false;
	}

	@Override
	public boolean on() throws Exception {
		return this.isSuspended || ((RefrigeratorCI)this.offering).isRunning();
	}

	@Override
	public boolean switchOn() throws Exception {
		((RefrigeratorCI)this.offering).startRefrigerator();
		return true;
	}

	@Override
	public boolean switchOff() throws Exception {
		((RefrigeratorCI)this.offering).stopRefrigerator();
		return true;
	}

	@Override
	public int maxMode() throws Exception {
		return 1;
	}

	@Override
	public boolean upMode() throws Exception {
		return false;
	}

	@Override
	public boolean downMode() throws Exception {
		return false;
	}

	@Override
	public boolean setMode(int modeIndex) throws Exception {
		return true;
	}

	@Override
	public int currentMode() throws Exception {
		return 1;
	}

	@Override
	public boolean suspended() throws Exception {
		return this.isSuspended;
	}

	@Override
	public boolean suspend() throws Exception {
		((RefrigeratorCI)this.offering).stopRefrigerator();
		this.isSuspended = true;
		return true;
	}

	@Override
	public boolean resume() throws Exception {
		((RefrigeratorCI)this.offering).startRefrigerator();
		this.isSuspended = false;
		return true;
	}

	@Override
	public double emergency() throws Exception {
		double currentFreezingTemperature =
					((RefrigeratorCI)this.offering).getCurrentFreezingTemperature();
		double targetFreezingTemperature =
					((RefrigeratorCI)this.offering).getTargetFreezingTemperature();
		double deltaFreezing = Math.abs(targetFreezingTemperature - currentFreezingTemperature);
		
		double currentRefrigerationTemperature =
				((RefrigeratorCI)this.offering).getCurrentRefrigerationTemperature();
		double targetRefrigerationTemperature =
				((RefrigeratorCI)this.offering).getTargetRefrigerationTemperature();
		double deltaRefrigeration = Math.abs(targetRefrigerationTemperature - currentRefrigerationTemperature);
		
		if (currentFreezingTemperature > RefrigeratorConnector.MAX_ADMISSIBLE_FREEZING_TEMP ||
				deltaFreezing >= RefrigeratorConnector.MAX_ADMISSIBLE_DELTA && 
				currentRefrigerationTemperature > RefrigeratorConnector.MAX_ADMISSIBLE_REFRIGERATION_TEMP ||
				deltaRefrigeration >= RefrigeratorConnector.MAX_ADMISSIBLE_DELTA ) {
			return 3.0;
		} else if (currentFreezingTemperature > RefrigeratorConnector.MAX_ADMISSIBLE_FREEZING_TEMP ||
				deltaFreezing >= RefrigeratorConnector.MAX_ADMISSIBLE_DELTA) {
			return 2.0;
		} else if (currentRefrigerationTemperature > RefrigeratorConnector.MAX_ADMISSIBLE_REFRIGERATION_TEMP ||
				deltaRefrigeration >= RefrigeratorConnector.MAX_ADMISSIBLE_DELTA) {
			return 1.0;
		} else {
			return (deltaFreezing > deltaRefrigeration? deltaFreezing : deltaRefrigeration) / RefrigeratorConnector.MAX_ADMISSIBLE_DELTA;
		}
	}

}
