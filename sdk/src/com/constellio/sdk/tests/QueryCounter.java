package com.constellio.sdk.tests;

import java.util.concurrent.atomic.AtomicInteger;

import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.extensions.AfterQueryParams;
import com.constellio.data.extensions.BigVaultServerExtension;

public class QueryCounter extends BigVaultServerExtension {

	private QueryCounterFilter filter;
	private AtomicInteger counter = new AtomicInteger();

	public QueryCounter(DataLayerFactory dataLayerFactory, final String name) {
		this.filter = new QueryCounterFilter() {
			@Override
			public boolean isCounted(AfterQueryParams params) {
				return params.getQueryName() != null && params.getQueryName().equals(name);
			}
		};
		dataLayerFactory.getExtensions().getSystemWideExtensions().bigVaultServerExtension.add(this);
	}

	public QueryCounter(DataLayerFactory dataLayerFactory, QueryCounterFilter filter) {
		this.filter = filter;
		dataLayerFactory.getExtensions().getSystemWideExtensions().bigVaultServerExtension.add(this);
	}

	@Override
	public void afterQuery(AfterQueryParams params) {
		if (filter.isCounted(params)) {
			counter.incrementAndGet();
		}
	}

	public int newQueryCalls() {
		int calls = counter.get();
		counter.set(0);
		return calls;
	}

	public void reset() {
		counter.set(0);
	}

	public interface QueryCounterFilter {

		boolean isCounted(AfterQueryParams params);
	}

	public static QueryCounterFilter ACCEPT_ALL = new QueryCounterFilter() {

		@Override
		public boolean isCounted(AfterQueryParams params) {
			return true;
		}
	};

	public static QueryCounterFilter ON_SCHEMA_TYPES(final String... schemaTypes) {
		return new QueryCounterFilter() {

			@Override
			public boolean isCounted(AfterQueryParams params) {
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
			}
		};
	}

	public static QueryCounterFilter ON_COLLECTION(final String collection) {
		return new QueryCounterFilter() {

			@Override
			public boolean isCounted(AfterQueryParams params) {
				for (String fq : params.getSolrParams().getParams("fq")) {
					if (fq.contains("collection_s:" + collection)) {
						return true;
					}

					if (fq.contains("collection_s:" + collection)) {
						return true;
					}

				}
				return false;
			}
		};
	}

}
