package com.constellio.app.modules.rm.services.menu.behaviors;

import com.constellio.app.api.extensions.params.NavigateToFromAPageParams;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.actions.DocumentRecordActionsServices;
import com.constellio.app.modules.rm.services.logging.DecommissioningLoggingService;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.builders.UserToVOBuilder;
import com.constellio.app.modules.rm.ui.components.document.DocumentActionsPresenterUtils;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.pages.cart.DefaultFavoritesTable;
import com.constellio.app.modules.rm.ui.pages.cart.DefaultFavoritesTable.CartItem;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.util.DecommissionNavUtil;
import com.constellio.app.modules.rm.util.RMNavigationUtils;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.*;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.RecordVORuntimeException.RecordVORuntimeException_NoSuchMetadata;
import com.constellio.app.ui.framework.builders.ContentVersionToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DownloadLink;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.report.LabelButtonV2;
import com.constellio.app.ui.framework.components.RMSelectionPanelReportPresenter;
import com.constellio.app.ui.framework.components.ReportTabButton;
import com.constellio.app.ui.framework.components.content.ContentVersionVOResource;
import com.constellio.app.ui.framework.components.content.UpdateContentVersionWindowImpl;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.utils.Factory;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.contents.ContentConversionManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.logging.SearchEventServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.pages.search.SearchPresenter.CURRENT_SEARCH_EVENT;
import static com.constellio.app.ui.pages.search.SearchPresenter.SEARCH_EVENT_DWELL_TIME;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

public class DocumentMenuItemActionBehaviors {

	private RMModuleExtensions rmModuleExtensions;
	private ModelLayerCollectionExtensions extensions;
	private String collection;
	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private RecordServices recordServices;
	private RMSchemasRecordsServices rm;
	private LoggingServices loggingServices;
	private DecommissioningLoggingService decommissioningLoggingService;
	private SearchServices searchServices;
	private MetadataSchemasManager metadataSchemasManager;
	private DocumentRecordActionsServices documentRecordActionsServices;

	public DocumentMenuItemActionBehaviors(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
		recordServices = modelLayerFactory.newRecordServices();
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		loggingServices = modelLayerFactory.newLoggingServices();
		decommissioningLoggingService = new DecommissioningLoggingService(appLayerFactory.getModelLayerFactory());
		extensions = modelLayerFactory.getExtensions().forCollection(collection);
		metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		documentRecordActionsServices = new DocumentRecordActionsServices(collection, appLayerFactory);
	}

	public void display(MenuItemActionBehaviorParams params) {
		Map<String, String> formParams = params.getFormParams();
		String documentId = params.getRecordVO().getId();

		RMNavigationUtils.navigateToDisplayDocument(documentId, formParams, appLayerFactory, collection);
		updateSearchResultClicked(params.getRecordVO());
	}

	public void open(MenuItemActionBehaviorParams params) {
		String agentURL = ConstellioAgentUtils.getAgentURL(params.getRecordVO(), params.getContentVersionVO());
		Page.getCurrent().open(agentURL, params.isContextualMenu() ? "_top" : null);
		loggingServices.openDocument(recordServices.getDocumentById(params.getRecordVO().getId()), params.getUser());
	}

	public void copy(MenuItemActionBehaviorParams params) {
		BaseView view = params.getView();
		Map<String, String> formParams = params.getFormParams();
		String documentId = params.getRecordVO().getId();

		boolean areSearchTypeAndSearchIdPresent = DecommissionNavUtil.areTypeAndSearchIdPresent(formParams);

		if (areSearchTypeAndSearchIdPresent) {
			view.navigate().to(RMViews.class).addDocumentWithContentFromDecommission(documentId,
					DecommissionNavUtil.getSearchId(formParams), DecommissionNavUtil.getSearchType(formParams));
		} else if (formParams != null && formParams.get(RMViews.FAV_GROUP_ID_KEY) != null) {
			view.navigate().to(RMViews.class)
					.addDocumentWithContentFromFavorites(documentId, formParams.get(RMViews.FAV_GROUP_ID_KEY));
		} else if (rmModuleExtensions.navigateToAddDocumentWhileKeepingTraceOfPreviousView(
				new NavigateToFromAPageParams(formParams, documentId))) {
		} else {
			view.navigate().to(RMViews.class).addDocumentWithContent(documentId);
		}
	}

