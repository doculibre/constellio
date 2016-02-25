package com.constellio.data.dao.services.recovery;

import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.recovery.transactionWriter.RecoveryTransactionWriter;
import com.constellio.data.dao.services.transactionLog.TransactionLogReadWriteServices;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.data.io.services.facades.IOServices;

public class RecoveryTransactionReadWriteServices extends TransactionLogReadWriteServices {
	public RecoveryTransactionReadWriteServices(IOServices ioServices,
			DataLayerConfiguration configuration) {
		super(ioServices, configuration, new AcceptAllExtension());
	}

	@Override
	public String toLogEntry(BigVaultServerTransaction transaction) {
		return new RecoveryTransactionWriter(new AcceptAllExtension()).toLogEntry(transaction);
	}

	public String toLogEntry(List<Object> documents) {
		return new RecoveryTransactionWriter(new AcceptAllExtension()).addAllSolrDocuments(documents);
	}

	private static class AcceptAllExtension extends DataLayerSystemExtensions {
		@Override
		public boolean isDocumentFieldLoggedInTransactionLog(String field, String schema, String collection,
				boolean defaultValue) {
			return true;
		}
	}
}
