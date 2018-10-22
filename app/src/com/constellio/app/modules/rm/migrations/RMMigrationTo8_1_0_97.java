package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.document.DocumentHasContentCalculator;
import com.constellio.app.modules.rm.model.calculators.folder.FolderHasContentCalculator;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.ArrayList;
import java.util.List;

public class RMMigrationTo8_1_0_97 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.1.0.97";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new RMMigrationTo8_1_0_97.RMSchemaAlterationFor_8_0_97(collection, migrationResourcesProvider, appLayerFactory).migrate();

		updateAllMediumTypes(collection, appLayerFactory.getModelLayerFactory());
	}

	private void updateAllMediumTypes(String collection, ModelLayerFactory modelLayerFactory) throws Exception {
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		MetadataSchemaType mediumTypeSchemaType = types.getSchemaType(MediumType.SCHEMA_TYPE);
		MetadataSchema mediumTypeSchema = mediumTypeSchemaType.getDefaultSchema();
		List<Record> mediumTypes = modelLayerFactory.newSearchServices().getAllRecords(mediumTypeSchemaType);

		List<Record> nonAnalogicalMediumTypes = new ArrayList<>();
		for (Record mediumType : mediumTypes) {
			if (Boolean.FALSE.equals(mediumType.<Boolean>get(mediumTypeSchema.getMetadata(MediumType.ANALOGICAL)))) {
				nonAnalogicalMediumTypes.add(mediumType);
			}
		}

		if (nonAnalogicalMediumTypes.size() == 1) {
			nonAnalogicalMediumTypes.get(0).set(mediumTypeSchema.getMetadata(MediumType.ACTIVATED_ON_CONTENT), true);
			modelLayerFactory.newRecordServices().update(nonAnalogicalMediumTypes.get(0));
		}
	}

	class RMSchemaAlterationFor_8_0_97 extends MetadataSchemasAlterationHelper {

		protected RMSchemaAlterationFor_8_0_97(String collection, MigrationResourcesProvider migrationResourcesProvider,
											   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder mediumTypeSchema = typesBuilder.getDefaultSchema(MediumType.SCHEMA_TYPE);
			mediumTypeSchema.createUndeletable(MediumType.ACTIVATED_ON_CONTENT).setType(MetadataValueType.BOOLEAN)
					.setDefaultRequirement(true).setDefaultValue(false).setEssential(true);

			MetadataSchemaBuilder documentSchema = typesBuilder.getDefaultSchema(Document.SCHEMA_TYPE);
			documentSchema.createUndeletable(Document.HAS_CONTENT).setType(MetadataValueType.BOOLEAN)
					.defineDataEntry().asCalculated(DocumentHasContentCalculator.class);

			MetadataSchemaBuilder folderSchema = typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE);
			folderSchema.createUndeletable(Folder.HAS_CONTENT).setType(MetadataValueType.BOOLEAN)
					.defineDataEntry().asCalculated(FolderHasContentCalculator.class);
			folderSchema.createUndeletable(Folder.SUB_FOLDERS_WITH_CONTENT).setType(MetadataValueType.BOOLEAN).defineDataEntry()
					.asAggregatedOr(folderSchema.get(Folder.PARENT_FOLDER), folderSchema.get(Folder.HAS_CONTENT));
			folderSchema.createUndeletable(Folder.DOCUMENTS_WITH_CONTENT).setType(MetadataValueType.BOOLEAN).defineDataEntry()
					.asAggregatedOr(documentSchema.get(Document.FOLDER), documentSchema.get(Document.HAS_CONTENT));
		}
	}
}
