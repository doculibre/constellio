package com.constellio.data.dao.services.transactionLog;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;

public interface SecondTransactionLogManager extends StatefulService {

	void prepare(String transactionId, BigVaultServerTransaction transaction);

	void flush(String transactionId);

	void cancel(String transactionId);

	String regroupAndMoveInVault();

	void destroyAndRebuildSolrCollection();

	void moveTLOGToBackup();

	void deleteLastTLOGBackup();

	void setAutomaticLog(boolean automaticMode);
}