package com.constellio.app.modules.rm.extensions.imports;

import java.util.Map;

import org.joda.time.LocalDateTime;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.behaviors.RecordImportExtension;
import com.constellio.model.extensions.events.recordsImport.BuildParams;
import com.constellio.model.services.factories.ModelLayerFactory;

public class DocumentRuleImportExtension extends RecordImportExtension {

	RMSchemasRecordsServices rm;

	public DocumentRuleImportExtension(String collection, ModelLayerFactory modelLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
	}

	@Override
	public String getDecoratedSchemaType() {
		return Document.SCHEMA_TYPE;
	}

	@Override
	public void build(BuildParams event) {
		Map<String, Object> fields = event.getImportRecord().getFields();
		Document document = rm.wrapDocument(event.getRecord());
		if (document.getFormCreatedBy() == null) {
			String createdBy = (String) fields.get(Schemas.CREATED_BY.getLocalCode());
			if (createdBy != null) {
				document.setFormCreatedBy(document.getCreatedBy());
			}
		}

		if (document.getFormCreatedOn() == null) {
			LocalDateTime createdOn = (LocalDateTime) fields.get(Schemas.CREATED_ON.getLocalCode());
			document.setFormCreatedOn(createdOn);
		}

		if (document.getFormModifiedBy() == null) {
			String modifiedBy = (String) fields.get(Schemas.MODIFIED_BY.getLocalCode());
			if (modifiedBy != null) {
				document.setFormModifiedBy(document.getModifiedBy());
			}
		}

		if (document.getFormModifiedOn() == null) {
			LocalDateTime modifiedOn = (LocalDateTime) fields.get(Schemas.MODIFIED_ON.getLocalCode());
			document.setFormModifiedOn(modifiedOn);
		}
	}
}