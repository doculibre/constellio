package com.constellio.data.dao.services.bigVault.solr;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.conf.SolrServerType;
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
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient.RouteException;
import org.apache.solr.client.solrj.impl.HttpSolrClient.RemoteSolrException;
import org.apache.solr.client.solrj.io.SolrClientCache;
import org.apache.solr.client.solrj.io.stream.StreamContext;
import org.apache.solr.client.solrj.io.stream.TupleStream;
import org.apache.solr.client.solrj.request.GenericSolrRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.MoreLikeThisParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.UpdateParams;
import org.apache.solr.common.util.NamedList;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import static com.constellio.data.dao.services.bigVault.solr.SolrUtils.NULL_STRING;

public class BigVaultServer implements Cloneable {

	private static final int HTTP_ERROR_500_INTERNAL = 500;
	private static final int HTTP_ERROR_409_CONFLICT = 409;
	private static final int HTTP_ERROR_400_BAD_REQUEST = 400;
	private static final int HTTP_ERROR_404_NOT_FOUND = 404;

	private static final Logger LOGGER = LoggerFactory.getLogger(BigVaultServer.class);

	private static final int NORMAL_RESILIENCE_MODE_ATTEMPTS = 10;

	//First 40 attempts should take about 30 minutes, the next 24 attempts will take about 120 min (5 minutes each)
	private static final int HIGH_RESILIENCE_MODE_ATTEMPTS = 40 + 24;

	private static int MAX_FAIL_ATTEMPT = NORMAL_RESILIENCE_MODE_ATTEMPTS;

	private BigVaultLogger bigVaultLogger;
	private DataLayerSystemExtensions extensions;
	private DataLayerConfiguration configurations;

	private final String name;
	private final SolrServerFactory solrServerFactory;
	private final SolrClient server;

	private final AtomicFileSystem fileSystem;
	private final List<BigVaultServerListener> listeners;
	private final List<BigVaultServerTransaction> postponedTransactions = new ArrayList<>();
	private long lastCommit;
	private Semaphore commitSemaphore = new Semaphore(1);

	public BigVaultServer(String name, BigVaultLogger bigVaultLogger, SolrServerFactory solrServerFactory,
						  DataLayerSystemExtensions extensions, DataLayerConfiguration configurations) {
		this(name, bigVaultLogger, solrServerFactory, extensions, configurations, new ArrayList<BigVaultServerListener>());
	}

	public BigVaultServer(String name, BigVaultLogger bigVaultLogger, SolrServerFactory solrServerFactory,
						  DataLayerSystemExtensions extensions, DataLayerConfiguration configurations,
						  List<BigVaultServerListener> listeners) {
		this.solrServerFactory = solrServerFactory;
		this.server = solrServerFactory.newSolrServer(name);
		this.fileSystem = solrServerFactory.getConfigFileSystem(name);
		this.bigVaultLogger = bigVaultLogger;
		this.name = name;
		this.extensions = extensions;
		this.configurations = configurations;
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

	private static String cachedVersion;

	public String getVersion() {

		if (cachedVersion == null) {
			synchronized (this) {
				if (cachedVersion == null) {
					SolrRequest request = new GenericSolrRequest(SolrRequest.METHOD.GET, "/admin/system", new ModifiableSolrParams());
					try {
						NamedList<Object> response = server.request(request);
						NamedList<Object> luceneResponse = (NamedList<Object>) response.get("lucene");
						cachedVersion = luceneResponse.get("solr-spec-version").toString();
					} catch (Throwable t) {
						t.printStackTrace();
						cachedVersion = "";
					}

				}
			}
		}

		return cachedVersion;
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
		if (response.getResults() != null) {
			final int resultsSize = response.getResults().size();
		}
		long end = new Date().getTime();

		final long qtime = end - start;
		extensions.afterQuery(params, queryName, qtime, response.getResults() == null ? 0 : response.getResults().size(), response.getDebugMap());

		for (BigVaultServerListener listener : this.listeners) {
			if (listener instanceof BigVaultServerQueryListener) {
				((BigVaultServerQueryListener) listener).onQuery(params, response);
			}
		}
		return response;
	}

	public SolrDocument realtimeGet(String id, boolean callExtensions)
			throws BigVaultException.CouldNotExecuteQuery {

		try {
			SolrDocument document = server.getById(id);

			if (callExtensions) {
				extensions.afterRealtimeGetById(0, id, document != null);
			}

			return document;
		} catch (SolrServerException | IOException e) {
			throw new BigVaultException.CouldNotExecuteQuery("realtime get of " + id, e);
		}

	}

	public List<SolrDocument> realtimeGet(List<String> ids, boolean callExtensions)
			throws BigVaultException.CouldNotExecuteQuery {

		try {
			List<SolrDocument> documents = server.getById(ids);

			if (callExtensions) {
				for (SolrDocument document : documents) {
					extensions.afterRealtimeGetById(0, (String) document.getFieldValue("id"), true);
				}

				if (documents.size() != ids.size()) {
					Set<String> foundIds = documents.stream().map((r) -> (String) r.getFieldValue("id")).collect(Collectors.toSet());
					for (String id : ids) {
						if (!foundIds.contains(id)) {
							extensions.afterRealtimeGetById(0, id, false);
						}
					}
				}
			}

			return documents;
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
				if (remoteSolrException.code() == HTTP_ERROR_404_NOT_FOUND && remoteSolrException.getMessage().contains("mlt")) {
					throw new BigVaultRuntimeException.MLTComponentNotConfigured();
				}
			}

			return handleQueryException(params, currentAttempt, e);

		} catch (RemoteSolrException solrServerException) {
			if (solrServerException.code() == HTTP_ERROR_400_BAD_REQUEST) {
				throw new BadRequest(params, solrServerException);
			}

			if (solrServerException.code() == HTTP_ERROR_404_NOT_FOUND && solrServerException.getMessage().contains("mlt")) {
				throw new BigVaultRuntimeException.MLTComponentNotConfigured();
			}

			return handleQueryException(params, currentAttempt, solrServerException);
		}
	}

