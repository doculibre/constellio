package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
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

public class RMContainerRecordExtensionAcceptanceTest extends ConstellioTest {
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
	public void whenModifyingContainerWithInexistentFavoritesIdsThenIdsAreDeleted() throws RecordServicesException {
		ContainerRecord container = records.getContainerBac01().setFavorites(NON_EXISTING_CART_IDS);
		recordServices.add(container);

		container.setTitle("TestModifié");
		recordServices.update(container);

		assertThat(container.getFavorites()).isEmpty();
	}

	@Test
	public void whenModifyingContainerWithSomeExistingFavoritesIdsThenNonExistingIdsAreDeleted()
			throws RecordServicesException {
		Cart cart = rm.newCart().setOwner(users.adminIn(zeCollection).getId());
		cart.setTitle("Chocolat");
		recordServices.add(cart);
		String existingId = cart.getId();
		List<String> listWithOneExistingId = new ArrayList<>();
		listWithOneExistingId.add(existingId);
		listWithOneExistingId.addAll(NON_EXISTING_CART_IDS);

		ContainerRecord containerRecord = records.getContainerBac01().setFavorites(listWithOneExistingId);
		recordServices.add(containerRecord);

		containerRecord.setTitle("TestModifié");
		recordServices.update(containerRecord);

		assertThat(containerRecord.getFavorites()).containsOnly(existingId);
	}

	@Test
	public void whenModifyingContainerWithExistentFavoritesIdsThenFavoritesListStaysTheSame()
			throws RecordServicesException {
		Cart firstCart = rm.newCart().setOwner(users.adminIn(zeCollection).getId());
		firstCart.setTitle("Uno");
		Cart secondCart = rm.newCart().setOwner(users.adminIn(zeCollection).getId());
		secondCart.setTitle("Dos");
		recordServices.add(firstCart);
		recordServices.add(secondCart);
		List<String> listWithExistingIds = asList(firstCart.getId(), secondCart.getId());

		ContainerRecord containerRecord = records.getContainerBac01().setFavorites(listWithExistingIds);
		recordServices.add(containerRecord);

		containerRecord.setTitle("TestModifié");
		recordServices.update(containerRecord);

		assertThat(containerRecord.getFavorites()).containsOnly(firstCart.getId(), secondCart.getId());
	}

}
