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
package com.constellio.model.services.taxonomies;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.SPEQueryResponse;

public class LinkableTaxonomySearchResponse {

	private long numFound;

	private List<TaxonomySearchRecord> records;

	private long qTime;

	public LinkableTaxonomySearchResponse(long numFound,
			List<TaxonomySearchRecord> records) {
		this.numFound = numFound;
		this.records = records;
	}

	public long getNumFound() {
		return numFound;
	}

	public long getQTime() {
		return qTime;
	}

	public List<TaxonomySearchRecord> getRecords() {
		return records;
	}

	public static LinkableTaxonomySearchResponse forLinkableRecords(SPEQueryResponse speQueryResponse) {
		return forLinkableRecords(speQueryResponse.getRecords(), speQueryResponse.getNumFound());
	}

	public static LinkableTaxonomySearchResponse forLinkableRecords(List<Record> linkableRecords, long numFound) {
		List<TaxonomySearchRecord> records = new ArrayList<>();
		for (Record linkableRecord : linkableRecords) {
			records.add(new TaxonomySearchRecord(linkableRecord, true));
		}
		return new LinkableTaxonomySearchResponse(numFound, records);
	}

	public LinkableTaxonomySearchResponse withQTime(long qTime) {
		this.qTime = qTime;
		return this;
	}
}
