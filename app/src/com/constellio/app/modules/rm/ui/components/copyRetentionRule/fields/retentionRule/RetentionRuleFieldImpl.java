package com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.retentionRule;

import com.constellio.app.modules.rm.ui.components.folder.fields.FolderRetentionRuleFieldImpl;
import com.vaadin.data.Property;

public class RetentionRuleFieldImpl extends FolderRetentionRuleFieldImpl implements RetentionRuleField{
    public RetentionRuleFieldImpl(String collection) {
        super(collection);
    }

    @Override
    public void setFieldValue(String value) {
        super.setFieldValue(value);
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
