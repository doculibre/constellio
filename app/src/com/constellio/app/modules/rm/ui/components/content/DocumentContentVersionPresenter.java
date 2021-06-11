package com.constellio.app.modules.rm.ui.components.content;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.pages.search.SearchPresenter.CURRENT_SEARCH_EVENT;
import static com.constellio.app.ui.pages.search.SearchPresenter.SEARCH_EVENT_DWELL_TIME;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.util.RMNavigationUtils;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.RecordVORuntimeException;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.DateFormatUtils;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.SearchEventServices;

public class DocumentContentVersionPresenter implements Serializable {


	private DocumentContentVersionWindow window;

	private DocumentVO documentVO;

	private ContentVersionVO contentVersionVO;

	private String agentURL;

	private SchemaPresenterUtils presenterUtils;

	private DocumentToVOBuilder documentVOBuilder;

	private transient ConstellioFactories constellioFactories;

	private transient ModelLayerFactory modelLayerFactory;
	private transient AppLayerFactory appLayerFactory;

	private transient RMSchemasRecordsServices rmSchemasRecordsServices;
	private RMModuleExtensions rmModuleExtensions;

	private Map<String, String> params;

	public DocumentContentVersionPresenter(DocumentContentVersionWindow window, Map<String, String> params) {
		this.window = window;
		this.params = params;
		initTransientObjects();

		RecordVO recordVO = window.getRecordVO();
		Record record = rmSchemasRecordsServices.get(recordVO.getId());
		documentVO = documentVOBuilder.build(record, VIEW_MODE.DISPLAY, window.getSessionContext());
		contentVersionVO = documentVO.getContent();

		SessionContext sessionContext = window.getSessionContext();
		presenterUtils = new SchemaPresenterUtils(Document.DEFAULT_SCHEMA, constellioFactories, sessionContext);

		boolean checkOutLinkVisible = isCheckOutLinkVisible();
		window.setCheckOutLinkVisible(checkOutLinkVisible);

		rmModuleExtensions = appLayerFactory.getExtensions().forCollection(window.getSessionContext().getCurrentCollection())
				.forModule(ConstellioRMModule.ID);

		String readOnlyMessage;
		if (isCheckedOutByOtherUser()) {
			readOnlyMessage = $("DocumentContentVersionWindow.checkedOutByOtherUser", getCheckOutInfo());
		} else if (isCheckedOutByCurrentUser()) {
			readOnlyMessage = $("DocumentContentVersionWindow.checkedOutByCurrentUser", getCheckOutInfo());
		} else if (!isLatestVersion()) {
			readOnlyMessage = $("DocumentContentVersionWindow.notLatestVersion", getVersionNumber());
		} else {
			readOnlyMessage = null;
		}
		window.setReadOnlyMessage(readOnlyMessage);

		if (ConstellioAgentUtils.isAgentSupported()) {
			agentURL = ConstellioAgentUtils.getAgentURL(documentVO, contentVersionVO);
		} else {
			agentURL = null;
		}
		window.setAgentURL(agentURL);
	}


	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		SessionContext sessionContext = window.getSessionContext();
		String collection = sessionContext.getCurrentCollection();

