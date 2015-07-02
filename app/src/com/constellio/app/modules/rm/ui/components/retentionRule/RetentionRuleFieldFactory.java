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

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.ui.entities.VariableRetentionPeriodVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.vaadin.ui.Field;

import java.util.List;

public abstract class RetentionRuleFieldFactory extends RecordFieldFactory {

	@Override
	public Field<?> build(RecordVO recordVO, MetadataVO metadataVO) {
		Field<?> field;
		String metadataCode = MetadataVO.getCodeWithoutPrefix(metadataVO.getCode());
		RetentionRuleVO retentionRuleVO = (RetentionRuleVO) recordVO;
		switch (metadataCode) {
		case RetentionRule.COPY_RETENTION_RULES:
			field = new CopyRetentionRuleTable(retentionRuleVO, true, getOpenPeriodsDDVList()) {
				@Override
				protected void onDisposalTypeChange(CopyRetentionRule copyRetentionRule) {
					RetentionRuleFieldFactory.this.onDisposalTypeChange(copyRetentionRule);
				}
			};
			postBuild(field, recordVO, metadataVO);
			return field;
		case RetentionRule.DOCUMENT_TYPES_DETAILS:
			field = new ListAddRemoveRetentionRuleDocumentTypeField();
			postBuild(field, recordVO, metadataVO);
			return field;
		case "categories":
			field = super.build(recordVO, metadataVO);
			((ListAddRemoveRecordLookupField) field).setIgnoreLinkability(true);
			return field;
		default:
			return super.build(recordVO, metadataVO);
		}
	}

	protected abstract List<VariableRetentionPeriodVO> getOpenPeriodsDDVList();

	protected abstract void onDisposalTypeChange(CopyRetentionRule rule);
}
