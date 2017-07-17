package com.constellio.app.ui.pages.management.labels;

import java.io.Serializable;

public interface CustomLabelField<T> extends Serializable {

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
