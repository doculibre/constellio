package com.constellio.data.dao.services.bigVault.solr.listeners;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;

public interface BigVaultServerQueryListener extends BigVaultServerListener{
	void onQuery(SolrParams params, QueryResponse response);
}
