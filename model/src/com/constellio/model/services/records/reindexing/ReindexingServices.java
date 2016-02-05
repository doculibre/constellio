package com.constellio.model.services.records.reindexing;

import static com.constellio.model.entities.schemas.Schemas.SCHEMA;
import static com.constellio.model.services.records.BulkRecordTransactionImpactHandling.NO_IMPACT_HANDLING;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.RecordDaoException.NoSuchRecordWithId;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogManager;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.BulkRecordTransactionHandler;
import com.constellio.model.services.records.BulkRecordTransactionHandlerOptions;
import com.constellio.model.services.records.utils.RecordDTOIterator;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class ReindexingServices {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReindexingServices.class);

	private static final String REINDEX_TYPES = "reindexTypes";

	private ModelLayerFactory modelLayerFactory;

	private SecondTransactionLogManager logManager;

	public ReindexingServices(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.logManager = modelLayerFactory.getDataLayerFactory().getSecondTransactionLogManager();
	}

	public void reindexCollections(ReindexationMode reindexationMode) {
		reindexCollections(new ReindexationParams(reindexationMode));
	}

	public void reindexCollections(ReindexationParams params) {
		if (logManager != null && params.getReindexationMode().isFullRewrite()) {
			logManager.regroupAndMoveInVault();
			logManager.moveTLOGToBackup();
			RecordDao recordDao = modelLayerFactory.getDataLayerFactory().newRecordDao();
			try {

				RecordDTO recordDTO = recordDao.get("the_private_key");
				try {
					recordDao.execute(
							new TransactionDTO(RecordsFlushing.LATER()).withNewRecords(asList(recordDTO)).withFullRewrite(true));
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
		transactionOptions.setValidationsEnabled(false);
		if (params.getReindexationMode().isFullRecalculation()) {
			transactionOptions.forceReindexationOfMetadatas(TransactionRecordsReindexation.ALL());
		}
		transactionOptions.setFullRewrite(params.getReindexationMode().isFullRewrite());
		reindexCollection(collection, params, transactionOptions);

	}

	private long getRecordCountOfType(String collection, List<String> reindexedSchemaTypes) {
		List<LogicalSearchValueCondition> conditions = new ArrayList<>();

		for (String reindexedSchemaType : reindexedSchemaTypes) {
			conditions.add(LogicalSearchQueryOperators.startingWithText(reindexedSchemaType));
		}

		LogicalSearchCondition condition = fromAllSchemasIn(collection).where(SCHEMA).isAny(conditions);
		return modelLayerFactory.newSearchServices().getResultsCount(new LogicalSearchQuery(condition));
	}

	private void reindexCollection(String collection, ReindexationParams params, RecordUpdateOptions transactionOptions) {

		if (params.getReindexationMode().isFullRewrite()) {
			recreateIndexes(collection);
		}

		BulkRecordTransactionHandlerOptions options = new BulkRecordTransactionHandlerOptions()
				.withBulkRecordTransactionImpactHandling(NO_IMPACT_HANDLING)
				.setTransactionOptions(transactionOptions)
				.withRecordsPerBatch(params.getBatchSize());

		BulkRecordTransactionHandler bulkTransactionHandler = new BulkRecordTransactionHandler(
				modelLayerFactory.newRecordServices(), REINDEX_TYPES, options);

		try {
			MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);

			for (String typeCode : types.getSchemaTypesSortedByDependency()) {
				LOGGER.info("Indexing '" + typeCode + "'");
				reindexCollectionType(bulkTransactionHandler, types, typeCode);
			}
		} finally {
			bulkTransactionHandler.closeAndJoin();
		}
		modelLayerFactory.getDataLayerFactory().newRecordDao().removeOldLocks();
	}

	private void recreateIndexes(String collection) {
		RecordDao recordDao = modelLayerFactory.getDataLayerFactory().newRecordDao();

		LogicalSearchQuery query = new LogicalSearchQuery()
				.setCondition(fromAllSchemasIn(collection).returnAll())
				.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(Schemas.PARENT_PATH));

		Iterator<Record> idsIterator = modelLayerFactory.newSearchServices().recordsIterator(query, 50000);
		recordDao.recreateZeroCounterIndexesIn(collection, new RecordDTOIterator(idsIterator));
	}

	private void reindexCollectionType(BulkRecordTransactionHandler bulkTransactionHandler, MetadataSchemaTypes types,
			String typeCode) {

		SearchServices searchServices = modelLayerFactory.newSearchServices();
		MetadataSchemaType type = types.getSchemaType(typeCode);
		List<Metadata> metadatas = type.getAllMetadatas().onlyParentReferences().onlyReferencesToType(typeCode);
		Set<String> ids = new HashSet<>();

		long counter = searchServices.getResultsCount(new LogicalSearchQuery(from(type).returnAll()));
		long current = 0;
		while (true) {
			Set<String> idsInCurrentBatch = new HashSet<>();
			Iterator<Record> records = searchServices.recordsIterator(new LogicalSearchQuery(from(type).returnAll()), 1000);
			while (records.hasNext()) {
				Record record = records.next();
				if (metadatas.isEmpty() || (!ids.contains(record.getId()) && !idsInCurrentBatch.contains(record.getId()))) {
					if (metadatas.isEmpty()) {
						current++;
						LOGGER.info("Indexing '" + typeCode + "' : " + current + "/" + counter);
						bulkTransactionHandler.append(record);
					} else {
						String parentId = getParentIdOfSameType(metadatas, record);

						if (parentId == null || ids.contains(parentId)) {
							bulkTransactionHandler.append(record);
							idsInCurrentBatch.add(record.getId());
						}
					}

				}
			}
			bulkTransactionHandler.barrier();
			modelLayerFactory.newRecordServices().flush();
			ids.addAll(idsInCurrentBatch);
			if (metadatas.isEmpty() || ids.size() == counter) {
				break;
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