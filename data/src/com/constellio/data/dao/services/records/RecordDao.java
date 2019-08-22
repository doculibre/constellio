package com.constellio.data.dao.services.records;

import com.constellio.data.dao.dto.records.QueryResponseDTO;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.dao.services.bigVault.RecordDaoException;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;

import java.util.Iterator;
import java.util.List;

public interface RecordDao {

	RecordDTO get(String id, boolean callExtensions)
			throws RecordDaoException.NoSuchRecordWithId;

	RecordDTO realGet(String id, boolean callExtensions)
			throws RecordDaoException.NoSuchRecordWithId;

	List<RecordDTO> realGet(List<String> ids, boolean callExtensions);

	default RecordDTO get(String id)
			throws RecordDaoException.NoSuchRecordWithId {
		return get(id, true);
	}

	default RecordDTO realGet(String id)
			throws RecordDaoException.NoSuchRecordWithId {
		return realGet(id, true);
	}

	default List<RecordDTO> realGet(List<String> ids) {
		return realGet(ids, true);
	}

	QueryResponseDTO query(String queryName, SolrParams params);

	List<RecordDTO> searchQuery(String queryName, SolrParams params);

	QueryResponse nativeQuery(String queryName, SolrParams params);

	QueryResponseDTO query(SolrParams params);

	List<RecordDTO> searchQuery(SolrParams params);

	QueryResponse nativeQuery(SolrParams params);

	long documentsCount();

	DataStoreTypesFactory getTypesFactory();

	TransactionResponseDTO execute(TransactionDTO transaction)
			throws RecordDaoException.OptimisticLocking;

	//List<String> getReferencedRecordsInHierarchy(String recordId);

	void flush();

	void removeOldLocks();

	long getCurrentVersion(String id);

	void recreateZeroCounterIndexesIn(String collection, Iterator<RecordDTO> recordsIterator);

	BigVaultServer getBigVaultServer();

	void expungeDeletes();
}
