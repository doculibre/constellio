package com.constellio.app.modules.rm.ui.pages.cart;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static java.util.Arrays.asList;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class CartPresenterAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();
	SearchServices searchServices;
	RMSchemasRecordsServices rm;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus()
				.withAllTest(users));
		searchServices = getModelLayerFactory().newSearchServices();
		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());

	}

	@Test
	public void whenDecommissioningFolderOfCartThenReturnFoldersOfGivenAdministrativeUnitAndDecommisioningType()
			throws Exception {

		Cart cart = rm.newCart();
		cart.setFolders(asList(records.folder_A01));

		for (Folder folder : rm.searchFolders(ALL)) {

			System.out.println(
					folder.getId() + "\t" + folder.getAdministrativeUnit() + "\t" + folder.getArchivisticStatus().getCode() + "\t"
							+ folder.getInactiveDisposalType().getCode() + "\t" + folder.getExpectedTransferDate() + "\t" + folder
							.getExpectedDepositDate() + "\t" + folder.getExpectedDestructionDate());
		}

	}
}