	private QueryResponse handleQueryException(SolrParams params, int currentAttempt, Exception solrServerException)
			throws BigVaultException.CouldNotExecuteQuery {
		if (currentAttempt < MAX_FAIL_ATTEMPT) {
			int sleep = waitedMillisecondsBeforeNextAttempt(currentAttempt);
			LOGGER.warn("Solr thrown an unexpected exception, retrying the query '{}' in {} milliseconds...",
					SolrUtils.toString(params), sleep, solrServerException);
			sleepBeforeRetrying(sleep);

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
				return handleRemoteSolrExceptionWhileAddingRecords(transaction, currentAttempt, solrServerException, code);

			} catch (SolrServerException | IOException e) {
				if (e.getCause() != null && e.getCause() instanceof RemoteSolrException) {
					RemoteSolrException remoteSolrException = (RemoteSolrException) e.getCause();
					int code = getExceptionCode(remoteSolrException);
					return handleRemoteSolrExceptionWhileAddingRecords(transaction, currentAttempt, remoteSolrException,
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
						"" + MAX_FAIL_ATTEMPT + " errors occured while add/updating records",
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
																			   int currentAttempt, Exception exception,
																			   int code)
			throws BigVaultException.OptimisticLocking, BigVaultException.CouldNotExecuteQuery {
		if (code == HTTP_ERROR_409_CONFLICT) {
			return handleOptimisticLockingException(transaction, exception);

		} else if (code == HTTP_ERROR_400_BAD_REQUEST) {
			throw new BigVaultRuntimeException.BadRequest(transaction, exception);

			//Solrcloud return an error 500 for updates with conflicts
		} else if (code == HTTP_ERROR_500_INTERNAL && isRouteExceptionVersionConflict(exception)) {
			return handleOptimisticLockingException(transaction, exception);

		} else if (code == HTTP_ERROR_500_INTERNAL) {
			throw new SolrInternalError(transaction, exception);

		} else {
			int sleep = waitedMillisecondsBeforeNextAttempt(currentAttempt);
			LOGGER.warn("Solr thrown an unexpected exception, while handling addAll. Retrying in {} milliseconds...",
					sleep, exception);
			sleepBeforeRetrying(sleep);
			return retryAddAll(transaction, currentAttempt, exception);
		}
	}

	private boolean isRouteExceptionVersionConflict(Exception exception) {
		//Solrcloud send different exceptions depending on the reason of the conflict
		return exception.getMessage().startsWith("version conflict") || exception.getMessage().startsWith(
				"Document not found for update");
	}

	TransactionResponseDTO retryAddAll(BigVaultServerTransaction transaction, int currentAttempt, Exception e)
			throws CouldNotExecuteQuery, OptimisticLocking {
		if (currentAttempt < MAX_FAIL_ATTEMPT) {
			return tryAddAll(transaction, currentAttempt + 1);

		} else {
			throw new BigVaultRuntimeException("" + MAX_FAIL_ATTEMPT + " errors occured while add/updating records", e);
		}
	}

	private TransactionResponseDTO handleOptimisticLockingException(BigVaultServerTransaction transaction,
																	Exception optimisticLockingException)
			throws BigVaultException.OptimisticLocking {
		//		try {
		//			softCommit();
		//		} catch (IOException | SolrServerException solrServerException) {
		//			LOGGER.warn("Failed to softCommit records that caused an optimistic locking exception", optimisticLockingException);
		//			sleepBeforeRetrying(solrServerException);
		//			throw new BigVaultRuntimeException("" + maxFailAttempt + " errors occured while committing records",
		//					solrServerException);
		//		}

		Map map = null;
		String id = BigVaultException.OptimisticLocking.retreiveId(optimisticLockingException.getMessage());
		Long version = BigVaultException.OptimisticLocking.retreiveVersion(optimisticLockingException.getMessage());
		List<String> recordsWithNewVersion = new ArrayList<>();

		for (SolrInputDocument updatedDoc : transaction.getUpdatedDocuments()) {
			String updatedDocId = (String) updatedDoc.getFieldValue("id");
			recordsWithNewVersion.add(updatedDocId);
			if (id.equals(updatedDocId)) {
				break;
			}
		}

		throw new BigVaultException.OptimisticLocking(id, version, recordsWithNewVersion, optimisticLockingException);
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
		long methodEnteranceTimeStamp = new Date().getTime();
		trySoftCommit(0, methodEnteranceTimeStamp);
		long end = new Date().getTime();
		extensions.afterCommmit(null, end - methodEnteranceTimeStamp);
	}

	TransactionResponseDTO add(BigVaultServerTransaction transaction)
			throws SolrServerException, IOException {

		int commitWithin = transaction.getRecordsFlushing().getWithinMilliseconds();
		if (transaction.isRequiringLock()) {
			String transactionId = UUIDV1Generator.newRandomId();
			verifyTransactionOptimisticLocking(commitWithin, transactionId, transaction.getUpdatedDocuments());
			transaction.setTransactionId(transactionId);
		}
		return processChanges(transaction);
	}

	void verifyTransactionOptimisticLocking(int commitWithin, String transactionId,
											List<SolrInputDocument> updatedDocuments)
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
		// FIXME can be removed once we support solr 8+
		if (configurations.getRecordsDaoSolrServerType() == SolrServerType.CLOUD) {
			req.setParam("min_rf", String.valueOf(configurations.getSolrMinimalReplicationFactor()));
		}
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

	private void trySoftCommit(int currentAttempt, long methodEnteranceTimeStamp)
			throws IOException, SolrServerException {

		if (methodEnteranceTimeStamp < lastCommit) {
			//Another thread has committed during the wait
			return;
		}

		try {
			commitSemaphore.acquire();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		if (methodEnteranceTimeStamp < lastCommit) {
			//Another thread has committed during the wait
			commitSemaphore.release();
			return;
		} else {
			boolean released = false;
			try {
				long timeStampWhenStartingToCommit = new Date().getTime();
				server.commit(true, true, true);
				lastCommit = timeStampWhenStartingToCommit;
				commitSemaphore.release();
				released = true;
			} catch (SolrServerException | IOException | RemoteSolrException solrServerException) {
				commitSemaphore.release();
				released = true;

				if (currentAttempt < MAX_FAIL_ATTEMPT) {
					int sleep = waitedMillisecondsBeforeNextAttempt(currentAttempt);
					LOGGER.warn("Solr thrown an unexpected exception, retrying the softCommit... in {} milliseconds",
							sleep, solrServerException);
					sleepBeforeRetrying(sleep);
					trySoftCommit(currentAttempt + 1, methodEnteranceTimeStamp);
				} else {
					throw solrServerException;
				}
			} finally {
				if (!released) {
					commitSemaphore.release();
				}
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

	private void sleepBeforeRetrying(int sleep) {
		if (sleep > 0) {
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
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
		return new BigVaultServer(name, bigVaultLogger, solrServerFactory, extensions, configurations, this.listeners);
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

	public boolean isMLTAvailable() {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("mlt", "true");
		params.set("mlt.count", "1");
		params.set("q", "id:the_private_key");
		params.set("mlt.fl", "value_s");
		params.add(MoreLikeThisParams.MIN_DOC_FREQ, "0");
		params.add(MoreLikeThisParams.MIN_TERM_FREQ, "0");
		params.add(CommonParams.QT, "/mlt");
		try {
			QueryResponse response = server.query(params);
			return response.getResponse().get("response") != null && response.getResponse().get("match") != null;

		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
	}

	public TupleStream tupleStream(Map<String, String> props) {

		StreamContext streamContext = new StreamContext();
		SolrClientCache solrClientCache = new SolrClientCache();
		streamContext.setSolrClientCache(solrClientCache);
		TupleStream tupleStream = solrServerFactory.newTupleStream(name, props);
		tupleStream.setStreamContext(streamContext);
		return tupleStream;
	}

	private int waitedMillisecondsBeforeNextAttempt(int currentAttempt) {
		if (currentAttempt <= 10) {
			return 0;

			//Only with high resilience mode, next 10 attempts are made over 1 minute
		} else if (currentAttempt <= 20) {
			return 6000;

			//Only with high resilience mode, next 10 attempts are made over 10 minute
		} else if (currentAttempt <= 30) {
			return 60000;

			//Only with high resilience mode, next 10 attempts are made over 20 minute
		} else if (currentAttempt <= 40) {
			return 2 * 60000;

			//Only with high resilience mode, next attempts are made every 5 minutes
		} else {
			return 5 * 60_000;
		}
	}

	public void setResilienceModeToNormal() {
		MAX_FAIL_ATTEMPT = NORMAL_RESILIENCE_MODE_ATTEMPTS;
	}

	public void setResilienceModeToHigh() {
		MAX_FAIL_ATTEMPT = HIGH_RESILIENCE_MODE_ATTEMPTS;

	}

	public void setResilienceModeToZero() {
		MAX_FAIL_ATTEMPT = 0;
	}
}
