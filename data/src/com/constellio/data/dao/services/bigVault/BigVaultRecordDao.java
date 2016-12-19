package com.constellio.data.dao.services.bigVault;

import static com.constellio.data.dao.services.bigVault.solr.SolrUtils.NULL_ITEM_LOCALDATE;
import static com.constellio.data.dao.services.bigVault.solr.SolrUtils.NULL_ITEM_LOCAL_DATE_TIME;
import static com.constellio.data.dao.services.bigVault.solr.SolrUtils.convertLocalDateTimeToSolrDate;
import static com.constellio.data.dao.services.bigVault.solr.SolrUtils.convertLocalDateToSolrDate;
import static com.constellio.data.dao.services.bigVault.solr.SolrUtils.convertNullToSolrValue;
import static com.constellio.data.dao.services.bigVault.solr.SolrUtils.isMultiValueStringOrText;
import static com.constellio.data.dao.services.bigVault.solr.SolrUtils.isMultivalue;
import static com.constellio.data.dao.services.bigVault.solr.SolrUtils.isSingleValueStringOrText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.FieldStatsInfo;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Collation;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Correction;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.MoreLikeThisParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.data.dao.dto.records.QueryResponseDTO;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.services.DataLayerLogger;
import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.dao.services.bigVault.RecordDaoException.NoSuchRecordWithId;
import com.constellio.data.dao.services.bigVault.RecordDaoRuntimeException.RecordDaoRuntimeException_RecordsFlushingFailed;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException;
import com.constellio.data.dao.services.bigVault.solr.BigVaultRuntimeException;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.bigVault.solr.SolrUtils;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.solr.ConstellioSolrInputDocument;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogManager;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.data.utils.KeyListMap;
import com.constellio.data.utils.LangUtils;
import com.google.common.base.Joiner;

public class BigVaultRecordDao implements RecordDao {
	private static final Logger LOGGER = LoggerFactory.getLogger(BigVaultRecordDao.class);

	public static final Integer NULL_NUMBER = Integer.MIN_VALUE;
	public static final String COLLECTION_FIELD = "collection_s";
	public static final String REF_COUNT_PREFIX = "idx_rfc_";
	public static final String ACTIVE_IDX_PREFIX = "idx_act_";
	public static final String REFCOUNT_FIELD = "refs_d";
	public static final String TYPE_FIELD = "type_s";
	public static final String ANCESTORS_FIELD = "ancestors_ss";
	public static final String PRINCIPALPATH_FIELD = "principalpath_s";
	public static final String DELETED_FIELD = "deleted_s";
	private static final String ID_FIELD = "id";
	private static final String VERSION_FIELD = "_version_";
	private final BigVaultServer bigVaultServer;
	private final DataStoreTypesFactory dataStoreTypesFactory;
	private final DataLayerLogger dataLayerLogger;
	private SecondTransactionLogManager secondTransactionLogManager;

	private static long january1_1900 = new LocalDate(1900, 1, 1).toDate().getTime();

	public static final LocalDate NULL_LOCALDATE = new LocalDate(4242, 6, 6);

	public static final Date NULL_DATE = new LocalDateTime(4242, 6, 6, 0, 0, 0, 0).toDate();

	public BigVaultRecordDao(BigVaultServer bigVaultServer, DataStoreTypesFactory dataStoreTypesFactory,
			SecondTransactionLogManager secondTransactionLogManager, DataLayerLogger dataLayerLogger) {
		this.dataLayerLogger = dataLayerLogger;
		this.bigVaultServer = bigVaultServer;
		this.dataStoreTypesFactory = dataStoreTypesFactory;
		this.secondTransactionLogManager = secondTransactionLogManager;
	}

	public BigVaultServerTransaction prepare(TransactionDTO transaction) {

		List<SolrInputDocument> newDocuments = new ArrayList<>();
		List<SolrInputDocument> updatedDocuments = new ArrayList<>();
		List<String> deletedRecordsIds = new ArrayList<>();
		List<String> deletedRecordsQueries = SolrUtils.toDeleteQueries(transaction.getDeletedByQueries());
		prepareDocumentsForSolrTransaction(transaction, newDocuments, updatedDocuments, deletedRecordsIds);

		if (!newDocuments.isEmpty() || !updatedDocuments.isEmpty() || !deletedRecordsIds.isEmpty() || !deletedRecordsQueries
				.isEmpty()) {
			return new BigVaultServerTransaction(transaction.getRecordsFlushing(), newDocuments, updatedDocuments,
					deletedRecordsIds, deletedRecordsQueries);
		} else {
			return null;
		}

	}

	@Override
	public TransactionResponseDTO execute(TransactionDTO transaction)
			throws RecordDaoException.OptimisticLocking {

		BigVaultServerTransaction bigVaultServerTransaction = prepare(transaction);

		if (bigVaultServerTransaction != null) {
			try {

				if (secondTransactionLogManager != null) {
					secondTransactionLogManager.prepare(transaction.getTransactionId(), bigVaultServerTransaction);
				}

				TransactionResponseDTO response = bigVaultServer.addAll(bigVaultServerTransaction);
				dataLayerLogger.logTransaction(transaction);
				if (secondTransactionLogManager != null) {
					secondTransactionLogManager.flush(transaction.getTransactionId());
				}
				return response;
			} catch (BigVaultException.OptimisticLocking e) {
				if (secondTransactionLogManager != null) {
					secondTransactionLogManager.cancel(transaction.getTransactionId());
				}
				if (e.getId().startsWith(ACTIVE_IDX_PREFIX)) {
					throw new RecordDaoRuntimeException.ReferenceToNonExistentIndex(e.getId());
				} else {
					throw new RecordDaoException.OptimisticLocking(e.getId(), e.getVersion(), e);
				}
			} catch (BigVaultException e) {
				if (secondTransactionLogManager != null) {
					secondTransactionLogManager.cancel(transaction.getTransactionId());
				}
				throw new RecordDaoRuntimeException(e);
			}
		}
		return new TransactionResponseDTO(0, new HashMap<String, Long>());
	}

	private void prepareDocumentsForSolrTransaction(TransactionDTO transaction, List<SolrInputDocument> newDocuments,
			List<SolrInputDocument> updatedDocuments, List<String> deletedRecordsIds) {
		Map<String, Double> recordsInTransactionRefCounts = new HashMap<>();
		Map<String, Double> recordsOutOfTransactionRefCounts = new HashMap<>();
		KeyListMap<String, String> recordsAncestors = new KeyListMap<>();
		Map<Object, SolrInputDocument> activeReferencesCheck = new HashMap<>();

		for (RecordDTO newRecord : transaction.getNewRecords()) {
			prepareNewRecord(transaction, newDocuments, recordsInTransactionRefCounts,
					recordsOutOfTransactionRefCounts, recordsAncestors, activeReferencesCheck, newRecord);
		}
		for (RecordDeltaDTO modifiedRecord : transaction.getModifiedRecords()) {
			prepareModifiedRecord(transaction, newDocuments, updatedDocuments, deletedRecordsIds, recordsInTransactionRefCounts,
					recordsOutOfTransactionRefCounts, recordsAncestors, modifiedRecord);
		}
		for (RecordDTO recordDTO : transaction.getDeletedRecords()) {
			prepareDeletedRecord(transaction, deletedRecordsIds, recordsInTransactionRefCounts, recordsOutOfTransactionRefCounts,
					recordDTO);
		}

		for (String id : transaction.getMarkedForReindexing()) {
			updatedDocuments.add(newMarkedForReindexingInputDocument(id));
		}

		if (!transaction.isSkippingReferenceToLogicallyDeletedValidation()) {
			for (SolrInputDocument activeReferenceCheck : activeReferencesCheck.values()) {
				updatedDocuments.add(activeReferenceCheck);
			}
		}

		refreshRefCountIndexForRecordsWithNewAncestors(recordsAncestors, recordsInTransactionRefCounts,
				recordsOutOfTransactionRefCounts, transaction);
		newDocuments.addAll(incrementReferenceCountersInTransactionInSolr(recordsInTransactionRefCounts, getCollection(
				transaction), recordsAncestors, transaction.getNewRecords()));
		updatedDocuments
				.addAll(incrementReferenceCountersOutOfTransactionInSolr(recordsOutOfTransactionRefCounts, recordsAncestors));
	}

