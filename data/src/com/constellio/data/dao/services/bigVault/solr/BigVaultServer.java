package com.constellio.data.dao.services.bigVault.solr;

import static com.constellio.data.dao.services.bigVault.solr.SolrUtils.NULL_STRING;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient.RouteException;
import org.apache.solr.client.solrj.impl.HttpSolrClient.RemoteSolrException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.UpdateParams;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException.CouldNotExecuteQuery;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException.OptimisticLocking;
import com.constellio.data.dao.services.bigVault.solr.BigVaultRuntimeException.BadRequest;
import com.constellio.data.dao.services.bigVault.solr.BigVaultRuntimeException.SolrInternalError;
import com.constellio.data.dao.services.bigVault.solr.BigVaultRuntimeException.TryingToRegisterListenerWithExistingId;
import com.constellio.data.dao.services.bigVault.solr.listeners.BigVaultServerAddEditListener;
import com.constellio.data.dao.services.bigVault.solr.listeners.BigVaultServerListener;
import com.constellio.data.dao.services.bigVault.solr.listeners.BigVaultServerQueryListener;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.dao.services.solr.ConstellioSolrInputDocument;
import com.constellio.data.dao.services.solr.DateUtils;
import com.constellio.data.dao.services.solr.SolrServerFactory;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import com.constellio.data.utils.TimeProvider;
import com.google.common.annotations.VisibleForTesting;

public class BigVaultServer implements Cloneable {

	private static final int HTTP_ERROR_500_INTERNAL = 500;
	private static final int HTTP_ERROR_409_CONFLICT = 409;
	private static final int HTTP_ERROR_400_BAD_REQUEST = 400;

	private static final Logger LOGGER = LoggerFactory.getLogger(BigVaultServer.class);
	private final int maxFailAttempt = 10;
	private final int waitedMillisecondsBetweenAttempts = 500;
	private BigVaultLogger bigVaultLogger;
	private DataLayerSystemExtensions extensions;

	private final String name;
	private final SolrServerFactory solrServerFactory;
	private final SolrClient server;

	private final AtomicFileSystem fileSystem;
	private final List<BigVaultServerListener> listeners;
	private final List<BigVaultServerTransaction> postponedTransactions = new ArrayList<>();

	public BigVaultServer(String name, BigVaultLogger bigVaultLogger, SolrServerFactory solrServerFactory
			, DataLayerSystemExtensions extensions) {
		this(name, bigVaultLogger, solrServerFactory, extensions, new ArrayList<BigVaultServerListener>());
	}

	public BigVaultServer(String name, BigVaultLogger bigVaultLogger, SolrServerFactory solrServerFactory,
			DataLayerSystemExtensions extensions, List<BigVaultServerListener> listeners) {
		this.solrServerFactory = solrServerFactory;
		this.server = solrServerFactory.newSolrServer(name);
		this.fileSystem = solrServerFactory.getConfigFileSystem(name);
		this.bigVaultLogger = bigVaultLogger;
		this.name = name;
		this.extensions = extensions;
		this.listeners = listeners;
	}

	public void registerListener(BigVaultServerListener listener) {
		for (BigVaultServerListener existingListener : this.listeners) {
			if (existingListener.getListenerUniqueId().equals(listener.getListenerUniqueId())) {
				throw new TryingToRegisterListenerWithExistingId(listener.getListenerUniqueId());
			}
		}
		this.listeners.add(listener);
	}

	public void unregisterListener(BigVaultServerListener listener) {
		Iterator<BigVaultServerListener> iterator = this.listeners.iterator();
		while (iterator.hasNext()) {
			BigVaultServerListener existingListener = iterator.next();
			if (existingListener.getListenerUniqueId().equals(listener.getListenerUniqueId())) {
				iterator.remove();
				//only one
				return;
			}
		}
	}

	public String getName() {
		return name;
	}

	@VisibleForTesting
	public SolrServerFactory getSolrServerFactory() {
		return solrServerFactory;
	}

	//chargement
	public QueryResponse query(final SolrParams params)
			throws BigVaultException.CouldNotExecuteQuery {
		return query(null, params);
	}

