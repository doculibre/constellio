package com.constellio.model.services.records.reindexing;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.RecordDaoException.NoSuchRecordWithId;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.sql.SqlRecordDaoType;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogManager;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.systemLogger.SystemLogger;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.batchprocess.RecordBatchProcess;
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
import com.constellio.model.services.background.RecordsReindexingBackgroundAction;
import com.constellio.model.services.batch.actions.ReindexMetadatasBatchProcessAction;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
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
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.VisibilityStatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
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
import java.util.function.Supplier;

import static com.constellio.model.conf.FoldersLocatorMode.PROJECT;
import static com.constellio.model.entities.enums.MemoryConsumptionLevel.LEAST_MEMORY_CONSUMPTION;
import static com.constellio.model.entities.enums.MemoryConsumptionLevel.LESS_MEMORY_CONSUMPTION;
import static com.constellio.model.entities.schemas.Schemas.SCHEMA;
import static com.constellio.model.entities.schemas.entries.AggregationType.REFERENCE_COUNT;
import static com.constellio.model.entities.schemas.entries.DataEntryType.AGGREGATED;
import static com.constellio.model.services.migrations.ConstellioEIMConfigs.WRITE_ZZRECORDS_IN_TLOG;
import static com.constellio.model.services.records.BulkRecordTransactionImpactHandling.NO_IMPACT_HANDLING;
import static com.constellio.model.services.records.RecordUtils.removeMetadataValuesOn;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

public class ReindexingServices {

	private static SystemReindexingInfos REINDEXING_INFOS;

	private static final Logger LOGGER = LoggerFactory.getLogger(ReindexingServices.class);

	private static final String REINDEX_TYPES = "reindexTypes";

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


