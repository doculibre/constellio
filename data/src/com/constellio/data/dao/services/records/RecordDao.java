/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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

}
