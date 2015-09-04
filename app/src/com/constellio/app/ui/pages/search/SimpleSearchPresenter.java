/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.search;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.NoSuchMetadataWithAtomicCode;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class SimpleSearchPresenter extends SearchPresenter<SimpleSearchView> {
	private int pageNumber;
	private String searchExpression;

	public SimpleSearchPresenter(SimpleSearchView view) {
		super(view);
	}

	@Override
	public SimpleSearchPresenter forRequestParameters(String params) {
		if (StringUtils.isNotBlank(params)) {
			String[] parts = params.split("/", 3);
			pageNumber = parts.length == 3 ? Integer.parseInt(parts[2]) : 1;
			if ("s".equals(parts[0])) {
				SavedSearch search = getSavedSearch(parts[1]);
				searchExpression = search.getFreeTextSearch();
				facetSelections.putAll(search.getSelectedFacets());
				sortCriterion = search.getSortField();
				sortOrder = SortOrder.valueOf(search.getSortOrder().name());
			} else {
				searchExpression = parts[1];
			}
		} else {
			searchExpression = "";
		}
		return this;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	@Override
	public boolean mustDisplayResults() {
		return !searchExpression.isEmpty();
	}

	@Override
	public int getPageNumber() {
		return pageNumber;
	}

	@Override
	public String getUserSearchExpression() {
		return searchExpression;
	}

	@Override
	public void suggestionSelected(String suggestion) {
		view.navigateTo().simpleSearch(suggestion);
	}

	@Override
	public List<MetadataVO> getMetadataAllowedInSort() {
		List<MetadataSchemaType> schemaTypes = allowedSchemaTypes();
		switch (schemaTypes.size()) {
		case 0:
			return new ArrayList<>();
		case 1:
			return getMetadataAllowedInSort(schemaTypes.get(0).getCode());
		default:
			return getCommonMetadataAllowedInSort(schemaTypes);
		}
	}

	private List<MetadataVO> getCommonMetadataAllowedInSort(List<MetadataSchemaType> schemaTypes) {
		List<MetadataVO> result = new ArrayList<>();
		for (MetadataVO metadata : getMetadataAllowedInSort(schemaTypes.get(0))) {
			String localCode = MetadataVO.getCodeWithoutPrefix(metadata.getCode());
			if (isMetadataInAllTypes(localCode, schemaTypes)) {
				result.add(metadata);
			}
		}
		return result;
	}

	private boolean isMetadataInAllTypes(String localCode, List<MetadataSchemaType> types) {
		for (MetadataSchemaType each : types) {
			try {
				each.getMetadataWithAtomicCode(localCode);
			} catch (NoSuchMetadataWithAtomicCode e) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected LogicalSearchCondition getSearchCondition() {
		if (allowedSchemaTypes().isEmpty()) {
			return fromAllSchemasIn(view.getCollection()).returnAll();
		} else {
			return from(allowedSchemaTypes()).returnAll();
		}
	}

	@Override
	protected SavedSearch prepareSavedSearch(SavedSearch search) {
		return search.setSearchType(SimpleSearchView.SEARCH_TYPE).setFreeTextSearch(searchExpression);
	}

	private List<MetadataSchemaType> allowedSchemaTypes() {
		List<MetadataSchemaType> result = new ArrayList<>();
		for (MetadataSchemaType type : types().getSchemaTypes()) {
			SchemaTypeDisplayConfig config = schemasDisplayManager()
					.getType(view.getSessionContext().getCurrentCollection(), type.getCode());
			if (config.isSimpleSearch()) {
				result.add(type);
			}
		}
		return result;
	}
}
