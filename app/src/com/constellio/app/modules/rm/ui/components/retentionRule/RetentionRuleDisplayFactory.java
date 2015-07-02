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

import static com.constellio.app.modules.rm.wrappers.RetentionRule.COPY_RETENTION_RULES;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.DOCUMENT_TYPES_DETAILS;

import com.constellio.app.modules.rm.ui.components.RMMetadataDisplayFactory;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.ui.entities.VariableRetentionPeriodVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.vaadin.ui.Component;

import java.util.List;

public class RetentionRuleDisplayFactory extends RMMetadataDisplayFactory {
	private List<VariableRetentionPeriodVO> openPeriodsDDVList;

	public RetentionRuleDisplayFactory(List<VariableRetentionPeriodVO> openPeriodsDDVList) {
		this.openPeriodsDDVList = openPeriodsDDVList;
	}

	@Override
	public Component build(RecordVO recordVO, MetadataValueVO metadataValueVO) {
		Component component;
		MetadataVO metadataVO = metadataValueVO.getMetadata();
		String metadataCode = MetadataVO.getCodeWithoutPrefix(metadataVO.getCode());
		RetentionRuleVO retentionRuleVO = (RetentionRuleVO) recordVO;
		if (COPY_RETENTION_RULES.equals(metadataCode)) {
			component = new CopyRetentionRuleTable(retentionRuleVO, false, openPeriodsDDVList);
		} else if (DOCUMENT_TYPES_DETAILS.equals(metadataCode)) {
			component = new RetentionRuleDocumentTypeDisplay(retentionRuleVO);
		} else {
			component = super.build(recordVO, metadataValueVO);
		}
		return component;
	}

}
