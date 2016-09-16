package com.constellio.app.services.schemas.bulkImport;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class SkippedRecordsImport {

	public static final String SKIP_BECAUSE_DEPENDENCE_FAILED = "skipBecauseDependenceFailed";

	KeySetMap<String, String> skippedBecauseOfFailure = new KeySetMap<>();
	KeySetMap<String, String> skippedBecauseOfDependency = new KeySetMap<>();

	public void markAsSkippedBecauseOfFailure(String schemaType, String id) {
		skippedBecauseOfFailure.add(schemaType, id);
	}

	public boolean isSkipped(String schemaType, String id) {
		return (skippedBecauseOfFailure.contains(schemaType) && skippedBecauseOfFailure.get(schemaType).contains(id)) ||
				(skippedBecauseOfDependency.contains(schemaType) && skippedBecauseOfDependency.get(schemaType).contains(id));
	}

	public void markAsSkippedBecauseOfDependencyFailure(String schemaType, String id) {
		skippedBecauseOfDependency.add(schemaType, id);
	}

	public void addWarningForSkippedRecordsBecauseOfDependencies(Language language, MetadataSchemaTypes types,
			ValidationErrors errors) {

		for (Map.Entry<String, Set<String>> entry : skippedBecauseOfDependency.getMapEntries()) {
			MetadataSchemaType type = types.getSchemaType(entry.getKey());

			Map<String, Object> parameters = new HashMap<>();
			parameters.put("prefix", type.getLabel(language) + " : ");
			parameters.put("impacts", "" + entry.getValue().size());
			errors.addWarning(SkippedRecordsImport.class, SKIP_BECAUSE_DEPENDENCE_FAILED, parameters);
		}

	}

}
