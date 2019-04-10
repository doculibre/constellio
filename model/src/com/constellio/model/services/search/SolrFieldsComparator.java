package com.constellio.model.services.search;

import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SolrFieldsComparator<T> implements Comparator<T> {

	List<DataStoreField> fields = new ArrayList<>();
	List<Boolean> directions = new ArrayList<>();

	private SolrFieldsComparator(DataStoreField field, boolean direction) {
		fields.add(field);
		directions.add(direction);
	}

	@Override
	public int compare(T o1, T o2) {
		//Already compared by solr
		return -1;
	}

	public static <T> SolrFieldsComparator<T> asc(Metadata metadata) {
		return new SolrFieldsComparator(metadata, true);
	}

	public static <T> SolrFieldsComparator<T> desc(Metadata metadata) {
		return new SolrFieldsComparator(metadata, false);
	}

	public SolrFieldsComparator thenAsc(Metadata metadata) {
		this.fields.add(metadata);
		this.directions.add(true);
		return this;
	}

	public SolrFieldsComparator thenDesc(Metadata metadata) {
		this.fields.add(metadata);
		this.directions.add(false);
		return this;
	}

	public List<DataStoreField> getFields() {
		return fields;
	}

	public List<Boolean> getDirections() {
		return directions;
	}
}
