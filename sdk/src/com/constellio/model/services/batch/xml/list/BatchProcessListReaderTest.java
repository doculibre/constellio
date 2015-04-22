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
package com.constellio.model.services.batch.xml.list;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.services.batch.actions.ReindexMetadatasBatchProcessAction;
import com.constellio.model.utils.ParametrizedInstanceUtils;
import com.constellio.sdk.tests.ConstellioTest;

public class BatchProcessListReaderTest extends ConstellioTest {

	@Mock ParametrizedInstanceUtils ParametrizedInstanceUtils;

	Document document;
	BatchProcessListWriter writer;
	BatchProcessListReader reader;
	Element rootElement;
	@Mock BatchProcess inexistentBatchProcess;
	@Mock BatchProcess batchProcess1;
	@Mock BatchProcess batchProcess2;
	@Mock BatchProcess batchProcess3;
	@Mock BatchProcess batchProcess4;
	BatchProcessAction batchProcessAction = new ReindexMetadatasBatchProcessAction(
			new ArrayList<String>(Arrays.asList("a", "b")));
	Element batchProcessActionElement;

	LocalDateTime requestDateTime1;
	LocalDateTime requestDateTime2;
	LocalDateTime startDateTime;

	@Before
	public void setup()
			throws Exception {

		when(batchProcess1.getId()).thenReturn("1");
		when(batchProcess2.getId()).thenReturn("2");
		when(batchProcess3.getId()).thenReturn("3");
		when(batchProcess4.getId()).thenReturn("4");

		document = new Document();
		writer = spy(new BatchProcessListWriter(document));

		writer.createEmptyProcessList();
		rootElement = spy(document.getRootElement());
		reader = spy(new BatchProcessListReader(document));

		requestDateTime1 = aDateTime();
		requestDateTime2 = aDateTime();
		startDateTime = new LocalDateTime();

		//	batchProcessActionElement = XMLElementUtils.newElementWithContent("errors", "zeSuperAction");

		//	doReturn(ParametrizedInstanceUtils).when(reader).newParametrizedInstanceUtils();
		//	doReturn(ParametrizedInstanceUtils).when(writer).newParametrizedInstanceUtils();

		//	when(ParametrizedInstanceUtils.toElement("action", batchProcessAction)).thenReturn(batchProcessActionElement);
		//		when(ParametrizedInstanceUtils.toObject(batchProcessActionElement, BatchProcessAction.class))
		//				.thenReturn(batchProcessAction);
	}

	@Test
	public void givenAPendingBatchProcessWhenReadThenItIsReturned()
			throws Exception {

		writer.addBatchProcess("1", "zeUltimateCollection", requestDateTime1, 1000, batchProcessAction);

		BatchProcess batchProcessReturned = reader.read("1");

		assertThat(batchProcessReturned.getId()).isEqualTo("1");
		assertThat(batchProcessReturned.getRequestDateTime()).isEqualTo(requestDateTime1);
		assertThat(batchProcessReturned.getTotalRecordsCount()).isEqualTo(1000);
		assertThat(batchProcessReturned.getCollection()).isEqualTo("zeUltimateCollection");
		assertThat(batchProcessReturned.getHandledRecordsCount()).isEqualTo(0);
		assertThat(batchProcessReturned.getAction().getClass()).isEqualTo(ReindexMetadatasBatchProcessAction.class);
	}

	@Test(expected = BatchProcessListReaderRuntimeException.NoBatchProcessesInList.class)
	public void givenNoPendingBatchProcessWhenReadThenException()
			throws Exception {

		reader.read("1");
	}

