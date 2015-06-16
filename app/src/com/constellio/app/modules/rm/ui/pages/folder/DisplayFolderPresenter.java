/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.ui.pages.folder;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.data.utils.TimeProvider.getLocalDateTime;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.enums.DefaultTabInFolderDisplay;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate.SchemaType;
import com.constellio.app.modules.rm.services.FolderDocumentMetadataSyncServices;
import com.constellio.app.modules.rm.services.LabelTemplateServices;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.builders.FolderToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.ComponentState;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.entities.FolderVO;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.ContentVersionVO.InputStreamProvider;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.borrowingServices.BorrowingServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.roles.Roles;

public class DisplayFolderPresenter extends SingleSchemaBasePresenter<DisplayFolderView> {
	private static Logger LOGGER = LoggerFactory.getLogger(DisplayFolderPresenter.class);
	private RecordVODataProvider documentsDataProvider;
	private RecordVODataProvider subFoldersDataProvider;
	private MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
	private FolderToVOBuilder voBuilder = new FolderToVOBuilder();
	private DocumentToVOBuilder documentVOBuilder = new DocumentToVOBuilder();
	private SchemaPresenterUtils documentPresenterUtils;
	private FolderVO folderVO;

	private transient RMSchemasRecordsServices rmSchemasRecordsServices;
	private transient BorrowingServices borrowingServices;

	public DisplayFolderPresenter(DisplayFolderView view) {
		super(view, Folder.DEFAULT_SCHEMA);

		ConstellioFactories constellioFactories = view.getConstellioFactories();
		SessionContext sessionContext = view.getSessionContext();
		documentPresenterUtils = new SchemaPresenterUtils(Document.DEFAULT_SCHEMA, constellioFactories, sessionContext);
		initTransientObjects();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, modelLayerFactory);
		borrowingServices = new BorrowingServices(collection, modelLayerFactory);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public void forParams(String params) {
		Record record = getRecord(params);
		this.folderVO = voBuilder.build(record, VIEW_MODE.DISPLAY, view.getSessionContext());
		setSchemaCode(record.getSchemaCode());
		view.setRecord(folderVO);
	}

	public void selectInitialTabForUser() {
		String defaultTabInFolderDisplay = getCurrentUser().getDefaultTabInFolderDisplay();
		if (StringUtils.isNotBlank(defaultTabInFolderDisplay)) {
			if (DefaultTabInFolderDisplay.METADATA.getCode().equals(defaultTabInFolderDisplay)) {
				view.selectMetadataTab();
			} else if (DefaultTabInFolderDisplay.DOCUMENTS.getCode().equals(defaultTabInFolderDisplay)) {
				view.selectDocumentsTab();
			} else if (DefaultTabInFolderDisplay.SUB_FOLDERS.getCode().equals(defaultTabInFolderDisplay)) {
				view.selectSubFoldersTab();
			}
		}
	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		return user.hasReadAccess().on(restrictedRecord);
	}

	@Override
	protected List<String> getRestrictedRecordIds(String params) {
		return asList(folderVO.getId());
	}

	private void disableMenuItems(Folder folder) {
		User user = getCurrentUser();
		view.setLogicallyDeletable(getDeleteButtonState(user, folder));
		view.setEditButtonState(getEditButtonState(user, folder));
		view.setAddSubFolderButtonState(getAddFolderButtonState(user, folder));
		view.setAddDocumentButtonState(getAddDocumentButtonState(user, folder));
		view.setDuplicateFolderButtonState(getDuplicateFolderButtonState(user, folder));
		view.setAuthorizationButtonState(getAuthorizationButtonState(user, folder));
		view.setShareFolderButtonState(getShareButtonState(user, folder));
		view.setPrintButtonState(getPrintButtonState(user, folder));
		view.setBorrowButtonState(getBorrowButtonState(user, folder));
		view.setReturnFolderButtonState(getReturnFolderButtonState(user, folder));
		view.setBorrowedMessage(getBorrowMessageState(folder));
	}

	String getBorrowMessageState(Folder folder) {
		String borrowedMessage;
		if (folder.getBorrowed() != null && folder.getBorrowed()) {
			borrowedMessage = "DisplayFolderview.borrowedFolder";
		} else {
			borrowedMessage = null;
		}
		return borrowedMessage;
	}

