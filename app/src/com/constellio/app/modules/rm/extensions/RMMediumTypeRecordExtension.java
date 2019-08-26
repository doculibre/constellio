package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.mediumType.MediumTypeService;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionValidationEvent;
import com.constellio.model.extensions.events.records.RecordReindexationEvent;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.schemas.MetadataList;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.model.entities.schemas.Schemas.CODE;

@Slf4j
public class RMMediumTypeRecordExtension extends RecordExtension {

	private AppLayerFactory appLayerFactory;
	private RMSchemasRecordsServices rm;
	private MediumTypeService mediumTypeService;
	private ReindexingServices reindexingServices;

	public RMMediumTypeRecordExtension(String collection, AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		mediumTypeService = new MediumTypeService(collection, appLayerFactory);
		reindexingServices = new ReindexingServices(appLayerFactory.getModelLayerFactory());
	}

	@Override
	public ValidationErrors validateLogicallyDeletable(RecordLogicalDeletionValidationEvent event) {
		if (MediumType.SCHEMA_TYPE.equals(event.getSchemaTypeCode())
			&& event.getRecord().get(CODE).equals("DM")) {
			ValidationErrors validationErrors = new ValidationErrors();
			validationErrors.add(RMMediumTypeRecordExtension.class, "cannotDeleteMediumTypeWithCodeDM");
			return validationErrors;
		}
		return super.validateLogicallyDeletable(event);
	}

	@Override
	public void recordInModificationBeforeSave(RecordInModificationBeforeSaveEvent event) {
		if (event.isSchemaType(Folder.SCHEMA_TYPE)) {
			Folder folder = rm.wrapFolder(event.getRecord());
			MetadataList modifiedMetadatas = event.getModifiedMetadatas();
			if (modifiedMetadatas.containsMetadataWithLocalCode(Folder.HAS_CONTENT)) {
				if (folder.hasContent()) {
					addActivatedOnContentMediumTypes(folder);
				} else {
					removeActivatedOnContentMediumTypes(folder);
				}
				event.recalculateRecord(Collections.singletonList(Folder.MEDIA_TYPE));
			}
		} else if (event.isSchemaType(MediumType.SCHEMA_TYPE)) {
			MediumType mediumType = rm.wrapMediumType(event.getRecord());
			MetadataList modifiedMetadatas = event.getModifiedMetadatas();
			if (modifiedMetadatas.containsMetadataWithLocalCode(MediumType.ACTIVATED_ON_CONTENT) &&
				mediumType.isActivatedOnContent()) {
				appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
			}
		}
	}

	@Override
	public void recordReindexed(RecordReindexationEvent event) {
		if (!reindexingServices.isLockFileExisting()) {
			return;
		}

		if (event.getRecord().isOfSchemaType(Folder.SCHEMA_TYPE)) {
			Folder folder = rm.wrapFolder(event.getRecord());
			if (folder.hasContent()) {
				addActivatedOnContentMediumTypes(folder);
			} else {
				removeActivatedOnContentMediumTypes(folder);
			}
			event.recalculateRecord(Collections.singletonList(Folder.MEDIA_TYPE));
		}
	}

	private void addActivatedOnContentMediumTypes(Folder folder) {
		Set<String> mediumTypes = new HashSet<>(folder.getMediumTypes());
		for (MediumType mediumType : mediumTypeService.getActivatedOnContentMediumTypes()) {
			mediumTypes.add(mediumType.getId());
		}
		folder.setMediumTypes(new ArrayList<>(mediumTypes));
	}

	private void removeActivatedOnContentMediumTypes(Folder folder) {
		List<String> mediumTypes = new ArrayList<>(folder.getMediumTypes());
		for (MediumType mediumType : mediumTypeService.getActivatedOnContentMediumTypes()) {
			mediumTypes.remove(mediumType.getId());
		}
		folder.setMediumTypes(mediumTypes);
	}
}
