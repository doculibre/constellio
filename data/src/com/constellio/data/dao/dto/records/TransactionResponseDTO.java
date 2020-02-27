package com.constellio.data.dao.dto.records;

import java.util.Collections;
import java.util.Map;

public class TransactionResponseDTO {

	private int qtime;

	private int rf;

	private Map<String, Long> newDocumentVersions;

	public TransactionResponseDTO(int qtime, Map<String, Long> newDocumentVersions) {
		this(qtime, -1, newDocumentVersions);
	}

	public TransactionResponseDTO(int qtime, int rf, Map<String, Long> newDocumentVersions) {
		this.qtime = qtime;
		this.rf = rf;
		this.newDocumentVersions = Collections.unmodifiableMap(newDocumentVersions);
	}

	public int getQtime() {
		return qtime;
	}

	public int getRf() {
		return rf;
	}

	public Map<String, Long> getNewDocumentVersions() {
		return newDocumentVersions;
	}

	public Long getNewDocumentVersion(String id) {
		return newDocumentVersions.get(id);
	}
}
