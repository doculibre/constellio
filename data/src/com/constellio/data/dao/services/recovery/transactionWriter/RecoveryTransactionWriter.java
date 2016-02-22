package com.constellio.data.dao.services.recovery.transactionWriter;

import static org.apache.solr.client.solrj.util.ClientUtils.toSolrInputDocument;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.bigVault.solr.SolrUtils;
import com.constellio.data.dao.services.solr.ConstellioSolrInputDocument;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_CannotParseLogCommand;
import com.constellio.data.dao.services.transactionLog.writer1.TransactionWriterV1;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.data.utils.KeyListMap;

public class RecoveryTransactionWriter extends TransactionWriterV1 {

	public RecoveryTransactionWriter(DataLayerSystemExtensions extensions) {
		super(extensions);
	}

	@Override
	protected void appendDeletedByQuery(StringBuilder stringBuilder, String deletedByQuery) {
		throw new RuntimeException("Delete by query not supported in recovery mode");
	}

	@Override
	public String toLogEntry(BigVaultServerTransaction transaction) {
		if (transaction.getDeletedQueries()!= null && !transaction.getDeletedQueries().isEmpty()) {
			throw new RuntimeException("Delete by query not supported in recovery mode");
		}

		StringBuilder stringBuilder = new StringBuilder("--transaction--\n");

		for (SolrInputDocument solrDocument : transaction.getUpdatedDocuments()) {
			appendAddUpdateSolrDocument(stringBuilder, solrDocument);
		}
		return stringBuilder.toString();
	}

	public String addAll(List<SolrDocument> documents) {
		StringBuilder stringBuilder = new StringBuilder("--transaction--\n");

		for (SolrDocument document : documents) {
			appendAddUpdateSolrDocument(stringBuilder, toSolrInputDocument(document));
		}
		return stringBuilder.toString();
	}

}
