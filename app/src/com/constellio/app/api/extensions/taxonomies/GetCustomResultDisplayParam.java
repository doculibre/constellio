package com.constellio.app.api.extensions.taxonomies;

import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;

public class GetCustomResultDisplayParam {

	SearchResultVO searchResultVO;
	MetadataDisplayFactory componentFactory;

	public GetCustomResultDisplayParam(SearchResultVO searchResultVO,
			MetadataDisplayFactory componentFactory) {
		this.searchResultVO = searchResultVO;
		this.componentFactory = componentFactory;
	}

	public String getSchemaType() {
		return searchResultVO.getRecordVO().getSchema().getTypeCode();
	}

	public SearchResultVO getSearchResultVO() {
		return searchResultVO;
	}

	public MetadataDisplayFactory getComponentFactory() {
		return componentFactory;
	}
}
