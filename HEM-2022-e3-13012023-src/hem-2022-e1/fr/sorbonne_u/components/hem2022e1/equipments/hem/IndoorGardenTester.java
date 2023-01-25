package fr.sorbonne_u.components.hem2022e1.equipments.hem;

// Copyright Jacques Malenfant, Sorbonne Universite.
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

import java.time.Instant;
import org.junit.jupiter.api.Assertions;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGarden;
import fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenCI;
import fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenOutboundPort;
import fr.sorbonne_u.components.utils.phaser.PhaserCI;
import fr.sorbonne_u.components.utils.phaser.PhaserComponent;
import fr.sorbonne_u.components.utils.phaser.PhaserConnector;
import fr.sorbonne_u.components.utils.phaser.PhaserOutboundPort;

// -----------------------------------------------------------------------------
/**
 * The class <code>IndoorGardenTester</code> implements a component
 * dedicated to the testing of the component {@code IndoorGarden} in
 * cooperation with the component {@code HEM}.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// TODO	// no more invariants
 * </pre>
 * 
 * <p>Created on : 2022-09-19</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@RequiredInterfaces(required = {IndoorGardenCI.class, PhaserCI.class})
public class		IndoorGardenTester
extends	AbstractComponent
{
	protected IndoorGardenOutboundPort	outboundPort;
	protected PhaserOutboundPort		phaserOutboundPort;

	protected			IndoorGardenTester()
	{
		super(1, 0);
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public synchronized void	start() throws ComponentStartException
	{
		super.start();
		try {
			this.outboundPort = new IndoorGardenOutboundPort(this);
			this.outboundPort.publishPort();
			this.doPortConnection(
					this.outboundPort.getPortURI(),
					IndoorGarden.INBOUND_PORT_URI_PREFIX,
					fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGardenConnector.class.
														getCanonicalName());
			this.phaserOutboundPort = new PhaserOutboundPort(this);
			this.phaserOutboundPort.publishPort();
			this.doPortConnection(
					this.phaserOutboundPort.getPortURI(),
					PhaserComponent.INBOUND_PORT_URI_PREFIX,
					PhaserConnector.class.getCanonicalName());
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
		
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception 
	{
		Instant i0 = Instant.parse("2023-01-13T00:00:00Z");
		this.outboundPort.setCurrentTime(i0.plusSeconds(9 * 3600));
		this.phaserOutboundPort.arriveAndAwaitAdvance(); // 1
		Instant t1 = i0.plusSeconds(10 * 3600);
		Instant t2 = i0.plusSeconds(23 * 3600);
		this.outboundPort.planLight(t1, t2);
		this.phaserOutboundPort.arriveAndAwaitAdvance(); // 2
		this.phaserOutboundPort.arriveAndAwaitAdvance(); // 3
		this.outboundPort.cancelPlanLight();
		this.phaserOutboundPort.arriveAndAwaitAdvance(); // 4
		this.phaserOutboundPort.arriveAndAwaitAdvance(); // 5
		t1 = i0.plusSeconds(7 * 3600);
		t2 = i0.plusSeconds(23 * 3600);
		this.outboundPort.planLight(t1, t2);
		Assertions.assertEquals(t1, this.outboundPort.getPlannedLightOn());
		Assertions.assertEquals(t2, this.outboundPort.getPlannedLightOff());
		this.phaserOutboundPort.arriveAndAwaitAdvance(); // 6
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void	finalise() throws Exception
	{
		this.doPortDisconnection(this.outboundPort.getPortURI());
		this.doPortDisconnection(this.phaserOutboundPort.getPortURI());
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.outboundPort.unpublishPort();
			this.phaserOutboundPort.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
}
// -----------------------------------------------------------------------------
