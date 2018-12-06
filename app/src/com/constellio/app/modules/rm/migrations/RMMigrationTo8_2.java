package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.RMTypes;
import com.constellio.app.modules.rm.model.calculators.FolderCopyRulesExpectedDepositDatesCalculator2;
import com.constellio.app.modules.rm.model.calculators.FolderCopyRulesExpectedDestructionDatesCalculator2;
import com.constellio.app.modules.rm.model.calculators.FolderCopyRulesExpectedTransferDatesCalculator;
import com.constellio.app.modules.rm.model.calculators.FolderCopyRulesExpectedTransferDatesCalculator2;
import com.constellio.app.modules.rm.model.calculators.document.DocumentHasContentCalculator;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.records.ConditionnedActionExecutorInBatchBuilder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataTransiency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class RMMigrationTo8_2 implements MigrationScript {
	private static final String FOLDERS = "folders";
	private static final String DOCUMENTS = "documents";
	private static final String CONTAINERS = "containers";
	KeySetMap<String, String> FAVORITES_LIST_MAP = new KeySetMap<>();
	private static final String FOLDER_DECOMMISSIONING_DATE = "decommissioningDate";

	@Override
	public String getVersion() {
		return "8.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor8_2a(collection, provider, appLayerFactory).migrate();

		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.cartSchema()).returnAll());

		for (Record record : searchServices.search(query)) {
			Cart cart = rm.wrapCart(record);
			if (cart.getMetadataSchemaTypes().hasMetadata(Cart.DEFAULT_SCHEMA + "_" + FOLDERS)) {
				addToFavoritesList(cart.get(FOLDERS), cart.getId());
			}
			if (cart.getMetadataSchemaTypes().hasMetadata(Cart.DEFAULT_SCHEMA + "_" + DOCUMENTS)) {
				addToFavoritesList(cart.get(DOCUMENTS), cart.getId());
			}
			if (cart.getMetadataSchemaTypes().hasMetadata(Cart.DEFAULT_SCHEMA + "_" + CONTAINERS)) {
				addToFavoritesList(cart.get(CONTAINERS), cart.getId());
			}
		}

		modifyRecords(rm.folder.schemaType(), Folder.FAVORITES, modelLayerFactory);
		modifyRecords(rm.document.schemaType(), Document.FAVORITES, modelLayerFactory);
		modifyRecords(rm.containerRecord.schemaType(), ContainerRecord.FAVORITES, modelLayerFactory);

		updateAllMediumTypes(collection, appLayerFactory.getModelLayerFactory());

		new SchemaAlterationFor8_2b(collection, provider, appLayerFactory).migrate();
	}

	private void modifyRecords(final MetadataSchemaType metadataSchemaType, final String metadataCode,
							   final ModelLayerFactory modelLayerFactory) {
		ConditionnedActionExecutorInBatchBuilder conditionnedActionExecutorInBatchBuilder = onCondition(modelLayerFactory, from(metadataSchemaType).returnAll());
		conditionnedActionExecutorInBatchBuilder.setBatchSize(500);
		conditionnedActionExecutorInBatchBuilder.setOptions(RecordUpdateOptions.validationExceptionSafeOptions());
		conditionnedActionExecutorInBatchBuilder.modifyingRecordsWithImpactHandling(new ConditionnedActionExecutorInBatchBuilder.RecordScript() {
			@Override
			public void modifyRecord(Record record) {
				Metadata metadata = modelLayerFactory.getMetadataSchemasManager().getSchemaOf(record).getMetadata(metadataCode);
				if (FAVORITES_LIST_MAP.contains(record.getId())) {
					List<String> favoritesList = new ArrayList<>();
					for (String value : FAVORITES_LIST_MAP.get(record.getId())) {
						favoritesList.add(value);
					}
					record.set(metadata, favoritesList);
				}
			}
		});
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

	public ConditionnedActionExecutorInBatchBuilder onCondition(ModelLayerFactory modelLayerFactory,
																LogicalSearchCondition condition) {
		return new ConditionnedActionExecutorInBatchBuilder(modelLayerFactory, condition);
	}

	public void addToFavoritesList(Object records, String cartId) {
		for (String recordId : (List<String>) records) {
			FAVORITES_LIST_MAP.add(recordId, cartId);
		}

	}

	private class SchemaAlterationFor8_2a extends MetadataSchemasAlterationHelper {
		public SchemaAlterationFor8_2a(String collection, MigrationResourcesProvider migrationResourcesProvider,
									   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			MetadataSchemaBuilder defaultFolderSchema = builder.getDefaultSchema(Folder.SCHEMA_TYPE);
			defaultFolderSchema
					.createUndeletable(Folder.FAVORITES).setType(MetadataValueType.STRING).setMultivalue(true).setDefaultRequirement(true).setSystemReserved(true).setUndeletable(true);
			builder.getDefaultSchema(Document.SCHEMA_TYPE)
					.createUndeletable(Document.FAVORITES).setType(MetadataValueType.STRING).setMultivalue(true).setDefaultRequirement(true).setSystemReserved(true).setUndeletable(true);
			builder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE)
					.createUndeletable(ContainerRecord.FAVORITES).setType(MetadataValueType.STRING).setMultivalue(true).setDefaultRequirement(true).setSystemReserved(true).setUndeletable(true);

			for (MetadataSchemaTypeBuilder rmType : RMTypes.rmSchemaTypes(builder)) {
				rmType.setSecurity(asList(Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE, Task.SCHEMA_TYPE, AdministrativeUnit.SCHEMA_TYPE)
						.contains(rmType.getCode()));
			}

			MetadataSchemaBuilder folderSchema = types().getSchema(Folder.DEFAULT_SCHEMA);
			if (folderSchema.hasMetadata(FOLDER_DECOMMISSIONING_DATE)) {
				folderSchema.get(FOLDER_DECOMMISSIONING_DATE).setTransiency(MetadataTransiency.PERSISTED).setEssential(false).setEnabled(false)
						.defineDataEntry().asManual();
			}

			boolean defaultDateCalculators = ((CalculatedDataEntry) folderSchema
					.get(Folder.COPY_RULES_EXPECTED_TRANSFER_DATES).getDataEntry()).getCalculator()
					instanceof FolderCopyRulesExpectedTransferDatesCalculator;

			if (defaultDateCalculators) {
				folderSchema.get(Folder.COPY_RULES_EXPECTED_TRANSFER_DATES).defineDataEntry()
						.asCalculated(FolderCopyRulesExpectedTransferDatesCalculator2.class);
				folderSchema.get(Folder.COPY_RULES_EXPECTED_DEPOSIT_DATES).defineDataEntry()
						.asCalculated(FolderCopyRulesExpectedDepositDatesCalculator2.class);
				folderSchema.get(Folder.COPY_RULES_EXPECTED_DESTRUCTION_DATES).defineDataEntry()
						.asCalculated(FolderCopyRulesExpectedDestructionDatesCalculator2.class);
			}

			MetadataSchemaBuilder mediumTypeSchema = typesBuilder.getDefaultSchema(MediumType.SCHEMA_TYPE);
			mediumTypeSchema.createUndeletable(MediumType.ACTIVATED_ON_CONTENT).setType(MetadataValueType.BOOLEAN)
					.setDefaultRequirement(false).setDefaultValue(false).setEssential(true);

			MetadataSchemaBuilder documentSchema = typesBuilder.getDefaultSchema(Document.SCHEMA_TYPE);
			documentSchema.createUndeletable(Document.HAS_CONTENT).setType(MetadataValueType.BOOLEAN)
					.defineDataEntry().asCalculated(DocumentHasContentCalculator.class);

			MetadataBuilder folderHasContent = folderSchema.createUndeletable(Folder.HAS_CONTENT).setType(MetadataValueType.BOOLEAN);

			Map<MetadataBuilder, List<MetadataBuilder>> metadatasByRefMetadata = new HashMap<>();
			metadatasByRefMetadata.put(documentSchema.get(Document.FOLDER), singletonList(documentSchema.get(Document.HAS_CONTENT)));
			metadatasByRefMetadata.put(folderSchema.get(Folder.PARENT_FOLDER), singletonList(folderHasContent));
			folderHasContent.defineDataEntry().asAggregatedOr(metadatasByRefMetadata);

			defaultFolderSchema.getMetadata(Folder.MEDIA_TYPE).setEssentialInSummary(true);
		}
	}

	private class SchemaAlterationFor8_2b extends MetadataSchemasAlterationHelper {
		public SchemaAlterationFor8_2b(String collection, MigrationResourcesProvider migrationResourcesProvider,
									   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			MetadataSchemaTypes metadataSchemaTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
			if (metadataSchemaTypes.hasMetadata(Cart.DEFAULT_SCHEMA + "_" + FOLDERS)) {
				builder.getDefaultSchema(Cart.SCHEMA_TYPE).deleteMetadataWithoutValidation(FOLDERS);
			}
			if (metadataSchemaTypes.hasMetadata(Cart.DEFAULT_SCHEMA + "_" + DOCUMENTS)) {
				builder.getDefaultSchema(Cart.SCHEMA_TYPE).deleteMetadataWithoutValidation(DOCUMENTS);
			}
			if (metadataSchemaTypes.hasMetadata(Cart.DEFAULT_SCHEMA + "_" + CONTAINERS)) {
				builder.getDefaultSchema(Cart.SCHEMA_TYPE).deleteMetadataWithoutValidation(CONTAINERS);
			}
		}
	}
}
