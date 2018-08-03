package com.constellio.app.modules.rm.ui.components.folder.fields;

import com.constellio.app.ui.framework.components.fields.date.JodaDateField;
import org.joda.time.LocalDate;

import java.util.Date;

public class FolderPreviewReturnDateFieldImpl extends JodaDateField implements FolderPreviewReturnDateField {

	@Override
	public LocalDate getFieldValue() {
		return (LocalDate) getConvertedValue();
	}

	@Override
	public void setFieldValue(Object value) {
		setInternalValue((Date) value);
	}

}
