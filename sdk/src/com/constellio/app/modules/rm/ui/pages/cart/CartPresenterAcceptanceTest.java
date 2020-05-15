package com.constellio.app.modules.rm.ui.pages.cart;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingType;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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

		cartPresenter = new CartPresenter(null, cartView);
	}

	@Test
	public void givenCartWithFolderOfDifferentAdministrativeThenGetCommonAdministrativeUnitReturnNull()
			throws Exception {

		Cart cart = rm.newCart();
		cart.setOwner(users.adminIn(zeCollection));
		List<Record> folders = asList(records.getFolder_A10().getWrappedRecord(), records.getFolder_C04().getWrappedRecord(), records.getFolder_C07().getWrappedRecord());
		addFoldersToCart(cart, folders);
		cart.setTitle("ze cart");
		recordServices.add(cart);
		recordServices.execute(new Transaction(folders));
		cartPresenter.forParams(cart.getId());

		assertThat(cartPresenter.getCommonAdministrativeUnit(cartPresenter.getCartFolders())).isNull();

	}

	@Test
	public void givenCartWithFolderOfSameAdministrativeThenGetCommonAdministrativeUnitReturnTheUnit()
			throws Exception {

		Cart cart = rm.newCart();
		cart.setOwner(users.adminIn(zeCollection));
		List<Record> folders = asList(records.getFolder_A14().getWrappedRecord(), records.getFolder_A15().getWrappedRecord(), records.getFolder_A16().getWrappedRecord(), records.getFolder_A17().getWrappedRecord());
		addFoldersToCart(cart, folders);
		cart.setTitle("ze cart");
		recordServices.add(cart);
		recordServices.execute(new Transaction(folders));
		cartPresenter.forParams(cart.getId());

		assertThat(cartPresenter.getCommonAdministrativeUnit(cartPresenter.getCartFolders())).isEqualTo(records.unitId_10a);

	}

	@Test
	public void givenCartWithFolderOfDifferentArchivisticaStatusWhenFindCommonDecomListTypeThenReturnEmptyList()
			throws Exception {

		Cart cart = rm.newCart();
		cart.setOwner(users.adminIn(zeCollection));
		List<Record> folders = asList(records.getFolder_A10().getWrappedRecord(), records.getFolder_A48().getWrappedRecord(), records.getFolder_A16().getWrappedRecord(), records.getFolder_A17().getWrappedRecord());
		addFoldersToCart(cart, folders);
		cart.setTitle("ze cart");
		recordServices.add(cart);
		recordServices.execute(new Transaction(folders));
		cartPresenter.forParams(cart.getId());

		assertThat(cartPresenter.getCommonDecommissioningListTypes(cartPresenter.getCartFolders())).isEmpty();
	}

	@Test
	public void givenCartWithActiveFoldersWithSameDisposalTypeWhenFindCommonDecomListTypeThenReturnTwoChoices()
			throws Exception {

		Cart cart = rm.newCart();
		cart.setOwner(users.adminIn(zeCollection));
		List<Record> folders = asList(records.getFolder_A17().getWrappedRecord(), records.getFolder_A18().getWrappedRecord(), records.getFolder_A19().getWrappedRecord(), records.getFolder_A20().getWrappedRecord());
		addFoldersToCart(cart, folders);
		cart.setTitle("ze cart");
		recordServices.add(cart);
		recordServices.execute(new Transaction(folders));
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
		List<Record> folders = asList(records.getFolder_A08().getWrappedRecord(), records.getFolder_A09().getWrappedRecord(), records.getFolder_A10().getWrappedRecord(), records.getFolder_A11().getWrappedRecord());
		addFoldersToCart(cart, folders);
		cart.setTitle("ze cart");
		recordServices.add(cart);
		recordServices.execute(new Transaction(folders));
		cartPresenter.forParams(cart.getId());

		assertThat(cartPresenter.getCommonDecommissioningListTypes(cartPresenter.getCartFolders())).isEmpty();

	}

	@Test
	public void givenCartWithActivOpenedFoldersWithDifferentDisposalTypeWhenFindCommonDecomListTypeThenReturnOneChoice()
			throws Exception {
		Cart cart = rm.newCart();
		cart.setOwner(users.adminIn(zeCollection));
		List<Record> folders = asList(records.getFolder_A07().getWrappedRecord(), records.getFolder_A08().getWrappedRecord());
		addFoldersToCart(cart, folders);
		cart.setTitle("ze cart");
		recordServices.add(cart);
		recordServices.execute(new Transaction(folders));
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
		List<Record> folders = asList(records.getFolder_A17().getWrappedRecord(), records.getFolder_A18().getWrappedRecord(), records.getFolder_A19().getWrappedRecord(), records.getFolder_A20().getWrappedRecord());
		addFoldersToCart(cart, folders);
		cart.setTitle("ze cart");
		recordServices.add(cart);
		recordServices.execute(new Transaction(folders));
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
		List<Record> folders = asList(records.getFolder_A54().getWrappedRecord(), records.getFolder_A55().getWrappedRecord(), records.getFolder_A56().getWrappedRecord(), records.getFolder_C34().getWrappedRecord());
		addFoldersToCart(cart, folders);
		cart.setTitle("ze cart");
		recordServices.add(cart);
		recordServices.execute(new Transaction(folders));
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
		List<Record> folders = asList(records.getFolder_A47().getWrappedRecord(), records.getFolder_A48().getWrappedRecord());
		addFoldersToCart(cart, folders);
		cart.setTitle("ze cart");
		recordServices.add(cart);
		recordServices.execute(new Transaction(folders));
		cartPresenter.forParams(cart.getId());

		assertThat(cartPresenter.getCommonDecommissioningListTypes(cartPresenter.getCartFolders())).isEmpty();

	}

	@Test
	public void givenNewCartWithBorrowedFoldersThenCannotCreateDecommissioningList()
			throws Exception {

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		recordServices.physicallyDeleteNoMatterTheStatus(records.getList10().getWrappedRecord(), User.GOD, new RecordPhysicalDeleteOptions());
		recordServices.physicallyDeleteNoMatterTheStatus(records.getList17().getWrappedRecord(), User.GOD, new RecordPhysicalDeleteOptions());
		new BorrowingServices(zeCollection, getModelLayerFactory()).borrowFolder(records.folder_A48, LocalDate.now(),
				LocalDate.now().plusDays(1), records.getAdmin(), records.getAdmin(), BorrowingType.BORROW, true);

		Cart cart = rm.newCart();
		cart.setOwner(users.adminIn(zeCollection));
		List<Record> folders = asList(records.getFolder_A47().getWrappedRecord(), records.getFolder_A48().getWrappedRecord());
		addFoldersToCart(cart, folders);
		cart.setTitle("ze cart");
		recordServices.add(cart);
		recordServices.execute(new Transaction(folders));
		cartPresenter.forParams(cart.getId());

		assertThat(cartPresenter.isAnyFolderBorrowed()).isTrue();
	}

	private void addFoldersToCart(Cart cart, List<Record> folders) {
		for (Record record : folders) {
			rm.wrapFolder(record).addFavorite(cart.getId());
		}
	}
}
