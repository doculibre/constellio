package com.constellio.data.dao.services.bigVault.solr;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException.CouldNotExecuteQuery;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException.OptimisticLocking;
import com.constellio.data.dao.services.bigVault.solr.BigVaultRuntimeException.BadRequest;
import com.constellio.data.dao.services.solr.SolrServerFactory;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient.RouteException;
import org.apache.solr.client.solrj.impl.HttpSolrClient.RemoteSolrException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.data.dao.dto.records.RecordsFlushing.LATER;
import static com.constellio.data.dao.dto.records.RecordsFlushing.NOW;
import static com.constellio.data.dao.dto.records.RecordsFlushing.WITHIN_MILLISECONDS;
import static com.constellio.data.dao.dto.records.RecordsFlushing.WITHIN_SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BigVaultServerUnitTest extends ConstellioTest {
	@Mock DataLayerSystemExtensions extensions;
	@Mock DataLayerConfiguration configurations;
	@Mock AtomicFileSystem solrFileSystem;
	@Mock SolrParams solrParams;
	@Mock SolrClient server;
	@Mock SolrServerFactory solrServerFactory;
	@Mock SolrInputDocument doc1, doc2, doc3, doc4, doc5, doc6, doc7, doc8, doc9, doc10, doc11;
	@Mock List<String> deletedDocs1, deletedDocs2, deletedDocs3, deleteQueriesStrings;
	List<SolrInputDocument> addedDocs = new ArrayList<>();
	List<SolrInputDocument> modifiedDocs = new ArrayList<>();
	@Mock List<SolrParams> deleteQueries;

	BigVaultServer bigVaultServer;
	@Mock QueryResponse theQueryResponse;
	@Mock SolrDocumentList theQueryResults;
	@Mock SolrDocumentList emptyQueryResults;
	@Mock SolrDocumentList twoElementsQueryResults;
	@Mock SolrDocument theUniqueResult;

	@Before
	public void setUp() {
		String serverName = "Test";
		when(solrServerFactory.getConfigFileSystem(serverName)).thenReturn(solrFileSystem);
		when(solrServerFactory.newSolrServer(serverName)).thenReturn(server);
		bigVaultServer = spy(new BigVaultServer(serverName, BigVaultLogger.disabled(), solrServerFactory, extensions, configurations));
		when(emptyQueryResults.size()).thenReturn(0);
		when(twoElementsQueryResults.size()).thenReturn(2);
	}

	@Test
	public void whenAddThenCommitEveryDoc()
			throws Exception {

		doReturn(null).when(bigVaultServer).add(any(BigVaultServerTransaction.class));

		List<SolrInputDocument> transaction1NewDocs = Arrays.asList(doc1);
		List<SolrInputDocument> transaction1UpdatedDocs = Arrays.asList(doc2);
		List<SolrInputDocument> transaction2NewDocs = Arrays.asList(doc3);
		List<SolrInputDocument> transaction3UpdatedDocs = Arrays.asList(doc4);

		BigVaultServerTransaction t1, t2, t3, t4;

		bigVaultServer.addAll(t1 = new BigVaultServerTransaction(LATER).setNewDocuments(transaction1NewDocs)
				.setUpdatedDocuments(transaction1UpdatedDocs));
		bigVaultServer.addAll(t2 = new BigVaultServerTransaction(NOW).setNewDocuments(transaction2NewDocs)
				.setDeletedRecords(deletedDocs1));
		bigVaultServer.addAll(t3 = new BigVaultServerTransaction(WITHIN_SECONDS(1))
				.setUpdatedDocuments(transaction3UpdatedDocs).setDeletedRecords(deletedDocs2));
		bigVaultServer.addAll(t4 = new BigVaultServerTransaction(WITHIN_MILLISECONDS(2)).setDeletedRecords(deletedDocs3));

		InOrder inOrder = Mockito.inOrder(server, bigVaultServer);

		inOrder.verify(bigVaultServer).add(t1);
		inOrder.verify(bigVaultServer).add(t2);
		inOrder.verify(server).commit(true, true, true);
		inOrder.verify(bigVaultServer).add(t3);
		inOrder.verify(bigVaultServer).add(t4);
	}

	@Test(expected = BigVaultException.NonUniqueResult.class)
	public void givenNonUniqueResultsWhenQuerySingleResultThenThrowNonUniqueResultException()
			throws Exception {
		when(server.query(solrParams)).thenReturn(theQueryResponse);
		when(theQueryResponse.getResults()).thenReturn(twoElementsQueryResults);

		bigVaultServer.querySingleResult(solrParams);
	}

	@Test(expected = BigVaultException.NoResult.class)
	public void givenNoResultWhenQuerySingleResultThenThrowNoResultException()
			throws Exception {
		when(server.query(solrParams)).thenReturn(theQueryResponse);
		when(theQueryResponse.getResults()).thenReturn(emptyQueryResults);

		bigVaultServer.querySingleResult(solrParams);
	}

	@Test()
	public void givenUniqueResultWhenQuerySingleResultThenReturnUniqueResult()
			throws Exception {
		when(server.query(solrParams)).thenReturn(theQueryResponse);
		when(theQueryResponse.getResults()).thenReturn(theQueryResults);
		when(theQueryResults.size()).thenReturn(1);
		when(theQueryResults.get(0)).thenReturn(theUniqueResult);

		assertThat((Object) bigVaultServer.querySingleResult(solrParams)).isEqualTo(theUniqueResult);
	}

	@Test
	public void whenQueryResultsThenReturnNestedSolrServerQueryResults()
			throws Exception {
		when(server.query(solrParams)).thenReturn(theQueryResponse);
		when(theQueryResponse.getResults()).thenReturn(theQueryResults);

		assertThat((Object) bigVaultServer.queryResults(solrParams)).isEqualTo(theQueryResults);

		verify(server).query(solrParams);
	}

	@Test(expected = BigVaultException.CouldNotExecuteQuery.class)
	public void givenQueryWhenNestedSolrExceptionThenThrowCouldNotExecuteQuery()
			throws Exception {
		SolrServerException e = mock(SolrServerException.class);
		when(e.getMessage()).thenReturn("Random injected fault");
		when(server.query(solrParams)).thenThrow(e);

		bigVaultServer.query(solrParams);
	}

	@Test(expected = BigVaultException.CouldNotExecuteQuery.class)
	public void givenQueryWhenRemoteSolrExceptionWithCode500ThenThrowCouldNotExecuteQuery()
			throws Exception {
		RemoteSolrException e = mock(RemoteSolrException.class);
		when(e.getMessage()).thenReturn("Random injected fault");
		when(e.code()).thenReturn(500);
		when(server.query(solrParams)).thenThrow(e);

		bigVaultServer.query(solrParams);
	}

	@Test(expected = BadRequest.BadRequest.class)
	public void givenQueryWhenRemoteSolrExceptionWithCode400ThenThrowBadRequest()
			throws Exception {
		RemoteSolrException e = mock(RemoteSolrException.class);
		when(e.getMessage()).thenReturn("Random injected fault");
		when(e.code()).thenReturn(400);
		when(server.query(solrParams)).thenThrow(e);

		bigVaultServer.query(solrParams);
	}

	@Test(expected = BigVaultRuntimeException.SolrInternalError.class)
	public void whenRemoteSolrExceptionWithErrorCode500IsThrownThenThrowSolrInternalExceptionThrown()
			throws CouldNotExecuteQuery, OptimisticLocking, IOException, SolrServerException {

		RemoteSolrException e = mock(RemoteSolrException.class);
		when(e.getMessage()).thenReturn("Ze message");
		when(e.code()).thenReturn(500);

		BigVaultServerTransaction transaction = new BigVaultServerTransaction(LATER(), addedDocs, modifiedDocs, deletedDocs1,
				deleteQueriesStrings);

		doThrow(e).when(bigVaultServer).addAndCommit(transaction);
		bigVaultServer.tryAddAll(transaction, 3);

	}

	@Test(expected = BigVaultRuntimeException.SolrInternalError.class)
	public void whenRouteExceptionWithErrorCode500IsThrownThenThrowSolrInternalExceptionThrown()
			throws CouldNotExecuteQuery, OptimisticLocking, IOException, SolrServerException {

		BigVaultServerTransaction transaction = new BigVaultServerTransaction(LATER(), addedDocs, modifiedDocs, deletedDocs1,
				deleteQueriesStrings);

		RouteException e = mock(RouteException.class);
		when(e.code()).thenReturn(500);
		when(e.getMessage()).thenReturn("Ze message");
		doThrow(e).when(bigVaultServer)
				.addAndCommit(transaction);

		bigVaultServer.tryAddAll(transaction, 3);

	}

	@Test(expected = OptimisticLocking.class)
	public void whenRouteExceptionWithErrorCode500WithMessageConflictThenThrowOptimisticLocking()
			throws Exception {

		BigVaultServerTransaction transaction = new BigVaultServerTransaction(LATER(), addedDocs, modifiedDocs, deletedDocs1,
				deleteQueriesStrings);

		RouteException e = mock(RouteException.class);
		when(e.code()).thenReturn(500);
		when(e.getMessage()).thenReturn(
				"version conflict for e80fda8a-f842-48de-9ccf-c6ec847a18f7 expected=1487130319535472640 actual=1487130319704293376");
		doThrow(e).when(bigVaultServer)
				.addAndCommit(transaction);

		bigVaultServer.tryAddAll(transaction, 3);

	}

	@Test(expected = OptimisticLocking.class)
	public void whenRouteExceptionWithErrorCode500WithDocumentNotFoundForUpdateThenThrowOptimisticLocking()
			throws Exception {

		BigVaultServerTransaction transaction = new BigVaultServerTransaction(LATER(), addedDocs, modifiedDocs, deletedDocs1,
				deleteQueriesStrings);

		RouteException e = mock(RouteException.class);
		when(e.code()).thenReturn(500);
		when(e.getMessage()).thenReturn("Document not found for update.  id=idx_act_idOfAnotherNonExistentIndex");
		doThrow(e).when(bigVaultServer)
				.addAndCommit(transaction);

		bigVaultServer.tryAddAll(transaction, 3);

	}

	@Test
	public void whenSolrServerThenRetryUpTo10Times()
			throws Exception {

		BigVaultServerTransaction transaction = new BigVaultServerTransaction(LATER(), addedDocs, modifiedDocs, deletedDocs1,
				deleteQueriesStrings);


		Exception e = new RemoteSolrException("host", 42, "no", null);
		doThrow(e).when(bigVaultServer)
				.addAndCommit(transaction);

		try {
			bigVaultServer.tryAddAll(transaction, 3);
		} catch (Exception ex) {
			assertThat(ex).isInstanceOf(BigVaultRuntimeException.class);
		}

		verify(bigVaultServer).retryAddAll(transaction, 3, e);
		verify(bigVaultServer).retryAddAll(transaction, 4, e);
		verify(bigVaultServer).retryAddAll(transaction, 5, e);
		verify(bigVaultServer).retryAddAll(transaction, 6, e);
		verify(bigVaultServer).retryAddAll(transaction, 7, e);
		verify(bigVaultServer).retryAddAll(transaction, 8, e);
		verify(bigVaultServer).retryAddAll(transaction, 9, e);
		verify(bigVaultServer).retryAddAll(transaction, 10, e);
		verify(bigVaultServer, never()).retryAddAll(transaction, 11, e);


	}


	//@Test
	public void whenSolrServerThenRetryUpTo60Times()
			throws Exception {

		BigVaultServerTransaction transaction = new BigVaultServerTransaction(LATER(), addedDocs, modifiedDocs, deletedDocs1,
				deleteQueriesStrings);

		bigVaultServer.setResilienceModeToHigh();
		Exception e = new RemoteSolrException("host", 42, "no", null);
		doThrow(e).when(bigVaultServer)
				.addAndCommit(transaction);

		try {
			bigVaultServer.tryAddAll(transaction, 3);
		} catch (Exception ex) {
			assertThat(ex).isInstanceOf(BigVaultRuntimeException.class);
		}

		verify(bigVaultServer).retryAddAll(transaction, 3, e);
		verify(bigVaultServer).retryAddAll(transaction, 4, e);
		verify(bigVaultServer).retryAddAll(transaction, 5, e);
		verify(bigVaultServer).retryAddAll(transaction, 6, e);
		verify(bigVaultServer).retryAddAll(transaction, 7, e);
		verify(bigVaultServer).retryAddAll(transaction, 8, e);
		verify(bigVaultServer).retryAddAll(transaction, 9, e);
		verify(bigVaultServer).retryAddAll(transaction, 10, e);
		verify(bigVaultServer).retryAddAll(transaction, 11, e);
		verify(bigVaultServer).retryAddAll(transaction, 12, e);
		verify(bigVaultServer).retryAddAll(transaction, 13, e);
		verify(bigVaultServer).retryAddAll(transaction, 14, e);
		verify(bigVaultServer, never()).retryAddAll(transaction, 15, e);


	}


}
