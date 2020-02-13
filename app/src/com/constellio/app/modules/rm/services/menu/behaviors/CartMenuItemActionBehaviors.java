package com.constellio.app.modules.rm.services.menu.behaviors;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.enums.DecomListStatus;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateManager;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.reports.builders.search.SearchResultReportParameters;
import com.constellio.app.modules.rm.reports.builders.search.SearchResultReportWriterFactory;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.actions.CartActionsServices;
import com.constellio.app.modules.rm.services.cart.CartEmailService;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.services.menu.behaviors.ui.CartBatchProcessingPresenter;
import com.constellio.app.modules.rm.services.menu.behaviors.ui.CartBatchProcessingViewImpl;
import com.constellio.app.modules.rm.services.menu.behaviors.util.RMMessageUtil;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.builders.FolderToVOBuilder;
import com.constellio.app.modules.rm.ui.buttons.RenameDialogButton;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.entities.FolderVO;
import com.constellio.app.modules.rm.ui.pages.cart.CartView;
import com.constellio.app.modules.rm.ui.pages.pdf.ConsolidatedPdfButton;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailStatus;
import com.constellio.app.modules.rm.wrappers.utils.CartUtil;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DeleteWithJustificationButton;
import com.constellio.app.ui.framework.buttons.SIPButton.SIPButtonImpl;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.report.LabelButtonV2;
import com.constellio.app.ui.framework.components.NewReportPresenter;
import com.constellio.app.ui.framework.components.ReportTabButton;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeComboBox;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupFieldWithIgnoreOneRecord;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWithCaptionVO;
import com.constellio.app.ui.framework.stream.DownloadStreamResource;
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
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.emails.EmailServices.EmailMessage;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.reports.ReportServices;
import com.constellio.model.services.search.SearchServices;
import com.jgoodies.common.base.Strings;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.constellio.app.modules.rm.model.enums.FolderStatus.ACTIVE;
import static com.constellio.app.modules.rm.model.enums.FolderStatus.SEMI_ACTIVE;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.enums.BatchProcessingMode.ALL_METADATA_OF_SCHEMA;
import static com.constellio.model.entities.enums.BatchProcessingMode.ONE_METADATA;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

@Slf4j
public class CartMenuItemActionBehaviors {

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
	private CartActionsServices cartActionsServices;

	public CartMenuItemActionBehaviors(String collection, AppLayerFactory appLayerFactory) {
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
		this.cartActionsServices = new CartActionsServices(collection, appLayerFactory);
	}

