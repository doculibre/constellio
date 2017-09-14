package com.constellio.data.extensions;

import org.apache.solr.common.params.SolrParams;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;

public class BigVaultServerExtension {

	@Deprecated
	public void afterQuery(SolrParams solrParams, long qtime) {
	}

	public void afterQuery(AfterQueryParams params) {
	}

	public void afterUpdate(BigVaultServerTransaction transaction, long qtime) {
	}

	public void afterCommit(BigVaultServerTransaction transaction, long qtime) {
	}
}
