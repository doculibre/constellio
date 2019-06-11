package com.constellio.app.modules.rm.services.menu.behaviors;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.pages.cart.DefaultFavoritesTable;
import com.constellio.app.modules.rm.ui.pages.cart.DefaultFavoritesTable.CartItem;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class CartWindowButton extends WindowButton {

	private RMSchemasRecordsServices rm;
	private MenuItemActionBehaviorParams params;
	private AppLayerFactory appLayerFactory;
	private RecordServices recordServices;
	private String collection;
	private Record record;
	private SearchServices searchServices;
	private MetadataSchemasManager metadataSchemasManager;

	public CartWindowButton(MenuItemActionBehaviorParams params) {
		super($("DisplayFolderView.addToCart"), $("DisplayFolderView.selectCart"));

		this.params = params;
		this.appLayerFactory = params.getView().getConstellioFactories().getAppLayerFactory();
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.collection = params.getView().getSessionContext().getCurrentCollection();
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.record = rm.get(params.getRecordVO().getId());
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		this.metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
	}

	public void addToCart() {
		if (params.getUser().has(RMPermissionsTo.USE_GROUP_CART).globally()) {
			click();
		} else if (params.getUser().has(RMPermissionsTo.USE_MY_CART).globally()) {
			addToDefaultCart(params);
		}
	}

	@Override
	protected Component buildWindowContent() {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();

		HorizontalLayout newCartLayout = new HorizontalLayout();
		newCartLayout.setSpacing(true);
		newCartLayout.addComponent(new Label($("CartView.newCart")));
		final BaseTextField newCartTitleField;
		newCartLayout.addComponent(newCartTitleField = new BaseTextField());
		newCartTitleField.setRequired(true);
		BaseButton saveButton;
		newCartLayout.addComponent(saveButton = new BaseButton($("save")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				try {
					createNewCartAndAddToItRequested(newCartTitleField.getValue(), params);
					getWindow().close();
				} catch (Exception e) {
					params.getView().showErrorMessage(MessageUtils.toMessage(e));
				}
			}
		});
		saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		TabSheet tabSheet = new TabSheet();
		Table ownedCartsTable = buildOwnedFavoritesTable(getWindow(), params);

		final RecordVOLazyContainer sharedCartsContainer = new RecordVOLazyContainer(getSharedCartsDataProvider(params));
		RecordVOTable sharedCartsTable = new RecordVOTable($("CartView.sharedCarts"), sharedCartsContainer);
		sharedCartsTable.addItemClickListener((ItemClickListener) event -> {
			addToCartRequested(sharedCartsContainer.getRecordVO((int) event.getItemId()).getId(), params.getView());
			getWindow().close();
		});

		sharedCartsTable.setPageLength(Math.min(15, sharedCartsContainer.size()));
		sharedCartsTable.setWidth("100%");
		tabSheet.addTab(ownedCartsTable);
		tabSheet.addTab(sharedCartsTable);
		layout.addComponents(newCartLayout, tabSheet);
		layout.setExpandRatio(tabSheet, 1);
		return layout;
	}

	private RecordVODataProvider getSharedCartsDataProvider(MenuItemActionBehaviorParams params) {
		MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
		final MetadataSchemaVO cartSchemaVO = schemaVOBuilder.build(rm.cartSchema(), VIEW_MODE.TABLE, params.getView().getSessionContext());
		return new RecordVODataProvider(cartSchemaVO, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(), params.getView().getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(rm.cartSchema()).where(rm.cartSharedWithUsers())
						.isContaining(asList(params.getUser().getId()))).sortAsc(Schemas.TITLE);
			}
		};
	}


	private DefaultFavoritesTable buildOwnedFavoritesTable(final Window window, MenuItemActionBehaviorParams params) {
		List<CartItem> cartItems = new ArrayList<>();
		if (hasCurrentUserPermissionToUseMyCart(params.getUser())) {
			cartItems.add(new DefaultFavoritesTable.CartItem($("CartView.defaultFavorites")));
		}
		for (Cart cart : getOwnedCarts(params.getUser())) {
			cartItems.add(new DefaultFavoritesTable.CartItem(cart, cart.getTitle()));
		}
		final DefaultFavoritesTable.FavoritesContainer container = new DefaultFavoritesTable.FavoritesContainer(DefaultFavoritesTable.CartItem.class, cartItems);
		DefaultFavoritesTable defaultFavoritesTable = new DefaultFavoritesTable("favoritesTable", container, getCartMetadataSchemaVO(params.getView().getSessionContext()));
		defaultFavoritesTable.setCaption($("CartView.ownedCarts"));
		defaultFavoritesTable.addItemClickListener((ItemClickListener) event -> {
			Cart cart = container.getCart((CartItem) event.getItemId());
			if (cart == null) {
				addToDefaultCart(params);
			} else {
				addToCartRequested(cart.getId(), params.getView());
			}
			window.close();
		});
		defaultFavoritesTable.setPageLength(Math.min(15, container.size()));
		container.removeContainerProperty(DefaultFavoritesTable.CartItem.DISPLAY_BUTTON);
		defaultFavoritesTable.setWidth("100%");
		return defaultFavoritesTable;
	}

	MetadataSchemaVO getCartMetadataSchemaVO(SessionContext sessionContext) {
		return new MetadataSchemaToVOBuilder().build(metadataSchemasManager.getSchemaTypes(sessionContext.getCurrentCollection())
				.getDefaultSchema(Cart.SCHEMA_TYPE), RecordVO.VIEW_MODE.TABLE, sessionContext);
	}

	private void addToCartRequested(String cartId, BaseView baseView) {
		if (rm.numberOfDocumentsInFavoritesReachesLimit(cartId, 1)) {
			baseView.showMessage($("DisplayDocumentView.cartCannotContainMoreThanAThousandDocuments"));
		} else {
			addToFavorite(cartId, record);
			Transaction transaction = new Transaction(RecordUpdateOptions.validationExceptionSafeOptions());
			transaction.addUpdate(record);
			try {
				recordServices.execute(transaction);
				baseView.showMessage($("DocumentActionsComponent.addedToCart"));
			} catch (RecordServicesException e) {
				throw new ImpossibleRuntimeException(e);
			}
		}
	}

	private List<Cart> getOwnedCarts(User user) {
		return rm.wrapCarts(searchServices.search(new LogicalSearchQuery(from(rm.cartSchema()).where(rm.cart.owner())
				.isEqualTo(user.getId())).sortAsc(Schemas.TITLE)));
	}

	private boolean hasCurrentUserPermissionToUseMyCart(User user) {
		return user.has(RMPermissionsTo.USE_MY_CART).globally();
	}

	private void addToDefaultCart(MenuItemActionBehaviorParams params) {
		if (rm.numberOfDocumentsInFavoritesReachesLimit(params.getUser().getId(), 1)) {
			params.getView().showMessage($("DisplayDocumentView.cartCannotContainMoreThanAThousandDocuments"));
		} else {
			addToFavorite(params.getUser().getId(), record);
			try {
				recordServices.update(record, RecordUpdateOptions.validationExceptionSafeOptions());
			} catch (RecordServicesException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			params.getView().showMessage($("DisplayDocumentView.documentAddedToDefaultFavorites"));
		}
	}

	private void createNewCartAndAddToItRequested(String title, MenuItemActionBehaviorParams params) {
		Cart cart = rm.newCart();
		cart.setTitle(title);
		cart.setOwner(params.getUser());

		addToFavorite(cart.getId(), record);

		try {
			recordServices.execute(new Transaction(cart.getWrappedRecord()).setUser(params.getUser()));
			recordServices.update(record, RecordUpdateOptions.validationExceptionSafeOptions());
			params.getView().showMessage($("DocumentActionsComponent.addedToCart"));
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}
	}

	private Record addToFavorite(String cartId, Record record) {
		if (record.getTypeCode().equals(Document.SCHEMA_TYPE)) {
			Document document = rm.wrapDocument(record);
			document.addFavorite(cartId);
		} else if (record.getTypeCode().equals(Folder.SCHEMA_TYPE)) {
			Folder folder = rm.wrapFolder(record);
			folder.addFavorite(cartId);
		} else if (record.getTypeCode().equals(ContainerRecord.SCHEMA_TYPE)) {
			ContainerRecord containerRecord = rm.wrapContainerRecord(record);
			containerRecord.addFavorite(cartId);
		} else {
			throw new IllegalArgumentException("This schemaType is not supported");
		}

		return record;
	}
}
