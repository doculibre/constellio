package com.constellio.model.services.search.query.list;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.VisibilityStatusFilter;
import com.constellio.model.services.search.query.BaseSearchQueryImplementation;
import com.constellio.model.services.search.query.SearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery.UserFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuerySort;

import java.util.ArrayList;
import java.util.List;

public class RecordListSearchQuery extends BaseSearchQueryImplementation implements SearchQuery {

	private List<Record> records;
	private List<String> recordIds;

	private int startRow;
	private List<UserFilter> userFilters = new ArrayList<>();
	private List<String> statisticFields = new ArrayList<>();
	private VisibilityStatusFilter visibilityStatusFilter;
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
		return 0;
	}

	@Override
	public SearchQuery setStartRow(int row) {
		return null;
	}

	@Override
	public int getNumberOfRows() {
		return isListOfIds() ? recordIds.size() : records.size();
	}

	@Override
	public RecordListSearchQuery setNumberOfRows(int number) {
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

}