	//chargement
	public QueryResponse query(String queryName, final SolrParams params)
			throws BigVaultException.CouldNotExecuteQuery {
		int currentAttempt = 0;
		long start = new Date().getTime();
		final QueryResponse response = tryQuery(params, currentAttempt);
		final int resultsSize = response.getResults().size();
		long end = new Date().getTime();

		final long qtime = end - start;
		extensions.afterQuery(params, queryName, qtime, response.getResults().size());

		for (BigVaultServerListener listener : this.listeners) {
			if (listener instanceof BigVaultServerQueryListener) {
				((BigVaultServerQueryListener) listener).onQuery(params, response);
			}
		}
		return response;
	}

	public SolrDocument realtimeGet(String id)
			throws BigVaultException.CouldNotExecuteQuery {

		try {
			return server.getById(id);
		} catch (SolrServerException | IOException e) {
			throw new BigVaultException.CouldNotExecuteQuery("realtime get of " + id, e);
		}

	}

	public List<SolrDocument> realtimeGet(List<String> ids)
			throws BigVaultException.CouldNotExecuteQuery {

		try {
			return server.getById(ids);
		} catch (SolrServerException | IOException e) {
			throw new BigVaultException.CouldNotExecuteQuery("realtime get of " + ids, e);
		}

	}

	private QueryResponse tryQuery(SolrParams params, int currentAttempt)
			throws BigVaultException.CouldNotExecuteQuery {

		try {
			return server.query(params);
		} catch (IOException | SolrServerException e) {
			LOGGER.error("Error while querying solr server", e);
			if (e.getCause() instanceof RemoteSolrException) {
				RemoteSolrException remoteSolrException = (RemoteSolrException) e.getCause();
				if (remoteSolrException.code() == HTTP_ERROR_400_BAD_REQUEST) {
					throw new BadRequest(params, e);
				}
			}

			return handleQueryException(params, currentAttempt, e);

		} catch (RemoteSolrException solrServerException) {
			if (solrServerException.code() == HTTP_ERROR_400_BAD_REQUEST) {
				throw new BadRequest(params, solrServerException);
			}

			return handleQueryException(params, currentAttempt, solrServerException);
		}
	}

	private QueryResponse handleQueryException(SolrParams params, int currentAttempt, Exception solrServerException)
			throws BigVaultException.CouldNotExecuteQuery {
		if (currentAttempt < maxFailAttempt) {
			LOGGER.warn("Solr thrown an unexpected exception, retrying the query '{}' in {} milliseconds...",
					SolrUtils.toString(params), waitedMillisecondsBetweenAttempts, solrServerException);
			sleepBeforeRetrying(solrServerException);
			return tryQuery(params, currentAttempt + 1);
		} else {
			throw new BigVaultException.CouldNotExecuteQuery("query", params, solrServerException);
		}
	}

	public SolrDocumentList queryResults(SolrParams params)
			throws BigVaultException.CouldNotExecuteQuery {

		return query(params).getResults();
	}

	public SolrDocument querySingleResult(SolrParams params)
			throws BigVaultException {
		SolrDocumentList results = queryResults(params);
		if (results.isEmpty()) {
			throw new BigVaultException.NoResult(params);
		} else if (results.size() > 1) {
			throw new BigVaultException.NonUniqueResult(params, results);
		} else {
			SolrDocument document = results.get(0);
			if (document == null) {
				throw new BigVaultException.NoResult(params);
			}
			return document;
		}
	}

	public TransactionResponseDTO addAll(BigVaultServerTransaction transaction)
			throws BigVaultException {
		for (BigVaultServerListener listener : this.listeners) {
			if (listener instanceof BigVaultServerAddEditListener) {
				((BigVaultServerAddEditListener) listener).beforeAdd(transaction);
			}
		}
		int currentAttempt = 0;
		long start = new Date().getTime();
		TransactionResponseDTO response = tryAddAll(transaction, currentAttempt);
		long end = new Date().getTime();
		extensions.afterUpdate(transaction, end - start);
		for (BigVaultServerListener listener : this.listeners) {
			if (listener instanceof BigVaultServerAddEditListener) {
				((BigVaultServerAddEditListener) listener).afterAdd(transaction, response);
			}
		}
		return response;
	}

