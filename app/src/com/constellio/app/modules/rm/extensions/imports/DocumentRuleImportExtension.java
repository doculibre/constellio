package com.constellio.app.modules.rm.extensions.imports;

import java.util.Map;

import com.constellio.model.entities.records.Content;
import com.constellio.model.services.contents.UserSerializedContentFactory;
import com.constellio.model.services.records.SimpleImportContent;
import com.constellio.model.services.records.StructureImportContent;
import org.joda.time.LocalDateTime;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.behaviors.RecordImportExtension;
import com.constellio.model.extensions.events.recordsImport.BuildParams;
import com.constellio.model.services.factories.ModelLayerFactory;
import sun.java2d.pipe.SpanShapeRenderer;

public class DocumentRuleImportExtension extends RecordImportExtension {

	RMSchemasRecordsServices rm;
	ModelLayerFactory modelLayerFactory;
	String collection;

	public DocumentRuleImportExtension(String collection, ModelLayerFactory modelLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		this.modelLayerFactory = modelLayerFactory;
		this.collection = collection;
	}

	@Override
	public String getDecoratedSchemaType() {
		return Document.SCHEMA_TYPE;
	}

	@Override
	public void build(BuildParams event) {
		Map<String, Object> fields = event.getImportRecord().getFields();
		Document document = rm.wrapDocument(event.getRecord());



		String createdBy = (String) fields.get(Schemas.CREATED_BY.getLocalCode());
		if (createdBy != null) {
			document.setFormCreatedBy(document.getCreatedBy());
		}

		LocalDateTime createdOn = (LocalDateTime) fields.get(Schemas.CREATED_ON.getLocalCode());
		if (createdOn != null) {
			document.setFormCreatedOn(createdOn);
		}

		String modifiedBy = (String) fields.get(Schemas.MODIFIED_BY.getLocalCode());
		if (modifiedBy != null) {
			document.setFormModifiedBy(document.getModifiedBy());
		}

		LocalDateTime modifiedOn = (LocalDateTime) fields.get(Schemas.MODIFIED_ON.getLocalCode());
		if (modifiedOn != null) {
			document.setFormModifiedOn(modifiedOn);
		}
	}
}