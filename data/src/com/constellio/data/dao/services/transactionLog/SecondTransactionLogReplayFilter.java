package com.constellio.data.dao.services.transactionLog;

import org.apache.solr.common.SolrInputDocument;

public interface SecondTransactionLogReplayFilter {

	boolean isReplayingAdd(String id, String schema, SolrInputDocument solrInputDocument);

	boolean isReplayingUpdate(String id, SolrInputDocument solrInputDocument);

}
