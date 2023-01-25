package fr.sorbonne_u.components.utils.phaser;

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

import java.util.concurrent.Phaser;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>PhaserComponent</code> implements a synchroniser component
 * based on the Java standard class {@code Phaser} and offers the same
 * services (TODO).
 *
 * <p><strong>Description</strong></p>
 * 
 * TODO: add the remaining methods from java.util.concurrent.Phaser.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// TODO
 * </pre>
 * 
 * <p>Created on : 2022-09-19</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@OfferedInterfaces(offered = {PhaserCI.class})
public class			PhaserComponent
extends		AbstractComponent
implements	PhaserImplementationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** a convenient string to be used as inbound port URI prefix.			*/
	public static final String	INBOUND_PORT_URI_PREFIX = "phaser-ibp";
	protected Phaser			javaPhaser;
	protected PhaserInboundPort	inboundPort;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a phaser component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 * 
	 * @param numberOfParticipants	number of participants in the phaser.
	 * @throws Exception 			<i>to do</i>.
	 */
	protected			PhaserComponent(int numberOfParticipants)
	throws Exception
	{
		this(INBOUND_PORT_URI_PREFIX, numberOfParticipants);
	}

	/**
	 * create a phaser component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 * 
	 * @param inboundPortURI		URI of the inbound port of the component.
	 * @param numberOfParticipants	number of participants in the phaser.
	 * @throws Exception			<i>to do</i>.
	 */
	protected			PhaserComponent(
		String inboundPortURI,
		int numberOfParticipants
		) throws Exception
	{
		super(0, 0);
		assert	inboundPortURI != null && !inboundPortURI.isEmpty() :
				new PreconditionException(
						"inboundPortURI != null && !inboundPortURI.isEmpty()");

		if (numberOfParticipants > 0) {
			this.javaPhaser = new Phaser(numberOfParticipants);
		} else {
			this.javaPhaser = new Phaser();
		}
		this.inboundPort = new PhaserInboundPort(inboundPortURI, this);
		this.inboundPort.publishPort();
	}

	/**
	 * create a phaser component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param reflectionInboundPortURI	URI of the reflection inbound port of the component.
	 * @param inboundPortURI			URI of the inbound port of the component.
	 * @param numberOfParticipants		number of participants in the phaser.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			PhaserComponent(
		String reflectionInboundPortURI,
		String inboundPortURI,
		int numberOfParticipants
		) throws Exception
	{
		super(reflectionInboundPortURI, 0, 0);

		if (numberOfParticipants > 0) {
			this.javaPhaser = new Phaser(numberOfParticipants);
		} else {
			this.javaPhaser = new Phaser();
		}
		this.inboundPort = new PhaserInboundPort(inboundPortURI, this);
		this.inboundPort.publishPort();
	}

	/**
	 * initialise the component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param inboundPortURI		URI of the inbound port of the component.
	 * @param numberOfParticipants	number of participants in the phaser.
	 * @throws Exception			<i>to do</i>.
	 */
	protected void		initialise(
		String inboundPortURI,
		int numberOfParticipants
		) throws Exception
	{
		assert	inboundPortURI != null && !inboundPortURI.isEmpty() :
				new PreconditionException(
					"inboundPortURI != null && !inboundPortURI.isEmpty()");
		if (numberOfParticipants > 0) {
			this.javaPhaser = new Phaser(numberOfParticipants);
		} else {
			this.javaPhaser = new Phaser();
		}
		this.inboundPort = new PhaserInboundPort(inboundPortURI, this);
		this.inboundPort.publishPort();
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
	 * @see fr.sorbonne_u.components.utils.phaser.PhaserImplementationI#arriveAndAwaitAdvance()
	 */
	@Override
	public int			arriveAndAwaitAdvance() throws Exception
	{
		return this.javaPhaser.arriveAndAwaitAdvance();
	}
}
// -----------------------------------------------------------------------------
