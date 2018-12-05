package com.constellio.app.modules.rm.ui.components.content;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.RecordVORuntimeException;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.SearchEventServices;

import java.io.IOException;
import java.io.Serializable;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.pages.search.SearchPresenter.CURRENT_SEARCH_EVENT;
import static com.constellio.app.ui.pages.search.SearchPresenter.SEARCH_EVENT_DWELL_TIME;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

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

	public DocumentContentVersionPresenter(DocumentContentVersionWindow window) {
		this.window = window;

		initTransientObjects();

		RecordVO recordVO = window.getRecordVO();
		Record record = rmSchemasRecordsServices.get(recordVO.getId());
		documentVO = documentVOBuilder.build(record, VIEW_MODE.DISPLAY, window.getSessionContext());
		contentVersionVO = documentVO.getContent();

		SessionContext sessionContext = window.getSessionContext();
		presenterUtils = new SchemaPresenterUtils(Document.DEFAULT_SCHEMA, constellioFactories, sessionContext);

		boolean checkOutLinkVisible = isCheckOutLinkVisible();
		window.setCheckOutLinkVisible(checkOutLinkVisible);

		String readOnlyMessage;
		if (!hasWritePermission()) {
			readOnlyMessage = $("DocumentContentVersionWindow.noWritePermission");
		} else if (isCheckedOutByOtherUser()) {
			readOnlyMessage = $("DocumentContentVersionWindow.checkedOutByOtherUser");
		} else if (!isCheckedOut()) {
			readOnlyMessage = $("DocumentContentVersionWindow.notCheckedOut");
		} else if (!isLatestVersion()) {
			readOnlyMessage = $("DocumentContentVersionWindow.notLatestVersion");
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

	private boolean hasWritePermission() {
		User currentUser = presenterUtils.getCurrentUser();
		Record record = presenterUtils.getRecord(documentVO.getId());
		return currentUser.hasWriteAccess().on(record);
	}

	private boolean isLatestVersion() {
		String latestVersion = documentVO.getContent().getVersion();
		String contentVersionVOVersion = contentVersionVO.getVersion();
		return latestVersion.equals(contentVersionVOVersion);
	}

	private boolean isCheckedOut() {
		return documentVO.getContent().getCheckoutUserId() != null;
	}

	private boolean isCheckedOutByOtherUser() {
		User currentUser = presenterUtils.getCurrentUser();
		String checkOutUserId = documentVO.getContent().getCheckoutUserId();
		return checkOutUserId != null && !checkOutUserId.equals(currentUser.getId());
	}

	private boolean isCheckOutLinkVisible() {
		return hasWritePermission() && !isCheckedOut() && isLatestVersion() && !rmSchemasRecordsServices.isEmail(contentVersionVO.getFileName());
	}

	public void displayDocumentLinkClicked() {
		window.closeWindow();
		String documentId = documentVO.getId();
		window.navigate().to(RMViews.class).displayDocument(documentId);
		updateSearchResultClicked();
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
