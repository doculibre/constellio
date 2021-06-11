package com.constellio.model.entities.schemas;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public interface ModificationImpact {

	String getCollection();

	List<Metadata> getMetadataToReindex();

	/**
	 * This method should only be used when necessary (reporting), since it can cause extra compuation
	 *
	 * @return
	 */
	List<ModificationImpactDetail> getDetails();

	boolean isHandledNow();

	@AllArgsConstructor
	class ModificationImpactDetail {

		@Getter
		MetadataSchemaType getImpactedSchemaType;

		@Getter
		int getPotentialImpactCount;

	}
}
