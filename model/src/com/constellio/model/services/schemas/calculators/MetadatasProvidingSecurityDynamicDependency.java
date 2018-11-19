package com.constellio.model.services.schemas.calculators;

import com.constellio.model.entities.calculators.dependencies.DynamicLocalDependency;
import com.constellio.model.entities.schemas.Metadata;

public class MetadatasProvidingSecurityDynamicDependency extends DynamicLocalDependency {
	@Override
	public boolean isDependentOf(Metadata metadata, Metadata caclulatedMetadata) {
		return metadata.isRelationshipProvidingSecurity();
	}
}
