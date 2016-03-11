package com.constellio.app.modules.rm.ui.pages.document;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.components.document.fields.CustomDocumentField;
import com.constellio.app.modules.rm.ui.components.document.fields.DocumentContentField;
import com.constellio.app.modules.rm.ui.components.document.fields.DocumentContentField.ContentUploadedListener;
import com.constellio.app.modules.rm.ui.components.document.fields.DocumentContentField.NewFileClickListener;
import com.constellio.app.modules.rm.ui.components.document.fields.DocumentCopyRuleField;
import com.constellio.app.modules.rm.ui.components.document.fields.DocumentFolderField;
import com.constellio.app.modules.rm.ui.components.document.fields.DocumentTypeField;
import com.constellio.app.modules.rm.ui.components.document.newFile.NewFileWindow.NewFileCreatedListener;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMObject;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.ContentVersionToVOBuilder;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.users.UserServices;

public class AddEditDocumentPresenter extends SingleSchemaBasePresenter<AddEditDocumentView> {
	private transient ContentVersionToVOBuilder contentVersionToVOBuilder;

	private DocumentToVOBuilder voBuilder;
	private boolean addView;
	private boolean addViewWithCopy;
	private DocumentVO documentVO;
	private String userDocumentId;
	private SchemaPresenterUtils userDocumentPresenterUtils;
	private transient RMSchemasRecordsServices rmSchemasRecordsServices;
	private boolean newFile;

