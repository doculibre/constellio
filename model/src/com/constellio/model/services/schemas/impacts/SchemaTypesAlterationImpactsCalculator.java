/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
