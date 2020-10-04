package com.constellio.app.modules.rm.ui.components.document;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.pages.search.SearchPresenter.CURRENT_SEARCH_EVENT;
import static com.constellio.app.ui.pages.search.SearchPresenter.SEARCH_EVENT_DWELL_TIME;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.util.RMNavigationUtils;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.RecordVORuntimeException.RecordVORuntimeException_NoSuchMetadata;
import com.constellio.app.ui.framework.builders.ContentVersionToVOBuilder;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.DateFormatUtils;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.SearchEventServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.security.AuthorizationsServices;

@Deprecated
public class DocumentActionsPresenterUtils<T extends DocumentActionsComponent> implements Serializable {

	private static final int WAIT_ONE_SECOND = 1;

	protected SchemaPresenterUtils presenterUtils;

	protected ContentVersionToVOBuilder contentVersionVOBuilder;
	protected DocumentVO documentVO;
	protected T actionsComponent;

	private transient User currentUser;
	private Record currentDocument;
	protected transient DocumentToVOBuilder voBuilder;
	private transient RMSchemasRecordsServices rmSchemasRecordsServices;
	private transient ModelLayerCollectionExtensions extensions;

	public DocumentActionsPresenterUtils(T actionsComponent) {
		this.actionsComponent = actionsComponent;

		ConstellioFactories constellioFactories = actionsComponent.getConstellioFactories();
		SessionContext sessionContext = actionsComponent.getSessionContext();
		presenterUtils = new SchemaPresenterUtils(Document.DEFAULT_SCHEMA, constellioFactories, sessionContext);
		contentVersionVOBuilder = new ContentVersionToVOBuilder(presenterUtils.modelLayerFactory());

		initTransientObjects();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		rmSchemasRecordsServices = new RMSchemasRecordsServices(presenterUtils.getCollection(),
				presenterUtils.appLayerFactory());
		voBuilder = new DocumentToVOBuilder(presenterUtils.modelLayerFactory());
		extensions = presenterUtils.modelLayerFactory().getExtensions().forCollection(presenterUtils.getCollection());
	}

	public DocumentVO getRecordVO() {
		return this.documentVO;
	}

	public void editDocumentButtonClicked(Map<String, String> params) {
		if (isEditDocumentPossible()) {

			RMNavigationUtils.navigateToEditDocument(documentVO.getId(), params,
					actionsComponent.getConstellioFactories().getAppLayerFactory(),
					actionsComponent.getSessionContext().getCurrentCollection());

			updateSearchResultClicked();
		}
	}

	protected boolean isEditDocumentPossible() {
		return getAuthorizationServices().canWrite(getCurrentUser(), currentDocument());
	}

	public void setRecordVO(RecordVO recordVO) {
		if (recordVO instanceof DocumentVO) {
			this.documentVO = (DocumentVO) recordVO;
		} else {
			this.documentVO = new DocumentVO(recordVO);
		}
		this.currentDocument = documentVO.getRecord();
		presenterUtils.setSchemaCode(recordVO.getSchema().getCode());
	}

	public void updateWindowClosed() {
		currentDocument = presenterUtils.getRecord(documentVO.getId());
		documentVO = voBuilder.build(currentDocument, VIEW_MODE.DISPLAY, actionsComponent.getSessionContext());
		updateActionsComponent();
	}

	public boolean isDeleteContentVersionPossible() {
		return getCurrentUser().has(CorePermissions.DELETE_CONTENT_VERSION).on(currentDocument()) &&
			   !extensions.isModifyBlocked(currentDocument(), getCurrentUser()) &&
			   getCurrentUser().hasDeleteAccess().on(currentDocument());
	}

	public boolean isDeleteContentVersionPossible(ContentVersionVO contentVersionVO) {
		return getContent().isDeleteContentVersionPossible(contentVersionVO.getVersion()) &&
			   !extensions.isModifyBlocked(currentDocument(), getCurrentUser());
	}

	public void deleteContentVersionButtonClicked(ContentVersionVO contentVersionVO) {
		if (isDeleteContentVersionPossible(contentVersionVO)) {
			String version = contentVersionVO.getVersion();

			Record record = presenterUtils.getRecord(documentVO.getId());
			Document document = new Document(record, presenterUtils.types());
			Content content = document.getContent();
			content.deleteVersion(contentVersionVO.getVersion(), presenterUtils.getCurrentUser());

			try {
				presenterUtils.recordServices().update(record);
				currentDocument = record;
				documentVO = voBuilder.build(record, VIEW_MODE.DISPLAY, actionsComponent.getSessionContext());

				ContentVersionVO currentVersionVO = buildContentVersionVO(content);
				documentVO.setContent(currentVersionVO);

				updateActionsComponent();
				actionsComponent.showMessage($("DocumentActionsComponent.contentVersionDeleted", version));

				createVersionDeletionEvent(record, version);

			} catch (RecordServicesException e) {
				actionsComponent.showErrorMessage(MessageUtils.toMessage(e));
			}
		}
	}