	private SolrInputDocument newMarkedForReindexingInputDocument(String id) {
		SolrInputDocument solrInputDocument = new SolrInputDocument();
		solrInputDocument.addField("id", id);
		solrInputDocument.addField("_version_", "1");
		Map<String, String> setToTrue = new HashMap<>();
		setToTrue.put("set", "__TRUE__");
		solrInputDocument.addField("markedForReindexing_s", setToTrue);
		return solrInputDocument;
	}

	private Object getCollection(TransactionDTO transaction) {
		if (!transaction.getNewRecords().isEmpty()) {
			return getCollection(transaction.getNewRecords().get(0));
		} else if (!transaction.getModifiedRecords().isEmpty()) {
			return getCollection(transaction.getModifiedRecords().get(0));
		} else if (!transaction.getDeletedRecords().isEmpty()) {
			return getCollection(transaction.getDeletedRecords().get(0));
		} else {
			return "";
		}
	}

	private void prepareModifiedRecord(TransactionDTO transaction, List<SolrInputDocument> newDocuments,
			List<SolrInputDocument> updatedDocuments, List<String> deletedRecordsIds,
			Map<String, Double> recordsInTransactionRefCounts, Map<String, Double> recordsOutOfTransactionRefCounts,
			KeyListMap<String, String> recordsAncestors, RecordDeltaDTO modifiedRecord) {
		if (!modifiedRecord.getModifiedFields().isEmpty()) {
			SolrInputDocument solrInputDocument = buildDeltaSolrDocument(modifiedRecord);
			if (transaction.isFullRewrite()) {
				solrInputDocument.removeField("_version_");
			}
			updatedDocuments.add(solrInputDocument);
			deleteIndexForLogicallyDeletedRecord(deletedRecordsIds, modifiedRecord);
			addActiveIndexesForRestoredRecord(newDocuments, modifiedRecord);
			if (modifiedRecord.getModifiedFields().containsKey("parentpath_ss")) {
				recordsAncestors.set(modifiedRecord.getId(), getModifiedRecordAncestors(modifiedRecord));
			}
			if (!transaction.isSkippingReferenceToLogicallyDeletedValidation()) {
				updatedDocuments.addAll(verifyIndexForNewReferences(modifiedRecord, transaction, recordsInTransactionRefCounts));
			}
			incrementReferenceCounterForNewReferences(modifiedRecord, transaction, recordsInTransactionRefCounts,
					recordsOutOfTransactionRefCounts);
		}
	}

	private void prepareNewRecord(TransactionDTO transaction, List<SolrInputDocument> newDocuments,
			Map<String, Double> recordsInTransactionRefCounts,
			Map<String, Double> recordsOutOfTransactionRefCounts, KeyListMap<String, String> recordsAncestors,
			Map<Object, SolrInputDocument> activeReferencesCheck, RecordDTO newRecord) {
		Object collection = getCollection(newRecord);

		SolrInputDocument solrInputDocument = buildSolrDocument(newRecord);
		if (transaction.isFullRewrite()) {
			solrInputDocument.removeField("_version_");
		}
		newDocuments.add(solrInputDocument);
		if (isNotLogicallyDeleted(newRecord) && supportIndexes(newRecord)) {
			newDocuments.add(buildActiveIndexSolrDocument(newRecord.getId(), collection));
		}

		if (hasNoVersion(newRecord) && supportIndexes(newRecord)) {
			if (!recordsInTransactionRefCounts.containsKey(newRecord.getId())) {
				recordsInTransactionRefCounts.put(newRecord.getId(), 0.0);
			}
			recordsAncestors.set(newRecord.getId(), getRecordAncestors(newRecord));
			verifyIndexForAllReferences(newRecord, transaction, activeReferencesCheck);
		}

		incrementReferenceCounterForAllReferences(newRecord, transaction, recordsInTransactionRefCounts,
				recordsOutOfTransactionRefCounts);
	}

	private boolean supportIndexes(RecordDTO record) {
		String schema = (String) record.getFields().get("schema_s");
		return schema != null;
	}

	private boolean hasNoVersion(RecordDTO record) {
		return record.getVersion() == -1;
	}

	private boolean isNotLogicallyDeleted(RecordDTO record) {
		return !Boolean.TRUE.equals(record.getFields().get("deleted_s"));
	}

	private void prepareDeletedRecord(TransactionDTO transaction, List<String> deletedRecordsIds,
			Map<String, Double> recordsInTransactionRefCounts, Map<String, Double> recordsOutOfTransactionRefCounts,
			RecordDTO recordDTO) {
		deletedRecordsIds.add(recordDTO.getId());
		decrementReferenceCounterForAllReferences(recordDTO, transaction, recordsInTransactionRefCounts,
				recordsOutOfTransactionRefCounts);
		deletedRecordsIds.add(REF_COUNT_PREFIX + recordDTO.getId());
	}

	private void refreshRefCountIndexForRecordsWithNewAncestors(KeyListMap<String, String> recordsAncestors,
			Map<String, Double> recordsInTransactionRefCounts, Map<String, Double> recordsOutOfTransactionRefCounts,
			TransactionDTO transaction) {
		for (Object recordId : recordsAncestors.getNestedMap().keySet()) {
			if (referencedIdIsNewRecordInTransaction(recordId, transaction)) {
				addReferenceToMapWithValue((String) recordId, recordsInTransactionRefCounts, 0.0);
			} else {
				addReferenceToMapWithValue((String) recordId, recordsOutOfTransactionRefCounts, 0.0);
			}
		}
	}

	private void addActiveIndexesForRestoredRecord(List<SolrInputDocument> newDocuments, RecordDeltaDTO modifiedRecord) {
		if (recordIsRestored(modifiedRecord)) {
			newDocuments
					.add(buildActiveIndexSolrDocument(modifiedRecord.getId(),
							modifiedRecord.getInitialFields().get(COLLECTION_FIELD)));
		}
	}

