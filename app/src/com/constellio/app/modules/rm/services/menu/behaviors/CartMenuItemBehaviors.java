package com.constellio.app.modules.rm.services.menu.behaviors;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateManager;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.actions.CartRecordActionsServices;
import com.constellio.app.modules.rm.services.cart.CartEmailService;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.services.menu.behaviors.ui.CartBatchProcessingPresenter;
import com.constellio.app.modules.rm.services.menu.behaviors.ui.CartBatchProcessingViewImpl;
import com.constellio.app.modules.rm.ui.pages.cart.RenameDialog;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.utils.CartUtil;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DeleteWithJustificationButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.report.LabelButtonV2;
import com.constellio.app.ui.framework.components.ReportViewer.DownloadStreamResource;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupFieldWithIgnoreOneRecord;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingButton;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingModifyingOneMetadataButton;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.enums.BatchProcessingMode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.emails.EmailServices.EmailMessage;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.jgoodies.common.base.Strings;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.enums.BatchProcessingMode.ALL_METADATA_OF_SCHEMA;
import static com.constellio.model.entities.enums.BatchProcessingMode.ONE_METADATA;

public class CartMenuItemBehaviors {

	private RMModuleExtensions rmModuleExtensions;
	private ModelLayerCollectionExtensions modelCollectionExtensions;
	private String collection;
	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private RecordServices recordServices;
	private RMSchemasRecordsServices rm;
	private LoggingServices loggingServices;
	private SearchServices searchServices;
	private DecommissioningService decommissioningService;
	private Navigation navigation;
	private CartUtil cartUtil;
	private CartRecordActionsServices cartRecordActionsServices;
	private static Logger LOGGER = LoggerFactory.getLogger(CartMenuItemBehaviors.class);

	public CartMenuItemBehaviors(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		this.rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
		this.recordServices = modelLayerFactory.newRecordServices();
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.loggingServices = modelLayerFactory.newLoggingServices();
		this.modelCollectionExtensions = modelLayerFactory.getExtensions().forCollection(collection);
		this.decommissioningService = new DecommissioningService(collection, appLayerFactory);
		this.navigation = new Navigation();
		this.cartUtil = new CartUtil(collection, appLayerFactory);
		this.cartRecordActionsServices = new CartRecordActionsServices(collection, appLayerFactory);
	}

	public void renameAction(MenuItemActionBehaviorParams params) {
		Cart cart = rm.getCart(params.getRecordVO().getId());

		RenameDialog button = new RenameDialog(null,
				$("CartView.reNameCartGroup"),
				$("CartView.reNameCartGroup"), false) {
			@Override
			public void save(String newTitle) {
				if (renameFavoriteGroup(newTitle, cart, params.getView())) {
					getWindow().close();
					navigation.to(RMViews.class).cart(cart.getId());
				}
			}
		};

		if (!cart.getId().equals(params.getUser().getId())) {
			button.setOriginalValue(cart.getTitle());
		}

		button.click();
	}

	private boolean renameFavoriteGroup(String name, Cart cart, BaseView baseView) {

		if (Strings.isNotBlank(name)) {
			try {
				cart.setTitle(name);
				recordServices.update(cart.getWrappedRecord());
			} catch (RecordServicesException e) {
				throw new RuntimeException("Unexpected error when updating cart");
			}
		} else {
			baseView.showErrorMessage(i18n.$("requiredFieldWithName", i18n.$("title")));
			return false;
		}

		return true;
	}

	public void prepareEmailAction(MenuItemActionBehaviorParams params) {
		Cart cart = rm.getCart(params.getRecordVO().getId());

		EmailMessage emailMessage = new CartEmailService(collection, modelLayerFactory)
				.createEmailForCart(cartUtil.cartOwner(params.getUser(), cart), cartUtil.getCartDocumentIds(cart.getId()), params.getUser());
		String filename = emailMessage.getFilename();
		InputStream stream = emailMessage.getInputStream();
		startDownload(stream, filename);
	}

	public void startDownload(final InputStream stream, String filename) {
		Resource resource = new DownloadStreamResource(new StreamSource() {
			@Override
			public InputStream getStream() {
				return stream;
			}
		}, filename);
		Page.getCurrent().open(resource, null, false);
	}

