package com.constellio.app.modules.rm.ui.components;

import com.constellio.app.modules.rm.ui.components.retentionRule.RetentionRuleReferenceDisplay;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.vaadin.ui.Component;

public class RMMetadataDisplayFactory extends MetadataDisplayFactory {

	@Override
	public Component buildSingleValue(RecordVO recordVO, MetadataVO metadata, Object displayValue) {
		Component displayComponent;

		String schemaTypeCode = metadata.getSchemaTypeCode();
		MetadataValueType metadataValueType = metadata.getType();

		if (MetadataValueType.REFERENCE.equals(metadataValueType) && RetentionRule.SCHEMA_TYPE.equals(schemaTypeCode)) {
			displayComponent = new RetentionRuleReferenceDisplay(displayValue.toString());
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
