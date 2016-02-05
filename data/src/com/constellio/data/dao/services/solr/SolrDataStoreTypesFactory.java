package com.constellio.data.dao.services.solr;

import com.constellio.data.dao.services.DataStoreTypesFactory;

public class SolrDataStoreTypesFactory implements DataStoreTypesFactory {

	@Override
	public String forString(boolean multivalue) {
		return multivalue ? "ss" : "s";
	}

	@Override
	public String forText(boolean multivalue) {
		return multivalue ? "txt" : "t";
	}

	@Override
	public String forDouble(boolean multivalue) {
		return multivalue ? "ds" : "d";
	}

	@Override
	public String forDate(boolean multivalue) {
		return multivalue ? "das" : "da";
	}

	@Override
	public String forDateTime(boolean multivalue) {
		return multivalue ? "dts" : "dt";
	}

	// We use a string Solr type for boolean fields, because null is not possible in a Solr boolean.
	@Override
	public String forBoolean(boolean multivalue) {
		return multivalue ? "ss" : "s";
	}

}
