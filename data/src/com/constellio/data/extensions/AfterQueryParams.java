package com.constellio.data.extensions;

import org.apache.solr.common.params.SolrParams;

public interface AfterQueryParams {

	SolrParams getSolrParams();

	long getQtime();

	int getReturnedResultsCount();

	String getQueryName();

	boolean isGetByIdQuery();

}
