package com.constellio.model.services.records.cache;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import lombok.AllArgsConstructor;

/**
 * Will be replaced in 9.1 with a better version
 */
@AllArgsConstructor
public class Version9_0LocalCacheConfigs {


	boolean legacyIdentifierIndexedInMemory;

	public boolean excludedDuringLastCacheRebuild(Metadata metadata) {
		return !legacyIdentifierIndexedInMemory && Schemas.LEGACY_ID.isSameLocalCode(metadata);

	}
}