	TransactionResponseDTO tryAddAll(BigVaultServerTransaction transaction, int currentAttempt)
			throws BigVaultException.OptimisticLocking, BigVaultException.CouldNotExecuteQuery {
		if (!transaction.getUpdatedDocuments().isEmpty() || !transaction.getNewDocuments().isEmpty()
				|| !transaction.getDeletedQueries().isEmpty() || !transaction.getDeletedRecords().isEmpty()) {
			try {
				return addAndCommit(transaction);

			} catch (RemoteSolrException | RouteException solrServerException) {
				int code = getExceptionCode(solrServerException);
				return handleRemoteSolrExceptionWhileAddingRecords(transaction, currentAttempt + 1, solrServerException, code);

			} catch (SolrServerException | IOException e) {
				if (e.getCause() != null && e.getCause() instanceof RemoteSolrException) {
					RemoteSolrException remoteSolrException = (RemoteSolrException) e.getCause();
					int code = getExceptionCode(remoteSolrException);
					return handleRemoteSolrExceptionWhileAddingRecords(transaction, currentAttempt + 1, remoteSolrException,
							code);
				}

				StringBuilder stringBuilder = new StringBuilder("Failed to execute this transaction : \n<transaction>");

				for (SolrInputDocument document : transaction.getUpdatedDocuments()) {
					stringBuilder.append(ClientUtils.toXML(document));
					stringBuilder.append("\n");
				}
				for (SolrInputDocument document : transaction.getNewDocuments()) {
					stringBuilder.append(ClientUtils.toXML(document));
					stringBuilder.append("\n");
				}
				stringBuilder.append("\n");
				stringBuilder.append("</transaction>");
				LOGGER.error(stringBuilder.toString());

				throw new BigVaultException.CouldNotExecuteQuery(
						"" + maxFailAttempt + " errors occured while add/updating records",
						e);
			}
		} else {
			return new TransactionResponseDTO(0, new HashMap<String, Long>());
		}
	}

	private int getExceptionCode(Throwable e) {
		if (e instanceof RemoteSolrException) {
			return ((RemoteSolrException) e).code();
		} else if (e instanceof RouteException) {
			return ((RouteException) e).code();
		} else {
			return -1;
		}
	}

	private TransactionResponseDTO handleRemoteSolrExceptionWhileAddingRecords(BigVaultServerTransaction transaction,
			int currentAttempt, Exception exception, int code)
			throws BigVaultException.OptimisticLocking, BigVaultException.CouldNotExecuteQuery {
		if (code == HTTP_ERROR_409_CONFLICT) {
			return handleOptimisticLockingException(exception);

		} else if (code == HTTP_ERROR_400_BAD_REQUEST) {
			throw new BigVaultRuntimeException.BadRequest(transaction, exception);

			//Solrcloud return an error 500 for updates with conflicts
		} else if (code == HTTP_ERROR_500_INTERNAL && isRouteExceptionVersionConflict(exception)) {
			return handleOptimisticLockingException(exception);

		} else if (code == HTTP_ERROR_500_INTERNAL) {
			throw new SolrInternalError(transaction, exception);

		} else {
			LOGGER.warn("Solr thrown an unexpected exception, while handling addAll. Retrying in {} milliseconds...",
					waitedMillisecondsBetweenAttempts, exception);
			sleepBeforeRetrying(exception);
			return retryAddAll(transaction, currentAttempt + 1, exception);
		}
	}

	private boolean isRouteExceptionVersionConflict(Exception exception) {
		//Solrcloud send different exceptions depending on the reason of the conflict
		return exception.getMessage().startsWith("version conflict") || exception.getMessage().startsWith(
				"Document not found for update");
	}

