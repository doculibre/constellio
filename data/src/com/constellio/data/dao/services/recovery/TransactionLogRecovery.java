package com.constellio.data.dao.services.recovery;

import com.constellio.data.dao.services.bigVault.solr.listeners.BigVaultServerAddEditListener;
import com.constellio.data.dao.services.bigVault.solr.listeners.BigVaultServerQueryListener;

public interface TransactionLogRecovery extends RecoveryService, BigVaultServerAddEditListener,
		BigVaultServerQueryListener {

}
