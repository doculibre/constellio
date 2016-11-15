package com.constellio.data.dao.services.transactionLog;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_NotAllLogsWereDeletedCorrectlyException;

public interface SecondTransactionLogManager extends StatefulService {

	void prepare(String transactionId, BigVaultServerTransaction transaction);

	void flush(String transactionId);

	void cancel(String transactionId);

	void setSequence(String sequenceId, long value);

	void nextSequence(String sequenceId);

	String regroupAndMoveInVault();

	void destroyAndRebuildSolrCollection();

	void moveTLOGToBackup();

	void deleteLastTLOGBackup();

	void setAutomaticRegroupAndMoveInVaultEnabled(boolean enabled);

	void deleteUnregroupedLog()
			throws SecondTransactionLogRuntimeException_NotAllLogsWereDeletedCorrectlyException;

}