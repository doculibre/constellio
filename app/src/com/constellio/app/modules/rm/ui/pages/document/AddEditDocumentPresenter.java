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
package com.constellio.app.modules.rm.ui.pages.document;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.components.document.fields.CustomDocumentField;
import com.constellio.app.modules.rm.ui.components.document.fields.DocumentContentField;
import com.constellio.app.modules.rm.ui.components.document.fields.DocumentTypeField;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.RMObject;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.ContentVersionVO.InputStreamProvider;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.ContentVersionToVOBuilder;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;

public class AddEditDocumentPresenter extends SingleSchemaBasePresenter<AddEditDocumentView> {

	private DocumentToVOBuilder voBuilder = new DocumentToVOBuilder();

	private ContentVersionToVOBuilder contentVersionToVOBuilder = new ContentVersionToVOBuilder();

	private boolean addView;

	private DocumentVO documentVO;
	
	private String userDocumentId;

	private SchemaPresenterUtils userDocumentPresenterUtils;

	private transient RMSchemasRecordsServices rmSchemasRecordsServices;

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
	}

	public void forParams(String params) {
		Map<String, String> paramsMap = ParamUtils.getParamsMap(params);
		String id = paramsMap.get("id");
		String parentId = paramsMap.get("parentId");
		userDocumentId = paramsMap.get("userDocumentId");

		Document document;
		if (StringUtils.isNotBlank(id)) {
			document = rmSchemasRecordsServices.getDocument(id);
			addView = false;
		} else {
			document = rmSchemasRecordsServices.newDocument();
			addView = true;
		}

		documentVO = voBuilder.build(document.getWrappedRecord(), VIEW_MODE.FORM);
		if (parentId != null) {
			documentVO.set(Document.FOLDER, parentId);
		}

		if (addView && userDocumentId != null) {
			populateFromUserDocument(userDocumentId);
		}
		String currentSchemaCode = documentVO.getSchema().getCode();
		setSchemaCode(currentSchemaCode);
		view.setRecord(documentVO);
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
			}

			if (status != null && status.isInactive()) {
				requiredPermissions.add(RMPermissionsTo.CREATE_INACTIVE_DOCUMENT);
			}

			return user.hasAll(requiredPermissions).on(restrictedRMObject) && user.hasWriteAccess().on(restrictedRMObject);
		} else {
			List<String> requiredPermissions = new ArrayList<>();
			FolderStatus status = restrictedRMObject.getArchivisticStatus();
			if (status != null && status.isSemiActive()) {
				requiredPermissions.add(RMPermissionsTo.MODIFY_SEMIACTIVE_DOCUMENT);
			}

			if (status != null && status.isInactive()) {
				requiredPermissions.add(RMPermissionsTo.MODIFY_INACTIVE_DOCUMENT);
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
		// Reset as new content
		contentVersionVO.setHash(null);
		contentVersionVO.setVersion(null);
		documentVO.setContent(contentVersionVO);
		documentVO.setTitle(userDocument.getContent().getCurrentVersion().getFilename());
	}

	public boolean isAddView() {
		return addView;
	}

	public void cancelButtonClicked() {
		if (addView) {
			String parentId = documentVO.getFolder();
			if (parentId != null) {
				view.navigateTo().displayFolder(parentId);
			} else {
				view.navigateTo().recordsManagement();
			}
		} else {
			view.navigateTo().displayDocument(documentVO.getId());
		}
	}

	public void saveButtonClicked() {
		Record record = toRecord(documentVO);
		addOrUpdate(record);
		if (userDocumentId != null) {
			Record userDocumentRecord = userDocumentPresenterUtils.getRecord(userDocumentId);
			userDocumentPresenterUtils.delete(userDocumentRecord, null);
		}
		view.navigateTo().displayDocument(record.getId());
	}

	private RMSchemasRecordsServices rmSchemas() {
		return new RMSchemasRecordsServices(collection, modelLayerFactory);
	}

	public void customFieldValueChanged(CustomDocumentField<?> customField) {
		adjustTypeField(customField);
	}

	void adjustTypeField(CustomDocumentField<?> valueChangeField) {
		String currentSchemaCode = getSchemaCode();
		CustomDocumentField<?> documentTypeField = view.getForm().getCustomField(Document.TYPE);
		CustomDocumentField<?> contentField = view.getForm().getCustomField(Document.CONTENT);
		String recordIdForDocumentType = (String) documentTypeField.getFieldValue();
		if (valueChangeField instanceof DocumentTypeField) {
			// Ensure that we don't change the schema for the record
			if (!isAddView()) {
				if (StringUtils.isNotBlank(recordIdForDocumentType)) {
					String schemaCodeForDocumentTypeRecordId = rmSchemasRecordsServices.getSchemaCodeForDocumentTypeRecordId(recordIdForDocumentType);
					if (schemaCodeForDocumentTypeRecordId == null) {
						schemaCodeForDocumentTypeRecordId = Document.DEFAULT_SCHEMA;
					}
					if (!currentSchemaCode.equals(schemaCodeForDocumentTypeRecordId)) {
						view.showErrorMessage($("AddEditDocumentView.cannotSelectDocumentType"));
						// Set initial value
						documentTypeField.setFieldValue(documentVO.getType());
					}	
				}	
			}
			if (isReloadRequiredAfterDocumentTypeChange()) {
				reloadFormAfterDocumentTypeChange();
			}
		} else if (valueChangeField instanceof DocumentContentField) {
			ContentVersionVO contentVersionVO = (ContentVersionVO) contentField.getFieldValue();
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

	boolean isReloadRequiredAfterDocumentTypeChange() {
		boolean reload;
		if (addView) {
			String currentSchemaCode = getSchemaCode();
			String documentTypeRecordId = (String) view.getForm().getCustomField(Document.TYPE).getFieldValue();
			if (StringUtils.isNotBlank(documentTypeRecordId)) {
				String schemaCodeForDocumentTypeRecordId = rmSchemasRecordsServices.getSchemaCodeForDocumentTypeRecordId(documentTypeRecordId);
				if (schemaCodeForDocumentTypeRecordId != null) {
					reload = !currentSchemaCode.equals(schemaCodeForDocumentTypeRecordId);
				} else if (!currentSchemaCode.equals(Document.DEFAULT_SCHEMA)) {	
					reload = true;
				} else {
					reload = false;
				}
			} else {
				reload = !currentSchemaCode.equals(Document.DEFAULT_SCHEMA);
			}
		} else {
			reload = false;
		}
		return reload;
	}

	void reloadFormAfterDocumentTypeChange() {
		String documentTypeId = (String) view.getForm().getCustomField(Document.TYPE).getFieldValue();
		
		Document document;
		if (documentTypeId != null) {
			String schemaCodeFormDocumentTypeId = rmSchemasRecordsServices.getSchemaCodeForDocumentTypeRecordId(documentTypeId);
			if (Email.SCHEMA.equals(schemaCodeFormDocumentTypeId)) {
				ContentVersionVO documentContent = (ContentVersionVO) view.getForm().getCustomField(Document.CONTENT).getFieldValue();
				if (documentContent != null) {
					String fileName = documentContent.getFileName();
					if (rmSchemasRecordsServices.isEmail(fileName)) {
						InputStreamProvider inputStreamProvider = documentContent.getInputStreamProvider();
						InputStream in = inputStreamProvider.getInputStream(AddEditDocumentPresenter.class + ".reloadFormAfterDocumentTypeChange");
						document = rmSchemasRecordsServices.newEmail(fileName, in);
					} else {
						document = rmSchemasRecordsServices.newEmail();
					}
				} else {
					document = rmSchemasRecordsServices.newEmail();
				}
			} else {
				document = rmSchemasRecordsServices.newDocumentWithType(documentTypeId);
			}
		} else {
			document = rmSchemasRecordsServices.newDocument();
		}
		
		MetadataSchema documentSchema = document.getSchema();
		String currentSchemaCode = documentSchema.getCode();
		setSchemaCode(currentSchemaCode);
		
		List<String> ignoredMetadataCodes = Arrays.asList(Document.TYPE);
		// Populate new record with previous record's metadata values
		
		view.getForm().commit();
		
		for (MetadataVO metadataVO : documentVO.getMetadatas()) {
			String metadataCode = metadataVO.getCode();
			String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
			if (!ignoredMetadataCodes.contains(metadataCodeWithoutPrefix)) {
				try {
					Metadata matchingMetadata = documentSchema.getMetadata(metadataCodeWithoutPrefix);
					Object metadataValue = documentVO.get(metadataVO);
					if (metadataValue instanceof ContentVersionVO) {
						// Special case dealt with later
						metadataValue = null;
					}
					document.getWrappedRecord().set(matchingMetadata, metadataValue);
				} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
					// Ignore
				}
			}
		}

		ContentVersionVO contentVersionVO = (ContentVersionVO) view.getForm().getCustomField(Document.CONTENT).getFieldValue();
		documentVO = voBuilder.build(document.getWrappedRecord(), VIEW_MODE.FORM);
		documentVO.setContent(contentVersionVO);
		
		view.setRecord(documentVO);
	 	view.getForm().reload();
	}
	
}
