package com.constellio.data.extensions;

import static com.constellio.data.frameworks.extensions.ExtensionUtils.getBooleanValue;

import org.apache.solr.common.params.SolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.data.frameworks.extensions.ExtensionUtils.BooleanCaller;
import com.constellio.data.frameworks.extensions.VaultBehaviorsList;

public class DataLayerSystemExtensions {

	private static final Logger VAULT_LOGGER = LoggerFactory.getLogger(DataLayerSystemExtensions.class);

	//------------ Extension points -----------
	public VaultBehaviorsList<BigVaultServerExtension> bigVaultServerExtension = new VaultBehaviorsList<>();
	public VaultBehaviorsList<TransactionLogExtension> transactionLogExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<BigVaultServerExtension> getBigVaultServerExtension() {
		return bigVaultServerExtension;
	}

	public void afterQuery(final SolrParams params, final long qtime, final int resultsSize) {
		for (BigVaultServerExtension extension : bigVaultServerExtension) {
			try {
				extension.afterQuery(params, qtime);
				extension.afterQuery(new AfterQueryParams() {
					@Override
					public SolrParams getSolrParams() {
						return params;
					}

					@Override
					public long getQtime() {
						return qtime;
					}

					@Override
					public int getReturnedResultsCount() {
						return resultsSize;
					}

				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void afterUpdate(BigVaultServerTransaction transaction, long qtime) {
		for (BigVaultServerExtension extension : bigVaultServerExtension) {
			extension.afterUpdate(transaction, qtime);
		}
	}

	public void afterCommmit(BigVaultServerTransaction transaction, long qtime) {
		for (BigVaultServerExtension extension : bigVaultServerExtension) {
			extension.afterCommit(transaction, qtime);
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
