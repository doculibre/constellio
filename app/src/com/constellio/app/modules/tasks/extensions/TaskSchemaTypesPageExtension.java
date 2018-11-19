package com.constellio.app.modules.tasks.extensions;

import com.constellio.app.api.extensions.SchemaTypesPageExtension;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.model.entities.schemas.MetadataFilter;
import com.constellio.model.entities.schemas.MetadataFilterFactory;

import java.util.Arrays;
import java.util.List;

public class TaskSchemaTypesPageExtension extends SchemaTypesPageExtension {
	@Override
	public List<MetadataFilter> getMetadataAccessExclusionFilters() {
		return Arrays.asList(MetadataFilterFactory.excludeMetadataOfSchemaType(RMTask.SCHEMA_TYPE, RMTask.PARENT_TASK));
	}
}