		constellioFactories = window.getConstellioFactories();
		modelLayerFactory = constellioFactories.getModelLayerFactory();
		appLayerFactory = constellioFactories.getAppLayerFactory();

		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
		documentVOBuilder = new DocumentToVOBuilder(modelLayerFactory);
	}


	protected boolean hasWritePermission() {
		User currentUser = presenterUtils.getCurrentUser();
		Record record = presenterUtils.getRecord(documentVO.getId());
		return currentUser.hasWriteAccess().on(record);
	}

	public User getCurrentUser() {
		return presenterUtils.getCurrentUser();
	}

	private boolean isLatestVersion() {
		String latestVersion = documentVO.getContent().getVersion();
		String contentVersionVOVersion = contentVersionVO.getVersion();
		return latestVersion.equals(contentVersionVOVersion);
	}

	private String getVersionNumber() {
		return contentVersionVO.getVersion();
	}

	private boolean isCheckedOut() {
		return documentVO.getContent().getCheckoutUserId() != null;
	}

	private boolean isCheckedOutByOtherUser() {
		User currentUser = presenterUtils.getCurrentUser();
		String checkOutUserId = documentVO.getContent().getCheckoutUserId();
		return checkOutUserId != null && !checkOutUserId.equals(currentUser.getId());
	}

	private boolean isCheckedOutByCurrentUser() {
		User currentUser = presenterUtils.getCurrentUser();
		String checkOutUserId = documentVO.getContent().getCheckoutUserId();
		return checkOutUserId != null && checkOutUserId.equals(currentUser.getId());
	}

	private Map<String, Object> getCheckOutInfo() {
		Map<String, Object> params = new HashMap<>();
		params.put("usersTitle", rmSchemasRecordsServices.getUser(documentVO.getContent().getCheckoutUserId()).getTitle());
		params.put("checkOutDateTime", DateFormatUtils.format(documentVO.getContent().getCheckoutDateTime()));
		return params;
	}

	private boolean isCheckOutLinkVisible() {
		return hasWritePermission() && !isCheckedOut() && isLatestVersion() && !rmSchemasRecordsServices
				.isEmail(contentVersionVO.getFileName());
	}

	public void displayDocumentLinkClicked() {
		String documentId = documentVO.getId();
		if (Toggle.SEARCH_RESULTS_VIEWER.isEnabled()) {
			window.displayInWindow();
		} else {
			window.closeWindow();
			RMNavigationUtils.navigateToDisplayDocument(documentId, params, appLayerFactory,
					window.getSessionContext().getCurrentCollection());
		}
		updateSearchResultClicked();
	}

	public boolean isNavigationStateDocumentView() {
		String documentId = documentVO.getId();
		Navigation navigation = new Navigation();
		return navigation.to(RMViews.class).getState().equals(RMNavigationConfiguration.DISPLAY_DOCUMENT + "/" + documentId);
	}

	void checkOutLinkClicked() {
		if (!isCheckedOut()) {
			updateSearchResultClicked();
			User currentUser = presenterUtils.getCurrentUser();
			Document document = rmSchemasRecordsServices.getDocument(documentVO.getId());
			document.getContent().checkOut(currentUser);
			presenterUtils.addOrUpdate(document.getWrappedRecord(), new RecordUpdateOptions().setOverwriteModificationDateAndUser(false));
			modelLayerFactory.newLoggingServices().borrowRecord(document.getWrappedRecord(), currentUser, TimeProvider.getLocalDateTime());

			SessionContext sessionContext = window.getSessionContext();
			documentVO = documentVOBuilder.build(document.getWrappedRecord(), VIEW_MODE.DISPLAY, window.getSessionContext());
			agentURL = ConstellioAgentUtils.getAgentURL(documentVO, contentVersionVO, sessionContext);
			window.closeWindow();
			if (agentURL != null) {
				window.open(agentURL);
			}
		}
	}

	public void openWithAgentLinkClicked() {
		updateSearchResultClicked();
		window.closeWindow();
		window.open(agentURL);
	}

	protected void updateSearchResultClicked() {
		if (Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()) {
			ConstellioUI.getCurrent().setAttribute(SEARCH_EVENT_DWELL_TIME, System.currentTimeMillis());

			SearchEventServices searchEventServices = new SearchEventServices(presenterUtils.getCollection(),
					presenterUtils.modelLayerFactory());
			SearchEvent searchEvent = ConstellioUI.getCurrentSessionContext().getAttribute(CURRENT_SEARCH_EVENT);
			if (searchEvent != null) {
				searchEventServices.incrementClickCounter(searchEvent.getId());

				String url = null;
				try {
					url = documentVO.get("url");
				} catch (RecordVORuntimeException.RecordVORuntimeException_NoSuchMetadata e) {
				}
				String clicks = defaultIfBlank(url, documentVO.getId());
				searchEventServices.updateClicks(searchEvent, clicks);
			}
		}
	}
}
