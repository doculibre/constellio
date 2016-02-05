package com.constellio.app.modules.rm.ui.components.folder.fields;

import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.vaadin.ui.ComboBox;

public class FolderUniformSubdivisionFieldImpl extends ComboBox implements FolderUniformSubdivisionField {

	private RecordIdToCaptionConverter captionConverter = new RecordIdToCaptionConverter();

	@Override
	public String getItemCaption(Object itemId) {
		return captionConverter.convertToPresentation((String) itemId, String.class, getLocale());
	}

	@Override
	public String getFieldValue() {
		return (String) getConvertedValue();
	}

	@Override
	public void setFieldValue(Object value) {
		setInternalValue(value);
	}

}