	public AddEditDocumentPresenter(AddEditDocumentView view) {
		super(view, Document.DEFAULT_SCHEMA);
		initTransientObjects();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		ConstellioFactories constellioFactories = view.getConstellioFactories();
		SessionContext sessionContext = view.getSessionContext();
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, modelLayerFactory);
		userDocumentPresenterUtils = new SchemaPresenterUtils(UserDocument.DEFAULT_SCHEMA, constellioFactories, sessionContext);
		contentVersionToVOBuilder = new ContentVersionToVOBuilder(modelLayerFactory);
		voBuilder = new DocumentToVOBuilder(modelLayerFactory);
	}

	public void forParams(String params) {
		Map<String, String> paramsMap = ParamUtils.getParamsMap(params);
		String id = paramsMap.get("id");
		String idCopy = paramsMap.get("idCopy");
		String parentId = paramsMap.get("parentId");
		userDocumentId = paramsMap.get("userDocumentId");

		Document document;
		if (StringUtils.isNotBlank(id)) {
			document = rmSchemasRecordsServices.getDocument(id);
			addView = false;
		} else {
			document = rmSchemasRecordsServices.newDocument();
			if (StringUtils.isNotBlank(idCopy)) {
				addViewWithCopy = true;
			}
			addView = true;
		}

		documentVO = voBuilder.build(document.getWrappedRecord(), VIEW_MODE.FORM, view.getSessionContext());
		if (userDocumentId != null) {
			populateFromUserDocument(userDocumentId);
		}
		if (addViewWithCopy) {
			populateFromExistingDocument(idCopy);
		}
		if (parentId != null) {
			documentVO.set(Document.FOLDER, parentId);
		}
		if (areDocumentRetentionRulesEnabled()) {
			Document record = rmSchemas().wrapDocument(toRecord(documentVO));
			recordServices().recalculate(record);
			documentVO.set(Document.APPLICABLE_COPY_RULES, record.getApplicableCopyRules());
		}
		String currentSchemaCode = documentVO.getSchema().getCode();
		setSchemaCode(currentSchemaCode);
		view.setRecord(documentVO);
	}

	private void populateFromExistingDocument(String existingDocumentId) {
		Document document = rmSchemasRecordsServices.getDocument(existingDocumentId);
		Content content = document.getContent();
		ContentVersion contentVersion = content.getCurrentVersion();
		ContentVersionVO contentVersionVO = contentVersionToVOBuilder.build(content, contentVersion);
		contentVersionVO.setMajorVersion(contentVersion.isMajor());
		contentVersionVO.setVersion(contentVersion.getVersion());
		documentVO.setContent(contentVersionVO);
		documentVO.setTitle(document.getTitle() + " (" + $("AddEditDocumentViewImpl.copy") + ")");
		documentVO.setFolder(document.getFolder());
	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		RMObject restrictedRMObject = rmSchemas().wrapRMObject(restrictedRecord);

		if (addView) {
			List<String> requiredPermissions = new ArrayList<>();
			requiredPermissions.add(RMPermissionsTo.CREATE_DOCUMENTS);
			FolderStatus status = restrictedRMObject.getArchivisticStatus();
			if (status != null && status.isSemiActive()) {
				requiredPermissions.add(RMPermissionsTo.CREATE_SEMIACTIVE_DOCUMENT);
				if (restrictedRMObject.getBorrowed() != null && restrictedRMObject.getBorrowed()) {
					requiredPermissions.add(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER);
				}
			}

			if (status != null && status.isInactive()) {
				requiredPermissions.add(RMPermissionsTo.CREATE_INACTIVE_DOCUMENT);
				if (restrictedRMObject.getBorrowed() != null && restrictedRMObject.getBorrowed()) {
					requiredPermissions.add(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER);
				}
			}

			return user.hasAll(requiredPermissions).on(restrictedRMObject) && user.hasWriteAccess().on(restrictedRMObject);
		} else {
			List<String> requiredPermissions = new ArrayList<>();
			FolderStatus status = restrictedRMObject.getArchivisticStatus();
			if (status != null && status.isSemiActive()) {
				requiredPermissions.add(RMPermissionsTo.MODIFY_SEMIACTIVE_DOCUMENT);
				if (restrictedRMObject.getBorrowed() != null && restrictedRMObject.getBorrowed()) {
					requiredPermissions.add(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER);
				}
			}

			if (status != null && status.isInactive()) {
				requiredPermissions.add(RMPermissionsTo.MODIFY_INACTIVE_DOCUMENT);
				if (restrictedRMObject.getBorrowed() != null && restrictedRMObject.getBorrowed()) {
					requiredPermissions.add(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER);
				}
			}

			return user.hasAll(requiredPermissions).on(restrictedRMObject) && user.hasWriteAccess().on(restrictedRMObject);
		}
	}

	@Override
	protected List<String> getRestrictedRecordIds(String params) {
		Map<String, String> paramsMap = ParamUtils.getParamsMap(params);
		String parentId = paramsMap.get("parentId");
		List<String> ids = new ArrayList<>();
		if (!addView) {
			ids.add(documentVO.getId());
		} else if (parentId != null) {
			ids.add(parentId);
		}
		return ids;
	}

	protected void populateFromUserDocument(String userDocumentId) {
		Record userDocumentRecord = userDocumentPresenterUtils.getRecord(userDocumentId);
		UserDocument userDocument = new UserDocument(userDocumentRecord, userDocumentPresenterUtils.types());
		Content content = userDocument.getContent();
		ContentVersion contentVersion = content.getCurrentVersion();
		ContentVersionVO contentVersionVO = contentVersionToVOBuilder.build(content, contentVersion);
		String folderId = userDocument.getFolder();

		// Reset as new content
		contentVersionVO.setHash(null);
		contentVersionVO.setVersion(null);

		String filename = contentVersion.getFilename();
		String extension = FilenameUtils.getExtension(filename);
		if ("eml".equals(extension) || "msg".equals(extension)) {
			InputStream messageInputStream = contentVersionVO.getInputStreamProvider().getInputStream("populateFromUserDocument");
			Email email = rmSchemasRecordsServices.newEmail(filename, messageInputStream);
			documentVO = voBuilder.build(email.getWrappedRecord(), VIEW_MODE.FORM, view.getSessionContext());
			contentVersionVO.setMajorVersion(true);
		}
		if (StringUtils.isNotBlank(folderId)) {
			documentVO.setFolder(folderId);
		}
		if (StringUtils.isBlank(documentVO.getTitle())) {
			documentVO.setTitle(userDocument.getTitle());
		}
		documentVO.setContent(contentVersionVO);
	}

	public boolean isAddView() {
		return addView;
	}

	public void cancelButtonClicked() {
		if (addView) {
			String parentId = documentVO.getFolder();
			if (parentId != null) {
				view.navigate().to(RMViews.class).displayFolder(parentId);
			} else {
				view.navigate().to().home();
			}
		} else {
			view.navigate().to(RMViews.class).displayDocument(documentVO.getId());
		}
	}

	private void setAsNewVersionOfContent(Document document) {
		ContentManager contentManager = modelLayerFactory.getContentManager();
		Document documentBeforeChange = rmSchemasRecordsServices.getDocument(document.getId());
		Content contentBeforeChange = documentBeforeChange.getContent();
		ContentVersionVO contentVersionVO = documentVO.getContent();
		String filename = contentVersionVO.getFileName();
		InputStream in = contentVersionVO.getInputStreamProvider().getInputStream("AddEditDocumentPresenter.saveButtonClicked");
		boolean majorVersion = Boolean.TRUE.equals(contentVersionVO.isMajorVersion());

		ContentVersionDataSummary contentVersionSummary;
		try {
			contentVersionSummary = contentManager.upload(in, filename);
		} finally {
			IOUtils.closeQuietly(in);
		}
		contentBeforeChange.updateContentWithName(getCurrentUser(), contentVersionSummary, majorVersion, filename);
		document.setContent(contentBeforeChange);
	}

	public void saveButtonClicked() {
		Record record = toRecord(documentVO, newFile);
		Document document = rmSchemas().wrapDocument(record);

		boolean editWithUserDocument = !addView && userDocumentId != null;
		if (editWithUserDocument) {
			setAsNewVersionOfContent(document);
		}

		if (!canSaveDocument(document, getCurrentUser())) {
			view.showMessage($("AddEditDocumentView.noPermissionToSaveDocument"));
			return;
		}

		if (addViewWithCopy) {
			setRecordContent(record, documentVO);
		}
		if (newFile) {
			document.getContent().checkOut(getCurrentUser());
		}
		LocalDateTime time = TimeProvider.getLocalDateTime();
		if (isAddView()) {
			document.setFormCreatedBy(getCurrentUser()).setFormCreatedOn(time);
		}
		document.setFormModifiedBy(getCurrentUser()).setFormModifiedOn(time);

		if (documentVO.getContent() != null) {
			String currentTitle = document.getTitle();
			String currentContentFilename = documentVO.getContent().getFileName();
			String extension = FilenameUtils.getExtension(currentContentFilename);
			if (currentTitle.endsWith("." + extension)) {
				document.getContent().renameCurrentVersion(currentTitle);
			}
		}

		addOrUpdate(record);
		if (userDocumentId != null) {
			UserServices userServices = modelLayerFactory.newUserServices();
			Record userDocumentRecord = userDocumentPresenterUtils.getRecord(userDocumentId);
			String userDocumentCollection = userDocumentRecord.getCollection();
			User currentUser = getCurrentUser();
			User userDocumentUser = userServices.getUserInCollection(currentUser.getUsername(), userDocumentCollection);
			userDocumentPresenterUtils.delete(userDocumentRecord, null, userDocumentUser);
		}
		view.navigate().to(RMViews.class).displayDocument(record.getId());
	}

	private void setRecordContent(Record record, DocumentVO documentVO) {
		Metadata contentMetadata = schema().getMetadata(Document.CONTENT);
		Object content = record.get(contentMetadata);
		if (content != null) {
			//user modified the content
			return;
		}
		ContentManager contentManager = modelLayerFactory.getContentManager();
		ContentVersionVO contentVO = documentVO.getContent();
		Boolean majorVersion = contentVO.isMajorVersion();
		String fileName = contentVO.getFileName();
		String hash = contentVO.getHash();
		ContentVersionDataSummary contentVersionDataSummary = contentManager.getContentVersionSummary(hash);
		Content copiedContent;
		if (majorVersion != null && majorVersion) {
			copiedContent = contentManager.createMajor(getCurrentUser(), fileName, contentVersionDataSummary);
		} else {
			copiedContent = contentManager.createMinor(getCurrentUser(), fileName, contentVersionDataSummary);
		}
		record.set(contentMetadata, copiedContent);
	}

	private RMSchemasRecordsServices rmSchemas() {
		return new RMSchemasRecordsServices(collection, modelLayerFactory);
	}

	public void customFieldValueChanged(CustomDocumentField<?> customField) {
		adjustTypeField(customField);
		adjustContentField(customField);
		adjustFolderField(customField);
	}

	void adjustTypeField(CustomDocumentField<?> valueChangeField) {
		String currentSchemaCode = getSchemaCode();
		DocumentTypeField documentTypeField = getTypeField();
		DocumentContentField contentField = getContentField();
		String recordIdForDocumentType = documentTypeField.getFieldValue();
		if (valueChangeField instanceof DocumentTypeField) {
			// Ensure that we don't change the schema for the record
			if (!isAddView()) {
				if (StringUtils.isNotBlank(recordIdForDocumentType)) {
					String schemaCodeForDocumentTypeRecordId = rmSchemasRecordsServices
							.getSchemaCodeForDocumentTypeRecordId(recordIdForDocumentType);
					if (schemaCodeForDocumentTypeRecordId == null) {
						schemaCodeForDocumentTypeRecordId = Document.DEFAULT_SCHEMA;
					}
				}
			}
			if (isReloadRequiredAfterDocumentTypeChange()) {
				reloadFormAfterDocumentTypeChange();
			}
		} else if (valueChangeField instanceof DocumentContentField) {
			ContentVersionVO contentVersionVO = contentField.getFieldValue();
			if (contentVersionVO != null && isAddView()) {
				String fileName = contentVersionVO.getFileName();
				if (rmSchemasRecordsServices.isEmail(fileName)) {
					String recordIdForEmailSchema = rmSchemasRecordsServices.getRecordIdForEmailSchema();
					if (!recordIdForEmailSchema.equals(recordIdForDocumentType)) {
						documentTypeField.setFieldValue(recordIdForEmailSchema);
						contentField.setVisible(false);
						documentTypeField.setVisible(false);
						reloadFormAfterDocumentTypeChange();
					}
				}
			}
		}
	}

	void adjustContentField(CustomDocumentField<?> valueChangeField) {
		if (isAddView() && valueChangeField instanceof DocumentContentField) {
			DocumentContentField contentField = getContentField();
			boolean newFileButtonVisible = contentField.getFieldValue() == null;
			contentField.setNewFileButtonVisible(newFileButtonVisible);
			if (newFileButtonVisible) {
				newFile = false;
			}
			contentField.setMajorVersionFieldVisible(!newFile);
		}
	}

	void adjustFolderField(CustomDocumentField<?> valueChangeField) {
		if (valueChangeField instanceof DocumentFolderField) {
			String folderId = (String) view.getForm().getCustomField(Document.FOLDER).getFieldValue();
			documentVO.setFolder(folderId);
			if (areDocumentRetentionRulesEnabled()) {
				Document record = rmSchemas().wrapDocument(toRecord(documentVO));
				recordServices().recalculate(record);
				documentVO.set(Document.APPLICABLE_COPY_RULES, record.getApplicableCopyRules());
			}
			List<String> ignoredMetadataCodes = Arrays.asList(Document.FOLDER);
			reloadFormAndPopulateCurrentMetadatasExcept(ignoredMetadataCodes);
			view.getForm().getCustomField(Document.FOLDER).focus();
		}
	}

	boolean isReloadRequiredAfterDocumentTypeChange() {
		boolean reload;
		String currentSchemaCode = getSchemaCode();
		String documentTypeRecordId = (String) view.getForm().getCustomField(Document.TYPE).getFieldValue();
		if (StringUtils.isNotBlank(documentTypeRecordId)) {
			if (!rmSchemasRecordsServices.getDocumentType(documentTypeRecordId).getTemplates().isEmpty()) {
				reload = true;
			} else {
				String schemaCodeForDocumentTypeRecordId = rmSchemasRecordsServices
						.getSchemaCodeForDocumentTypeRecordId(documentTypeRecordId);
				if (schemaCodeForDocumentTypeRecordId != null) {
					reload = !currentSchemaCode.equals(schemaCodeForDocumentTypeRecordId);
				} else {
					reload = !currentSchemaCode.equals(Document.DEFAULT_SCHEMA);
				}
			}
		} else {
			reload = !currentSchemaCode.equals(Document.DEFAULT_SCHEMA);
		}
		return reload;
	}

	void reloadFormAfterDocumentTypeChange() {
		String documentTypeId = (String) view.getForm().getCustomField(Document.TYPE).getFieldValue();

		String newSchemaCode;
		if (documentTypeId != null) {
			newSchemaCode = rmSchemasRecordsServices.getSchemaCodeForDocumentTypeRecordId(documentTypeId);
		} else {
			newSchemaCode = Document.DEFAULT_SCHEMA;
		}
		if (newSchemaCode == null) {
			newSchemaCode = Document.DEFAULT_SCHEMA;
		}

		Record documentRecord = toRecord(documentVO);
		Document document = new Document(documentRecord, types());

		setSchemaCode(newSchemaCode);
		document.changeSchemaTo(newSchemaCode);
		MetadataSchema newSchema = document.getSchema();

		view.getForm().commit();

		for (MetadataVO metadataVO : documentVO.getMetadatas()) {
			String metadataCode = metadataVO.getCode();
			String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
			try {
				Metadata matchingMetadata = newSchema.getMetadata(metadataCodeWithoutPrefix);
				if (matchingMetadata.getDataEntry().getType() == DataEntryType.MANUAL) {
					Object metadataValue = documentVO.get(metadataVO);
					Object defaultValue = metadataVO.getDefaultValue();
					if (metadataValue instanceof ContentVersionVO) {
						// Special case dealt with later
						metadataValue = null;
						document.getWrappedRecord().set(matchingMetadata, metadataValue);
					} else if (metadataValue == null || !metadataValue.equals(defaultValue)) {
						document.getWrappedRecord().set(matchingMetadata, metadataValue);
					}
				}
			} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
				// Ignore
			}
		}

		ContentVersionVO contentVersionVO = (ContentVersionVO) view.getForm().getCustomField(Document.CONTENT).getFieldValue();
		documentVO = voBuilder.build(document.getWrappedRecord(), VIEW_MODE.FORM, view.getSessionContext());
		documentVO.setContent(contentVersionVO);

		view.setRecord(documentVO);
		view.getForm().reload();
		addContentFieldListeners();
	}

	void reloadFormAndPopulateCurrentMetadatasExcept(List<String> ignoredMetadataCodes) {
		//		List<String> ignoredMetadataCodes = Arrays.asList(Document.FOLDER);
		// Populate new record with previous record's metadata values

		view.getForm().commit();

		for (MetadataVO metadataVO : documentVO.getMetadatas()) {
			String metadataCode = metadataVO.getCode();
			String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
			if (!ignoredMetadataCodes.contains(metadataCodeWithoutPrefix)) {
				try {
					MetadataVO matchingMetadata = documentVO.getMetadata(metadataCodeWithoutPrefix);
					Object metadataValue = documentVO.get(metadataVO);
					if (metadataValue instanceof ContentVersionVO) {
						// Special case dealt with later
						metadataValue = null;
					}
					documentVO.set(matchingMetadata, metadataValue);
					//				}
				} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
					// Ignore
				}
			}
		}

		ContentVersionVO contentVersionVO = (ContentVersionVO) view.getForm().getCustomField(Document.CONTENT)
				.getFieldValue();
		documentVO.setContent(contentVersionVO);

		view.setRecord(documentVO);
		view.getForm().reload();
		addContentFieldListeners();
	}

	private DocumentTypeField getTypeField() {
		return (DocumentTypeField) view.getForm().getCustomField(Document.TYPE);
	}

	private DocumentContentField getContentField() {
		return (DocumentContentField) view.getForm().getCustomField(Document.CONTENT);
	}

	private DocumentCopyRuleField getCopyRuleField() {
		return (DocumentCopyRuleField) view.getForm().getCustomField(Document.MAIN_COPY_RULE_ID_ENTERED);
	}

	public void viewAssembled() {
		addContentFieldListeners();
	}

	private void addContentFieldListeners() {
		final DocumentContentField contentField = getContentField();

		contentField.addContentUploadedListener(new ContentUploadedListener() {
			@Override
			public void newContentUploaded() {
				ContentVersionVO contentVersionVO = contentField.getFieldValue();
				if (contentVersionVO != null) {
					view.getForm().commit();
					contentVersionVO.setMajorVersion(true);
					Record documentRecord = toRecord(documentVO);
					Document document = new Document(documentRecord, types());
					Content content = toContent(contentVersionVO);
					document.setContent(content);
					modelLayerFactory.newRecordPopulateServices().populate(documentRecord);
					documentVO = voBuilder.build(documentRecord, VIEW_MODE.FORM, view.getSessionContext());
					documentVO.getContent().setMajorVersion(null);
					documentVO.getContent().setHash(null);
					view.setRecord(documentVO);
					view.getForm().reload();
				}
			}
		});

		boolean newFileButtonVisible = isAddView() && documentVO.getContent() == null;
		contentField.setNewFileButtonVisible(newFileButtonVisible);
		contentField.addNewFileClickListener(new NewFileClickListener() {
			@Override
			public void newFileClicked() {
				String documentTypeRecordId = documentVO.getType();
				contentField.getNewFileWindow().setDocumentTypeId(documentTypeRecordId);
				contentField.getNewFileWindow().open();
			}
		});
		contentField.setMajorVersionFieldVisible(!newFile);
		contentField.getNewFileWindow().addNewFileCreatedListener(new NewFileCreatedListener() {
			@Override
			public void newFileCreated(Content content) {
				view.getForm().commit();
				contentField.setNewFileButtonVisible(false);
				contentField.setMajorVersionFieldVisible(false);
				contentField.getNewFileWindow().close();
				ContentVersionVO contentVersionVO = contentVersionToVOBuilder.build(content);
				contentVersionVO.setMajorVersion(false);
				contentVersionVO.setHash(null);
				documentVO.setContent(contentVersionVO);
				documentVO.setTitle(contentVersionVO.getFileName());
				newFile = true;
				view.getForm().reload();
				// Will have been lost after reloading the form
				addContentFieldListeners();
			}
		});

		getCopyRuleField().setVisible(
				areDocumentRetentionRulesEnabled() && documentVO.getList(Document.APPLICABLE_COPY_RULES).size() > 1);
	}

	private boolean canSaveDocument(Document document, User user) {
		if (!addView) {
			return true;
		}

		Folder folder = rmSchemas().getFolder(document.getFolder());
		switch (folder.getPermissionStatus()) {
		case ACTIVE:
			return user.has(RMPermissionsTo.CREATE_DOCUMENTS).on(folder);
		case SEMI_ACTIVE:
			return user.has(RMPermissionsTo.CREATE_SEMIACTIVE_DOCUMENT).on(folder);
		default:
			return user.has(RMPermissionsTo.CREATE_INACTIVE_DOCUMENT).on(folder);
		}
	}

	private boolean areDocumentRetentionRulesEnabled() {
		return new RMConfigs(modelLayerFactory.getSystemConfigurationsManager()).areDocumentRetentionRulesEnabled();
	}
}
