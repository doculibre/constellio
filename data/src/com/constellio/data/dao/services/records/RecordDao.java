package com.constellio.data.dao.services.records;

import java.util.Iterator;
import java.util.List;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;

import com.constellio.data.dao.dto.records.QueryResponseDTO;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.dao.services.bigVault.RecordDaoException;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;

public interface RecordDao {

	RecordDTO get(String id)
			throws RecordDaoException.NoSuchRecordWithId;

	QueryResponseDTO query(SolrParams params);

	List<RecordDTO> searchQuery(SolrParams params);

	QueryResponse nativeQuery(SolrParams params);

	long documentsCount();

	DataStoreTypesFactory getTypesFactory();

	TransactionResponseDTO execute(TransactionDTO transaction)
			throws RecordDaoException.OptimisticLocking;

	List<String> getReferencedRecordsInHierarchy(String recordId);

	void flush();

	void removeOldLocks();

	long getCurrentVersion(String id);

	void recreateZeroCounterIndexesIn(String collection, Iterator<RecordDTO> recordsIterator);

	BigVaultServer getBigVaultServer();

	void expungeDeletes();
}
