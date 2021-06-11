package com.constellio.app.modules.rm.ui.components.folder;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.ui.components.folder.fields.CustomFolderField;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.ui.Field;

import java.util.HashMap;
import java.util.Map;

public abstract class FolderFormImpl extends RecordForm implements FolderForm {

	private Map<String, Field> extraField;

	public FolderFormImpl(RecordVO record, ConstellioFactories constellioFactories) {
		super(record, new FolderFieldFactory(
				record.getSchema().getCollection(),
				record.<CopyRetentionRule>getList(Folder.APPLICABLE_COPY_RULES),
				getRetentionRuleEnteredValue(record),
				record.getMetadataValue(record.getMetadata(Folder.PARENT_FOLDER)).<String>getValue()), constellioFactories);

		extraField = new HashMap<>();
	}


	private static String getRetentionRuleEnteredValue(RecordVO record) {
		MetadataVO metadataVO = record.getMetadataOrNull(Folder.RETENTION_RULE_ENTERED);
		if (metadataVO != null) {
			return record.getMetadataValue(metadataVO).getValue();
		}
		return null;
	}

	@Override
	public CustomFolderField<?> getCustomField(String metadataCode) {
		return (CustomFolderField<?>) getField(metadataCode);
	}

	@Override
	public ConstellioFactories getConstellioFactories() {
		return ConstellioFactories.getInstance();
	}

	@Override
	public SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

	public void addExtraFieldToForm(Field component, int index, String key) {
		formLayout.addComponent(component, index);
		extraField.put(key, component);
	}

	public Field getExtraField(String key) {
		return extraField.get(key);
	}

	@Override
	public void extraActionBeforeComparingOldAndNewRecord(RecordVO recordVO) {
		if (recordVO.get(Folder.PARENT_FOLDER) != null) {
			recordVO.set(Folder.CATEGORY_ENTERED, null);
		}
	}
}