	private boolean recordIsRestored(RecordDeltaDTO modifiedRecord) {
		Map<String, Object> modifiedFields = modifiedRecord.getModifiedFields();
		return modifiedRecord.getInitialFields().get(DELETED_FIELD) == Boolean.TRUE
				&& modifiedFields.containsKey(DELETED_FIELD)
				&& (modifiedFields.get(DELETED_FIELD) == null || modifiedFields.get(DELETED_FIELD) == Boolean.FALSE);
	}

	public List<String> getReferencedRecordsInHierarchy(String recordId) {
		List<String> references = new ArrayList<>();
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", ID_FIELD + ":" + REF_COUNT_PREFIX + recordId + " OR " + ANCESTORS_FIELD + ":" + recordId);
		params.set("fq", "-" + REFCOUNT_FIELD + ":0");
		List<RecordDTO> indexes = query(params).getResults();
		for (RecordDTO index : indexes) {
			references.add(index.getId().substring(REF_COUNT_PREFIX.length()));
		}
		return references;
	}

	@Override
	public void flush() {
		try {
			bigVaultServer.softCommit();
		} catch (IOException | SolrServerException e) {
			throw new RecordDaoRuntimeException_RecordsFlushingFailed(e);
		}
	}

	@Override
	public void removeOldLocks() {
		bigVaultServer.removeLockWithAgeGreaterThan(10);
	}

