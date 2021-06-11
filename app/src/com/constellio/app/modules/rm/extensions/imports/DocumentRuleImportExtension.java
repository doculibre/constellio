package com.constellio.app.modules.rm.extensions.imports;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.RMObject;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.behaviors.RecordImportExtension;
import com.constellio.model.extensions.events.recordsImport.BuildParams;
import com.constellio.model.extensions.events.recordsImport.PrevalidationParams;
import com.constellio.model.extensions.events.recordsImport.ValidationParams;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.joda.time.LocalDateTime;

import java.util.Map;

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
		if (createdBy != null && !fields.containsKey(RMObject.FORM_CREATED_BY)) {
			document.setFormCreatedBy(document.getCreatedBy());
		}

		LocalDateTime createdOn = (LocalDateTime) fields.get(Schemas.CREATED_ON.getLocalCode());
		if (createdOn != null && !fields.containsKey(RMObject.FORM_CREATED_ON)) {
			document.setFormCreatedOn(createdOn);
		}

		String modifiedBy = (String) fields.get(Schemas.MODIFIED_BY.getLocalCode());
		if (modifiedBy != null && !fields.containsKey(RMObject.FORM_MODIFIED_BY)) {
			document.setFormModifiedBy(document.getModifiedBy());
		}

		LocalDateTime modifiedOn = (LocalDateTime) fields.get(Schemas.MODIFIED_ON.getLocalCode());
		if (modifiedOn != null && !fields.containsKey(RMObject.FORM_MODIFIED_ON)) {
			document.setFormModifiedOn(modifiedOn);
		}
	}

	@Override
	public ExtensionBooleanResult skipPrevalidation(PrevalidationParams event) {
		Map<String, Object> fields = event.getImportRecord().getFields();
		Object isModel = fields.get(Document.IS_MODEL);
		if (isModel != null) {
			return skipValidations((String) isModel);
		}
		return super.skipPrevalidation(event);
	}

	@Override
	public ExtensionBooleanResult skipValidation(ValidationParams event) {
		Map<String, Object> fields = event.getImportRecord().getFields();
		Object isModel = fields.get(Document.IS_MODEL);
		if (isModel != null) {
			return skipValidations((String) isModel);
		}
		return super.skipValidation(event);
	}

	private ExtensionBooleanResult skipValidations(String isModel) {
		return "true".equals(isModel) ? ExtensionBooleanResult.FORCE_TRUE : ExtensionBooleanResult.FALSE;
	}
}