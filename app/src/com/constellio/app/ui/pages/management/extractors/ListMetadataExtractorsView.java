package com.constellio.app.ui.pages.management.extractors;

import com.constellio.app.ui.entities.MetadataExtractorVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

import java.util.List;

public interface ListMetadataExtractorsView extends BaseView, AdminViewGroup {

	void setMetadataExtractorVOs(List<MetadataExtractorVO> metadataExtractorVOs);

	void removeMetadataExtractorVO(MetadataExtractorVO metadataExtractorVO);

}
