package com.constellio.data.dao.services.transactionLog.sql;

import java.util.ArrayList;
import java.util.List;

public class TransactionLogContent {

	private String transactionId;

	private List<TransactionDocumentLogContent> newDocuments;

	private List<TransactionDocumentLogContent> updatedDocuments;

	private List<String> deletedRecords;

	private List<String> deletedQueries;

	public TransactionLogContent() {
		this.newDocuments = new ArrayList<>();
		this.updatedDocuments = new ArrayList<>();
		this.deletedRecords = new ArrayList<>();
		this.deletedQueries = new ArrayList<>();
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public List<TransactionDocumentLogContent> getNewDocuments() {
		return newDocuments;
	}

	public void setNewDocuments(
			List<TransactionDocumentLogContent> newDocuments) {
		this.newDocuments = newDocuments;
	}

	public List<TransactionDocumentLogContent> getUpdatedDocuments() {
		return updatedDocuments;
	}

	public void setUpdatedDocuments(
			List<TransactionDocumentLogContent> updatedDocuments) {
		this.updatedDocuments = updatedDocuments;
	}

	public List<String> getDeletedRecords() {
		return deletedRecords;
	}

	public void setDeletedRecords(List<String> deletedRecords) {
		this.deletedRecords = deletedRecords;
	}

	public List<String> getDeletedQueries() {
		return deletedQueries;
	}

	public void setDeletedQueries(List<String> deletedQueries) {
		this.deletedQueries = deletedQueries;
	}

	public void addNewDocuments(
			TransactionDocumentLogContent newDocument) {
		this.newDocuments.add(newDocument);
	}

	public void addUpdatedDocuments(
			TransactionDocumentLogContent updatedDocument) {
		this.updatedDocuments.add(updatedDocument);
	}

	public void addDeletedRecords(String deletedRecord) {
		this.deletedRecords.add(deletedRecord);
	}

	public void addDeletedQueries(String deletedQuery) {
		this.deletedQueries.add(deletedQuery);
	}
}
