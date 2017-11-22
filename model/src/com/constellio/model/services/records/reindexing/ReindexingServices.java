package com.constellio.model.services.records.reindexing;

import static com.constellio.model.conf.FoldersLocatorMode.PROJECT;
import static com.constellio.model.entities.schemas.Schemas.SCHEMA;
import static com.constellio.model.services.migrations.ConstellioEIMConfigs.WRITE_ZZRECORDS_IN_TLOG;
import static com.constellio.model.services.records.BulkRecordTransactionImpactHandling.NO_IMPACT_HANDLING;
import static com.constellio.model.services.records.RecordUtils.removeMetadataValuesOn;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

import java.util.ArrayList;
import java.util.Collections;
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
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.Octets;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataNetworkLink;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.InMemoryAggregatedValuesParams;
import com.constellio.model.services.batch.actions.ReindexMetadatasBatchProcessAction;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.BulkRecordTransactionHandler;
import com.constellio.model.services.records.BulkRecordTransactionHandlerOptions;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.aggregations.GetMetadatasUsedToCalculateParams;
import com.constellio.model.services.records.aggregations.MetadataAggregationHandler;
import com.constellio.model.services.records.aggregations.MetadataAggregationHandlerFactory;
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
				.setUpdateAggregatedMetadatas(false);
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

		ReindexingRecordsProvider recordsProvider = new ReindexingRecordsProvider(modelLayerFactory, mainThreadQueryRows);
		ReindexingAggregatedValuesTempStorage aggregatedValuesTempStorage = new InMemoryReindexingAggregatedValuesTempStorage();
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
							reindexCollectionType(bulkTransactionHandler, types, logger,
									recordsProvider.newSchemaTypeProvider(type, level), aggregatedValuesTempStorage, params);
						}
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
			ReindexingLogger logger, ReindexingSchemaTypeRecordsProvider recordsProvider,
			ReindexingAggregatedValuesTempStorage aggregatedValuesTempStorage, ReindexationParams params) {

		MetadataSchemaType type = recordsProvider.type;
		long counter = searchServices.getResultsCount(new LogicalSearchQuery(from(type).returnAll()));
		List<Metadata> metadatas = type.getAllMetadatas().onlyParentReferences().onlyReferencesToType(type.getCode());
		List<Metadata> metadatasMarkedForDeletion = type.getAllMetadatas().onlyMarkedForDeletion();

		Map<String, List<MetadataNetworkLink>> allAggregationLinksToCurrentSchemaType =
				types.getMetadataNetwork().getAggregationMetadataNetworkLinkRegroupedByReference(recordsProvider.type.getCode());

		List<MetadataNetworkLink> allAggregationLinksFromCurrentSchemaType =
				types.getMetadataNetwork().getAggregationMetadataNetworkLinksFromSchemaType(recordsProvider.type.getCode());

		long current = 0;
		REINDEXING_TYPE:

		while (true) {
			//LOGGER.info("starting a new iteration");
			Iterator<Record> recordsIterator = recordsProvider.startNewSchemaTypeIteration();
			while (recordsIterator.hasNext()) {
				REINDEXING_INFOS = new SystemReindexingInfos(type.getCollection(), type.getCode(), current, counter);

				Record record = recordsIterator.next();
				if (params.getReindexationMode().isFullRecalculation()) {
					record.set(Schemas.MARKED_FOR_REINDEXING, null);
				}
				removeMetadataValuesOn(metadatasMarkedForDeletion, record);

				System.out.println("Reindexing " + record.getId());

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
								aggregatedValuesTempStorage.addOrReplace(referencedRecordId, record.getId(),
										link.getToMetadata(), record.getValues(link.getToMetadata()));
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
										link.getToMetadata(), record.getValues(link.getToMetadata()));
							}

						}
					}

					logger.updateProgression(current, counter);
					bulkTransactionHandler.append(record);
					recordsProvider.markRecordAsHandled(record);

				}

			}

			bulkTransactionHandler.barrier();
			modelLayerFactory.newRecordServices().flush();
			recordsProvider.markIterationAsFinished();
			logger.onEndOfIteration(recordsProvider.getSkippedRecordsCount(), recordsProvider.getCurrentIteration());
			if (!recordsProvider.isRequiringAnotherIteration()) {
				break REINDEXING_TYPE;
			}

		}

	}

	private boolean updateAggregatedMetadata(Record record, Metadata aggregatingMetadata,
			ReindexingAggregatedValuesTempStorage aggregatedValuesTempStorage) {

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
			values.addAll(aggregatedValuesTempStorage.getAllValues(record.getId(), metadata));
		}

		LOGGER.info("Updating aggregating metadata " + aggregatingMetadata.getLocalCode() + " of record "
				+ record.getId() + " based on values " + values);
		InMemoryAggregatedValuesParams params = new InMemoryAggregatedValuesParams(aggregatingMetadata, values);
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

}