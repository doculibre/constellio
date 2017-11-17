package com.constellio.model.services.records.reindexing;

import static com.constellio.model.conf.FoldersLocatorMode.PROJECT;
import static com.constellio.model.entities.schemas.Schemas.SCHEMA;
import static com.constellio.model.entities.schemas.entries.DataEntryType.MANUAL;
import static com.constellio.model.entities.schemas.entries.DataEntryType.SEQUENCE;
import static com.constellio.model.services.migrations.ConstellioEIMConfigs.WRITE_ZZRECORDS_IN_TLOG;
import static com.constellio.model.services.records.BulkRecordTransactionImpactHandling.NO_IMPACT_HANDLING;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.RecordDaoException.NoSuchRecordWithId;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogManager;
import com.constellio.data.utils.Octets;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.batch.actions.ReindexMetadatasBatchProcessAction;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.BulkRecordTransactionHandler;
import com.constellio.model.services.records.BulkRecordTransactionHandlerOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.utils.RecordDTOIterator;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class ReindexingServices {

	private static SystemReindexingInfos REINDEXING_INFOS;

	private static final Logger LOGGER = LoggerFactory.getLogger(ReindexingServices.class);

	private static final String REINDEX_TYPES = "reindexTypes";

	private RecordServices recordServices;
	private ModelLayerFactory modelLayerFactory;
	private DataLayerFactory dataLayerFactory;

	private SecondTransactionLogManager logManager;

	private int mainThreadQueryRows;

	public ReindexingServices(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.recordServices = modelLayerFactory.newRecordServices();
		this.dataLayerFactory = modelLayerFactory.getDataLayerFactory();
		this.logManager = dataLayerFactory.getSecondTransactionLogManager();
		this.mainThreadQueryRows = modelLayerFactory.getConfiguration().getReindexingQueryBatchSize();
	}

	public static SystemReindexingInfos getReindexingInfos() {
		return REINDEXING_INFOS;
	}

	public static void markReindexingHasFinished() {
		REINDEXING_INFOS = null;
	}

	public void reindexCollections(ReindexationMode reindexationMode) {
		reindexCollections(new ReindexationParams(reindexationMode));
	}

	public void reindexCollections(ReindexationParams params) {

		dataLayerFactory.getDataLayerLogger().setQueryLoggingEnabled(false);
		try {
			if (params.isBackground()) {
				BatchProcessesManager batchProcessesManager = modelLayerFactory.getBatchProcessesManager();
				for (MetadataSchemaType schemaType : params.getReindexedSchemaTypes()) {
					BatchProcessAction action = ReindexMetadatasBatchProcessAction.allMetadatas();
					batchProcessesManager.addPendingBatchProcess(from(schemaType).returnAll(), action, "reindexing");
				}

			} else {

				if (logManager != null && params.getReindexationMode().isFullRewrite()) {
					logManager.regroupAndMoveInVault();
					logManager.moveTLOGToBackup();
					RecordDao recordDao = dataLayerFactory.newRecordDao();
					try {

						List<RecordDTO> records = new ArrayList<>();

						records.add(recordDao.get("the_private_key"));

						for (Map.Entry<String, Long> entry : dataLayerFactory.getSequencesManager().getSequences().entrySet()) {
							RecordDTO sequence = recordDao.get("seq_" + entry.getKey());
							records.add(sequence);
						}

						try {
							recordDao.execute(
									new TransactionDTO(RecordsFlushing.LATER()).withNewRecords(records).withFullRewrite(true));
						} catch (OptimisticLocking optimisticLocking) {
							throw new RuntimeException(optimisticLocking);
						}
					} catch (NoSuchRecordWithId noSuchRecordWithId) {
						//OK
					}
					recordDao.expungeDeletes();
				}

				for (String collection : modelLayerFactory.getCollectionsListManager().getCollections()) {
					reindexCollection(collection, params);
				}

				if (logManager != null && params.getReindexationMode().isFullRewrite()) {
					logManager.regroupAndMoveInVault();
					logManager.deleteLastTLOGBackup();
				}

				RecordDao recordDao = modelLayerFactory.getDataLayerFactory().newRecordDao();
				recordDao.expungeDeletes();

				deleteMetadatasMarkedForDeletion();

			}

		} finally {
			REINDEXING_INFOS = null;

			dataLayerFactory.getDataLayerLogger().setQueryLoggingEnabled(true);
		}

	}

	private void deleteMetadatasMarkedForDeletion() {
		for (String collection : modelLayerFactory.getCollectionsListManager().getCollections()) {
			MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
			if (hasMetadataMarkedForDeletion(types)) {
				modelLayerFactory.getMetadataSchemasManager().modify(collection, new MetadataSchemaTypesAlteration() {
					@Override
					public void alter(MetadataSchemaTypesBuilder types) {
						for (MetadataSchemaTypeBuilder schemaType : types.getTypes()) {

							for (MetadataSchemaBuilder schema : schemaType.getAllSchemas()) {

								List<MetadataBuilder> metadatasToDelete = new ArrayList<MetadataBuilder>();
								for (MetadataBuilder metadata : schema.getMetadatas()) {
									if (metadata.isMarkedForDeletion() && metadata.getInheritance() == null) {
										metadatasToDelete.add(metadata);
									}
								}
								for (MetadataBuilder metadata : metadatasToDelete) {
									schemaType.getDefaultSchema().deleteMetadataWithoutValidation(metadata);
								}
							}
						}
					}
				});

			}
		}
	}

	private boolean hasMetadataMarkedForDeletion(MetadataSchemaTypes types) {

		for (MetadataSchemaType type : types.getSchemaTypes()) {
			if (!type.getAllMetadatas().onlyMarkedForDeletion().isEmpty()) {
				return true;
			}
		}

		return false;
	}

	public void reindexCollection(String collection, ReindexationMode reindexationMode) {
		reindexCollection(collection, new ReindexationParams(reindexationMode));
	}

	public void reindexCollection(String collection, ReindexationParams params) {

		if (!params.getReindexedSchemaTypes().isEmpty()) {
			long count = getRecordCountOfType(collection, params.getReindexedSchemaTypes());
			if (count == 0) {
				return;
			}
		}

		RecordUpdateOptions transactionOptions = new RecordUpdateOptions().setUpdateModificationInfos(false);
		transactionOptions.setValidationsEnabled(false).setCatchExtensionsValidationsErrors(true)
				.setCatchExtensionsExceptions(true).setCatchBrokenReferenceErrors(true)
				.setUpdateAggregatedMetadatas(true);
		if (params.getReindexationMode().isFullRecalculation()) {
			transactionOptions.setForcedReindexationOfMetadatas(TransactionRecordsReindexation.ALL());
		}
		transactionOptions.setFullRewrite(params.getReindexationMode().isFullRewrite());

		reindexCollection(collection, params, transactionOptions);

	}

	private long getRecordCountOfType(String collection, List<MetadataSchemaType> reindexedSchemaTypes) {
		List<LogicalSearchValueCondition> conditions = new ArrayList<>();

		for (MetadataSchemaType reindexedSchemaType : reindexedSchemaTypes) {
			conditions.add(LogicalSearchQueryOperators.startingWithText(reindexedSchemaType.getCode()));
		}

		LogicalSearchCondition condition = fromAllSchemasIn(collection).where(SCHEMA).isAny(conditions);
		return modelLayerFactory.newSearchServices().getResultsCount(new LogicalSearchQuery(condition));
	}

	private void reindexCollection(String collection, ReindexationParams params, RecordUpdateOptions transactionOptions) {
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);

		if (params.getReindexationMode().isFullRewrite()) {
			recreateIndexes(collection);
		}

		ReindexingRecordsProvider reindexingRecordsProvider = new ReindexingRecordsProvider(modelLayerFactory,
				mainThreadQueryRows);

		int level = 0;
		while (isReindexingLevel(level, types)) {

			BulkRecordTransactionHandlerOptions options = new BulkRecordTransactionHandlerOptions()
					.withBulkRecordTransactionImpactHandling(NO_IMPACT_HANDLING)
					.setTransactionOptions(transactionOptions).showProgressionInConsole(false);

			if (Toggle.FASTER_REINDEXING.isEnabled()) {
				options.withRecordsPerBatch(100000);
				options.setMaxRecordsTotalSizePerBatch(Octets.megaoctets(25).getOctets());

			} else {
				int batchSize = params.getBatchSize();
				if (batchSize == 0) {
					batchSize = modelLayerFactory.getConfiguration().getReindexingThreadBatchSize();
				}
				options.withRecordsPerBatch(batchSize);
			}

			BulkRecordTransactionHandler bulkTransactionHandler = new BulkRecordTransactionHandler(
					modelLayerFactory.newRecordServices(), REINDEX_TYPES, options);

			try {
				for (String typeCode : types.getSchemaTypesSortedByDependency()) {
					if (isReindexingOfTypeRequired(level, types, typeCode)) {
						if (level == 0) {
							LOGGER.info("Collection '" + collection + "' - Indexing '" + typeCode + "'");
						} else {
							LOGGER.info("Collection '" + collection + "' - Indexing '" + typeCode
									+ "' (Dependency level " + level + ")");
						}
						reindexCollectionType(bulkTransactionHandler, types, params,
								reindexingRecordsProvider.newSchemaTypeProvider(types.getSchemaType(typeCode), level));
					}
				}

			} finally {
				bulkTransactionHandler.closeAndJoin();
			}
			modelLayerFactory.getDataLayerFactory().newRecordDao().removeOldLocks();
			level++;
		}
	}

	private boolean isReindexingOfTypeRequired(int level, MetadataSchemaTypes types, String typeCode) {
		MetadataSchemaType type = types.getSchemaType(typeCode);

		if (Event.SCHEMA_TYPE.equals(type.getCode()) && !modelLayerFactory.getConfiguration().isReindexingEvents()) {
			return false;
		}

		return types.getMetadataNetwork().getMaxLevelOf(type.getCode()) >= level;
	}

	private boolean isReindexingLevel(int level, MetadataSchemaTypes types) {
		for (MetadataSchemaType type : types.getSchemaTypes()) {
			if (types.getMetadataNetwork().getMaxLevelOf(type.getCode()) >= level) {
				return true;
			}
		}
		return false;
	}

	private void recreateIndexes(String collection) {
		RecordDao recordDao = modelLayerFactory.getDataLayerFactory().newRecordDao();

		LogicalSearchQuery query = new LogicalSearchQuery()
				.setCondition(fromAllSchemasIn(collection).returnAll())
				.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(Schemas.PATH));

		Iterator<Record> idsIterator = modelLayerFactory.newSearchServices().recordsIterator(query, 50000);
		recordDao.recreateZeroCounterIndexesIn(collection, new RecordDTOIterator(idsIterator));
	}

	private void reindexCollectionType(BulkRecordTransactionHandler bulkTransactionHandler, MetadataSchemaTypes types,
			ReindexationParams params, ReindexingSchemaTypeRecordsProvider recordsProvider) {

		SearchServices searchServices = modelLayerFactory.newSearchServices();
		MetadataSchemaType type = recordsProvider.type;
		boolean writeZZrecords = modelLayerFactory.getSystemConfigurationsManager().getValue(WRITE_ZZRECORDS_IN_TLOG);

		boolean typeReindexed = type.isInTransactionLog() || writeZZrecords;

		FoldersLocator foldersLocator = new FoldersLocator();
		if (typeReindexed && foldersLocator.getFoldersLocatorMode() == PROJECT) {
			//Running on dev computer
			typeReindexed = !Event.SCHEMA_TYPE.equals(type.getCode());
		}

		if (typeReindexed) {
			long counter = searchServices.getResultsCount(new LogicalSearchQuery(from(type).returnAll()));
			List<Metadata> metadatas = type.getAllMetadatas().onlyParentReferences().onlyReferencesToType(type.getCode());
			List<Metadata> metadatasMarkedForDeletion = type.getAllMetadatas().onlyMarkedForDeletion();

			long current = 0;
			REINDEXING_TYPE:
			while (true) {
				Iterator<Record> recordsIterator = recordsProvider.startNewSchemaTypeIteration();
				while (recordsIterator.hasNext()) {
					REINDEXING_INFOS = new SystemReindexingInfos(type.getCollection(), type.getCode(), current, counter);
					Record record = recordsIterator.next();
					for (Metadata metadata : metadatasMarkedForDeletion) {
						if (metadata.getDataEntry().getType() == MANUAL || metadata.getDataEntry().getType() == SEQUENCE) {
							record.set(metadata, null);
						}
					}
					if (metadatas.isEmpty()) {
						current++;
						if (current % 1000 == 0 || current == counter) {
							LOGGER.info("Collection '" + types.getCollection() + "' - Indexing '" + type.getCode() + "' : "
									+ current + "/" + counter);
						}
						bulkTransactionHandler.append(record);
					} else {
						String parentId = getParentIdOfSameType(metadatas, record);

						if (parentId == null || recordsProvider.isAlreadyHandled(parentId) || parentId.equals(record.getId())) {
							current++;
							if (current % 100 == 0 || current == counter) {
								LOGGER.info("Collection '" + types.getCollection() + "' - Indexing '" + type.getCode() + "' : "
										+ current + "/" + counter);
							}
							bulkTransactionHandler.append(record);
							recordsProvider.markRecordAsHandled(record);

						} else {
							recordsProvider.markRecordAsSkipped(record);

						}
					}

				}

				bulkTransactionHandler.barrier();
				modelLayerFactory.newRecordServices().flush();
				recordsProvider.markIterationAsFinished();
				int skippedRecordsCount = recordsProvider.getSkippedRecordsCount();
				if (skippedRecordsCount > 0) {
					LOGGER.info("Collection '" + types.getCollection() + "' - Indexing '" + type.getCode() + "' : Iteration "
							+ recordsProvider.getCurrentIteration() + " has finished with " + skippedRecordsCount
							+ " records skipped, iterating an other time...");
				}
				if (!recordsProvider.isRequiringAnotherIteration()) {
					break REINDEXING_TYPE;
				}

			}
		}

	}

	private String getParentIdOfSameType(List<Metadata> metadatas, Record record) {

		for (Metadata metadata : metadatas) {
			String value = record.get(metadata);
			if (value != null) {
				return value;
			}
		}

		return null;
	}

}