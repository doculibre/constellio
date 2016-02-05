package com.constellio.model.services.records.cache;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.params.SolrParams;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.extensions.BigVaultServerExtension;

public class StatsBigVaultServerExtension extends BigVaultServerExtension {

	public List<String> byIds = new ArrayList<>();
	public List<SolrParams> queries = new ArrayList<>();

	@Override
	public void afterUpdate(BigVaultServerTransaction transaction, long qtime) {
		super.afterUpdate(transaction, qtime);
	}

	static String GET_BY_ID_PREFIX = "id:";

	@Override
	public void afterQuery(SolrParams solrParams, long qtime) {

		String[] filterQueries = solrParams.getParams("fq");

		if (filterQueries.length == 1 && filterQueries[0].startsWith(GET_BY_ID_PREFIX)) {
			byIds.add(filterQueries[0].replace(GET_BY_ID_PREFIX, ""));
		}
		queries.add(solrParams);
	}

	public void clear() {
		byIds.clear();
		queries.clear();
	}
}
