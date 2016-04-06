package com.constellio.app.modules.rm.ui.components.document.fields;

import java.util.List;

import com.constellio.app.modules.rm.model.CopyRetentionRuleInRule;

public interface DocumentCopyRuleField extends CustomDocumentField<String> {
	void setFieldChoices(List<CopyRetentionRuleInRule> rules);
}
