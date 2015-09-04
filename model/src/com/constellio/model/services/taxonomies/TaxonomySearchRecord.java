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

import com.constellio.model.entities.records.Record;

public class TaxonomySearchRecord {

	private Record record;
	private boolean linkable;
	private boolean hasChildren;

	public TaxonomySearchRecord(Record record, boolean linkable, boolean hasChildren) {
		this.record = record;
		this.linkable = linkable;
		this.hasChildren = hasChildren;
	}

	public Record getRecord() {
		return record;
	}

	public boolean isLinkable() {
		return linkable;
	}

	@Override
	public String toString() {
		if (linkable) {
			return "Linkable '" + record + "'";
		} else {
			return "Not linkable '" + record + "'";

		}
	}

	public boolean hasChildren() {
		return hasChildren;
	}

	public String getId() {
		return record.getId();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		TaxonomySearchRecord that = (TaxonomySearchRecord) o;

		if (hasChildren != that.hasChildren) {
			return false;
		}
		if (linkable != that.linkable) {
			return false;
		}
		if (record != null ? !record.equals(that.record) : that.record != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = record != null ? record.hashCode() : 0;
		result = 31 * result + (linkable ? 1 : 0);
		result = 31 * result + (hasChildren ? 1 : 0);
		return result;
	}
}
