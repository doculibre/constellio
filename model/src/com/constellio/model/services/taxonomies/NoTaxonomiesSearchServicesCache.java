package com.constellio.model.services.taxonomies;

public class NoTaxonomiesSearchServicesCache implements TaxonomiesSearchServicesCache {

	@Override
	public synchronized void insert(String userId, String recordId, Boolean value) {
	}

	@Override
	public synchronized void invalidateAll() {
	}

	@Override
	public synchronized void invalidateWithChildren(String recordId) {
	}

	@Override
	public synchronized void invalidateWithoutChildren(String recordId) {
	}

	@Override
	public synchronized void invalidateRecord(String recordId) {
	}

	@Override
	public synchronized void invalidateUser(String userId) {
	}

	@Override
	public synchronized Boolean getCachedValue(String userId, String recordId) {
		return null;
	}

}
