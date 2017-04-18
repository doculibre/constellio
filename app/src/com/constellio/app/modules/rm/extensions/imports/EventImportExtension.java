package com.constellio.app.modules.rm.extensions.imports;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.extensions.behaviors.RecordImportExtension;
import com.constellio.model.extensions.events.recordsImport.BuildParams;
import com.constellio.model.extensions.events.recordsImport.PrevalidationParams;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class EventImportExtension extends RecordImportExtension {

	public static final String TYPE_REQUIRED = "typeRequired";

	RMSchemasRecordsServices rm;

	public EventImportExtension(String collection, ModelLayerFactory modelLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
	}

	@Override
	public void prevalidate(PrevalidationParams params) {
		Map<String, Object> fields = params.getImportRecord().getFields();
		Object type = fields.get("type");
		if (type == null || !(type instanceof String) || StringUtils.isBlank((String) type)) {
			params.getErrors().add(RetentionRuleImportExtension.class, TYPE_REQUIRED);
		}
	}

	@Override
	public String getDecoratedSchemaType() {
		return Event.SCHEMA_TYPE;
	}

	@Override
	public void build(BuildParams params) {
		Event event = rm.wrapEvent(params.getRecord());

		if (event.getRecordId() != null) {
			if (event.getType().toLowerCase().contains(Folder.SCHEMA_TYPE.toLowerCase())) {
				Folder folder = rm.getFolderWithLegacyId(event.getRecordId());
				if(folder == null) {
					try {
						folder = rm.getFolder(event.getRecordId());
					} catch (Exception e) {
						event.setRecordId(null);
					}
				}
				if(folder != null) {
					event.setRecordId(folder.getId());
					event.setTitle(folder.getTitle());
				}
			}

			if (event.getType().toLowerCase().contains(Document.SCHEMA_TYPE.toLowerCase())) {
				Document document = rm.getDocumentByLegacyId(event.getRecordId());
				if(document == null) {
					try {
						document = rm.getDocument(event.getRecordId());
					} catch (Exception e) {
						event.setRecordId(null);
					}
				}
				if(document != null) {
					event.setRecordId(document.getId());
					event.setTitle(document.getTitle());
				}
			}

			if (event.getType().toLowerCase().contains(ContainerRecord.SCHEMA_TYPE.toLowerCase())) {
				ContainerRecord containerRecord = rm.getContainerRecordWithLegacyId(event.getRecordId());
				if(containerRecord == null) {
					try {
						containerRecord = rm.getContainerRecord(event.getRecordId());
					} catch (Exception e) {
						event.setRecordId(null);
					}
				}
				if(containerRecord != null) {
					event.setRecordId(containerRecord.getId());
					event.setTitle(containerRecord.getTitle());
				}
			}
		}
	}

}
