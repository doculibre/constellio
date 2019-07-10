package com.constellio.data.dao.services.bigVault;

import com.constellio.data.dao.dto.records.FacetPivotValue;
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.data.dao.dto.records.MoreLikeThisDTO;
import com.constellio.data.dao.dto.records.QueryResponseDTO;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDTOMode;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.SolrRecordDTO;
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
import com.constellio.data.utils.KeyListMap;
import com.constellio.data.utils.LangUtils;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.FieldStatsInfo;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Collation;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Correction;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.MoreLikeThisParams;
import org.apache.solr.common.params.SolrParams;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.constellio.data.dao.services.bigVault.solr.SolrUtils.NULL_ITEM_LOCALDATE;
import static com.constellio.data.dao.services.bigVault.solr.SolrUtils.NULL_ITEM_LOCAL_DATE_TIME;
import static com.constellio.data.dao.services.bigVault.solr.SolrUtils.convertLocalDateTimeToSolrDate;
import static com.constellio.data.dao.services.bigVault.solr.SolrUtils.convertLocalDateToSolrDate;
import static com.constellio.data.dao.services.bigVault.solr.SolrUtils.convertNullToSolrValue;
import static com.constellio.data.dao.services.bigVault.solr.SolrUtils.isMultiValueStringOrText;
import static com.constellio.data.dao.services.bigVault.solr.SolrUtils.isMultivalue;
import static com.constellio.data.dao.services.bigVault.solr.SolrUtils.isSingleValueStringOrText;

public class BigVaultRecordDao implements RecordDao {
	private static final Logger LOGGER = LoggerFactory.getLogger(BigVaultRecordDao.class);

	public static final Integer NULL_NUMBER = Integer.MIN_VALUE;
	public static final String COLLECTION_FIELD = "collection_s";
	//	public static final String REF_COUNT_PREFIX = "idx_rfc_";
	//	public static final String ACTIVE_IDX_PREFIX = "idx_act_";

