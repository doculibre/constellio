package com.constellio.app.modules.rm.services.menu.behaviors;

import com.constellio.app.api.extensions.params.NavigateToFromAPageParams;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.actions.DocumentRecordActionsServices;
import com.constellio.app.modules.rm.services.logging.DecommissioningLoggingService;
import com.constellio.app.modules.rm.services.menu.behaviors.util.BehaviorsUtil;
import com.constellio.app.modules.rm.services.menu.behaviors.util.RMUrlUtil;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.builders.UserToVOBuilder;
import com.constellio.app.modules.rm.ui.buttons.CartWindowButton;
import com.constellio.app.modules.rm.ui.buttons.CartWindowButton.AddedRecordType;
import com.constellio.app.modules.rm.ui.buttons.RenameDialogButton;
import com.constellio.app.modules.rm.ui.components.document.DocumentActionsPresenterUtils;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.util.DecommissionNavUtil;
import com.constellio.app.modules.rm.util.RMNavigationUtils;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.RecordVORuntimeException.RecordVORuntimeException_NoSuchMetadata;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.builders.ContentVersionToVOBuilder;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DownloadLink;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.buttons.report.LabelButtonV2;
import com.constellio.app.ui.framework.clipboard.CopyToClipBoard;
import com.constellio.app.ui.framework.components.RMSelectionPanelReportPresenter;
import com.constellio.app.ui.framework.components.ReportTabButton;
import com.constellio.app.ui.framework.components.content.ContentVersionVOResource;
import com.constellio.app.ui.framework.components.content.UpdateContentVersionWindowImpl;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.utils.Factory;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.contents.ContentConversionManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.logging.SearchEventServices;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.io.FilenameUtils;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.pages.search.SearchPresenter.CURRENT_SEARCH_EVENT;
import static com.constellio.app.ui.pages.search.SearchPresenter.SEARCH_EVENT_DWELL_TIME;
import static com.constellio.app.ui.util.UrlUtil.getConstellioUrl;
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
	private DocumentRecordActionsServices documentRecordActionsServices;

	public DocumentMenuItemActionBehaviors(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
		recordServices = modelLayerFactory.newRecordServices();
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		loggingServices = modelLayerFactory.newLoggingServices();
		decommissioningLoggingService = new DecommissioningLoggingService(appLayerFactory.getModelLayerFactory());
		extensions = modelLayerFactory.getExtensions().forCollection(collection);
		documentRecordActionsServices = new DocumentRecordActionsServices(collection, appLayerFactory);
	}

	public void getConsultationLink(Document document, MenuItemActionBehaviorParams params) {

		document = loadingFullRecordIfSummary(document);
		String constellioURL = getConstellioUrl(modelLayerFactory);

		CopyToClipBoard.copyToClipBoard(constellioURL + RMUrlUtil.getPathToConsultLinkForDocument(document.getId()));
	}


	public void display(Document document, MenuItemActionBehaviorParams params) {
		document = loadingFullRecordIfSummary(document);
		Map<String, String> formParams = params.getFormParams();
		String documentId = document.getId();

		RMNavigationUtils.navigateToDisplayDocument(documentId, formParams, appLayerFactory, collection);
		updateSearchResultClicked(document.getWrappedRecord());
	}

	public void open(Document document, MenuItemActionBehaviorParams params) {
		document = loadingFullRecordIfSummary(document);
		String agentURL = ConstellioAgentUtils.getAgentURL(params.getRecordVO(), params.getContentVersionVO());
		Page.getCurrent().open(agentURL, params.isContextualMenu() ? "_top" : null);
		loggingServices.openDocument(recordServices.getDocumentById(document.getId()), params.getUser());
	}

	public void copy(Document document, MenuItemActionBehaviorParams params) {
		document = loadingFullRecordIfSummary(document);
		BaseView view = params.getView();
		Map<String, String> formParams = params.getFormParams();
		String documentId = document.getId();

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

	public void edit(Document document, MenuItemActionBehaviorParams params) {
		document = loadingFullRecordIfSummary(document);
		params.getView().navigate().to(RMViews.class).editDocument(document.getId());
		updateSearchResultClicked(document.getWrappedRecord());
	}

	public void rename(Document document, MenuItemActionBehaviorParams params) {
		RenameDialogButton window = new RenameDialogButton(
				FontAwesome.PENCIL_SQUARE_O,
				$("DisplayDocumentView.renameContent"),
				$("DisplayDocumentView.renameContent"),
				false) {
			@Override
			public void save(String newValue) {
				boolean isManualEntry = rm.folder.title().getDataEntry().getType() == DataEntryType.MANUAL;
				boolean isNotAddOnly = !rm.documentSchemaType().getDefaultSchema().getMetadata(Schemas.TITLE_CODE)
						.getPopulateConfigs().isAddOnly();

				String filename = document.getContent().getCurrentVersion().getFilename();
				String extension = FilenameUtils.getExtension(filename);

				if (!(FilenameUtils.getExtension(newValue).equals(extension))) {
					if (!extension.isEmpty()) {
						newValue = FilenameUtils.removeExtension(newValue) + "." + extension;
					} else {
						newValue = FilenameUtils.removeExtension(newValue);
					}
				}

				if (filename.equals(document.getTitle())) {
					if (isManualEntry && isNotAddOnly) {
						document.setTitle(newValue);
					}

				} else if (FilenameUtils.removeExtension(filename).equals(document.getTitle())) {
					if (isManualEntry && isNotAddOnly) {
						document.setTitle(FilenameUtils.removeExtension(newValue));
					}
				}

				document.getContent().renameCurrentVersion(newValue);
				try {
					recordServices.update(document.getWrappedRecord());
					getWindow().close();
					navigateToDisplayDocument(document.getId(), params.getFormParams());
				} catch (RecordServicesException e) {
					params.getView().showErrorMessage(MessageUtils.toMessage(e));
				}
			}
		};
		window.click();
	}

	public void download(Document document, MenuItemActionBehaviorParams params) {
		document = loadingFullRecordIfSummary(document);
		ContentVersionVOResource contentVersionResource = new ContentVersionVOResource(params.getContentVersionVO());
		Resource downloadedResource = DownloadLink.wrapForDownload(contentVersionResource);
		Page.getCurrent().open(downloadedResource, null, false);
		loggingServices.downloadDocument(recordServices.getDocumentById(document.getId()), params.getUser());
	}

	public void delete(Document documentSummary, MenuItemActionBehaviorParams params) {
		Document document = loadingFullRecordIfSummary(documentSummary);
		final boolean isDocumentCheckout = documentRecordActionsServices.isContentCheckedOut(document.getContent());

		Button deleteDocumentButton = new DeleteButton($("DisplayDocumentView.deleteDocument")) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				deleteConfirmationDocumentButtonClicked(document, params);
			}

			@Override
			protected String getConfirmDialogMessage() {
				if (isDocumentCheckout) {
					return $("DocumentMenuItemActionBehaviors.documentIsCheckoutDeleteConfirmationMessage");
				} else {
					return super.getConfirmDialogMessage();
				}
			}
		};

		deleteDocumentButton.click();

	}


	private void deleteConfirmationDocumentButtonClicked(Document document, MenuItemActionBehaviorParams params) {
		document = loadingFullRecordIfSummary(document);
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
			if (BehaviorsUtil.reloadIfSearchView(params.getView())) {
				return;
			} else if (parentId != null) {
				navigateToDisplayFolder(parentId, params.getFormParams());
			} else {
				params.getView().navigate().to().recordsManagement();
			}
		} else {
			MessageUtils.getCannotDeleteWindow(validateDeleteDocumentPossibleExtensively(
					document.getWrappedRecord(), params.getUser())).openWindow();
		}
	}

	public void finalize(Document document, MenuItemActionBehaviorParams params) {
		document = loadingFullRecordIfSummary(document);
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

	public void linkToDocument(Document documentSummary, MenuItemActionBehaviorParams params) {
		Document document = loadingFullRecordIfSummary(documentSummary);
		WindowButton.WindowConfiguration publicLinkConfig = new WindowConfiguration(true, false, "75%", "125px");
		WindowButton publicLinkButton = new WindowButton(
				$("DocumentContextMenu.publicLink"), $("DocumentContextMenu.publicLink"), publicLinkConfig) {
			@Override
			protected Component buildWindowContent() {
				String url = modelLayerFactory.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.CONSTELLIO_URL);
				String publicLink = url + "dl?id=" + document.getId();

				Label link = new Label(publicLink);
				Label message = new Label($("DocumentContextMenu.publicLinkInfo"));
				message.addStyleName(ValoTheme.LABEL_BOLD);
				return new VerticalLayout(message, link);
			}
		};
		publicLinkButton.click();
	}


	public void publish(Document document, MenuItemActionBehaviorParams params) {
		document = loadingFullRecordIfSummary(document);
		document.setPublished(true);
		try {
			recordServices.update(document);
			linkToDocument(document, params);
			params.getView().refreshActionMenu();
		} catch (RecordServicesException e) {
			params.getView().showErrorMessage(MessageUtils.toMessage(e));
		}
	}

	public void createPdf(Document document, MenuItemActionBehaviorParams params) {
		document = loadingFullRecordIfSummary(document);
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


	public void unPublish(Document document, MenuItemActionBehaviorParams params) {
		document = loadingFullRecordIfSummary(document);
		document.setPublished(false);
		try {
			recordServices.update(document);
			params.getView().refreshActionMenu();
		} catch (RecordServicesException e) {
			params.getView().showErrorMessage(MessageUtils.toMessage(e));
		}
	}

	public void addToSelection(Document document, MenuItemActionBehaviorParams params) {
		params.getView().getSessionContext().addSelectedRecordId(document.getId(), Document.SCHEMA_TYPE);
	}

	public void removeToSelection(Document document, MenuItemActionBehaviorParams params) {
		params.getView().getSessionContext().removeSelectedRecordId(document.getId(), Document.SCHEMA_TYPE);
	}

	public void addToCart(Document document, MenuItemActionBehaviorParams params) {
		document = loadingFullRecordIfSummary(document);
		CartWindowButton cartWindowButton = new CartWindowButton(document.getWrappedRecord(), params, AddedRecordType.DOCUMENT);
		cartWindowButton.addToCart();
	}

	public void printLabel(Document document, MenuItemActionBehaviorParams params) {
		document = loadingFullRecordIfSummary(document);
		Factory<List<LabelTemplate>> customLabelTemplatesFactory = (Factory<List<LabelTemplate>>) () -> appLayerFactory.getLabelTemplateManager().listExtensionTemplates(Document.SCHEMA_TYPE);

		Factory<List<LabelTemplate>> defaultLabelTemplatesFactory = (Factory<List<LabelTemplate>>) () -> appLayerFactory.getLabelTemplateManager().listTemplates(Document.SCHEMA_TYPE);

		SessionContext sessionContext = params.getView().getSessionContext();
		UserToVOBuilder userToVOBuilder = new UserToVOBuilder();
		UserVO userVO = userToVOBuilder.build(params.getUser().getWrappedRecord(),
				VIEW_MODE.DISPLAY, sessionContext);

		Button labels = new LabelButtonV2($("DisplayFolderView.printLabel"),
				$("DisplayFolderView.printLabel"), customLabelTemplatesFactory,
				defaultLabelTemplatesFactory, appLayerFactory,
				sessionContext.getCurrentCollection(), userVO, getDocumentVO(params, document));

		labels.click();
	}

	public void checkIn(Document document, MenuItemActionBehaviorParams params) {
		document = loadingFullRecordIfSummary(document);
		if (documentRecordActionsServices.isCheckInActionPossible(document.getWrappedRecord(), params.getUser())) {
			UpdateContentVersionWindowImpl uploadWindow =
					createUpdateContentVersionWindow(getDocumentVO(params, document), params.getView());
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

	public void checkOut(Document document, MenuItemActionBehaviorParams params) {
		document = loadingFullRecordIfSummary(document);
		if (documentRecordActionsServices.isCheckOutActionPossible(document.getWrappedRecord(), params.getUser())) {
			updateSearchResultClicked(document.getWrappedRecord());
			Content content = document.getContent();
			content.checkOut(params.getUser());
			modelLayerFactory.newLoggingServices().borrowRecord(document.getWrappedRecord(), params.getUser(), TimeProvider.getLocalDateTime());
			try {
				recordServices.update(document.getWrappedRecord(), new RecordUpdateOptions().setOverwriteModificationDateAndUser(false));

				DocumentVO documentVO = getDocumentVO(params, document);

				params.getView().refreshActionMenu();

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
		} else if (documentRecordActionsServices.isCheckOutActionNotPossibleDocumentDeleted(document.getWrappedRecord(), params.getUser())) {
			params.getView().showErrorMessage($("DocumentActionsComponent.cantCheckOutDocumentDeleted"));
		}
	}

	public void addAuthorization(Document document, MenuItemActionBehaviorParams params) {
		document = loadingFullRecordIfSummary(document);
		params.getView().navigate().to().shareContent(document.getId());
		updateSearchResultClicked(document.getWrappedRecord());
	}


	public void manageAuthorizations(Document document, MenuItemActionBehaviorParams params) {
		document = loadingFullRecordIfSummary(document);
		params.getView().navigate().to().listObjectAccessAndRoleAuthorizations(document.getId());
		updateSearchResultClicked(document.getWrappedRecord());
	}

	public void reportGeneratorButton(Document documentSummary, MenuItemActionBehaviorParams params) {
		Document document = loadingFullRecordIfSummary(documentSummary);
		RMSelectionPanelReportPresenter rmSelectionPanelReportPresenter = new RMSelectionPanelReportPresenter(appLayerFactory, collection, params.getUser()) {
			@Override
			public String getSelectedSchemaType() {
				return Document.SCHEMA_TYPE;
			}

			@Override
			public List<String> getSelectedRecordIds() {
				return asList(document.getId());
			}
		};

		ReportTabButton reportGeneratorButton = new ReportTabButton($("SearchView.metadataReportTitle"),
				$("SearchView.metadataReportTitle"),
				appLayerFactory, collection, false, false,
				rmSelectionPanelReportPresenter, params.getView().getSessionContext()) {

		};

		reportGeneratorButton.setRecordVoList(getDocumentVO(params, document));
		reportGeneratorButton.click();
	}

	public void upload(Document document, MenuItemActionBehaviorParams params) {
		document = loadingFullRecordIfSummary(document);
		DocumentVO documentVO = getDocumentVO(params, document);
		UpdateContentVersionWindowImpl uploadWindow = createUpdateContentVersionWindow(documentVO, params.getView());

		uploadWindow.open(false);
	}

	public void alertAvailable(Document document, MenuItemActionBehaviorParams params) {
		document = loadingFullRecordIfSummary(document);
		List<String> usersToAlert = document.getAlertUsersWhenAvailable();
		String currentUserId = params.getUser().getId();
		List<String> newUsersToAlert = new ArrayList<>(usersToAlert);

		String currentBorrower = getCurrentBorrowerOf(document);

		if (!newUsersToAlert.contains(currentUserId) && currentBorrower != null && !currentUserId.equals(currentBorrower)) {
			newUsersToAlert.add(currentUserId);
			document.setAlertUsersWhenAvailable(newUsersToAlert);
			try {
				recordServices.update(document);
			} catch (RecordServicesException e) {
				params.getView().showErrorMessage(MessageUtils.toMessage(e));
			}
		}
		params.getView().showMessage($("RMObject.createAlert"));
	}

	private String getCurrentBorrowerOf(Document document) {
		return document.getContent() == null ? null : document.getContent().getCheckoutUserId();
	}

	private DocumentVO getDocumentVO(MenuItemActionBehaviorParams params, Document document) {
		return new DocumentToVOBuilder(modelLayerFactory).build(document.getWrappedRecord(),
				VIEW_MODE.DISPLAY, params.getView().getSessionContext());
	}

	private UpdateContentVersionWindowImpl createUpdateContentVersionWindow(RecordVO recordVO, BaseView view) {
		final Map<RecordVO, MetadataVO> recordMap = new HashMap<>();
		recordMap.put(recordVO, recordVO.getMetadata(Document.CONTENT));

		return new UpdateContentVersionWindowImpl(recordMap) {
			@Override
			public void close() {
				super.close();
				view.updateUI();
			}
		};
	}

	// FIXME move elsewhere?
	private void updateSearchResultClicked(Record record) {
		if (Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()) {
			ConstellioUI.getCurrent().setAttribute(SEARCH_EVENT_DWELL_TIME, System.currentTimeMillis());

			SearchEventServices searchEventServices = new SearchEventServices(collection, modelLayerFactory);
			SearchEvent searchEvent = ConstellioUI.getCurrentSessionContext().getAttribute(CURRENT_SEARCH_EVENT);

			if (searchEvent != null) {
				searchEventServices.incrementClickCounter(searchEvent.getId());

				String url = null;
				try {
					url = record.get(Schemas.URL);
				} catch (RecordVORuntimeException_NoSuchMetadata ignored) {
				}
				String clicks = defaultIfBlank(url, record.getId());
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

	private Document loadingFullRecordIfSummary(Document document) {
		if (document.isSummary()) {
			return rm.getDocument(document.getId());
		} else {
			return document;
		}

	}
}
