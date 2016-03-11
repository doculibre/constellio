package com.constellio.app.modules.rm.ui.components.document.fields;

import java.util.List;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleInRule;
import com.vaadin.ui.ComboBox;

public class DocumentCopyRuleFieldImpl extends ComboBox implements DocumentCopyRuleField {
	public DocumentCopyRuleFieldImpl(List<CopyRetentionRuleInRule> rules) {
		setFieldChoices(rules);
	}

	@Override
	public String getFieldValue() {
		return (String) getValue();
	}

	@Override
	public void setFieldValue(Object value) {
		setValue(value);
	}

	public void setFieldChoices(List<CopyRetentionRuleInRule> rules) {
		removeAllItems();
		for (CopyRetentionRuleInRule rule : rules) {
			CopyRetentionRule copyRule = rule.getCopyRetentionRule();
			addItem(copyRule.getId());
			setItemCaption(copyRule.getId(), copyRule.toString());
		}
	}
}
