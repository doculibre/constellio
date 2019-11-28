package com.constellio.app.modules.rm.ui.components;

import com.constellio.app.modules.rm.ui.components.retentionRule.AdministrativeUnitReferenceDisplay;
import com.constellio.app.modules.rm.ui.components.retentionRule.CategoryReferenceDisplay;
import com.constellio.app.modules.rm.ui.components.retentionRule.RetentionRuleReferenceDisplay;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

import java.util.ArrayList;
import java.util.List;

public class RMMetadataDisplayFactory extends MetadataDisplayFactory {

	@Override
	public Component build(RecordVO recordVO, MetadataValueVO metadataValue) {
		Component displayComponent;
		MetadataVO metadataVO = metadataValue.getMetadata();
		if (metadataVO.codeMatches(Folder.KEYWORDS) || metadataVO.codeMatches(Document.KEYWORDS)) {
			StringBuilder sb = new StringBuilder();
			List<String> keywords = new ArrayList<>();
			if (metadataValue.getValue() != null && metadataValue.getValue() instanceof String) {
				keywords.add(metadataValue.getValue().toString());
			} else {
				keywords = metadataValue.getValue();
			}
			if (keywords != null) {
				for (String keyword : keywords) {
					if (sb.length() > 0) {
						sb.append(", ");
					}
					sb.append(keyword);
				}
			}
			if (sb.length() > 0) {
				displayComponent = new Label(sb.toString());
			} else {
				displayComponent = null;
			}
		} else {
			displayComponent = super.build(recordVO, metadataValue);
		}
		return displayComponent;
	}

	@Override
	public Component buildSingleValue(RecordVO recordVO, MetadataVO metadata, Object displayValue) {
		Component displayComponent;

		String schemaTypeCode = metadata.getSchemaTypeCode();
		MetadataValueType metadataValueType = metadata.getType();

		if (MetadataValueType.REFERENCE.equals(metadataValueType) && RetentionRule.SCHEMA_TYPE.equals(schemaTypeCode)) {
			displayComponent = new RetentionRuleReferenceDisplay(displayValue.toString());
		} else if (MetadataValueType.REFERENCE.equals(metadataValueType) && AdministrativeUnit.SCHEMA_TYPE
				.equals(schemaTypeCode)) {
			displayComponent = new AdministrativeUnitReferenceDisplay(displayValue.toString());
		} else if (MetadataValueType.REFERENCE.equals(metadataValueType) && Category.SCHEMA_TYPE
				.equals(schemaTypeCode)) {
			displayComponent = new CategoryReferenceDisplay(displayValue.toString());
		} else {
			displayComponent = super.buildSingleValue(recordVO, metadata, displayValue);
		}
		return displayComponent;
	}

	//	@Override
	//	protected Component newContentVersionDisplayComponent(RecordVO recordVO, ContentVersionVO contentVersionVO) {
	//		Component displayComponent;
	//		String agentURL = ConstellioAgentUtils.getAgentURL(recordVO, contentVersionVO);
	//		if (agentURL != null) {
	//			displayComponent = new ConstellioAgentLink(agentURL, contentVersionVO);
	//		} else {
	//			displayComponent = new DownloadContentVersionLink(contentVersionVO);
	//		}
	//		return displayComponent;
	//	}

}
