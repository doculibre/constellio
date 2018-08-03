package com.constellio.app.modules.rm.ui.components.retentionRule;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.ui.entities.VariableRetentionPeriodVO;

import java.util.List;

public interface RetentionRuleTablePresenter {

	CopyRetentionRule newDocumentCopyRetentionRule();

	CopyRetentionRule newFolderCopyRetentionRule(boolean principal);

	CopyRetentionRule newDocumentDefaultCopyRetentionRule(boolean principal);

	List<VariableRetentionPeriodVO> getOpenPeriodsDDVList();
}
