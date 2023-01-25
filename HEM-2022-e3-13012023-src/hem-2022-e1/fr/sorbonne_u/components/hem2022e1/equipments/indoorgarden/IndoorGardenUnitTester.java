package fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden;

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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;

import java.time.Instant;

import org.junit.jupiter.api.Assertions;

// -----------------------------------------------------------------------------
/**
 * The class <code>IndoorGardenUnitTester</code> implements a component
 * dedicated to the unit testing of the component {@code IndoorGarden}.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariants
 * </pre>
 * 
 * <p>Created on : 2022-09-19</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@RequiredInterfaces(required = {IndoorGardenCI.class})
public class			IndoorGardenUnitTester
extends		AbstractComponent
{
	protected final String				inboundPortURI;
	protected IndoorGardenOutboundPort	outboundPort;
	protected final boolean				isSimulated;

	protected			IndoorGardenUnitTester(
		String inboundPortURI,
		boolean isSimulated
		)
	{
		super(1, 0);
		this.inboundPortURI = inboundPortURI;
		this.isSimulated = isSimulated;

		this.tracer.get().setTitle("Indoor garden unit tester component");
		this.tracer.get().setRelativePosition(0, 2);
		this.toggleTracing();		
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
					inboundPortURI,
					IndoorGardenConnector.class.getCanonicalName());
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		if (this.isSimulated) {
			Instant i0 = Instant.parse("2023-01-13T00:00:00Z");
			this.traceMessage("Simulated unit test begins...\n");
			this.outboundPort.setCurrentTime(i0.plusSeconds(12 * 3600));
			Assertions.assertFalse(this.outboundPort.isOn());
			this.outboundPort.on();
			this.traceMessage(".\n");
			Assertions.assertTrue(this.outboundPort.isOn());
			this.outboundPort.off();
			this.traceMessage(".\n");
			Assertions.assertFalse(this.outboundPort.isOn());
			this.outboundPort.on();
			this.traceMessage(".\n");
			Assertions.assertFalse(this.outboundPort.isLightPlanned());
			Instant t1 = i0.plusSeconds(16 * 3600);
			Instant t2 = i0.plusSeconds(23 * 3600);
			this.outboundPort.planLight(t1, t2);
			this.traceMessage(".\n");
			Assertions.assertTrue(this.outboundPort.isLightPlanned());
			Assertions.assertFalse(this.outboundPort.isLightOn());
			this.outboundPort.cancelPlanLight();
			this.traceMessage(".\n");
			Assertions.assertFalse(this.outboundPort.isLightPlanned());
			t1 = i0.plusSeconds(10 * 3600);
			t2 = i0.plusSeconds(23 * 3600);
			this.outboundPort.planLight(t1, t2);
			this.traceMessage(".\n");
			Assertions.assertTrue(this.outboundPort.isLightPlanned());
			Assertions.assertTrue(this.outboundPort.isLightOn());
			Assertions.assertEquals(t1, this.outboundPort.getPlannedLightOn());
			Assertions.assertEquals(t2, this.outboundPort.getPlannedLightOff());
			Assertions.assertFalse(this.outboundPort.isDeactivated());
			this.outboundPort.deactivate();
			this.traceMessage(".\n");
			Assertions.assertTrue(this.outboundPort.isDeactivated());
			Assertions.assertFalse(this.outboundPort.isLightOn());
			this.outboundPort.reactivate();
			this.traceMessage(".\n");
			Assertions.assertFalse(this.outboundPort.isDeactivated());
			Assertions.assertTrue(this.outboundPort.isLightOn());
			this.traceMessage("... then ends.\n");
		} else {
			this.traceMessage("Real-time unit test begins...\n");
			Assertions.assertFalse(this.outboundPort.isOn());
			this.outboundPort.on();
			this.traceMessage(".\n");
			Assertions.assertTrue(this.outboundPort.isOn());
			this.outboundPort.off();
			this.traceMessage(".\n");
			Assertions.assertFalse(this.outboundPort.isOn());
			this.outboundPort.on();
			this.traceMessage(".\n");
			Assertions.assertFalse(this.outboundPort.isLightPlanned());
			Instant now = Instant.now();
			Instant t1 = now.plusSeconds(5);
			Instant t2 = now.plusSeconds(15);
			this.outboundPort.planLight(t1, t2);
			this.traceMessage(".\n");
			Assertions.assertTrue(this.outboundPort.isLightPlanned());
			Assertions.assertFalse(this.outboundPort.isLightOn());
			this.outboundPort.cancelPlanLight();
			this.traceMessage(".\n");
			Assertions.assertFalse(this.outboundPort.isLightPlanned());
			this.outboundPort.planLight(t1, t2);
			this.traceMessage(".\n");
			Assertions.assertTrue(this.outboundPort.isLightPlanned());
			Thread.sleep(6000L);
			this.traceMessage("After sleeping 6s : " +
								this.outboundPort.getCurrentTime() + "\n");
			Assertions.assertTrue(this.outboundPort.isLightOn());
			Assertions.assertEquals(t1, this.outboundPort.getPlannedLightOn());
			Assertions.assertEquals(t2, this.outboundPort.getPlannedLightOff());
			Assertions.assertFalse(this.outboundPort.isDeactivated());
			this.outboundPort.deactivate();
			this.traceMessage(".\n");
			Assertions.assertTrue(this.outboundPort.isDeactivated());
			Assertions.assertFalse(this.outboundPort.isLightOn());
			this.outboundPort.reactivate();
			this.traceMessage(".\n");
			Assertions.assertFalse(this.outboundPort.isDeactivated());
			Assertions.assertTrue(this.outboundPort.isLightOn());
			Thread.sleep(12000L);
			this.traceMessage("After sleeping 12s : " +
								this.outboundPort.getCurrentTime() + "\n");
			Assertions.assertFalse(this.outboundPort.isLightOn());
			this.outboundPort.off();
			Assertions.assertFalse(this.outboundPort.isOn());
			this.traceMessage("... then ends.\n");
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void	finalise() throws Exception
	{
		this.doPortDisconnection(this.outboundPort.getPortURI());
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
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();		
	}
}
// -----------------------------------------------------------------------------
