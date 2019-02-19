package com.constellio.app.modules.rm.services.sip;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.sip.record.RecordPathProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;

class RMZipPathProvider implements RecordPathProvider {

	private RMSchemasRecordsServices rm;

	public RMZipPathProvider(RMSchemasRecordsServices rm) {
		this.rm = rm;
	}

	@Override
	public String getPath(Record record) {

		String parent = null;
		String pathIdentifier = record.getId();
		boolean addSchemaTypeInPath = true;

		if (Authorization.SCHEMA_TYPE.equals(record.getTypeCode())) {
			Authorization authorization = rm.wrapSolrAuthorizationDetails(record);
			//addSchemaTypeInPath = false;
			parent = authorization.getTarget();

		} else if (Category.SCHEMA_TYPE.equals(record.getTypeCode())) {
			Category category = rm.wrapCategory(record);
			//addSchemaTypeInPath = false;
			pathIdentifier = category.getCode();
			parent = category.getParent();

		} else if (Folder.SCHEMA_TYPE.equals(record.getTypeCode())) {
			Folder folder = rm.wrapFolder(record);
			parent = folder.getParentFolder() != null ? folder.getParentFolder() : folder.getCategory();

		} else if (Document.SCHEMA_TYPE.equals(record.getTypeCode())) {
			parent = rm.wrapDocument(record).getFolder();

		}

		StringBuilder path = new StringBuilder();
		if (parent == null) {
			path.append("/data/");
		} else {
			path.append(getPath(rm.getModelLayerFactory().newRecordServices().getDocumentById(parent))).append("/");
		}

		if (addSchemaTypeInPath) {
			path.append(record.getTypeCode()).append("-");
		}
		path.append(pathIdentifier);

		return path.toString();
	}
}
