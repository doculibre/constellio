package com.constellio.app.modules.rm.ui.components.folder.fields;

import com.constellio.app.modules.rm.model.CopyRetentionRule;

import java.util.List;

public interface FolderCopyRuleField extends CustomFolderField<String> {
	void setFieldChoices(List<CopyRetentionRule> rules);
}
