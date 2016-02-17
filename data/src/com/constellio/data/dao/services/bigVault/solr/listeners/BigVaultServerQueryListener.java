package com.constellio.data.dao.services.bigVault.solr.listeners;

import org.apache.solr.client.solrj.response.QueryResponse;

public interface BigVaultServerQueryListener extends BigVaultServerListener{
	void onQuery(QueryResponse response);
}
