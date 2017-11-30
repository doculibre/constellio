package com.constellio.data.dao.services.bigVault.solr;

import java.util.Map;

public interface BigVaultServerCache {

	/**
	 * This service update values of keys if they are greater than the current value, no matter if the key is locked or not
	 * If the value of a key is lower than the stored version, the given value is ignored
	 *
	 * This service does not need to be transactionnal (or thread safe)
	 *
	 * @param versions A map ok keys and values to update
	 */
	void insertRecordVersion(Map<String, Long> versions);

	/**
	 * This service verify that keys have the given optimistic locking value.
	 *
	 * @param versions A map ok keys and values to update
	 */
	BigVaultServerCacheValidationResponse validateVersionsAndLock(Map<String, Long> recordsToLock);

	void unlockWithNewVersions(Map<String, Long> newVersions);

}
