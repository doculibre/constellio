package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by constellios on 2017-04-03.
 */
public class RMDocumentExtensionAcceptanceTest extends ConstellioTest {
	private static final List<String> NON_EXISTING_CART_IDS = asList("01", "02");

	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	RMSchemasRecordsServices rm;
	Users users = new Users();

	@Before
	public void setUp() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
						.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList().withDocumentsHavingContent()
		);

		recordServices = getModelLayerFactory().newRecordServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		users.setUp(getModelLayerFactory().newUserServices());
	}

	@Test
	public void whenCheckingIfDocumentDocumentTypeLogicallyOrPhysicallyDeletableThenFalse()
			throws Exception {
		Document documentWithContent_a19 = records.getDocumentWithContent_A19();

		documentWithContent_a19.getContent().checkOut(records.getAdmin());

		recordServices.update(documentWithContent_a19);

		Record record = documentWithContent_a19.getWrappedRecord();

		assertThat(recordServices.validateLogicallyDeletable(record, User.GOD).isEmpty()).isFalse();
		assertThat(recordServices.validateLogicallyThenPhysicallyDeletable(record, User.GOD).isEmpty()).isFalse();
	}

	@Test
	public void whenModifyingDocumentWithInexistentFavoritesIdsThenIdsAreDeleted() throws RecordServicesException {
		Document document = records.getDocumentWithContent_A19().setFavorites(NON_EXISTING_CART_IDS);
		recordServices.add(document);

		document.setTitle("TestModifié");
		recordServices.update(document);

		assertThat(document.getFavorites()).isEmpty();
	}

	@Test
	public void whenModifyingDocumentWithSomeExistingFavoritesIdsThenNonExistingIdsAreDeleted()
			throws RecordServicesException {
		Cart cart = rm.newCart().setOwner(users.adminIn(zeCollection).getId());
		cart.setTitle("Vanilla");
		recordServices.add(cart);
		String existingId = cart.getId();
		List<String> listWithOneExistingId = new ArrayList<>();
		listWithOneExistingId.add(existingId);
		listWithOneExistingId.addAll(NON_EXISTING_CART_IDS);

		Document document = records.getDocumentWithContent_A19().setFavorites(listWithOneExistingId);
		recordServices.add(document);

		document.setTitle("TestModifié");
		recordServices.update(document);

		assertThat(document.getFavorites()).containsOnly(existingId);
	}

	@Test
	public void whenModifyingDocumentWithExistentFavoritesIdsThenFavoritesListStaysTheSame()
			throws RecordServicesException {
		Cart firstCart = rm.newCart().setOwner(users.adminIn(zeCollection).getId());
		firstCart.setTitle("ein");
		Cart secondCart = rm.newCart().setOwner(users.adminIn(zeCollection).getId());
		secondCart.setTitle("zwei");
		recordServices.add(firstCart);
		recordServices.add(secondCart);
		List<String> listWithExistingIds = asList(firstCart.getId(), secondCart.getId());

		Document document = records.getDocumentWithContent_A19().setFavorites(listWithExistingIds);
		recordServices.add(document);

		document.setTitle("TestModifié");
		recordServices.update(document);

		assertThat(document.getFavorites()).containsOnly(firstCart.getId(), secondCart.getId());
	}

}