	public void edit(MenuItemActionBehaviorParams params) {
		params.getView().navigate().to(RMViews.class).editDocument(params.getRecordVO().getId());
		updateSearchResultClicked(params.getRecordVO());
	}

	public void download(MenuItemActionBehaviorParams params) {
		ContentVersionVOResource contentVersionResource = new ContentVersionVOResource(params.getContentVersionVO());
		Resource downloadedResource = DownloadLink.wrapForDownload(contentVersionResource);
		Page.getCurrent().open(downloadedResource, null, false);
		loggingServices.downloadDocument(recordServices.getDocumentById(params.getRecordVO().getId()), params.getUser());
	}

	public void delete(MenuItemActionBehaviorParams params) {
		Document document = rm.getDocument(params.getRecordVO().getId());

		if (validateDeleteDocumentPossibleExtensively(document.getWrappedRecord(), params.getUser()).isEmpty()) {
			String parentId = document.getFolder();
			try {
				SchemaPresenterUtils presenterUtils = new SchemaPresenterUtils(Document.DEFAULT_SCHEMA,
						params.getView().getConstellioFactories(), params.getView().getSessionContext());
				presenterUtils.delete(document.getWrappedRecord(), null, true, 1);
			} catch (RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord e) {
				params.getView().showMessage(MessageUtils.toMessage(e));
				return;
			}
			if (parentId != null) {
				navigateToDisplayFolder(parentId, params.getFormParams());
			} else {
				params.getView().navigate().to().recordsManagement();
			}
		} else {
			MessageUtils.getCannotDeleteWindow(validateDeleteDocumentPossibleExtensively(
					document.getWrappedRecord(), params.getUser())).openWindow();
		}
	}

	public void finalize(MenuItemActionBehaviorParams params) {
		Document document = rm.getDocument(params.getRecordVO().getId());
		Content content = document.getContent();
		content.finalizeVersion();
		try {
			recordServices.update(document.getWrappedRecord());

			String newMajorVersion = content.getCurrentVersion().getVersion();
			loggingServices.finalizeDocument(document.getWrappedRecord(), params.getUser());
			params.getView().showMessage($("DocumentActionsComponent.finalizedVersion", newMajorVersion));
			Page.getCurrent().reload();
		} catch (RecordServicesException e) {
			params.getView().showErrorMessage(MessageUtils.toMessage(e));
		}
	}

	public void publish(MenuItemActionBehaviorParams params) {
		Document documentFromParam =  rm.getDocument(params.getRecordVO().getId());
		documentFromParam.setPublished(true);
		try {
			recordServices.update(documentFromParam);
		} catch (RecordServicesException e) {
			params.getView().showErrorMessage(MessageUtils.toMessage(e));
		}
	}

	public void createPdf(MenuItemActionBehaviorParams params) {
		Document document = rm.getDocument(params.getRecordVO().getId());
		String extention = FilenameUtils.getExtension(document.getContent().getCurrentVersion().getFilename());

		if (!extention.toUpperCase().equals("PDF") && !extention.equals("PDFA")) {
			Content content = document.getContent();
			ContentConversionManager conversionManager = new ContentConversionManager(modelLayerFactory);
			if (content != null) {
				try {
					conversionManager = new ContentConversionManager(modelLayerFactory);
					conversionManager.convertContentToPDFA(params.getUser(), content);
					recordServices.update(document.getWrappedRecord());

					decommissioningLoggingService.logPdfAGeneration(document, params.getUser());

					navigateToDisplayDocument(document.getId(), params.getFormParams());

					params.getView().showMessage($("DocumentActionsComponent.createPDFASuccess"));
				} catch (Exception e) {
					params.getView().showErrorMessage(
							$("DocumentActionsComponent.createPDFAFailure") + " : " + MessageUtils.toMessage(e));
				} finally {
					conversionManager.close();
				}
			}
		} else {
			params.getView().showMessage($("DocumentActionsComponent.documentAllreadyPDFA"));
		}
	}


