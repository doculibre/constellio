package com.constellio.model.entities.calculators;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.List;

public interface InitializedMetadataValueCalculator<T> {

	void initialize(List<Metadata> schemaMetadatas, Metadata calculatedMetadata);

	void initialize(MetadataSchemaTypes types, MetadataSchema schema, Metadata calculatedMetadata);

}
