package com.constellio.app.modules.rm.ui.pages.trigger;

import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.ui.field.TableAddRemoveTriggerActionField;
import com.constellio.app.modules.rm.wrappers.triggers.Trigger;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.framework.components.fields.AdvancedSearchCriteriaField;
import com.constellio.app.ui.pages.search.criteria.CriterionFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.vaadin.ui.Field;

public class TriggerFieldFactory extends MetadataFieldFactory {

	private ConstellioFactories constellioFactories;
	private RecordVO triggerRecord;

	public TriggerFieldFactory(ConstellioFactories constellioFactories, RecordVO triggerRecord) {
		this.constellioFactories = constellioFactories;
		this.triggerRecord = triggerRecord;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	protected Field<?> newMultipleValueField(MetadataVO metadata, String recordId) {
		Field<?> field;

		MetadataInputType metadataInputType = metadata.getMetadataInputType();
		MetadataValueType metadataValueType = metadata.getType();

		if (metadataInputType == MetadataInputType.HIDDEN) {
			field = null;
		} else {
			switch (metadataValueType) {
				case STRUCTURE:
					if (metadata.getStructureFactory() instanceof CriterionFactory) {
						field = new AdvancedSearchCriteriaField(constellioFactories);
						postBuild(field, metadata);
					} else {
						field = super.newMultipleValueField(metadata, recordId);
						;
					}
					break;
				case REFERENCE:
					if (metadata.getCode().equals(Trigger.DEFAULT_SCHEMA + "_" + Trigger.ACTIONS)) {
						field = new TableAddRemoveTriggerActionField(constellioFactories, triggerRecord);
					} else {
						field = super.newMultipleValueField(metadata, recordId);
					}
					break;
				default:
					field = super.newMultipleValueField(metadata, recordId);
					break;
			}
		}

		return field;
	}
}