		if (params.isBackground()) {
			try {
				BatchProcessesManager batchProcessesManager = modelLayerFactory.getBatchProcessesManager();
				for (MetadataSchemaType schemaType : params.getReindexedSchemaTypes()) {
					BatchProcessAction action = ReindexMetadatasBatchProcessAction.allMetadatas();
					batchProcessesManager.addPendingBatchProcess(from(schemaType).returnAll(), action, "reindexing");
				}

			} finally {
				REINDEXING_INFOS = null;
			}

		} else {
			try {
				modelLayerFactory.getDataLayerFactory().getEventBusManager().pause();
				modelLayerFactory.getRecordsCaches().disableVolatileCache();
				dataLayerFactory.getDataLayerLogger().setQueryLoggingEnabled(false);
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

				SystemLogger.info("Reindexing started");
				if (logManager != null && params.getReindexationMode().isFullRewrite()) {
					logManager.regroupAndMove();
					logManager.transactionLOGReindexationStartStrategy();
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
					modelLayerFactory.getSecurityModelCache().invalidate(collection);
				}

				if (logManager != null && params.getReindexationMode().isFullRewrite()) {
					logManager.regroupAndMove();
					logManager.transactionLOGReindexationCleanupStrategy();
				}

				RecordDao recordDao = modelLayerFactory.getDataLayerFactory().newRecordDao();
				recordDao.expungeDeletes();

				deleteMetadatasMarkedForDeletion();

				BatchProcessesManager batchProcessesManager = modelLayerFactory.getBatchProcessesManager();
				for (BatchProcess batchProcess : new ArrayList<>(batchProcessesManager.getAllNonFinishedBatchProcesses())) {
					if (batchProcess instanceof RecordBatchProcess) {
						if (((RecordBatchProcess) batchProcess).getAction() instanceof RecordsReindexingBackgroundAction) {
							try {
								batchProcessesManager.cancelBatchProcessNoMatterItStatus(batchProcess);
							} catch (Exception e) {
								LOGGER.info("Failed to cancel reindexing batch process");
							}
						}
					}
				}
				SystemLogger.info("Reindexing finished");

			} finally {

				REINDEXING_INFOS = null;

				dataLayerFactory.getDataLayerLogger().setQueryLoggingEnabled(true);
				modelLayerFactory.getRecordsCaches().enableVolatileCache();
				modelLayerFactory.getDataLayerFactory().getEventBusManager().resume();
			}
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
			if (!params.getReindexedSchemaTypes().isEmpty()) {
				long count = getRecordCountOfType(collection, params.getReindexedSchemaTypes());
				if (count == 0) {
					return;
				}
			}

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
			REINDEXING_INFOS = null;
		}

	}

	private long getRecordCountOfType(String collection, List<MetadataSchemaType> reindexedSchemaTypes) {
		List<LogicalSearchValueCondition> conditions = new ArrayList<>();

		for (MetadataSchemaType reindexedSchemaType : reindexedSchemaTypes) {
			conditions.add(LogicalSearchQueryOperators.startingWithText(reindexedSchemaType.getCode()));
		}

		LogicalSearchCondition condition = fromAllSchemasIn(collection).where(SCHEMA).isAny(conditions);
		return modelLayerFactory.newSearchServices().getResultsCount(new LogicalSearchQuery(condition)
				.filteredByVisibilityStatus(VisibilityStatusFilter.ALL));
	}

	private void reindexCollection(String collection, ReindexationParams params,
								   RecordUpdateOptions transactionOptions) {
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);

		ReindexingRecordsProvider recordsProvider = new ReindexingRecordsProvider(modelLayerFactory, mainThreadQueryRows);
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

				BulkRecordTransactionHandler bulkTransactionHandler = new BulkRecordTransactionHandler(
						modelLayerFactory.newRecordServices(), REINDEX_TYPES, options);

				ReindexingLogger logger = new ReindexingLogger(collection, LOGGER);
				try {

					List<String> typesSortedByDependency = types.getSchemaTypesSortedByDependency();

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
								reindexCollectionType(bulkTransactionHandler, type, logger,
										level, recordsProvider, aggregatedValuesTempStorage, params);
							}
						}
					}

				} finally {
					bulkTransactionHandler.closeAndJoin();
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

	private void reindexCollectionType(BulkRecordTransactionHandler bulkTransactionHandler, MetadataSchemaType type,
									   ReindexingLogger logger, int level,
									   ReindexingRecordsProvider collectionRecordsProvider,
									   ReindexingAggregatedValuesTempStorage aggregatedValuesTempStorage,
									   ReindexationParams params) {

		MetadataSchemaTypes types = type.getSchemaTypes();
		long counter = searchServices.getResultsCount(new LogicalSearchQuery(from(type).returnAll())
				.filteredByVisibilityStatus(VisibilityStatusFilter.ALL));

		//If 2% or less are skipped, they are retrieved using getById
		int threshold = Math.max(1_000, 2 * (int) counter / 100);

		ReindexingSchemaTypeRecordsProvider recordsProvider = collectionRecordsProvider.newSchemaTypeProvider(type, level, threshold);
		List<Metadata> metadatas = type.getAllMetadatas().onlyParentReferences().onlyReferencesToType(type.getCode());
		List<Metadata> metadatasMarkedForDeletion = type.getAllMetadatas().onlyMarkedForDeletion();

		Map<String, List<MetadataNetworkLink>> allAggregationLinksToCurrentSchemaType =
				types.getMetadataNetwork().getAggregationMetadataNetworkLinkRegroupedByReference(type.getCode());

		List<MetadataNetworkLink> allAggregationLinksFromCurrentSchemaType =
				types.getMetadataNetwork().getAggregationMetadataNetworkLinksFromSchemaType(type.getCode());

		long lastCacheLogging = counter < 1_000_000 ? new Date().getTime() : 0;

		long current = 0;

		long maxRecordSize = searchServices.getMaxRecordSize(type);
		long sizePerBatch = maxRecordSize * bulkTransactionHandler.getRecordsPerBatch();
		long sizeOfThreads = sizePerBatch * bulkTransactionHandler.getNumberOfThreads();
		long sizeOfQueue = sizePerBatch * bulkTransactionHandler.getMaxQueueSize();
		long sizeOfIterator = maxRecordSize * recordsProvider.getMainThreadQueryRows();

		Supplier<SystemReindexingConsumptionInfos> consumptionSupplier = () -> {
			SystemReindexingConsumptionInfos infos = new SystemReindexingConsumptionInfos();

			infos.getHeapInfos().add(new SystemReindexingConsumptionHeapInfo("Max record size ", maxRecordSize, false));
			infos.getHeapInfos().add(new SystemReindexingConsumptionHeapInfo("Records per batch", bulkTransactionHandler.getRecordsPerBatch(), false));
			infos.getHeapInfos().add(new SystemReindexingConsumptionHeapInfo("Number of threads", bulkTransactionHandler.getNumberOfThreads(), false));
			infos.getHeapInfos().add(new SystemReindexingConsumptionHeapInfo("Size of queue", bulkTransactionHandler.getMaxQueueSize(), false));

			infos.getHeapInfos().add(new SystemReindexingConsumptionHeapInfo("Queue (Estimation based on configs and max record size)", sizeOfQueue, true));
			infos.getHeapInfos().add(new SystemReindexingConsumptionHeapInfo("WriteThreads (Estimation based on configs and max record size)", sizeOfThreads, true));
			infos.getHeapInfos().add(new SystemReindexingConsumptionHeapInfo("ReadIterator (Estimation based on configs and max record size)", sizeOfIterator, true));
			aggregatedValuesTempStorage.populateCacheConsumptionInfos(infos);
			return infos;
		};

		Integer nextBreakPoint = readBreakPoint(type);

		REINDEXING_TYPE:
		while (true) {
			//LOGGER.info("starting a new iteration");
			Iterator<Record> recordsIterator = recordsProvider.startNewSchemaTypeIteration();
			while (recordsIterator.hasNext()) {
				REINDEXING_INFOS = new SystemReindexingInfos(type.getCollection(), type.getCode(), current, counter, consumptionSupplier);

				if (new Date().getTime() - lastCacheLogging > 30 * 60 * 1000) {
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
				}

				while (nextBreakPoint != null && current >= nextBreakPoint) {
					LOGGER.info("Reindexing is currently paused, waiting removal or modification of breakpoint file '/opt/constellio/work/reindexing-breakpoint.txt'");
					try {
						Thread.sleep(300_000);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					nextBreakPoint = readBreakPoint(type);
				}

				Record record = recordsIterator.next();
				if (params.getReindexationMode().isFullRecalculation()) {
					record.set(Schemas.MARKED_FOR_REINDEXING, null);
				}
				removeMetadataValuesOn(metadatasMarkedForDeletion, record);

				if (recordsProvider.dependencyLevel % 2 == 0) {
					if (metadatas.isEmpty()) {
						current++;
						logger.updateProgression(current, counter);
						bulkTransactionHandler.append(record);

					} else {
						String parentId = getParentIdOfSameType(metadatas, record);
						if (parentId == null || recordsProvider.isAlreadyHandledInPreviousBatch(parentId) || parentId
								.equals(record.getId())) {
							current++;
							logger.updateProgression(current, counter);
							bulkTransactionHandler.append(record);
							recordsProvider.markRecordAsHandled(record);

						} else {
							int skippedRecordsSize = recordsProvider.markRecordAsSkipped(record.getId());
							logger.updateSkipsCount(skippedRecordsSize);

						}
					}

					for (String refMetadataLocalCode : allAggregationLinksToCurrentSchemaType.keySet()) {
						List<MetadataNetworkLink> links = allAggregationLinksToCurrentSchemaType.get(refMetadataLocalCode);
						Metadata refMetadata = types.getMetadata(refMetadataLocalCode);
						List<String> referencedRecordIds = record.getValues(refMetadata);

						for (String referencedRecordId : referencedRecordIds) {
							for (MetadataNetworkLink link : links) {
								DataEntry dataEntry = link.getFromMetadata().getDataEntry();
								if (dataEntry.getType() == AGGREGATED &&
									((AggregatedDataEntry) dataEntry).getAgregationType().equals(REFERENCE_COUNT)) {
									String aggregatedMetadataLocalCode = link.getFromMetadata().getLocalCode();
									aggregatedValuesTempStorage.incrementReferenceCount(referencedRecordId, aggregatedMetadataLocalCode);
								}
								aggregatedValuesTempStorage.addOrReplace(referencedRecordId, record.getId(),
										link.getToMetadata().getLocalCode(), record.getValues(link.getToMetadata()));
							}
						}
					}
				} else {
					current++;

					boolean aggregatedMetadataModified = false;
					for (MetadataNetworkLink linkOfAggregatedMetadataToUpdate : allAggregationLinksFromCurrentSchemaType) {
						aggregatedMetadataModified = updateAggregatedMetadata(record,
								linkOfAggregatedMetadataToUpdate.getFromMetadata(), aggregatedValuesTempStorage);
					}

					if (aggregatedMetadataModified) {
						recordServices.recalculate(record);
					}

					for (String refMetadataLocalCode : allAggregationLinksToCurrentSchemaType.keySet()) {
						List<MetadataNetworkLink> links = allAggregationLinksToCurrentSchemaType.get(refMetadataLocalCode);
						Metadata refMetadata = types.getMetadata(refMetadataLocalCode);
						List<String> referencedRecordIds = record.getValues(refMetadata);

						for (String referencedRecordId : referencedRecordIds) {

							if (hasLinkWithinSameSchemaType(links)
								&& recordsProvider.isAlreadyHandledInCurrentOrPreviousBatch(referencedRecordId)
								&& !referencedRecordId.equals(record.getId())) {

								LOGGER.info("Record " + referencedRecordId + " will be recalculated");
								int skippedRecordsSize = recordsProvider.markRecordAsSkipped(referencedRecordId);
								current--;
								logger.updateSkipsCount(skippedRecordsSize);
							}
							for (MetadataNetworkLink link : links) {
								aggregatedValuesTempStorage.addOrReplace(referencedRecordId, record.getId(),
										link.getToMetadata().getLocalCode(), record.getValues(link.getToMetadata()));
							}

						}
					}

					logger.updateProgression(current, counter);
					bulkTransactionHandler.append(record);
					recordsProvider.markRecordAsHandled(record);

				}

			}

			try {
				bulkTransactionHandler.barrier();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			modelLayerFactory.newRecordServices().flush();
			recordsProvider.markIterationAsFinished();
			logger.onEndOfIteration(recordsProvider.getSkippedRecordsCount(), recordsProvider.getCurrentIteration());
			if (!recordsProvider.isRequiringAnotherIteration()) {
				break REINDEXING_TYPE;
			}

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

	private boolean hasLinkWithinSameSchemaType(List<MetadataNetworkLink> links) {
		for (MetadataNetworkLink link : links) {
			if (link.isWithingSameSchemaType()) {
				return true;
			}
		}
		return false;
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