package com.constellio.data.dao.services.recovery.transactionWriter;

import static org.apache.solr.client.solrj.util.ClientUtils.toSolrInputDocument;

import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.transactionLog.writer1.TransactionWriterV1;
import com.constellio.data.extensions.DataLayerSystemExtensions;

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

	public String addAllSolrDocuments(List<Object> documents) {
		StringBuilder stringBuilder = new StringBuilder("--transaction--\n");

		for (Object document : documents) {
			if(document instanceof  SolrInputDocument){
				appendAddUpdateSolrDocument(stringBuilder, (SolrInputDocument) document);
			}else if(document instanceof SolrDocument){
				appendAddUpdateSolrDocument(stringBuilder, toSolrInputDocument((SolrDocument) document));
			}else{
				throw new RuntimeException("Expecting solr document or solr input document : " + document.getClass().getName());
			}
		}
		return stringBuilder.toString();
	}


}