	private TransactionResponseDTO retryAddAll(BigVaultServerTransaction transaction, int currentAttempt, Exception e)
			throws CouldNotExecuteQuery, OptimisticLocking {
		if (currentAttempt < maxFailAttempt) {
			return tryAddAll(transaction, currentAttempt + 1);

		} else {
			throw new BigVaultRuntimeException("" + maxFailAttempt + " errors occured while add/updating records", e);
		}
	}

	private TransactionResponseDTO handleOptimisticLockingException(Exception optimisticLockingException)
			throws BigVaultException.OptimisticLocking {
		try {
			softCommit();
		} catch (IOException | SolrServerException solrServerException) {
			LOGGER.warn("Failed to softCommit records that caused an optimistic locking exception", optimisticLockingException);
			sleepBeforeRetrying(solrServerException);
			throw new BigVaultRuntimeException("" + maxFailAttempt + " errors occured while committing records",
					solrServerException);
		}
		throw new BigVaultException.OptimisticLocking(optimisticLockingException);
	}

	TransactionResponseDTO addAndCommit(BigVaultServerTransaction transaction)
			throws SolrServerException, IOException {

		if (transaction.getRecordsFlushing() == RecordsFlushing.ADD_LATER()) {
			synchronized (postponedTransactions) {
				postponedTransactions.add(transaction);
			}
			return null;
		}

		TransactionResponseDTO response = add(transaction);
		if (transaction.getRecordsFlushing() == RecordsFlushing.NOW) {
			softCommit();
		}
		bigVaultLogger.log(transaction.getNewDocuments(), transaction.getUpdatedDocuments());
		return response;
	}

	public void flush()
			throws IOException, SolrServerException {
		if (!postponedTransactions.isEmpty()) {

			BigVaultUpdateRequest req = new BigVaultUpdateRequest();
			req.setCommitWithin(-1);

			synchronized (postponedTransactions) {

				for (BigVaultServerTransaction postponedTransaction : postponedTransactions) {
					req.add(copyRemovingVersionsFromAtomicUpdate(postponedTransaction.getNewDocuments()));
					req.add(copyRemovingVersionsFromAtomicUpdate(postponedTransaction.getUpdatedDocuments()));
				}
				postponedTransactions.clear();
			}

			req.process(server);

		}
		softCommit();
	}

	public void softCommit()
			throws IOException, SolrServerException {
		long start = new Date().getTime();
		trySoftCommit(0);
		long end = new Date().getTime();
		extensions.afterCommmit(null, end - start);
	}

	TransactionResponseDTO add(BigVaultServerTransaction transaction)
			throws SolrServerException, IOException {

		for (BigVaultServerListener listener : this.listeners) {
			if (listener instanceof BigVaultServerAddEditListener) {
				((BigVaultServerAddEditListener) listener).beforeAdd(transaction);
			}
		}
		int commitWithin = transaction.getRecordsFlushing().getWithinMilliseconds();
		if (transaction.isRequiringLock()) {
			String transactionId = UUIDV1Generator.newRandomId();
			verifyTransactionOptimisticLocking(commitWithin, transactionId, transaction.getUpdatedDocuments());
			transaction.setTransactionId(transactionId);
		}
		TransactionResponseDTO response = processChanges(transaction);
		for (BigVaultServerListener listener : this.listeners) {
			if (listener instanceof BigVaultServerAddEditListener) {
				((BigVaultServerAddEditListener) listener).afterAdd(transaction, response);
			}
		}
		return response;
	}

	void verifyTransactionOptimisticLocking(int commitWithin, String transactionId, List<SolrInputDocument> updatedDocuments)
			throws IOException, SolrServerException {
		try {
			if (!updatedDocuments.isEmpty()) {
				List<SolrInputDocument> optimisticLockingValidations = copyAtomicUpdatesKeepingOnlyIdAndVersion(transactionId,
						updatedDocuments);

				if (!optimisticLockingValidations.isEmpty()) {
					server.add(optimisticLockingValidations, commitWithin);
				}
			}
		} catch (Exception e) {
			deleteLocksOfTransaction(transactionId);
			throw e;
		}

	}

