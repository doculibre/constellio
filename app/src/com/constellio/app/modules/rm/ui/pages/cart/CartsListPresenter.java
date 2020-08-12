package com.constellio.app.modules.rm.ui.pages.cart;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.OptimisticLockException;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.jgoodies.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class CartsListPresenter extends SingleSchemaBasePresenter<CartsListView> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CartsListPresenter.class);

	private RecordToVOBuilder recordToVOBuilder = new RecordToVOBuilder();
	private final MetadataSchemaVO schemaVO;
	private RMSchemasRecordsServices rm;
	private RecordServices recordServices;

	public CartsListPresenter(CartsListView view) {
		super(view, Cart.DEFAULT_SCHEMA);
		schemaVO = new MetadataSchemaToVOBuilder().build(defaultSchema(), RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		recordServices = modelLayerFactory.newRecordServices();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.USE_GROUP_CART).globally();
	}

	public void cartClickedInTable(RecordVO recordVO) {
		view.navigate().to(RMViews.class).cart(recordVO.getId());
	}

	public boolean renameFavoritesGroup(Cart cart, String name) {

		if (Strings.isNotBlank(name)) {
			try {
				cart.setTitle(name);
				recordServices.update(cart.getWrappedRecord(), getCurrentUser());
			} catch (RecordServicesException e) {
				throw new RuntimeException("Unexpected error when updating cart");
			}
		} else {
			view.showErrorMessage(i18n.$("requiredFieldWithName", i18n.$("title")));
			return false;
		}

		return true;
	}

	public void deleteButtonClicked(RecordVO recordVO) {
		try {
			delete(toRecord(recordVO));
		} catch (OptimisticLockException e) {
			LOGGER.error(e.getMessage());
			view.showErrorMessage(e.getMessage());
		}
		view.navigate().to(RMViews.class).listCarts();
	}

	public void cartClickedInTable(Cart cart) {
		view.navigate().to(RMViews.class).cart(cart.getId());
	}

	public void deleteButtonClicked(Cart cart) {
		delete(cart.getWrappedRecord());
		view.navigate().to(RMViews.class).listCarts();
	}

	public RecordVODataProvider getOwnedCartsDataProvider() {
		return new RecordVODataProvider(schemaVO, recordToVOBuilder, modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(defaultSchema()).where(getMetadata(Cart.OWNER))
						.isEqualTo(getCurrentUser().getId())).sortAsc(Schemas.TITLE);
			}
		};
	}

	public boolean isMyCartVisible() {
		return getCurrentUser().has(RMPermissionsTo.USE_MY_CART).globally();
	}

	public RecordVODataProvider getSharedCartsDataProvider() {
		return new RecordVODataProvider(schemaVO, recordToVOBuilder, modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(defaultSchema()).where(getMetadata(Cart.SHARED_WITH_USERS))
						.isContaining(Arrays.asList(getCurrentUser().getId()))).sortAsc(Schemas.TITLE);
			}
		};
	}

	public List<Cart> getOwnedCarts() {
		return rm.wrapCarts(searchServices().search(new LogicalSearchQuery(from(defaultSchema()).where(getMetadata(Cart.OWNER))
				.isEqualTo(getCurrentUser().getId())).sortAsc(Schemas.TITLE)));
	}

	public void saveButtonClicked(String title) {
		Cart cart = rm.newCart();
		cart.setTitle(title);
		cart.setOwner(getCurrentUser());
		try {
			recordServices().execute(new Transaction(cart.getWrappedRecord()).setUser(getCurrentUser()));
			view.navigate().to(RMViews.class).listCarts();
		} catch (RecordServicesException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void displayDefaultFavorites() {
		view.navigate().to(RMViews.class).cart(getCurrentUser().getId());
	}

	public User getCurrentUser() {
		return super.getCurrentUser();
	}

	public Record getUser(String userId) {
		return searchServices().searchSingleResult(from(rm.userSchemaType()).where(getMetadata("id")).isEqualTo(userId));
	}

	public Record getCreatedBy(Cart cart) {
		return searchServices().searchSingleResult(from(rm.userSchemaType()).where(getMetadata("id")).isEqualTo(cart.getCreatedBy()));
	}

	public Record getModifiedBy(Cart cart) {
		return searchServices().searchSingleResult(from(rm.userSchemaType()).where(getMetadata("id")).isEqualTo(cart.getModifiedBy()));
	}

	public Record getOwner(Cart cart) {
		return searchServices().searchSingleResult(from(rm.userSchemaType()).where(getMetadata("id")).isEqualTo(cart.getOwner()));
	}

	public MetadataSchemaVO getSchema() {
		return new MetadataSchemaToVOBuilder().build(schema(Cart.DEFAULT_SCHEMA), RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
	}
}
