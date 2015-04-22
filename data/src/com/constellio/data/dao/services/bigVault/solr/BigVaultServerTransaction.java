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
package com.constellio.data.dao.services.bigVault.solr;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;

import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;

public class BigVaultServerTransaction {

	private String transactionId;
	private RecordsFlushing recordsFlushing;
	private List<SolrInputDocument> newDocuments = new ArrayList<>();
	private List<SolrInputDocument> updatedDocuments = new ArrayList<>();
	private List<String> deletedRecords = new ArrayList<>();
	private List<String> deletedQueries = new ArrayList<>();

	public BigVaultServerTransaction(RecordsFlushing recordsFlushing,
			List<SolrInputDocument> newDocuments, List<SolrInputDocument> updatedDocuments,
			List<String> deletedRecords, List<String> deletedQueries) {
		this.transactionId = UUIDV1Generator.newRandomId();
		this.recordsFlushing = recordsFlushing;
		this.newDocuments = newDocuments;
		this.updatedDocuments = updatedDocuments;
		this.deletedRecords = deletedRecords;
		this.deletedQueries = deletedQueries;
	}

	public BigVaultServerTransaction(String transactionId, RecordsFlushing recordsFlushing,
			List<SolrInputDocument> newDocuments, List<SolrInputDocument> updatedDocuments,
			List<String> deletedRecords, List<String> deletedQueries) {
		this.transactionId = transactionId;
		this.recordsFlushing = recordsFlushing;
		this.newDocuments = newDocuments;
		this.updatedDocuments = updatedDocuments;
		this.deletedRecords = deletedRecords;
		this.deletedQueries = deletedQueries;
	}

	public BigVaultServerTransaction(RecordsFlushing recordsFlushing) {
		this.transactionId = UUIDV1Generator.newRandomId();
		this.recordsFlushing = recordsFlushing;
	}

	public RecordsFlushing getRecordsFlushing() {
		return recordsFlushing;
	}

	public List<SolrInputDocument> getNewDocuments() {
		return newDocuments;
	}

	public List<SolrInputDocument> getUpdatedDocuments() {
		return updatedDocuments;
	}

	public List<String> getDeletedRecords() {
		return deletedRecords;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public List<String> getDeletedQueries() {
		return deletedQueries;
	}

	public BigVaultServerTransaction setTransactionId(String transactionId) {
		this.transactionId = transactionId;
		return this;
	}

	public BigVaultServerTransaction setRecordsFlushing(RecordsFlushing recordsFlushing) {
		this.recordsFlushing = recordsFlushing;
		return this;
	}

	public BigVaultServerTransaction setNewDocuments(List<SolrInputDocument> newDocuments) {
		this.newDocuments = newDocuments;
		return this;
	}

	public BigVaultServerTransaction setUpdatedDocuments(List<SolrInputDocument> updatedDocuments) {
		this.updatedDocuments = updatedDocuments;
		return this;
	}

	public BigVaultServerTransaction setDeletedRecords(List<String> deletedRecords) {
		this.deletedRecords = deletedRecords;
		return this;
	}

	public BigVaultServerTransaction setDeletedQueries(List<String> deletedQueries) {
		this.deletedQueries = deletedQueries;
		return this;
	}

	public BigVaultServerTransaction addDeletedQuery(String deletedQuery) {
		this.deletedQueries.add(deletedQuery);
		return this;
	}
}
