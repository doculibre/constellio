package com.constellio.sdk.tests;

import java.util.concurrent.atomic.AtomicInteger;

import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.extensions.AfterQueryParams;
import com.constellio.data.extensions.BigVaultServerExtension;

public class QueryCounter extends BigVaultServerExtension {

	private String name;
	private AtomicInteger counter = new AtomicInteger();

	public QueryCounter(DataLayerFactory dataLayerFactory, String name) {
		this.name = name;
		dataLayerFactory.getExtensions().getSystemWideExtensions().bigVaultServerExtension.add(this);
	}

	@Override
	public void afterQuery(AfterQueryParams params) {
		if (params.getQueryName() != null && params.getQueryName().equals(name)) {
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
}
