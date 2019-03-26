package com.constellio.app.api.extensions.taxonomies;

import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.SolrQueryBuilderContext;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Constelio on 2016-10-19.
 */
public class UserSearchEvent {

	private SPEQueryResponse speQueryResponse;

	private LogicalSearchQuery query;

	private SavedSearch savedSearch;

	private LocalDateTime queryDateTime;

	private String language;

	private String username;

	public UserSearchEvent(SPEQueryResponse speQueryResponse, LogicalSearchQuery query, SavedSearch savedSearch,
						   LocalDateTime queryDateTime, String language, String username) {
		this.speQueryResponse = speQueryResponse;
		this.query = query;
		this.savedSearch = savedSearch;
		this.queryDateTime = queryDateTime;
		this.language = language;
		this.username = username;
	}

	public SPEQueryResponse getSpeQueryResponse() {
		return speQueryResponse;
	}

	public LogicalSearchQuery getQuery() {
		return new LogicalSearchQuery(query);
	}

	public LocalDateTime getQueryDateTime() {
		return queryDateTime;
	}

	public SavedSearch getSavedSearch() {
		return savedSearch;
	}

	public String getUserID() {
		return savedSearch.getUser();
	}

	public String getUsername() {
		return username;
	}

	public String getCollection() {
		return savedSearch.getCollection();
	}

	public long getNumFound() {
		return speQueryResponse.getNumFound();
	}

	public long getQtime() {
		return speQueryResponse.getQtime();
	}

	public String getSolrQuery() {
		return query.getCondition().getSolrQuery(new SolrQueryBuilderContext(true, new ArrayList<>(), language, null, null, null));
	}

	public List<Criterion> getCriterionList() {
		return savedSearch.getAdvancedSearch();
	}

	public String getCriterionListAsString() {
		List<Criterion> criterionList = getCriterionList();
		StringBuilder sb = new StringBuilder();
		for (Criterion criterion : criterionList) {
			sb.append(convertCriterionToString(criterion));
		}
		return sb.toString();
	}

	private String convertCriterionToString(Criterion criterion) {
		StringBuilder sb = new StringBuilder();
		if (criterion.isLeftParens()) {
			sb.append("(");
		}
		sb.append(new SchemaUtils().getLocalCodeFromMetadataCode(criterion.getMetadataCode()));
		sb.append(" " + criterion.getSearchOperator().toString() + " ");
		sb.append(criterion.getValue());
		if (criterion.isRightParens()) {
			sb.append(")");
		}
		sb.append(" " + criterion.getBooleanOperator().toString() + " ");
		return sb.toString();
	}

	public String getLanguage() {
		return language;
	}

	public UserSearchEvent setLanguage(String language) {
		this.language = language;
		return this;
	}
}
