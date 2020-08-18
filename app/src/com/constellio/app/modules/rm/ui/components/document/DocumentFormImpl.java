package com.constellio.app.modules.rm.ui.components.document;

import com.constellio.app.modules.rm.model.CopyRetentionRuleInRule;
import com.constellio.app.modules.rm.ui.components.document.fields.CustomDocumentField;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.pages.base.SessionContext;

public abstract class DocumentFormImpl extends RecordForm implements DocumentForm {

	public DocumentFormImpl(RecordVO record, boolean isViewOnly, ConstellioFactories constellioFactories) {
		super(record, new DocumentFieldFactory(record.getMetadataValue(
				record.getMetadata(Document.FOLDER)).<String>getValue(),
				record.getMetadataValue(record.getMetadata(Document.TYPE)).<String>getValue(),
				record.<CopyRetentionRuleInRule>getList(Document.APPLICABLE_COPY_RULES), isViewOnly), constellioFactories);
	}

	@Override
	public CustomDocumentField<?> getCustomField(String metadataCode) {
		return (CustomDocumentField<?>) getField(metadataCode);
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
