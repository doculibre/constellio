package com.constellio.data.dao.services.recovery.transactionWriter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import com.constellio.data.dao.services.transactionLog.writer1.TransactionWriterV1;
import com.constellio.data.extensions.DataLayerSystemExtensions;

public class RecoveryTransactionWriter extends TransactionWriterV1 {
	private final File recordsBefore;

	public RecoveryTransactionWriter(File recordsBefore) {
		super(null);
		this.recordsBefore = recordsBefore;
	}

	@Override
	protected void appendDeletedByQuery(StringBuilder stringBuilder, String deletedByQuery) {
		throw new RuntimeException("Delete by query not supported in recovery mode");
	}

	public SolrInputDocument getDocument(String recordId) {
		return new SolrInputDocument();
	}

	public void addAll(List<SolrDocument> documents) {
		//TODO
		StringBuilder stringBuilder = new StringBuilder();

		for (SolrDocument document : documents) {
			//super.appendAddUpdateSolrDocument(stringBuilder, document);

		}

	}
}
