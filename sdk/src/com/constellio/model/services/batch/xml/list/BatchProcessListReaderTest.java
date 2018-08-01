package com.constellio.model.services.batch.xml.list;

import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.batchprocess.RecordBatchProcess;
import com.constellio.model.services.batch.actions.ReindexMetadatasBatchProcessAction;
import com.constellio.model.utils.ParametrizedInstanceUtils;
import com.constellio.sdk.tests.ConstellioTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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

		writer.addRecordBatchProcess("1", "zeQuery", "zeUltimateCollection", requestDateTime1, 1000, batchProcessAction);

		RecordBatchProcess batchProcessReturned = (RecordBatchProcess) reader.read("1");

		assertThat(batchProcessReturned.getId()).isEqualTo("1");
		assertThat(batchProcessReturned.getRequestDateTime()).isEqualTo(requestDateTime1);
		assertThat(batchProcessReturned.getTotalRecordsCount()).isEqualTo(1000);
		assertThat(batchProcessReturned.getCollection()).isEqualTo("zeUltimateCollection");
		assertThat(batchProcessReturned.getQuery()).isEqualTo("zeQuery");
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

		writer.addRecordBatchProcess("1", "zeQuery", "zeUltimateCollection", requestDateTime1, 1000, batchProcessAction);
		writer.markBatchProcessAsPending("1");
		writer.startNextBatchProcess(startDateTime);

		assertThat(reader.readPendingBatchProcesses()).isEmpty();
		assertThat(reader.readStandbyBatchProcesses()).isEmpty();
		RecordBatchProcess batchProcessReturned = (RecordBatchProcess) reader.readCurrent();

		assertThat(batchProcessReturned.getId()).isEqualTo("1");
		assertThat(batchProcessReturned.getRequestDateTime()).isEqualTo(requestDateTime1);
		assertThat(batchProcessReturned.getStartDateTime()).isEqualTo(startDateTime);
		assertThat(batchProcessReturned.getQuery()).isEqualTo("zeQuery");
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
			assertThat(((RecordBatchProcess) batchProcess).getTotalRecordsCount()).isEqualTo(1000);
			assertThat(((RecordBatchProcess) batchProcess).getHandledRecordsCount()).isEqualTo(0);
			assertThat(((RecordBatchProcess) batchProcess).getAction().getClass())
					.isEqualTo(ReindexMetadatasBatchProcessAction.class);
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
			assertThat(((RecordBatchProcess) batchProcess).getTotalRecordsCount()).isEqualTo(1000);
			assertThat(((RecordBatchProcess) batchProcess).getHandledRecordsCount()).isEqualTo(0);
			assertThat(((RecordBatchProcess) batchProcess).getAction().getClass())
					.isEqualTo(ReindexMetadatasBatchProcessAction.class);
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
			assertThat(((RecordBatchProcess) batchProcess).getTotalRecordsCount()).isEqualTo(1000);
			assertThat(((RecordBatchProcess) batchProcess).getHandledRecordsCount()).isEqualTo(0);
			assertThat(((RecordBatchProcess) batchProcess).getAction().getClass())
					.isEqualTo(ReindexMetadatasBatchProcessAction.class);
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
			assertThat(((RecordBatchProcess) batchProcess).getTotalRecordsCount()).isEqualTo(2000);
			assertThat(((RecordBatchProcess) batchProcess).getHandledRecordsCount()).isEqualTo(2000);
			assertThat(((RecordBatchProcess) batchProcess).getAction().getClass())
					.isEqualTo(ReindexMetadatasBatchProcessAction.class);
		}
	}

	@Test
	public void givenAFinishedBatchProcessWith2ErrorsWhenReadFinishedBatchProcessesThenTheyAreReturned()
			throws Exception {

		writer.addRecordBatchProcess("1", "zeQuery", "zeUltimateCollection", requestDateTime2, 1000, batchProcessAction);
		writer.markBatchProcessAsFinished(batchProcess1, 2);

		List<BatchProcess> batchProcessesReturned = reader.readFinishedBatchProcesses();

		assertThat(batchProcessesReturned.get(0).getId()).isEqualTo("1");
		assertThat(batchProcessesReturned.get(0).getErrors()).isEqualTo(2);
		assertThat(((RecordBatchProcess) batchProcessesReturned.get(0)).getHandledRecordsCount()).isEqualTo(1000);
	}

	@Test
	public void givenNoFinishedBatchProcessesWhenReadFinishedBatchProcessesThenReturnEmptyList()
			throws Exception {

		reader.readFinishedBatchProcesses().isEmpty();
	}

	private void givenTwoFinishedTwoPendingAndThreeStandbyBatchProcesses() {
		writer.addRecordBatchProcess("1", "zeQuery1", "zeUltimateCollection", requestDateTime1, 2000, batchProcessAction);
		writer.addRecordBatchProcess("2", "zeQuery2", "zeUltimateCollection", requestDateTime1, 2000, batchProcessAction);
		writer.addRecordBatchProcess("3", "zeQuery3", "zeUltimateCollection", requestDateTime2, 1000, batchProcessAction);
		writer.addRecordBatchProcess("4", "zeQuery4", "zeUltimateCollection", requestDateTime2, 1000, batchProcessAction);
		writer.addRecordBatchProcess("5", "zeQuery5", "zeUltimateCollection", requestDateTime2, 1000, batchProcessAction);
		writer.addRecordBatchProcess("6", "zeQuery6", "zeUltimateCollection", requestDateTime2, 1000, batchProcessAction);
		writer.addRecordBatchProcess("7", "zeQuery7", "zeUltimateCollection", requestDateTime2, 1000, batchProcessAction);
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
