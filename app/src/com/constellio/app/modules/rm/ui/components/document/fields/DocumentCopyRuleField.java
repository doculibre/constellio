package com.constellio.app.modules.rm.ui.components.document.fields;

import com.constellio.app.modules.rm.model.CopyRetentionRuleInRule;

import java.util.List;

public interface DocumentCopyRuleField extends CustomDocumentField<String> {
	void setFieldChoices(List<CopyRetentionRuleInRule> rules);
}
