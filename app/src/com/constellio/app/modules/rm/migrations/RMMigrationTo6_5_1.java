package com.constellio.app.modules.rm.migrations;

import static com.constellio.model.entities.schemas.MetadataValueType.DATE;
import static com.constellio.model.entities.schemas.MetadataValueType.ENUM;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.FolderArchivisticStatusCalculator2;
import com.constellio.app.modules.rm.model.calculators.FolderExpectedDepositDateCalculator2;
import com.constellio.app.modules.rm.model.calculators.FolderExpectedDestructionDateCalculator2;
import com.constellio.app.modules.rm.model.calculators.FolderExpectedTransferDateCalculator2;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilderRuntimeException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderRuntimeException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderRuntimeException.NoSuchSchemaType;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class RMMigrationTo6_5_1 implements MigrationScript {
	@Override
	public String getVersion() {
		return "6.5.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory factory)
			throws Exception {
		new SchemaAlterationsFor6_5_1(collection, provider, factory).migrate();
		modifyArchivisticCalculators(collection, factory);

		makeArchivisticDatesBasedOnNumbers(collection, factory);
	}

	private void makeArchivisticDatesBasedOnNumbers(String collection, AppLayerFactory appLayerFactory) {
		deleteIfPossibleOrDisableFolderCalendarYearMetadata(collection, appLayerFactory);
		deleteIfPossibleOrDisableFolderCalendarYearEntredMetadata(collection, appLayerFactory);
		deleteIfPossibleOrDisableDocumentCalendarYearMetadata(collection, appLayerFactory);
		deleteIfPossibleOrDisableDocuemntCalendarYearEntredMetadata(collection, appLayerFactory);

		//2. TODO change archivistic calculators
	}

	private void deleteIfPossibleOrDisableDocuemntCalendarYearEntredMetadata(String collection, AppLayerFactory appLayerFactory) {
		Migration6_5_1_Helper
				.deleteIfPossibleOrDisableMetadata("calendarYearEntered", Document.SCHEMA_TYPE, collection, appLayerFactory);
	}

	private void deleteIfPossibleOrDisableDocumentCalendarYearMetadata(String collection, AppLayerFactory appLayerFactory) {
		Migration6_5_1_Helper
				.deleteIfPossibleOrDisableMetadata("calendarYear", Document.SCHEMA_TYPE, collection, appLayerFactory);
	}

	private void deleteIfPossibleOrDisableFolderCalendarYearEntredMetadata(String collection, AppLayerFactory appLayerFactory) {
		Migration6_5_1_Helper
				.deleteIfPossibleOrDisableMetadata("calendarYearEntered", Folder.SCHEMA_TYPE, collection, appLayerFactory);
	}

	private void deleteIfPossibleOrDisableFolderCalendarYearMetadata(String collection, AppLayerFactory appLayerFactory) {
		Migration6_5_1_Helper
				.deleteIfPossibleOrDisableMetadata("calendarYear", Folder.SCHEMA_TYPE, collection, appLayerFactory);
	}

	private void modifyArchivisticCalculators(String collection, AppLayerFactory factory) {
		factory.getModelLayerFactory().getMetadataSchemasManager().modify(collection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaBuilder folderSchemaBuilder = types.getDefaultSchema(Folder.SCHEMA_TYPE);
				folderSchemaBuilder.getMetadata(Folder.EXPECTED_TRANSFER_DATE).defineDataEntry()
						.asCalculated(FolderExpectedTransferDateCalculator2.class);
				folderSchemaBuilder.getMetadata(Folder.EXPECTED_DEPOSIT_DATE).defineDataEntry()
						.asCalculated(FolderExpectedDepositDateCalculator2.class);
				folderSchemaBuilder.getMetadata(Folder.EXPECTED_DESTRUCTION_DATE).defineDataEntry()
						.asCalculated(FolderExpectedDestructionDateCalculator2.class);
				folderSchemaBuilder.getMetadata(Folder.ARCHIVISTIC_STATUS).defineDataEntry()
						.asCalculated(FolderArchivisticStatusCalculator2.class);
			}
		});

	}

	public static class SchemaAlterationsFor6_5_1 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationsFor6_5_1(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder folderDefaultSchema = types().getDefaultSchema(Folder.SCHEMA_TYPE);
			createArchivisticManualMetadata(folderDefaultSchema);
			createFolderTimeRangeMetadata(folderDefaultSchema);
		}

		private void createFolderTimeRangeMetadata(MetadataSchemaBuilder folderDefaultSchema) {
			folderDefaultSchema.create(Folder.TIME_RANGE).setType(STRING)
					.setInputMask("9999-9999").setEnabled(false);
		}

		private void createArchivisticManualMetadata(MetadataSchemaBuilder folderDefaultSchema) {
			folderDefaultSchema.createUndeletable(Folder.MANUAL_ARCHIVISTIC_STATUS).setType(ENUM).defineAsEnum(FolderStatus.class)
					.setEnabled(false);
			folderDefaultSchema.createUndeletable(Folder.MANUAL_EXPECTED_DESTRIUCTION_DATE).setType(DATE).setEnabled(false);
			folderDefaultSchema.createUndeletable(Folder.MANUAL_EXPECTED_DEPOSIT_DATE).setType(DATE).setEnabled(false);
			folderDefaultSchema.createUndeletable(Folder.MANUAL_EXPECTED_TRANSFER_DATE).setType(DATE).setEnabled(false);
		}
	}

	public static class Migration6_5_1_Helper {
		public static void deleteIfPossibleOrDisableMetadata(String metadataLocalCode, String schemaTypeCode, String collection,
				AppLayerFactory appLayerFactory) {

			if (isUsed(metadataLocalCode, schemaTypeCode, collection, appLayerFactory)) {
				disableMetadta(metadataLocalCode, schemaTypeCode, collection, appLayerFactory);
			} else {
				deleteMetadata(metadataLocalCode, schemaTypeCode, collection, appLayerFactory);
			}

		}

		private static void deleteMetadata(final String metadataLocalCode, final String schemaTypeCode, String collection,
				AppLayerFactory appLayerFactory) {
			MetadataSchemasManager schemaManager = appLayerFactory.getModelLayerFactory()
					.getMetadataSchemasManager();
			try {
				final Metadata metadata = schemaManager.getSchemaTypes(collection).getDefaultSchema(schemaTypeCode)
						.getMetadata(metadataLocalCode);
				schemaManager.modify(collection, new MetadataSchemaTypesAlteration() {
							@Override
							public void alter(MetadataSchemaTypesBuilder types) {
								MetadataSchemaBuilder schemaBuilder = types.getDefaultSchema(schemaTypeCode);
								schemaBuilder.deleteMetadataWithoutValidation(metadata);
							}
						}
				);
			} catch (MetadataSchemasRuntimeException.NoSuchMetadata | MetadataSchemasRuntimeException.NoSuchSchemaType e) {
				//OK metadat does not exist any more
			}
		}

		private static void disableMetadta(final String metadataLocalCode, final String schemaTypeCode, String collection,
				AppLayerFactory appLayerFactory) {
			MetadataSchemasManager schemaManager = appLayerFactory.getModelLayerFactory()
					.getMetadataSchemasManager();
			schemaManager.modify(collection, new MetadataSchemaTypesAlteration() {
				@Override
				public void alter(MetadataSchemaTypesBuilder types) {
					try {
						types.getDefaultSchema(schemaTypeCode).getMetadata(metadataLocalCode).setEnabled(false);
					} catch (MetadataSchemaBuilderRuntimeException.NoSuchMetadata | MetadataSchemaTypesBuilderRuntimeException.NoSuchSchemaType e) {
						//OK metadat does not exist any more
					}
				}
			});
		}

		public static boolean isUsed(String metadataLocalCode, String schemaTypeCode, String collection,
				AppLayerFactory appLayerFactory) {
			MetadataSchemasManager schemaManager = appLayerFactory.getModelLayerFactory()
					.getMetadataSchemasManager();
			MetadataSchemaTypes types = schemaManager.getSchemaTypes(collection);
			try {
				Metadata metadata = types.getDefaultSchema(schemaTypeCode)
						.getMetadata(metadataLocalCode);
				LogicalSearchCondition query = LogicalSearchQueryOperators.from(types.getSchemaType(schemaTypeCode))
						.where(metadata).isNotNull();
				SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
				return searchServices.hasResults(new LogicalSearchQuery(query));
			} catch (MetadataSchemasRuntimeException.NoSuchSchemaType | MetadataSchemasRuntimeException.NoSuchMetadata e) {
				//ok
				return false;
			}
		}
	}


}
