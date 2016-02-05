package com.constellio.app.modules.tasks.ui.components.fields;

import java.io.Serializable;

public interface CustomTaskField<T> extends Serializable {

	T getFieldValue();

	void setFieldValue(Object value);

	boolean isVisible();

	void setVisible(boolean visible);

	boolean isRequired();

	void setRequired(boolean required);
}
