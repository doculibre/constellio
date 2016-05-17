package com.constellio.app.modules.rm.ui.components.copyRetentionRule;

import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.copyRetentionRule.CopyRetentionRuleField;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.retentionRule.CopyRetentionRuleDependencyField;
import com.constellio.app.ui.pages.base.SessionContextProvider;

public interface RecordWithCopyRetentionRuleParametersFields extends SessionContextProvider {
    CopyRetentionRuleDependencyField getCopyRetentionRuleDependencyField();

    CopyRetentionRuleField getCopyRetentionRuleField();

    String getSchemaType();

    String getType();
}
