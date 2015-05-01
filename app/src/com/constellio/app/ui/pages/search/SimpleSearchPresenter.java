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

import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.google.common.base.Strings;

public class SimpleSearchPresenter extends SearchPresenter<SimpleSearchView> {
	private int pageNumber;
	private String searchExpression;

	public SimpleSearchPresenter(SimpleSearchView view) {
		super(view);
	}

	@Override
	public SimpleSearchPresenter forRequestParameters(String params) {
		if (Strings.isNullOrEmpty(params)) {
			searchExpression = "";
			pageNumber = 0;
		} else {
			String[] parts = params.split("/", 2);
			searchExpression = parts[0];
			pageNumber = parts.length == 2 ? Integer.parseInt(parts[1]) : 1;
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
	protected LogicalSearchCondition getSearchCondition() {
		if (allowedSchemaTypes().isEmpty()) {
			return fromAllSchemasIn(view.getCollection()).returnAll();
		} else {
			return from(allowedSchemaTypes()).returnAll();
		}
	}

	private List<MetadataSchemaType> allowedSchemaTypes() {
		List<MetadataSchemaType> result = new ArrayList<>();
		for (MetadataSchemaType type : types().getSchemaTypes()) {
			SchemaTypeDisplayConfig config = schemasDisplayManager().getType(view.getCollection(), type.getCode());
			if (config.isSimpleSearch()) {
				result.add(type);
			}
		}
		return result;
	}
}
