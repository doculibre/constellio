package com.constellio.data.dao.dto.records;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.common.SolrInputDocument;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;

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

	public TransactionResponseDTO trimFrom(BigVaultServerTransaction transaction) {
		Map<String, Long> trimmedVersions = new HashMap<>();

		for (SolrInputDocument document : transaction.getNewDocuments()) {
			String id = (String) document.getFieldValue("id");
			trimmedVersions.put(id, newDocumentVersions.get(id));
		}

		for (SolrInputDocument document : transaction.getUpdatedDocuments()) {
			String id = (String) document.getFieldValue("id");
			trimmedVersions.put(id, newDocumentVersions.get(id));
		}

		return new TransactionResponseDTO(qtime, trimmedVersions);
	}
}
