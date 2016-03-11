package com.constellio.app.modules.rm.ui.components.folder.fields;

import java.util.List;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.vaadin.ui.ComboBox;

public class FolderCopyRuleFieldImpl extends ComboBox implements FolderCopyRuleField {
	public FolderCopyRuleFieldImpl(List<CopyRetentionRule> rules) {
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

	public void setFieldChoices(List<CopyRetentionRule> rules) {
		removeAllItems();
		for (CopyRetentionRule rule : rules) {
			addItem(rule.getId());
			setItemCaption(rule.getId(), rule.toString());
		}
	}
}
