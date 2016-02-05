package com.constellio.app.ui.pages.home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.query.ResultsProjection;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class SortRecordsUsingIdsAndApplyViewDateResultsProjection implements ResultsProjection {

	Map<String, LocalDateTime> eventsViewDateTimes;
	List<String> recordIds;

	public SortRecordsUsingIdsAndApplyViewDateResultsProjection(List<String> recordIds,
			Map<String, LocalDateTime> eventsViewDateTimes) {
		this.recordIds = recordIds;
		this.eventsViewDateTimes = eventsViewDateTimes;
	}

	@Override
	public SPEQueryResponse project(LogicalSearchQuery query, SPEQueryResponse originalResponse) {
		final List<Record> records = new ArrayList<>(originalResponse.getRecords());

		for (Record record : records) {
			LocalDateTime view = eventsViewDateTimes.get(record.getId());
			record.set(Schemas.MODIFIED_ON, view);
		}

		Collections.sort(records, new Comparator<Record>() {
			@Override
			public int compare(Record r1, Record r2) {
				int r1Index = recordIds.indexOf(r1.getId());
				int r2Index = recordIds.indexOf(r2.getId());
				return new Integer(r1Index).compareTo(r2Index);
			}
		});

		return new SPEQueryResponse(records, new HashMap<Record, Map<Record, Double>>());
	}
}
