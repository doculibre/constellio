package com.constellio.app.modules.rm.ui.components.retentionRule;

import com.constellio.app.modules.rm.ui.components.retentionRule.RetentionRuleDisplayFactory.RetentionRuleDisplayPresenter;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

public class RetentionRuleDisplay extends RecordDisplay {
	public RetentionRuleDisplay(RetentionRuleDisplayPresenter presenter, RetentionRuleVO retentionRuleVO) {
		super(retentionRuleVO, new RetentionRuleDisplayFactory(presenter));
	}

	@Override
	protected void addCaptionAndDisplayComponent(Label captionLabel, Component displayComponent) {
		if (displayComponent instanceof FolderCopyRetentionRuleTable) {
			FolderCopyRetentionRuleTable folderCopyRetentionRuleTable = (FolderCopyRetentionRuleTable) displayComponent;
			folderCopyRetentionRuleTable.setCaption(captionLabel.getValue());
			mainLayout.addComponent(folderCopyRetentionRuleTable);
		} else if (displayComponent instanceof DocumentCopyRetentionRuleTable) {
			DocumentCopyRetentionRuleTable documentCopyRetentionRuleTable = (DocumentCopyRetentionRuleTable) displayComponent;
			documentCopyRetentionRuleTable.setCaption(captionLabel.getValue());
			mainLayout.addComponent(documentCopyRetentionRuleTable);
		} else {
			super.addCaptionAndDisplayComponent(captionLabel, displayComponent);
		}
	}
}
