package com.constellio.app.modules.tasks.ui.components.fields;

import com.vaadin.ui.TextArea;

/**
 * Created by Constellio on 2017-03-31.
 */
public class TaskReasonFieldImpl extends TextArea implements TaskReasonField {

    @Override
    public String getFieldValue() {
        return getValue();
    }

    @Override
    public void setFieldValue(Object value) {
        setValue((String) value);
    }
}
