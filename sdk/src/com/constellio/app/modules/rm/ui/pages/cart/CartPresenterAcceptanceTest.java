package com.constellio.app.modules.rm.ui.pages.cart;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;

public class CartPresenterAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();
	SearchServices searchServices;
	RecordServices recordServices;
	RMSchemasRecordsServices rm;

	SessionContext sessionContext;
	@Mock CartView cartView;
	CartPresenter cartPresenter;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus()
				.withAllTest(users));
		searchServices = getModelLayerFactory().newSearchServices();
		recordServices = getModelLayerFactory().newRecordServices();
		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());

		sessionContext = FakeSessionContext.forRealUserIncollection(users.adminIn(zeCollection));
		when(cartView.getCollection()).thenReturn(zeCollection);
		when(cartView.getSessionContext()).thenReturn(sessionContext);
		when(cartView.getConstellioFactories()).thenReturn(getConstellioFactories());

		cartPresenter = new CartPresenter(cartView);

	}

	@Test
	public void givenCartWithFolderOfDifferentAdministrativeThenGetCommonAdministrativeUnitReturnNull()
			throws Exception {

		Cart cart = rm.newCart();
		cart.setOwner(users.adminIn(zeCollection));
		cart.setFolders(asList(records.folder_A10, records.folder_C04, records.folder_C07));
		cart.setTitle("ze cart");
		recordServices.add(cart);
		cartPresenter.forParams(cart.getId());

		assertThat(cartPresenter.getCommonAdministrativeUnit(cartPresenter.getCartFolders())).isNull();

	}

	@Test
	public void givenCartWithFolderOfSameAdministrativeThenGetCommonAdministrativeUnitReturnTheUnit()
			throws Exception {

		Cart cart = rm.newCart();
		cart.setOwner(users.adminIn(zeCollection));
		cart.setFolders(asList(records.folder_A14, records.folder_A15, records.folder_A16, records.folder_A17));
		cart.setTitle("ze cart");
		recordServices.add(cart);
		cartPresenter.forParams(cart.getId());

		assertThat(cartPresenter.getCommonAdministrativeUnit(cartPresenter.getCartFolders())).isEqualTo(records.unitId_10a);

	}

	@Test
	public void givenCartWithFolderOfDifferentArchivisticaStatusWhenFindCommonDecomListTypeThenReturnEmptyList()
			throws Exception {

		Cart cart = rm.newCart();
		cart.setOwner(users.adminIn(zeCollection));
		cart.setFolders(asList(records.folder_A10, records.folder_A48, records.folder_A16, records.folder_A17));
		cart.setTitle("ze cart");
		recordServices.add(cart);
		cartPresenter.forParams(cart.getId());

		assertThat(cartPresenter.getCommonDecommissioningListTypes(cartPresenter.getCartFolders())).isEmpty();
	}

	@Test
	public void givenCartWithActiveFoldersWithSameDisposalTypeWhenFindCommonDecomListTypeThenReturnTwoChoices()
			throws Exception {

		Cart cart = rm.newCart();
		cart.setOwner(users.adminIn(zeCollection));
		cart.setFolders(asList(records.folder_A17, records.folder_A18, records.folder_A19, records.folder_A20));
		cart.setTitle("ze cart");
		recordServices.add(cart);
		cartPresenter.forParams(cart.getId());

		assertThat(cartPresenter.getCommonDecommissioningListTypes(cartPresenter.getCartFolders())).containsOnly(
				DecommissioningListType.FOLDERS_TO_TRANSFER,
				DecommissioningListType.FOLDERS_TO_DEPOSIT
		);

	}

	@Test
	public void givenCartWithActiveFoldersWithSomeUnclosedWhenFindCommonDecomListTypeThenReturnEmptyList()
			throws Exception {

		Cart cart = rm.newCart();
		cart.setOwner(users.adminIn(zeCollection));
		cart.setFolders(asList(records.folder_A08, records.folder_A09, records.folder_A10, records.folder_A11));
		cart.setTitle("ze cart");
		recordServices.add(cart);
		cartPresenter.forParams(cart.getId());

		assertThat(cartPresenter.getCommonDecommissioningListTypes(cartPresenter.getCartFolders())).isEmpty();

	}

	@Test
	public void givenCartWithActivOpenedFoldersWithDifferentDisposalTypeWhenFindCommonDecomListTypeThenReturnOneChoice()
			throws Exception {

		Cart cart = rm.newCart();
		cart.setOwner(users.adminIn(zeCollection));
		cart.setFolders(asList(records.folder_A07, records.folder_A08));
		cart.setTitle("ze cart");
		recordServices.add(cart);
		cartPresenter.forParams(cart.getId());

		assertThat(cartPresenter.getCommonDecommissioningListTypes(cartPresenter.getCartFolders())).containsOnly(
				DecommissioningListType.FOLDERS_TO_CLOSE
		);

	}

	@Test
	public void givenCartWithActivClosedFoldersWithDifferentDisposalTypeWhenFindCommonDecomListTypeThenReturnOneChoice()
			throws Exception {

		Cart cart = rm.newCart();
		cart.setOwner(users.adminIn(zeCollection));
		cart.setFolders(asList(records.folder_A17, records.folder_A18, records.folder_A19, records.folder_A20));
		cart.setTitle("ze cart");
		recordServices.add(cart);
		cartPresenter.forParams(cart.getId());

		assertThat(cartPresenter.getCommonDecommissioningListTypes(cartPresenter.getCartFolders())).containsOnly(
				DecommissioningListType.FOLDERS_TO_TRANSFER,
				DecommissioningListType.FOLDERS_TO_DEPOSIT
		);

	}

	@Test
	public void givenCartWithSemiActiveFoldersWithSameDisposalTypeWhenFindCommonDecomListTypeThenReturnOneChoices()
			throws Exception {

		Cart cart = rm.newCart();
		cart.setOwner(users.adminIn(zeCollection));
		cart.setFolders(asList(records.folder_A44, records.folder_A45, records.folder_A46, records.folder_A47));
		cart.setTitle("ze cart");
		recordServices.add(cart);
		cartPresenter.forParams(cart.getId());

		assertThat(cartPresenter.getCommonDecommissioningListTypes(cartPresenter.getCartFolders())).containsOnly(
				DecommissioningListType.FOLDERS_TO_DESTROY
		);

	}

	@Test
	public void givenCartWithSemiActiveFoldersWithDifferentDisposalTypeWhenFindCommonDecomListTypeThenReturnEmptyList()
			throws Exception {

		Cart cart = rm.newCart();
		cart.setOwner(users.adminIn(zeCollection));
		cart.setFolders(asList(records.folder_A47, records.folder_A48));
		cart.setTitle("ze cart");
		recordServices.add(cart);
		cartPresenter.forParams(cart.getId());

		assertThat(cartPresenter.getCommonDecommissioningListTypes(cartPresenter.getCartFolders())).isEmpty();

	}
}
