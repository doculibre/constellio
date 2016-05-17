package com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.copyRetentionRule;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCopyRuleFieldImpl;
import com.constellio.app.ui.i18n.i18n;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class CopyRetentionRuleFieldImpl extends FolderCopyRuleFieldImpl implements CopyRetentionRuleField {
    public CopyRetentionRuleFieldImpl() {
        super(new ArrayList<CopyRetentionRule>());
        setCaption($("BatchProcessingButton.copyRetentionRuleField"));
    }

    @Override
    public void setOptions(List<CopyRetentionRule> options) {
        setFieldChoices(options);
    }

}
