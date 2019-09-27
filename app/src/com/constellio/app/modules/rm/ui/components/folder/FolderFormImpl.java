package com.constellio.app.modules.rm.ui.components.folder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.ui.components.folder.fields.CustomFolderField;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.ui.Field;

public abstract class FolderFormImpl extends RecordForm implements FolderForm {

	private Map<String, Field> extraField;

	public FolderFormImpl(RecordVO record) {
		super(record, new FolderFieldFactory(
					record.getSchema().getCollection(),
					record.<CopyRetentionRule>getList(Folder.APPLICABLE_COPY_RULES),
					record.getMetadataValue(record.getMetadata(Folder.RETENTION_RULE_ENTERED)).<String>getValue(),
					record.getMetadataValue(record.getMetadata(Folder.ALLOWED_DOCUMENT_TYPES)).<List<String>>getValue()));

		extraField = new HashMap<>();
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
}
