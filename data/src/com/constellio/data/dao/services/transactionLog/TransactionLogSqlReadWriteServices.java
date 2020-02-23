package com.constellio.data.dao.services.transactionLog;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.dto.sql.RecordTransactionSqlDTO;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.transactionLog.reader1.TransactionJsonMapperObjectReaderV1;
import com.constellio.data.dao.services.transactionLog.writer1.TransactionJsonMapperObjectWriterV1;
import com.constellio.data.extensions.DataLayerSystemExtensions;

import java.util.Iterator;

public class TransactionLogSqlReadWriteServices implements LogReaderWriterServices<RecordTransactionSqlDTO> {

	static final String READ_LOG_FOR_REPLAY = XMLSecondTransactionLogManager.class.getSimpleName() + "_readLogForReplay";

	private DataLayerConfiguration configuration;

	private DataLayerSystemExtensions extensions;

	public TransactionLogSqlReadWriteServices(DataLayerConfiguration configuration,
											  DataLayerSystemExtensions extensions) {
		this.configuration = configuration;
		this.extensions = extensions;
	}

	@Override
	public Iterator<BigVaultServerTransaction> newOperationsIterator(RecordTransactionSqlDTO log) {

		return null;
	}

	@Override
	public BigVaultServerTransaction newOperation(RecordTransactionSqlDTO log) {
		TransactionJsonMapperObjectReaderV1 writer = new TransactionJsonMapperObjectReaderV1(this.configuration);
		return writer.reBuildBigVaultServerTransactionArray(log.getContent());
	}

	@Override
	public String toLogEntry(BigVaultServerTransaction transaction) {
		return new TransactionJsonMapperObjectWriterV1(configuration.isWriteZZRecords(), extensions).toLogEntry(transaction);
	}

	@Override
	public String toSetSequenceLogEntry(String sequenceId, long value) {
		return null;
	}

	@Override
	public String toNextSequenceLogEntry(String sequenceId) {
		return null;
	}

}
