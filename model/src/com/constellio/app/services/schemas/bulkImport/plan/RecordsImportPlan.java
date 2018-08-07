package com.constellio.app.services.schemas.bulkImport.plan;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;

import java.util.List;
import java.util.Map;

public class RecordsImportPlan {

	private List<MetadataSchemaType> importedSchemaTypesInOrder;

	private Map<String, Metadata> metadatasRequiringASecondPhaseSplittedBySchemaTypes;

	public RecordsImportPlan(List<MetadataSchemaType> importedSchemaTypesInOrder,
							 Map<String, Metadata> metadatasRequiringASecondPhaseSplittedBySchemaTypes) {
		this.importedSchemaTypesInOrder = importedSchemaTypesInOrder;
		this.metadatasRequiringASecondPhaseSplittedBySchemaTypes = metadatasRequiringASecondPhaseSplittedBySchemaTypes;
	}

	public List<MetadataSchemaType> getImportedSchemaTypesInOrder() {
		return importedSchemaTypesInOrder;
	}

	public Map<String, Metadata> getMetadatasRequiringASecondPhaseSplittedBySchemaTypes() {
		return metadatasRequiringASecondPhaseSplittedBySchemaTypes;
	}
}