	private List<SolrInputDocument> copyAtomicUpdatesKeepingOnlyIdAndVersion(String transactionId,
			List<SolrInputDocument> updatedDocuments) {
		List<SolrInputDocument> optimisticLockingValidationDocuments = new ArrayList<>();
		for (SolrInputDocument updatedDocument : updatedDocuments) {
			SolrInputDocument solrInputDocument = new ConstellioSolrInputDocument();
			Object version = updatedDocument.getFieldValue("_version_");
			if (version != null) {
				solrInputDocument.setField("id", updatedDocument.getFieldValue("id"));
				solrInputDocument.setField("_version_", version);
				solrInputDocument.setField("sys_s", newAtomicSet(""));
				optimisticLockingValidationDocuments.add(solrInputDocument);

				boolean onlyMarkingForReindexing =
						updatedDocument.getFieldValue("markedForReindexing_s") != null &&
								updatedDocument.getFieldNames().size() == 3;

				if (updatedDocument.getFieldValue("type_s") == null && !onlyMarkingForReindexing) {
					String lockId = "lock__" + updatedDocument.getFieldValue("id");
					SolrInputDocument lockDocument = new SolrInputDocument();
					lockDocument.setField("id", lockId);
					lockDocument.setField("transaction_s", transactionId);
					lockDocument.setField("_version_", -1);
					lockDocument.setField("lockCreation_dt", TimeProvider.getLocalDateTime().toDate());
					optimisticLockingValidationDocuments.add(lockDocument);
				}
			}
		}

		return optimisticLockingValidationDocuments;
	}

	private List<SolrInputDocument> copyRemovingVersionsFromAtomicUpdate(List<SolrInputDocument> updatedDocuments) {
		List<SolrInputDocument> withoutVersions = new ArrayList<>();
		for (SolrInputDocument document : updatedDocuments) {

			SolrInputDocument solrInputDocument = new ConstellioSolrInputDocument();
			solrInputDocument.putAll(document);
			solrInputDocument.setField("sys_s", newAtomicSet(NULL_STRING));
			solrInputDocument.removeField("_version_");
			withoutVersions.add(solrInputDocument);
		}
		return withoutVersions;
	}

	TransactionResponseDTO processChanges(BigVaultServerTransaction transaction)
			throws SolrServerException, IOException {

		int commitWithin = transaction.getRecordsFlushing().getWithinMilliseconds();

		BigVaultUpdateRequest req = new BigVaultUpdateRequest();
		List<String> deletedQueriesAndLocks = new ArrayList<>(transaction.getDeletedQueries());
		if (transaction.isRequiringLock()) {
			deletedQueriesAndLocks.add("transaction_s:" + transaction.getTransactionId());
			List<SolrInputDocument> docsWithoutVersions = copyRemovingVersionsFromAtomicUpdate(transaction.getUpdatedDocuments());
			req.add(docsWithoutVersions);

		} else {
			req.add(transaction.getUpdatedDocuments());
		}
		req.setCommitWithin(commitWithin);
		req.setParam(UpdateParams.VERSIONS, "true");
		req.add(transaction.getNewDocuments());
		req.deleteById(transaction.getDeletedRecords());
		req.setDeleteQuery(deletedQueriesAndLocks);

		//Only added when a lock is required, because optimistic locking is handled before execution
		if (transaction.isRequiringLock() && !postponedTransactions.isEmpty()) {
			synchronized (postponedTransactions) {

				for (BigVaultServerTransaction postponedTransaction : postponedTransactions) {
					req.add(copyRemovingVersionsFromAtomicUpdate(postponedTransaction.getNewDocuments()));
					req.add(copyRemovingVersionsFromAtomicUpdate(postponedTransaction.getUpdatedDocuments()));
				}
				postponedTransactions.clear();
			}
		}

		UpdateResponse updateResponse = req.process(server);

		return SolrUtils.createTransactionResponseDTO(updateResponse);
	}

	private List<SolrInputDocument> withoutIndexes(List<SolrInputDocument> newDocuments) {
		List<SolrInputDocument> withoutIndexes = new ArrayList<>();
		for (SolrInputDocument doc : newDocuments) {
			if (!((String) doc.getFieldValue("id")).startsWith("idx_")) {
				withoutIndexes.add(doc);
			}
		}
		return withoutIndexes;
	}

