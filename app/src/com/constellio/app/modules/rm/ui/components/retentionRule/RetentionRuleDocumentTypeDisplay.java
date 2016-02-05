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