	public void unPublish(MenuItemActionBehaviorParams params) {
		Document documentFromParam =  rm.getDocument(params.getRecordVO().getId());
		documentFromParam.setPublished(false);
		try {
			recordServices.update(documentFromParam);
		} catch (RecordServicesException e) {
			params.getView().showErrorMessage(MessageUtils.toMessage(e));
		}
	}

	public void addToSelection(MenuItemActionBehaviorParams params) {
		params.getView().getSessionContext().addSelectedRecordId(params.getRecordVO().getId(),
				params.getRecordVO().getSchema().getTypeCode());
	}

	public void removeToSelection(MenuItemActionBehaviorParams params) {
		params.getView().getSessionContext().removeSelectedRecordId(params.getRecordVO().getId(),
				params.getRecordVO().getSchema().getTypeCode());
	}

	public void addToCart(MenuItemActionBehaviorParams params) {
		if (params.getUser().has(RMPermissionsTo.USE_GROUP_CART).globally()) {
			addToCartWindow(params);
		} else if (params.getUser().has(RMPermissionsTo.USE_MY_CART).globally()) {
			addToDefaultCart(params);
		}
	}

	private void addToCartWindow(MenuItemActionBehaviorParams params) {
		final Document document = rm.getDocument(params.getRecordVO().getId());

		WindowButton windowButton = new WindowButton($("DisplayFolderView.addToCart"), $("DisplayFolderView.selectCart")) {
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
							createNewCartAndAddToItRequested(newCartTitleField.getValue(), document, params);
							getWindow().close();
						} catch (Exception e) {
							params.getView().showErrorMessage(MessageUtils.toMessage(e));
						}
					}
				});
				saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				TabSheet tabSheet = new TabSheet();
				Table ownedCartsTable = buildOwnedFavoritesTable(getWindow(), params, document);

				final RecordVOLazyContainer sharedCartsContainer = new RecordVOLazyContainer(getSharedCartsDataProvider(params));
				RecordVOTable sharedCartsTable = new RecordVOTable($("CartView.sharedCarts"), sharedCartsContainer);
				sharedCartsTable.addItemClickListener((ItemClickListener) event -> {
					addToCartRequested(sharedCartsContainer.getRecordVO((int) event.getItemId()), params.getView(), document);
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
		};

		windowButton.click();
	}

	public void printLabel(MenuItemActionBehaviorParams params) {

		Factory<List<LabelTemplate>> customLabelTemplatesFactory = new Factory<List<LabelTemplate>>() {
			@Override
			public List<LabelTemplate> get() {
				return appLayerFactory.getLabelTemplateManager().listExtensionTemplates(Document.SCHEMA_TYPE);
			}
		};

		Factory<List<LabelTemplate>> defaultLabelTemplatesFactory = new Factory<List<LabelTemplate>>() {
			@Override
			public List<LabelTemplate> get() {
				return appLayerFactory.getLabelTemplateManager().listTemplates(Document.SCHEMA_TYPE);
			}
		};
		SessionContext sessionContext = params.getView().getSessionContext();
		UserToVOBuilder userToVOBuilder = new UserToVOBuilder();
		UserVO userVO = userToVOBuilder.build(params.getUser().getWrappedRecord(),
				VIEW_MODE.DISPLAY, sessionContext);

		Button labels = new LabelButtonV2($("DisplayFolderView.printLabel"),
				$("DisplayFolderView.printLabel"), customLabelTemplatesFactory,
				defaultLabelTemplatesFactory, appLayerFactory,
				sessionContext.getCurrentCollection(), userVO
				, params.getRecordVO());

		labels.click();
	}

	public void checkIn(MenuItemActionBehaviorParams params) {
		Document document = rm.getDocument(params.getRecordVO().getId());

		if (documentRecordActionsServices.isCheckInActionPossible(document.getWrappedRecord(), params.getUser())) {
			UpdateContentVersionWindowImpl uploadWindow = createUpdateContentVersionWindow(params);
			uploadWindow.open(false);
		} else if (documentRecordActionsServices.isCancelCheckOutPossible(document)) {
			Content content = document.getContent();
			content.checkIn();
			modelLayerFactory.newLoggingServices().returnRecord(document.getWrappedRecord(), params.getUser());
			try {
				recordServices.update(document, new RecordUpdateOptions().setOverwriteModificationDateAndUser(false));
				DocumentVO documentVO = getDocumentVO(params, document);
				ContentVersionVO currentVersionVO = new ContentVersionToVOBuilder(modelLayerFactory)
						.build(content, params.getView().getSessionContext());
				documentVO.setContent(currentVersionVO);

				params.getView().updateUI();
				params.getView().showMessage($("DocumentActionsComponent.canceledCheckOut"));
			} catch (RecordServicesException e) {
				params.getView().showErrorMessage(MessageUtils.toMessage(e));
			}
		}
	}

	public void checkOut(MenuItemActionBehaviorParams params) {
		Document document = rm.getDocument(params.getRecordVO().getId());

		if (documentRecordActionsServices.isCheckOutActionPossible(document.getWrappedRecord(), params.getUser())) {
			updateSearchResultClicked(getDocumentVO(params, document));
			Content content = document.getContent();
			content.checkOut(params.getUser());
			modelLayerFactory.newLoggingServices().borrowRecord(document.getWrappedRecord(), params.getUser(), TimeProvider.getLocalDateTime());
			try {
				recordServices.update(document.getWrappedRecord(), new RecordUpdateOptions().setOverwriteModificationDateAndUser(false));

				DocumentVO documentVO = getDocumentVO(params, document);

				String checkedOutVersion = content.getCurrentVersion().getVersion();
				params.getView().showMessage($("DocumentActionsComponent.checkedOut", checkedOutVersion));
				String agentURL = ConstellioAgentUtils.getAgentURL(documentVO, documentVO.getContent(), params.getView().getSessionContext());
				if (agentURL != null) {
					Page.getCurrent().open(agentURL, null);
					loggingServices.openDocument(document.getWrappedRecord(), params.getUser());
				}
			} catch (RecordServicesException e) {
				params.getView().showErrorMessage(MessageUtils.toMessage(e));
			}
		}
	}

	public void addAuthorization(MenuItemActionBehaviorParams params) {
		params.getView().navigate().to().listObjectAccessAndRoleAuthorizations(params.getRecordVO().getId());
		updateSearchResultClicked(params.getRecordVO());
	}

	public void reportGeneratorButton(MenuItemActionBehaviorParams params) {
		RMSelectionPanelReportPresenter rmSelectionPanelReportPresenter = new RMSelectionPanelReportPresenter(appLayerFactory, collection, params.getUser()) {
			@Override
			public String getSelectedSchemaType() {
				return Document.SCHEMA_TYPE;
			}

			@Override
			public List<String> getSelectedRecordIds() {
				return asList(params.getRecordVO().getId());
			}
		};

		ReportTabButton reportGeneratorButton = new ReportTabButton($("SearchView.metadataReportTitle"),
				$("SearchView.metadataReportTitle"),
				appLayerFactory, collection, false, false,
				rmSelectionPanelReportPresenter, params.getView().getSessionContext()) {

		};

		reportGeneratorButton.setRecordVoList(params.getRecordVO());
		reportGeneratorButton.click();
	}

	private DocumentVO getDocumentVO(MenuItemActionBehaviorParams params, Document document) {
		return new DocumentToVOBuilder(modelLayerFactory).build(document.getWrappedRecord(),
				VIEW_MODE.DISPLAY, params.getView().getSessionContext());
	}

	private void addToCartRequested(RecordVO recordVO, BaseView baseView, Document document) {
		Cart cart = rm.getCart(recordVO.getId());
		addToCartRequested(cart, baseView, document);
	}

	private DefaultFavoritesTable buildOwnedFavoritesTable(final Window window, MenuItemActionBehaviorParams params,
														   Document document) {
		List<CartItem> cartItems = new ArrayList<>();
		if(hasCurrentUserPermissionToUseMyCart(params.getUser())) {
			cartItems.add(new DefaultFavoritesTable.CartItem($("CartView.defaultFavorites")));
		}
		for (Cart cart : getOwnedCarts(params.getUser())) {
			cartItems.add(new DefaultFavoritesTable.CartItem(cart, cart.getTitle()));
		}
		final DefaultFavoritesTable.FavoritesContainer container = new DefaultFavoritesTable.FavoritesContainer(DefaultFavoritesTable.CartItem.class, cartItems);
		DefaultFavoritesTable defaultFavoritesTable = new DefaultFavoritesTable("favoritesTable", container, getCartMetadataSchemaVO(params.getView().getSessionContext()));
		defaultFavoritesTable.setCaption($("CartView.ownedCarts"));
		defaultFavoritesTable.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				Cart cart = container.getCart((DefaultFavoritesTable.CartItem) event.getItemId());
				if (cart == null) {
					addToDefaultCart(params, document);
				} else {
					addToCartRequested(cart, params.getView(), document);
				}
				window.close();
			}
		});
		defaultFavoritesTable.setPageLength(Math.min(15, container.size()));
		container.removeContainerProperty(DefaultFavoritesTable.CartItem.DISPLAY_BUTTON);
		defaultFavoritesTable.setWidth("100%");
		return defaultFavoritesTable;
	}

	private MetadataSchemaVO getCartMetadataSchemaVO(SessionContext sessionContext) {
		return new MetadataSchemaToVOBuilder().build(metadataSchemasManager.getSchemaTypes(sessionContext.getCurrentCollection())
				.getDefaultSchema(Cart.SCHEMA_TYPE), RecordVO.VIEW_MODE.TABLE, sessionContext);
	}

	private void addToCartRequested(Cart cart, BaseView baseView,  Document document) {
		if (rm.numberOfDocumentsInFavoritesReachesLimit(cart.getId(), 1)) {
			baseView.showMessage($("DisplayDocumentView.cartCannotContainMoreThanAThousandDocuments"));
		} else {
			document.addFavorite(cart.getId());
			Transaction transaction = new Transaction(RecordUpdateOptions.validationExceptionSafeOptions());
			transaction.addUpdate(document.getWrappedRecord());
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

	private void addToDefaultCart(MenuItemActionBehaviorParams params, Document document) {
		if (rm.numberOfDocumentsInFavoritesReachesLimit(params.getUser().getId(), 1)) {
			params.getView().showMessage($("DisplayDocumentView.cartCannotContainMoreThanAThousandDocuments"));
		} else {
			document.addFavorite(params.getUser().getId());
			try {
				recordServices.update(document.getWrappedRecord(), RecordUpdateOptions.validationExceptionSafeOptions());
			} catch (RecordServicesException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			params.getView().showMessage($("DisplayDocumentView.documentAddedToDefaultFavorites"));
		}
	}

	public void addToDefaultCart(MenuItemActionBehaviorParams params) {
		Document document = rm.getDocument(params.getRecordVO().getId());

		addToDefaultCart(params, document);
	}

	public void upload(MenuItemActionBehaviorParams params) {
		UpdateContentVersionWindowImpl uploadWindow = createUpdateContentVersionWindow(params);

		uploadWindow.open(false);
	}

	@NotNull
	private UpdateContentVersionWindowImpl createUpdateContentVersionWindow(MenuItemActionBehaviorParams params) {
		final Map<RecordVO, MetadataVO> recordMap = new HashMap<>();
		recordMap.put(params.getRecordVO(), params.getRecordVO().getMetadata(Document.CONTENT));

		return new UpdateContentVersionWindowImpl(recordMap) {
			@Override
			public void close() {
				super.close();
				params.getView().updateUI();
			}
		};
	}

	private void createNewCartAndAddToItRequested(String title, Document document,
												  MenuItemActionBehaviorParams params) {
		Cart cart = rm.newCart();
		cart.setTitle(title);
		cart.setOwner(params.getUser());
		document.addFavorite(cart.getId());
		try {
			recordServices.execute(new Transaction(cart.getWrappedRecord()).setUser(params.getUser()));
			recordServices.update(document.getWrappedRecord(), RecordUpdateOptions.validationExceptionSafeOptions());
			params.getView().showMessage($("DocumentActionsComponent.addedToCart"));
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}
	}

	private RecordVODataProvider getSharedCartsDataProvider(MenuItemActionBehaviorParams params) {
		MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
		final MetadataSchemaVO cartSchemaVO = schemaVOBuilder.build(rm.cartSchema(), VIEW_MODE.TABLE, params.getView().getSessionContext());
		return new RecordVODataProvider(cartSchemaVO, new RecordToVOBuilder(), modelLayerFactory, params.getView().getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(rm.cartSchema()).where(rm.cartSharedWithUsers())
						.isContaining(asList(params.getUser().getId()))).sortAsc(Schemas.TITLE);
			}
		};
	}

	private void updateSearchResultClicked(RecordVO recordVO) {
		if (Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()) {
			ConstellioUI.getCurrent().setAttribute(SEARCH_EVENT_DWELL_TIME, System.currentTimeMillis());

			SearchEventServices searchEventServices = new SearchEventServices(collection, modelLayerFactory);
			SearchEvent searchEvent = ConstellioUI.getCurrentSessionContext().getAttribute(CURRENT_SEARCH_EVENT);

			if (searchEvent != null) {
				searchEventServices.incrementClickCounter(searchEvent.getId());

				String url = null;
				try {
					url = recordVO.get("url");
				} catch (RecordVORuntimeException_NoSuchMetadata ignored) {
				}
				String clicks = defaultIfBlank(url, recordVO.getId());
				searchEventServices.updateClicks(searchEvent, clicks);
			}
		}
	}


	private ValidationErrors validateDeleteDocumentPossibleExtensively(Record record, User user) {
		ValidationErrors validationErrors = new ValidationErrors();
		validationErrors.addAll(validateDeleteDocumentPossible(record, user).getValidationErrors());
		validationErrors.addAll(recordServices.validateLogicallyDeletable(record, user).getValidationErrors());
		return validationErrors;
	}

	private ValidationErrors validateDeleteDocumentPossible(Record record, User user) {
		ValidationErrors validationErrors = new ValidationErrors();
		boolean userHasDeleteAccess = user.hasDeleteAccess().on(record);
		if (!userHasDeleteAccess) {
			validationErrors.add(DocumentActionsPresenterUtils.class, "userDoesNotHaveDeleteAccess");
		} else {
			validationErrors = extensions.validateDeleteAuthorized(record, user);
		}
		return validationErrors;
	}

	private void navigateToDisplayDocument(String documentId, Map<String, String> params) {
		RMNavigationUtils.navigateToDisplayDocument(documentId, params, appLayerFactory, collection);
	}

	private void navigateToDisplayFolder(String folderId, Map<String, String> params) {
		RMNavigationUtils.navigateToDisplayFolder(folderId, params, appLayerFactory, collection);
	}

}
