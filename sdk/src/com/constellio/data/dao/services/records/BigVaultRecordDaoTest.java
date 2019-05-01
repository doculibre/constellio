package com.constellio.data.dao.services.records;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.SolrRecordDTO;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.DataLayerLogger;
import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.dao.services.bigVault.BigVaultRecordDao;
import com.constellio.data.dao.services.bigVault.RecordDaoException;
import com.constellio.data.dao.services.bigVault.RecordDaoRuntimeException;
import com.constellio.data.dao.services.bigVault.RecordDaoRuntimeException.RecordDaoRuntimeException_RecordsFlushingFailed;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestUtils.MapBuilder;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.sdk.tests.TestUtils.asMap;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BigVaultRecordDaoTest extends ConstellioTest {

	List emptyList = new ArrayList<>();
	@Mock DataLayerLogger dataLayerLogger;
	@Mock BigVaultServer bigVaultServer;
	@Mock DataStoreTypesFactory dataStoreTypesFactory;
	@Mock TransactionDTO transactionDTO;
	String zeTransactionId = "zeTransactionId";
	@Mock SecondTransactionLogManager secondTransactionLogManager;

	BigVaultRecordDao recordDao;

	@Before
	public void setUp()
			throws Exception {
		recordDao = new BigVaultRecordDao(bigVaultServer, dataStoreTypesFactory, null, dataLayerLogger);

	}

	//@Test
	// TODO Fix sorting
	public void whenAddingMultipleChildrenThenParentIsOnlyIncrementedOnceByTheQuantityOfNewChildren()
			throws Exception {

		ArgumentCaptor<BigVaultServerTransaction> transactionCaptor = ArgumentCaptor.forClass(BigVaultServerTransaction.class);

		RecordDTO child1 = new SolrRecordDTO("child1",
				buildParamMapWith("zeCollection", "zeType_default").andWith("parentPId_s", "parent")
						.andWith("otherRefId_s", "zeRef")
						.build(), false);
		RecordDTO child2 = new SolrRecordDTO("child2",
				buildParamMapWith("zeCollection", "zeType_default").andWith("parentPId_s", "parent")
						.andWith("otherRefId_s", "zeRef")
						.build(), false);

		TransactionDTO transactionDTO = new TransactionDTO(RecordsFlushing.NOW)
				.withNewRecords(Arrays.asList(child1, child2));
		recordDao.execute(transactionDTO);

		verify(bigVaultServer).addAll(transactionCaptor.capture());

		assertThat(transactionCaptor.getValue().getRecordsFlushing()).isEqualTo(RecordsFlushing.NOW());
		assertThat(transactionCaptor.getValue().getDeletedQueries()).isEmpty();
		assertThat(transactionCaptor.getValue().getDeletedRecords()).isEmpty();

		List<SolrInputDocument> addedDocuments = transactionCaptor.getValue().getNewDocuments();
		List<SolrInputDocument> modifiedDocuments = transactionCaptor.getValue().getUpdatedDocuments();
		assertThat(addedDocuments).hasSize(6);
		assertThat(addedDocuments.get(0).getFieldValue("id")).isEqualTo("child1");
		assertThat(addedDocuments.get(0).getFieldValue("_version_")).isEqualTo(0L);
		assertThat(addedDocuments.get(0).getFieldValue("schema_s")).isEqualTo("zeType_default");
		assertThat(addedDocuments.get(0).getFieldValue("parentPId_s")).isEqualTo("parent");
		assertThat(addedDocuments.get(0).getFieldValue("otherRefId_s")).isEqualTo("zeRef");
		assertThat(addedDocuments.get(0).getFieldValue("collection_s")).isEqualTo("zeCollection");

		assertThat(addedDocuments.get(1).getFieldValue("id")).isEqualTo("idx_act_child1");
		assertThat(addedDocuments.get(1).getFieldValue("type_s")).isEqualTo("index");
		assertThat(addedDocuments.get(1).getFieldValue("collection_s")).isEqualTo("zeCollection");

		assertThat(addedDocuments.get(2).getFieldValue("id")).isEqualTo("child2");
		assertThat(addedDocuments.get(2).getFieldValue("_version_")).isEqualTo(0L);
		assertThat(addedDocuments.get(2).getFieldValue("schema_s")).isEqualTo("zeType_default");
		assertThat(addedDocuments.get(2).getFieldValue("parentPId_s")).isEqualTo("parent");
		assertThat(addedDocuments.get(2).getFieldValue("otherRefId_s")).isEqualTo("zeRef");
		assertThat(addedDocuments.get(2).getFieldValue("collection_s")).isEqualTo("zeCollection");

		assertThat(addedDocuments.get(3).getFieldValue("id")).isEqualTo("idx_act_child2");
		assertThat(addedDocuments.get(3).getFieldValue("type_s")).isEqualTo("index");
		assertThat(addedDocuments.get(3).getFieldValue("collection_s")).isEqualTo("zeCollection");

		assertThat(addedDocuments.get(4).getFieldValue("id")).isEqualTo("idx_rfc_child1");
		assertThat(addedDocuments.get(4).getFieldValue("type_s")).isEqualTo("index");
		assertThat(addedDocuments.get(4).getFieldValue("refs_d")).isEqualTo(0.0);
		assertThat(addedDocuments.get(4).getFieldValue("collection_s")).isEqualTo("zeCollection");
		assertThat(addedDocuments.get(4).getFieldValue("ancestors_ss")).isNull();

		assertThat(addedDocuments.get(5).getFieldValue("id")).isEqualTo("idx_rfc_child2");
		assertThat(addedDocuments.get(5).getFieldValue("type_s")).isEqualTo("index");
		assertThat(addedDocuments.get(5).getFieldValue("refs_d")).isEqualTo(0.0);
		assertThat(addedDocuments.get(5).getFieldValue("collection_s")).isEqualTo("zeCollection");
		assertThat(addedDocuments.get(5).getFieldValue("ancestors_ss")).isNull();

		assertThat(modifiedDocuments).hasSize(3);
		assertThat(modifiedDocuments.get(0).getFieldValue("id")).isEqualTo("idx_act_zeRef");
		assertThat(modifiedDocuments.get(0).getFieldValue("type_s")).isEqualTo("index");
		assertThat(modifiedDocuments.get(0).getFieldValue("collection_s")).isEqualTo("zeCollection");
		assertThat(modifiedDocuments.get(0).getFieldValue("_version_")).isEqualTo(1L);

		assertThat(modifiedDocuments.get(1).getFieldValue("id")).isEqualTo("idx_act_parent");
		assertThat(modifiedDocuments.get(1).getFieldValue("type_s")).isEqualTo("index");
		assertThat(modifiedDocuments.get(1).getFieldValue("collection_s")).isEqualTo("zeCollection");
		assertThat(modifiedDocuments.get(1).getFieldValue("_version_")).isEqualTo(1L);

		assertThat(modifiedDocuments.get(2).getFieldValue("id")).isEqualTo("idx_rfc_zeRef");
		assertThat(modifiedDocuments.get(2).getFieldValue("refs_d")).isEqualTo(asMap("inc", 2.0));
		assertThat(modifiedDocuments.get(2).getFieldValue("ancestors_ss")).isNull();
	}

	@Test
	public void whenFlushingThenSoftCommit()
			throws Exception {

		recordDao.flush();

		verify(bigVaultServer).flush();

	}

	@Test(expected = RecordDaoRuntimeException_RecordsFlushingFailed.class)
	public void givenIOExcepionWhenFlushingThenThrowException()
			throws Exception {

		doThrow(IOException.class).when(bigVaultServer).flush();

		recordDao.flush();

	}

	@Test(expected = RecordDaoRuntimeException_RecordsFlushingFailed.class)
	public void givenSolrServerExcepionWhenFlushingThenThrowException()
			throws Exception {

		doThrow(SolrServerException.class).when(bigVaultServer).flush();

		recordDao.flush();

	}

	@Test
	public void givenRecordDaoWithSecondTransactionLogThenLogTransactions()
			throws Exception {

		recordDao = new BigVaultRecordDao(bigVaultServer, dataStoreTypesFactory, secondTransactionLogManager, dataLayerLogger);
		when(transactionDTO.getTransactionId()).thenReturn(zeTransactionId);
		when(transactionDTO.getDeletedByQueries())
				.thenReturn(asList((SolrParams) new ModifiableSolrParams().set("q", "request")));
		recordDao.execute(transactionDTO);

		InOrder inOrder = inOrder(secondTransactionLogManager, bigVaultServer);
		inOrder.verify(secondTransactionLogManager)
				.prepare(eq(transactionDTO.getTransactionId()), any(BigVaultServerTransaction.class));
		inOrder.verify(bigVaultServer).addAll(any(BigVaultServerTransaction.class));
		inOrder.verify(secondTransactionLogManager).flush(zeTransactionId, null);

	}

	@Test
	public void givenRecordDaoWithSecondTransactionLogWhenOptimisticLockingExceptionOccurWhenExecutingThenCancel()
			throws Exception {

		recordDao = new BigVaultRecordDao(bigVaultServer, dataStoreTypesFactory, secondTransactionLogManager, dataLayerLogger);
		when(transactionDTO.getTransactionId()).thenReturn(zeTransactionId);
		when(transactionDTO.getDeletedByQueries())
				.thenReturn(asList((SolrParams) new ModifiableSolrParams().set("q", "request")));
		BigVaultException.OptimisticLocking exception = mock(BigVaultException.OptimisticLocking.class);
		when(exception.getId()).thenReturn("zeId");
		doThrow(exception).when(bigVaultServer).addAll(any(BigVaultServerTransaction.class));

		try {
			recordDao.execute(transactionDTO);
			fail("exception expected");
		} catch (RecordDaoException.OptimisticLocking e) {
			//Ok
		}

		InOrder inOrder = inOrder(secondTransactionLogManager, bigVaultServer);
		inOrder.verify(secondTransactionLogManager)
				.prepare(eq(transactionDTO.getTransactionId()), any(BigVaultServerTransaction.class));
		inOrder.verify(bigVaultServer).addAll(any(BigVaultServerTransaction.class));
		inOrder.verify(secondTransactionLogManager).cancel(zeTransactionId);
		verify(secondTransactionLogManager, never()).flush(zeTransactionId, null);

	}

	@Test
	public void givenRecordDaoWithSecondTransactionLogWhenBigVaultExceptionOccurWhenExecutingThenCancel()
			throws Exception {

		recordDao = new BigVaultRecordDao(bigVaultServer, dataStoreTypesFactory, secondTransactionLogManager, dataLayerLogger);
		when(transactionDTO.getTransactionId()).thenReturn(zeTransactionId);
		when(transactionDTO.getDeletedByQueries())
				.thenReturn(asList((SolrParams) new ModifiableSolrParams().set("q", "request")));
		doThrow(BigVaultException.class).when(bigVaultServer)
				.addAll(any(BigVaultServerTransaction.class));

		try {
			recordDao.execute(transactionDTO);
			fail("exception expected");
		} catch (RecordDaoRuntimeException e) {
			//Ok
		}

		InOrder inOrder = inOrder(secondTransactionLogManager, bigVaultServer);
		inOrder.verify(secondTransactionLogManager)
				.prepare(eq(transactionDTO.getTransactionId()), any(BigVaultServerTransaction.class));
		inOrder.verify(bigVaultServer).addAll(any(BigVaultServerTransaction.class));
		inOrder.verify(secondTransactionLogManager).cancel(zeTransactionId);
		verify(secondTransactionLogManager, never()).flush(zeTransactionId, null);

	}

	private MapBuilder<String, Object> buildParamMapWith(String collection, String schema) {
		return MapBuilder.with("collection_s", (Object) collection).andWith("schema_s", schema);
	}

}
