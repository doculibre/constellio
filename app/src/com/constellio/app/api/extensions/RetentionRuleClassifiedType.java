package com.constellio.app.api.extensions;

import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;

public interface RetentionRuleClassifiedType {

	MetadataSchemaTypeVO getSchemaType();

	RecordVODataProvider getDataProvider();

	String getCountLabel();

	String getTabLabel();
}
