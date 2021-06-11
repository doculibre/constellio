package com.constellio.app.modules.rm.ui.pages.userDocuments;

import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.ui.pages.userDocuments.ListUserDocumentsView.UserFolderBreadcrumbItem;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.RMUserFolder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.ContentVersionVO.InputStreamProvider;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserDocumentVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.UserDocumentToVOBuilder;
import com.constellio.app.ui.framework.builders.UserFolderToVOBuilder;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.framework.components.content.UpdatableContentVersionPresenter;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.icap.IcapException;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.UserDocumentsServices;
import com.constellio.model.services.users.UserServices;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.Html5File;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListUserDocumentsPresenter extends SingleSchemaBasePresenter<ListUserDocumentsView> implements UpdatableContentVersionPresenter {

	Boolean allItemsSelected = false;

	Boolean allItemsDeselected = false;

	private static Logger LOGGER = LoggerFactory.getLogger(ListUserDocumentsPresenter.class);

	private UserDocumentToVOBuilder voBuilder = new UserDocumentToVOBuilder();

	private RecordVODataProvider userFoldersDataProvider;

	private RecordVODataProvider userDocumentsDataProvider;

	private ConstellioEIMConfigs eimConfigs;

	boolean newContentSinceLastRefresh = false;

	private RecordVO currentFolderVO;

	public ListUserDocumentsPresenter(ListUserDocumentsView view) {
		super(view, UserDocument.DEFAULT_SCHEMA);
		this.eimConfigs = new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager());
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	void forParams(String params) {
		SessionContext sessionContext = view.getSessionContext();
		MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
		UserDocumentToVOBuilder userDocumentVOBuilder = new UserDocumentToVOBuilder();
		UserFolderToVOBuilder userFolderVOBuilder = new UserFolderToVOBuilder();

		MetadataSchema userFolderSchema = schema(UserFolder.DEFAULT_SCHEMA);
		MetadataSchemaVO userFolderSchemaVO = schemaVOBuilder.build(userFolderSchema, VIEW_MODE.TABLE, sessionContext);
		userFoldersDataProvider = new RecordVODataProvider(userFolderSchemaVO, userFolderVOBuilder, modelLayerFactory,
				view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				return getUserFoldersQuery();
			}
		};

		MetadataSchema userDocumentSchema = schema(UserDocument.DEFAULT_SCHEMA);
		MetadataSchemaVO userDocumentSchemaVO = schemaVOBuilder.build(userDocumentSchema, VIEW_MODE.TABLE, sessionContext);
		userDocumentsDataProvider = new RecordVODataProvider(userDocumentSchemaVO, userDocumentVOBuilder, modelLayerFactory,
				view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				return getUserDocumentsQuery();
			}
		};

		computeAllItemsSelected();
		view.setUserContent(Arrays.asList(userFoldersDataProvider, userDocumentsDataProvider));
		view.setBackButtonVisible(false);
	}

	private LogicalSearchQuery getUserFoldersQuery() {
		User currentUser = getCurrentUser();
		MetadataSchema userFolderSchema = schema(UserFolder.DEFAULT_SCHEMA);
		Metadata userMetadata = userFolderSchema.getMetadata(UserFolder.USER);
		Metadata parentUserFolderMetadata = userFolderSchema.getMetadata(UserFolder.PARENT_USER_FOLDER);

		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(userFolderSchema).where(userMetadata).is(currentUser.getWrappedRecord());
		if (currentFolderVO != null) {
			condition = condition.andWhere(parentUserFolderMetadata).isEqualTo(currentFolderVO.getId());
		} else {
			condition = condition.andWhere(parentUserFolderMetadata).isNull();
		}
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);
		query.sortAsc(Schemas.IDENTIFIER);

		return query;
	}

	private LogicalSearchQuery getUserDocumentsQuery() {
		User currentUser = getCurrentUser();
		MetadataSchema userDocumentSchema = schema(UserDocument.DEFAULT_SCHEMA);
		Metadata userMetadata = userDocumentSchema.getMetadata(UserDocument.USER);
		Metadata userFolderMetadata = userDocumentSchema.getMetadata(UserDocument.USER_FOLDER);

		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(userDocumentSchema).where(userMetadata).is(currentUser.getWrappedRecord());
		if (currentFolderVO != null) {
			condition = condition.andWhere(userFolderMetadata).isEqualTo(currentFolderVO.getId());
		} else {
			condition = condition.andWhere(userFolderMetadata).isNull();
		}
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);
		query.sortAsc(Schemas.IDENTIFIER);

		return query;
	}

	boolean isSelected(RecordVO recordVO) {
		SessionContext sessionContext = view.getSessionContext();
		return sessionContext.getSelectedRecordIds().contains(recordVO.getId());
	}

	void selectionChanged(RecordVO recordVO, boolean selected) {
		allItemsSelected = false;
		allItemsDeselected = false;

		String recordId = recordVO.getId();
		String schemaTypeCode = recordVO.getSchema().getTypeCode();
		SessionContext sessionContext = view.getSessionContext();
		List<String> selectedRecordIds = sessionContext.getSelectedRecordIds();
		if (selected && !selectedRecordIds.contains(recordId)) {
			sessionContext.addSelectedRecordId(recordId, schemaTypeCode);
		} else if (!selected) {
			sessionContext.removeSelectedRecordId(recordId, schemaTypeCode);
		}
		view.refresh();
	}

	void handleFile(final File file, String fileName, String mimeType, long length) {
		MetadataSchema userDocumentSchema = schema(UserDocument.DEFAULT_SCHEMA);
		Record newRecord = recordServices().newRecordWithSchema(userDocumentSchema);

		SessionContext sessionContext = view.getSessionContext();
		UserVO currentUserVO = sessionContext.getCurrentUser();
		String collection = sessionContext.getCurrentCollection();

		UserServices userServices = modelLayerFactory.newUserServices();

		User currentUser = userServices.getUserInCollection(currentUserVO.getUsername(), collection);

		InputStreamProvider inputStreamProvider = new InputStreamProvider() {
			@Override
			public InputStream getInputStream(String streamName) {
				IOServices ioServices = ConstellioFactories.getInstance().getIoServicesFactory().newIOServices();
				try {
					return ioServices.newFileInputStream(file, streamName);
				} catch (FileNotFoundException e) {
					return null;
				}
			}

			@Override
			public void deleteTemp() {
				FileUtils.deleteQuietly(file);
				file.deleteOnExit();
			}
		};
		UserDocumentVO newUserDocumentVO = (UserDocumentVO) voBuilder.build(newRecord, VIEW_MODE.FORM, view.getSessionContext());
		ContentVersionVO contentVersionVO = new ContentVersionVO(null, null, fileName, mimeType, length, null, null, null,
				null, null, null, inputStreamProvider);
		contentVersionVO.setMajorVersion(true);
		newUserDocumentVO.set(UserDocument.USER, currentUser.getWrappedRecord());
		newUserDocumentVO.set(UserDocument.CONTENT, contentVersionVO);

		try {
			// TODO More elegant way to achieve this
			newRecord = toRecord(newUserDocumentVO);

			addOrUpdate(newRecord);
			contentVersionVO.getInputStreamProvider().deleteTemp();
			if (Boolean.TRUE.equals(contentVersionVO.hasFoundDuplicate())) {
				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
				LogicalSearchQuery duplicateDocumentsQuery = new LogicalSearchQuery()
						.setCondition(LogicalSearchQueryOperators.from(rm.documentSchemaType())
								.where(rm.document.contentHashes()).isEqualTo(contentVersionVO.getDuplicatedHash())
						).filteredByStatus(StatusFilter.ACTIVES)
						.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(Schemas.IDENTIFIER, Schemas.TITLE))
						.setNumberOfRows(100).filteredWithUserRead(getCurrentUser());
				Metadata userMetadata = userDocumentSchema.getMetadata(UserDocument.USER);
				List<Document> duplicateDocuments = rm.searchDocuments(duplicateDocumentsQuery);
				LogicalSearchQuery duplicateUserDocumentsQuery = new LogicalSearchQuery()
						.setCondition(LogicalSearchQueryOperators.from(rm.userDocumentSchemaType())
								.where(rm.userDocument.contentHashes()).isEqualTo(contentVersionVO.getDuplicatedHash())
								.andWhere(Schemas.IDENTIFIER).isNotEqual(newRecord.getId())
								.andWhere(userMetadata).is(currentUser.getWrappedRecord()))
						.filteredByStatus(StatusFilter.ACTIVES)
						.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(Schemas.IDENTIFIER, Schemas.TITLE))
						.filteredWithUserRead(getCurrentUser());
				List<UserDocument> duplicateUserDocuments = rm.searchUserDocuments(duplicateUserDocumentsQuery);
				if (duplicateDocuments.size() > 0 || duplicateUserDocuments.size() > 0) {
					VerticalLayout verticalLayout = new VerticalLayout();
					verticalLayout.addComponent(new Label($("ContentManager.hasFoundDuplicateWithConfirmationPlainText", StringUtils.defaultIfBlank(contentVersionVO.getFileName(), ""))));
					VerticalLayout components = new VerticalLayout();
					for (Document document : duplicateDocuments) {
						components.addComponents(new Label(document.getTitle() + ": "), generateDisplayLink(document));
						verticalLayout.addComponent(components);
					}
					for (UserDocument userDocument : duplicateUserDocuments) {
						components.addComponents(new Label(userDocument.getTitle() + ": "), generateUserDocumentDisplayLink());
						verticalLayout.addComponent(components);
					}
					view.showUploadMessage(verticalLayout);
					newContentSinceLastRefresh = true;
				}
			}
		} catch (final IcapException e) {
			view.showUploadErrorMessage(e.getMessage());
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			Throwable cause = e.getCause();
			if (cause != null && StringUtils.isNotBlank(cause.getMessage()) && cause instanceof ValidationException) {
				view.showUploadErrorMessage(cause.getMessage());
			} else {
				view.showUploadErrorMessage(MessageUtils.toMessage(e));
			}
		}
	}

	public void deleteButtonClicked(RecordVO userContentVO, boolean refreshUI) {
		User currentUser = getCurrentUser();
		String schemaTypeCode = userContentVO.getSchema().getTypeCode();
		Record record = userContentVO.getRecord();

		if (UserFolder.SCHEMA_TYPE.equals(schemaTypeCode)) {
			this.setSchemaCode(UserFolder.DEFAULT_SCHEMA);
			this.setSchemaCode(UserDocument.DEFAULT_SCHEMA);
		}

		try {
			if (UserFolder.SCHEMA_TYPE.equals(schemaTypeCode)) {
				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
				RMUserFolder userFolder = rm.wrapUserFolder(record);
				DecommissioningService decommissioningService = new DecommissioningService(collection, appLayerFactory);
				decommissioningService.deleteUserFolder(userFolder, currentUser);
				if (refreshUI) {
					userFoldersDataProvider.fireDataRefreshEvent();
				}
			} else {
				delete(record);
				if (refreshUI) {
					userDocumentsDataProvider.fireDataRefreshEvent();
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			view.showErrorMessage(MessageUtils.toMessage(e));
		}
	}

	private int secondsSinceLastRefresh = 0;

	private long lastKnownUserFoldersCount = -1;

	private long lastKnownUserDocumentsCount = -1;

	void backgroundViewMonitor() {
		secondsSinceLastRefresh++;
		if (view.isInAWindow() && secondsSinceLastRefresh >= 2 && newContentSinceLastRefresh) {
			newContentSinceLastRefresh = false;
			secondsSinceLastRefresh = 0;
			userDocumentsDataProvider.fireDataRefreshEvent();
		}

		if (secondsSinceLastRefresh >= 10) {
			SearchServices searchServices = modelLayerFactory.newSearchServices();

			secondsSinceLastRefresh = 0;
			long userFoldersCount = searchServices.getResultsCount(getUserFoldersQuery());
			if (lastKnownUserFoldersCount != userFoldersCount) {
				lastKnownUserFoldersCount = userFoldersCount;
				userFoldersDataProvider.fireDataRefreshEvent();
			}
			long userDocumentsCount = searchServices.getResultsCount(getUserDocumentsQuery());
			if (lastKnownUserDocumentsCount != userDocumentsCount) {
				lastKnownUserDocumentsCount = userDocumentsCount;
				userDocumentsDataProvider.fireDataRefreshEvent();
			}
		}
	}

	void computeAllItemsSelected() {
		SessionContext sessionContext = view.getSessionContext();
		List<String> selectedRecordIds = sessionContext.getSelectedRecordIds();
		SearchServices searchServices = modelLayerFactory.newSearchServices();

		List<String> userFolderIds = searchServices.searchRecordIds(getUserFoldersQuery());
		for (String userFolderId : userFolderIds) {
			if (!selectedRecordIds.contains(userFolderId)) {
				allItemsSelected = false;
				return;
			}
		}
		List<String> userDocumentIds = searchServices.searchRecordIds(getUserDocumentsQuery());
		for (String userDocumentId : userDocumentIds) {
			if (!selectedRecordIds.contains(userDocumentId)) {
				allItemsSelected = false;
				return;
			}
		}
		allItemsSelected = !userFolderIds.isEmpty() || !userDocumentIds.isEmpty();
	}

	boolean isAllItemsSelected() {
		return allItemsSelected;
	}

	boolean isAllItemsDeselected() {
		return allItemsDeselected;
	}

	void selectAllClicked() {
		allItemsSelected = true;
		allItemsDeselected = false;

		SessionContext sessionContext = view.getSessionContext();
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		List<String> userFolderIds = searchServices.searchRecordIds(getUserFoldersQuery());
		for (String userFolderId : userFolderIds) {
			sessionContext.addSelectedRecordId(userFolderId, UserFolder.SCHEMA_TYPE);
		}
		List<String> userDocumentIds = searchServices.searchRecordIds(getUserDocumentsQuery());
		for (String userDocumentId : userDocumentIds) {
			sessionContext.addSelectedRecordId(userDocumentId, UserDocument.SCHEMA_TYPE);
		}
	}

	void deselectAllClicked() {
		allItemsSelected = false;
		allItemsDeselected = true;

		SessionContext sessionContext = view.getSessionContext();
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		List<String> userFolderIds = searchServices.searchRecordIds(getUserFoldersQuery());
		for (String userFolderId : userFolderIds) {
			sessionContext.removeSelectedRecordId(userFolderId, UserFolder.SCHEMA_TYPE);
		}
		List<String> userDocumentIds = searchServices.searchRecordIds(getUserDocumentsQuery());
		for (String userDocumentId : userDocumentIds) {
			sessionContext.removeSelectedRecordId(userDocumentId, UserDocument.SCHEMA_TYPE);
		}
	}

	private LinkButton generateDisplayLink(Document document) {
		String constellioUrl = eimConfigs.getConstellioUrl();
		String displayURL = RMNavigationConfiguration.DISPLAY_DOCUMENT;
		String url = constellioUrl + "#!" + displayURL + "/" + document.getId();
		return getUrlButton(url);
	}

	private LinkButton generateUserDocumentDisplayLink() {
		String constellioUrl = eimConfigs.getConstellioUrl();
		String displayURL = RMNavigationConfiguration.LIST_USER_DOCUMENTS;
		String url = constellioUrl + "#!" + displayURL;
		return getUrlButton(url);
	}

	private LinkButton getUrlButton(String url) {
		return new LinkButton(url) {
			@Override
			protected void buttonClick(ClickEvent event) {
				view.openURL(url);
				view.closeAllWindows();
			}
		};
	}

	@Override
	public ContentVersionVO getUpdatedContentVersionVO(RecordVO recordVO, ContentVersionVO previousConventVersionVO) {
		UserDocumentVO updatedUserDocument = voBuilder.build(recordServices().getDocumentById(recordVO.getId()), VIEW_MODE.FORM, view.getSessionContext());
		return updatedUserDocument.getContent();
	}

	public void refreshDocuments() {
		userDocumentsDataProvider.fireDataRefreshEvent();
	}

	public boolean isQuotaSpaceConfigActivated() {
		return new UserDocumentsServices(modelLayerFactory).isQuotaSpaceConfigActivated();
	}

	public double getAvailableSpace() {
		return new UserDocumentsServices(modelLayerFactory).getAvailableSpaceInMegaBytes(getCurrentUser().getUsername(), collection);
	}

	public boolean isSpaceLimitReached(DragAndDropEvent event) {
		long totalLength = 0L;
		DragAndDropWrapper.WrapperTransferable transferable = (DragAndDropWrapper.WrapperTransferable) event
				.getTransferable();
		Html5File[] files = transferable.getFiles();
		if (files == null) {
			return false;
		}

		for (Html5File file : files) {
			totalLength = totalLength + file.getFileSize();
		}
		return new UserDocumentsServices(modelLayerFactory)
				.isSpaceLimitReached(getCurrentUser().getUsername(), collection, totalLength);
	}

	public boolean isSpaceLimitReached(long length) {
		return new UserDocumentsServices(modelLayerFactory)
				.isSpaceLimitReached(getCurrentUser().getUsername(), collection, length);
	}

	public boolean isDisplayButtonVisible() {
		return ConstellioAgentUtils.isAdvancedFeaturesEnabled();
	}

	public boolean isDisplayButtonVisible(RecordVO recordVO) {
		return recordVO.getSchema().getCode().equals(UserFolder.DEFAULT_SCHEMA);
	}

	public void displayButtonClicked(RecordVO recordVO) {
		this.currentFolderVO = recordVO;
		view.setBackButtonVisible(true);
		updateBreadcrumbs();
		view.refresh();
	}

	public void backButtonClicked() {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		UserFolder currentFolder = rm.getUserFolder(currentFolderVO.getId());
		if (currentFolder.getParent() != null) {
			UserFolder currentFolderParent = rm.getUserFolder(currentFolder.getParent());
			currentFolderVO = voBuilder.build(currentFolderParent.getWrappedRecord(), VIEW_MODE.DISPLAY, view.getSessionContext());
		} else {
			currentFolderVO = null;
			view.setBackButtonVisible(false);
		}
		updateBreadcrumbs();
		view.refresh();
	}

	public boolean breadcrumbItemClicked(BreadcrumbItem item) {
		boolean handled;
		if (item instanceof UserFolderBreadcrumbItem) {
			String folderId = ((UserFolderBreadcrumbItem) item).getFolderId();
			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
			UserFolder currentFolder = rm.getUserFolder(folderId);
			currentFolderVO = voBuilder.build(currentFolder.getWrappedRecord(), VIEW_MODE.DISPLAY, view.getSessionContext());

			updateBreadcrumbs();
			view.refresh();
			handled = true;
		} else if (item instanceof TitleBreadcrumbTrail.CurrentViewItem) {
			currentFolderVO = null;

			view.setBackButtonVisible(false);
			updateBreadcrumbs();
			view.refresh();
			handled = true;
		} else {
			handled = false;
		}
		return handled;
	}

	private void updateBreadcrumbs() {
		List<BreadcrumbItem> items = new ArrayList<>();
		if (currentFolderVO != null) {
			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
			String folderId = currentFolderVO.getId();
			while (folderId != null) {
				UserFolder currentFolder = rm.getUserFolder(folderId);
				boolean enabled = !items.isEmpty();
				items.add(new UserFolderBreadcrumbItem(folderId, enabled));
				folderId = currentFolder.getParent();
			}
		}
		view.setBreadcrumbs(items);
	}
}
