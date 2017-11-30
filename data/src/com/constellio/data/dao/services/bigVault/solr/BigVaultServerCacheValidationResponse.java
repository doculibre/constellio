package com.constellio.data.dao.services.bigVault.solr;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class BigVaultServerCacheValidationResponse {

	public static BigVaultServerCacheValidationResponse ACCEPTED = new BigVaultServerCacheValidationResponse(
			Collections.<String, Long>emptyMap(), Collections.<String>emptySet());

	Map<String, Long> keysWithBadVersionAndTheirExpectedVersion;

	Set<String> lockedKeys;

	boolean accepted;

	public BigVaultServerCacheValidationResponse(Map<String, Long> keysWithBadVersionAndTheirExpectedVersion,
			Set<String> lockedKeys) {
		this.keysWithBadVersionAndTheirExpectedVersion = keysWithBadVersionAndTheirExpectedVersion;
		this.lockedKeys = lockedKeys;
		this.accepted = keysWithBadVersionAndTheirExpectedVersion.isEmpty() && lockedKeys.isEmpty();
	}
}
