package com.constellio.app.modules.tasks.ui.components.fields;

import com.constellio.app.modules.rm.ui.components.converters.DocumentIdToContextCaptionConverter;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;

public class TaskLinkedDocumentsFieldImpl extends LookupRecordField implements TaskLinkedDocumentsField {

	public TaskLinkedDocumentsFieldImpl() {
		this(false);
	}

	public TaskLinkedDocumentsFieldImpl(boolean writeAccess) {
		super(Document.SCHEMA_TYPE, writeAccess);
		setItemConverter(new DocumentIdToContextCaptionConverter());
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
