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

public class TaxonomySearchRecord {

	private Record record;
	private boolean linkable;

	public TaxonomySearchRecord(Record record, boolean linkable) {
		this.record = record;
		this.linkable = linkable;
	}

	public Record getRecord() {
		return record;
	}

	public boolean isLinkable() {
		return linkable;
	}

	public static List<TaxonomySearchRecord> forRecords(List<Record> childs, boolean linkable) {

		List<TaxonomySearchRecord> taxonomySearchRecords = new ArrayList<>();

		for (Record child : childs) {
			taxonomySearchRecords.add(new TaxonomySearchRecord(child, linkable));
		}

		return taxonomySearchRecords;
	}

}
