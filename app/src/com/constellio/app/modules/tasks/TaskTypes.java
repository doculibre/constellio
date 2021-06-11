package com.constellio.app.modules.tasks;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskTypes {

	public static List<String> schemaTypesCodes;

	public static List<String> customSchemas;

	static {
		List<String> codesList = new ArrayList<>();
		codesList.add(Task.SCHEMA_TYPE);
		codesList.add(TaskType.SCHEMA_TYPE);
		codesList.add(TaskStatus.SCHEMA_TYPE);

		schemaTypesCodes = Collections.unmodifiableList(codesList);

		List<String> taskCustomSchemasList = new ArrayList<>();

		customSchemas = Collections.unmodifiableList(taskCustomSchemasList);
	}

	public static List<MetadataSchemaType> taskSchemaTypes(AppLayerFactory appLayerFactory, String collection) {
		return taskSchemaTypes(appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection));
	}

	public static List<MetadataSchemaType> taskSchemaTypes(MetadataSchemaTypes types) {
		List<MetadataSchemaType> schemaTypes = new ArrayList<>();

		for (String code : schemaTypesCodes) {
			if (types.hasType(code)) {
				schemaTypes.add(types.getSchemaType(code));
			}
		}

		return Collections.unmodifiableList(schemaTypes);
	}

	public static List<MetadataSchemaTypeBuilder> taskSchemaTypes(MetadataSchemaTypesBuilder types) {
		List<MetadataSchemaTypeBuilder> schemaTypes = new ArrayList<>();

		for (String code : schemaTypesCodes) {
			if (types.hasSchemaType(code)) {
				schemaTypes.add(types.getSchemaType(code));
			}
		}

		return Collections.unmodifiableList(schemaTypes);
	}

	public static List<MetadataSchema> taskSchemas(AppLayerFactory appLayerFactory, String collection) {
		return taskSchemas(appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection));
	}

	public static List<MetadataSchema> taskSchemas(MetadataSchemaTypes types) {
		List<MetadataSchemaType> rmTypes = taskSchemaTypes(types);
		List<MetadataSchema> rmSchemas = new ArrayList<>();

		for (MetadataSchemaType schemaType : rmTypes) {
			for (MetadataSchema schema : schemaType.getAllSchemas()) {
				rmSchemas.add(schema);
			}
		}

		for (String customSchema : customSchemas) {
			if (types.hasSchema(customSchema)) {
				rmSchemas.add(types.getSchema(customSchema));
			}
		}
		return rmSchemas;
	}

}
