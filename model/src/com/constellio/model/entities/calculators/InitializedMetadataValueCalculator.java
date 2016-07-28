package com.constellio.model.entities.calculators;

import java.util.List;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public interface InitializedMetadataValueCalculator<T> extends MetadataValueCalculator<T> {

	void initialize(List<Metadata> schemaMetadatas, Metadata calculatedMetadata);

	void initialize(MetadataSchemaTypes types, MetadataSchema schema, Metadata calculatedMetadata);

}