	public void batchDuplicateAction(MenuItemActionBehaviorParams params) {
		Cart cart = rm.getCart(params.getRecordVO().getId());

		if (!cartRecordActionsServices.isBatchDuplicateActionPossible(cart.getWrappedRecord(), params.getUser())) {
			params.getView().showErrorMessage($("CartView.cannotDuplicate"));
			return;
		}
		List<Folder> folders = cartUtil.getCartFolders(cart.getId());
		for (Folder folder : folders) {
			if (!rmModuleExtensions.isCopyActionPossibleOnFolder(folder, params.getUser())) {
				params.getView().showErrorMessage($("CartView.actionBlockedByExtension"));
				return;
			}
		}

		try {
			DecommissioningService service = new DecommissioningService(params.getView().getCollection(), appLayerFactory);
			for (Folder folder : folders) {
				if (!folder.isLogicallyDeletedStatus()) {
					service.duplicateStructureAndSave(folder, params.getUser());
				}
			}
			params.getView().showMessage($("CartView.duplicated"));
		} catch (RecordServicesException.ValidationException e) {
			params.getView().showErrorMessage($(e.getErrors()));
		} catch (Exception e) {
			params.getView().showErrorMessage(e.getMessage());
		}
	}

	public void documentBatchProcessingAction(MenuItemActionBehaviorParams params) {
		Button button = buildBatchProcessingButton(Document.SCHEMA_TYPE, params);

		button.click();
	}

	public void folderBatchProcessingAction(MenuItemActionBehaviorParams params) {

		Button button = buildBatchProcessingButton(Folder.SCHEMA_TYPE, params);

		button.click();
	}

	public void containerRecordBatchProcessingAction(MenuItemActionBehaviorParams params) {

		Button button = buildBatchProcessingButton(ContainerRecord.SCHEMA_TYPE, params);

		button.click();
	}

	private Button buildBatchProcessingButton(final String schemaType, MenuItemActionBehaviorParams params) {
		CartBatchProcessingPresenter cartBatchProcessingPresenter = new CartBatchProcessingPresenter(appLayerFactory,
				params.getUser(),
				params.getRecordVO().getId(), params.getView());

		BatchProcessingMode mode = cartBatchProcessingPresenter.getBatchProcessingMode();
		WindowButton button;
		if (mode.equals(ALL_METADATA_OF_SCHEMA)) {
			button = new BatchProcessingButton(cartBatchProcessingPresenter, new CartBatchProcessingViewImpl(schemaType, cartBatchProcessingPresenter));
		} else if (mode.equals(ONE_METADATA)) {
			button = new BatchProcessingModifyingOneMetadataButton(cartBatchProcessingPresenter, new CartBatchProcessingViewImpl(schemaType, cartBatchProcessingPresenter));
		} else {
			throw new RuntimeException("Unsupported mode " + mode);
		}

		return button;
	}

	public void foldersLabelsAction(MenuItemActionBehaviorParams params) {
		Button button = buildLabelsButton(Folder.SCHEMA_TYPE, params);
		button.click();
	}

	public void documentLabelsAction(MenuItemActionBehaviorParams params) {
		Button button = buildLabelsButton(Document.SCHEMA_TYPE, params);
		button.click();
	}

	public void containerLabelsAction(MenuItemActionBehaviorParams params) {
		Button button = buildLabelsButton(ContainerRecord.SCHEMA_TYPE, params);
		button.click();
	}

	public List<LabelTemplate> getCustomTemplates(String schemaType) {
		LabelTemplateManager labelTemplateManager = appLayerFactory.getLabelTemplateManager();
		return labelTemplateManager.listExtensionTemplates(schemaType);
	}

	public List<LabelTemplate> getDefaultTemplates(String schemaType) {
		LabelTemplateManager labelTemplateManager = appLayerFactory.getLabelTemplateManager();
		return labelTemplateManager.listTemplates(schemaType);
	}

