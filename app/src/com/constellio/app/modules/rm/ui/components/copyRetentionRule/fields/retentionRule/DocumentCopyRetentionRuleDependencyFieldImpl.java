package com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.retentionRule;

import com.constellio.app.modules.rm.ui.components.folder.fields.LookupFolderField;
import com.vaadin.data.Property;

public class DocumentCopyRetentionRuleDependencyFieldImpl  extends LookupFolderField implements CopyRetentionRuleDependencyField {
    public DocumentCopyRetentionRuleDependencyFieldImpl(String collection) {
        super();
    }

    @Override
    public String getFieldValue() {
        return super.getValue();
    }

    @Override
    public void setFieldValue(String value) {
        super.setValue(value);
    }

    @Override
    public void addValueChangeListener(final RetentionValueChangeListener listener) {
        addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
                String newValue = (String) event.getProperty().getValue();
                listener.valueChanged(newValue);
            }
        });
    }
}

