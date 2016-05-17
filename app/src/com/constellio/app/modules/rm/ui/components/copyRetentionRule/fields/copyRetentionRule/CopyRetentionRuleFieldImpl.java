package com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.copyRetentionRule;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCopyRuleFieldImpl;

import java.util.ArrayList;
import java.util.List;

public class CopyRetentionRuleFieldImpl extends FolderCopyRuleFieldImpl implements CopyRetentionRuleField {
    public CopyRetentionRuleFieldImpl() {
        super(new ArrayList<CopyRetentionRule>());
    }

    @Override
    public void setOptions(List<CopyRetentionRule> options) {
        setFieldChoices(options);
    }

}
