package com.constellio.app.modules.rm.ui.components.copyRetentionRule;

import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.copyRetentionRule.CopyRetentionRuleField;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.retentionRule.CopyRetentionRuleDependencyField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCopyStatusEnteredField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCopyStatusEnteredFieldImpl;
import com.constellio.app.ui.pages.base.SessionContextProvider;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.List;

public interface RecordWithCopyRetentionRuleParametersFields extends SessionContextProvider {
    CopyRetentionRuleDependencyField getCopyRetentionRuleDependencyField();

    CopyRetentionRuleField getCopyRetentionRuleField();

    String getSchemaType();

    LogicalSearchQuery getQuery();

    List<String> getSelectedRecords();

    String getType();

    FolderCopyStatusEnteredFieldImpl getFolderCopyStatusEnteredField();
}
