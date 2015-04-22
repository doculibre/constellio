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
package com.constellio.data.dao.dto.records;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.solr.common.params.SolrParams;

public class TransactionDTO {

	private String transactionId;

	public List<RecordDTO> newRecords;

	public List<RecordDeltaDTO> modifiedRecords;
	private List<RecordDTO> deletedRecords;

	private List<SolrParams> deletedByQueries;

	private Map<String, RecordDTO> newRecordsById;

	private RecordsFlushing recordsFlushing;

	public TransactionDTO(RecordsFlushing recordsFlushing) {
		this.transactionId = UUID.randomUUID().toString();
		this.recordsFlushing = recordsFlushing;
		this.newRecords = Collections.emptyList();
		this.modifiedRecords = Collections.emptyList();
		this.deletedRecords = Collections.emptyList();
		this.deletedByQueries = Collections.emptyList();
		newRecordsById = buildNewRecordsByIdMap();
	}

	public TransactionDTO(RecordsFlushing recordsFlushing, String transactionId) {
		this.transactionId = transactionId;
		this.recordsFlushing = recordsFlushing;
		this.newRecords = Collections.emptyList();
		this.modifiedRecords = Collections.emptyList();
		this.deletedRecords = Collections.emptyList();
		this.deletedByQueries = Collections.emptyList();
		newRecordsById = buildNewRecordsByIdMap();
	}

	public TransactionDTO(String transactionId, RecordsFlushing recordsFlushing, List<RecordDTO> newRecords,
			List<RecordDeltaDTO> modifiedRecords) {
		super();
		this.transactionId = transactionId;
		this.recordsFlushing = recordsFlushing;
		this.newRecords = Collections.unmodifiableList(newRecords);
		this.modifiedRecords = Collections.unmodifiableList(modifiedRecords);
		this.deletedRecords = Collections.emptyList();
		this.deletedByQueries = Collections.emptyList();
		newRecordsById = buildNewRecordsByIdMap();
	}

	public TransactionDTO(String transactionId, RecordsFlushing recordsFlushing, List<RecordDTO> newRecords,
			List<RecordDeltaDTO> modifiedRecords, List<RecordDTO> deletedRecords, List<SolrParams> deletedByQueries) {
		super();
		this.transactionId = transactionId;
		this.recordsFlushing = recordsFlushing;
		this.newRecords = Collections.unmodifiableList(newRecords);
		this.modifiedRecords = Collections.unmodifiableList(modifiedRecords);
		this.deletedRecords = Collections.unmodifiableList(deletedRecords);
		this.deletedRecords = Collections.unmodifiableList(deletedRecords);
		this.deletedByQueries = Collections.unmodifiableList(deletedByQueries);
		newRecordsById = buildNewRecordsByIdMap();
	}

	public List<RecordDTO> getNewRecords() {
		return newRecords;
	}

	public List<RecordDeltaDTO> getModifiedRecords() {
		return modifiedRecords;

	}

	public List<RecordDTO> getDeletedRecords() {
		return deletedRecords;
	}

	public List<SolrParams> getDeletedByQueries() {
		return deletedByQueries;
	}

	public TransactionDTO withNewRecords(List<RecordDTO> records) {
		List<RecordDTO> newRecords = new ArrayList<>();
		newRecords.addAll(this.newRecords);
		newRecords.addAll(records);

		return new TransactionDTO(transactionId, recordsFlushing, newRecords, modifiedRecords, deletedRecords, deletedByQueries);
	}

	public TransactionDTO withModifiedRecords(List<RecordDeltaDTO> records) {
		List<RecordDeltaDTO> modifiedRecords = new ArrayList<>();
		modifiedRecords.addAll(this.modifiedRecords);
		modifiedRecords.addAll(records);

		return new TransactionDTO(transactionId, recordsFlushing, newRecords, modifiedRecords, deletedRecords, deletedByQueries);
	}

	public TransactionDTO withDeletedRecords(List<RecordDTO> records) {
		List<RecordDTO> deletedRecords = new ArrayList<>();
		deletedRecords.addAll(this.deletedRecords);
		deletedRecords.addAll(records);

		return new TransactionDTO(transactionId, recordsFlushing, newRecords, modifiedRecords, deletedRecords, deletedByQueries);
	}

	public TransactionDTO withDeletedByQueries(SolrParams... deletedByQueries) {
		return withDeletedByQueries(Arrays.asList(deletedByQueries));
	}

	public TransactionDTO withDeletedByQueries(List<SolrParams> deletedByQueries) {
		List<SolrParams> queries = new ArrayList<>();
		queries.addAll(this.deletedByQueries);
		queries.addAll(deletedByQueries);

		return new TransactionDTO(transactionId, recordsFlushing, newRecords, modifiedRecords, deletedRecords, queries);
	}

	public boolean hasRecord(String value) {
		return newRecordsById.containsKey(value);
	}

	public RecordsFlushing getRecordsFlushing() {
		return recordsFlushing;
	}

	private Map<String, RecordDTO> buildNewRecordsByIdMap() {

		Map<String, RecordDTO> allRecordsByIdMap = new HashMap<>();
		for (RecordDTO newRecord : newRecords) {
			allRecordsByIdMap.put(newRecord.getId(), newRecord);
		}

		return allRecordsByIdMap;
	}

	public String getTransactionId() {
		return transactionId;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "transactionId");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "transactionId");
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