	@Test
	public void givenACurrentBatchProcessWhenReadCurrentThenItIsReturned()
			throws Exception {

		writer.addBatchProcess("1", "zeUltimateCollection", requestDateTime1, 1000, batchProcessAction);
		writer.markBatchProcessAsPending("1");
		writer.startNextBatchProcess(startDateTime);

		assertThat(reader.readPendingBatchProcesses()).isEmpty();
		assertThat(reader.readStandbyBatchProcesses()).isEmpty();
		BatchProcess batchProcessReturned = reader.readCurrent();

		assertThat(batchProcessReturned.getId()).isEqualTo("1");
		assertThat(batchProcessReturned.getRequestDateTime()).isEqualTo(requestDateTime1);
		assertThat(batchProcessReturned.getStartDateTime()).isEqualTo(startDateTime);
		assertThat(batchProcessReturned.getTotalRecordsCount()).isEqualTo(1000);
		assertThat(batchProcessReturned.getHandledRecordsCount()).isEqualTo(0);
		assertThat(batchProcessReturned.getAction().getClass()).isEqualTo(ReindexMetadatasBatchProcessAction.class);
	}

	@Test
	public void givenNoCurrentBatchProcessWhenReadCurrentThenNullReturned()
			throws Exception {

		assertThat(reader.readCurrent()).isNull();
	}

	@Test
	public void givenThreeStandbyBatchProcessesWhenReadStandbyBatchProcessesThenTheyAreReturned()
			throws Exception {

		givenTwoFinishedTwoPendingAndThreeStandbyBatchProcesses();

		List<BatchProcess> batchProcessesReturned = reader.readStandbyBatchProcesses();

		assertThat(batchProcessesReturned.size()).isEqualTo(3);
		assertThat(batchProcessesReturned.get(0).getId()).isEqualTo("5");
		assertThat(batchProcessesReturned.get(1).getId()).isEqualTo("6");
		assertThat(batchProcessesReturned.get(2).getId()).isEqualTo("7");
		for (BatchProcess batchProcess : batchProcessesReturned) {
			assertThat(batchProcess.getRequestDateTime()).isEqualTo(requestDateTime2);
			assertThat(batchProcess.getTotalRecordsCount()).isEqualTo(1000);
			assertThat(batchProcess.getHandledRecordsCount()).isEqualTo(0);
			assertThat(batchProcess.getAction().getClass()).isEqualTo(ReindexMetadatasBatchProcessAction.class);
		}
	}

	@Test
	public void givenTwoPendingAndThreeStandbyBatchProcessesWhenMarkAllAsPendingThen5Pending()
			throws Exception {

		givenTwoFinishedTwoPendingAndThreeStandbyBatchProcesses();
		writer.markAllBatchProcessAsPending();

		List<BatchProcess> standbyBatchProcesses = reader.readStandbyBatchProcesses();
		assertThat(standbyBatchProcesses.size()).isEqualTo(0);

		List<BatchProcess> pendingBatchProcesses = reader.readPendingBatchProcesses();
		assertThat(pendingBatchProcesses.size()).isEqualTo(5);
		assertThat(pendingBatchProcesses.get(0).getId()).isEqualTo("3");
		assertThat(pendingBatchProcesses.get(1).getId()).isEqualTo("4");
		assertThat(pendingBatchProcesses.get(2).getId()).isEqualTo("5");
		assertThat(pendingBatchProcesses.get(3).getId()).isEqualTo("6");
		assertThat(pendingBatchProcesses.get(4).getId()).isEqualTo("7");
		for (BatchProcess batchProcess : pendingBatchProcesses) {
			assertThat(batchProcess.getRequestDateTime()).isEqualTo(requestDateTime2);
			assertThat(batchProcess.getTotalRecordsCount()).isEqualTo(1000);
			assertThat(batchProcess.getHandledRecordsCount()).isEqualTo(0);
			assertThat(batchProcess.getAction().getClass()).isEqualTo(ReindexMetadatasBatchProcessAction.class);
		}
	}

	@Test
	public void givenTwoPendingBatchProcessesWhenReadPendingBatchProcessesThenTheyAreReturned()
			throws Exception {

		givenTwoFinishedTwoPendingAndThreeStandbyBatchProcesses();

		List<BatchProcess> batchProcessesReturned = reader.readPendingBatchProcesses();

		assertThat(batchProcessesReturned.size()).isEqualTo(2);
		assertThat(batchProcessesReturned.get(0).getId()).isEqualTo("3");
		assertThat(batchProcessesReturned.get(1).getId()).isEqualTo("4");
		for (BatchProcess batchProcess : batchProcessesReturned) {
			assertThat(batchProcess.getRequestDateTime()).isEqualTo(requestDateTime2);
			assertThat(batchProcess.getTotalRecordsCount()).isEqualTo(1000);
			assertThat(batchProcess.getHandledRecordsCount()).isEqualTo(0);
			assertThat(batchProcess.getAction().getClass()).isEqualTo(ReindexMetadatasBatchProcessAction.class);
		}
	}

