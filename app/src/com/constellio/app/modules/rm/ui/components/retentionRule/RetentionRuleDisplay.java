/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.ui.components.retentionRule;

import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

public class RetentionRuleDisplay extends RecordDisplay {

	public RetentionRuleDisplay(RetentionRuleVO retentionRuleVO) {
		super(retentionRuleVO, new RetentionRuleDisplayFactory());
	}

	@Override
	protected void addCaptionAndDisplayComponent(Label captionLabel, Component displayComponent) {
		if (displayComponent instanceof CopyRetentionRuleTable) {
			CopyRetentionRuleTable copyRetentionRuleTable = (CopyRetentionRuleTable) displayComponent;
			copyRetentionRuleTable.setCaption(captionLabel.getValue());
			mainLayout.addComponent(copyRetentionRuleTable);
		} else {
			super.addCaptionAndDisplayComponent(captionLabel, displayComponent);
		}
	}

}
