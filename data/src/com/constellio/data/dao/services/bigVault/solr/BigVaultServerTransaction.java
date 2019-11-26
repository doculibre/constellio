package com.constellio.data.dao.services.bigVault.solr;

import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.solr.common.SolrInputDocument;

import java.util.ArrayList;
import java.util.List;

public class BigVaultServerTransaction {

	private String transactionId;
	private RecordsFlushing recordsFlushing;
	private List<SolrInputDocument> newDocuments = new ArrayList<>();
	private List<SolrInputDocument> updatedDocuments = new ArrayList<>();
	private List<String> deletedRecords = new ArrayList<>();
	private List<String> deletedQueries = new ArrayList<>();
	private boolean testRollBackMode = false;

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

	public int addUpdateSize() {
		return newDocuments.size() + updatedDocuments.size();
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

	@JsonIgnore
	private List<String> getAddUpdateDeleteRecordIds() {
		List<String> ids = new ArrayList<>();
		for (SolrInputDocument doc : newDocuments) {
			ids.add((String) doc.getFieldValue("id"));
		}
		for (SolrInputDocument doc : updatedDocuments) {
			ids.add((String) doc.getFieldValue("id"));
		}
		ids.addAll(deletedRecords);

		return ids;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@JsonIgnore
	public boolean isOnlyAdd() {
		return updatedDocuments.isEmpty() && deletedQueries.isEmpty() && deletedRecords.isEmpty();
	}

	@JsonIgnore
	public boolean isParallelisable() {
		boolean parallelisable = false;
		if (deletedRecords.isEmpty() && deletedQueries.isEmpty()) {
			parallelisable = true;

			for (SolrInputDocument solrInputDocument : updatedDocuments) {
				String id = (String) solrInputDocument.getFieldValue("id");
				if (!id.startsWith("idx_rfc")) {
					parallelisable = false;
				}
			}

		}
		return parallelisable;
	}

	public boolean isInTestRollbackMode() {
		return testRollBackMode;
	}

	public BigVaultServerTransaction setTestRollBackMode(boolean testRollBackMode) {
		this.testRollBackMode = testRollBackMode;
		return this;
	}

	@JsonIgnore
	public boolean isRequiringLock() {

		int updatedDocumentsWithOptimisticLocking = 0;
		for (SolrInputDocument updatedDocument : updatedDocuments) {
			if (updatedDocument.getField("_version_") != null) {
				if (updatedDocument.getField("markedForReindexing_s") == null) {
					updatedDocumentsWithOptimisticLocking++;
				}
			}
		}

		return updatedDocumentsWithOptimisticLocking > 1 || (updatedDocumentsWithOptimisticLocking == 1
															 && newDocuments.size() > 0);

	}
}
