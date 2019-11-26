package com.constellio.data.dao.dto.records;

import java.sql.Date;

public class TransactionSqlDTO {

	private int id;
	private String transactionUUID;
	private Date timestamp;
	private int logVersion;
	private String transactionSummary;
	private String content;

	public TransactionSqlDTO() {
	}

	public TransactionSqlDTO(String transactionUUID, Date timestamp, int logVersion,
							 String transactionSummary, String content) {

		this.transactionUUID = transactionUUID;
		this.timestamp = timestamp;
		this.logVersion = logVersion;
		this.transactionSummary = transactionSummary;
		this.content = content;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTransactionUUID() {
		return transactionUUID;
	}

	public void setTransactionUUID(String transactionUUID) {
		this.transactionUUID = transactionUUID;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public int getLogVersion() {
		return logVersion;
	}

	public void setLogVersion(int logVersion) {
		this.logVersion = logVersion;
	}

	public String getTransactionSummary() {
		return transactionSummary;
	}

	public void setTransactionSummary(String transactionSummary) {
		this.transactionSummary = transactionSummary;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