	private ComponentState getBorrowButtonState(User user, Folder folder) {
		try {
			borrowingServices.validateCanBorrow(user, folder.getWrappedRecord());
			return ComponentState.visibleIf(user.has(RMPermissionsTo.BORROW_FOLDER).on(folder));
		} catch (Exception e) {
			return ComponentState.INVISIBLE;
		}
	}

	private ComponentState getReturnFolderButtonState(User user, Folder folder) {
		try {
			borrowingServices.validateCanReturnFolder(user, folder.getWrappedRecord());
			return ComponentState.visibleIf(user.has(RMPermissionsTo.BORROW_FOLDER).on(folder));
		} catch (Exception e) {
			return ComponentState.INVISIBLE;
		}
	}

	ComponentState getPrintButtonState(User user, Folder folder) {
		AuthorizationsServices authorizationsServices = modelLayerFactory.newAuthorizationsServices();
		if (authorizationsServices.canRead(user, folder.getWrappedRecord())) {
			if (folder.getArchivisticStatus().isInactive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER).on(folder) && user
							.has(RMPermissionsTo.MODIFY_INACTIVE_FOLDERS).on(folder));
				}
				return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_INACTIVE_FOLDERS).on(folder));
			}
			if (folder.getArchivisticStatus().isSemiActive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER).on(folder) && user
							.has(RMPermissionsTo.MODIFY_SEMIACTIVE_FOLDERS).on(folder));
				}
				return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_FOLDERS).on(folder));
			}
			return ComponentState.ENABLED;
		}
		return ComponentState.INVISIBLE;
	}

	private ComponentState getDuplicateFolderButtonState(User user, Folder folder) {
		AuthorizationsServices authorizationsServices = modelLayerFactory.newAuthorizationsServices();
		if (authorizationsServices.canWrite(user, folder.getWrappedRecord())) {
			if (folder.getArchivisticStatus().isInactive()) {
				return ComponentState.visibleIf(user.has(RMPermissionsTo.DUPLICATE_INACTIVE_FOLDER).on(folder));
			}
			if (folder.getArchivisticStatus().isSemiActive()) {
				return ComponentState.visibleIf(user.has(RMPermissionsTo.DUPLICATE_SEMIACTIVE_FOLDER).on(folder));
			}
			return ComponentState.ENABLED;
		}
		return ComponentState.INVISIBLE;
	}

	private ComponentState getAuthorizationButtonState(User user, Folder folder) {
		return ComponentState.visibleIf(user.has(RMPermissionsTo.MANAGE_FOLDER_AUTHORIZATIONS).on(folder));
	}

	private ComponentState getShareButtonState(User user, Folder folder) {
		if (user.has(RMPermissionsTo.SHARE_FOLDER).on(folder)) {
			if (folder.getArchivisticStatus().isInactive()) {
				return ComponentState.visibleIf(user.has(RMPermissionsTo.SHARE_A_INACTIVE_FOLDER).on(folder));
			}
			if (folder.getArchivisticStatus().isSemiActive()) {
				return ComponentState.visibleIf(user.has(RMPermissionsTo.SHARE_A_SEMIACTIVE_FOLDER).on(folder));
			}
			return ComponentState.ENABLED;
		}
		return ComponentState.INVISIBLE;
	}

	ComponentState getDeleteButtonState(User user, Folder folder) {
		if (user.hasDeleteAccess().on(folder)) {
			if (folder.getArchivisticStatus().isInactive()) {
				System.out.println(user.has(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER).on(folder));
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER).on(folder) && user
							.has(RMPermissionsTo.DELETE_INACTIVE_FOLDERS).on(folder));
				}
				return ComponentState.visibleIf(user.has(RMPermissionsTo.DELETE_INACTIVE_FOLDERS).on(folder));
			}
			if (folder.getArchivisticStatus().isSemiActive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER).on(folder) && user
							.has(RMPermissionsTo.DELETE_SEMIACTIVE_FOLDERS).on(folder));
				}
				return ComponentState.visibleIf(user.has(RMPermissionsTo.DELETE_SEMIACTIVE_FOLDERS).on(folder));
			}
			return ComponentState.ENABLED;
		}
		return ComponentState.INVISIBLE;
	}

	ComponentState getEditButtonState(User user, Folder folder) {
		if (user.hasWriteAccess().on(folder)) {
			if (folder.getArchivisticStatus().isInactive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER).on(folder) && user
							.has(RMPermissionsTo.MODIFY_INACTIVE_FOLDERS).on(folder));
				}
				return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_INACTIVE_FOLDERS).on(folder));
			}
			if (folder.getArchivisticStatus().isSemiActive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER).on(folder) && user
							.has(RMPermissionsTo.MODIFY_SEMIACTIVE_FOLDERS).on(folder));
				}
				return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_FOLDERS).on(folder));
			}
			return ComponentState.ENABLED;
		}
		return ComponentState.INVISIBLE;
	}

	ComponentState getAddFolderButtonState(User user, Folder folder) {
		if (user.hasWriteAccess().on(folder) &&
				user.hasAll(RMPermissionsTo.CREATE_SUB_FOLDERS, RMPermissionsTo.CREATE_FOLDERS).on(folder)) {
			if (folder.getArchivisticStatus().isInactive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER).on(folder) && user
							.has(RMPermissionsTo.CREATE_SUB_FOLDERS_IN_INACTIVE_FOLDERS).on(folder));
				}
				return ComponentState.visibleIf(user.has(RMPermissionsTo.CREATE_SUB_FOLDERS_IN_INACTIVE_FOLDERS).on(folder));
			}
			if (folder.getArchivisticStatus().isSemiActive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER).on(folder) && user
							.has(RMPermissionsTo.CREATE_SUB_FOLDERS_IN_SEMIACTIVE_FOLDERS).on(folder));
				}
				return ComponentState.visibleIf(user.has(RMPermissionsTo.CREATE_SUB_FOLDERS_IN_SEMIACTIVE_FOLDERS).on(folder));
			}
			return ComponentState.ENABLED;
		}
		return ComponentState.INVISIBLE;
	}

	ComponentState getAddDocumentButtonState(User user, Folder folder) {
		if (user.hasWriteAccess().on(folder) &&
				user.has(RMPermissionsTo.CREATE_DOCUMENTS).on(folder)) {
			if (folder.getArchivisticStatus().isInactive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER).on(folder) && user
							.has(RMPermissionsTo.CREATE_INACTIVE_DOCUMENT).on(folder));
				}
				return ComponentState.visibleIf(user.has(RMPermissionsTo.CREATE_INACTIVE_DOCUMENT).on(folder));
			}
			if (folder.getArchivisticStatus().isSemiActive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER).on(folder) && user
							.has(RMPermissionsTo.CREATE_SEMIACTIVE_DOCUMENT).on(folder));
				}
				return ComponentState.visibleIf(user.has(RMPermissionsTo.CREATE_SEMIACTIVE_DOCUMENT).on(folder));
			}
			return ComponentState.ENABLED;
		}
		return ComponentState.INVISIBLE;
	}

	private MetadataSchemaType getFoldersSchemaType() {
		return schemaType(Folder.SCHEMA_TYPE);
	}

	private MetadataSchemaType getDocumentsSchemaType() {
		return schemaType(Document.SCHEMA_TYPE);
	}

	private MetadataSchema getFoldersSchema() {
		return schema(Folder.DEFAULT_SCHEMA);
	}

	private MetadataSchema getDocumentsSchema() {
		return schema(Document.DEFAULT_SCHEMA);
	}

	public void viewAssembled() {
		MetadataSchema documentsSchema = getDocumentsSchema();
		MetadataSchemaVO documentsSchemaVO = schemaVOBuilder.build(documentsSchema, VIEW_MODE.TABLE);
		documentsDataProvider = new RecordVODataProvider(documentsSchemaVO, voBuilder, modelLayerFactory) {
			@Override
			protected LogicalSearchQuery getQuery() {
				Record record = getRecord(folderVO.getId());
				MetadataSchemaType documentsSchemaType = getDocumentsSchemaType();
				MetadataSchema documentsSchema = getDocumentsSchema();
				Metadata folderMetadata = documentsSchema.getMetadata(Document.FOLDER);
				LogicalSearchQuery query = new LogicalSearchQuery();
				query.setCondition(from(documentsSchemaType).where(folderMetadata).is(record));
				return query.sortDesc(Schemas.MODIFIED_ON);
			}
		};
		view.setDocuments(documentsDataProvider);

		MetadataSchemaVO foldersSchemaVO = schemaVOBuilder.build(schema(), VIEW_MODE.TABLE);
		subFoldersDataProvider = new RecordVODataProvider(foldersSchemaVO, voBuilder, modelLayerFactory) {
			@Override
			protected LogicalSearchQuery getQuery() {
				Record record = getRecord(folderVO.getId());
				MetadataSchemaType foldersSchemaType = getFoldersSchemaType();
				MetadataSchema foldersSchema = getFoldersSchema();
				Metadata parentFolderMetadata = foldersSchema.getMetadata(Folder.PARENT_FOLDER);
				LogicalSearchQuery query = new LogicalSearchQuery();
				query.setCondition(from(foldersSchemaType).where(parentFolderMetadata).is(record));
				return query.sortDesc(Schemas.MODIFIED_ON);
			}
		};
		view.setSubFolders(subFoldersDataProvider);

		RMSchemasRecordsServices schemas = new RMSchemasRecordsServices(collection, modelLayerFactory);
		Folder folder = schemas.wrapFolder(toRecord(folderVO));
		disableMenuItems(folder);
		modelLayerFactory.newLoggingServices().logRecordView(folder.getWrappedRecord(), getCurrentUser());
	}

	public void backButtonClicked() {
		String parentId = folderVO.getParentFolder();
		if (parentId != null) {
			view.navigateTo().displayFolder(parentId);
		} else {
			view.navigateTo().recordsManagement();
		}
	}

	public void addDocumentButtonClicked() {
		view.navigateTo().addDocument(folderVO.getId(), null);
	}

	public void addSubFolderButtonClicked() {
		view.navigateTo().addFolder(folderVO.getId(), null);
	}

	public void editFolderButtonClicked() {
		view.navigateTo().editFolder(folderVO.getId());
	}

	public void deleteFolderButtonClicked(String reason) {
		String parentId = folderVO.get(Folder.PARENT_FOLDER);
		Record record = toRecord(folderVO);
		delete(record, reason);
		if (parentId != null) {
			view.navigateTo().displayFolder(parentId);
		} else {
			view.navigateTo().recordsManagement();
		}
	}

	public void duplicateFolderButtonClicked() {
		Folder folder = rmSchemasRecordsServices().getFolder(folderVO.getId());
		Folder duplicatedFolder = decommissioningService().duplicateAndSave(folder);
		view.navigateTo().editFolder(duplicatedFolder.getId());
	}

	public void duplicateStructureButtonClicked() {
		Folder folder = rmSchemasRecordsServices().getFolder(folderVO.getId());
		Folder duplicatedFolder = decommissioningService().duplicateStructureAndSave(folder);
		view.navigateTo().displayFolder(duplicatedFolder.getId());
		view.showMessage($("DisplayFolderView.duplicated"));
	}

	public void linkToFolderButtonClicked() {
		// TODO ZeroClipboardComponent
		view.showMessage("Clipboard integration TODO!");
	}

	public void addAuthorizationButtonClicked() {
		view.navigateTo().listObjectAuthorizations(folderVO.getId());
	}

	public void shareFolderButtonClicked() {
		view.navigateTo().shareContent(folderVO.getId());
	}

	public void printLabelButtonClicked() {
		// TODO Plug reports component
		view.showMessage("Print label window!");
	}

	public void documentClicked(RecordVO documentVO) {
		view.navigateTo().displayDocument(documentVO.getId());
	}

	public void subFolderClicked(RecordVO subFolderVO) {
		view.navigateTo().displayFolder(subFolderVO.getId());
	}

	private DecommissioningService decommissioningService() {
		return new DecommissioningService(getCurrentUser().getCollection(), modelLayerFactory);
	}

	private RMSchemasRecordsServices rmSchemasRecordsServices() {
		return new RMSchemasRecordsServices(getCurrentUser().getCollection(), modelLayerFactory);
	}

	private boolean documentExists(String fileName) {
		Record record = getRecord(folderVO.getId());

		MetadataSchemaType documentsSchemaType = getDocumentsSchemaType();
		MetadataSchema documentsSchema = getDocumentsSchema();
		Metadata folderMetadata = documentsSchema.getMetadata(Document.FOLDER);
		Metadata titleMetadata = documentsSchema.getMetadata(Schemas.TITLE.getCode());
		LogicalSearchQuery query = new LogicalSearchQuery();
		LogicalSearchCondition parentCondition = from(documentsSchemaType).where(folderMetadata).is(record);
		query.setCondition(parentCondition.andWhere(titleMetadata).is(fileName));

		SearchServices searchServices = modelLayerFactory.newSearchServices();
		return searchServices.query(query).getNumFound() > 0;
	}

	public void contentVersionUploaded(ContentVersionVO uploadedContentVO) {
		view.selectDocumentsTab();
		String fileName = uploadedContentVO.getFileName();
		if (!documentExists(fileName)) {
			try {
				uploadedContentVO.setMajorVersion(true);
				Record newRecord;
				if (rmSchemasRecordsServices().isEmail(fileName)) {
					InputStreamProvider inputStreamProvider = uploadedContentVO.getInputStreamProvider();
					InputStream in = inputStreamProvider.getInputStream(DisplayFolderPresenter.class + ".contentVersionUploaded");
					Document document = rmSchemasRecordsServices.newEmail(fileName, in);
					newRecord = document.getWrappedRecord();
				} else {
					Document document = rmSchemasRecordsServices.newDocument();
					newRecord = document.getWrappedRecord();
				}
				DocumentVO documentVO = documentVOBuilder.build(newRecord, VIEW_MODE.FORM);
				documentVO.setFolder(folderVO);
				documentVO.setTitle(fileName);
				documentVO.setContent(uploadedContentVO);

				documentPresenterUtils.setSchemaCode(newRecord.getSchemaCode());
				newRecord = documentPresenterUtils.toRecord(documentVO);

				new FolderDocumentMetadataSyncServices(appLayerFactory, collection)
						.updateFolderWithContentProperties(newRecord);

				documentPresenterUtils.addOrUpdate(newRecord);
				documentsDataProvider.fireDataRefreshEvent();
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	public String getFolderTitle() {
		return folderVO.getTitle();
	}

	private String validateUserAndPreviewReturnDate(String userId, Date previewReturnDate) {
		String errorMessage = null;
		if (StringUtils.isBlank(userId) || userId == null) {
			errorMessage = "DisplayFolderView.invalidBorrower";
			return errorMessage;
		}
		if (previewReturnDate != null) {
			LocalDateTime dateTime = LocalDateTime.fromDateFields(previewReturnDate);
			if (dateTime.isBefore(getLocalDateTime())) {
				errorMessage = "DisplayFolderView.invalidPreviewReturnDate";
				return errorMessage;
			}
		} else {
			errorMessage = "DisplayFolderView.invalidPreviewReturnDate";
			return errorMessage;
		}
		return errorMessage;
	}

	public boolean borrowFolder(Date previewReturnDate, String userId) {
		String errorMessage = validateUserAndPreviewReturnDate(userId, previewReturnDate);
		if (errorMessage != null) {
			view.showErrorMessage($(errorMessage));
			return false;
		} else {
			Record record = recordServices().getDocumentById(userId);
			User borrowerEntered = wrapUser(record);
			try {
				borrowingServices.borrowFolder(folderVO.getId(), previewReturnDate, getCurrentUser(), borrowerEntered);
				view.navigateTo().displayFolder(folderVO.getId());
				return true;
			} catch (RecordServicesException e) {
				view.showErrorMessage($("DisplayFolderView.cannotBorrowFolder"));
				return false;
			}
		}
	}

	public void returnFolder() {
		try {
			borrowingServices.returnFolder(folderVO.getId(), getCurrentUser());
			view.navigateTo().displayFolder(folderVO.getId());
		} catch (RecordServicesException e) {
			view.showErrorMessage($("DisplayFolderView.cannotReturnFolder"));
		}
	}

	private User wrapUser(Record record) {
		return new User(record, types(), getRoles());
	}

	private Roles getRoles() {
		return modelLayerFactory.getRolesManager().getCollectionRoles(collection);
	}

	//TODO Thiago
	public List<LabelTemplate> getTemplates() {
		LabelTemplateServices labelTemplateServices = new LabelTemplateServices(appLayerFactory);
		return labelTemplateServices.getTemplates(SchemaType.FOLDER.name());
	}
}
