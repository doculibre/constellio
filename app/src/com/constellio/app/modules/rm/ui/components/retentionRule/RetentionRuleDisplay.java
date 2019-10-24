package com.constellio.app.modules.rm.ui.components.retentionRule;

import com.constellio.app.modules.rm.ui.components.retentionRule.RetentionRuleDisplayFactory.RetentionRuleDisplayPresenter;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import java.util.Locale;

public class RetentionRuleDisplay extends RecordDisplay {

	public RetentionRuleDisplay(RetentionRuleDisplayPresenter presenter, RetentionRuleVO retentionRuleVO,
								Locale locale) {
		super(retentionRuleVO, new RetentionRuleDisplayFactory(presenter, locale));

		addStyleName("retention-rule-display");
	}

	@Override
	protected void addCaptionAndDisplayComponent(Label captionLabel, Component displayComponent, VerticalLayout layout) {
		if (layout.getWidth() != 100 && layout.getWidthUnits() != Unit.PERCENTAGE) {
			layout.setWidth("100%");
		}
		if (displayComponent instanceof FolderCopyRetentionRuleTable) {
			FolderCopyRetentionRuleTable folderCopyRetentionRuleTable = (FolderCopyRetentionRuleTable) displayComponent;
			folderCopyRetentionRuleTable.setCaption(captionLabel.getValue());
			folderCopyRetentionRuleTable.setWidth("100%");
			layout.addComponent(folderCopyRetentionRuleTable);
			displayComponent.addStyleName(STYLE_FULL_WIDTH);
		} else if (displayComponent instanceof DocumentCopyRetentionRuleTable) {
			DocumentCopyRetentionRuleTable documentCopyRetentionRuleTable = (DocumentCopyRetentionRuleTable) displayComponent;
			documentCopyRetentionRuleTable.setCaption(captionLabel.getValue());
			documentCopyRetentionRuleTable.setWidth("100%");
			layout.addComponent(documentCopyRetentionRuleTable);
			displayComponent.addStyleName(STYLE_FULL_WIDTH);
		} else if (displayComponent instanceof DocumentDefaultCopyRetentionRuleTable) {
			DocumentDefaultCopyRetentionRuleTable documentDefaultCopyRetentionRuleTable = (DocumentDefaultCopyRetentionRuleTable) displayComponent;
			documentDefaultCopyRetentionRuleTable.setCaption(captionLabel.getValue());
			documentDefaultCopyRetentionRuleTable.setWidth("100%");
			layout.addComponent(documentDefaultCopyRetentionRuleTable);
			displayComponent.addStyleName(STYLE_FULL_WIDTH);
		} else {
			super.addCaptionAndDisplayComponent(captionLabel, displayComponent, layout);
		}
	}

	@Override
	protected boolean isCaptionAndDisplayComponentWidthUndefined() {
		return true;
	}

}
