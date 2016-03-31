package com.constellio.app.modules.rm.extensions.imports;

import java.util.Map;

import org.joda.time.LocalDateTime;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.behaviors.RecordImportExtension;
import com.constellio.model.extensions.events.recordsImport.BuildParams;
import com.constellio.model.services.factories.ModelLayerFactory;

public class FolderRuleImportExtension extends RecordImportExtension {

	RMSchemasRecordsServices rm;

	public FolderRuleImportExtension(String collection, ModelLayerFactory modelLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
	}

	@Override
	public String getDecoratedSchemaType() {
		return Folder.SCHEMA_TYPE;
	}

	@Override
	public void build(BuildParams event) {
		Map<String, Object> fields = event.getImportRecord().getFields();
		Folder folder = rm.wrapFolder(event.getRecord());
		if (folder.getFormCreatedBy() == null) {
			String createdBy = (String) fields.get(Schemas.CREATED_BY.getLocalCode());
			if (createdBy != null) {
				folder.setFormCreatedBy(folder.getCreatedBy());
			}
		}

		if (folder.getFormCreatedOn() == null) {
			LocalDateTime createdOn = (LocalDateTime) fields.get(Schemas.CREATED_ON.getLocalCode());
			folder.setFormCreatedOn(createdOn);
		}

		if (folder.getFormModifiedBy() == null) {
			String modifiedBy = (String) fields.get(Schemas.MODIFIED_BY.getLocalCode());
			if (modifiedBy != null) {
				folder.setFormModifiedBy(folder.getModifiedBy());
			}
		}

		if (folder.getFormModifiedOn() == null) {
			LocalDateTime modifiedOn = (LocalDateTime) fields.get(Schemas.MODIFIED_ON.getLocalCode());
			folder.setFormModifiedOn(modifiedOn);
		}

		String enteredMainCopyId = folder.getMainCopyRuleIdEntered();
		if (enteredMainCopyId != null && folder.getRetentionRuleEntered() != null) {
			RetentionRule rule = rm.getRetentionRule(folder.getRetentionRuleEntered());

			String validId = null;
			for (CopyRetentionRule copy : rule.getCopyRetentionRules()) {
				if (copy.getId().equals(enteredMainCopyId) || (copy.getCode() != null && copy.getCode()
						.equals(enteredMainCopyId))) {
					validId = copy.getId();
					break;
				}
			}
			folder.setMainCopyRuleEntered(validId);
		}

	}

}
