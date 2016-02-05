package com.constellio.sdk.tests.schemas;

import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public interface MetadataSchemaTypeConfigurator {

	void configure(MetadataSchemaTypeBuilder builder, MetadataSchemaTypesBuilder schemaTypes);

}
