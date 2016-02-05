package com.constellio.data.dao.dto.records;

import java.util.Collections;
import java.util.Map;

public class TransactionResponseDTO {

	private int qtime;

	private Map<String, Long> newDocumentVersions;

	public TransactionResponseDTO(int qtime, Map<String, Long> newDocumentVersions) {
		this.qtime = qtime;
		this.newDocumentVersions = Collections.unmodifiableMap(newDocumentVersions);
	}

	public int getQtime() {
		return qtime;
	}

	public Map<String, Long> getNewDocumentVersions() {
		return newDocumentVersions;
	}

	public Long getNewDocumentVersion(String id) {
		return newDocumentVersions.get(id);
	}
}
