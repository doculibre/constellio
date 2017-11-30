package com.constellio.data.dao.services.bigVault.solr;

public class MemoryBigVaultServerCacheUtils {

	public static boolean validateOptimisticLocking(Long currentVaultVersion, Long expected) {
		if (expected == null || expected.equals(0L)) {
			return true;

		} else if (expected.equals(-1L)) {
			return currentVaultVersion == null;

		} else if (expected.equals(1L)) {
			return currentVaultVersion != null;

		} else {
			return expected.equals(currentVaultVersion);

		}
	}

}
