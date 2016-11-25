package com.constellio.data.extensions;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.data.frameworks.extensions.ExtensionUtils.BooleanCaller;
import com.constellio.data.frameworks.extensions.VaultBehaviorsList;

import org.apache.solr.common.params.SolrParams;

import static com.constellio.data.frameworks.extensions.ExtensionUtils.getBooleanValue;

public class DataLayerSystemExtensions {

	//------------ Extension points -----------
	public VaultBehaviorsList<BigVaultServerExtension> bigVaultServerExtension = new VaultBehaviorsList<>();
	public VaultBehaviorsList<TransactionLogExtension> transactionLogExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<BigVaultServerExtension> getBigVaultServerExtension() {
		return bigVaultServerExtension;
	}

	public void afterQuery(SolrParams params, long qtime) {
		for (BigVaultServerExtension extension : bigVaultServerExtension) {
			try {
				extension.afterQuery(params, qtime);
			} catch (Exception e) {
				//	e.printStackTrace();
			}
		}
	}

	public void afterUpdate(BigVaultServerTransaction transaction, long qtime) {
		for (BigVaultServerExtension extension : bigVaultServerExtension) {
			extension.afterUpdate(transaction, qtime);
		}
	}

	//----------------- Callers ---------------

	public boolean isDocumentFieldLoggedInTransactionLog(final String field, final String schema, final String collection,
			boolean defaultValue) {
		return getBooleanValue(getTransactionLogExtensions(), defaultValue, new BooleanCaller<TransactionLogExtension>() {
			@Override
			public ExtensionBooleanResult call(TransactionLogExtension extension) {
				return extension.isDocumentFieldLoggedInTransactionLog(field, schema, collection);
			}
		});
	}

	public VaultBehaviorsList<TransactionLogExtension> getTransactionLogExtensions() {
		return transactionLogExtensions;
	}

}