	void deleteLocksOfTransaction(String transactionId)
			throws SolrServerException, IOException {
		BigVaultUpdateRequest req = new BigVaultUpdateRequest();
		req.setDeleteQuery(Arrays.asList("transaction_s:" + transactionId));
		req.process(server);
	}

	private void trySoftCommit(int currentAttempt)
			throws IOException, SolrServerException {
		try {
			server.commit(true, true, true);
		} catch (SolrServerException | IOException | RemoteSolrException solrServerException) {
			if (currentAttempt < maxFailAttempt) {
				LOGGER.warn("Solr thrown an unexpected exception, retrying the softCommit... in {} milliseconds",
						waitedMillisecondsBetweenAttempts, solrServerException);
				sleepBeforeRetrying(solrServerException);
				trySoftCommit(currentAttempt);
			} else {
				throw solrServerException;
			}
		}
	}

	List<String> toDeletedQueries(List<SolrParams> params) {
		List<String> queries = new ArrayList<>();

		for (SolrParams param : params) {
			queries.add(toDeleteQueries(param));
		}

		return queries;
	}

	String toDeleteQueries(SolrParams params) {

		StringBuffer query = new StringBuffer();
		query.append("((");
		query.append(params.get("q"));
		query.append(")");

		if (params.getParams("fq") != null) {
			for (String fq : params.getParams("fq")) {
				query.append(" AND (");
				query.append(fq);
				query.append(")");
			}
		}

		query.append(")");
		return query.toString();
	}

	public SolrClient getNestedSolrServer() {
		return server;
	}

	public AtomicFileSystem getSolrFileSystem() {
		return fileSystem;
	}

	private void sleepBeforeRetrying(Exception e) {

		//		if (!e.getMessage().contains("Random injected fault")) {
		//			try {
		//				Thread.sleep(waitedMillisecondsBetweenAttempts);
		//			} catch (InterruptedException e2) {
		//				throw new RuntimeException(e2);
		//			}
		//		}
	}

	private Map<String, Object> newAtomicSet(Object value) {
		Map<String, Object> map = new HashMap<>();
		map.put("set", value);
		return map;
	}

	public void removeLockWithAgeGreaterThan(int ageInSeconds) {
		LocalDateTime localDateTime = DateUtils.correctDate(TimeProvider.getLocalDateTime()).minusSeconds(ageInSeconds - 1);
		String query = "id:lock__* AND lockCreation_dt:{* TO " + localDateTime + "Z}";
		try {
			server.deleteByQuery(query);
		} catch (SolrServerException | IOException e) {
			throw new SolrInternalError(e);
		}
	}

	public long countDocuments() {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "*:*");
		try {
			return query(params).getResults().getNumFound();
		} catch (CouldNotExecuteQuery couldNotExecuteQuery) {
			throw new RuntimeException(couldNotExecuteQuery);
		}
	}

	public void reload() {
		solrServerFactory.reloadSolrServer(name);
	}

	@Override
	public BigVaultServer clone() {
		return new BigVaultServer(name, bigVaultLogger, solrServerFactory, extensions, this.listeners);
	}

	public void disableLogger() {
		bigVaultLogger = BigVaultLogger.disabled();
	}

	public void setExtensions(DataLayerSystemExtensions extensions) {
		this.extensions = extensions;
	}

	public void expungeDeletes() {
		try {

			UpdateRequest updateRequest = new UpdateRequest();
			updateRequest.setAction(UpdateRequest.ACTION.COMMIT, true, true, true);
			updateRequest.process(server);
			updateRequest.setParam("expungeDeletes", "true");
		} catch (SolrServerException | IOException | RemoteSolrException e) {
			//TODO
			throw new RuntimeException(e);
		} catch (OutOfMemoryError e) {
			try {
				server.close();
			} catch (IOException ioe) {
				LOGGER.error("Error while closing server", ioe);
			}
			throw e;
		}
	}

	public void unregisterAllListeners() {
		this.listeners.clear();
	}
}
