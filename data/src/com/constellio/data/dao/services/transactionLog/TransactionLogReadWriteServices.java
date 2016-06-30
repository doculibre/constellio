package com.constellio.data.dao.services.transactionLog;

import java.io.BufferedReader;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.transactionLog.reader1.ReaderLinesIteratorV1;
import com.constellio.data.dao.services.transactionLog.reader1.ReaderTransactionLinesIteratorV1;
import com.constellio.data.dao.services.transactionLog.reader1.ReaderTransactionsIteratorV1;
import com.constellio.data.dao.services.transactionLog.writer1.TransactionWriterV1;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.data.io.services.facades.IOServices;

public class TransactionLogReadWriteServices {

	static final String READ_LOG_FOR_REPLAY = XMLSecondTransactionLogManager.class.getSimpleName() + "_readLogForReplay";

	private DataLayerSystemExtensions extensions;
	private IOServices ioServices;
	private DataLayerConfiguration configuration;

	public TransactionLogReadWriteServices(IOServices ioServices, DataLayerConfiguration configuration,
			DataLayerSystemExtensions extensions) {
		this.ioServices = ioServices;
		this.configuration = configuration;
		this.extensions = extensions;
	}

	public Iterator<BigVaultServerTransaction> newOperationsIterator(File tLog) {
		final BufferedReader tLogBufferedReader = ioServices.newBufferedFileReader(tLog, READ_LOG_FOR_REPLAY);
		Iterator<String> linesIterator = new ReaderLinesIteratorV1(ioServices, tLogBufferedReader);
		final Iterator<List<String>> transactionLinesIterator = new ReaderTransactionLinesIteratorV1(linesIterator);
		return new ReaderTransactionsIteratorV1(tLog.getName(), transactionLinesIterator, configuration);
	}

	public String toLogEntry(BigVaultServerTransaction transaction) {
		return new TransactionWriterV1(configuration.isWriteZZRecords(), extensions).toLogEntry(transaction);
	}
}
