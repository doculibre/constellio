package com.constellio.model.services.records;

import com.constellio.data.dao.services.records.DataStore;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.batchprocess.RecordBatchProcess;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.ModificationImpact;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.CollectionFilters;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.SolrQueryBuilderContext;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class AddToBatchProcessImpactHandlerRealTest extends ConstellioTest {

	@Mock BatchProcessesManager batchProcessManager;
	@Mock SearchServices searchServices;

	@Mock BatchProcessAction action;

	@Mock Metadata reindexedMetadata;
	List<Metadata> reindexedMetadatas;
	@Mock ModificationImpact modificationImpact;
	@Mock RecordBatchProcess theBatchProcess;

	AddToBatchProcessImpactHandler handler;

	TestsSchemasSetup setup = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = setup.new ZeSchemaMetadatas();

	@Mock LogicalSearchCondition condition;

	@Before
	public void setUp()
			throws Exception {
		//		defineSchemasManager().using(setup);
		//		condition = from(zeSchema.type()).where(Schemas.TITLE).isEqualTo("Ze title");

		when(condition.getFilters()).thenReturn(new CollectionFilters(zeCollection, DataStore.RECORDS, false));
		when(condition.getSolrQuery(any(SolrQueryBuilderContext.class))).thenReturn("zeQuery");
		when(condition.getCollection()).thenReturn(zeCollection);

		when(reindexedMetadata.getCollection()).thenReturn(zeCollection);
		reindexedMetadatas = Arrays.asList(reindexedMetadata);
		when(modificationImpact.getLogicalSearchCondition()).thenReturn(condition);
		when(modificationImpact.getMetadataToReindex()).thenReturn(reindexedMetadatas);

		handler = spy(new AddToBatchProcessImpactHandler(batchProcessManager, searchServices));

		doReturn(action).when(handler).newBatchProcessAction(reindexedMetadatas);
	}

	@Test
	public void whenHandlingModificationImpactThenSearchRecordsAndAddToBatchProcess()
			throws Exception {

		when(searchServices.hasResults(condition)).thenReturn(true);
		when(batchProcessManager.addBatchProcessInStandby(eq(condition), any(BatchProcessAction.class), anyString()))
				.thenReturn(theBatchProcess);

		handler.prepareToHandle(modificationImpact);
		List<BatchProcess> theBatchProcesses = handler.getAllCreatedBatchProcesses();

		verify(batchProcessManager).addBatchProcessInStandby(condition, action, "reindex.transaction");
		assertThat(theBatchProcesses).containsOnly(theBatchProcess);

		verify(batchProcessManager, never()).markAsPending(theBatchProcess);
		handler.handle();
		verify(batchProcessManager).markAsPending(theBatchProcess);
	}

	@Test
	public void givenNoRecordsThenNoBatchProcessCreated()
			throws Exception {

		when(searchServices.hasResults(condition)).thenReturn(false);
		when(batchProcessManager.addBatchProcessInStandby(eq(condition), any(BatchProcessAction.class), anyString()))
				.thenReturn(theBatchProcess);

		handler.prepareToHandle(modificationImpact);

		verify(batchProcessManager, never()).addBatchProcessInStandby(any(LogicalSearchCondition.class),
				any(BatchProcessAction.class), anyString());

	}

	@Test
	public void givenCancelledThenCancelThenCancelBatchProcessesAndClearList()
			throws Exception {

		when(searchServices.hasResults(condition)).thenReturn(true);
		when(batchProcessManager.addBatchProcessInStandby(eq(condition), any(BatchProcessAction.class), anyString()))
				.thenReturn(theBatchProcess);

		handler.prepareToHandle(modificationImpact);
		List<BatchProcess> theBatchProcesses = handler.getAllCreatedBatchProcesses();

		verify(batchProcessManager).addBatchProcessInStandby(condition, action, "reindex.transaction");
		assertThat(theBatchProcesses).containsOnly(theBatchProcess);

		verify(batchProcessManager, never()).cancelStandByBatchProcess(theBatchProcess);
		handler.cancel();
		verify(batchProcessManager).cancelStandByBatchProcess(theBatchProcess);
		assertThat(handler.createdBatchProcesses).isEmpty();
	}

	@Test
	public void givenEmptySearchResultWhenHandlingImpactThenNoBatchProcessCreated()
			throws Exception {

		when(searchServices.searchRecordIds(any(LogicalSearchQuery.class))).thenReturn(new ArrayList<String>());

		handler.prepareToHandle(modificationImpact);

		verifyZeroInteractions(batchProcessManager);

	}

}
