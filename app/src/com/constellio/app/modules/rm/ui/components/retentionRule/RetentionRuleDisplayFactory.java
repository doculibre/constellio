package com.constellio.app.modules.rm.ui.components.retentionRule;

import static com.constellio.app.modules.rm.wrappers.RetentionRule.COPY_RETENTION_RULES;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.DOCUMENT_COPY_RETENTION_RULES;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.DOCUMENT_TYPES_DETAILS;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.PRINCIPAL_DEFAULT_DOCUMENT_COPY_RETENTION_RULE;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.SECONDARY_DEFAULT_DOCUMENT_COPY_RETENTION_RULE;

import java.util.List;

import com.constellio.app.modules.rm.ui.components.RMMetadataDisplayFactory;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.VariableRetentionPeriodVO;
import com.vaadin.ui.Component;

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
			component = new FolderCopyRetentionRuleTable(retentionRuleVO, false, openPeriodsDDVList);
		} else if (DOCUMENT_COPY_RETENTION_RULES.equals(metadataCode)) {
			component = new DocumentCopyRetentionRuleTable(retentionRuleVO, false, openPeriodsDDVList);
		} else if (PRINCIPAL_DEFAULT_DOCUMENT_COPY_RETENTION_RULE.equals(metadataCode)) {
			component = new DocumentDefaultCopyRetentionRuleTable(retentionRuleVO, false, openPeriodsDDVList);
		} else if (SECONDARY_DEFAULT_DOCUMENT_COPY_RETENTION_RULE.equals(metadataCode)) {
			component = null;
		} else if (DOCUMENT_TYPES_DETAILS.equals(metadataCode)) {
			component = new RetentionRuleDocumentTypeDisplay(retentionRuleVO);
		} else {
			component = super.build(recordVO, metadataValueVO);
		}
		return component;
	}

}
