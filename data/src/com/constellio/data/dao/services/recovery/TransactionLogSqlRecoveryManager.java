package com.constellio.data.dao.services.recovery;

import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException.CouldNotExecuteQuery;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogManager;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.LazyIterator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class TransactionLogSqlRecoveryManager implements TransactionLogRecovery {

	private final static Logger LOGGER = LoggerFactory.getLogger(TransactionLogXmlRecoveryManager.class);
	//private static final String RECOVERY_WORK_DIR = TransactionLogXmlRecoveryManager.class.getName() + "recoveryWorkDir";

	final DataLayerFactory dataLayerFactory;
	RecoveryTransactionReadWriteServices readWriteServices;
	private boolean inRollbackMode;
	Set<String> loadedRecordsIds, fullyLoadedRecordsIds, newRecordsIds, contentRecords;

	public TransactionLogSqlRecoveryManager(DataLayerFactory dataLayerFactory) {
		this.dataLayerFactory = dataLayerFactory;
	}

	@Override
	public void startRollbackMode() {
		if (!inRollbackMode) {
			LOGGER.info("Rollback mode started");
			realStartRollback();
		}
	}

	@Override
	public void realStartRollback() {
		loadedRecordsIds = new HashSet<>();
		fullyLoadedRecordsIds = new HashSet<>();
		newRecordsIds = new HashSet<>();
		inRollbackMode = true;
		SecondTransactionLogManager transactionLogManager = dataLayerFactory
				.getSecondTransactionLogManager();
		transactionLogManager.setAutomaticRegroupAndMoveEnabled(false);
		transactionLogManager.regroupAndMove();
		dataLayerFactory.getRecordsVaultServer().registerListener(this);
	}

	@Override
	public void stopRollbackMode() {
		if (inRollbackMode) {
			LOGGER.info("Rollback mode stopped");
			realStopRollback();
		}
	}

	@Override
	public void realStopRollback() {
		dataLayerFactory.getRecordsVaultServer().unregisterListener(this);
		inRollbackMode = false;
		contentRecords.clear();
		SecondTransactionLogManager transactionLogManager = dataLayerFactory
				.getSecondTransactionLogManager();
		transactionLogManager.regroupAndMove();
		transactionLogManager.setAutomaticRegroupAndMoveEnabled(true);
	}

	@Override
	public boolean isInRollbackMode() {
		return inRollbackMode;
	}

	@Override
	public void disableRollbackModeDuringSolrRestore() {
		stopRollbackMode();
	}

	@Override
	public void rollback(Throwable t) {
		if (inRollbackMode) {
			LOGGER.info("Rolling back");
			realRollback(t);

		} else {
			LOGGER.info("Not in rolling back mode");
		}
	}

	@Override
	public void realRollback(Throwable t) {
		dataLayerFactory.getRecordsVaultServer().unregisterListener(this);
		recover();
		LOGGER.info("Rollback - recovered solr");
		contentRecords.clear();
		LOGGER.info("deleteRecoveryFile() done");
		SecondTransactionLogManager transactionLogManager = dataLayerFactory
				.getSecondTransactionLogManager();
		transactionLogManager.deleteUnregroupedLog();
		LOGGER.info("transactionLogManager.deleteUnregroupedLog() done");
		transactionLogManager.setAutomaticRegroupAndMoveEnabled(true);
		LOGGER.info("transactionLogManager.setAutomaticRegroupAndMoveEnabled(true) done");
		inRollbackMode = false;
	}

	private void recover() {
		BigVaultServer bigVaultServer = dataLayerFactory.getRecordsVaultServer();
		SolrClient server = bigVaultServer.getNestedSolrServer();

		removeNewRecords(server);

		try {
			bigVaultServer.softCommit();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		}
	}

	private void restore(SolrClient server, final Set<String> alteredRecordsIds) {
		if (alteredRecordsIds == null || alteredRecordsIds.isEmpty()) {
			return;
		}

		final Iterator<BigVaultServerTransaction> transactionsIterator = this.readWriteServices
				.newOperationsIterator(null);

		Iterator<List<SolrInputDocument>> docsToRecoverIterator = new LazyIterator<List<SolrInputDocument>>() {
			@Override
			protected List<SolrInputDocument> getNextOrNull() {
				if (transactionsIterator.hasNext()) {
					List<SolrInputDocument> currentAlteredDocuments = new ArrayList<>();
					BigVaultServerTransaction currentTransaction = transactionsIterator.next();
					for (SolrInputDocument newDocument : currentTransaction.getNewDocuments()) {
						String currentDocumentId = (String) newDocument.getFieldValue("id");
						if (alteredRecordsIds.contains(currentDocumentId)) {
							currentAlteredDocuments.add(newDocument);
						}
					}
					for (SolrInputDocument document : currentTransaction.getUpdatedDocuments()) {
						String currentDocumentId = (String) document.getFieldValue("id");
						if (alteredRecordsIds.contains(currentDocumentId)) {
							currentAlteredDocuments.add(document);
						}
					}
					return currentAlteredDocuments;

				} else {
					return null;
				}
			}
		};

		int batchSize = 1000;
		Iterator<List<SolrInputDocument>> iterator = BatchBuilderIterator.forListIterator(docsToRecoverIterator, batchSize);

		while (iterator.hasNext()) {
			try {
				server.add(iterator.next());
			} catch (SolrServerException | HttpSolrClient.RemoteSolrException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

	private void removeNewRecords(SolrClient server) {
		if (this.newRecordsIds.isEmpty()) {
			return;
		}

		int batchSize = 1000;
		Iterator<List<String>> iterator = new BatchBuilderIterator(this.newRecordsIds.iterator(), batchSize);
		while (iterator.hasNext()) {

			try {
				server.deleteById(iterator.next());
			} catch (SolrServerException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

	@Override
	public void afterAdd(BigVaultServerTransaction transaction, TransactionResponseDTO responseDTO) {
		//Nothing to do even if transaction did not succeed
	}

	@Override
	public void beforeAdd(BigVaultServerTransaction transaction) {
		if (transaction.getDeletedQueries() != null && !transaction.getDeletedQueries().isEmpty()) {
			if (!transaction.isInTestRollbackMode()) {
				throw new ImpossibleRuntimeException("Delete by query not supported in recovery mode");
			}
		}
		handleAddUpdateFullDocuments(transaction.getNewDocuments());
		handleDeletedDocuments(transaction.getDeletedRecords());
	}

	private boolean isTestMode() {
		return dataLayerFactory.getDataLayerConfiguration().isInRollbackTestMode();
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
		if (!this.fullyLoadedRecordsIds.containsAll(recordsIds)) {
			throw new RuntimeException("Records not loaded after their load request : " +
									   StringUtils.join(CollectionUtils.subtract(recordsIds, this.fullyLoadedRecordsIds), ", "));
		}
	}

	private void provokeRecordsLoad(Set<String> recordsIds) {
		//do not reload

		for (String id : recordsIds) {
			Set<String> recordsToLoadIds = new HashSet<>(recordsIds);
			recordsToLoadIds.removeAll(this.fullyLoadedRecordsIds);
			if (recordsToLoadIds.isEmpty()) {
				return;
			}
			//query solr to load non loaded
			ModifiableSolrParams solrParams = new ModifiableSolrParams();
			//field:(value1 OR value2 OR value3)
			solrParams.set("rows", "999999999");
			solrParams.set("q", "id:" + id);
			try {
				dataLayerFactory.getRecordsVaultServer().query(solrParams);
			} catch (CouldNotExecuteQuery e) {
				throw new RuntimeException(e);
			}
		}
	}


	void handleAddUpdateFullDocuments(List<SolrInputDocument> addUpdateFullDocuments) {
		if (addUpdateFullDocuments == null || addUpdateFullDocuments.isEmpty()) {
			return;
		}
		Set<String> possiblyNewDocumentsIds = new HashSet<>();
		Set<String> addUpdateFullDocumentsIds = new HashSet<>();
		for (SolrInputDocument document : addUpdateFullDocuments) {
			String id = (String) document.getFieldValue("id");
			addUpdateFullDocumentsIds.add(id);
			if (!this.loadedRecordsIds.contains(id)) {
				possiblyNewDocumentsIds.add(id);
			}
		}
		Collection<String> updatedFullDocuments;
		if (!possiblyNewDocumentsIds.isEmpty()) {
			Set<String> newDocuments = getOnlyNewDocuments(possiblyNewDocumentsIds);
			this.newRecordsIds.addAll(newDocuments);
			updatedFullDocuments = CollectionUtils.removeAll(addUpdateFullDocumentsIds, newDocuments);
		} else {
			updatedFullDocuments = addUpdateFullDocumentsIds;
		}
	}

	//TODO test me
	private List<SolrInputDocument> getDocumentsHavingIds(List<SolrInputDocument> solrInputDocuments,
														  final Collection<String> ids) {
		List<SolrInputDocument> returnList = new ArrayList<>();
		for (SolrInputDocument document : solrInputDocuments) {
			String id = (String) document.getFieldValue("id");
			if (ids.contains(id)) {
				returnList.add(document);
			}
		}
		return returnList;
	}

	//TODO test me
	Set<String> getOnlyNewDocuments(Set<String> possiblyNewDocumentsIds) {

		BigVaultServer bigVaultServer = dataLayerFactory.getRecordsVaultServer();
		SolrClient server = bigVaultServer.getNestedSolrServer();

		//field:(value1 OR value2 OR value3)

		Set<String> newDocuments = new HashSet<>();

		for (String id : possiblyNewDocumentsIds) {
			ModifiableSolrParams solrParams = new ModifiableSolrParams();
			solrParams.set("q", "id:" + id);
			solrParams.set("fl", "id");

			QueryResponse response = null;
			try {
				response = server.query(solrParams);
			} catch (IOException | SolrServerException e) {
				throw new RuntimeException(e);
			}
			SolrDocumentList result = response.getResults();
			if (result.getNumFound() == 0) {
				newDocuments.add(id);
			}
		}

		return newDocuments;
	}

	private Set<String> getIdsNotInResult(Set<String> possiblyNewDocumentsIds, SolrDocumentList result) {
		Set<String> returnSet = new HashSet<>();
		for (SolrDocument document : result) {
			String id = (String) document.get("id");
			if (!possiblyNewDocumentsIds.contains(id)) {
				returnSet.add(id);
			}
		}
		return returnSet;
	}

	@Override
	public void onQuery(SolrParams params, QueryResponse response) {
		boolean fullSearch = isFullSearch(params);
		SolrDocumentList results = response.getResults();
		List<Object> documentsToSave = new ArrayList<>();
		List<String> loadedDocuments = new ArrayList<>();
		for (SolrDocument document : results) {
			String currentId = (String) document.get("id");
			if (!this.fullyLoadedRecordsIds.contains(currentId)) {
				documentsToSave.add(document);
				loadedDocuments.add(currentId);
			}
		}
		if (fullSearch) {
			appendLoadedRecordsFile(documentsToSave);
		}
		this.loadedRecordsIds.addAll(loadedDocuments);
	}

	//TODO test me
	boolean isFullSearch(SolrParams params) {
		return StringUtils.isBlank(params.get("fl"));
	}

	private void appendLoadedRecordsFile(List<Object> documentsToSave) {
		List<Object> notAlreadySavedDocuments = new ArrayList<>();
		List<String> notAlreadyLoadedDocumentsIds = new ArrayList<>();
		for (Object document : documentsToSave) {
			String id = getDocumentId(document);
			if (!this.fullyLoadedRecordsIds.contains(id)) {
				notAlreadySavedDocuments.add(document);
				notAlreadyLoadedDocumentsIds.add(id);
			}
		}
		if (notAlreadySavedDocuments.isEmpty()) {
			return;
		}
		this.fullyLoadedRecordsIds.addAll(notAlreadyLoadedDocumentsIds);
		String transaction = this.readWriteServices.toLogEntry(notAlreadySavedDocuments);

	}

	private String getDocumentId(Object document) {
		if (document instanceof SolrDocument) {
			return (String) ((SolrDocument) document).getFieldValue("id");
		} else if (document instanceof SolrInputDocument) {
			return (String) ((SolrInputDocument) document).getFieldValue("id");
		} else {
			throw new ImpossibleRuntimeException(
					"Expecting solr document or solr input document : " + document.getClass().getName());
		}
	}

	@Override
	public String getListenerUniqueId() {
		return "recoveryListener";
	}

	public void close() {
		stopRollbackMode();
		contentRecords.clear();
	}

}