	public void rename(Cart cart, MenuItemActionBehaviorParams params) {
		RenameDialogButton button = new RenameDialogButton(null,
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

	public void prepareEmail(Cart cart, MenuItemActionBehaviorParams params) {
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

	public void batchDuplicate(Cart cart, MenuItemActionBehaviorParams params) {
		if (!cartActionsServices.isBatchDuplicateActionPossible(cart.getWrappedRecord(), params.getUser())) {
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

	public void documentBatchProcessing(Cart cart, MenuItemActionBehaviorParams params) {
		Button button = buildBatchProcessingButton(Document.SCHEMA_TYPE, cart.getId(), params);

		button.click();
	}

	public void folderBatchProcessing(Cart cart, MenuItemActionBehaviorParams params) {

		Button button = buildBatchProcessingButton(Folder.SCHEMA_TYPE, cart.getId(), params);

		button.click();
	}

	public void containerRecordBatchProcessing(Cart cart, MenuItemActionBehaviorParams params) {

		Button button = buildBatchProcessingButton(ContainerRecord.SCHEMA_TYPE, cart.getId(), params);

		button.click();
	}

	private Button buildBatchProcessingButton(final String schemaType, String cartId,
											  MenuItemActionBehaviorParams params) {
		CartBatchProcessingPresenter cartBatchProcessingPresenter =
				new CartBatchProcessingPresenter(appLayerFactory, params.getUser(), cartId, params.getView());

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

	public void foldersLabels(Cart cart, MenuItemActionBehaviorParams params) {
		Button button = buildLabelsButton(Folder.SCHEMA_TYPE, cart.getId(), params);
		button.click();
	}

	public void documentLabels(Cart cart, MenuItemActionBehaviorParams params) {
		Button button = buildLabelsButton(Document.SCHEMA_TYPE, cart.getId(), params);
		button.click();
	}

	public void containerRecordLabels(Cart cart, MenuItemActionBehaviorParams params) {
		Button button = buildLabelsButton(ContainerRecord.SCHEMA_TYPE, cart.getId(), params);
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

	private Button buildLabelsButton(final String schemaType, String cartId, MenuItemActionBehaviorParams params) {
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
				$("SearchView.printLabels"),
				$("SearchView.printLabels"),
				customLabelTemplatesFactory,
				defaultLabelTemplatesFactory,
				appLayerFactory,
				params.getView().getCollection(),
				sessionContext.getCurrentUser()
		);

		labelsButton.setElementsWithIds(cartUtil.getNotDeletedRecordsIds(schemaType, params.getUser(), cartId),
				schemaType, sessionContext);

		return labelsButton;
	}

	public void batchDelete(Cart cart, MenuItemActionBehaviorParams params) {
		Button button;
		if (!isNeedingAReasonToDeleteRecords()) {
			button = new DeleteButton(false) {
				@Override
				protected void confirmButtonClick(ConfirmDialog dialog) {
					deletionRequested(null, cart, params);
				}

				@Override
				protected String getConfirmDialogMessage() {
					StringBuilder stringBuilder = getRecordCountByTypeAsText(cart);

					return $("CartView.deleteConfirmationMessageWithoutJustification", stringBuilder.toString());
				}
			};
		} else {
			button = new DeleteWithJustificationButton(false) {
				@Override
				protected void deletionConfirmed(String reason) {
					deletionRequested(reason, cart, params);
				}

				@Override
				protected String getConfirmDialogMessage() {
					StringBuilder stringBuilder = getRecordCountByTypeAsText(cart);

					return $("CartView.deleteConfirmationMessage", stringBuilder.toString());
				}
			};

			((DeleteWithJustificationButton) button).setMessageContentMode(ContentMode.HTML);
		}

		button.click();
	}

	private StringBuilder getRecordCountByTypeAsText(Cart cart) {
		return RMMessageUtil.getRecordCountByTypeAsText(cartUtil.getCartFolderIds(cart.getId()).size(),
				cartUtil.getCartDocumentIds(cart.getId()).size(), cartUtil.getCartContainersIds(cart.getId()).size());
	}

	public void deletionRequested(String reason, Cart cart, MenuItemActionBehaviorParams params) {
		if (!cartActionsServices.isBatchDeleteActionPossible(cart.getWrappedRecord(), params.getUser())) {
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
	public void empty(Cart cart, MenuItemActionBehaviorParams params) {

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

	public void share(Cart cart, MenuItemActionBehaviorParams params) {

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
						shareWithUsersRequested(lookup.getValue(), cart, params);
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

	public void shareWithUsersRequested(List<String> userids, Cart cart, MenuItemActionBehaviorParams params) {
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
				log.error(e.getMessage(), e);
				nestedException = new ValidationException(((RecordServicesException.ValidationException) e).getErrors());
			} else {
				nestedException = e;
			}
			throw new RuntimeException(nestedException);
		}
	}


	public void decommission(Cart cart, MenuItemActionBehaviorParams params) {
		WindowButton windowButton = new WindowButton($("CartView.decommissioningList"), $("CartView.createDecommissioningList")) {

			@Override
			public void buttonClick(ClickEvent event) {
				if (!isSubFolderDecommissioningAllowed() && isAnyFolderASubFolder(cart.getId())) {
					params.getView().showErrorMessage($("CartView.cannotDecommissionSubFolder"));
				} else if (getCommonAdministrativeUnit(cartUtil.getCartFolders(cart.getId())) == null) {
					params.getView().showErrorMessage($("CartView.foldersFromDifferentAdminUnits"));
				} else if (getCommonDecommissioningListTypes(cartUtil.getCartFolders(cart.getId())).isEmpty()) {
					params.getView().showErrorMessage($("CartView.foldersShareNoCommonDecommisioningTypes"));
				} else if (isAnyFolderBorrowed(cart.getId())) {
					params.getView().showErrorMessage($("CartView.aFolderIsBorrowed"));
				} else if (isAnyFolderInDecommissioningList(cart.getId())) {
					params.getView().showErrorMessage($("CartView.aFolderIsInADecommissioningList"));
				} else if (isDecommissioningActionPossible(cart.getId(), params.getUser(), params.getView())) {
					super.buttonClick(event);
				}
			}

			@Override
			protected Component buildWindowContent() {
				VerticalLayout layout = new VerticalLayout();

				final BaseTextField titleField = new BaseTextField($("title"));
				layout.addComponent(titleField);

				final EnumWithSmallCodeComboBox<DecommissioningListType> decomTypeField = new EnumWithSmallCodeComboBox<>(DecommissioningListType.class);
				decomTypeField.removeAllItems();
				decomTypeField.addItems(getCommonDecommissioningListTypes(cartUtil.getCartFolders(cart.getId())));
				decomTypeField.setCaption($("CartView.decommissioningTypeField"));
				layout.addComponent(decomTypeField);

				BaseButton saveButton = new BaseButton($("save")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						if (StringUtils.isBlank(titleField.getValue())) {
							params.getView().showErrorMessage($("CartView.decommissioningListIsMissingTitle"));
							return;
						}
						if (decomTypeField.getValue() == null) {
							params.getView().showErrorMessage($("CartView.decommissioningListIsMissingType"));
							return;
						}
						buildDecommissioningListRequested(titleField.getValue(), (DecommissioningListType)
										decomTypeField.getValue(),
								cart.getId(), params.getUser(), params.getView());
						getWindow().close();
					}
				};
				saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
				layout.addComponent(saveButton);
				layout.setSpacing(true);
				return layout;
			}
		};

		windowButton.click();
	}

	public void buildDecommissioningListRequested(String title, DecommissioningListType decomType, String cartId,
												  User user, BaseView view) {
		DecommissioningList list = rm.newDecommissioningList();
		list.setTitle(title);
		list.setAdministrativeUnit(getCommonAdministrativeUnit(cartUtil.getCartFolders(cartId)));
		list.setDecommissioningListType(decomType);
		if (isDecommissioningListWithSelectedFolders()) {
			list.setFolderDetailsFor(cartUtil.getNotDeletedCartFolders(cartId), FolderDetailStatus.SELECTED);
		} else {
			list.setFolderDetailsFor(cartUtil.getNotDeletedCartFolders(cartId), FolderDetailStatus.INCLUDED);
		}

		try {
			recordServices.add(list, user);
			view.navigate().to(RMViews.class).displayDecommissioningList(list.getId());
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}
	}

	public boolean isDecommissioningListWithSelectedFolders() {
		return new RMConfigs(modelLayerFactory.getSystemConfigurationsManager()).isDecommissioningListWithSelectedFolders();
	}

	public boolean isDecommissioningActionPossible(String cartId, User user, BaseView view) {
		List<Record> records = rm.get(cartUtil.getCartFolderIds(cartId));
		for (Record record : records) {
			Folder folder = rm.wrapFolder(record);
			if (!rmModuleExtensions.isDecommissioningActionPossibleOnFolder(folder, user)) {
				view.showErrorMessage($("CartView.actionBlockedByExtension"));
				return false;
			}
		}
		return true;
	}

	public boolean isAnyFolderInDecommissioningList(String cartId) {
		return searchServices.getResultsCount(
				from(rm.decommissioningList.schemaType()).where(rm.decommissioningList.status())
						.isNotEqual(DecomListStatus.PROCESSED)
						.andWhere(rm.decommissioningList.folders()).isContaining(cartUtil.getCartFolderIds(cartId))) > 0;
	}

	public boolean isAnyFolderBorrowed(String cartId) {
		return searchServices.getResultsCount(from(rm.folder.schemaType()).where(rm.folder.borrowed()).isTrue()
				.andWhere(Schemas.IDENTIFIER).isIn(cartUtil.getCartFolderIds(cartId))) > 0;
	}

	private List<DecommissioningListType> getCommonDecommissioningListTypes(List<Folder> folders) {
		List<DecommissioningListType> commonTypes = new ArrayList<>();
		boolean first = true;

		FolderStatus folderStatus = null;

		for (Folder folder : folders) {
			if (folderStatus == null) {
				folderStatus = folder.getArchivisticStatus();
			} else if (folderStatus != folder.getArchivisticStatus()) {
				return commonTypes;
			}
		}

		for (Folder folder : folders) {
			if (first) {
				commonTypes.addAll(findDecommissioningListTypes(folder));

				first = false;
			} else {
				List<DecommissioningListType> types = findDecommissioningListTypes(folder);
				Iterator<DecommissioningListType> commonTypesIterator = commonTypes.iterator();
				while (commonTypesIterator.hasNext()) {
					if (!types.contains(commonTypesIterator.next())) {
						commonTypesIterator.remove();
					}
				}
			}

		}

		return commonTypes;
	}

	private List<DecommissioningListType> findDecommissioningListTypes(Folder folder) {
		List<DecommissioningListType> types = new ArrayList<>();
		if (folder.getCloseDate() == null) {
			types.add(DecommissioningListType.FOLDERS_TO_CLOSE);

		}
		if (folder.getCloseDate() != null || folder.hasExpectedDates()) {
			if (folder.getArchivisticStatus() == ACTIVE) {
				types.add(DecommissioningListType.FOLDERS_TO_TRANSFER);
			}
			if (folder.getArchivisticStatus() == SEMI_ACTIVE || folder.getArchivisticStatus() == ACTIVE) {
				if (folder.getExpectedDepositDate() != null) {
					types.add(DecommissioningListType.FOLDERS_TO_DEPOSIT);
				}
				if (folder.getExpectedDestructionDate() != null) {
					types.add(DecommissioningListType.FOLDERS_TO_DESTROY);
				}
			}
		}

		return types;
	}

	private String getCommonAdministrativeUnit(List<Folder> folders) {
		String administrativeUnit = null;

		for (Folder folder : folders) {
			if (administrativeUnit == null) {
				administrativeUnit = folder.getAdministrativeUnit();
			} else {
				if (!administrativeUnit.equals(folder.getAdministrativeUnit())) {
					return null;
				}
			}
		}

		return administrativeUnit;
	}

	public boolean isSubFolderDecommissioningAllowed() {
		return appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager().getValue(RMConfigs.SUB_FOLDER_DECOMMISSIONING);
	}

	public boolean isAnyFolderASubFolder(String cartId) {
		return searchServices.getResultsCount(from(rm.folder.schemaType()).where(rm.folder.parentFolder()).isNotNull()
				.andWhere(Schemas.IDENTIFIER).isIn(cartUtil.getCartFolderIds(cartId))) > 0;
	}

	public void printMetadataReportAction(Cart cart, MenuItemActionBehaviorParams params) {
		CartNewReportPresenter cartNewReportPresenter = new CartNewReportPresenter(params.getUser(), (CartView) params.getView(), cart.getId());

		ReportTabButton reportGeneratorButton = new ReportTabButton($("ReportGeneratorButton.buttonText"), $("ReportGeneratorButton.windowText"),
				appLayerFactory, collection, false, false, cartNewReportPresenter, ConstellioUI.getCurrentSessionContext()) {
			@Override
			public void buttonClick(ClickEvent event) {
				List<RecordVO> allRecords = new ArrayList<>();
				allRecords.addAll(getNotDeletedCartFoldersVO(cart.getId(), params.getView()));
				allRecords.addAll(getNotDeletedCartDocumentVO(cart.getId(), params.getView()));
				setRecordVoList(allRecords.toArray(new RecordVO[0]));
				super.buttonClick(event);
			}
		};

		reportGeneratorButton.click();
	}

	private class CartNewReportPresenter implements NewReportPresenter {

		private CartView view;
		private User user;
		private String cartId;

		public CartNewReportPresenter(User user, CartView baseView, String cartId) {
			this.cartId = cartId;
			this.user = user;
			this.view = baseView;
		}

		@Override
		public List<ReportWithCaptionVO> getSupportedReports() {
			List<ReportWithCaptionVO> supportedReports = new ArrayList<>();
			ReportServices reportServices = new ReportServices(modelLayerFactory, collection);
			List<String> userReports = reportServices.getUserReportTitles(user, view.getCurrentSchemaType());
			if (userReports != null) {
				for (String reportTitle : userReports) {
					supportedReports.add(new ReportWithCaptionVO(reportTitle, reportTitle));
				}
			}
			return supportedReports;
		}

		@Override
		public NewReportWriterFactory getReport(String report) {
			return new SearchResultReportWriterFactory(appLayerFactory);
		}

		@Override
		public Object getReportParameters(String report) {
			List<String> recordids = cartUtil.getNotDeletedRecordsIds(view.getCurrentSchemaType(), user, cartId);

			return new SearchResultReportParameters(recordids, view.getCurrentSchemaType(),
					collection, report, user, null);
		}
	}

	public void createSIPArchvesAction(Cart cart, MenuItemActionBehaviorParams params) {
		SIPButtonImpl siPbutton = new SIPButtonImpl($("SIPButton.caption"), $("SIPButton.caption"), ConstellioUI.getCurrent().getHeader(), true) {
			@Override
			public void buttonClick(ClickEvent event) {
				setAllObject(getNotDeletedCartFoldersVO(cart.getId(), params.getView()).toArray(new FolderVO[0]));
				super.buttonClick(event);
			}
		};

		siPbutton.click();
	}

	public List<FolderVO> getNotDeletedCartFoldersVO(String cartId, BaseView view) {
		FolderToVOBuilder builder = new FolderToVOBuilder();
		List<FolderVO> folderVOS = new ArrayList<>();
		for (Folder folder : cartUtil.getCartFolders(cartId)) {
			if (!folder.isLogicallyDeletedStatus()) {
				folderVOS.add(builder.build(folder.getWrappedRecord(), VIEW_MODE.DISPLAY, view.getSessionContext()));
			}
		}
		return folderVOS;
	}

	public void consolidatedPdfAction(Cart cart, MenuItemActionBehaviorParams params) {
		Button consolidatedPdfButton = new ConsolidatedPdfButton() {
			@Override
			public void buttonClick(ClickEvent event) {
				List<String> notDeletedDocumentIds = new ArrayList<>();
				List<DocumentVO> notDeletedDocumentVOs = getNotDeletedCartDocumentVO(cart.getId(), params.getView());
				for (DocumentVO documentVO : notDeletedDocumentVOs) {
					notDeletedDocumentIds.add(documentVO.getId());
				}
				if (isPdfGenerationActionPossible(notDeletedDocumentIds, params.getView(), params.getUser())) {
					setRecordIds(notDeletedDocumentIds);
					super.buttonClick(event);
				}
			}

		};

		consolidatedPdfButton.click();
	}

	public boolean isPdfGenerationActionPossible(List<String> recordIds, BaseView view, User user) {
		List<Record> records = rm.get(recordIds);
		for (Record record : records) {
			if (!rmModuleExtensions.isCreatePDFAActionPossibleOnDocument(rm.wrapDocument(record), user)) {
				view.showErrorMessage($("CartView.actionBlockedByExtension"));
				return false;
			}
		}
		return true;
	}

	public List<DocumentVO> getNotDeletedCartDocumentVO(String cartId, BaseView view) {
		DocumentToVOBuilder builder = new DocumentToVOBuilder(modelLayerFactory);
		List<DocumentVO> documentVOS = new ArrayList<>();
		for (Document document : cartUtil.getCartDocuments(cartId)) {
			if (!document.isLogicallyDeletedStatus()) {
				documentVOS.add(builder.build(document.getWrappedRecord(), VIEW_MODE.DISPLAY, view.getSessionContext()));
			}
		}
		return documentVOS;
	}
}
