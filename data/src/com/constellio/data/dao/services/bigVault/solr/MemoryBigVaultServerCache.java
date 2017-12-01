package com.constellio.data.dao.services.bigVault.solr;

import static com.constellio.data.dao.services.bigVault.solr.MemoryBigVaultServerCacheUtils.validateOptimisticLocking;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MemoryBigVaultServerCache implements BigVaultServerCache {

	Map<String, Long> versions = new HashMap<>();

	Set<String> lockedIds = new HashSet<>();

	@Override
	public void insertRecordVersion(Map<String, Long> newVersions) {
		for (Map.Entry<String, Long> entry : newVersions.entrySet()) {
			Long vaultVersion = versions.get(entry.getKey());
			if (vaultVersion == null || entry.getValue() > vaultVersion) {
				versions.put(entry.getKey(), entry.getValue());
			}
		}
	}

	@Override
	public synchronized BigVaultServerCacheValidationResponse validateVersionsAndLock(Map<String, Long> recordsToLock) {

		Set<String> lockedInTransaction = new HashSet<>();
		Map<String, Long> keysWithBadVersionInTransaction = new HashMap<>();

		for (Map.Entry<String, Long> entry : recordsToLock.entrySet()) {

			if (lockedIds.contains(entry.getKey())) {
				lockedInTransaction.add(entry.getKey());
			}
			Long currentVaultVersion = versions.get(entry.getKey());
			if (!validateOptimisticLocking(currentVaultVersion, entry.getValue())) {
				keysWithBadVersionInTransaction.put(entry.getKey(), currentVaultVersion);
			}
		}

		if (lockedInTransaction.isEmpty() && keysWithBadVersionInTransaction.isEmpty()) {
			lockedIds.addAll(recordsToLock.keySet());
		}

		return new BigVaultServerCacheValidationResponse(keysWithBadVersionInTransaction, lockedInTransaction);
	}

	@Override
	public synchronized void unlockWithNewVersions(Map<String, Long> newVersions) {
		lockedIds.removeAll(newVersions.keySet());
		versions.putAll(newVersions);
	}

	@Override
	public void unlock(Set<String> recordsToUnlock) {
		lockedIds.removeAll(recordsToUnlock);
	}
}
