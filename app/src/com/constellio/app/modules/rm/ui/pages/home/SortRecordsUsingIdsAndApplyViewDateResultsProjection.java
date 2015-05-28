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
package com.constellio.app.modules.rm.ui.pages.home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

		Collections.sort(records, new Comparator<Record>() {
			@Override
			public int compare(Record r1, Record r2) {
				int r1Index = recordIds.indexOf(r1.getId());
				int r2Index = recordIds.indexOf(r2.getId());
				return new Integer(r1Index).compareTo(r2Index);
			}
		});

		for (Record record : records) {
			LocalDateTime view = eventsViewDateTimes.get(record.getId());
			record.set(Schemas.MODIFIED_ON, view);
		}

		return new SPEQueryResponse(records);
	}
}
