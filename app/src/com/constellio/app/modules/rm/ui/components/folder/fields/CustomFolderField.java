package com.constellio.app.modules.rm.ui.components.folder.fields;

import java.io.Serializable;

public interface CustomFolderField<T> extends Serializable {

	T getFieldValue();

	void setFieldValue(Object value);

	boolean isVisible();

	void setVisible(boolean visible);

	boolean isRequired();

	void setRequired(boolean required);

	void setReadOnly(boolean readOnly);

	void focus();

	void setCaption(String caption);
	
}
