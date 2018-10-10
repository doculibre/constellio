package com.constellio.app.modules.rm.extensions.app;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.mediumType.MediumTypeService;
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
import com.constellio.model.extensions.events.records.RecordReindexationEvent;
import com.constellio.model.extensions.events.records.RecordRestorationEvent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.singletonList;

@Slf4j
public class RMRecordMediumTypeExtension extends RecordExtension {

	private ModelLayerFactory modelLayerFactory;
	private RMSchemasRecordsServices rm;
	private RecordServices recordServices;
	private MediumTypeService mediumTypeService;

	private MediumType digitalMediumType;

	private enum EventType {
		CREATION, MODIFICATION, LOGICAL_DELETION, RESTORATION
	}

	public RMRecordMediumTypeExtension(String collection, AppLayerFactory appLayerFactory) {
		modelLayerFactory = appLayerFactory.getModelLayerFactory();
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		mediumTypeService = new MediumTypeService(collection, appLayerFactory);

		digitalMediumType = mediumTypeService.getDigitalMediumType();
	}

	@Override
	public void recordModified(RecordModificationEvent event) {
		if (isSynchronisationDisabled()) {
			return;
		}

		String schemaType = event.getSchemaTypeCode();
		if (schemaType.equals(Folder.SCHEMA_TYPE)) {
			if (event.hasModifiedMetadata(Folder.MEDIUM_TYPES) || event.hasModifiedMetadata(Folder.PARENT_FOLDER)) {
				updateParentFolderMediumType(event.getRecord(), event.getOriginalRecord(), EventType.MODIFICATION);
			}
		} else if (schemaType.equals(Document.SCHEMA_TYPE)) {
			if (event.hasModifiedMetadata(Document.CONTENT) || event.hasModifiedMetadata(Document.FOLDER)) {
				updateParentFolderMediumType(event.getRecord(), event.getOriginalRecord(), EventType.MODIFICATION);
			}
		}
	}

	@Override
	public void recordLogicallyDeleted(RecordLogicalDeletionEvent event) {
		if (isSynchronisationDisabled()) {
			return;
		}

		String schemaType = event.getSchemaTypeCode();
		if (schemaType.equals(Folder.SCHEMA_TYPE) || schemaType.equals(Document.SCHEMA_TYPE)) {
			updateParentFolderMediumType(event.getRecord(), null, EventType.LOGICAL_DELETION);
		}
	}

	@Override
	public void recordRestored(RecordRestorationEvent event) {
		if (isSynchronisationDisabled()) {
			return;
		}

		String schemaType = event.getSchemaTypeCode();
		if (schemaType.equals(Folder.SCHEMA_TYPE) || schemaType.equals(Document.SCHEMA_TYPE)) {
			updateParentFolderMediumType(event.getRecord(), null, EventType.RESTORATION);
		}
	}

	@Override
	public void recordCreated(RecordCreationEvent event) {
		if (isSynchronisationDisabled()) {
			return;
		}

		String schemaType = event.getSchemaTypeCode();
		if (schemaType.equals(Folder.SCHEMA_TYPE) || schemaType.equals(Document.SCHEMA_TYPE)) {
			updateParentFolderMediumType(event.getRecord(), null, EventType.CREATION);
		}
	}

	@Override
	public void recordReindexed(RecordReindexationEvent event) {
		if (isSynchronisationDisabled() || !event.getSchemaTypeCode().equals(Folder.SCHEMA_TYPE)) {
			return;
		}

		List<String> mediumTypes = mediumTypeService.getHierarchicalMediumTypes(event.getRecord());

		Folder folder = rm.wrapFolder(event.getRecord());
		if (folder.getMediumTypes().size() != mediumTypes.size() || !folder.getMediumTypes().containsAll(mediumTypes)) {
			folder.setMediumTypes(mediumTypes);
		}
	}

	private void updateParentFolderMediumType(Record record, Record originalRecord, EventType eventType) {
		try {
			String parentFolderId = getParentFolder(record);
			if (parentFolderId == null) {
				return;
			}
			if (isMediumTypeAdded(record, eventType)) {
				List<String> mediumTypes = getAddedMediumTypes(record, originalRecord);
				if (!mediumTypes.isEmpty()) {
					addMediumTypeToParentFolder(parentFolderId, mediumTypes);
				}
			} else if (isMediumTypeRemoved(record, originalRecord, eventType)) {
				Folder folder = rm.getFolder(parentFolderId).set(Schemas.MARKED_FOR_REINDEXING, true);
				recordServices.update(folder.getWrappedRecord());
			}
		} catch (RecordServicesException e) {
			log.error("Failed to update parent folder's medium types", e);
		}
	}

	private void addMediumTypeToParentFolder(String parentFolderId, List<String> addedMediumTypes)
			throws RecordServicesException {
		Folder folder = rm.getFolder(parentFolderId);
		if (!folder.getMediumTypes().containsAll(addedMediumTypes)) {
			Set<String> mediumTypes = new HashSet<>(folder.getMediumTypes());
			mediumTypes.addAll(addedMediumTypes);
			folder.setMediumTypes(new ArrayList<>(mediumTypes));

			recordServices.update(folder);
		}
	}

	private boolean isMediumTypeAdded(Record record, EventType eventType) {
		if (isFolder(record)) {
			return eventType.equals(EventType.CREATION) || eventType.equals(EventType.RESTORATION);
		} else {
			if (eventType.equals(EventType.CREATION) || eventType.equals(EventType.MODIFICATION) ||
				eventType.equals(EventType.RESTORATION)) {
				return isDigital(record);
			} else {
				return false;
			}
		}
	}

	private boolean isMediumTypeRemoved(Record record, Record originalRecord, EventType eventType) {
		if (isFolder(record)) {
			return eventType.equals(EventType.MODIFICATION) || eventType.equals(EventType.LOGICAL_DELETION);
		} else {
			if (eventType.equals(EventType.MODIFICATION)) {
				return !isDigital(record) && isDigital(originalRecord);
			} else if (eventType.equals(EventType.LOGICAL_DELETION)) {
				return isDigital(record);
			} else {
				return false;
			}
		}
	}

	private List<String> getAddedMediumTypes(Record record, Record originalRecord) {
		Set<String> mediumTypes = new HashSet<>(getCurrentMediumTypes(record));
		mediumTypes.removeAll(getOriginalMediumTypes(originalRecord));
		return new ArrayList<>(mediumTypes);
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

	private boolean isSynchronisationDisabled() {
		return !(new RMConfigs(modelLayerFactory.getSystemConfigurationsManager()).isMediumTypeSynchronisationEnabled());
	}

}
