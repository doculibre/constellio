package com.constellio.data.dao.services.bigVault;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.dto.records.TransactionSearchDTO;
import com.constellio.data.dao.services.DataLayerLogger;
import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.solr.ConstellioSolrInputDocument;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogManager;
import com.constellio.data.utils.KeyListMap;
import com.constellio.data.utils.LangUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BigVaultSearchDao extends BigVaultRecordDao {
	private static final Logger LOGGER = LoggerFactory.getLogger(BigVaultSearchDao.class);

	public static final String COLLECTION_FIELD = "collection_s";

	private static final String ID_FIELD = "id";
	public static final String DATE_SEARCH_FIELD = ".search_ss";

	private static List<String> searchDataFieldName = new ArrayList(Arrays.asList("id",
			"categoryId_s", "attachedPrincipalAncestorsIntIds_is", "schema_s", "archivisticStatus_s",
			"administrativeUnitId_s", "collection_s", "tokens_ss", "attachedAncestors_ss", "hidden_s", "deleted_s"));

	private static List<String> searchDataFieldendName = new ArrayList(Arrays.asList("_txt_fr", "_t_fr", "_txt_en", "_t_en"));

	private static long january1_1900 = new LocalDate(1900, 1, 1).toDate().getTime();

	public BigVaultSearchDao(BigVaultServer bigVaultServer, DataStoreTypesFactory dataStoreTypesFactory,
							 SecondTransactionLogManager secondTransactionLogManager, DataLayerLogger dataLayerLogger) {
		super(bigVaultServer, dataStoreTypesFactory,
				secondTransactionLogManager, dataLayerLogger);
	}

	public BigVaultServerTransaction prepare(TransactionSearchDTO transaction) {

		List<SolrInputDocument> newDocuments = new ArrayList<>();
		List<SolrInputDocument> updatedDocuments = new ArrayList<>();
		List<String> deletedRecordsIds = new ArrayList<>();
		prepareDocumentsForSolrTransaction(transaction, newDocuments, updatedDocuments, deletedRecordsIds);

		if (!newDocuments.isEmpty() || !updatedDocuments.isEmpty() || !deletedRecordsIds.isEmpty()) {
			return new BigVaultServerTransaction(transaction.getRecordsFlushing(), newDocuments, updatedDocuments,
					deletedRecordsIds, new ArrayList<>());
		} else {
			return null;
		}

	}

	@Override
	public TransactionResponseDTO executeSimple(TransactionSearchDTO transaction) {

		BigVaultServerTransaction bigVaultServerTransaction = prepare(transaction);

		if (bigVaultServerTransaction != null) {
			try {

				TransactionResponseDTO response = this.bigVaultServer.addAll(bigVaultServerTransaction);

				return response;
			} catch (Exception e) {

				throw new RecordDaoRuntimeException(e);
			}
		}
		return new TransactionResponseDTO(0, new HashMap<String, Long>());
	}

	private void prepareDocumentsForSolrTransaction(TransactionSearchDTO transaction,
													List<SolrInputDocument> newDocuments,
													List<SolrInputDocument> updatedDocuments,
													List<String> deletedRecordsIds) {
		KeyListMap<String, String> recordsAncestors = new KeyListMap<>();

		for (RecordDTO newRecord : transaction.getNewRecords()) {
			prepareNewRecord(newDocuments, recordsAncestors, newRecord);
		}
		for (RecordDeltaDTO modifiedRecord : transaction.getModifiedRecords()) {
			prepareModifiedRecord(updatedDocuments,
					recordsAncestors, modifiedRecord);
		}
		for (RecordDTO recordDTO : transaction.getDeletedRecords()) {
			prepareDeletedRecord(deletedRecordsIds, recordDTO);
		}

		filterDocumentsForSearch(newDocuments);
		filterDocumentsForSearch(updatedDocuments);

	}

	private void prepareModifiedRecord(List<SolrInputDocument> updatedDocuments,
									   KeyListMap<String, String> recordsAncestors, RecordDeltaDTO modifiedRecord) {
		if (!modifiedRecord.getModifiedFields().isEmpty() || !modifiedRecord.getIncrementedFields().isEmpty()) {
			SolrInputDocument solrInputDocument = buildDeltaSolrDocument(modifiedRecord);

			updatedDocuments.add(solrInputDocument);
			if (modifiedRecord.getModifiedFields().containsKey("path_ss")) {
				recordsAncestors.set(modifiedRecord.getId(), getModifiedRecordAncestors(modifiedRecord));
			}
		}
	}

	private void prepareNewRecord(List<SolrInputDocument> newDocuments,
								  KeyListMap<String, String> recordsAncestors, RecordDTO newRecord) {

		SolrInputDocument solrInputDocument = buildSolrDocument(newRecord);

		newDocuments.add(solrInputDocument);

		if (hasNoVersion(newRecord) && supportIndexes(newRecord)) {

			recordsAncestors.set(newRecord.getId(), getRecordAncestors(newRecord));
		}

	}

	private void filterDocumentsForSearch(List<SolrInputDocument> documents) {

		for (SolrInputDocument document : documents) {
			List<String> fields = document.getFieldNames().stream()
					.filter(field ->
							(searchDataFieldName.stream()
									.noneMatch(search -> field.equals(search)))
							&& (searchDataFieldendName.stream()
									.noneMatch(search -> field.endsWith(search)))
					)
					.collect(Collectors.toList());
			for (String field : fields) {
				document.removeField(field);
			}
		}
	}

	private boolean supportIndexes(RecordDTO record) {
		String schema = (String) record.getFields().get("schema_s");
		return schema != null;
	}

	private boolean hasNoVersion(RecordDTO record) {
		return record.getVersion() == -1;
	}

	private void prepareDeletedRecord(List<String> deletedRecordsIds,
									  RecordDTO recordDTO) {
		deletedRecordsIds.add(recordDTO.getId());
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

}