	private Button buildLabelsButton(final String schemaType, MenuItemActionBehaviorParams params) {
		Factory<List<LabelTemplate>> customLabelTemplatesFactory = new Factory<List<LabelTemplate>>() {
			@Override
			public List<LabelTemplate> get() {
				return getCustomTemplates(schemaType);
			}
		};
		Factory<List<LabelTemplate>> defaultLabelTemplatesFactory = new Factory<List<LabelTemplate>>() {
			@Override
			public List<LabelTemplate> get() {
				return getDefaultTemplates(schemaType);
			}
		};
		SessionContext sessionContext = params.getView().getSessionContext();
		LabelButtonV2 labelsButton = new LabelButtonV2(
				$("SearchView.labels"),
				$("SearchView.printLabels"),
				customLabelTemplatesFactory,
				defaultLabelTemplatesFactory,
				appLayerFactory,
				params.getView().getCollection(),
				sessionContext.getCurrentUser()
		);

		labelsButton.setElementsWithIds(cartUtil.getNotDeletedRecordsIds(schemaType, params.getUser(),
				params.getRecordVO().getId()), schemaType, sessionContext);

		return labelsButton;
	}

	public void batchDeleteAction(MenuItemActionBehaviorParams params) {
		Cart cart = rm.getCart(params.getRecordVO().getId());

		Button button;
		if (!isNeedingAReasonToDeleteRecords()) {
			button = new DeleteButton(false) {
				@Override
				protected void confirmButtonClick(ConfirmDialog dialog) {
					deletionRequested(null, params);
				}

				@Override
				protected String getConfirmDialogMessage() {
					List<String> cartFolderIds = cartUtil.getCartFolderIds(cart.getId());
					List<String> cartDocumentIds = cartUtil.getCartDocumentIds(cart.getId());

					StringBuilder stringBuilder = new StringBuilder();
					String prefix = "";
					if (cartFolderIds != null && !cartFolderIds.isEmpty()) {
						stringBuilder.append(prefix + cartFolderIds.size() + " " + $("CartView.folders"));
						prefix = " " + $("CartView.andAll") + " ";
					}
					if (cartDocumentIds != null && !cartDocumentIds.isEmpty()) {
						stringBuilder.append(prefix + cartDocumentIds.size() + " " + $("CartView.documents"));
					}
					return $("CartView.deleteConfirmationMessageWithoutJustification", stringBuilder.toString());
				}
			};
		} else {
			button = new DeleteWithJustificationButton(false) {
				@Override
				protected void deletionConfirmed(String reason) {
					deletionRequested(reason, params);
				}

				@Override
				protected String getConfirmDialogMessage() {
					List<String> cartFolderIds = cartUtil.getCartFolderIds(cart.getId());
					List<String> cartDocumentIds = cartUtil.getCartDocumentIds(cart.getId());

					StringBuilder stringBuilder = new StringBuilder();
					String prefix = "";
					if (cartFolderIds != null && !cartFolderIds.isEmpty()) {
						stringBuilder.append(prefix + cartFolderIds.size() + " " + $("CartView.folders"));
						prefix = " " + $("CartView.andAll") + " ";
					}
					if (cartDocumentIds != null && !cartDocumentIds.isEmpty()) {
						stringBuilder.append(prefix + cartDocumentIds.size() + " " + $("CartView.documents"));
					}
					return $("CartView.deleteConfirmationMessage", stringBuilder.toString());
				}
			};
		}

		button.click();
	}

	public void deletionRequested(String reason, MenuItemActionBehaviorParams params) {
		Cart cart = rm.getCart(params.getRecordVO().getId());

		if (!cartRecordActionsServices.isBatchDeleteActionPossible(rm.get(params.getRecordVO().getId()), params.getUser())) {
			params.getView().showErrorMessage($("CartView.cannotDelete"));
			return;
		}
		for (Record record : recordServices.getRecordsById(params.getView().getCollection(), getAllCartItems(cart.getId()))) {
			ValidationErrors validateDeleteAuthorized = modelCollectionExtensions.validateDeleteAuthorized(record, params.getUser());
			if (!validateDeleteAuthorized.isEmpty()) {
				MessageUtils.getCannotDeleteWindow(validateDeleteAuthorized).openWindow();
				return;
			}
		}

		for (Record record : recordServices.getRecordsById(params.getView().getCollection(), getAllCartItems(cart.getId()))) {
			delete(record, reason, params);
		}
		cartEmptyingRequested(cart.getId(), params.getView());
	}

