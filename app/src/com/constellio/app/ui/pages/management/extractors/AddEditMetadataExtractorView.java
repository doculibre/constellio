package com.constellio.app.ui.pages.management.extractors;

import java.util.List;

import com.constellio.app.ui.entities.MetadataExtractorVO;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface AddEditMetadataExtractorView extends BaseView, AdminViewGroup {

	void setMetadataExtractorVO(MetadataExtractorVO metadataExtractorVO);

	void setSchemaTypeOptions(List<MetadataSchemaTypeVO> schemaTypeVOs);

	void setSchemaOptions(List<MetadataSchemaVO> schemaVOs);

	void setMetadataOptions(List<MetadataVO> metadataVOs);

	void setRegexMetadataOptions(List<MetadataVO> metadataVOs);

	void setSchemaTypeFieldVisible(boolean visible);

	void setSchemaFieldVisible(boolean visible);

	void setMetadataFieldEnabled(boolean enabled);

}
