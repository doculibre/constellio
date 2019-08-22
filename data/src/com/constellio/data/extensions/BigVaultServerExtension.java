package com.constellio.data.extensions;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import org.apache.solr.common.params.SolrParams;

public class BigVaultServerExtension {

	@Deprecated
	public void afterQuery(SolrParams solrParams, long qtime) {
	}

	public void afterQuery(AfterQueryParams params) {
	}

	public void afterRealtimeGetById(AfterGetByIdParams params) {
	}

	public void afterGetById(AfterGetByIdParams params) {
	}


	public void afterUpdate(BigVaultServerTransaction transaction, long qtime) {
	}

	public void afterCommit(BigVaultServerTransaction transaction, long qtime) {
	}
}
