package com.constellio.app.modules.rm.ui.components.folder;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.ui.components.folder.fields.CustomFolderField;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.pages.base.SessionContext;

public abstract class FolderFormImpl extends RecordForm implements FolderForm {

	public FolderFormImpl(RecordVO record) {
		super(record, new FolderFieldFactory(
				record.getSchema().getCollection(),
				record.<CopyRetentionRule>getList(Folder.APPLICABLE_COPY_RULES)));
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
}
