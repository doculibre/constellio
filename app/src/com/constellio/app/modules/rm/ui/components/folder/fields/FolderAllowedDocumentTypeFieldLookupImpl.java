package com.constellio.app.modules.rm.ui.components.folder.fields;

import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;

import java.util.List;

import static com.constellio.app.services.factories.ConstellioFactories.getInstance;
import static com.constellio.app.ui.application.ConstellioUI.getCurrentSessionContext;

public class FolderAllowedDocumentTypeFieldLookupImpl extends ListAddRemoveRecordLookupField implements FolderAllowedDocumentTypeField {

	public FolderAllowedDocumentTypeFieldLookupImpl(String retentionRule, List<String> currentTypes) {
		super(DocumentType.SCHEMA_TYPE, null, new FolderAllowedDocumentTypeTextInputDataProvider(getInstance(),
				getCurrentSessionContext(), retentionRule, currentTypes));
		initContent();
	}

	@Override
	public List<String> getFieldValue() {
		return (List<String>) getConvertedValue();
	}

	@Override
	public void setFieldValue(Object value) {
		setInternalValue((List<String>) value);
	}
}
