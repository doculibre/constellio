package com.constellio.app.modules.rm.extensions.app;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordCreationEvent;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionEvent;
import com.constellio.model.extensions.events.records.RecordModificationEvent;
import com.constellio.model.extensions.events.records.RecordRestorationEvent;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Collections.singletonList;

@Slf4j
public class RMRecordMediumTypeExtension extends RecordExtension {

	private RMSchemasRecordsServices rm;
	private RecordServices recordServices;

	private MediumType digitalMediumType;

	public RMRecordMediumTypeExtension(String collection, AppLayerFactory appLayerFactory) {
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();

		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.mediumTypeSchemaType()).where(ALL));
		for (MediumType mediumType :
				rm.wrapMediumTypes(appLayerFactory.getModelLayerFactory().newSearchServices().search(query))) {
			if (!mediumType.isAnalogical()) {
				digitalMediumType = mediumType;
				break;
			}
		}
	}

	@Override
	public void recordModified(RecordModificationEvent event) {
		String schemaType = event.getSchemaTypeCode();
		if (schemaType.equals(Folder.SCHEMA_TYPE)) {
			if (event.hasModifiedMetadata(Folder.MEDIUM_TYPES) || event.hasModifiedMetadata(Folder.PARENT_FOLDER)) {
				updateParentFolderMediumType(event.getRecord(), event.getOriginalRecord());
			}
		} else if (schemaType.equals(Document.SCHEMA_TYPE)) {
			if (event.hasModifiedMetadata(Document.CONTENT) || event.hasModifiedMetadata(Document.FOLDER)) {
				updateParentFolderMediumType(event.getRecord(), event.getOriginalRecord());
			}
		}
	}

	@Override
	public void recordLogicallyDeleted(RecordLogicalDeletionEvent event) {
		String schemaType = event.getSchemaTypeCode();
		if (schemaType.equals(Folder.SCHEMA_TYPE) || schemaType.equals(Document.SCHEMA_TYPE)) {
			updateParentFolderMediumType(event.getRecord(), null);
		}
	}

	@Override
	public void recordRestored(RecordRestorationEvent event) {
		String schemaType = event.getRecord().getSchemaCode();
		if (schemaType.equals(Folder.SCHEMA_TYPE) || schemaType.equals(Document.SCHEMA_TYPE)) {
			updateParentFolderMediumType(event.getRecord(), null);
		}
	}

	@Override
	public void recordCreated(RecordCreationEvent event) {
		String schemaType = event.getSchemaTypeCode();
		if (schemaType.equals(Folder.SCHEMA_TYPE) || schemaType.equals(Document.SCHEMA_TYPE)) {
			updateParentFolderMediumType(event.getRecord(), null);
		}
	}

	private void updateParentFolderMediumType(Record record, Record originalRecord) {
		try {
			String parentFolderId = getParentFolder(record);
			if (parentFolderId == null) {
				return;
			}
			if (isMediumTypeAdded(record, originalRecord)) {
				String mediumType = getAddedMediumType(record, originalRecord);
				if (mediumType != null) {
					addMediumTypeToParentFolder(parentFolderId, mediumType);
				}
			} else if (isMediumTypeRemoved(record, originalRecord)) {
				Folder folder = rm.getFolder(parentFolderId).set(Schemas.MARKED_FOR_REINDEXING, true);
				recordServices.update(folder.getWrappedRecord());
			}
		} catch (RecordServicesException e) {
			log.error("Failed to update parent folder's medium type", e);
		}
	}

	private void addMediumTypeToParentFolder(String parentFolderId, String mediumType) throws RecordServicesException {
		Folder folder = rm.getFolder(parentFolderId);
		if (!folder.getMediumTypes().contains(mediumType)) {
			List<String> mediumTypes = new ArrayList<>(folder.getMediumTypes());
			mediumTypes.add(mediumType);
			folder.setMediumTypes(mediumTypes);

			recordServices.update(folder);
		}
	}

	private boolean isMediumTypeAdded(Record record, Record originalRecord) {
		return isFolder(record) ?
			   getCurrentMediumTypes(record).size() > getOriginalMediumTypes(originalRecord).size() :
			   isDigital(record);
	}

	private boolean isMediumTypeRemoved(Record record, Record originalRecord) {
		return isFolder(record) ?
			   getCurrentMediumTypes(record).size() < getOriginalMediumTypes(originalRecord).size() :
			   isDigital(record);
	}

	private String getAddedMediumType(Record record, Record originalRecord) {
		Set<String> mediumTypes = new HashSet<>(getCurrentMediumTypes(record));
		mediumTypes.removeAll(getOriginalMediumTypes(originalRecord));
		if (!mediumTypes.isEmpty()) {
			return mediumTypes.iterator().next();
		} else {
			return null;
		}
	}

	private String getParentFolder(Record record) {
		return isFolder(record) ?
			   record.<String>get(rm.folder.parentFolder()) : record.<String>get(rm.document.folder());
	}

	private List<String> getOriginalMediumTypes(Record record) {
		if (record == null) {
			return Collections.emptyList();
		}
		return isFolder(record) ? record.getCopyOfOriginalRecord().<List<String>>get(rm.folder.mediumTypes()) :
			   isDigital(record) ? singletonList(digitalMediumType.getId()) : Collections.<String>emptyList();
	}

	private List<String> getCurrentMediumTypes(Record record) {
		if (Boolean.TRUE.equals(record.get(Schemas.LOGICALLY_DELETED_STATUS))) {
			return Collections.emptyList();
		}
		return isFolder(record) ? record.<List<String>>get(rm.folder.mediumTypes()) :
			   isDigital(record) ? singletonList(digitalMediumType.getId()) : Collections.<String>emptyList();
	}

	private boolean isFolder(Record record) {
		return record.isOfSchemaType(Folder.SCHEMA_TYPE);
	}

	private boolean isDigital(Record record) {
		return record.get(rm.document.content()) != null;
	}

}