	public void cartEmptyingRequested(String cartId, BaseView view) {
		List<Record> records = cartUtil.getCartRecords(cartId);
		for (Record record : records) {
			removeFromFavorite(record, cartId);
		}
		Transaction transaction = new Transaction();
		transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		transaction.setOptions(RecordUpdateOptions.validationExceptionSafeOptions());
		transaction.addUpdate(records);
		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		view.navigate().to(RMViews.class).cart(cartId);
	}

	private void removeFromFavorite(Record record, String cartId) {
		String schemaCode = record.getSchemaCode();

		if (schemaCode.startsWith(Folder.SCHEMA_TYPE)) {
			Folder folder = rm.wrapFolder(record);
			folder.removeFavorite(cartId);
		} else if (schemaCode.startsWith(Document.SCHEMA_TYPE)) {
			Document document = rm.wrapDocument(record);
			document.removeFavorite(cartId);
		} else if (schemaCode.startsWith(ContainerRecord.SCHEMA_TYPE)) {
			ContainerRecord containerRecord = rm.wrapContainerRecord(record);
			containerRecord.removeFavorite(cartId);
		}
	}

	protected final void delete(Record record, String reason,
								MenuItemActionBehaviorParams menuItemActionBehaviorParams) {
		delete(record, reason, true, false, menuItemActionBehaviorParams);
	}


	protected final void delete(Record record, String reason, boolean physically, boolean throwException,
								MenuItemActionBehaviorParams params) {
		SchemaPresenterUtils presenterUtils = new SchemaPresenterUtils(Document.DEFAULT_SCHEMA,
				params.getView().getConstellioFactories(), params.getView().getSessionContext());
		presenterUtils.delete(record, null, true, 1);
	}

	public List<String> getAllCartItems(String cartId) {
		List<String> result = new ArrayList<>();

		result.addAll(cartUtil.getCartFolderIds(cartId));
		result.addAll(cartUtil.getCartDocumentIds(cartId));
		result.addAll(cartUtil.getCartContainersIds(cartId));

		return result;
	}

	public boolean isNeedingAReasonToDeleteRecords() {
		return new RMConfigs(modelLayerFactory.getSystemConfigurationsManager()).isNeedingAReasonBeforeDeletingFolders();
	}

	// Message de confirmation ne pas oublié.
	public void emptyAction(MenuItemActionBehaviorParams params) {
		Cart cart = rm.getCart(params.getRecordVO().getId());

		List<Record> records = cartUtil.getCartRecords(cart.getId());
		for (Record record : records) {
			removeFromFavorite(record, cart.getId());
		}
		Transaction transaction = new Transaction();
		transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		transaction.setOptions(RecordUpdateOptions.validationExceptionSafeOptions());
		transaction.addUpdate(records);
		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		params.getView().navigate().to(RMViews.class).cart(cart.getId());
	}

	public void shareAction(MenuItemActionBehaviorParams params) {
		Cart cart = rm.getCart(params.getRecordVO().getId());


		Button shareButton = new WindowButton($("CartView.share"), $("CartView.shareWindow")) {
			@Override
			protected Component buildWindowContent() {
				VerticalLayout layout = new VerticalLayout();
				layout.setSpacing(true);

				final ListAddRemoveRecordLookupFieldWithIgnoreOneRecord lookup =
						new ListAddRemoveRecordLookupFieldWithIgnoreOneRecord(User.SCHEMA_TYPE, params.getUser().getId());
				lookup.setValue(cart.getSharedWithUsers());

				layout.addComponent(lookup);

				BaseButton saveButton = new BaseButton($("save")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						shareWithUsersRequested(lookup.getValue(), params);
						getWindow().close();
					}
				};
				saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
				layout.addComponent(saveButton);
				return layout;
			}
		};

