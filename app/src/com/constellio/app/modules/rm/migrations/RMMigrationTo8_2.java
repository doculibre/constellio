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
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.records.ConditionnedActionExecutorInBatchBuilder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
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
import org.apache.poi.ss.formula.functions.T;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class RMMigrationTo8_2 implements MigrationScript {
	private static final String FOLDERS = "folders";
	private static final String DOCUMENTS = "documents";
	private static final String CONTAINERS = "containers";

	private static final Logger LOGGER = LoggerFactory.getLogger(RMMigrationTo8_2.class);

	private static final String FOLDER_DECOMMISSIONING_DATE = "decommissioningDate";

	@Override
	public String getVersion() {
		return "8.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {

		if (!appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema().hasMetadataWithCode(Folder.FAVORITES)) {

			new SchemaAlterationFor8_2a(collection, provider, appLayerFactory).migrate();
		}

		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.cartSchema()).returnAll());

		KeySetMap<String, String> foldersFavorites = new KeySetMap<>();
		KeySetMap<String, String> documentsFavorites = new KeySetMap<>();
		KeySetMap<String, String> containersFavorites = new KeySetMap<>();

		for (Record record : searchServices.search(query)) {
			Cart cart = rm.wrapCart(record);
			if (cart.getMetadataSchemaTypes().hasMetadata(Cart.DEFAULT_SCHEMA + "_" + FOLDERS)) {
				for (String recordId : cart.<List<String>>get(FOLDERS)) {
					foldersFavorites.add(recordId, cart.getId());
				}
			}
			if (cart.getMetadataSchemaTypes().hasMetadata(Cart.DEFAULT_SCHEMA + "_" + DOCUMENTS)) {
				for (String recordId : cart.<List<String>>get(DOCUMENTS)) {
					documentsFavorites.add(recordId, cart.getId());
				}
			}
			if (cart.getMetadataSchemaTypes().hasMetadata(Cart.DEFAULT_SCHEMA + "_" + CONTAINERS)) {
				for (String recordId : cart.<List<String>>get(CONTAINERS)) {
					containersFavorites.add(recordId, cart.getId());
				}
			}
		}

		modifyRecords(rm.folder.schemaType(), modelLayerFactory, foldersFavorites);
		modifyRecords(rm.document.schemaType(), modelLayerFactory, documentsFavorites);
		modifyRecords(rm.containerRecord.schemaType(), modelLayerFactory, containersFavorites);

		new SchemaAlterationFor8_2b(collection, provider, appLayerFactory).migrate();

		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transaction = displayManager.newTransactionBuilderFor(collection);

		transaction.add(displayManager.getSchema(collection, MediumType.DEFAULT_SCHEMA)
				.withNewFormMetadata(MediumType.DEFAULT_SCHEMA + "_" + MediumType.ACTIVATED_ON_CONTENT)
		);
		displayManager.execute(transaction.build());

	}

	private void modifyRecords(final MetadataSchemaType metadataSchemaType,
							   final ModelLayerFactory modelLayerFactory, KeySetMap<String, String> favoritesList) {

		LOGGER.info("Finding already migrated " + metadataSchemaType.getCode() + "s...");
		Set<String> alreadyMigratedRecords = findAlreadyMigratedRecords(metadataSchemaType, modelLayerFactory);

		Map<String, Set<String>> recordsCarts = favoritesList.getNestedMap();

		BatchBuilderIterator<String> iterator = new BatchBuilderIterator<>(recordsCarts.keySet().iterator(), 10000);

		int migratedCount = 0;
		while (iterator.hasNext()) {
			LOGGER.info(metadataSchemaType.getCode() + " migration : " + migratedCount + "/" + recordsCarts.size());
			List<String> ids = iterator.next();

			List<SolrInputDocument> inputDocuments = new ArrayList<>();

			for (String id : ids) {
				List<String> carts = new ArrayList<>(recordsCarts.get(id));
				if (!alreadyMigratedRecords.contains(id)) {
					SolrInputDocument inputDocument = new SolrInputDocument();
					inputDocument.setField("id", id);

					Map<String, Object> incrementalSet = new HashMap<>();
					incrementalSet.put("set", carts);
					inputDocument.setField("favorites_ss", incrementalSet);

					inputDocuments.add(inputDocument);

				}
				migratedCount++;
			}

			if (!inputDocuments.isEmpty()) {
				SolrClient solrClient = modelLayerFactory.getDataLayerFactory().newRecordDao().getBigVaultServer().getNestedSolrServer();
				try {
					solrClient.add(inputDocuments);
					solrClient.commit(true, true, true);
				} catch (SolrServerException | IOException e) {
					throw new RuntimeException(e);
				}
			}


		}
		LOGGER.info(metadataSchemaType.getCode() + " migration : " + recordsCarts.size() + "/" + recordsCarts.size());

	}

	private Set<String> findAlreadyMigratedRecords(final MetadataSchemaType metadataSchemaType,
												   final ModelLayerFactory modelLayerFactory) {

		Set<String> alreadyMigratedRecords = new HashSet<>();
		SearchServices searchServices = modelLayerFactory.newSearchServices();

		Metadata favorites = metadataSchemaType.getDefaultSchema().getMetadata("favorites");
		LogicalSearchQuery query = new LogicalSearchQuery(from(metadataSchemaType).where(favorites).<T>isNotNull());
		Iterator<String> idsIterator = searchServices.recordsIdsIterator(query);
		while (idsIterator.hasNext()) {
			alreadyMigratedRecords.add(idsIterator.next());
		}

		return alreadyMigratedRecords;
	}

	public ConditionnedActionExecutorInBatchBuilder onCondition(ModelLayerFactory modelLayerFactory,
																LogicalSearchCondition condition) {
		return new ConditionnedActionExecutorInBatchBuilder(modelLayerFactory, condition);
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
