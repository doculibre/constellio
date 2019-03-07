package com.constellio.app.modules.rm.services.sip;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.sip.record.RecordPathProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.schemas.Schemas;
import org.joda.time.LocalDateTime;

import java.util.List;

class RMZipPathProvider implements RecordPathProvider {

	private AppLayerFactory appLayerFactory;

	public RMZipPathProvider(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public String getPath(Record record) {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(record.getCollection(), appLayerFactory);

		String parent = null;
		String pathIdentifier = record.getId();
		boolean addSchemaTypeInPath = true;

		if (Task.SCHEMA_TYPE.equals(record.getTypeCode()) || Event.SCHEMA_TYPE.equals(record.getTypeCode())) {
			LocalDateTime createdOn = record.get(Schemas.CREATED_ON);
			int year = createdOn == null ? 1900 : createdOn.getYear();
			int month = createdOn == null ? 1 : createdOn.getMonthOfYear();
			int day = createdOn == null ? 1 : createdOn.getDayOfMonth();

			return "/data/_" + year + "/_" + year + "-" + month + "/_" + year + "-" + month + "-" + day + "/"
				   + record.getTypeCode() + "-" + record.getId();


		} else if (Authorization.SCHEMA_TYPE.equals(record.getTypeCode())) {
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

		} else if (ContainerRecord.SCHEMA_TYPE.equals(record.getTypeCode())) {
			Object storageSpaceObject = rm.wrapContainerRecord(record).get(ContainerRecord.STORAGE_SPACE);

			if (storageSpaceObject == null) {
				parent = null;
			} else if (storageSpaceObject instanceof List) {
				List storageSpaceList = (List) storageSpaceObject;

				if (storageSpaceList.size() == 0) {
					parent = null;
				} else {
					parent = (String) storageSpaceList.get(0);
				}
			} else {
				parent = rm.wrapContainerRecord(record).getStorageSpace();
			}
		} else if (StorageSpace.SCHEMA_TYPE.equals(StorageSpace.SCHEMA_TYPE)) {
			parent = rm.wrapStorageSpace(record).getParentStorageSpace();
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
