package com.constellio.model.services.records.reindexing;

import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.RecordDaoException.NoSuchRecordWithId;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.sql.SqlRecordDaoType;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogManager;
import com.constellio.data.services.tenant.TenantLocal;
import com.constellio.data.utils.KeyIntMap;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.systemLogger.SystemLogger;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataNetworkLink;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.AggregatedDataEntry;
import com.constellio.model.entities.schemas.entries.AggregatedValuesEntry;
import com.constellio.model.entities.schemas.entries.DataEntry;
import com.constellio.model.entities.schemas.entries.InMemoryAggregatedValuesParams;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.BulkRecordTransactionHandler;
import com.constellio.model.services.records.BulkRecordTransactionHandlerOptions;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.aggregations.GetMetadatasUsedToCalculateParams;
import com.constellio.model.services.records.aggregations.MetadataAggregationHandler;
import com.constellio.model.services.records.aggregations.MetadataAggregationHandlerFactory;
import com.constellio.model.services.records.cache.CacheMemoryConsumptionReportBuilder;
import com.constellio.model.services.records.reindexing.SystemReindexingConsumptionInfos.SystemReindexingConsumptionHeapInfo;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.VisibilityStatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static com.constellio.data.conf.FoldersLocatorMode.PROJECT;
import static com.constellio.model.entities.enums.MemoryConsumptionLevel.LEAST_MEMORY_CONSUMPTION;
import static com.constellio.model.entities.enums.MemoryConsumptionLevel.LESS_MEMORY_CONSUMPTION;
import static com.constellio.model.entities.schemas.entries.AggregationType.REFERENCE_COUNT;
import static com.constellio.model.entities.schemas.entries.DataEntryType.AGGREGATED;
import static com.constellio.model.services.migrations.ConstellioEIMConfigs.WRITE_ZZRECORDS_IN_TLOG;
import static com.constellio.model.services.records.BulkRecordTransactionImpactHandling.NO_IMPACT_HANDLING;
import static com.constellio.model.services.records.RecordUtils.migrateMetadataTypeAndMultivalueOn;
import static com.constellio.model.services.records.RecordUtils.removeMetadataValuesOn;
import static com.constellio.model.services.records.reindexing.ReindexingSchemaTypeRecordsProvider.RecordReindexingStatus.IN_CURRENT_BATCH;
import static com.constellio.model.services.records.reindexing.ReindexingSchemaTypeRecordsProvider.RecordReindexingStatus.IN_PREVIOUS_BATCH;
import static com.constellio.model.services.records.reindexing.ReindexingSchemaTypeRecordsProvider.RecordReindexingStatus.LATER;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ReindexingServices {

	private static final TenantLocal<SystemReindexingInfos> reindexingInfos = new TenantLocal<>();

	private static final Logger LOGGER = LoggerFactory.getLogger(ReindexingServices.class);

	private static final String REINDEX_TYPES_SINGLE = "reindexTypes-single";
	private static final String REINDEX_TYPES_MULTI = "reindexTypes-multi";

	private RecordServices recordServices;
	private ModelLayerFactory modelLayerFactory;
	private SearchServices searchServices;
	private DataLayerFactory dataLayerFactory;

	private SecondTransactionLogManager logManager;

	private int mainThreadQueryRows;


	public ReindexingServices(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.searchServices = modelLayerFactory.newSearchServices();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.dataLayerFactory = modelLayerFactory.getDataLayerFactory();
		this.logManager = dataLayerFactory.getSecondTransactionLogManager();

		if (modelLayerFactory.getSystemConfigs().getMemoryConsumptionLevel() == LEAST_MEMORY_CONSUMPTION) {
			this.mainThreadQueryRows = 50;

		} else if (modelLayerFactory.getSystemConfigs().getMemoryConsumptionLevel() == LESS_MEMORY_CONSUMPTION) {
			this.mainThreadQueryRows = 100;

		} else {
			this.mainThreadQueryRows = modelLayerFactory.getConfiguration().getReindexingQueryBatchSize();
		}
	}

	public static boolean isReindexing() {
		return getReindexingInfos() != null;
	}

	public static SystemReindexingInfos getReindexingInfos() {
		return reindexingInfos.get();
	}

	public static void markReindexingHasFinished() {
		reindexingInfos.set(null);
	}

	public void reindexCollections(ReindexationMode reindexationMode) {
		reindexCollections(new ReindexationParams(reindexationMode));
	}

	public void reindexCollections(ReindexationParams params) {


		try {
			modelLayerFactory.getDataLayerFactory().getRecordsVaultServer().setResilienceModeToHigh();
			modelLayerFactory.getDataLayerFactory().getEventBusManager().pause();
			modelLayerFactory.getRecordsCaches().disableVolatileCache();
			dataLayerFactory.getDataLayerLogger().setQueryLoggingEnabled(false);
			//				if (dataLayerFactory.getTransactionLogReplicationFactorManager() != null) {
			//					dataLayerFactory.getTransactionLogReplicationFactorManager().setEnabled(false);
			//				}

			waitForCacheLoading();

			RecordDao recordDao = dataLayerFactory.newRecordDao();
			SystemLogger.info("Reindexing started");
			if (logManager != null && params.getReindexationMode().isFullRewrite()) {
				startNewTransactionalLog(recordDao, params);
			}

			//Expunge before
			recordDao.expungeDeletes();

			for (String collection : modelLayerFactory.getCollectionsListManager().getCollections()) {
				reindexCollection(collection, params);
				modelLayerFactory.getSecurityModelCache().invalidate(collection);
			}

			if (logManager != null && params.getReindexationMode().isFullRewrite()) {
				logManager.regroupAndMove();
				logManager.transactionLOGReindexationCleanupStrategy();
			}

			//Expunge after
			recordDao.expungeDeletes();

			deleteMetadatasMarkedForDeletion();

			modelLayerFactory.getBatchProcessesManager().cancelReindexingBatchProcesses();
			SystemLogger.info("Reindexing finished");

		} finally {

			reindexingInfos.set(null);
			//				if (dataLayerFactory.getTransactionLogReplicationFactorManager() != null) {
			//					dataLayerFactory.getTransactionLogReplicationFactorManager().setEnabled(true);
			//				}
			dataLayerFactory.getDataLayerLogger().setQueryLoggingEnabled(true);
			modelLayerFactory.getRecordsCaches().enableVolatileCache();
			modelLayerFactory.getDataLayerFactory().getEventBusManager().resume();
			modelLayerFactory.getDataLayerFactory().getRecordsVaultServer().setResilienceModeToNormal();
		}


	}

	private void waitForCacheLoading() {
		int waitedCounter = 0;
		while (!modelLayerFactory.getRecordsCaches().areSummaryCachesInitialized()
			   && modelLayerFactory.getConfiguration().isSummaryCacheEnabled()) {
			if (waitedCounter++ % 6 == 0) {
				LOGGER.info("Waiting end of cache loading to start reindexing");
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void startNewTransactionalLog(RecordDao recordDao, ReindexationParams params) {
		logManager.regroupAndMove();
		logManager.transactionLOGReindexationStartStrategy();

		try {

			List<RecordDTO> records = new ArrayList<>();

			if (params.isIncludeSolrPrivateKey() || !FoldersLocator.usingAppWrapper()) {
				records.add(recordDao.get("the_private_key"));
			}

			for (Map.Entry<String, Long> entry : dataLayerFactory.getSequencesManager().getSequences().entrySet()) {
				try {
					RecordDTO sequence = recordDao.realGet("seq_" + entry.getKey());
					records.add(sequence);
				} catch (Throwable t) {
					t.printStackTrace();
				}
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

		try {

			RecordUpdateOptions transactionOptions = new RecordUpdateOptions().setUpdateModificationInfos(false);
			transactionOptions.setValidationsEnabled(false).setCatchExtensionsValidationsErrors(true)
					.setCatchExtensionsExceptions(true).setCatchBrokenReferenceErrors(true)
					.setUpdateAggregatedMetadatas(false).setOverwriteModificationDateAndUser(false)
					.setRepopulate(params.isRepopulate());
			if (params.getReindexationMode().isFullRecalculation()) {
				transactionOptions.setForcedReindexationOfMetadatas(TransactionRecordsReindexation.ALL());
			}
			transactionOptions.setFullRewrite(params.getReindexationMode().isFullRewrite());

			reindexCollection(collection, params, transactionOptions);

		} finally {
			reindexingInfos.set(null);
		}

	}

	private void reindexCollection(String collection, ReindexationParams params,
								   RecordUpdateOptions transactionOptions) {
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);

		ReindexingAggregatedValuesTempStorage aggregatedValuesTempStorage = newReindexingAggregatedValuesTempStorage();

		BulkRecordTransactionHandlerOptions options = new BulkRecordTransactionHandlerOptions()
				.withBulkRecordTransactionImpactHandling(NO_IMPACT_HANDLING)
				.setTransactionOptions(transactionOptions).showProgressionInConsole(false);

		int batchSize = params.getBatchSize();
		if (batchSize == 0) {

			if (modelLayerFactory.getSystemConfigs().getMemoryConsumptionLevel() == LEAST_MEMORY_CONSUMPTION) {
				batchSize = 20;

			} else if (modelLayerFactory.getSystemConfigs().getMemoryConsumptionLevel() == LESS_MEMORY_CONSUMPTION) {
				batchSize = 20;

			} else {
				batchSize = modelLayerFactory.getConfiguration().getReindexingThreadBatchSize();
			}

		}
		options.withRecordsPerBatch(batchSize);

		int countOfBatchesFilledByASingleQuery = mainThreadQueryRows / batchSize;
		//The reader thread must have enough space in the queue to store the entire query result
		options.setQueueSize(countOfBatchesFilledByASingleQuery + 1);
		options.setMaxRecordsTotalSizePerBatch(modelLayerFactory.getConfiguration().getReindexingThreadMaxBatchMemorySize());

		try {
			int level = 0;
			while (isReindexingLevel(level, types)) {

				BulkRecordTransactionHandler singleThreadBulkTransactionHandler = new BulkRecordTransactionHandler(
						modelLayerFactory.newRecordServices(), REINDEX_TYPES_SINGLE, options.withNumberOfThreads(1));

				BulkRecordTransactionHandler multiThreadBulkTransactionHandler = new BulkRecordTransactionHandler(
						modelLayerFactory.newRecordServices(), REINDEX_TYPES_MULTI, options);

				ReindexingLogger logger = new ReindexingLogger(collection, LOGGER);
				LevelReindexingContext context = new LevelReindexingContext(singleThreadBulkTransactionHandler,
						multiThreadBulkTransactionHandler, logger, level, aggregatedValuesTempStorage, params);

				try {

					List<String> typesSortedByDependency = types.getSchemaTypesCodesSortedByDependency();

					if (level % 2 == 1) {
						List<String> reversedTypesSortedByDependency = new ArrayList<>(typesSortedByDependency);
						Collections.reverse(reversedTypesSortedByDependency);
						typesSortedByDependency = reversedTypesSortedByDependency;
					}


					for (String typeCode : typesSortedByDependency) {
						if (isReindexingOfTypeRequired(level, types, typeCode)) {
							logger.startingToReindexSchemaType(typeCode, level);

							MetadataSchemaType type = types.getSchemaType(typeCode);
							boolean writeZZrecords = modelLayerFactory.getSystemConfigurationsManager()
									.getValue(WRITE_ZZRECORDS_IN_TLOG);
							boolean typeReindexed = type.isInTransactionLog() || writeZZrecords;

							FoldersLocator foldersLocator = new FoldersLocator();
							if (typeReindexed && foldersLocator.getFoldersLocatorMode() == PROJECT) {
								//Running on dev computer
								typeReindexed = !Event.SCHEMA_TYPE.equals(type.getCode());
							}

							if (typeReindexed) {
								reindexCollectionType(context, type);
							}
						}
					}

				} finally {
					try {
						singleThreadBulkTransactionHandler.closeAndJoin();
					} catch (Throwable t) {
						SystemLogger.error("An error occured during the reindexing : ", t);
					}

					try {
						multiThreadBulkTransactionHandler.closeAndJoin();
					} catch (Throwable t) {
						SystemLogger.error("An error occured during the reindexing : ", t);
					}
				}
				modelLayerFactory.getDataLayerFactory().newRecordDao().removeOldLocks();
				level++;
			}
		} finally {
			aggregatedValuesTempStorage.clear();
			aggregatedValuesTempStorage = null;
		}
	}

	@NotNull
	private ReindexingAggregatedValuesTempStorage newReindexingAggregatedValuesTempStorage() {
		ReindexingAggregatedValuesTempStorage aggregatedValuesTempStorage;

		if (new ConstellioEIMConfigs(modelLayerFactory).getMemoryConsumptionLevel().isPrioritizingMemoryConsumptionOrNormal()) {
			File aggregatedValuesTempStorageFile = new FoldersLocator().getReindexingAggregatedValuesFolder();
			try {
				FileUtils.deleteDirectory(aggregatedValuesTempStorageFile);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			aggregatedValuesTempStorageFile.mkdirs();
			aggregatedValuesTempStorage = new FileSystemReindexingAggregatedValuesTempStorage(aggregatedValuesTempStorageFile);
		} else {
			aggregatedValuesTempStorage = new InMemoryReindexingAggregatedValuesTempStorage();
		}
		return aggregatedValuesTempStorage;
	}

	private boolean isReindexingOfTypeRequired(int level, MetadataSchemaTypes types, String typeCode) {
		MetadataSchemaType type = types.getSchemaType(typeCode);

		if (Event.SCHEMA_TYPE.equals(type.getCode())) {
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

	private void reindexCollectionType(LevelReindexingContext levelContext, MetadataSchemaType type) {

		SchemaTypeReindexingContext context = newSchemaTypeReindexingContext(levelContext, type);

		long lastCacheLogging = context.resultsCount < 1_000_000 ? new Date().getTime() : 0;
		Supplier<SystemReindexingConsumptionInfos> consumptionSupplier = newSystemReindexingConsumptionInfosSupplier(context);

		Integer nextBreakPoint = readBreakPoint(type);

		while (true) {
			Iterator<Record> recordsIterator = context.recordsProvider.startNewSchemaTypeIteration();


			while (recordsIterator.hasNext()) {
				reindexingInfos.set(new SystemReindexingInfos(type.getCollection(), type.getCode(), context.current.get(), context.resultsCount, consumptionSupplier));

				if (new Date().getTime() - lastCacheLogging > 30 * 60 * 1000) {
					lastCacheLogging = writeCacheReport();
				}

				while (nextBreakPoint != null && context.current.get() >= nextBreakPoint) {
					LOGGER.info("Reindexing is currently paused, waiting removal or modification of breakpoint file '/opt/constellio/work/reindexing-breakpoint.txt'");
					try {
						Thread.sleep(300_000);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					nextBreakPoint = readBreakPoint(type);
				}

				for (Record record : context.recordsProvider.getPostponedRecordsReadyForReindexing()) {
					reindexRecord(context, record);
				}

				reindexRecord(context, recordsIterator.next());

			}

			while (context.recordsProvider.hasPostponedRecords()) {
				for (Record record : context.recordsProvider.getPostponedRecordsReadyForReindexing()) {
					reindexRecord(context, record);
				}
				try {
					context.getBulkTransactionHandler().barrier();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}

			try {
				context.getBulkTransactionHandler().barrier();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			modelLayerFactory.newRecordServices().flush();
			context.recordsProvider.markIterationAsFinished();
			levelContext.logger.onEndOfIteration(context.recordsProvider.getSkippedRecordsCount(), context.recordsProvider.getCurrentIteration());
			if (!context.recordsProvider.isRequiringAnotherIteration()) {
				markTypeOrMultivalueMigrationHasDoneAndUpdateSchema(type, context.metadatasMarkedForTypeOrMutlivalueMigration);
				return;
			}

		}

	}

	private long writeCacheReport() {

		if (!FoldersLocator.usingAppWrapper()) {
			return new Date().getTime();
		}

		long lastCacheLogging;
		String cacheReport = new CacheMemoryConsumptionReportBuilder(modelLayerFactory).build();
		LOGGER.info(cacheReport);

		String date = new LocalDateTime().toString();
		File reportFile = new File(new FoldersLocator().getWorkFolder(), "cacheReport-" + date + ".txt");

		try {
			FileUtils.write(reportFile, cacheReport);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		lastCacheLogging = new Date().getTime();
		return lastCacheLogging;
	}

	@NotNull
	private SchemaTypeReindexingContext newSchemaTypeReindexingContext(LevelReindexingContext levelContext,
																	   MetadataSchemaType type) {
		MetadataSchemaTypes types = type.getSchemaTypes();
		long resultsCount = searchServices.getResultsCount(new LogicalSearchQuery(from(type).returnAll())
				.filteredByVisibilityStatus(VisibilityStatusFilter.ALL));

		//If 2% or less are skipped, they are retrieved using getById
		int threshold = Math.max(1_000, 2 * (int) resultsCount / 100);

		ReindexingSchemaTypeRecordsProvider recordsProvider = new ReindexingSchemaTypeRecordsProvider(
				modelLayerFactory, mainThreadQueryRows, levelContext, type, threshold);


		List<Metadata> selfReferenceParentMetadatas = type.getAllMetadatas().onlyParentReferences().onlyReferencesToType(type.getCode());
		List<Metadata> metadatasMarkedForDeletion = type.getAllMetadatas().onlyMarkedForDeletion();
		MetadataList metadatasMarkedForTypeOrMutlivalueMigration = type.getAllMetadatas()
				.only(metadata -> metadata.getMarkedForMigrationToType() != null || metadata.isMarkedForMigrationToMultivalue() != null);

		Map<String, List<MetadataNetworkLink>> allAggregationLinksToCurrentSchemaType =
				types.getMetadataNetwork().getAggregationMetadataNetworkLinkRegroupedByReference(type.getCode());

		List<MetadataNetworkLink> allAggregationLinksFromCurrentSchemaType =
				types.getMetadataNetwork().getAggregationMetadataNetworkLinksFromSchemaType(type.getCode());

		AtomicLong current = new AtomicLong();

		levelContext.multiThreadBulkTransactionHandler.unregisterCompletedTransactionCallBacks();
		if (!selfReferenceParentMetadatas.isEmpty()) {
			levelContext.multiThreadBulkTransactionHandler.registerCompletedTransactionCallBack(recordsProvider::markTransactionRecordAsHandled);
		}
		return new SchemaTypeReindexingContext(levelContext.logger,
				levelContext.aggregatedValuesTempStorage, levelContext.params, type, recordsProvider, selfReferenceParentMetadatas,
				metadatasMarkedForDeletion, metadatasMarkedForTypeOrMutlivalueMigration,
				allAggregationLinksToCurrentSchemaType, allAggregationLinksFromCurrentSchemaType, resultsCount, current, levelContext, new KeyIntMap<>());
	}

	@AllArgsConstructor
	static class SchemaTypeReindexingContext {

		ReindexingLogger logger;
		ReindexingAggregatedValuesTempStorage aggregatedValuesTempStorage;
		ReindexationParams params;
		MetadataSchemaType type;
		ReindexingSchemaTypeRecordsProvider recordsProvider;
		List<Metadata> selfReferenceParentMetadatas;
		List<Metadata> metadatasMarkedForDeletion;
		MetadataList metadatasMarkedForTypeOrMutlivalueMigration;
		Map<String, List<MetadataNetworkLink>> allAggregationLinksToCurrentSchemaType;
		List<MetadataNetworkLink> allAggregationLinksFromCurrentSchemaType;
		long resultsCount;
		AtomicLong current;
		LevelReindexingContext levelContext;
		KeyIntMap<String> postponingCounter;

		public void appendRecordForReindexing(Record record) {
			getBulkTransactionHandler().append(record);
		}


		public BulkRecordTransactionHandler getBulkTransactionHandler() {
			if (recordsProvider.isUseSingleThread()) {
				return levelContext.singleThreadBulkTransactionHandler;
			} else {
				return levelContext.multiThreadBulkTransactionHandler;
			}
		}

		public void postpone(Record record, RecordId parentId) {

			if (!recordsProvider.tryToPostpone(record, parentId)) {
				LOGGER.info("Could not postpone record, limit achieved. Waiting...");
				getBulkTransactionHandler().pushCurrent();
				while (!recordsProvider.tryToPostpone(record, parentId)) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}

			}
		}
	}

	@AllArgsConstructor
	static class LevelReindexingContext {
		BulkRecordTransactionHandler singleThreadBulkTransactionHandler;
		BulkRecordTransactionHandler multiThreadBulkTransactionHandler;
		ReindexingLogger logger;
		int level;
		ReindexingAggregatedValuesTempStorage aggregatedValuesTempStorage;
		ReindexationParams params;

	}

	private void reindexRecord(SchemaTypeReindexingContext context, Record record) {
		if (dataLayerFactory.getTransactionLogReplicationFactorManager() != null && dataLayerFactory.getTransactionLogReplicationFactorManager().isDegraded()) {
			LOGGER.warn("Degradation detected, pausing reindexation for 30 seconds...");
			try {
				TimeUnit.SECONDS.sleep(30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (context.params.getReindexationMode().isFullRecalculation()) {
			record.set(Schemas.MARKED_FOR_REINDEXING, null);
		}
		removeMetadataValuesOn(context.metadatasMarkedForDeletion, record);
		migrateMetadataTypeAndMultivalueOn(context.metadatasMarkedForTypeOrMutlivalueMigration, record);

		if (context.levelContext.level % 2 == 0) {
			reindexRecordInDependencyDirection(context, record);
		} else {
			reindexRecordInReverseDependencyDirection(context, record);

		}
	}

	private void reindexRecordInReverseDependencyDirection(SchemaTypeReindexingContext context, Record record) {
		context.current.incrementAndGet();

		boolean aggregatedMetadataModified = false;
		for (MetadataNetworkLink linkOfAggregatedMetadataToUpdate : context.allAggregationLinksFromCurrentSchemaType) {
			aggregatedMetadataModified = updateAggregatedMetadata(record,
					linkOfAggregatedMetadataToUpdate.getFromMetadata(), context.aggregatedValuesTempStorage);
		}

		if (aggregatedMetadataModified) {
			recordServices.recalculate(record);
		}

		for (String refMetadataLocalCode : context.allAggregationLinksToCurrentSchemaType.keySet()) {
			List<MetadataNetworkLink> links = context.allAggregationLinksToCurrentSchemaType.get(refMetadataLocalCode);
			Metadata refMetadata = context.type.getSchemaTypes().getMetadata(refMetadataLocalCode);
			List<String> referencedRecordIds = record.getValues(refMetadata);
			boolean sameTypeReference = refMetadata.getReferencedSchemaTypeCode().equals(refMetadata.getSchemaTypeCode());

			for (String referencedRecordId : referencedRecordIds) {

				if (sameTypeReference

					//&& context.recordsProvider.isAlreadyHandledInCurrentOrPreviousBatch(referencedRecordId)
					//Francis : I removed current batch, since this could create invalid agregated values
					&& context.recordsProvider.getRecordStatus(RecordId.id(referencedRecordId)) != LATER
					&& !referencedRecordId.equals(record.getId())) {

					LOGGER.info("Record " + referencedRecordId + " will be recalculated");
					int skippedRecordsSize = context.recordsProvider.markRecordAsSkipped(referencedRecordId);
					context.current.decrementAndGet();
					context.logger.updateSkipsCount(skippedRecordsSize);
				}
				for (MetadataNetworkLink link : links) {
					context.aggregatedValuesTempStorage.addOrReplace(referencedRecordId, record.getId(),
							link.getToMetadata().getLocalCode(), record.getValues(link.getToMetadata()));
				}

			}
		}

		context.logger.updateProgression(context.current.get(), context.resultsCount);
		context.appendRecordForReindexing(record);
		context.recordsProvider.markRecordAsHandledSoon(record);
	}

	private void reindexRecordInDependencyDirection(SchemaTypeReindexingContext context, Record record) {

		boolean postponed = false;
		if (context.selfReferenceParentMetadatas.isEmpty()) {
			context.logger.updateProgression(context.current.incrementAndGet(), context.resultsCount);
			context.appendRecordForReindexing(record);

		} else {
			String parentStrId = getParentIdOfSameType(context.selfReferenceParentMetadatas, record);
			RecordId parentId = parentStrId == null ? null : RecordId.id(parentStrId);
			if (parentId == null
				|| context.recordsProvider.getRecordStatus(parentId) == IN_PREVIOUS_BATCH
				|| parentId.equals(record.getId())) {
				context.logger.updateProgression(context.current.incrementAndGet(), context.resultsCount);
				context.appendRecordForReindexing(record);
				context.recordsProvider.markRecordAsHandledSoon(record);

			} else if (context.recordsProvider.getRecordStatus(parentId) == IN_CURRENT_BATCH) {
				//Since reindexing is executed in parallel, the dependent record may be handled by another thread.
				// Joining this thread is not a good idea since the thread could also join this one, causing a deadlock
				if (context.recordsProvider.isUseSingleThread()) {
					//Only thread is used, the barrier will flush
					try {
						context.getBulkTransactionHandler().barrier();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					context.logger.updateProgression(context.current.incrementAndGet(), context.resultsCount);
					context.appendRecordForReindexing(record);
					context.recordsProvider.markRecordAsHandledSoon(record);
				} else {

					context.postpone(record, parentId);

					postponed = true;
				}

			} else {
				if (RecordId.isIntId(record.getId()) && parentId.isInteger()) {
					context.postpone(record, parentId);
					postponed = true;
				} else {
					int skippedRecordsSize = context.recordsProvider.markRecordAsSkipped(record.getId());
					context.logger.updateSkipsCount(skippedRecordsSize);
				}


			}
		}

		if (postponed) {
			if (context.recordsProvider.postponedBlocked + context.recordsProvider.postponedReady > 1000) {
				context.getBulkTransactionHandler().pushCurrent();
			}
		} else {
			for (String refMetadataLocalCode : context.allAggregationLinksToCurrentSchemaType.keySet()) {
				List<MetadataNetworkLink> links = context.allAggregationLinksToCurrentSchemaType.get(refMetadataLocalCode);
				Metadata refMetadata = context.type.getSchemaTypes().getMetadata(refMetadataLocalCode);
				List<String> referencedRecordIds = record.getValues(refMetadata);

				for (String referencedRecordId : referencedRecordIds) {
					for (MetadataNetworkLink link : links) {
						DataEntry dataEntry = link.getFromMetadata().getDataEntry();
						if (dataEntry.getType() == AGGREGATED &&
							((AggregatedDataEntry) dataEntry).getAgregationType().equals(REFERENCE_COUNT)) {
							String aggregatedMetadataLocalCode = link.getFromMetadata().getLocalCode();
							context.aggregatedValuesTempStorage.incrementReferenceCount(referencedRecordId, aggregatedMetadataLocalCode);
						}
						context.aggregatedValuesTempStorage.addOrReplace(referencedRecordId, record.getId(),
								link.getToMetadata().getLocalCode(), record.getValues(link.getToMetadata()));
					}
				}
			}
		}
	}

	@NotNull
	private Supplier<SystemReindexingConsumptionInfos> newSystemReindexingConsumptionInfosSupplier(
			SchemaTypeReindexingContext context) {
		long maxRecordSize = searchServices.getMaxRecordSize(context.type);
		long sizePerBatch = maxRecordSize * context.getBulkTransactionHandler().getRecordsPerBatch();
		long sizeOfThreads = sizePerBatch * context.getBulkTransactionHandler().getNumberOfThreads();
		long sizeOfQueue = sizePerBatch * context.getBulkTransactionHandler().getMaxQueueSize();
		long sizeOfIterator = maxRecordSize * context.recordsProvider.getMainThreadQueryRows();

		Supplier<SystemReindexingConsumptionInfos> consumptionSupplier = () -> {
			SystemReindexingConsumptionInfos infos = new SystemReindexingConsumptionInfos();

			infos.getHeapInfos().add(new SystemReindexingConsumptionHeapInfo("Max record size ", maxRecordSize, false));
			infos.getHeapInfos().add(new SystemReindexingConsumptionHeapInfo("Records per batch", context.getBulkTransactionHandler().getRecordsPerBatch(), false));
			infos.getHeapInfos().add(new SystemReindexingConsumptionHeapInfo("Number of threads", context.getBulkTransactionHandler().getNumberOfThreads(), false));
			infos.getHeapInfos().add(new SystemReindexingConsumptionHeapInfo("Size of queue", context.getBulkTransactionHandler().getMaxQueueSize(), false));

			infos.getHeapInfos().add(new SystemReindexingConsumptionHeapInfo("Queue (Estimation based on configs and max record size)", sizeOfQueue, true));
			infos.getHeapInfos().add(new SystemReindexingConsumptionHeapInfo("WriteThreads (Estimation based on configs and max record size)", sizeOfThreads, true));
			infos.getHeapInfos().add(new SystemReindexingConsumptionHeapInfo("ReadIterator (Estimation based on configs and max record size)", sizeOfIterator, true));
			context.aggregatedValuesTempStorage.populateCacheConsumptionInfos(infos);
			return infos;
		};
		return consumptionSupplier;
	}

	private void markTypeOrMultivalueMigrationHasDoneAndUpdateSchema(MetadataSchemaType type,
																	 MetadataList metadatasMarkedForTypeAndMutlivalueMigration) {

		if (!metadatasMarkedForTypeAndMutlivalueMigration.isEmpty()) {
			modelLayerFactory.getMetadataSchemasManager().modify(type.getCollection(), (MetadataSchemaTypesAlteration) types -> {

				Set<MetadataBuilder> allMetadatas = types.getSchemaType(type.getCode()).getAllMetadatas();
				metadatasMarkedForTypeAndMutlivalueMigration.stream().forEach(mm -> {
					allMetadatas.stream().filter(mb -> mm.isSame(mb.getOriginalMetadata()))
							.findFirst().ifPresent(m -> {
						if (mm.getMarkedForMigrationToType() != null) {
							m.setTypeWithoutValidation(mm.getMarkedForMigrationToType());
						}
						if (mm.isMarkedForMigrationToMultivalue() != null) {
							m.setMultivalue(Boolean.TRUE.equals(mm.isMarkedForMigrationToMultivalue()));
						}

						m.setMarkedForMigrationToType(null).setMarkedForMigrationToMultivalue(null);
					});
				});
			});
		}
	}

	private Integer readBreakPoint(MetadataSchemaType type) {
		FoldersLocator foldersLocator = new FoldersLocator();
		File breakpointFile = new File(foldersLocator.getWorkFolder(), "reindexing-breakpoint.txt");
		if (breakpointFile.exists()) {
			try {
				List<String> lines = FileUtils.readLines(breakpointFile, "UTF-8");
				if (!lines.isEmpty()) {
					String content = lines.get(0).trim();
					if (StringUtils.isNotBlank(content) &&
						(content.startsWith(type.getCode() + ":") || content.startsWith(type.getCollection() + ":" + type.getCode() + ":"))) {
						return Integer.valueOf(StringUtils.substringAfterLast(content, ":"));
					}
				}
			} catch (Throwable t) {
				LOGGER.error("ERROR reading breakpoint file", t);
			}
		}

		return null;
	}

	private boolean updateAggregatedMetadata(final Record record, final Metadata aggregatingMetadata,
											 final ReindexingAggregatedValuesTempStorage aggregatedValuesTempStorage) {

		final MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(record.getCollection());
		MetadataAggregationHandler handler = MetadataAggregationHandlerFactory.getHandlerFor(aggregatingMetadata);

		GetMetadatasUsedToCalculateParams calculateParams = new GetMetadatasUsedToCalculateParams(aggregatingMetadata) {
			@Override
			public Metadata getMetadata(String metadataCode) {
				return types.getMetadata(metadataCode);
			}
		};

		List<Metadata> metadatasUsedToCalculate = handler.getMetadatasUsedToCalculate(calculateParams);
		List<Object> values = new ArrayList<>();
		for (Metadata metadata : metadatasUsedToCalculate) {
			values.addAll(aggregatedValuesTempStorage.getAllValues(record.getId(), metadata.getLocalCode()));
		}

		InMemoryAggregatedValuesParams params = new InMemoryAggregatedValuesParams(record.getId(), aggregatingMetadata, values) {

			@Override
			public List<AggregatedValuesEntry> getEntries() {
				return aggregatedValuesTempStorage.getAllEntriesWithValues(record.getId());
			}

			@Override
			public int getReferenceCount() {
				return aggregatedValuesTempStorage.getReferenceCount(record.getId(), aggregatingMetadata.getLocalCode());
			}
		};
		Object aggregatedValue = handler.calculate(params);
		Object currentRecordValue = record.get(aggregatingMetadata);
		if (!LangUtils.isEqual(aggregatedValue, currentRecordValue)) {
			((RecordImpl) record).updateAutomaticValue(aggregatingMetadata, aggregatedValue);
			return true;
		} else {
			return false;
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

	public boolean isLockFileExisting() {
		return modelLayerFactory.getFoldersLocator().getReindexationLock().exists();
	}

	public void removeLockFile() {
		File reindexationLock = modelLayerFactory.getFoldersLocator().getReindexationLock();
		modelLayerFactory.getIOServicesFactory().newFileService().deleteQuietly(reindexationLock);
	}

	public void createLockFile() {
		File reindexationLock = modelLayerFactory.getFoldersLocator().getReindexationLock();
		reindexationLock.getParentFile().mkdirs();
		try {
			reindexationLock.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void resetLogVersionSql() throws SQLException {
		this.dataLayerFactory.getSqlRecordDao().getRecordDao(SqlRecordDaoType.TRANSACTIONS).resetVersion();
	}
}