package com.constellio.model.services.records.cache;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.extensions.AfterGetByIdParams;
import com.constellio.data.extensions.BigVaultServerExtension;
import org.apache.solr.common.params.SolrParams;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

	@Override
	public void afterRealtimeGetById(AfterGetByIdParams solrParams) {
		byIds.add(solrParams.getId());
		// FIXME create better solrParams ?
		queries.add(new SolrParams() {
			@Override
			public String get(String param) {
				return null;
			}

			@Override
			public String[] getParams(String param) {
				return new String[0];
			}

			@Override
			public Iterator<String> getParameterNamesIterator() {
				return null;
			}
		});
	}

	public void clear() {
		byIds.clear();
		queries.clear();
	}
}
