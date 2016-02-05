package com.constellio.model.services.schemas.impacts;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class SchemaTypesAlterationImpactsCalculator {

	public List<SchemaTypesAlterationImpact> calculatePotentialImpacts(MetadataSchemaTypesBuilder types) {
		List<SchemaTypesAlterationImpact> impacts = new ArrayList<>();

		//TODO Disabled, because there is a problem with impacts handling causing a change of status to destroy all search indexes.

		//		for (MetadataSchemaTypeBuilder type : types.getTypes()) {
		//			SchemaTypesAlterationImpact impact = calculatePotentialImpacts(type);
		//			if (impact != null) {
		//				impacts.add(impact);
		//			}
		//		}

		return impacts;
	}

	private SchemaTypesAlterationImpact calculatePotentialImpacts(MetadataSchemaTypeBuilder type) {
		List<String> reindexedMetadataForSearch = new ArrayList<>();
		List<String> convertedToSingleValue = new ArrayList<>();
		List<String> convertedToMultiValue = new ArrayList<>();
		boolean reindexAutocomplete = false;

		for (MetadataBuilder modifiedMetadata : type.getAllMetadatas()) {
			Metadata originalMetadata = modifiedMetadata.getOriginalMetadata();
			if (originalMetadata != null) {
				if (originalMetadata.isMultivalue() && !modifiedMetadata.isMultivalue()) {
					convertedToSingleValue.add(modifiedMetadata.getLocalCode());
				}
				if (!originalMetadata.isMultivalue() && modifiedMetadata.isMultivalue()) {
					convertedToMultiValue.add(modifiedMetadata.getLocalCode());
				}
				if (originalMetadata.isSearchable() != modifiedMetadata.isSearchable()) {
					reindexedMetadataForSearch.add(modifiedMetadata.getLocalCode());
				}
				if (originalMetadata.isSchemaAutocomplete() != modifiedMetadata.isSchemaAutocomplete()) {
					reindexAutocomplete = true;
				}
			}
		}

		SchemaTypesAlterationImpact impact = null;
		if (reindexAutocomplete || !reindexedMetadataForSearch.isEmpty() || !convertedToMultiValue.isEmpty()
				|| !convertedToSingleValue.isEmpty()) {
			BatchProcessAction batchProcessAction = new SchemaTypeAlterationBatchProcessAction(reindexedMetadataForSearch,
					convertedToSingleValue, convertedToMultiValue);
			impact = new SchemaTypesAlterationImpact(batchProcessAction, type.getCode());
		}
		return impact;
	}

}
