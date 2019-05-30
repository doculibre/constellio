package com.constellio.app.modules.rm.services.actions.behaviors;

import com.constellio.app.api.extensions.params.NavigateToFromAPageParams;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.components.document.DocumentActionsPresenterUtils;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.util.DecommissionNavUtil;
import com.constellio.app.modules.rm.util.RMNavigationUtils;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVORuntimeException.RecordVORuntimeException_NoSuchMetadata;
import com.constellio.app.ui.framework.buttons.DownloadLink;
import com.constellio.app.ui.framework.components.content.ContentVersionVOResource;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.logging.SearchEventServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;

import java.util.Map;

import static com.constellio.app.ui.pages.search.SearchPresenter.CURRENT_SEARCH_EVENT;
import static com.constellio.app.ui.pages.search.SearchPresenter.SEARCH_EVENT_DWELL_TIME;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

public class DocumentRecordActionBehaviors {

	private RMModuleExtensions rmModuleExtensions;
	private ModelLayerCollectionExtensions extensions;
	private String collection;
	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private RecordServices recordServices;
	private RMSchemasRecordsServices rm;
	private LoggingServices loggingServices;

	public DocumentRecordActionBehaviors(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
		recordServices = modelLayerFactory.newRecordServices();
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		loggingServices = modelLayerFactory.newLoggingServices();
		extensions = modelLayerFactory.getExtensions().forCollection(collection);
	}

	public void display(RecordActionBehaviorParams params) {
		Map<String, String> formParams = params.getFormParams();
		String documentId = params.getRecordVO().getId();

		RMNavigationUtils.navigateToDisplayDocument(documentId, formParams, appLayerFactory, collection);
		updateSearchResultClicked((DocumentVO) params.getRecordVO());
	}

	public void open(RecordActionBehaviorParams params) {
		String agentURL = ConstellioAgentUtils.getAgentURL(params.getRecordVO(), params.getContentVersionVO());
		Page.getCurrent().open(agentURL, params.isContextualMenu() ? "_top" : null);
		loggingServices.openDocument(recordServices.getDocumentById(params.getRecordVO().getId()), params.getUser());
	}

	public void copy(RecordActionBehaviorParams params) {
		BaseView view = params.getView();
		Map<String, String> formParams = params.getFormParams();
		String documentId = params.getRecordVO().getId();

		boolean areSearchTypeAndSearchIdPresent = DecommissionNavUtil.areTypeAndSearchIdPresent(formParams);

		if (areSearchTypeAndSearchIdPresent) {
			view.navigate().to(RMViews.class).addDocumentWithContentFromDecommission(documentId,
					DecommissionNavUtil.getSearchId(formParams), DecommissionNavUtil.getSearchType(formParams));
		} else if (formParams.get(RMViews.FAV_GROUP_ID_KEY) != null) {
			view.navigate().to(RMViews.class)
					.addDocumentWithContentFromFavorites(documentId, formParams.get(RMViews.FAV_GROUP_ID_KEY));
		} else if (rmModuleExtensions.navigateToAddDocumentWhileKeepingTraceOfPreviousView(
				new NavigateToFromAPageParams(formParams, documentId))) {
		} else {
			view.navigate().to(RMViews.class).addDocumentWithContent(documentId);
		}
	}

	public void edit(RecordActionBehaviorParams params) {
		params.getView().navigate().to(RMViews.class).editDocument(params.getRecordVO().getId());
		updateSearchResultClicked((DocumentVO) params.getRecordVO());
	}

	public void download(RecordActionBehaviorParams params) {
		ContentVersionVOResource contentVersionResource = new ContentVersionVOResource(params.getContentVersionVO());
		Resource downloadedResource = DownloadLink.wrapForDownload(contentVersionResource);
		Page.getCurrent().open(downloadedResource, null, false);
		loggingServices.downloadDocument(recordServices.getDocumentById(params.getRecordVO().getId()), params.getUser());
	}

	public void delete(RecordActionBehaviorParams params) {
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

	private void updateSearchResultClicked(DocumentVO documentVO) {
		if (Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()) {
			ConstellioUI.getCurrent().setAttribute(SEARCH_EVENT_DWELL_TIME, System.currentTimeMillis());

			SearchEventServices searchEventServices = new SearchEventServices(collection, modelLayerFactory);
			SearchEvent searchEvent = ConstellioUI.getCurrentSessionContext().getAttribute(CURRENT_SEARCH_EVENT);

			if (searchEvent != null) {
				searchEventServices.incrementClickCounter(searchEvent.getId());

				String url = null;
				try {
					url = documentVO.get("url");
				} catch (RecordVORuntimeException_NoSuchMetadata ignored) {
				}
				String clicks = defaultIfBlank(url, documentVO.getId());
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

	private void navigateToDisplayFolder(String folderId, Map<String, String> params) {
		RMNavigationUtils.navigateToDisplayFolder(folderId, params, appLayerFactory, collection);
	}

}
