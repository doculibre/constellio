package com.constellio.model.services.search.query.logical.condition;

import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.records.cache.LocalCacheConfigs;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class IsSupportingMemoryExecutionParams {

	@Getter
	boolean queryingTypesInSummaryCache;

	@Getter
	boolean requiringExecutionMethod;

	@Getter
	LocalCacheConfigs localCacheConfigs;

	@Getter
	MetadataSchemaType schemaType;
}
