package com.constellio.model.services.search.query.list;

import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.VisibilityStatusFilter;
import com.constellio.model.services.search.query.SearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery.DefaultUserFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery.UserFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuerySort;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;

public class RecordListSearchQuery implements SearchQuery {

	private List<Record> records;
	private List<String> recordIds;

	private int startRow;
	private List<UserFilter> userFilters = new ArrayList<>();
	private List<String> statisticFields = new ArrayList<>();
	private List<String> fieldFacets = new ArrayList<>();
	private KeySetMap<String, String> queryFacets = new KeySetMap<>();
	private String language;
	private VisibilityStatusFilter visibilityStatusFilter;
	private StatusFilter statusFilter;
	private List<LogicalSearchQuerySort> sortFields;

	public static RecordListSearchQuery createFromRecords(List<Record> records) {
		RecordListSearchQuery query = new RecordListSearchQuery();
		query.records = new ArrayList<>(records);

		return query;
	}

	public static RecordListSearchQuery createFromIds(List<String> recordIds) {
		RecordListSearchQuery query = new RecordListSearchQuery();
		query.recordIds = recordIds;

		return query;
	}

	public List<Record> getRecords() {
		return records;
	}

	public List<String> getRecordIds() {
		return recordIds;
	}

	public boolean isListOfIds() {
		return recordIds != null;
	}

	public RecordListSearchQuery convertIdsToSummaryRecords(ModelLayerFactory modelLayerFactory) {
		return convertIdsToSummaryRecords(modelLayerFactory.newRecordServices());
	}

	public RecordListSearchQuery convertIdsToSummaryRecords(RecordServices recordServices) {
		if (!isListOfIds()) {
			return this;
		}

		records = new ArrayList<>();
		recordIds.forEach(id -> records.add(recordServices.realtimeGetRecordSummaryById(id)));
		recordIds = null;

		return this;
	}

	public RecordListSearchQuery convertIdsToRecords(ModelLayerFactory modelLayerFactory) {
		return convertIdsToRecords(modelLayerFactory.newRecordServices());
	}

	public RecordListSearchQuery convertIdsToRecords(RecordServices recordServices) {
		if (!isListOfIds()) {
			return this;
		}

		records = new ArrayList<>();
		recordIds.forEach(id -> records.add(recordServices.realtimeGetRecordById(id)));
		recordIds = null;

		return this;
	}

	@Override
	public List<String> getFilterQueries() {
		return null;
	}

	@Override
	public int getStartRow() {
		return startRow;
	}

	@Override
	public RecordListSearchQuery setStartRow(int row) {
		startRow = row;
		return this;
	}

	@Override
	public int getNumberOfRows() {
		return 0;
	}

	@Override
	public RecordListSearchQuery setNumberOfRows(int number) {
		return this;
	}

	@Override
	public RecordListSearchQuery filteredWith(UserFilter userFilter) {
		userFilters = asList(userFilter);
		return this;
	}

	@Override
	public RecordListSearchQuery filteredWithUser(User user) {
		return filteredWithUser(user, Role.READ);
	}

	@Override
	public RecordListSearchQuery filteredWithUser(User user, String access) {
		if (user == null) {
			throw new IllegalArgumentException("user required");
		}
		if (access == null) {
			throw new IllegalArgumentException("access/permission required");
		}
		userFilters = asList((UserFilter) new DefaultUserFilter(user, access));
		return this;
	}

	@Override
	public RecordListSearchQuery filteredWithUser(User user, List<String> accessOrPermissions) {
		if (user == null) {
			throw new IllegalArgumentException("user required");
		}
		if (accessOrPermissions == null || accessOrPermissions.isEmpty()) {
			throw new IllegalArgumentException("access/permission required");
		}

		userFilters = new ArrayList<>();
		for (String accessOrPermission : accessOrPermissions) {
			userFilters.add(new DefaultUserFilter(user, accessOrPermission));
		}

		return this;
	}

	public SearchQuery filteredByStatus(StatusFilter status) {
		statusFilter = status;
		return this;
	}


	@Override
	public RecordListSearchQuery computeStatsOnField(DataStoreField metadata) {
		this.statisticFields.add(metadata.getDataStoreCode());
		return this;
	}

	@Override
	public RecordListSearchQuery sortAsc(DataStoreField metadata) {
		return this;
	}

	@Override
	public RecordListSearchQuery sortDesc(DataStoreField metadata) {
		return this;
	}

	@Override
	public RecordListSearchQuery clone() {
		RecordListSearchQuery query = new RecordListSearchQuery();
		query.records = records;
		query.recordIds = recordIds;
		query.startRow = startRow;
		query.statisticFields = statisticFields;
		query.userFilters = userFilters;
		query.visibilityStatusFilter = visibilityStatusFilter;
		query.sortFields = sortFields;

		return query;
	}

	@Override
	public void clearSort() {

	}

	@Override
	public RecordListSearchQuery setLanguage(String language) {
		this.language = language;
		return this;
	}

	@Override
	public RecordListSearchQuery setLanguage(Locale locale) {
		this.language = locale.getLanguage();
		return this;
	}

	public String getLanguage() {
		return language;
	}

	@Override
	public List<String> getFieldFacets() {
		return fieldFacets;
	}

	public RecordListSearchQuery setFieldFacets(List<String> fieldFacets) {
		this.fieldFacets = fieldFacets;
		return this;
	}

	@Override
	public KeySetMap<String, String> getQueryFacets() {
		return queryFacets;
	}

	public RecordListSearchQuery setQueryFacets(KeySetMap<String, String> queryFacets) {
		this.queryFacets = queryFacets;
		return this;
	}

	@Override
	public void clearFacets() {
		fieldFacets.clear();
		queryFacets.clear();
	}

	public List<UserFilter> getUserFilters() {
		return userFilters;
	}

	public VisibilityStatusFilter getVisibilityStatusFilter() {
		return visibilityStatusFilter;
	}

	@Override
	public StatusFilter getStatusFilter() {
		return statusFilter;
	}

	@Override
	public List<LogicalSearchQuerySort> getSortFields() {
		return sortFields;
	}
}
