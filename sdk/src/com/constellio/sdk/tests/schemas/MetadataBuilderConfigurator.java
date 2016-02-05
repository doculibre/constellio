package com.constellio.sdk.tests.schemas;

import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public interface MetadataBuilderConfigurator {

	void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes);

}
