/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.records;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.ModificationImpact;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;

public class AddToBatchProcessImpactHandlerTest extends ConstellioTest {

	@Mock BatchProcessesManager batchProcessManager;
	@Mock SearchServices searchServices;

	@Mock LogicalSearchCondition condition;

	@Mock BatchProcessAction action;

	@Mock Metadata reindexedMetadata;
	List<Metadata> reindexedMetadatas;
	@Mock ModificationImpact modificationImpact;
	@Mock List<String> theRecordIds;
	@Mock BatchProcess theBatchProcess;

	AddToBatchProcessImpactHandler handler;

	@Before
	public void setUp()
			throws Exception {
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

		when(searchServices.searchRecordIds(any(LogicalSearchQuery.class))).thenReturn(theRecordIds);
		when(batchProcessManager.add(eq(theRecordIds), eq("zeCollection"), any(BatchProcessAction.class)))
				.thenReturn(theBatchProcess);
		ArgumentCaptor<LogicalSearchQuery> query = ArgumentCaptor.forClass(LogicalSearchQuery.class);

		handler.prepareToHandle(modificationImpact);
		List<BatchProcess> theBatchProcesses = handler.getAllCreatedBatchProcesses();

		verify(searchServices).searchRecordIds(query.capture());
		verify(batchProcessManager).add(theRecordIds, "zeCollection", action);
		assertThat(query.getValue()).isInstanceOf(LogicalSearchQuery.class);
		assertThat(((LogicalSearchQuery) query.getValue()).getCondition()).isEqualTo(condition);
		assertThat(theBatchProcesses).containsOnly(theBatchProcess);

		verify(batchProcessManager, never()).markAsPending(theBatchProcess);
		handler.handle();
		verify(batchProcessManager).markAsPending(theBatchProcess);
	}

	@Test
	public void givenCancelledThenCancelThenCancelBatchProcessesAndClearList()
			throws Exception {

		when(searchServices.searchRecordIds(any(LogicalSearchQuery.class))).thenReturn(theRecordIds);
		when(batchProcessManager.add(eq(theRecordIds), eq("zeCollection"), any(BatchProcessAction.class)))
				.thenReturn(theBatchProcess);
		ArgumentCaptor<LogicalSearchQuery> query = ArgumentCaptor.forClass(LogicalSearchQuery.class);

		handler.prepareToHandle(modificationImpact);
		List<BatchProcess> theBatchProcesses = handler.getAllCreatedBatchProcesses();

		verify(searchServices).searchRecordIds(query.capture());
		verify(batchProcessManager).add(theRecordIds, "zeCollection", action);
		assertThat(query.getValue()).isInstanceOf(LogicalSearchQuery.class);
		assertThat(((LogicalSearchQuery) query.getValue()).getCondition()).isEqualTo(condition);
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
