package com.constellio.app.services.schemas.bulkImport.plan;

import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordsImportPlanServices {

	ModelLayerFactory modelLayerFactory;

	public RecordsImportPlanServices(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	public RecordsImportPlan buildRecordsImportPlan(ImportDataProvider importDataProvider) {
		List<MetadataSchemaType> importedSchemaTypesInOrder = new ArrayList<>();
		Map<String, Metadata> metadatasRequiringASecondPhaseSplittedBySchemaTypes = new HashMap<>();

		Map<String, String> dependencyMap = new HashMap<>();


		return new RecordsImportPlan(importedSchemaTypesInOrder, metadatasRequiringASecondPhaseSplittedBySchemaTypes);


	}


}
