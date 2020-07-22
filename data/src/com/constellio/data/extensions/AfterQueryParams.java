package com.constellio.data.extensions;

import org.apache.solr.common.params.SolrParams;

import java.util.Map;

public interface AfterQueryParams {

	SolrParams getSolrParams();

	long getQtime();

	int getReturnedResultsCount();

	String getQueryName();

	boolean isGetByIdQuery();

	Map<String, Object> getDebugMap();
}
