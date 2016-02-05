package com.constellio.app.modules.rm.ui.components.document.fields;

import java.io.Serializable;

public interface CustomDocumentField<T> extends Serializable {

	T getFieldValue();

	void setFieldValue(Object value);

	boolean isVisible();

	void setVisible(boolean visible);

	boolean isReadOnly();

	void setReadOnly(boolean readOnly);

	boolean isRequired();

	void setRequired(boolean required);

	void focus();
}
