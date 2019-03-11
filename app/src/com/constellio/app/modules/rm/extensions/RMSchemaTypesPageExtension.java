package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.SchemaTypesPageExtension;
import com.constellio.app.api.extensions.params.IsBuiltInMetadataAttributeModifiableParam;
import com.constellio.app.modules.rm.extensions.params.RMSchemaTypesPageExtensionExclusionByPropertyParams;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.schemas.MetadataFilter;
import com.constellio.model.entities.schemas.MetadataFilterFactory;

import java.util.Arrays;
import java.util.List;

import static com.constellio.model.entities.schemas.MetadataAttribute.REQUIRED;

import static com.constellio.model.entities.schemas.MetadataAttribute.REQUIRED;

public class RMSchemaTypesPageExtension extends SchemaTypesPageExtension {

	@Override
	public ExtensionBooleanResult isBuiltInMetadataAttributeModifiable(
			IsBuiltInMetadataAttributeModifiableParam param) {

		if (param.is(ContainerRecord.SCHEMA_TYPE, ContainerRecord.ADMINISTRATIVE_UNITS) && param.isAttribute(REQUIRED)) {
			return ExtensionBooleanResult.FORCE_TRUE;

		} else if (param.is(ContainerRecord.SCHEMA_TYPE, ContainerRecord.DECOMMISSIONING_TYPE) && param.isAttribute(REQUIRED)) {
			return ExtensionBooleanResult.FORCE_TRUE;

		}

		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	@Override
	public List<MetadataFilter> getMetadataAccessExclusionFilters() {
		return Arrays.asList(MetadataFilterFactory.excludeMetadataOfSchemaType(Document.SCHEMA_TYPE, Document.FOLDER),
				MetadataFilterFactory.excludeMetadataOfSchemaType(Folder.SCHEMA_TYPE, Folder.PARENT_FOLDER),
				MetadataFilterFactory.excludeMetadataOfSchemaType(AdministrativeUnit.SCHEMA_TYPE, AdministrativeUnit.PARENT),
				MetadataFilterFactory.excludeMetadataOfSchemaType(Category.SCHEMA_TYPE, Category.PARENT),
				MetadataFilterFactory.excludeMetadataOfSchemaType(Document.SCHEMA_TYPE, Document.TYPE));
	}

	@Override
	public boolean getMetadataAccessExclusionPropertyFilter(RMSchemaTypesPageExtensionExclusionByPropertyParams rmSchemaTypesPageExtensionExclusionByPropertyParams) {
		return rmSchemaTypesPageExtensionExclusionByPropertyParams.getMetadata().isEssentialInSummary();
	}
}
