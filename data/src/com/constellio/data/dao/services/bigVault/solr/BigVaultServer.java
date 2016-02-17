package com.constellio.data.dao.services.bigVault.solr;

import static com.constellio.data.dao.services.bigVault.solr.SolrUtils.NULL_STRING;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient.RouteException;
import org.apache.solr.client.solrj.impl.CloudSolrClient.RouteResponse;
import org.apache.solr.client.solrj.impl.HttpSolrClient.RemoteSolrException;
import org.apache.solr.client.solrj.request.SolrPing;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.UpdateParams;
import org.apache.solr.common.util.NamedList;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException.CouldNotExecuteQuery;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException.OptimisticLocking;
import com.constellio.data.dao.services.bigVault.solr.BigVaultRuntimeException.BadRequest;
import com.constellio.data.dao.services.bigVault.solr.BigVaultRuntimeException.SolrInternalError;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.dao.services.solr.ConstellioSolrInputDocument;
import com.constellio.data.dao.services.solr.DateUtils;
import com.constellio.data.dao.services.solr.SolrServerFactory;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.data.io.concurrent.filesystem.VersioningAtomicFileSystem;
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
	private final VersioningAtomicFileSystem fileSystem;

	public BigVaultServer(String name, BigVaultLogger bigVaultLogger, SolrServerFactory solrServerFactory
		, DataLayerSystemExtensions extensions) {
		this.solrServerFactory = solrServerFactory;
		this.server = solrServerFactory.newSolrServer(name);
		this.fileSystem = solrServerFactory.getConfigFileSystem(name);
		
		this.bigVaultLogger = bigVaultLogger;
		this.name = name;
		this.extensions = extensions;
	}

	public String getName() {
		return name;
	}
	
	@VisibleForTesting
	public SolrServerFactory getSolrServerFactory() {
		return solrServerFactory;
	}
	
	public QueryResponse query(SolrParams params)
			throws BigVaultException.CouldNotExecuteQuery {
		int currentAttempt = 0;
		long start = new Date().getTime();
		QueryResponse response = tryQuery(params, currentAttempt);
		long end = new Date().getTime();
		extensions.afterQuery(params, end - start);
		return response;
	}

	private QueryResponse tryQuery(SolrParams params, int currentAttempt)
			throws BigVaultException.CouldNotExecuteQuery {

		try {
			return server.query(params);
		} catch (SolrServerException solrServerException) {
			solrServerException.printStackTrace();
			if (solrServerException.getCause() instanceof RemoteSolrException) {
				RemoteSolrException remoteSolrException = (RemoteSolrException) solrServerException.getCause();
				if (remoteSolrException.code() == HTTP_ERROR_400_BAD_REQUEST) {
					throw new BadRequest(params, solrServerException);
				}
			}

			return handleQueryException(params, currentAttempt, solrServerException);

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
		int currentAttempt = 0;
		long start = new Date().getTime();
		TransactionResponseDTO response = tryAddAll(transaction, currentAttempt);
		long end = new Date().getTime();
		extensions.afterUpdate(transaction, end - start);
		return response;
	}

	TransactionResponseDTO tryAddAll(BigVaultServerTransaction transaction, int currentAttempt)
			throws BigVaultException.OptimisticLocking, BigVaultException.CouldNotExecuteQuery {
		try {
			return addAndCommit(transaction);

		} catch (RemoteSolrException | RouteException solrServerException) {
			int code = getExceptionCode(solrServerException);
			return handleRemoteSolrExceptionWhileAddingRecords(transaction, currentAttempt + 1, solrServerException, code);

		} catch (SolrServerException | IOException e) {
			if (e.getCause() != null && e.getCause() instanceof RemoteSolrException) {
				RemoteSolrException remoteSolrException = (RemoteSolrException) e.getCause();
				int code = getExceptionCode(remoteSolrException);
				return handleRemoteSolrExceptionWhileAddingRecords(transaction, currentAttempt + 1, remoteSolrException, code);
			}
			throw new BigVaultException.CouldNotExecuteQuery("" + maxFailAttempt + " errors occured while add/updating records",
					e);
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

		TransactionResponseDTO response = add(transaction);
		if (transaction.getRecordsFlushing() == RecordsFlushing.NOW) {
			softCommit();
		}
		bigVaultLogger.log(transaction.getNewDocuments(), transaction.getUpdatedDocuments());
		return response;
	}

	public void softCommit()
			throws IOException, SolrServerException {
		trySoftCommit(0);
	}

	TransactionResponseDTO add(BigVaultServerTransaction transaction)
			throws SolrServerException, IOException {

		String transactionId = UUIDV1Generator.newRandomId();
		int commitWithin = transaction.getRecordsFlushing().getWithinMilliseconds();
		verifyOptimisticLocking(commitWithin, transactionId, transaction.getUpdatedDocuments());
		transaction.setTransactionId(transactionId);
		return processChanges(transaction);

	}

	void verifyOptimisticLocking(int commitWithin, String transactionId, List<SolrInputDocument> updatedDocuments)
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

				if (updatedDocument.getFieldValue("type_s") == null) {
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

	private List<SolrInputDocument> copyRemovingVersionsFromAtomicUpdate(List<SolrInputDocument> updatedDocuments,
			List<String> deletedById) {
		List<SolrInputDocument> withoutVersions = new ArrayList<>();
		for (SolrInputDocument document : updatedDocuments) {

			if (document.getFieldValue("type_s") == null) {
				String lockId = "lock__" + document.getFieldValue("id");
				deletedById.add(lockId);
			}

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
		List<SolrInputDocument> newDocumentsWithoutIndexes = withoutIndexes(transaction.getNewDocuments());
		List<SolrInputDocument> updatedDocumentsWithoutIndexes = withoutIndexes(transaction.getUpdatedDocuments());

		BigVaultUpdateRequest req = new BigVaultUpdateRequest();
		List<String> deletedQueriesAndLocks = new ArrayList<>(transaction.getDeletedQueries());
		deletedQueriesAndLocks.add("transaction_s:" + transaction.getTransactionId());
		List<SolrInputDocument> docsWithoutVersions = copyRemovingVersionsFromAtomicUpdate(transaction.getUpdatedDocuments(),
				new ArrayList<String>());
		req.setCommitWithin(commitWithin);
		req.setParam(UpdateParams.VERSIONS, "true");
		req.add(docsWithoutVersions);
		req.add(transaction.getNewDocuments());
		req.deleteById(transaction.getDeletedRecords());
		req.setDeleteQuery(deletedQueriesAndLocks);
		UpdateResponse updateResponse = req.process(server);

		return new TransactionResponseDTO(retrieveQTime(updateResponse), retrieveNewDocumentVersions(updateResponse));
	}

	private int retrieveQTime(UpdateResponse updateResponse) {
		NamedList header = updateResponse.getResponseHeader();
		if (header != null) {
			return ((Number) header.get("QTime")).intValue();
		} else {
			return 0;
		}

	}

	private Map<String, Long> retrieveNewDocumentVersions(UpdateResponse updateResponse) {
		Map<String, Long> newVersions = new HashMap<>();
		NamedList responseNamedlist = updateResponse.getResponse();
		if (updateResponse.getResponse() instanceof RouteResponse) {
			RouteResponse routeResponses = (RouteResponse) responseNamedlist;
			NamedList<NamedList<Object>> routeResponsesNamedlist = routeResponses.getRouteResponses();

			for (Entry<String, NamedList<Object>> routeResponseNamedlistEntry : routeResponsesNamedlist) {
				retrieveVersionsFromNamedlist(routeResponseNamedlistEntry.getValue(), newVersions);
			}

		} else {
			retrieveVersionsFromNamedlist(responseNamedlist, newVersions);
		}
		return newVersions;
	}

	private void retrieveVersionsFromNamedlist(NamedList<Object> response, Map<String, Long> newVersions) {
		NamedList<Long> idNewVersionPairs = (NamedList<Long>) response.get("adds");

		if (idNewVersionPairs != null) {
			for (Entry<String, Long> entry : idNewVersionPairs) {
				newVersions.put(entry.getKey(), entry.getValue());
			}
		}
	}

	private List<SolrInputDocument> withoutIndexes(List<SolrInputDocument> newDocuments) {
		List<SolrInputDocument> withoutIndexes = new ArrayList<>();
		for (SolrInputDocument doc : newDocuments) {
			if (!"index".equals(doc.getFieldValue("type_s"))) {
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
	
	public VersioningAtomicFileSystem getSolrFileSystem(){
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
	
	public void reload(){
		solrServerFactory.reloadSolrServer(name);
	}
	
	@Override
	public BigVaultServer clone(){
		return new BigVaultServer(name, bigVaultLogger, solrServerFactory, extensions);
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
			server.shutdown();
			throw e;
		}
	}

	public void recover() {
		fileSystem.forceToRecoverAll();
	}

	public boolean isHealthy() {
		return solrServerFactory.isHealthy(name);
	}
}
