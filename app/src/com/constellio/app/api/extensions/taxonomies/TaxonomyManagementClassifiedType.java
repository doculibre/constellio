package com.constellio.app.api.extensions.taxonomies;

import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;

public interface TaxonomyManagementClassifiedType {

	MetadataSchemaTypeVO getSchemaType();

	RecordVODataProvider getDataProvider();

	String getCountLabel();

	String getTabLabel();
}
