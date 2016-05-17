package com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.retentionRule;

public interface RetentionRuleField {
    String getFieldValue();

    void setFieldValue(String value);

    void addValueChangeListener(RetentionValueChangeListener listener);

    interface RetentionValueChangeListener {

        void valueChanged(String newValue);

    }
}
