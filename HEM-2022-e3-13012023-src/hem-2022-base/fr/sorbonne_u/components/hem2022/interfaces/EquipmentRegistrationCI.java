package fr.sorbonne_u.components.hem2022.interfaces;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide an
// example of a cyber-physical system.
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

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

// -----------------------------------------------------------------------------
/**
 * The component interface <code>EquipmentRegistrationCI</code> defines
 * the registration service of home energy managers that allows intelligent
 * equipments to register for control.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This component interface is part of a master course project aiming at
 * developing a mockup of a future home energy manager connected to a smart
 * grid.
 * </p>
 * <p>
 * The designed home energy manager requires a public component interface to
 * control intelligent equipments, but in practice, home energy manager
 * providers and equipment providers may not have the same interfaces.
 * The idea here is to also define a public XML schema that allows an equipment
 * provider to provide the home energy manager with a description of how to
 * make the link between the home energy manager required interface and its
 * own offered interface. The home energy manager can then generate a specific
 * connector that will accept calls following the required interface but that
 * will call the equipment respecting its offered interface.
 * </p>
 * <p>
 * This component interface is offered by the home energy manager and is meant
 * to be required by equipments that want to be connected to it.
 * </p>
 * 
 * <p>Created on : 2021-09-09</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		EquipmentRegistrationCI
extends		OfferedCI, RequiredCI
{
	/**
	 * register a specific equipment on the home energy manager and connect it
	 * using the description of its offered interface.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uid != null && !uid.isEmpty()}
	 * pre	{@code controlPortURI != null && !controlPortURI.isEmpty()}
	 * pre	{@code path2xmlControlAdapter != null && !path2xmlControlAdapter.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param uid						unique identifier of the equipment (<i>e.g.</i>, serial number).
	 * @param controlPortURI			URI of the control inbound port of the equipment.
	 * @param path2xmlControlAdapter	path to the XML document file (as a string), which describes how to link the required interface to the offered interface of this equipment, null if the equipment offers exactly the required interface).
	 * @return							true if the registration succeeded, false otherwise.
	 * @throws Exception				<i>to do</i>.
	 */
	public boolean		register(
		String uid,
		String controlPortURI,
		String path2xmlControlAdapter
		) throws Exception;
}
// -----------------------------------------------------------------------------
