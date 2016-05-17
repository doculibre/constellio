package com.constellio.app.modules.rm.ui.components.copyRetentionRule;

import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.copyRetentionRule.CopyRetentionRuleField;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.retentionRule.RetentionRuleField;
import com.constellio.app.ui.pages.base.SessionContextProvider;

public interface RecordWithCopyRetentionRuleParametersFields extends SessionContextProvider {
    RetentionRuleField getRetentionField();

    CopyRetentionRuleField getCopyRetentionRuleField();
}
