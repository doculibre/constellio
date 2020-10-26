package com.constellio.app.modules.rm.ui.pages.document;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.CopyRetentionRuleInRule;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.EmailParsingServices;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
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
import com.constellio.app.modules.rm.ui.pages.extrabehavior.SecurityWithNoUrlParamSupport;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.util.RMNavigationUtils;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMObject;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.ContentVersionToVOBuilder;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.contents.ContentFactory;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentManager.UploadOptions;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.contents.icap.IcapException;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.users.UserServices;
import com.vaadin.ui.Field;
import com.vaadin.ui.Layout;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class AddEditDocumentPresenter extends SingleSchemaBasePresenter<AddEditDocumentView> implements SecurityWithNoUrlParamSupport {

	private transient ContentVersionToVOBuilder contentVersionToVOBuilder;

	private DocumentToVOBuilder voBuilder;
	private boolean addView;
	private boolean addViewWithCopy;
	private String id;
	protected DocumentVO documentVO;
	private String userDocumentId;
	private SchemaPresenterUtils userDocumentPresenterUtils;
	private transient RMSchemasRecordsServices rmSchemasRecordsServices;
	private boolean newFile;
	private boolean newFileAtStart;
	ConstellioEIMConfigs eimConfigs;
	private Map<String, String> params;
	private Document documentOriginalCopy = null;
	private boolean isFromUserDocument;
	private String copyId = null;

	public AddEditDocumentPresenter(AddEditDocumentView view, RecordVO recordVO) {
		super(view, Document.DEFAULT_SCHEMA);
		initTransientObjects();
		eimConfigs = modelLayerFactory.getSystemConfigs();
		this.id = recordVO != null ? recordVO.getId() : null;
		addView = recordVO == null;
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
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
		userDocumentPresenterUtils = new SchemaPresenterUtils(UserDocument.DEFAULT_SCHEMA, constellioFactories, sessionContext);
		contentVersionToVOBuilder = new ContentVersionToVOBuilder(modelLayerFactory);
		voBuilder = new DocumentToVOBuilder(modelLayerFactory);
	}

	public void forParams(String params) {
		String idCopy;
		String parentId;

		if (params != null) {
			Map<String, String> paramsMap = ParamUtils.getParamsMap(params);
			id = paramsMap.get("id");
			idCopy = paramsMap.get("idCopy");
			parentId = paramsMap.get("parentId");
			userDocumentId = paramsMap.get("userDocumentId");
			this.params = paramsMap;
			newFile = false;
			newFileAtStart = "true".equals(paramsMap.get("newFile"));
		} else {
			idCopy = null;
			parentId = null;
			userDocumentId = null;
			newFile = false;
			newFileAtStart = false;
		}

		Document document;
		if (StringUtils.isNotBlank(id)) {
			document = rmSchemasRecordsServices.getDocument(id);
			addView = false;
		} else {
			document = newDocument();
			if (StringUtils.isNotBlank(idCopy)) {
				addViewWithCopy = true;
			}
			addView = true;
		}
		this.isFromUserDocument = false;
		this.copyId = null;
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
		documentOriginalCopy = document;
		DecommissioningService decommissioningService = new DecommissioningService(collection, appLayerFactory);
		Document duplicatedDocument = decommissioningService.createDuplicateOfDocument(document, getCurrentUser());

		documentVO = voBuilder.build(duplicatedDocument.getWrappedRecord(), VIEW_MODE.FORM, view.getSessionContext());

		Content content = document.getContent();
		if (content != null) {
			ContentVersion contentVersion = content.getCurrentVersion();
			ContentVersionVO contentVersionVO = contentVersionToVOBuilder.build(content, contentVersion);
			contentVersionVO.setMajorVersion(contentVersion.isMajor());
			contentVersionVO.setVersion(contentVersion.getVersion());
			documentVO.setContent(contentVersionVO);
		}

		documentVO.setTitle(document.getTitle() + " (" + $("AddEditDocumentViewImpl.copy") + ")");
		documentVO.setFolder(document.getFolder());
		copyId = existingDocumentId;
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
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		LogicalSearchQuery duplicateDocumentsQuery = new LogicalSearchQuery()
				.setCondition(LogicalSearchQueryOperators.from(rm.documentSchemaType())
						.where(rm.document.content()).is(ContentFactory.isHash(contentVersionVO.getHash())))
				.filteredWithUser(getCurrentUser());
		List<Document> duplicateDocuments = rm.searchDocuments(duplicateDocumentsQuery);
		if (duplicateDocuments.size() > 0) {
			StringBuilder message = new StringBuilder($("ContentManager.hasFoundDuplicateWithConfirmation"));
			message.append("<br>");
			for (Document document : duplicateDocuments) {
				message.append("<br>-");
				message.append(document.getTitle());
				message.append(": ");
				message.append(generateDisplayLink(document));
			}
			view.showMessage(message.toString());
		}

		// Reset as new content
		contentVersionVO.setHash(null);
		contentVersionVO.setVersion(null);

		String fileName = contentVersion.getFilename();
		String extension = StringUtils.lowerCase(FilenameUtils.getExtension(fileName));
		if ("eml".equals(extension) || "msg".equals(extension)) {
			InputStream messageInputStream = contentVersionVO.getInputStreamProvider().getInputStream("populateFromUserDocument");
			Email email = new EmailParsingServices(rmSchemasRecordsServices).newEmail(fileName, messageInputStream);
			documentVO = voBuilder.build(email.getWrappedRecord(), VIEW_MODE.FORM, view.getSessionContext());
			contentVersionVO.setMajorVersion(true);
		} else {
			contentVersionVO.setMajorVersion(true);
		}
		if (StringUtils.isNotBlank(folderId)) {
			documentVO.setFolder(folderId);
		}
		if (StringUtils.isBlank(documentVO.getTitle())) {
			documentVO.setTitle(userDocument.getTitle());
		}
		documentVO.setContent(contentVersionVO);

		isFromUserDocument = true;
	}

	public boolean isAddView() {
		return addView;
	}

	public boolean isNewFileAtStart() {
		return newFileAtStart;
	}

	public void cancelButtonClicked() {
		if (view.isInWindow()) {
			view.closeAllWindows();
		} else if (userDocumentId != null) {
			view.navigate().to(RMViews.class).listUserDocuments();
		} else if (addViewWithCopy) {
			navigateToDocumentDisplay(documentOriginalCopy.getId());
		} else if (addView) {
			String parentId = documentVO.getFolder();
			if (parentId != null) {
				navigateToFolderDisplay(parentId);
			} else if (userDocumentId != null) {
				view.navigate().to(RMViews.class).listUserDocuments();
			} else {
				view.navigate().to().home();
			}
		} else {
			view.navigate().to().previousView();
		}
	}

	private void navigateToDocumentDisplay(String id) {
		RMNavigationUtils.navigateToDisplayDocument(id, params, appLayerFactory, view.getCollection());

	}

	private void navigateToFolderDisplay(String id) {
		RMNavigationUtils.navigateToDisplayFolder(id, params, appLayerFactory, view.getCollection());
	}

	private void setAsNewVersionOfContent(Document document) {
		Document documentBeforeChange = rmSchemasRecordsServices.getDocument(document.getId());
		Content contentBeforeChange = documentBeforeChange.getContent();
		ContentVersionVO contentVersionVO = documentVO.getContent();
		String filename = contentVersionVO.getFileName();
		InputStream in = contentVersionVO.getInputStreamProvider().getInputStream("AddEditDocumentPresenter.saveButtonClicked");
		boolean majorVersion = Boolean.TRUE.equals(contentVersionVO.isMajorVersion());

		ContentVersionDataSummary contentVersionSummary;
		try {
			UploadOptions options = new UploadOptions().setFileName(filename);
			ContentManager.ContentVersionDataSummaryResponse uploadResponse = uploadContent(in, options);
			contentVersionSummary = uploadResponse.getContentVersionDataSummary();
			contentBeforeChange.updateContentWithName(getCurrentUser(), contentVersionSummary, majorVersion, filename);
			document.setContent(contentBeforeChange);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	public RecordVO getUserDocumentRecordVO() {
		if (isFromUserDocument && userDocumentId != null) {
			UserDocument userDocument = rmSchemasRecordsServices.getUserDocument(userDocumentId);
			return voBuilder.build(userDocument.getWrappedRecord(), VIEW_MODE.FORM, view.getSessionContext());
		} else {
			return null;
		}
	}

	public RecordVO getDuplicateDocumentRecordVO() {
		if (copyId != null) {
			Document document = rmSchemasRecordsServices.getDocument(copyId);
			return voBuilder.build(document.getWrappedRecord(), VIEW_MODE.FORM, view.getSessionContext());
		} else {
			return null;
		}
	}

	public void saveButtonClicked() {
		Record record;
		Document document;

		try {
			//TODO should throw message if duplicate is found
			record = toRecord(documentVO, newFile);
			documentVO.setRecord(record);
			document = rmSchemas().wrapDocument(record);

			boolean editWithUserDocument = !addView && userDocumentId != null;
			if (editWithUserDocument) {
				setAsNewVersionOfContent(document);
			}
		} catch (final IcapException e) {
			view.showErrorMessage(e.getMessage());
			return;
		}

		if (!canSaveDocument(document, getCurrentUser())) {
			view.showMessage($("AddEditDocumentView.noPermissionToSaveDocument"));
			return;
		}

		Content content = document.getContent();
		if (content != null && Email.SCHEMA.equals(document.getSchema().getCode()) && !rmSchemas().isEmail(content.getCurrentVersion().getFilename())) {
			view.showErrorMessage($("Document.onlyMsgAndEmlDocumentAreAccepted"));
			return;
		}

		if (addViewWithCopy) {
			setRecordContent(record, documentVO);
		}
		RMConfigs rmConfigs = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		if (newFile && rmConfigs.areDocumentCheckedOutAfterCreation()) {
			content.checkOut(getCurrentUser());
		}
		LocalDateTime time = TimeProvider.getLocalDateTime();
		if (isAddView()) {
			document.setFormCreatedBy(getCurrentUser()).setFormCreatedOn(time);
		}
		document.setFormModifiedBy(getCurrentUser()).setFormModifiedOn(time);

		if (documentVO.getContent() != null) {
			String currentTitle = document.getTitle();
			String currentContentFilename = documentVO.getContent().getFileName();
			String extension = StringUtils.lowerCase(FilenameUtils.getExtension(currentContentFilename));
			if (currentTitle.endsWith("." + extension) &&
				(isAddView() || !document.getSchema().getMetadata(Schemas.TITLE_CODE).getPopulateConfigs().isAddOnly())) {
				content.renameCurrentVersion(currentTitle);
			}
		}
		addOrUpdate(record, RecordsFlushing.WITHIN_SECONDS(modelLayerFactory.getSystemConfigs().getTransactionDelay()));
		if (userDocumentId != null) {
			UserServices userServices = modelLayerFactory.newUserServices();
			Record userDocumentRecord = userDocumentPresenterUtils.getRecord(userDocumentId);
			String userDocumentCollection = userDocumentRecord.getCollection();
			User currentUser = getCurrentUser();
			User userDocumentUser = userServices.getUserInCollection(currentUser.getUsername(), userDocumentCollection);
			userDocumentPresenterUtils.delete(userDocumentRecord, null, userDocumentUser);
		}

		if (view.isInWindow()) {
			view.closeAllWindows();
		} else {
			navigateToDocumentDisplay(record.getId());
		}
		if (newFile && rmConfigs.areDocumentCheckedOutAfterCreation()) {
			String agentURL = ConstellioAgentUtils.getAgentURL(documentVO, documentVO.getContent());
			if (agentURL != null) {
				view.openAgentURL(agentURL);
			}
		}
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
		if (contentVO != null) {
			Boolean majorVersion = contentVO.isMajorVersion();
			String fileName = contentVO.getFileName();
			String hash = contentVO.getHash();
			ContentVersionDataSummary contentVersionDataSummary = contentManager.getContentVersionSummary(hash)
					.getContentVersionDataSummary();
			Content copiedContent;
			if (majorVersion != null && majorVersion) {
				copiedContent = contentManager.createMajor(getCurrentUser(), fileName, contentVersionDataSummary);
			} else {
				copiedContent = contentManager.createMinor(getCurrentUser(), fileName, contentVersionDataSummary);
			}
			record.set(contentMetadata, copiedContent);
		}
	}

	private RMSchemasRecordsServices rmSchemas() {
		return new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	public void customFieldValueChanged(CustomDocumentField<?> customField) {
		adjustTypeField(customField);
		adjustContentField(customField);
		adjustFolderField(customField);
	}

	void adjustTypeField(CustomDocumentField<?> valueChangeField) {
		String currentSchemaCode = getSchemaCode();
		DocumentTypeField documentTypeField = getTypeField();
		if (documentTypeField == null) {
			return;
		}
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
						contentVersionVO.setMajorVersion(true);
						setVisible(contentField, false);
						setVisible(documentTypeField, false);
						reloadFormAfterDocumentTypeChange();
					}
				}
			}
		}
	}

	private void setVisible(CustomDocumentField field, boolean isVisible) {
		field.setVisible(isVisible);
		if (field instanceof Field<?>) {
			Layout fieldLayout = view.getForm().getFieldLayout((Field<?>) field);
			if (fieldLayout != null) {
				fieldLayout.setVisible(isVisible);
			}
		}
	}

	void adjustContentField(CustomDocumentField<?> valueChangeField) {
		if (isAddView() && valueChangeField instanceof DocumentContentField) {
			DocumentContentField contentField = getContentField();
			ContentVersionVO contentVersionVO = contentField.getFieldValue();
			if (contentVersionVO != null && Boolean.TRUE.equals(contentVersionVO.hasFoundDuplicate())) {
				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
				LogicalSearchQuery duplicateDocumentsQuery = new LogicalSearchQuery()
						.setCondition(LogicalSearchQueryOperators.from(rm.documentSchemaType())
								.where(rm.document.content()).is(ContentFactory.isHash(contentVersionVO.getDuplicatedHash()))
								.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull()
								.andWhere(Schemas.IDENTIFIER).isNotEqual(documentVO.getId()))
						.filteredWithUser(getCurrentUser());
				List<Document> duplicateDocuments = rm.searchDocuments(duplicateDocumentsQuery);
				if (duplicateDocuments.size() > 0) {
					StringBuilder message = new StringBuilder($("ContentManager.hasFoundDuplicateWithConfirmation", StringUtils.defaultIfBlank(contentVersionVO.getFileName(), "")));
					message.append("<br>");
					for (Document document : duplicateDocuments) {
						message.append("<br>-");
						message.append(document.getTitle());
						message.append(": ");
						message.append(generateDisplayLink(document));
					}
					view.showClickableMessage(message.toString());
				}
			}
			boolean newFileButtonVisible = contentVersionVO == null;
			contentField.setNewFileButtonVisible(newFileButtonVisible);
			if (newFileButtonVisible) {
				newFile = false;
			}
			contentField.setMajorVersionFieldVisible(!newFile);

			if (newFileAtStart) {
				contentField.setReadOnly(true);
			}
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
			List<String> ignoredMetadataCodes = asList(Document.FOLDER);
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

	public void reloadFormAfterDocumentTypeChange() {
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

		view.getForm().commit();
		Record documentRecord = toRecord(documentVO);
		Document document = new Document(documentRecord, types());

		setSchemaCode(newSchemaCode);
		document.changeSchemaTo(newSchemaCode);
		MetadataSchema newSchema = document.getSchema();

		recordServices().recalculate(document);

		for (MetadataVO metadataVO : documentVO.getMetadatas()) {
			String metadataCode = metadataVO.getCode();
			String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
			try {
				Metadata matchingMetadata = newSchema.getMetadata(metadataCodeWithoutPrefix);
				if (matchingMetadata.getDataEntry().getType() == DataEntryType.MANUAL && !matchingMetadata.isSystemReserved()) {
					Object voMetadataValue = documentVO.get(metadataVO);
					Object defaultValue = matchingMetadata.getDefaultValue();
					Object voDefaultValue = metadataVO.getDefaultValue();
					if (voMetadataValue instanceof ContentVersionVO) {
						// Special case dealt with later
						voMetadataValue = null;
						document.getWrappedRecord().set(matchingMetadata, voMetadataValue);
					} else if (voMetadataValue == null && defaultValue == null) {
						document.getWrappedRecord().set(matchingMetadata, voMetadataValue);
					} else if ((voMetadataValue != null || (voDefaultValue instanceof List && !((List) voDefaultValue).isEmpty()))
							   && !voMetadataValue.equals(voDefaultValue)) {
						document.getWrappedRecord().set(matchingMetadata, voMetadataValue);
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

		if (view.getForm().getCustomField(Document.CONTENT) != null) {
			ContentVersionVO contentVersionVO = (ContentVersionVO) view.getForm().getCustomField(Document.CONTENT)
					.getFieldValue();
			documentVO.setContent(contentVersionVO);
		}

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
		DocumentContentField contentField = getContentField();
		if (addView && newFileAtStart && !contentField.getNewFileWindow().isOpened()) {
			contentField.getNewFileWindow().open();
		}
	}

	private void addNewFileCreatedListener() {
		final DocumentContentField contentField = getContentField();
		contentField.getNewFileWindow().addNewFileCreatedListener(new NewFileCreatedListener() {
			@Override
			public void newFileCreated(Content content, String documentTypeId) {
				view.getForm().commit();
				contentField.setNewFileButtonVisible(false);
				contentField.setMajorVersionFieldVisible(false);
				contentField.getNewFileWindow().close();
				ContentVersionVO contentVersionVO = contentVersionToVOBuilder.build(content);
				contentVersionVO.setMajorVersion(false);
				contentVersionVO.setHash(null);
				documentVO.setContent(contentVersionVO);
				String filename = contentVersionVO.getFileName();
				if (eimConfigs.isRemoveExtensionFromRecordTitle()) {
					filename = FilenameUtils.removeExtension(filename);
				}
				documentVO.setTitle(filename);
				documentVO.setType(documentTypeId);
				newFile = true;
				view.getForm().reload();

				if (documentTypeId != null) {
					reloadFormAfterDocumentTypeChange();
				}
				// Will have been lost after reloading the form
				addContentFieldListeners();
			}
		});
	}

	private void addContentFieldListeners() {
		final DocumentContentField contentField = getContentField();
		if (contentField != null) {
			contentField.addContentUploadedListener(new ContentUploadedListener() {
				@Override
				public void newContentUploaded() {
					isFromUserDocument = false;
					copyId = null;
					ContentVersionVO contentVersionVO = contentField.getFieldValue();
					if (contentVersionVO != null) {
						if (Boolean.TRUE.equals(contentVersionVO.hasFoundDuplicate())) {
							RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
							LogicalSearchQuery duplicateDocumentsQuery = new LogicalSearchQuery()
									.setCondition(LogicalSearchQueryOperators.from(rm.documentSchemaType())
											.where(rm.document.content())
											.is(ContentFactory.isHash(contentVersionVO.getDuplicatedHash()))
											.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull()
											.andWhere(Schemas.IDENTIFIER).isNotEqual(documentVO.getId()))
									.filteredWithUser(getCurrentUser());
							List<Document> duplicateDocuments = rm.searchDocuments(duplicateDocumentsQuery);
							if (duplicateDocuments.size() > 0) {
								StringBuilder message = new StringBuilder($("ContentManager.hasFoundDuplicateWithConfirmation", StringUtils.defaultIfBlank(contentVersionVO.getFileName(), "")));
								message.append("<br>");
								for (Document document : duplicateDocuments) {
									message.append("<br>-");
									message.append(document.getTitle());
									message.append(": ");
									message.append(generateDisplayLink(document));
								}
								view.showClickableMessage(message.toString());
							}
						}
						view.getForm().commit();
						contentVersionVO.setMajorVersion(true);
						Record documentRecord = toRecord(documentVO);
						Document document = new Document(documentRecord, types());
						try {
							Content content = toContent(documentVO, documentVO.getSchema().getMetadata(Document.CONTENT), contentVersionVO);
							document.setContent(content);
							String filename = contentVersionVO.getFileName();
							String extension = StringUtils.lowerCase(FilenameUtils.getExtension(filename));
							if ("eml".equals(extension) || "msg".equals(extension)) {
								IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
								InputStream inputStream = null;
								try {
									inputStream = contentVersionVO.getInputStreamProvider().getInputStream("populateFromEML");
									Email email = new EmailParsingServices(AddEditDocumentPresenter.this.rmSchemasRecordsServices)
											.newEmail(filename, inputStream);
									document = rmSchemas().wrapEmail(document.changeSchemaTo(Email.SCHEMA));

									((Email) document).setSubject(email.getSubject());
									((Email) document).setEmailObject(email.getEmailObject());
									((Email) document).setEmailSentOn(email.getEmailSentOn());
									((Email) document).setEmailReceivedOn(email.getEmailReceivedOn());
									((Email) document).setEmailFrom(email.getEmailFrom());
									((Email) document).setEmailTo(email.getEmailTo());
									((Email) document).setEmailCCTo(email.getEmailCCTo());
									((Email) document).setEmailBCCTo(email.getEmailBCCTo());
									((Email) document).setEmailAttachmentsList(email.getEmailAttachmentsList());
								} finally {
									ioServices.closeQuietly(inputStream);
								}
							}
							modelLayerFactory.newRecordPopulateServices().populate(documentRecord, documentVO.getRecord());
							documentVO = voBuilder.build(documentRecord, VIEW_MODE.FORM, view.getSessionContext());
							documentVO.getContent().setMajorVersion(null);
							documentVO.getContent().setHash(null);
							if (eimConfigs.isRemoveExtensionFromRecordTitle()) {
								filename = FilenameUtils.removeExtension(filename);
							}
							documentVO.setTitle(filename);
							view.setRecord(documentVO);
							view.getForm().reload();
							addContentFieldListeners();
						} catch (final IcapException e) {
							view.showErrorMessage(e.getMessage());

							documentVO.setContent(null);
							getContentField().setFieldValue(null);
						}
					} else {
						documentVO.setContent(null);
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
			addNewFileCreatedListener();

			DocumentCopyRuleField copyRuleField = getCopyRuleField();
			if (copyRuleField != null) {
				boolean copyRuleFieldVisible = areDocumentRetentionRulesEnabled() && documentVO.getList(Document.APPLICABLE_COPY_RULES).size() > 1;
				setVisible(copyRuleField, copyRuleFieldVisible);
				if (copyRuleFieldVisible) {
					copyRuleField.setFieldChoices(documentVO.<CopyRetentionRuleInRule>getList(Document.APPLICABLE_COPY_RULES));
				}
			}
		}
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

	Document newDocument() {
		User currentUser = getCurrentUser();
		Document document = rmSchemasRecordsServices.newDocument();
		document.setCreatedBy(currentUser.getId());
		document.setAuthor(currentUser.getFirstName() + " " + currentUser.getLastName());
		return document;
	}

	String generateDisplayLink(Document document) {
		String constellioUrl = eimConfigs.getConstellioUrl();
		String displayURL = RMNavigationConfiguration.DISPLAY_DOCUMENT;
		String url = constellioUrl + "#!" + displayURL + "/" + document.getId();
		return "<a href=\"" + url + "\">" + url + "</a>";
	}


	@Override
	public boolean hasPageAccess(User user) {
		if (addView) {
			throw new NotImplementedException("Dans le moment les fenetre supporte seulement le mode modifier");
		} else {
			return hasRestrictedRecordAccess(id, getCurrentUser(), recordServices().getDocumentById(id));
		}
	}
}
