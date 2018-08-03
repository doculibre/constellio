package com.constellio.app.modules.rm.extensions.api;

import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public abstract class AdvancedSearchPresenterExtension {

	public abstract LogicalSearchQuery addAdditionalSearchQueryFilters(AddAdditionalSearchQueryFiltersParams params);

	public static class AddAdditionalSearchQueryFiltersParams {
		private LogicalSearchQuery query;
		private String schemaTypeCode;

		public AddAdditionalSearchQueryFiltersParams(LogicalSearchQuery query, String schemaTypeCode) {
			this.query = query;
			this.schemaTypeCode = schemaTypeCode;
		}

		public LogicalSearchQuery getQuery() {
			return query;
		}

		public String getSchemaTypeCode() {
			return schemaTypeCode;
		}
	}

}
