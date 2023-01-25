package fr.sorbonne_u.components.hem2022e1;

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

import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryer;
import fr.sorbonne_u.components.hem2022e1.equipments.hairdryer.HairDryerUnitTester;
import fr.sorbonne_u.components.hem2022e1.equipments.heater.ThermostatedHeater;
import fr.sorbonne_u.components.hem2022e1.equipments.hem.HEM;
import fr.sorbonne_u.components.hem2022e1.equipments.hem.IndoorGardenTester;
import fr.sorbonne_u.components.hem2022e1.equipments.indoorgarden.IndoorGarden;
import fr.sorbonne_u.components.hem2022e1.equipments.meter.ElectricMeter;
import fr.sorbonne_u.components.utils.phaser.PhaserComponent;
import fr.sorbonne_u.components.AbstractComponent;

// -----------------------------------------------------------------------------
/**
 * The class <code>CVMIntegrationTest</code> defines the integration test
 * for the household energy management example.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no invariant
 * </pre>
 * 
 * <p>Created on : 2021-09-10</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			CVMIntegrationTest
extends		AbstractCVM
{
	public				CVMIntegrationTest() throws Exception
	{

	}

	/**
	 * @see fr.sorbonne_u.components.cvm.AbstractCVM#deploy()
	 */
	@Override
	public void			deploy() throws Exception
	{
		AbstractComponent.createComponent(
				ElectricMeter.class.getCanonicalName(),
				new Object[]{});

		AbstractComponent.createComponent(
				HairDryer.class.getCanonicalName(),
				new Object[]{});
		// At this stage, the unit tester for the hair dryer is added only
		// to show the hair dryer functioning; later on, it will be replaced
		// by a simulation of users' actions.
		AbstractComponent.createComponent(
				HairDryerUnitTester.class.getCanonicalName(),
				new Object[]{});

		// On the contrary, as the heater is controlled by the household energy
		// manager, there is no need to maintain a unit tester; later on, the
		// manager will act upon the heater through its control decisions.
		AbstractComponent.createComponent(
				ThermostatedHeater.class.getCanonicalName(),
				new Object[]{});

		AbstractComponent.createComponent(
				IndoorGarden.class.getCanonicalName(),
				new Object[]{true});
		// The indoor garden tester implements a test scenario for the
		// indoor garden component
		AbstractComponent.createComponent(
				IndoorGardenTester.class.getCanonicalName(),
				new Object[]{});
		// to perform the test, the indoor garden tester and the HEM must
		// interleave their actions upon the indoor garden in a precise order;
		// the phaser component helps putting this order in place
		AbstractComponent.createComponent(
				PhaserComponent.class.getCanonicalName(),
				new Object[]{2});

		AbstractComponent.createComponent(
				HEM.class.getCanonicalName(),
				new Object[]{});

		super.deploy();
	}

	public static void	main(String[] args)
	{
		try {
			CVMIntegrationTest cvm = new CVMIntegrationTest();
			cvm.startStandardLifeCycle(10000L);
			Thread.sleep(1000000L);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
// -----------------------------------------------------------------------------
