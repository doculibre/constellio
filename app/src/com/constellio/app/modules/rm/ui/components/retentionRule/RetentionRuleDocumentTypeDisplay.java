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

import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.modules.rm.wrappers.structures.RetentionRuleDocumentType;
import com.constellio.app.ui.framework.components.converters.EnumWithSmallCodeToCaptionConverter;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

public class RetentionRuleDocumentTypeDisplay extends Label {
	
	private RecordIdToCaptionConverter documentTypeConverter = new RecordIdToCaptionConverter();
	
	private EnumWithSmallCodeToCaptionConverter disposalTypeConverter = new EnumWithSmallCodeToCaptionConverter(DisposalType.class);

	public RetentionRuleDocumentTypeDisplay(RetentionRuleVO retentionRuleVO) {
		setContentMode(ContentMode.HTML);
		
		boolean disposalTypeVisible = retentionRuleVO.hasCopyRetentionRuleWithSortDispositionType();
		
		StringBuffer value = new StringBuffer();
		for (RetentionRuleDocumentType retentionRuleDocumentType : retentionRuleVO.getDocumentTypesDetails()) {
			if (value.length() > 0) {
				value.append("<br />");
			}
			String documentTypeId = retentionRuleDocumentType.getDocumentTypeId();
			DisposalType disposalType = retentionRuleDocumentType.getDisposalType();
			String documentTypeCaption = documentTypeConverter.convertToPresentation(documentTypeId, String.class, getLocale());
			value.append(documentTypeCaption);
			if (disposalTypeVisible && disposalType != null) {
				String disposalTypeCaption = disposalTypeConverter.convertToPresentation(disposalType.getCode(), String.class, getLocale());
				value.append(" (" + disposalTypeCaption + ")");
			}
		}
		setValue(value.toString());
	}

}
