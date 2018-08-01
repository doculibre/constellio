package com.constellio.app.modules.rm.extensions.api;

import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public abstract class DecommissioningBuilderPresenterExtension {

	public abstract LogicalSearchCondition addAdditionalSearchFilters(AddAdditionalSearchFiltersParams params);

	public static class AddAdditionalSearchFiltersParams {
		private SearchType searchType;
		private LogicalSearchCondition condition;

		public AddAdditionalSearchFiltersParams(SearchType searchType, LogicalSearchCondition condition) {
			this.searchType = searchType;
			this.condition = condition;
		}

		public SearchType getSearchType() {
			return searchType;
		}

		public LogicalSearchCondition getCondition() {
			return condition;
		}
	}

}