	public ContentVersionVO buildContentVersionVO(Content content) {
		return contentVersionVOBuilder.build(content);
	}

	private void createVersionDeletionEvent(Record record, String version) {
		SchemasRecordsServices schemasRecords = new SchemasRecordsServices(getCurrentUser().getCollection(),
				getModelLayerFactory());
		Event event = schemasRecords.newEvent();
		event.setType(EventType.DELETE_DOCUMENT);
		event.setUsername(getCurrentUser().getUsername());
		if (documentVO != null) {
			event.setUserRoles(org.apache.commons.lang3.StringUtils.join(getCurrentUser().getUserRoles().toArray(), "; "));
			event.setTitle(record.getTitle());
			event.setRecordId(documentVO.getId());
			event.setEventPrincipalPath((String) record.get(Schemas.PRINCIPAL_PATH));
		}
		event.setRecordVersion(version);
		try {
			getModelLayerFactory().newRecordServices().add(event);
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}
	}

	protected Content getContent() {
		Record record = currentDocument();
		if (record == null) {
			record = presenterUtils.getRecord(documentVO.getId());
		}
		Document document = new Document(record, presenterUtils.types());
		return document.getContent();
	}

	protected boolean isCurrentUserBorrower() {
		User currentUser = presenterUtils.getCurrentUser();
		Content content = getContent();
		return content != null && currentUser.getId().equals(content.getCheckoutUserId());
	}

	protected boolean isContentCheckedOut() {
		Content content = getContent();
		return content != null && content.getCheckoutUserId() != null;
	}

	public void updateActionsComponent() {
		updateBorrowedMessage();
		RecordVO documentVO = getRecordVO();
		actionsComponent.setRecordVO(documentVO);
	}

	protected void updateBorrowedMessage() {
		if (isContentCheckedOut()) {
			Content content = getContent();
			String borrowDate = DateFormatUtils.format(content.getCheckoutDateTime());
			if (!isCurrentUserBorrower()) {
				String checkoutUserId = content.getCheckoutUserId();
				User user = rmSchemasRecordsServices.getUser(checkoutUserId);
				String borrowerCaption = user.getTitle();
				String borrowedMessageKey = "DocumentActionsComponent.borrowedByOtherUser";
				actionsComponent.setBorrowedMessage(borrowedMessageKey, borrowerCaption, borrowDate);
			} else {
				String borrowerMessageKey = "DocumentActionsComponent.borrowedByCurrentUser";
				actionsComponent.setBorrowedMessage(borrowerMessageKey, borrowDate);
			}
		} else {
			actionsComponent.setBorrowedMessage(null);
		}
	}

	ModelLayerFactory getModelLayerFactory() {
		return presenterUtils.getConstellioFactories().getModelLayerFactory();
	}

	AuthorizationsServices getAuthorizationServices() {
		return getModelLayerFactory().newAuthorizationsServices();
	}

	Record currentDocument() {
		if (currentDocument == null) {
			currentDocument = rmSchemasRecordsServices.get(documentVO.getId());
		}
		return currentDocument;
	}

	public User getCurrentUser() {
		if (currentUser == null) {
			currentUser = presenterUtils.getCurrentUser();
		}
		return currentUser;
	}

	public String getContentTitle() {
		return getContent().getCurrentVersion().getFilename();
	}

	public boolean hasContent() {
		return getContent() != null;
	}

	public void alertWhenAvailable() {
		RMSchemasRecordsServices schemas = new RMSchemasRecordsServices(presenterUtils.getCollection(),
				presenterUtils.appLayerFactory());
		Document document = schemas.getDocument(documentVO.getId());
		List<String> usersToAlert = document.getAlertUsersWhenAvailable();
		String currentUserId = getCurrentUser().getId();
		List<String> newUsersToAlert = new ArrayList<>();
		newUsersToAlert.addAll(usersToAlert);

		String currentBorrower = getCurrentBorrowerOf(document);

		if (!newUsersToAlert.contains(currentUserId) && currentBorrower != null && !currentUserId.equals(currentBorrower)) {
			newUsersToAlert.add(currentUserId);
			document.setAlertUsersWhenAvailable(newUsersToAlert);
			presenterUtils.addOrUpdate(document.getWrappedRecord(), User.GOD);
		}
		actionsComponent.showMessage($("RMObject.createAlert"));
	}

	private String getCurrentBorrowerOf(Document document) {
		return document.getContent() == null ? null : document.getContent().getCheckoutUserId();
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
				} catch (RecordVORuntimeException_NoSuchMetadata e) {
				}
				String clicks = defaultIfBlank(url, documentVO.getId());
				searchEventServices.updateClicks(searchEvent, clicks);
			}
		}
	}
}
