/**
 * 
 */
package fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.connections;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.components.hem2022e3.equipments.refrigerator.RefrigeratorActuatorCI;

/**
 * The class <code>RefrigeratoActuatorConnector</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : 2023-01-04</p>
 * 
 * @author	<p>Hongyu YAN & Liuyi CHEN</p>
 */
public class RefrigeratoActuatorConnector 
extends AbstractConnector 
implements RefrigeratorActuatorCI 
{
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	@Override
	public void run() throws Exception {
		((RefrigeratorActuatorCI)this.offering).run();
	}

	@Override
	public void doNotRun() throws Exception {
		((RefrigeratorActuatorCI)this.offering).doNotRun();
	}

}
