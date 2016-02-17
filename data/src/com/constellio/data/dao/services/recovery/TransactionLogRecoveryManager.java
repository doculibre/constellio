package com.constellio.data.dao.services.recovery;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException.CouldNotExecuteQuery;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.bigVault.solr.listeners.BigVaultServerAddEditListener;
import com.constellio.data.dao.services.bigVault.solr.listeners.BigVaultServerQueryListener;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.recovery.transactionWriter.RecoveryTransactionWriter;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogManager;
import com.constellio.data.io.services.facades.IOServices;

public class TransactionLogRecoveryManager implements RecoveryService, BigVaultServerAddEditListener,
													  BigVaultServerQueryListener {
	private final static Logger LOGGER = LoggerFactory.getLogger(TransactionLogRecoveryManager.class);
	private static final String RECOVERY_WORK_DIR = TransactionLogRecoveryManager.class.getName() + "recoveryWorkDir";

	final DataLayerFactory dataLayerFactory;
	File recoveryWorkDir;
	RecoveryTransactionWriter transactionReaderWriter;
	private final IOServices ioServices;
	private boolean inRollbackMode;
	Set<String> loadedRecordsIds, newRecordsIds, deletedRecordsIds, updatedRecordsIds;

	public TransactionLogRecoveryManager(DataLayerFactory dataLayerFactory) {
		this.dataLayerFactory = dataLayerFactory;
		ioServices = this.dataLayerFactory.getIOServicesFactory().newIOServices();
	}

	@Override
	public void startRollbackMode() {
		if (!inRollbackMode) {
			realStartRollback();
		}
	}

	void realStartRollback() {
		loadedRecordsIds = new HashSet<>();
		newRecordsIds = new HashSet<>();
		deletedRecordsIds = new HashSet<>();
		updatedRecordsIds = new HashSet<>();
		createRecoveryFile();
		inRollbackMode = true;
		SecondTransactionLogManager transactionLogManager = dataLayerFactory
				.getSecondTransactionLogManager();
		transactionLogManager.regroupAndMoveInVault();
		transactionLogManager.setAutomaticLog(false);
		dataLayerFactory.getRecordsVaultServer().registerListener(this);
	}

	private void createRecoveryFile() {
		recoveryWorkDir = ioServices.newTemporaryFolder(RECOVERY_WORK_DIR);
		transactionReaderWriter = new RecoveryTransactionWriter(new File(recoveryWorkDir, "recordsBefore"));
	}

	@Override
	public void stopRollbackMode() {
		if (inRollbackMode) {
			realStopRollback();
		}
	}

	void realStopRollback() {
		dataLayerFactory.getRecordsVaultServer().unregisterListener(this);
		deleteRecoveryFile();
		inRollbackMode = false;
		SecondTransactionLogManager transactionLogManager = dataLayerFactory
				.getSecondTransactionLogManager();
		transactionLogManager.regroupAndMoveInVault();
		transactionLogManager.setAutomaticLog(true);
	}

	private void deleteRecoveryFile() {
		ioServices.deleteQuietly(recoveryWorkDir);
	}

	@Override
	public boolean isInRollbackMode() {
		return inRollbackMode;
	}

	public void disableRollbackModeDuringSolrRestore() {
		this.inRollbackMode = false;
	}

	@Override
	public void rollback(Throwable t) {
		if (inRollbackMode) {
			realRollback(t);
		}
	}

	void realRollback(Throwable t) {
		dataLayerFactory.getRecordsVaultServer().unregisterListener(this);
		recover();
		deleteRecoveryFile();
		SecondTransactionLogManager transactionLogManager = dataLayerFactory
				.getSecondTransactionLogManager();
		transactionLogManager.deleteUnregroupedLog();
		transactionLogManager.setAutomaticLog(true);
		inRollbackMode = false;
	}

	private void recover() {
		SolrClient server = dataLayerFactory.getContentsVaultServer().getNestedSolrServer();
		this.deletedRecordsIds.removeAll(this.newRecordsIds);
		this.updatedRecordsIds.removeAll(this.newRecordsIds);
		removeNewRecords(server);
		restore(server, this.deletedRecordsIds);
		restore(server, this.updatedRecordsIds);
	}

	private void restore(SolrClient server, Set<String> alteredRecordsIds) {
		//TODO by batch
		if(alteredRecordsIds== null ||alteredRecordsIds.isEmpty()){
			return;
		}
		List<SolrInputDocument> documents = new ArrayList<>();
		for(String recordId : alteredRecordsIds){
			documents.add(this.transactionReaderWriter.getDocument(recordId));
		}
		try {
			server.add(documents);
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void removeNewRecords(SolrClient server) {
		//TODO by batch
		try {
			server.deleteById(new ArrayList<>(this.newRecordsIds));
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void afterAdd(BigVaultServerTransaction transaction, TransactionResponseDTO responseDTO) {
		//Nothing to do even if transaction did not succeed
	}

	@Override
	public void beforeAdd(BigVaultServerTransaction transaction) {
		handleNewDocuments(transaction.getNewDocuments());
		handleUpdatedDocuments(transaction.getUpdatedDocuments());
		handleDeletedDocuments(transaction.getDeletedRecords());
		if (!transaction.getDeletedQueries().isEmpty()) {
			throw new RuntimeException("Delete by query not supported in recovery mode");
		}
	}

	void handleDeletedDocuments(List<String> deletedRecords) {
		if (deletedRecords == null || deletedRecords.isEmpty()) {
			return;
		}
		Set<String> deletedRecordsIds = new HashSet<>();
		for (String deletedRecordId : deletedRecords) {
			deletedRecordsIds.add(deletedRecordId);
		}
		ensureRecordLoaded(deletedRecordsIds);
	}

	void ensureRecordLoaded(Set<String> recordsIds) {
		provokeRecordsLoad(recordsIds);
		if (!this.loadedRecordsIds.containsAll(recordsIds)) {
			throw new RuntimeException("Records not loaded after their load request : " +
					StringUtils.join(CollectionUtils.subtract(this.loadedRecordsIds, recordsIds), ", "));
		}
	}

	private void provokeRecordsLoad(Set<String> recordsIds) {
		//do not reload
		Set<String> recordsToLoadIds = new HashSet<>(recordsIds);
		recordsToLoadIds.removeAll(this.loadedRecordsIds);
		//query solr to load non loaded
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		//field:(value1 OR value2 OR value3)
		solrParams.set("id", "(" + StringUtils.join(recordsToLoadIds, " OR ") + ")");
		try {
			dataLayerFactory.getRecordsVaultServer().query(solrParams);
		} catch (CouldNotExecuteQuery couldNotExecuteQuery) {
			//TODO replace with appropriate exception
			throw new RuntimeException(couldNotExecuteQuery);
		}
	}

	void handleUpdatedDocuments(List<SolrInputDocument> updatedDocuments) {
		if (updatedDocuments == null || updatedDocuments.isEmpty()) {
			return;
		}
		Set<String> updatedDocumentsIds = new HashSet<>();
		for (SolrInputDocument document : updatedDocuments) {
			String id = (String) document.getFieldValue("id");
			updatedDocumentsIds.add(id);

		}
		ensureRecordLoaded(updatedDocumentsIds);
		this.updatedRecordsIds.addAll(updatedDocumentsIds);
	}

	void handleNewDocuments(List<SolrInputDocument> newDocuments) {
		if (newDocuments == null || newDocuments.isEmpty()) {
			return;
		}
		for (SolrInputDocument document : newDocuments) {
			String id = (String) document.getFieldValue("id");
			this.newRecordsIds.add(id);
		}
	}

	@Override
	public void onQuery(QueryResponse response) {
		SolrDocumentList results = response.getResults();
		List<SolrDocument> documentsToSave = new ArrayList<>();
		List<String> loadedDocuments = new ArrayList<>();
		for(SolrDocument document : results){
			String currentId = (String) document.get("id");
			if(!this.loadedRecordsIds.contains(currentId)){
				documentsToSave.add(document);
				loadedDocuments.add(currentId);
			}
		}
		this.transactionReaderWriter.addAll(documentsToSave);
		this.loadedRecordsIds.addAll(loadedDocuments);
	}

	@Override
	public String getListenerUniqueId() {
		return "recoveryListener";
	}

	public void close() {
		deleteRecoveryFile();
	}
}
