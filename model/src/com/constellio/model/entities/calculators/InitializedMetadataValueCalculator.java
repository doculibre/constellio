package com.constellio.model.entities.calculators;

import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public interface InitializedMetadataValueCalculator<T> extends MetadataValueCalculator<T> {

	void initialize(MetadataSchemaTypes types, MetadataSchema schema);

}