	@Test
	public void givenNoPendingBatchProcessesWhenReadPendingBatchProcessesThenEmptyList()
			throws Exception {

		assertThat(reader.readPendingBatchProcesses()).isEmpty();
	}

	@Test
	public void givenNoStandbyBatchProcessesWhenReadStandbyBatchProcessesThenEmptyList()
			throws Exception {

		assertThat(reader.readStandbyBatchProcesses()).isEmpty();
	}

	@Test
	public void givenTwoFinishedBatchProcessesWhenReadFinishedBatchProcessesThenTheyAreReturned()
			throws Exception {

		givenTwoFinishedTwoPendingAndThreeStandbyBatchProcesses();

		List<BatchProcess> batchProcessesReturned = reader.readFinishedBatchProcesses();

		assertThat(batchProcessesReturned.size()).isEqualTo(2);
		assertThat(batchProcessesReturned.get(0).getId()).isEqualTo("1");
		assertThat(batchProcessesReturned.get(1).getId()).isEqualTo("2");
		for (BatchProcess batchProcess : batchProcessesReturned) {
			assertThat(batchProcess.getRequestDateTime()).isEqualTo(requestDateTime1);
			assertThat(batchProcess.getTotalRecordsCount()).isEqualTo(2000);
			assertThat(batchProcess.getHandledRecordsCount()).isEqualTo(2000);
			assertThat(batchProcess.getAction().getClass()).isEqualTo(ReindexMetadatasBatchProcessAction.class);
		}
	}

	@Test
	public void givenAFinishedBatchProcessWith2ErrorsWhenReadFinishedBatchProcessesThenTheyAreReturned()
			throws Exception {

		writer.addBatchProcess("1", "zeUltimateCollection", requestDateTime2, 1000, batchProcessAction);
		writer.markBatchProcessAsFinished(batchProcess1, 2);

		List<BatchProcess> batchProcessesReturned = reader.readFinishedBatchProcesses();

		assertThat(batchProcessesReturned.get(0).getId()).isEqualTo("1");
		assertThat(batchProcessesReturned.get(0).getErrors()).isEqualTo(2);
		assertThat(batchProcessesReturned.get(0).getHandledRecordsCount()).isEqualTo(1000);
	}

	@Test
	public void givenNoFinishedBatchProcessesWhenReadFinishedBatchProcessesThenReturnEmptyList()
			throws Exception {

		reader.readFinishedBatchProcesses().isEmpty();
	}

	private void givenTwoFinishedTwoPendingAndThreeStandbyBatchProcesses() {
		writer.addBatchProcess("1", "zeUltimateCollection", requestDateTime1, 2000, batchProcessAction);
		writer.addBatchProcess("2", "zeUltimateCollection", requestDateTime1, 2000, batchProcessAction);
		writer.addBatchProcess("3", "zeUltimateCollection", requestDateTime2, 1000, batchProcessAction);
		writer.addBatchProcess("4", "zeUltimateCollection", requestDateTime2, 1000, batchProcessAction);
		writer.addBatchProcess("5", "zeUltimateCollection", requestDateTime2, 1000, batchProcessAction);
		writer.addBatchProcess("6", "zeUltimateCollection", requestDateTime2, 1000, batchProcessAction);
		writer.addBatchProcess("7", "zeUltimateCollection", requestDateTime2, 1000, batchProcessAction);
		writer.markBatchProcessAsPending("1");
		writer.markBatchProcessAsPending("2");
		writer.markBatchProcessAsPending("3");
		writer.markBatchProcessAsPending("4");
		writer.startNextBatchProcess(new LocalDateTime());
		writer.markBatchProcessAsFinished(batchProcess1, 0);
		writer.startNextBatchProcess(new LocalDateTime());
		writer.markBatchProcessAsFinished(batchProcess2, 0);
	}
}
