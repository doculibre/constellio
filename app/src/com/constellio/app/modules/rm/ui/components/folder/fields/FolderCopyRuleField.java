package com.constellio.app.modules.rm.ui.components.folder.fields;

import java.util.List;

import com.constellio.app.modules.rm.model.CopyRetentionRule;

public interface FolderCopyRuleField extends CustomFolderField<String> {
	void setFieldChoices(List<CopyRetentionRule> rules);
}