	@Override
	public void recreateZeroCounterIndexesIn(String collection, Iterator<RecordDTO> idsIterator) {

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("fq", "id:idx_rfc_*");
		params.set("q", "type_s:index");
		params.set("fq", "collection_s:" + collection);

		BigVaultServerTransaction transaction = new BigVaultServerTransaction(RecordsFlushing.NOW())
				.addDeletedQuery(SolrUtils.toDeleteQueries(params));
		String transactionId = transaction.getTransactionId();
		try {

			if (secondTransactionLogManager != null) {
				secondTransactionLogManager.prepare(transactionId, transaction);
			}

			bigVaultServer.addAll(transaction);

			if (secondTransactionLogManager != null) {
				secondTransactionLogManager.flush(transactionId);
			}

		} catch (BigVaultException e) {
			if (secondTransactionLogManager != null) {
				secondTransactionLogManager.cancel(transactionId);
			}
			throw new RuntimeException(e);
		}

		Iterator<List<RecordDTO>> batchsOfIdsIterator = new BatchBuilderIterator<>(idsIterator, 10000);

		while (batchsOfIdsIterator.hasNext()) {
			List<RecordDTO> batchOfIds = batchsOfIdsIterator.next();

			List<SolrInputDocument> inputDocuments = new ArrayList<>();
			for (RecordDTO recordDTO : batchOfIds) {
				List<String> ancestors = getRecordAncestors(recordDTO);
				inputDocuments.add(buildReferenceCounterSolrDocument(recordDTO.getId(), collection, 0.0, ancestors));
			}

			BigVaultServerTransaction batchTransaction = new BigVaultServerTransaction(RecordsFlushing.NOW())
					.setNewDocuments(inputDocuments);
			String batchTransactionId = batchTransaction.getTransactionId();

			try {
				if (secondTransactionLogManager != null) {
					secondTransactionLogManager.prepare(batchTransactionId, batchTransaction);
				}

				bigVaultServer.addAll(batchTransaction);

				if (secondTransactionLogManager != null) {
					secondTransactionLogManager.flush(batchTransactionId);
				}

			} catch (BigVaultException e) {

				if (secondTransactionLogManager != null) {
					secondTransactionLogManager.cancel(batchTransactionId);
				}

				throw new RuntimeException(e);
			}
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<String> getRecordAncestors(RecordDTO newRecord) {
		List<String> recordsAncestors = new ArrayList<>();
		List<String> parentPaths = (List) newRecord.getFields().get("parentpath_ss");
		if (parentPaths != null) {
			for (String parentPath : parentPaths) {
				if (parentPath != null) {
					addParentPathToRecordsAncestors(newRecord.getId(), recordsAncestors, parentPath);
				}
			}
		}
		return recordsAncestors;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<String> getModifiedRecordAncestors(RecordDeltaDTO modifiedRecord) {

		List<String> recordsAncestors = new ArrayList<>();
		List<String> parentPaths = (List) modifiedRecord.getModifiedFields().get("parentpath_ss");
		if (parentPaths != null) {
			for (String parentPath : parentPaths) {
				addParentPathToRecordsAncestors(modifiedRecord.getId(), recordsAncestors, parentPath);
			}
		}
		return recordsAncestors;

	}

	private void addParentPathToRecordsAncestors(String recordId, List<String> recordsAncestors, String parentPath) {
		for (String parentId : parentPath.split("/")) {
			if (!recordsAncestors.contains(parentId) && !parentId.equals(recordId)) {
				recordsAncestors.add(parentId);
			}
		}
	}

	private List<SolrInputDocument> incrementReferenceCountersInTransactionInSolr(
			Map<String, Double> recordsInTransactionRefCounts, Object collection, KeyListMap<String, String> recordsAncestors,
			List<RecordDTO> fullyAddUpdatedRecord) {

		Set<String> newRecordIds = getIdsOfNewRecords(fullyAddUpdatedRecord);

		List<SolrInputDocument> refCountSolrDocuments = new ArrayList<>();

		Set<String> recordIds = new HashSet<>();
		recordIds.addAll(recordsAncestors.getNestedMap().keySet());
		recordIds.addAll(recordsInTransactionRefCounts.keySet());

		for (String id : recordIds) {
			Double value = recordsInTransactionRefCounts.get(id);
			if (value == null) {
				value = 0.0;
			}

			if (newRecordIds.contains(id)) {
				List<String> ancestors = recordsAncestors.get(id);
				refCountSolrDocuments.add(buildReferenceCounterSolrDocument(id, collection, value, ancestors));
			} else {
				List<String> ancestors = recordsAncestors.contains(id) ? recordsAncestors.get(id) : null;
				refCountSolrDocuments.add(updateReferenceCounterSolrDocument(id, value, ancestors));
			}
		}
		return refCountSolrDocuments;
	}

	private Set<String> getIdsOfNewRecords(List<RecordDTO> fullyAddUpdatedRecord) {
		Set<String> ids = new HashSet<>();

		for (RecordDTO recordDTO : fullyAddUpdatedRecord) {
			if (recordDTO.getVersion() == -1) {
				ids.add(recordDTO.getId());
			}
		}

		return ids;
	}

	private List<SolrInputDocument> incrementReferenceCountersOutOfTransactionInSolr(
			Map<String, Double> recordsOutOfTransactionRefCounts, KeyListMap<String, String> recordsAncestors) {
		List<SolrInputDocument> refCountSolrDocuments = new ArrayList<>();
		for (Map.Entry<String, Double> field : recordsOutOfTransactionRefCounts.entrySet()) {
			String id = field.getKey().toString();
			if (recordsAncestors.contains(id)) {
				List<String> ancestors = recordsAncestors.get(id);
				refCountSolrDocuments.add(updateReferenceCounterSolrDocument(id, field.getValue(), ancestors));
			} else {
				refCountSolrDocuments.add(updateReferenceCounterSolrDocument(id, field.getValue(), null));
			}

		}
		return refCountSolrDocuments;
	}

	private void deleteIndexForLogicallyDeletedRecord(List<String> deletedRecordsIds, RecordDeltaDTO modifiedRecord) {
		if (modifiedRecord.getModifiedFields().get(DELETED_FIELD) == Boolean.TRUE) {
			deletedRecordsIds.add(ACTIVE_IDX_PREFIX + modifiedRecord.getId());
		}
	}

	private void verifyIndexForAllReferences(RecordDTO newRecord, TransactionDTO transaction,
			Map<Object, SolrInputDocument> activeReferencesCheck) {
		Object collection = getCollection(newRecord);

		for (Map.Entry<String, Object> field : newRecord.getFields().entrySet()) {
			Object refId = field.getValue();

			if (field.getKey().endsWith("Id_s") && field.getValue() != null && !activeReferencesCheck.containsKey(refId)
					&& !referencedIdIsNewRecordInTransaction(refId, transaction)) {
				activeReferencesCheck.put(refId, setVersion1ToDocument((String) field.getValue(), collection));
			} else if (field.getKey().endsWith("Id_ss") && field.getValue() != null) {
				verifyIndexForReferencesInMultivalueField(transaction, collection, field, activeReferencesCheck);
			}
		}
	}

	private void verifyIndexForReferencesInMultivalueField(TransactionDTO transaction,
			Object collection, Entry<String, Object> field, Map<Object, SolrInputDocument> activeReferencesCheck) {
		for (Object referenceId : (List) field.getValue()) {
			if (referenceId != null
					&& !activeReferencesCheck.containsKey(referenceId)
					&& !referencedIdIsNewRecordInTransaction(referenceId, transaction)) {
				activeReferencesCheck.put(referenceId, setVersion1ToDocument((String) referenceId, collection));
			}
		}
	}

	private void incrementReferenceCounterForAllReferences(RecordDTO newRecord, TransactionDTO transaction,
			Map<String, Double> recordsInTransactionRefCounts, Map<String, Double> recordsOutOfTransactionRefCounts) {
		for (Map.Entry<String, Object> field : newRecord.getFields().entrySet()) {
			if (fieldIsNonParentReference(field)) {
				if (!referencedIdIsNewRecordInTransaction(field.getValue(), transaction)) {
					addReferenceToRecordMapToIncrement((String) field.getValue(), recordsOutOfTransactionRefCounts, newRecord);
				} else {
					addReferenceToRecordMapToIncrement((String) field.getValue(), recordsInTransactionRefCounts, newRecord);
				}
			} else if (field.getKey().endsWith("Id_ss") && field.getValue() != null) {
				incrementReferenceCounterForReferencesInMultivalueField(newRecord, transaction, field,
						recordsInTransactionRefCounts, recordsOutOfTransactionRefCounts);
			}
		}
	}

	private void decrementReferenceCounterForAllReferences(RecordDTO newRecord, TransactionDTO transaction,
			Map<String, Double> recordsInTransactionRefCounts, Map<String, Double> recordsOutOfTransactionRefCounts) {
		for (Map.Entry<String, Object> field : newRecord.getFields().entrySet()) {
			if (fieldIsNonParentReference(field)) {
				if (!referencedIdIsNewRecordInTransaction(field.getValue(), transaction)) {
					addReferenceToRecordMapToDecrement((String) field.getValue(), recordsOutOfTransactionRefCounts, newRecord);
				} else {
					addReferenceToRecordMapToDecrement((String) field.getValue(), recordsInTransactionRefCounts, newRecord);
				}
			} else if (field.getKey().endsWith("Id_ss") && field.getValue() != null) {
				decrementReferenceCounterForReferencesInMultivalueField(newRecord, transaction, field,
						recordsInTransactionRefCounts, recordsOutOfTransactionRefCounts);
			}
		}
	}

	private void addReferenceToRecordMapToIncrement(String referenceId, Map<String, Double> recordsOutOfTransactionRefCounts,
			RecordDTO newRecord) {
		if (referenceIsNotParentOrPrincipalConcept(referenceId, newRecord)) {
			addReferenceToMapWithValue(referenceId, recordsOutOfTransactionRefCounts, 1.0);
		}
	}

	private void addReferenceToRecordMapToIncrement(String referenceId, Map<String, Double> recordsOutOfTransactionRefCounts,
			RecordDeltaDTO modifiedRecord) {

		String principalPath = modifiedRecord.get(PRINCIPALPATH_FIELD);
		if (referenceIsNotParentOrPrincipalConcept(referenceId, principalPath)) {
			addReferenceToMapWithValue(referenceId, recordsOutOfTransactionRefCounts, 1.0);
		}
	}

	private void addReferenceToMapWithValue(String referenceId, Map<String, Double> recordsRefCounts,
			double value) {
		if (recordsRefCounts.containsKey(referenceId)) {
			double incrementedRefCount = recordsRefCounts.get(referenceId).doubleValue() + value;
			recordsRefCounts.put(referenceId, incrementedRefCount);
		} else if (referenceId != null) {
			recordsRefCounts.put(referenceId, value);
		}
	}

	private boolean referenceIsNotParentOrPrincipalConcept(String referenceId, RecordDTO newRecord) {
		String principalPath = (String) newRecord.getFields().get(PRINCIPALPATH_FIELD);
		return referenceId == null || principalPath == null || !principalPath.contains(referenceId);
	}

	private boolean referenceIsNotParentOrPrincipalConcept(String referenceId, String principalPath) {
		return referenceId == null || principalPath == null || !principalPath.contains(referenceId);
	}

	private void addReferenceToRecordMapToDecrement(String referenceId, Map<String, Double> recordsOutOfTransactionRefCounts,
			RecordDTO newRecord) {
		if (referenceIsNotParentOrPrincipalConcept(referenceId, newRecord)) {
			addReferenceToMapWithValue(referenceId, recordsOutOfTransactionRefCounts, -1.0);
		}
	}

	private void addReferenceToRecordMapToDecrement(String referenceId, Map<String, Double> recordsOutOfTransactionRefCounts,
			RecordDeltaDTO newRecord) {
		String principalPath = (String) newRecord.getInitialFields().get(PRINCIPALPATH_FIELD);
		if (referenceId != null && referenceIsNotParentOrPrincipalConcept(referenceId, principalPath)) {
			addReferenceToMapWithValue(referenceId, recordsOutOfTransactionRefCounts, -1.0);
		}
	}

	private Object getCollection(RecordDTO newRecord) {
		return newRecord.getFields().get(COLLECTION_FIELD);
	}

	private Object getCollection(RecordDeltaDTO newRecord) {
		return newRecord.getInitialFields().get(COLLECTION_FIELD);
	}

	private void incrementReferenceCounterForReferencesInMultivalueField(RecordDTO newRecord, TransactionDTO transaction,
			Entry<String, Object> field, Map<String, Double> recordsInTransactionRefCounts,
			Map<String, Double> recordsOutOfTransactionRefCounts) {
		for (Object referenceId : LangUtils.withoutDuplicates((List) field.getValue())) {
			if (!referencedIdIsNewRecordInTransaction(referenceId, transaction)) {
				addReferenceToRecordMapToIncrement((String) referenceId, recordsOutOfTransactionRefCounts, newRecord);
			} else {
				addReferenceToRecordMapToIncrement((String) referenceId, recordsInTransactionRefCounts, newRecord);
			}
		}
	}

	private void decrementReferenceCounterForReferencesInMultivalueField(RecordDTO newRecord, TransactionDTO transaction,
			Entry<String, Object> field, Map<String, Double> recordsInTransactionRefCounts,
			Map<String, Double> recordsOutOfTransactionRefCounts) {
		for (Object referenceId : (List) field.getValue()) {
			if (!referencedIdIsNewRecordInTransaction(referenceId, transaction)) {
				addReferenceToRecordMapToDecrement((String) referenceId, recordsOutOfTransactionRefCounts, newRecord);
			} else {
				addReferenceToRecordMapToDecrement((String) referenceId, recordsInTransactionRefCounts, newRecord);
			}
		}
	}

	private boolean fieldIsNonParentReference(Entry<String, Object> field) {
		return field.getKey().endsWith("Id_s") && !field.getKey().endsWith("PId_s");
	}

	private boolean referencedIdIsNewRecordInTransaction(Object value, TransactionDTO transaction) {
		return value != null && transaction.hasRecord((String) value);
	}

	private List<SolrInputDocument> verifyIndexForNewReferences(RecordDeltaDTO modifiedRecord, TransactionDTO transaction,
			Map<String, Double> recordsInTransactionRefCounts) {
		List<SolrInputDocument> referencedIndexes = new ArrayList<>();
		Object collection = modifiedRecord.getInitialFields().get(COLLECTION_FIELD);
		for (Map.Entry<String, Object> field : modifiedRecord.getModifiedFields().entrySet()) {
			if (fieldIsNewReferenceInTransaction(transaction, field)) {
				if (!recordsInTransactionRefCounts.containsKey(field.getValue())) {
					referencedIndexes.add(setVersion1ToDocument((String) field.getValue(), collection));
				}
			} else if (field.getKey().endsWith("Id_ss") && field.getValue() != null) {
				verifyIndexForNewReferencesInMultivalueField(modifiedRecord, referencedIndexes, field,
						recordsInTransactionRefCounts, transaction);
			}
		}
		return referencedIndexes;
	}

	private void incrementReferenceCounterForNewReferences(RecordDeltaDTO modifiedRecord, TransactionDTO transaction,
			Map<String, Double> recordsInTransactionRefCounts, Map<String, Double> recordsOutOfTransactionRefCounts) {
		for (Map.Entry<String, Object> field : modifiedRecord.getModifiedFields().entrySet()) {
			if (fieldIsNonParentReference(field)) {
				if (!referencedIdIsNewRecordInTransaction(field.getValue(), transaction)) {
					addReferenceToRecordMapToIncrement((String) field.getValue(), recordsOutOfTransactionRefCounts,
							modifiedRecord);
					addReferenceToRecordMapToDecrement((String) modifiedRecord.getInitialFields().get(field.getKey()),
							recordsOutOfTransactionRefCounts, modifiedRecord);
				} else {
					addReferenceToRecordMapToIncrement((String) field.getValue(), recordsInTransactionRefCounts, modifiedRecord);
				}
			} else if (field.getKey().endsWith("Id_ss")) {
				incrementReferenceCounterForNewReferencesInMultivalueField(modifiedRecord, field, transaction,
						recordsInTransactionRefCounts, recordsOutOfTransactionRefCounts);

			}
		}
	}

	private void incrementReferenceCounterForNewReferencesInMultivalueField(RecordDeltaDTO modifiedRecord,
			Entry<String, Object> field, TransactionDTO transaction, Map<String, Double> recordsInTransactionRefCounts,
			Map<String, Double> recordsOutOfTransactionRefCounts) {
		List<String> newReferences;
		if (modifiedRecord.getInitialFields().get(field.getKey()) != null) {
			newReferences = LangUtils.compare((List) modifiedRecord.getInitialFields().get(
					field.getKey()), (List) field.getValue()).getNewItems();
		} else {
			newReferences = (List) field.getValue();
		}
		for (String referenceId : newReferences) {
			if (!referencedIdIsNewRecordInTransaction(referenceId, transaction)) {
				addReferenceToRecordMapToIncrement(referenceId, recordsOutOfTransactionRefCounts, modifiedRecord);
			} else {
				addReferenceToRecordMapToIncrement(referenceId, recordsInTransactionRefCounts, modifiedRecord);
			}
		}
		if (modifiedRecord.getInitialFields().get(field.getKey()) != null) {
			List<String> removedReferences = LangUtils.compare((List) modifiedRecord.getInitialFields().get(field.getKey()),
					(List) field.getValue()).getRemovedItems();
			for (String referenceId : removedReferences) {
				if (!referencedIdIsNewRecordInTransaction(referenceId, transaction)) {
					addReferenceToRecordMapToDecrement(referenceId, recordsOutOfTransactionRefCounts, modifiedRecord);
				} else {
					addReferenceToRecordMapToDecrement(referenceId, recordsInTransactionRefCounts, modifiedRecord);
				}
			}
		}
	}

	private boolean fieldIsNewReferenceInTransaction(TransactionDTO transaction, Entry<String, Object> field) {
		return field.getKey().endsWith("Id_s") && field.getValue() != null && !referencedIdIsNewRecordInTransaction(
				field.getValue(), transaction);
	}

	private void verifyIndexForNewReferencesInMultivalueField(RecordDeltaDTO modifiedRecord,
			List<SolrInputDocument> referencedIndexes, Entry<String, Object> field,
			Map<String, Double> recordsInTransactionRefCounts, TransactionDTO transaction) {
		Object collection = modifiedRecord.getInitialFields().get(COLLECTION_FIELD);
		Object initialValue = modifiedRecord.getInitialFields().get(field.getKey());
		if (initialValue != null) {
			List newReferences = LangUtils
					.compare((List) modifiedRecord.getInitialFields().get(field.getKey()), (List) field.getValue())
					.getNewItems();
			for (Object referenceId : newReferences) {
				if (!recordsInTransactionRefCounts.containsKey(referenceId)) {
					if (referenceId != null && !referencedIdIsNewRecordInTransaction(referenceId, transaction)) {
						referencedIndexes.add(setVersion1ToDocument((String) referenceId, collection));
					}
				}
			}
		} else {
			for (Object referenceId : (List) field.getValue()) {
				if (referenceId != null && !referencedIdIsNewRecordInTransaction(referenceId, transaction)) {
					referencedIndexes.add(setVersion1ToDocument((String) referenceId, collection));
				}
			}
		}
	}

	private SolrInputDocument setVersion1ToDocument(String referenceId, Object collection) {
		SolrInputDocument referencedIndex = buildActiveIndexSolrDocument(referenceId, collection);
		referencedIndex.setField(VERSION_FIELD, 1L);
		return referencedIndex;
	}

	@Override
	public RecordDTO get(String id)
			throws RecordDaoException.NoSuchRecordWithId {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("fq", ID_FIELD + ":" + id);
		params.set("q", "*:*");

		RecordDTO entity = querySingleDocument(params);
		if (entity == null) {
			throw new RecordDaoException.NoSuchRecordWithId(id);
		} else {
			return entity;
		}
	}

	@Override
	public QueryResponse nativeQuery(SolrParams params) {
		try {
			QueryResponse response = bigVaultServer.query(params);
			dataLayerLogger.logQueryResponse(params, response);
			return response;
		} catch (BigVaultException.CouldNotExecuteQuery e) {
			throw new BigVaultRuntimeException.CannotListDocuments(e);
		}
	}

	@Override
	public List<RecordDTO> searchQuery(SolrParams params) {
		return query(params).getResults();
	}

	private RecordDTO querySingleDocument(ModifiableSolrParams params) {
		QueryResponse response = null;
		try {
			response = bigVaultServer.query(params);
		} catch (BigVaultException.CouldNotExecuteQuery e) {
			throw new BigVaultRuntimeException.CannotQuerySingleDocument(e);
		}

		SolrDocumentList documents = response.getResults();
		if (documents.isEmpty()) {
			return null;
		} else {
			return toEntity(documents.get(0));
		}
	}

	public QueryResponseDTO query(SolrParams params) {
		QueryResponse response = nativeQuery(params);

		List<RecordDTO> documents = new ArrayList<RecordDTO>();
		SolrDocumentList solrDocuments = response.getResults();
		for (SolrDocument solrDocument : solrDocuments) {
			documents.add(toEntity(solrDocument));
		}

		Map<String, Map<String, List<String>>> highlights = response.getHighlighting();
		Map<String, List<FacetValue>> fieldFacetValues = getFieldFacets(response);
		Map<String, Map<String, Object>> fieldsStatistics = getFieldsStats(response);
		Map<String, Integer> facetQueries = response.getFacetQuery();

		boolean correctlySpelt = true;
		List<String> spellcheckerSuggestions = new ArrayList<String>();

		SpellCheckResponse spellCheckResponse = response.getSpellCheckResponse();
		if (spellCheckResponse != null) {
			correctlySpelt = spellCheckResponse.isCorrectlySpelled();
			spellcheckerSuggestions = spellcheckerSuggestions(spellCheckResponse);
		}

		Map<RecordDTO, Map<RecordDTO, Double>> resultWithMoreLikeThis = new LinkedHashMap<>();
		if (params.get(MoreLikeThisParams.MLT) != null
				&& Boolean.parseBoolean(params.get(MoreLikeThisParams.MLT))) {
			try {
				resultWithMoreLikeThis = extractMoreLikeThis(response, params.get(MoreLikeThisParams.SIMILARITY_FIELDS));
			} catch (SolrServerException | IOException e) {
				throw new BigVaultRuntimeException.CannotListDocuments(e);
			}
		}

		return new QueryResponseDTO(documents, response.getQTime(), response.getResults().getNumFound(), fieldFacetValues,
				fieldsStatistics,
				facetQueries, highlights, correctlySpelt, spellcheckerSuggestions, resultWithMoreLikeThis);
	}

	private Map<String, Map<String, Object>> getFieldsStats(QueryResponse response) {
		Map<String, Map<String, Object>> fieldsStats = new HashMap<>();
		Map<String, FieldStatsInfo> statsInfo = response.getFieldStatsInfo();
		if (statsInfo != null) {
			for (String key : statsInfo.keySet()) {
				FieldStatsInfo fieldStatsInfo = response.getFieldStatsInfo().get(key);
				Map<String, Object> currentFieldStats = new HashMap<>();
				currentFieldStats.put("min", fieldStatsInfo.getMin());
				currentFieldStats.put("max", fieldStatsInfo.getMax());
				currentFieldStats.put("count", fieldStatsInfo.getCount());
				currentFieldStats.put("sum", fieldStatsInfo.getSum());
				currentFieldStats.put("missing", fieldStatsInfo.getMissing());
				fieldsStats.put(key, currentFieldStats);
			}
		}

		return fieldsStats;
	}

	private Map<RecordDTO, Map<RecordDTO, Double>> extractMoreLikeThis(QueryResponse response, String moreLikeThisFields)
			throws SolrServerException, IOException {
		Map<RecordDTO, Map<RecordDTO, Double>> moreLikeThisRes = new LinkedHashMap<>();

		NamedList<?> moreLikeThis = (NamedList<?>) response.getResponse().get("moreLikeThis");
		if (moreLikeThis != null) {
			for (int i = 0; i < moreLikeThis.size(); i++) {
				@SuppressWarnings("unchecked")
				List<SolrDocument> results = (List<SolrDocument>) moreLikeThis.getVal(i);
				SolrDocument aSolrDocument = response.getResults().get(i);
				JaccardDocumentSorter sorter = new JaccardDocumentSorter(bigVaultServer.getNestedSolrServer(), aSolrDocument,
						moreLikeThisFields, "id");
				List<SolrDocument> sortedResults = sorter.sort(results);
				Map<RecordDTO, Double> docMoreLikeThisRes = new LinkedHashMap<>();
				for (SolrDocument aSimilarDoc : sortedResults) {
					Double score = (Double) aSimilarDoc.get(JaccardDocumentSorter.SIMILARITY_SCORE_FIELD);
					aSimilarDoc.remove(JaccardDocumentSorter.SIMILARITY_SCORE_FIELD);
					RecordDTO entity = toEntity(aSimilarDoc);
					docMoreLikeThisRes.put(entity, score);
				}

				moreLikeThisRes.put(toEntity(aSolrDocument), docMoreLikeThisRes);
			}
		}
		return moreLikeThisRes;
	}

	private List<String> spellcheckerSuggestions(SpellCheckResponse spellCheckResponse) {
		List<String> spellcheckerSuggestions = new ArrayList<String>();
		List<Collation> collatedResults = spellCheckResponse.getCollatedResults();
		if (collatedResults != null) {
			for (Collation collation : collatedResults) {
				LinkedHashSet<String> suggestions = new LinkedHashSet<String>();
				for (Correction correction : collation.getMisspellingsAndCorrections()) {
					suggestions.add(correction.getCorrection());
				}
				spellcheckerSuggestions.add(Joiner.on(" ").join(suggestions));
			}
		}
		return spellcheckerSuggestions;
	}

	private Map<String, List<FacetValue>> getFieldFacets(QueryResponse response) {
		Map<String, List<FacetValue>> facetValues = new HashMap<>();

		if (response.getFacetFields() != null) {
			for (FacetField facetField : response.getFacetFields()) {

				List<FacetValue> fieldFacetValues = new ArrayList<>();

				for (Count count : facetField.getValues()) {
					fieldFacetValues.add(new FacetValue(count.getName(), count.getCount()));
				}

				facetValues.put(facetField.getName(), fieldFacetValues);
			}
		}

		return facetValues;
	}

	@Override
	public long documentsCount() {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "*:*");
		params.set("fq", "-type_s:index");
		params.set("rows", "1");
		return query(params).getNumFound();
	}

	protected SolrInputDocument buildSolrDocument(RecordDTO entity) {
		SolrInputDocument document = new ConstellioSolrInputDocument();
		document.addField(ID_FIELD, entity.getId());
		document.addField(VERSION_FIELD, entity.getVersion());

		for (Map.Entry<String, Object> field : entity.getFields().entrySet()) {
			String fieldName = field.getKey();
			if (!fieldName.equals("id")) {
				Object fieldValue = field.getValue();
				fieldValue = convertBigVaultValueToSolrValue(fieldName, fieldValue);
				document.addField(fieldName, fieldValue);
			}
		}

		for (Map.Entry<String, Object> field : entity.getCopyFields().entrySet()) {
			document.addField(field.getKey(), field.getValue());
		}

		return document;
	}

	protected SolrInputDocument buildActiveIndexSolrDocument(String recordId, Object collection) {
		SolrInputDocument document = new ConstellioSolrInputDocument();
		document.addField(ID_FIELD, ACTIVE_IDX_PREFIX + recordId);
		document.addField(TYPE_FIELD, "index");
		document.addField(COLLECTION_FIELD, collection == null ? null : collection);
		return document;
	}

	protected SolrInputDocument buildReferenceCounterSolrDocument(String recordId, Object collection, Double value,
			List<String> ancestors) {
		SolrInputDocument document = new ConstellioSolrInputDocument();
		String indexId = REF_COUNT_PREFIX + recordId;
		document.addField(ID_FIELD, indexId);
		document.addField(TYPE_FIELD, "index");
		document.addField(REFCOUNT_FIELD, value);
		document.addField(COLLECTION_FIELD, collection == null ? null : collection);
		document.addField(ANCESTORS_FIELD, ancestors);
		return document;
	}

	protected SolrInputDocument updateReferenceCounterSolrDocument(String recordId, Double value,
			List<String> ancestors) {
		SolrInputDocument document = new ConstellioSolrInputDocument();
		document.addField(ID_FIELD, REF_COUNT_PREFIX + recordId);
		document.addField(REFCOUNT_FIELD, LangUtils.newMapWithEntry("inc", value));

		if (ancestors != null) {
			if (ancestors.isEmpty()) {
				ancestors.add("");
			}
			document.addField(ANCESTORS_FIELD, ancestors);
		}
		return document;
	}

	private LocalDateTime convertSolrDateToLocalDateTime(Date date) {
		LocalDateTime localDateTime = new LocalDateTime(date).minusMillis(getOffset(date));
		LocalDate localDate = localDateTime.toLocalDate();
		if (localDate.equals(NULL_LOCALDATE)) {
			return null;
		} else if (localDateTime.getYear() < 1900) {
			return localDateTime.withTime(0, 0, 0, 0);
		} else {
			return localDateTime;
		}
	}

	private LocalDate convertSolrDateToLocalDate(Date date) {

		LocalDateTime localDateTime = convertSolrDateToLocalDateTime(date);
		LocalDate localDate = localDateTime == null ? null : localDateTime.toLocalDate();

		return localDate;
	}

	private int getOffset(Date date) {

		if (date.getTime() < january1_1900) {
			return 0;
		} else {
			return DateTimeZone.getDefault().getOffset(date.getTime());
		}
	}

	@SuppressWarnings("unchecked")
	Object convertBigVaultValueToSolrValue(String fieldName, Object fieldValue) {
		Object convertedFieldValue = fieldValue;
		if (fieldValue != null) {
			if (fieldName.endsWith("_dt")) {
				convertedFieldValue = convertLocalDateTimeToSolrDate((LocalDateTime) fieldValue);
			} else if (fieldName.endsWith("_dts") && fieldValue instanceof List) {
				List<LocalDateTime> localDateTimes = (List<LocalDateTime>) fieldValue;
				if (!localDateTimes.isEmpty()) {
					List<String> dates = new ArrayList<>();
					for (LocalDateTime localDateTime : localDateTimes) {
						if (localDateTime == null) {
							localDateTime = SolrUtils.NULL_ITEM_LOCAL_DATE_TIME;
						}
						dates.add(convertLocalDateTimeToSolrDate(localDateTime));
					}
					convertedFieldValue = dates;
				} else {
					convertedFieldValue = convertNullToSolrValue(fieldName);
				}
			} else if (fieldName.endsWith("_da")) {
				convertedFieldValue = convertLocalDateToSolrDate(
						("".equals(fieldValue)) ? SolrUtils.NULL_ITEM_LOCALDATE : (LocalDate) fieldValue);

			} else if (fieldName.endsWith("_das") && fieldValue instanceof List) {
				List<LocalDate> localDates = (List<LocalDate>) fieldValue;
				if (!localDates.isEmpty()) {
					List<String> dates = new ArrayList<>();
					for (LocalDate localDate : localDates) {
						if (localDate == null) {
							localDate = SolrUtils.NULL_ITEM_LOCALDATE;
						}
						dates.add(convertLocalDateToSolrDate(localDate));
					}
					convertedFieldValue = dates;
				} else {
					convertedFieldValue = convertNullToSolrValue(fieldName);
				}
			} else if (isSingleValueStringOrText(fieldName)) {
				convertedFieldValue = convertSingleValueBooleanToSolrValue(fieldValue);
			} else if (isMultiValueStringOrText(fieldName) && fieldValue instanceof List) {
				List fieldValueAsList = (List) fieldValue;
				if (!fieldValueAsList.isEmpty()) {
					if (fieldValueAsList.get(0) instanceof Boolean) {
						convertedFieldValue = convertMultivalueBooleansToSolrValues(fieldValue);
					} else {
						List convertedFieldValueList = new ArrayList<>();
						for (Object fieldValueAsListItem : fieldValueAsList) {
							if (fieldValueAsListItem == null) {
								fieldValueAsListItem = SolrUtils.NULL_STRING;
							}
							convertedFieldValueList.add(fieldValueAsListItem);
						}
						if (convertedFieldValueList.isEmpty()) {
							convertedFieldValue = convertNullToSolrValue(fieldName);
						} else {
							convertedFieldValue = convertedFieldValueList;
						}
					}
				} else {
					convertedFieldValue = convertNullToSolrValue(fieldName);
				}
			}
		} else {
			convertedFieldValue = convertNullToSolrValue(fieldName);
		}
		return convertedFieldValue;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <T> List<String> convertMultivalueBooleansToSolrValues(Object fieldValue) {
		List<Boolean> booleans = (List) fieldValue;
		List<String> strings = new ArrayList<String>();
		for (Boolean aBoolean : booleans) {
			if (Boolean.TRUE.equals(aBoolean)) {
				strings.add("__TRUE__");
			} else if (Boolean.FALSE.equals(aBoolean)) {
				strings.add("__FALSE__");
			}
		}
		return strings;
	}

	private Object convertSingleValueBooleanToSolrValue(Object fieldValue) {
		Object convertedFieldValue = fieldValue;
		if (Boolean.TRUE.equals(fieldValue)) {
			convertedFieldValue = "__TRUE__";
		} else if (Boolean.FALSE.equals(fieldValue)) {
			convertedFieldValue = "__FALSE__";
		}
		return convertedFieldValue;
	}

	protected RecordDTO toEntity(SolrDocument solrDocument) {
		String id = (String) solrDocument.get(ID_FIELD);
		long version = (Long) solrDocument.get(VERSION_FIELD);
		List<String> fields = null;

		Map<String, Object> fieldValues = new HashMap<String, Object>();

		for (String fieldName : solrDocument.getFieldNames()) {
			if (!fieldName.equals("sys_s") && !containsTwoUnderscoresAndIsNotVersionField(fieldName)) {
				Object value = convertSolrValueToBigVaultValue(fieldName, solrDocument.getFieldValue(fieldName));
				if (value != null) {
					fieldValues.put(fieldName, value);
				}
			}
		}
		return new RecordDTO(id, version, fields, fieldValues);
	}

	private boolean containsTwoUnderscoresAndIsNotVersionField(String field) {
		if (field.equals("_version_")) {
			return false;
		}
		int firstUnderScoreIndex = field.indexOf("_");
		if (firstUnderScoreIndex == -1) {
			return false;
		}
		int secondUnderScoreIndex = field.indexOf("_", firstUnderScoreIndex + 1);
		return secondUnderScoreIndex != -1;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	Object convertSolrValueToBigVaultValue(String fieldName, Object fieldValue) {
		Object convertedValue = fieldValue;
		if (fieldName.endsWith("_d")) {
			convertedValue = convertNumber(fieldValue);
		} else if (fieldName.endsWith("_dt")) {
			convertedValue = convertSolrDateToLocalDateTime((Date) fieldValue);
		} else if (fieldName.endsWith("_da")) {
			convertedValue = convertSolrDateToLocalDate((Date) fieldValue);
		} else if (isSingleValueStringOrText(fieldName)) {
			if ("__TRUE__".equals(fieldValue)) {
				convertedValue = true;
			} else if ("__FALSE__".equals(fieldValue)) {
				convertedValue = false;
			} else if ("__NULL__".equals(fieldValue)) {
				convertedValue = null;
			}
			if (fieldName.endsWith("_t") && fieldValue instanceof List) {
				convertedValue = ((List) fieldValue).get(0);
				if ("__NULL__".equals(convertedValue)) {
					convertedValue = null;
				}
			}
		} else if (fieldName.endsWith("_dts") && fieldValue instanceof List) {
			List<LocalDateTime> localDateTimes = new ArrayList<LocalDateTime>();
			boolean hasNonNullValues = false;
			List<Date> dates = ((List<Date>) fieldValue);
			for (Date date : dates) {
				LocalDateTime localDateTime = convertSolrDateToLocalDateTime(date);

				if (localDateTime != null) {
					if (localDateTime.equals(NULL_ITEM_LOCAL_DATE_TIME)) {
						localDateTime = null;
					} else {
						hasNonNullValues = true;
					}
					localDateTimes.add(localDateTime);
				}
			}
			convertedValue = hasNonNullValues ? localDateTimes : null;

		} else if (fieldName.endsWith("_das") && fieldValue instanceof List) {
			List<LocalDate> localDates = new ArrayList<LocalDate>();
			List<Date> dates = ((List<Date>) fieldValue);
			boolean hasNonNullValues = false;
			for (Date date : dates) {
				LocalDate localDate = convertSolrDateToLocalDate(date);
				if (localDate != null) {
					if (localDate.equals(NULL_ITEM_LOCALDATE)) {
						localDate = null;
					} else {
						hasNonNullValues = true;
					}
					localDates.add(localDate);
				}
			}
			convertedValue = hasNonNullValues ? localDates : null;

		} else if (isMultiValueStringOrText(fieldName) && fieldValue instanceof List) {
			convertedValue = convertMultivalueBooleanSolrValuesToBooleans(fieldValue);

		} else {
			convertedValue = fieldValue;
		}

		if (isSolrNullValue(fieldValue)) {
			if (isMultivalue(fieldName)) {
				return new ArrayList<>();
			} else {
				return null;
			}
		}

		return convertedValue;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List convertMultivalueBooleanSolrValuesToBooleans(Object fieldValue) {
		List<String> strings = (List) fieldValue;
		List<Boolean> booleans = new ArrayList<Boolean>();
		boolean hasBooleanValues = false;
		boolean hasNonNullValues = false;
		for (int i = 0; i < strings.size(); i++) {
			String aString = strings.get(i);
			if ("__TRUE__".equals(aString)) {
				booleans.add(Boolean.TRUE);
				hasBooleanValues = true;
				hasNonNullValues = true;
			} else if ("__FALSE__".equals(aString)) {
				booleans.add(Boolean.FALSE);
				hasBooleanValues = true;
				hasNonNullValues = true;
			} else if (SolrUtils.NULL_STRING.equals(aString)) {
				strings.set(i, null);
				booleans.add(null);
			} else {
				hasNonNullValues = true;
			}
		}
		if (hasNonNullValues) {
			return hasBooleanValues ? booleans : strings;
		} else {
			return null;
		}
	}

	private Double convertNumber(Object fieldValue) {
		if (fieldValue == null || fieldValue.equals((double) Integer.MIN_VALUE) || fieldValue.equals(Integer.MIN_VALUE)) {
			return null;
		} else {
			return ((Number) fieldValue).doubleValue();
		}
	}

	private boolean isSolrNullValue(Object fieldValue) {

		if (fieldValue == null) {
			return true;
		} else if (fieldValue instanceof List) {
			return ((List) fieldValue).isEmpty();
		} else {
			return false;
		}

		//
		//		if (NULL_STRING.equals(fieldValue)) {
		//			return true;
		//		} else if (NULL_DATE_TIME.equals(fieldValue) || NULL_DATE.equals(fieldValue)) {
		//			return true;
		//		} else if (NULL_NUMBER.equals(fieldValue)) {
		//			return true;
		//		} else if (fieldValue instanceof List) {
		//			List list = (List) fieldValue;
		//			if (list.contains(NULL_STRING) || list.contains(NULL_DATE_TIME) || list.contains(NULL_NUMBER) || list
		//					.contains(NULL_DATE)) {
		//				return true;
		//			}
		//		}

	}

	private SolrInputDocument buildDeltaSolrDocument(RecordDeltaDTO deltaDTO) {
		SolrInputDocument atomicUpdate = new ConstellioSolrInputDocument();
		atomicUpdate.addField("id", deltaDTO.getId());
		atomicUpdate.addField("_version_", deltaDTO.getFromVersion());

		for (Map.Entry<String, Object> modifiedField : deltaDTO.getModifiedFields().entrySet()) {
			Object solrValue = convertBigVaultValueToSolrValue(modifiedField.getKey(), modifiedField.getValue());
			atomicUpdate.addField(modifiedField.getKey(), LangUtils.newMapWithEntry("set", solrValue));
		}
		for (Map.Entry<String, Object> field : deltaDTO.getCopyfields().entrySet()) {
			Object solrValue = convertBigVaultValueToSolrValue(field.getKey(), field.getValue());
			atomicUpdate.addField(field.getKey(), LangUtils.newMapWithEntry("set", solrValue));
		}

		return atomicUpdate;
	}

	public long getCurrentVersion(String id) {

		try {
			return get(id).getVersion();
		} catch (NoSuchRecordWithId noSuchRecordWithId) {
			return -1L;
		}
	}

	@Override
	public DataStoreTypesFactory getTypesFactory() {
		return dataStoreTypesFactory;
	}

	public SecondTransactionLogManager getSecondTransactionLogManager() {
		return secondTransactionLogManager;
	}

	public BigVaultServer getBigVaultServer() {
		return bigVaultServer;
	}

	@Override
	public void expungeDeletes() {
		bigVaultServer.expungeDeletes();
	}

}
