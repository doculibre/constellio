package com.constellio.data.dao.services.transactionLog;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.transactionLog.sql.TransactionLogContent;
import com.constellio.data.dao.services.transactionLog.writer1.TransactionJsonMapperObjectWriterV1;
import com.constellio.data.extensions.DataLayerSystemExtensions;

public class TransactionLogSqlReadWriteServices {

	static final String READ_LOG_FOR_REPLAY = XMLSecondTransactionLogManager.class.getSimpleName() + "_readLogForReplay";

	private DataLayerConfiguration configuration;

	private DataLayerSystemExtensions extensions;

	public TransactionLogSqlReadWriteServices(DataLayerConfiguration configuration,
		DataLayerSystemExtensions extensions){
			this.configuration = configuration;
			this.extensions = extensions;
		}

	public String toLogEntry(BigVaultServerTransaction transaction) {
		return new TransactionJsonMapperObjectWriterV1(configuration.isWriteZZRecords(),extensions).toLogEntry(transaction);
	}

}
