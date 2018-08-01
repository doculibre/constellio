package com.constellio.app.modules.rm;

import com.constellio.app.modules.rm.wrappers.*;
import com.constellio.app.modules.rm.wrappers.type.*;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RMTypes {

	public static List<String> schemaTypesCodes;

	public static List<String> rmCustomSchemas;

	static {
		List<String> codesList = new ArrayList<>();
		codesList.add(Document.SCHEMA_TYPE);
		codesList.add(Folder.SCHEMA_TYPE);
		codesList.add(Category.SCHEMA_TYPE);
		codesList.add(AdministrativeUnit.SCHEMA_TYPE);
		codesList.add(RetentionRule.SCHEMA_TYPE);
		codesList.add(Cart.SCHEMA_TYPE);
		codesList.add(ContainerRecord.SCHEMA_TYPE);
		codesList.add(DecommissioningList.SCHEMA_TYPE);
		codesList.add(UniformSubdivision.SCHEMA_TYPE);
		codesList.add(StorageSpace.SCHEMA_TYPE);
		codesList.add(ContainerRecordType.SCHEMA_TYPE);
		codesList.add(DocumentType.SCHEMA_TYPE);
		codesList.add(FolderType.SCHEMA_TYPE);
		codesList.add(MediumType.SCHEMA_TYPE);
		codesList.add(StorageSpaceType.SCHEMA_TYPE);
		codesList.add(VariableRetentionPeriod.SCHEMA_TYPE);
		codesList.add(YearType.SCHEMA_TYPE);
		codesList.add(Cart.SCHEMA_TYPE);
		codesList.add(FilingSpace.SCHEMA_TYPE);

		schemaTypesCodes = Collections.unmodifiableList(codesList);

		List<String> rmCustomSchemasList = new ArrayList<>();
		rmCustomSchemasList.add(PrintableLabel.SCHEMA_NAME);
		rmCustomSchemasList.add(SIParchive.SCHEMA);

		rmCustomSchemas = Collections.unmodifiableList(rmCustomSchemasList);
	}

	public static List<MetadataSchemaType> rmSchemaTypes(AppLayerFactory appLayerFactory, String collection) {
		return rmSchemaTypes(appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection));
	}

	public static List<MetadataSchemaType> rmSchemaTypes(MetadataSchemaTypes types) {
		List<MetadataSchemaType> schemaTypes = new ArrayList<>();

		for (String code : schemaTypesCodes) {
			if (types.hasType(code)) {
				schemaTypes.add(types.getSchemaType(code));
			}
		}

		return Collections.unmodifiableList(schemaTypes);
	}

	public static List<MetadataSchema> rmSchemas(AppLayerFactory appLayerFactory, String collection) {
		return rmSchemas(appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection));
	}

	public static List<MetadataSchema> rmSchemas(MetadataSchemaTypes types) {
		List<MetadataSchemaType> taskTypes = rmSchemaTypes(types);
		List<MetadataSchema> taskSchemas = new ArrayList<>();

		for (MetadataSchemaType schemaType : taskTypes) {
			for (MetadataSchema schema : schemaType.getAllSchemas()) {
				taskSchemas.add(schema);
			}
		}

		for (String customSchema : rmCustomSchemas) {
			if (types.hasSchema(customSchema)) {
				taskSchemas.add(types.getSchema(customSchema));
			}
		}
		return taskSchemas;
	}

}
