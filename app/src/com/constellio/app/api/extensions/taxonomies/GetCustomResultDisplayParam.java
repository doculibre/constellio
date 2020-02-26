package com.constellio.app.api.extensions.taxonomies;

import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;

public class GetCustomResultDisplayParam {

	SearchResultVO searchResultVO;
	MetadataDisplayFactory componentFactory;
	String query;

	public GetCustomResultDisplayParam(SearchResultVO searchResultVO,
									   MetadataDisplayFactory componentFactory, String query) {
		this.searchResultVO = searchResultVO;
		this.componentFactory = componentFactory;
		this.query = query;
	}

	public String getSchemaType() {
		return searchResultVO.getRecordVO() != null ? searchResultVO.getRecordVO().getSchema().getTypeCode() : "";
	}

	public SearchResultVO getSearchResultVO() {
		return searchResultVO;
	}

	public MetadataDisplayFactory getComponentFactory() {
		return componentFactory;
	}

	public String getQuery() {
		return query;
	}
}
