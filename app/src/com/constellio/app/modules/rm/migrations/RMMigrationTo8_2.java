package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.FolderCopyRulesExpectedDepositDatesCalculator2;
import com.constellio.app.modules.rm.model.calculators.FolderCopyRulesExpectedDestructionDatesCalculator2;
import com.constellio.app.modules.rm.model.calculators.FolderCopyRulesExpectedTransferDatesCalculator;
import com.constellio.app.modules.rm.model.calculators.FolderCopyRulesExpectedTransferDatesCalculator2;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.records.ConditionnedActionExecutorInBatchBuilder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

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
		new SchemaAlterationFor8_2(collection, provider, appLayerFactory).migrate();

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
	}

	private void modifyRecords(final MetadataSchemaType metadataSchemaType, final String metadataCode,
							   final ModelLayerFactory modelLayerFactory) {
		ConditionnedActionExecutorInBatchBuilder conditionnedActionExecutorInBatchBuilder = onCondition(modelLayerFactory, from(metadataSchemaType).returnAll());
		conditionnedActionExecutorInBatchBuilder.setBatchSize(500);
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

	public ConditionnedActionExecutorInBatchBuilder onCondition(ModelLayerFactory modelLayerFactory,
																LogicalSearchCondition condition) {
		return new ConditionnedActionExecutorInBatchBuilder(modelLayerFactory, condition);
	}

	public void addToFavoritesList(Object records, String cartId) {
		for (String recordId : (List<String>) records) {
			FAVORITES_LIST_MAP.add(recordId, cartId);
		}

	}

	private class SchemaAlterationFor8_2 extends MetadataSchemasAlterationHelper {
		public SchemaAlterationFor8_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
									  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			builder.getDefaultSchema(Folder.SCHEMA_TYPE)
					.createUndeletable(Folder.FAVORITES).setType(MetadataValueType.STRING).setMultivalue(true).setDefaultRequirement(true).setSystemReserved(true).setUndeletable(true);
			builder.getDefaultSchema(Document.SCHEMA_TYPE)
					.createUndeletable(Document.FAVORITES).setType(MetadataValueType.STRING).setMultivalue(true).setDefaultRequirement(true).setSystemReserved(true).setUndeletable(true);
			builder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE)
					.createUndeletable(ContainerRecord.FAVORITES).setType(MetadataValueType.STRING).setMultivalue(true).setDefaultRequirement(true).setSystemReserved(true).setUndeletable(true);

			MetadataSchemaBuilder folderSchema = types().getSchema(Folder.DEFAULT_SCHEMA);
			if (folderSchema.hasMetadata(FOLDER_DECOMMISSIONING_DATE)) {
				folderSchema.get(FOLDER_DECOMMISSIONING_DATE).setEssential(false).setEnabled(false)
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
		}
	}
}
