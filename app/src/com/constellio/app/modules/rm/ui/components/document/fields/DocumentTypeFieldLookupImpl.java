package com.constellio.app.modules.rm.ui.components.document.fields;

import static com.constellio.app.services.factories.ConstellioFactories.getInstance;
import static com.constellio.app.ui.application.ConstellioUI.getCurrentSessionContext;

import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;

public class DocumentTypeFieldLookupImpl extends LookupRecordField implements DocumentTypeField {

	public DocumentTypeFieldLookupImpl(String folderId, String currentType) {
		super(DocumentType.SCHEMA_TYPE, false, new DocumentTypeTextInputDataProvider(getInstance(),
				getCurrentSessionContext(), folderId, currentType));
	}

	@Override
	public String getFieldValue() {
		return (String) getConvertedValue();
	}

	@Override
	public void setFieldValue(Object value) {
		setInternalValue((String) value);
	}

}
