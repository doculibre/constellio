package com.constellio.app.modules.rm;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.BagInfo;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.ExternalLink;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.SIParchive;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.modules.rm.wrappers.triggers.Trigger;
import com.constellio.app.modules.rm.wrappers.triggers.TriggerAction;
import com.constellio.app.modules.rm.wrappers.triggers.TriggerActionType;
import com.constellio.app.modules.rm.wrappers.triggers.TriggerType;
import com.constellio.app.modules.rm.wrappers.triggers.actions.MoveInFolderTriggerAction;
import com.constellio.app.modules.rm.wrappers.type.ContainerRecordType;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.modules.rm.wrappers.type.StorageSpaceType;
import com.constellio.app.modules.rm.wrappers.type.VariableRetentionPeriod;
import com.constellio.app.modules.rm.wrappers.type.YearType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

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
		codesList.add(BagInfo.SCHEMA_TYPE);
		codesList.add(Trigger.SCHEMA_TYPE);
		codesList.add(TriggerAction.SCHEMA_TYPE);
		codesList.add(TriggerActionType.SCHEMA_TYPE);
		codesList.add(TriggerType.SCHEMA_TYPE);
		codesList.add(ExternalLink.SCHEMA_TYPE);

		schemaTypesCodes = Collections.unmodifiableList(codesList);

		List<String> rmCustomSchemasList = new ArrayList<>();
		rmCustomSchemasList.add(PrintableLabel.SCHEMA_NAME);
		rmCustomSchemasList.add(SIParchive.SCHEMA);
		rmCustomSchemasList.add(MoveInFolderTriggerAction.SCHEMA);

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

	public static List<MetadataSchemaTypeBuilder> rmSchemaTypes(MetadataSchemaTypesBuilder types) {
		List<MetadataSchemaTypeBuilder> schemaTypes = new ArrayList<>();

		for (String code : schemaTypesCodes) {
			if (types.hasSchemaType(code)) {
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
