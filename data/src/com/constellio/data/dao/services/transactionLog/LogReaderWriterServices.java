package com.constellio.data.dao.services.transactionLog;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import java.util.Iterator;

public interface LogReaderWriterServices<Object> {

	Iterator<BigVaultServerTransaction> newOperationsIterator(Object tLog) ;

	BigVaultServerTransaction newOperation(Object log);

	String toLogEntry(BigVaultServerTransaction transaction);

	String toSetSequenceLogEntry(String sequenceId, long value);

	String toNextSequenceLogEntry(String sequenceId);
}
