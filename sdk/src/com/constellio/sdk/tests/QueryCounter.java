package com.constellio.sdk.tests;

import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.extensions.AfterQueryParams;
import com.constellio.data.extensions.BigVaultServerExtension;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class QueryCounter extends BigVaultServerExtension {

	private Function<AfterQueryParams, Boolean> filter;
	private AtomicInteger queryCounter = new AtomicInteger();
	private AtomicInteger returnedResultsCounter = new AtomicInteger();

	public QueryCounter(DataLayerFactory dataLayerFactory, final String name) {
		this.filter = params -> params.getQueryName() != null && params.getQueryName().equals(name);
		dataLayerFactory.getExtensions().getSystemWideExtensions().bigVaultServerExtension.add(this);
	}

	public QueryCounter(DataLayerFactory dataLayerFactory, Function<AfterQueryParams, Boolean> filter) {
		this.filter = filter;
		dataLayerFactory.getExtensions().getSystemWideExtensions().bigVaultServerExtension.add(this);
	}

	@Override
	public void afterQuery(AfterQueryParams params) {
		if (!isAlwaysExcludedQueryName(params.getQueryName()) && filter.apply(params)) {
			queryCounter.incrementAndGet();
			returnedResultsCounter.addAndGet(params.getReturnedResultsCount());
		}
	}

	private boolean isAlwaysExcludedQueryName(String queryName) {
		return queryName != null && queryName.contains("*SDK*");
	}

	public int newQueryCalls() {
		int calls = queryCounter.get();
		queryCounter.set(0);
		return calls;
	}

	public int newQueryReturnedResultsCount() {
		int calls = returnedResultsCounter.get();
		returnedResultsCounter.set(0);
		return calls;
	}

	public void reset() {
		queryCounter.set(0);
		returnedResultsCounter.set(0);
	}

	public static Function<AfterQueryParams, Boolean> ON_SCHEMA_TYPES(final String... schemaTypes) {
		return params -> {
			for (String fq : params.getSolrParams().getParams("fq")) {
				for (String schemaType : schemaTypes) {
					if (fq.equals("schema_s:" + schemaType + "_*")) {
						return true;
					}

					if (fq.equals("schema_s:" + schemaType)) {
						return true;
					}

				}
			}
			return false;
		};
	}

	public static Function<AfterQueryParams, Boolean> ON_COLLECTION(final String collection) {
		return params -> {
			for (String fq : params.getSolrParams().getParams("fq")) {
				if (fq.contains("collection_s:" + collection)) {
					return true;
				}

				if (fq.contains("collection_s:" + collection)) {
					return true;
				}

			}
			return false;
		};
	}

}
