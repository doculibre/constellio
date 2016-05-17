package com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.copyRetentionRule;

import com.constellio.app.modules.rm.model.CopyRetentionRule;

import java.util.List;

public interface CopyRetentionRuleField {
    void setOptions(List<CopyRetentionRule> options);

    String getFieldValue();

    void setFieldValue(Object string);
}