	public static final String REFCOUNT_FIELD = "refs_d";
	public static final String TYPE_FIELD = "type_s";
	public static final String ANCESTORS_FIELD = "ancestors_ss";
	public static final String PRINCIPALPATH_FIELD = "principalpath_s";
	public static final String DELETED_FIELD = "deleted_s";
	private static final String ID_FIELD = "id";
	private static final String VERSION_FIELD = "_version_";
	public static final String DATE_SEARCH_FIELD = ".search_ss";
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
					secondTransactionLogManager.flush(transaction.getTransactionId(), response);
				}
				return response;
			} catch (BigVaultException.OptimisticLocking e) {
				if (secondTransactionLogManager != null) {
					secondTransactionLogManager.cancel(transaction.getTransactionId());
				}
				throw new RecordDaoException.OptimisticLocking(e.getId(), e.getVersion(), e);
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
													List<SolrInputDocument> updatedDocuments,
													List<String> deletedRecordsIds) {
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

	private void prepareModifiedRecord(TransactionDTO transaction, List<SolrInputDocument> newDocuments,
									   List<SolrInputDocument> updatedDocuments, List<String> deletedRecordsIds,
									   Map<String, Double> recordsInTransactionRefCounts,
									   Map<String, Double> recordsOutOfTransactionRefCounts,
									   KeyListMap<String, String> recordsAncestors, RecordDeltaDTO modifiedRecord) {
		if (!modifiedRecord.getModifiedFields().isEmpty() || !modifiedRecord.getIncrementedFields().isEmpty()) {
			SolrInputDocument solrInputDocument = buildDeltaSolrDocument(modifiedRecord);
			if (transaction.isFullRewrite()) {
				solrInputDocument.removeField("_version_");
			}
			updatedDocuments.add(solrInputDocument);
			if (modifiedRecord.getModifiedFields().containsKey("path_ss")) {
				recordsAncestors.set(modifiedRecord.getId(), getModifiedRecordAncestors(modifiedRecord));
			}
		}
	}

	private void prepareNewRecord(TransactionDTO transaction, List<SolrInputDocument> newDocuments,
								  Map<String, Double> recordsInTransactionRefCounts,
								  Map<String, Double> recordsOutOfTransactionRefCounts,
								  KeyListMap<String, String> recordsAncestors,
								  Map<Object, SolrInputDocument> activeReferencesCheck, RecordDTO newRecord) {

		SolrInputDocument solrInputDocument = buildSolrDocument(newRecord);
		if (transaction.isFullRewrite()) {
			solrInputDocument.removeField("_version_");
		}
		newDocuments.add(solrInputDocument);

		if (hasNoVersion(newRecord) && supportIndexes(newRecord)) {
			if (!recordsInTransactionRefCounts.containsKey(newRecord.getId())) {
				recordsInTransactionRefCounts.put(newRecord.getId(), 0.0);
			}
			recordsAncestors.set(newRecord.getId(), getRecordAncestors(newRecord));
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

	private void prepareDeletedRecord(TransactionDTO transaction, List<String> deletedRecordsIds,
									  Map<String, Double> recordsInTransactionRefCounts,
									  Map<String, Double> recordsOutOfTransactionRefCounts,
									  RecordDTO recordDTO) {
		deletedRecordsIds.add(recordDTO.getId());
		decrementReferenceCounterForAllReferences(recordDTO, transaction, recordsInTransactionRefCounts,
				recordsOutOfTransactionRefCounts);
	}

	@Override
	public void flush() {
		try {
			bigVaultServer.flush();
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

			TransactionResponseDTO response = bigVaultServer.addAll(transaction);

			if (secondTransactionLogManager != null) {
				secondTransactionLogManager.flush(transactionId, response);
			}

		} catch (BigVaultException e) {
			if (secondTransactionLogManager != null) {
				secondTransactionLogManager.cancel(transactionId);
			}
			throw new RuntimeException(e);
		}

	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private List<String> getRecordAncestors(RecordDTO newRecord) {
		List<String> recordsAncestors = new ArrayList<>();
		List<String> parentPaths = toParentPaths((List) newRecord.getFields().get("path_ss"));
		if (parentPaths != null) {
			for (String parentPath : parentPaths) {
				if (parentPath != null) {
					addParentPathToRecordsAncestors(newRecord.getId(), recordsAncestors, parentPath);
				}
			}
		}
		return recordsAncestors;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private List<String> getModifiedRecordAncestors(RecordDeltaDTO modifiedRecord) {

		List<String> recordsAncestors = new ArrayList<>();
		List<String> parentPaths = toParentPaths((List) modifiedRecord.getModifiedFields().get("path_ss"));
		if (parentPaths != null) {
			for (String parentPath : parentPaths) {
				addParentPathToRecordsAncestors(modifiedRecord.getId(), recordsAncestors, parentPath);
			}
		}
		return recordsAncestors;

	}

	private List<String> toParentPaths(List paths) {
		List<String> parentPaths = new ArrayList<>();
		if (paths != null) {
			for (Object path : paths) {
				parentPaths.add(StringUtils.substringBeforeLast(path.toString(), "/"));
			}
		}
		return parentPaths;
	}

	private void addParentPathToRecordsAncestors(String recordId, List<String> recordsAncestors, String parentPath) {
		for (String parentId : parentPath.split("/")) {
			if (!recordsAncestors.contains(parentId) && !parentId.equals(recordId) && StringUtils.isNotEmpty(parentId)) {
				recordsAncestors.add(parentId);
			}
		}
	}

	private void incrementReferenceCounterForAllReferences(RecordDTO newRecord, TransactionDTO transaction,
														   Map<String, Double> recordsInTransactionRefCounts,
														   Map<String, Double> recordsOutOfTransactionRefCounts) {
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
														   Map<String, Double> recordsInTransactionRefCounts,
														   Map<String, Double> recordsOutOfTransactionRefCounts) {
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

	private void addReferenceToRecordMapToIncrement(String referenceId,
													Map<String, Double> recordsOutOfTransactionRefCounts,
													RecordDTO newRecord) {
		if (referenceIsNotParentOrPrincipalConcept(referenceId, newRecord)) {
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

	private void addReferenceToRecordMapToDecrement(String referenceId,
													Map<String, Double> recordsOutOfTransactionRefCounts,
													RecordDTO newRecord) {
		if (referenceIsNotParentOrPrincipalConcept(referenceId, newRecord)) {
			addReferenceToMapWithValue(referenceId, recordsOutOfTransactionRefCounts, -1.0);
		}
	}

	private Object getCollection(RecordDTO newRecord) {
		return newRecord.getFields().get(COLLECTION_FIELD);
	}

	private void incrementReferenceCounterForReferencesInMultivalueField(RecordDTO newRecord,
																		 TransactionDTO transaction,
																		 Entry<String, Object> field,
																		 Map<String, Double> recordsInTransactionRefCounts,
																		 Map<String, Double> recordsOutOfTransactionRefCounts) {
		for (Object referenceId : LangUtils.withoutDuplicates((List) field.getValue())) {
			if (!referencedIdIsNewRecordInTransaction(referenceId, transaction)) {
				addReferenceToRecordMapToIncrement((String) referenceId, recordsOutOfTransactionRefCounts, newRecord);
			} else {
				addReferenceToRecordMapToIncrement((String) referenceId, recordsInTransactionRefCounts, newRecord);
			}
		}
	}

	private void decrementReferenceCounterForReferencesInMultivalueField(RecordDTO newRecord,
																		 TransactionDTO transaction,
																		 Entry<String, Object> field,
																		 Map<String, Double> recordsInTransactionRefCounts,
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

	@Override
	public RecordDTO get(String id)
			throws RecordDaoException.NoSuchRecordWithId {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("fq", ID_FIELD + ":" + id);
		params.set("q", "*:*");

		RecordDTO entity = querySingleDocument("getById:" + id, params);
		if (entity == null) {
			throw new RecordDaoException.NoSuchRecordWithId(id, bigVaultServer.getName());
		} else {
			return entity;
		}
	}

	@Override
	public RecordDTO realGet(String id)
			throws RecordDaoException.NoSuchRecordWithId {
		SolrDocument solrDocument;
		try {
			solrDocument = bigVaultServer.realtimeGet(id);
		} catch (BigVaultException.CouldNotExecuteQuery e) {
			throw new BigVaultRuntimeException.CannotQuerySingleDocument(e);
		}

		if (solrDocument == null) {
			throw new RecordDaoException.NoSuchRecordWithId(id, bigVaultServer.getName());
		} else {
			return toEntity(solrDocument, RecordDTOMode.FULLY_LOADED);
		}
	}

	@Override
	public List<RecordDTO> realGet(List<String> ids) {

		List<RecordDTO> recordDTOS = new ArrayList<>();

		try {

			for (SolrDocument solrDocument : bigVaultServer.realtimeGet(ids)) {
				if (solrDocument != null) {
					recordDTOS.add(toEntity(solrDocument, RecordDTOMode.FULLY_LOADED));
				}
			}
		} catch (BigVaultException.CouldNotExecuteQuery e) {
			throw new BigVaultRuntimeException.CannotQuerySingleDocument(e);
		}

		return recordDTOS;
	}

	@Override
	public QueryResponse nativeQuery(String queryName, SolrParams params) {
		try {
			QueryResponse response = bigVaultServer.query(queryName, params);
			dataLayerLogger.logQueryResponse(queryName, params, response);
			return response;
		} catch (BigVaultException.CouldNotExecuteQuery e) {
			throw new BigVaultRuntimeException.CannotListDocuments(e);
		}
	}

	@Override
	public List<RecordDTO> searchQuery(String queryName, SolrParams params) {
		return query(queryName, params).getResults();
	}

	private RecordDTO querySingleDocument(String queryName, ModifiableSolrParams params) {
		QueryResponse response = null;
		try {
			response = bigVaultServer.query(queryName, params);
		} catch (BigVaultException.CouldNotExecuteQuery e) {
			throw new BigVaultRuntimeException.CannotQuerySingleDocument(e);
		}
		boolean partialFields = params.get(CommonParams.FL) != null;
		SolrDocumentList documents = response.getResults();
		if (documents.isEmpty()) {
			return null;
		} else {
			return toEntity(documents.get(0), partialFields ? RecordDTOMode.CUSTOM : RecordDTOMode.FULLY_LOADED);
		}
	}

	public QueryResponseDTO query(String queryName, SolrParams params) {
		QueryResponse response = nativeQuery(queryName, params);
		boolean partialFields = params.get(CommonParams.FL) != null;

		List<RecordDTO> documents = new ArrayList<RecordDTO>();
		SolrDocumentList solrDocuments = response.getResults();
		if (solrDocuments != null) {
			for (SolrDocument solrDocument : solrDocuments) {
				documents.add(toEntity(solrDocument, partialFields ? RecordDTOMode.CUSTOM : RecordDTOMode.FULLY_LOADED));
			}
		}
		Map<String, Map<String, List<String>>> highlights = response.getHighlighting();
		Map<String, List<FacetValue>> fieldFacetValues = getFieldFacets(response);
		Map<String, List<FacetPivotValue>> fieldFacetPivotValues = getFieldFacetPivots(response);
		Map<String, Map<String, Object>> fieldsStatistics = getFieldsStats(response);
		Map<String, Integer> facetQueries = response.getFacetQuery();

		boolean correctlySpelt = true;
		List<String> spellcheckerSuggestions = new ArrayList<String>();

		SpellCheckResponse spellCheckResponse = response.getSpellCheckResponse();
		if (spellCheckResponse != null) {
			correctlySpelt = spellCheckResponse.isCorrectlySpelled();
			spellcheckerSuggestions = spellcheckerSuggestions(spellCheckResponse);
		}


		List<MoreLikeThisDTO> resultWithMoreLikeThis = new ArrayList<>();
		if (params.get(MoreLikeThisParams.MLT) != null
			&& Boolean.parseBoolean(params.get(MoreLikeThisParams.MLT))) {
			try {
				resultWithMoreLikeThis = extractMoreLikeThis(response, params.get(MoreLikeThisParams.SIMILARITY_FIELDS), partialFields);
			} catch (SolrServerException | IOException e) {
				throw new BigVaultRuntimeException.CannotListDocuments(e);
			}
		}

		long numfound = response.getResults() == null ? 0 : response.getResults().getNumFound();

		return new QueryResponseDTO(documents, response.getQTime(), numfound, fieldFacetValues, fieldFacetPivotValues,
				fieldsStatistics, facetQueries, highlights, correctlySpelt, spellcheckerSuggestions, resultWithMoreLikeThis);
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

	private List<MoreLikeThisDTO> extractMoreLikeThis(QueryResponse response, String moreLikeThisFields,
													  boolean partialFields)
			throws SolrServerException, IOException {
		List<MoreLikeThisDTO> moreLikeThisResults = new ArrayList<>();

		if (moreLikeThisFields != null && response.getResponse().get("response") != null
			&& response.getResponse().get("match") != null) {
			List<SolrDocument> results = ((List<SolrDocument>) response.getResponse().get("response"));
			SolrDocument aSolrDocument = ((List<SolrDocument>) response.getResponse().get("match")).get(0);
			JaccardDocumentSorter sorter = new JaccardDocumentSorter(bigVaultServer, aSolrDocument, moreLikeThisFields, "id");
			List<SolrDocument> sortedResults = sorter.sort(results);
			for (SolrDocument aSimilarDoc : sortedResults) {
				Double score = (Double) aSimilarDoc.get(JaccardDocumentSorter.SIMILARITY_SCORE_FIELD);
				aSimilarDoc.remove(JaccardDocumentSorter.SIMILARITY_SCORE_FIELD);
				RecordDTO entity = toEntity(aSimilarDoc, partialFields ? RecordDTOMode.CUSTOM : RecordDTOMode.FULLY_LOADED);
				moreLikeThisResults.add(new MoreLikeThisDTO(entity, score));
			}

		}
		return moreLikeThisResults;
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

	private Map<String, List<FacetPivotValue>> getFieldFacetPivots(QueryResponse response) {
		Map<String, List<FacetPivotValue>> facetPivotValues = new LinkedHashMap<>();

		if (response.getFacetPivot() != null) {
			for (int i = 0; i < response.getFacetPivot().size(); i++) {
				List<FacetPivotValue> fieldfacetPivotValues = new ArrayList<>();

				List<PivotField> pivotFields = response.getFacetPivot().getVal(i);
				for (PivotField pivotField : pivotFields) {
					fieldfacetPivotValues.add(convertPivotFieldToFacetPivotValue(pivotField));
				}
				facetPivotValues.put(response.getFacetPivot().getName(i), fieldfacetPivotValues);
			}
		}

		return facetPivotValues;
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

		if (entity.getCopyFields() != null) {
			for (Map.Entry<String, Object> field : entity.getCopyFields().entrySet()) {
				document.addField(field.getKey(), field.getValue());
			}
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

	@SuppressWarnings({"rawtypes", "unchecked"})
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

	protected RecordDTO toEntity(SolrDocument solrDocument, RecordDTOMode mode) {
		String id = (String) solrDocument.get(ID_FIELD);
		long version = (Long) solrDocument.get(VERSION_FIELD);

		Map<String, Object> fieldValues = new HashMap<String, Object>();

		for (String fieldName : solrDocument.getFieldNames()) {
			if (!fieldName.equals("sys_s") && !containsTwoUnderscoresAndIsNotVersionField(fieldName) && !fieldName.endsWith(DATE_SEARCH_FIELD)) {
				Object value = convertSolrValueToBigVaultValue(fieldName, solrDocument.getFieldValue(fieldName));
				if (value != null) {
					fieldValues.put(fieldName, value);
				}
			}
		}
		return new SolrRecordDTO(id, version, fieldValues, mode);
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

	@SuppressWarnings({"rawtypes", "unchecked"})
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

	@SuppressWarnings({"rawtypes", "unchecked"})
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

	private FacetPivotValue convertPivotFieldToFacetPivotValue(PivotField pivotField) {
		FacetPivotValue facetPivotValue = FacetPivotValue.builder()
				.field(pivotField.getField())
				.value(pivotField.getValue())
				.count(pivotField.getCount())
				.build();

		if (pivotField.getPivot() != null && !pivotField.getPivot().isEmpty()) {
			List<FacetPivotValue> facetPivotValues = new ArrayList<>();
			for (PivotField nestedPivotField : pivotField.getPivot()) {
				facetPivotValues.add(convertPivotFieldToFacetPivotValue(nestedPivotField));
			}
			facetPivotValue.setFacetPivotValues(facetPivotValues);
		}

		return facetPivotValue;
	}

	private boolean isSolrNullValue(Object fieldValue) {

		if (fieldValue == null) {
			return true;
		} else if (fieldValue instanceof List) {
			return ((List) fieldValue).isEmpty();
		} else {
			return false;
		}

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
		for (Map.Entry<String, Double> field : deltaDTO.getIncrementedFields().entrySet()) {
			Object solrValue = convertBigVaultValueToSolrValue(field.getKey(), field.getValue());
			atomicUpdate.addField(field.getKey(), LangUtils.newMapWithEntry("inc", solrValue));
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

	@Override
	public List<RecordDTO> searchQuery(SolrParams params) {
		return searchQuery(null, params);
	}

	@Override
	public QueryResponseDTO query(SolrParams params) {
		return query(null, params);
	}

	@Override
	public QueryResponse nativeQuery(SolrParams params) {
		return nativeQuery(null, params);
	}
}