		shareButton.click();
	}

	public void shareWithUsersRequested(List<String> userids, MenuItemActionBehaviorParams params) {
		Cart cart = rm.getCart(params.getRecordVO().getId());

		List<Folder> folders = cartUtil.getCartFolders(cart.getId());
		for (Folder folder : folders) {
			if (!rmModuleExtensions.isShareActionPossibleOnFolder(folder, params.getUser())) {
				params.getView().showErrorMessage($("CartView.actionBlockedByExtension"));
				return;
			}
		}
		List<Document> documents = cartUtil.getCartDocuments(cart.getId());
		for (Document document : documents) {
			if (!rmModuleExtensions.isShareActionPossibleOnDocument(document, params.getUser())) {
				params.getView().showErrorMessage($("CartView.actionBlockedByExtension"));
				return;
			}
		}
		cart.setSharedWithUsers(userids);

		Transaction transaction = new Transaction();
		transaction.setUser(params.getUser());
		transaction.addUpdate(cart.getWrappedRecord());

		try {
			recordServices.executeHandlingImpactsAsync(transaction);
		} catch (RecordServicesException e) {
			Exception nestedException;
			if (e instanceof RecordServicesException.ValidationException) {
				LOGGER.error(e.getMessage(), e);
				nestedException = new ValidationException(((RecordServicesException.ValidationException) e).getErrors());
			} else {
				nestedException = e;
			}
			throw new RuntimeException(nestedException);
		}
	}


	public void decommissionAction(MenuItemActionBehaviorParams params) {
		//		WindowButton windowButton = new WindowButton($("CartView.decommissioningList"), $("CartView.createDecommissioningList")) {
		//
		//			@Override
		//			public void buttonClick(ClickEvent event) {
		//				if (!presenter.isSubFolderDecommissioningAllowed() && presenter.isAnyFolderASubFolder()) {
		//					showErrorMessage($("CartView.cannotDecommissionSubFolder"));
		//				} else if (presenter.getCommonAdministrativeUnit(presenter.getCartFolders()) == null) {
		//					showErrorMessage($("CartView.foldersFromDifferentAdminUnits"));
		//				} else if (presenter.getCommonDecommissioningListTypes(presenter.getCartFolders()).isEmpty()) {
		//					showErrorMessage($("CartView.foldersShareNoCommonDecommisioningTypes"));
		//				} else if (presenter.isAnyFolderBorrowed()) {
		//					showErrorMessage($("CartView.aFolderIsBorrowed"));
		//				} else if (presenter.isAnyFolderInDecommissioningList()) {
		//					showErrorMessage($("CartView.aFolderIsInADecommissioningList"));
		//				} else if (presenter.isDecommissioningActionPossible()) {
		//					super.buttonClick(event);
		//				}
		//			}
		//
		//			@Override
		//			protected Component buildWindowContent() {
		//				VerticalLayout layout = new VerticalLayout();
		//
		//				final BaseTextField titleField = new BaseTextField($("title"));
		//				layout.addComponent(titleField);
		//
		//				final EnumWithSmallCodeComboBox<DecommissioningListType> decomTypeField = new EnumWithSmallCodeComboBox<>(DecommissioningListType.class);
		//				decomTypeField.removeAllItems();
		//				decomTypeField.addItems(presenter.getCommonDecommissioningListTypes(presenter.getCartFolders()));
		//				decomTypeField.setCaption($("CartView.decommissioningTypeField"));
		//				layout.addComponent(decomTypeField);
		//
		//				BaseButton saveButton = new BaseButton($("save")) {
		//					@Override
		//					protected void buttonClick(ClickEvent event) {
		//						if (StringUtils.isBlank(titleField.getValue())) {
		//							showErrorMessage($("CartView.decommissioningListIsMissingTitle"));
		//							return;
		//						}
		//						if (decomTypeField.getValue() == null) {
		//							showErrorMessage($("CartView.decommissioningListIsMissingType"));
		//							return;
		//						}
		//						presenter.buildDecommissioningListRequested(titleField.getValue(), (DecommissioningListType) decomTypeField.getValue());
		//						getWindow().close();
		//					}
		//				};
		//				saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		//				layout.addComponent(saveButton);
		//				layout.setSpacing(true);
		//				return layout;
		//			}
		//		};
		//
		//		windowButton.click();
	}

	public void printMetadataReportAction(MenuItemActionBehaviorParams params) {

	}

	public void createSIPArchvesAction(MenuItemActionBehaviorParams params) {

	}

	public void consolidatedPdfAction(MenuItemActionBehaviorParams params) {

	}
}
