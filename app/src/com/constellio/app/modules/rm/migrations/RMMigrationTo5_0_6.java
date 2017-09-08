package com.constellio.app.modules.rm.migrations;

import static com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeCodeMode.REQUIRED_AND_UNIQUE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRUCTURE;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;
import static java.util.Arrays.asList;

import java.util.List;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.calculators.folder.FolderRetentionPeriodCodeCalculator.FolderActiveRetentionPeriodCodeCalculator;
import com.constellio.app.modules.rm.model.calculators.folder.FolderRetentionPeriodCodeCalculator.FolderSemiActiveRetentionPeriodCodeCalculator;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder;
import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeBuilderOptions;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.type.VariableRetentionPeriod;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.validators.metadatas.IntegerStringValidator;

public class RMMigrationTo5_0_6 implements MigrationScript {
	@Override
	public String getVersion() {
		return "5.0.6";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory) {

		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

		if (!rm.folder.schema().hasMetadataWithCode(Folder.LINEAR_SIZE)) {
			new SchemaAlterationFor5_0_6(collection, migrationResourcesProvider, appLayerFactory).migrate();
			updateFormAndDisplayConfigs(collection, appLayerFactory);
		}

		if (rm.getVariableRetentionPeriodWithCode("888") == null) {
			addVariablePeriod888And999(collection, migrationResourcesProvider, appLayerFactory);
		}

		modelLayerFactory.getSystemConfigurationsManager()
				.signalDefaultValueModification(RMConfigs.LINKABLE_CATEGORY_MUST_NOT_BE_ROOT, true);

		//Reindexation that was planned in 5.0.6, moved to 5.0.7
		//		ReindexingServices reindexingServices = appLayerFactory.getModelLayerFactory().newReindexingServices();
		//		reindexingServices.reindexCollection(collection, setReindexedSchemaTypes(asList(Folder.SCHEMA_TYPE)));

		//modelLayerFactory.getBatchProcessesManager().waitUntilAllFinished();
	}

	private void updateFormAndDisplayConfigs(String collection, AppLayerFactory appLayerFactory) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();

		SchemaTypesDisplayTransactionBuilder transactionBuilder = manager.newTransactionBuilderFor(collection);

		transactionBuilder
				.in(ContainerRecord.SCHEMA_TYPE)
				.addToForm(ContainerRecord.CAPACITY, ContainerRecord.FILL_RATIO_ENTRED)
				.atTheEnd();

		transactionBuilder
				.in(ContainerRecord.SCHEMA_TYPE)
				.addToDisplay(ContainerRecord.CAPACITY)
				.atTheEnd();

		transactionBuilder
				.in(Folder.SCHEMA_TYPE)
				.addToForm(Folder.LINEAR_SIZE)
				.atTheEnd();

		transactionBuilder
				.in(Folder.SCHEMA_TYPE)
				.addToDisplay(Folder.LINEAR_SIZE)
				.beforeTheHugeCommentMetadata();

		transactionBuilder
				.in(User.SCHEMA_TYPE)
				.addToDisplay(User.ALL_ROLES)
				.atTheEnd();

		transactionBuilder
				.in(VariableRetentionPeriod.SCHEMA_TYPE)
				.addToSearchResult(VariableRetentionPeriod.CODE)
				.atFirstPosition();

		transactionBuilder
				.in(ContainerRecord.SCHEMA_TYPE)
				.addToForm(ContainerRecord.STORAGE_SPACE)
				.afterMetadata(ContainerRecord.IDENTIFIER);

		transactionBuilder
				.in(Folder.SCHEMA_TYPE)
				.removeFromDisplay(Folder.RETENTION_RULE_ADMINISTRATIVE_UNITS);

		manager.execute(transactionBuilder.build());
	}

	private void addVariablePeriod888And999(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		VariableRetentionPeriod period888 = rm.newVariableRetentionPeriod().setCode("888")
				.setTitle(migrationResourcesProvider.getDefaultLanguageString("init.variablePeriod888"));
		VariableRetentionPeriod period999 = rm.newVariableRetentionPeriod().setCode("999")
				.setTitle(migrationResourcesProvider.getDefaultLanguageString("init.variablePeriod999"));
		try {
			appLayerFactory.getModelLayerFactory().newRecordServices().execute(new Transaction().addAll(period888, period999));
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	class SchemaAlterationFor5_0_6 extends MetadataSchemasAlterationHelper {

		MetadataSchemaTypes types;

		protected SchemaAlterationFor5_0_6(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
			types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
		}

		public String getVersion() {
			return "5.0.6";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			//Folder
			MetadataSchemaBuilder folderSchema = typesBuilder.getSchema(Folder.DEFAULT_SCHEMA);
			folderSchema.createUndeletable(Folder.LINEAR_SIZE).setType(MetadataValueType.NUMBER).setEssential(true);
			folderSchema.createUndeletable(Folder.ACTIVE_RETENTION_CODE).setType(MetadataValueType.STRING)
					.defineDataEntry().asCalculated(FolderActiveRetentionPeriodCodeCalculator.class);
			folderSchema.createUndeletable(Folder.SEMIACTIVE_RETENTION_CODE).setType(MetadataValueType.STRING)
					.defineDataEntry().asCalculated(FolderSemiActiveRetentionPeriodCodeCalculator.class);

			//Container
			typesBuilder.getSchema(ContainerRecord.DEFAULT_SCHEMA).createUndeletable(ContainerRecord.CAPACITY)
					.setType(MetadataValueType.NUMBER);
			typesBuilder.getSchema(ContainerRecord.DEFAULT_SCHEMA).createUndeletable(ContainerRecord.FILL_RATIO_ENTRED)
					.setType(MetadataValueType.NUMBER);

			ValueListItemSchemaTypeBuilder builder = new ValueListItemSchemaTypeBuilder(typesBuilder);
			builder.createValueListItemSchema(VariableRetentionPeriod.SCHEMA_TYPE, (String) null,
					ValueListItemSchemaTypeBuilderOptions.codeMetadataRequiredAndUnique());

			typesBuilder.getSchema(VariableRetentionPeriod.DEFAULT_SCHEMA)
					.get(VariableRetentionPeriod.CODE).setUnmodifiable(true).addValidator(IntegerStringValidator.class);

			modifyTextContentStructureAndUSRMetadatasToDontWriteNullValues(typesBuilder);

			MetadataSchemaBuilder userDocumentSchema = typesBuilder.getSchema(UserDocument.DEFAULT_SCHEMA);
			userDocumentSchema.create("folder").defineReferencesTo(typesBuilder.getSchema(Folder.DEFAULT_SCHEMA));
		}

		private void modifyTextContentStructureAndUSRMetadatasToDontWriteNullValues(MetadataSchemaTypesBuilder typesBuilder) {
			List<MetadataValueType> typesWithoutNullValues = asList(STRUCTURE, TEXT);
			for (MetadataSchemaTypeBuilder typeBuilder : typesBuilder.getTypes()) {
				for (MetadataBuilder metadata : typeBuilder.getAllMetadatas()) {
					if (metadata.getLocalCode().equals("comments")) {
						//Vaults started in version 5.0.1 has a CONTENTS type instead of STRUCTURE
						metadata.setTypeWithoutValidation(MetadataValueType.STRUCTURE);
					}

				}
			}
		}

	}
}

