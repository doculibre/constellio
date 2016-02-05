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
