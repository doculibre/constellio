package com.constellio.data.dao.dto.sql;

public class RecordTransactionSqlDTO {

	private int id;

	private String recordId;

	private String solrVersion;

	private int logVersion;

	private String content;

	public RecordTransactionSqlDTO(String recordId, int logVersion, String solrVersion, String content) {
		this.recordId = recordId;
		this.solrVersion = solrVersion;
		this.logVersion = logVersion;
		this.content = content;
	}

	public RecordTransactionSqlDTO() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	public String getSolrVersion() {
		return solrVersion;
	}


	public int getLogVersion() {
		return logVersion;
	}

	public void setLogVersion(int logVersion) {
		this.logVersion = logVersion;
	}

	public void setSolrVersion(String solrVersion) {
		this.solrVersion = solrVersion;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
