package com.constellio.model.services.batch.xml.list;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jdom2.Document;
import org.jdom2.Element;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.batchprocess.BatchProcessStatus;
import com.constellio.model.entities.batchprocess.RecordBatchProcess;
import com.constellio.sdk.tests.ConstellioTest;

public class BatchProcessListWriterTest extends ConstellioTest {

	private static final String ERRORS = "errors";
	private static final String PROGRESSION = "progression";
	private static final String ID = "id";
	private static final String STRING = "collection";
	private static final String BATCH_PROCESS = "batchProcess";
	private static final String PREVIOUS_BATCH_PROCESSES = "previousBatchProcesses";
	private static final String CURRENT_BATCH_PROCESS = "currentBatchProcess";
	private static final String PENDING_BATCH_PROCESSES = "pendingBatchProcesses";
	private static final String STANDBY_BATCH_PROCESSES = "standbyBatchProcesses";
	Document document;
	BatchProcessListWriter listWriter;
	Element rootElement;
	@Mock RecordBatchProcess batchProcess1;
	@Mock RecordBatchProcess batchProcess2;
	@Mock RecordBatchProcess inexistentBatchProcess;

	@Mock BatchProcessAction action;

	LocalDateTime localDateTime = aDateTime();

	@Before
	public void setup()
			throws Exception {

		when(action.getInstanceParameters()).thenReturn(new Object[] {});

		document = new Document();
		listWriter = spy(new BatchProcessListWriter(document));

		listWriter.createEmptyProcessList();
		rootElement = spy(document.getRootElement());
	}

	@Test
	public void whenCreateEmptyProcessListThenStructureCreated()
			throws Exception {

		assertThat(rootElement.getName()).isEqualTo("root");
		assertThat(rootElement.getChildren()).hasSize(4);
		assertThat(rootElement.getChild(PREVIOUS_BATCH_PROCESSES).getContent()).isEmpty();
		assertThat(rootElement.getChild(CURRENT_BATCH_PROCESS).getContent()).isEmpty();
		assertThat(rootElement.getChild(PENDING_BATCH_PROCESSES).getContent()).isEmpty();
		assertThat(rootElement.getChild(STANDBY_BATCH_PROCESSES).getContent()).isEmpty();
	}

	@Test
	public void whenAddOneBatchProcessThenItIsAddedInPreviousBatchProcessesList()
			throws Exception {

		listWriter.addRecordBatchProcess("1", "zeQuery", "zeUltimateCollection", new LocalDateTime(), 1000, action);

		assertThat(rootElement.getChild(STANDBY_BATCH_PROCESSES).getChildren()).hasSize(1);
		assertThat(rootElement.getChild(STANDBY_BATCH_PROCESSES).getChild(BATCH_PROCESS).getAttributeValue(ID)).isEqualTo("1");
		assertThat(rootElement.getChild(PENDING_BATCH_PROCESSES).getContent()).isEmpty();
		assertThat(rootElement.getChild(CURRENT_BATCH_PROCESS).getContent()).isEmpty();
		assertThat(rootElement.getChild(PREVIOUS_BATCH_PROCESSES).getContent()).isEmpty();

	}

	@Test
	public void whenAddTwoBatchProcessesThenTheyAreAddedInStandbyBatchProcessesList()
			throws Exception {

		listWriter.addRecordBatchProcess("1", "zeQuery1", "zeUltimateCollection", new LocalDateTime(), 1000, action);
		listWriter.addRecordBatchProcess("2", "zeQuery2", "zeUltimateCollection", new LocalDateTime(), 1000, action);

		assertThat(rootElement.getChild(STANDBY_BATCH_PROCESSES).getChildren()).hasSize(2);
		assertThat(rootElement.getChild(CURRENT_BATCH_PROCESS).getContent()).isEmpty();
		assertThat(rootElement.getChild(PENDING_BATCH_PROCESSES).getContent()).isEmpty();
		assertThat(rootElement.getChild(PREVIOUS_BATCH_PROCESSES).getContent()).isEmpty();

	}

	@Test
	public void givenTwoStandbyBatchProcessesWhenCancellingOneThenDeletedAndOtherStillInStandby()
			throws Exception {

		listWriter.addRecordBatchProcess("1", "zeQuery1", "zeUltimateCollection", new LocalDateTime(), 1000, action);
		listWriter.addRecordBatchProcess("2", "zeQuery2", "zeUltimateCollection", new LocalDateTime(), 1000, action);

		listWriter.cancelStandByBatchProcess("2");

		assertThat(rootElement.getChild(STANDBY_BATCH_PROCESSES).getChildren()).hasSize(1);
		Element firstElement = rootElement.getChild(STANDBY_BATCH_PROCESSES).getChildren().get(0);
		String id = firstElement.getAttributeValue("id");
		assertThat(id).isEqualTo("1");
		assertThat(rootElement.getChild(CURRENT_BATCH_PROCESS).getContent()).isEmpty();
		assertThat(rootElement.getChild(PENDING_BATCH_PROCESSES).getContent()).isEmpty();
		assertThat(rootElement.getChild(PREVIOUS_BATCH_PROCESSES).getContent()).isEmpty();

	}

	@Test
	public void givenTwoPendingBatchProcessesWhenStartNextBatchProcessThenNextBatchIsMovedToCurrentBactchProcessAndStartTimeIsUpdate()
			throws Exception {

		listWriter.addRecordBatchProcess("1", "zeQuery1", "zeUltimateCollection", new LocalDateTime(), 1000, action);
		listWriter.addRecordBatchProcess("2", "zeQuery2", "zeUltimateCollection", new LocalDateTime(), 1000, action);
		listWriter.markAllBatchProcessAsPending();

		listWriter.startNextBatchProcess(localDateTime);

		assertThat(rootElement.getChild(PENDING_BATCH_PROCESSES).getChildren()).hasSize(1);
		assertThat(rootElement.getChild(PENDING_BATCH_PROCESSES).getChild(BATCH_PROCESS).getAttribute(ID).getValue()).isEqualTo(
				"2");
		assertThat(rootElement.getChild(CURRENT_BATCH_PROCESS).getChildren()).hasSize(1);
		assertThat(rootElement.getChild(CURRENT_BATCH_PROCESS).getChild(BATCH_PROCESS).getAttribute(ID).getValue())
				.isEqualTo("1");
		assertThat(rootElement.getChild(CURRENT_BATCH_PROCESS).getChild(BATCH_PROCESS).getChild("startDateTime").getText())
				.isEqualTo(localDateTime.toString());
		assertThat(rootElement.getChild(PREVIOUS_BATCH_PROCESSES).getChildren()).isEmpty();
	}

	@Test(expected = BatchProcessListWriterRuntimeException.NoPendingBatchProcessesInList.class)
	public void givenNoPendingBacthProcessesWhenStartNextBatchProcessThenException()
			throws Exception {
		try {
			listWriter.startNextBatchProcess(localDateTime);
		} finally {
			assertThat(rootElement.getChild(STANDBY_BATCH_PROCESSES).getChildren()).isEmpty();
			assertThat(rootElement.getChild(PENDING_BATCH_PROCESSES).getChildren()).isEmpty();
			assertThat(rootElement.getChild(CURRENT_BATCH_PROCESS).getChildren()).isEmpty();
			assertThat(rootElement.getChild(PREVIOUS_BATCH_PROCESSES).getChildren()).isEmpty();
		}
	}

	@Test(expected = BatchProcessListWriterRuntimeException.CannotHaveTwoBatchProcessInCurrentBatchProcessList.class)
	public void givenABatchProcessInCurrentBatchProcessListWhenStartNextBatchProcessThenException()
			throws Exception {

		listWriter.addRecordBatchProcess("1", "zeQuery1", "zeUltimateCollection", new LocalDateTime(), 1000, action);
		listWriter.addRecordBatchProcess("2", "zeQuery2", "zeUltimateCollection", new LocalDateTime(), 1000, action);
		listWriter.markAllBatchProcessAsPending();
		listWriter.startNextBatchProcess(localDateTime);

		try {
			listWriter.startNextBatchProcess(localDateTime);
		} finally {
			assertThat(rootElement.getChild(STANDBY_BATCH_PROCESSES).getChildren()).isEmpty();
			assertThat(rootElement.getChild(PENDING_BATCH_PROCESSES).getChildren()).hasSize(1);
			assertThat(rootElement.getChild(PREVIOUS_BATCH_PROCESSES).getContent()).isEmpty();
			assertThat(rootElement.getChild(CURRENT_BATCH_PROCESS).getChildren()).hasSize(1);
		}
	}

	@Test
	public void givenABatchProcessInCurrentBatchProcessListWhenMarkBatchProcessAsFinishedThenBatchProcessIsMovedToPreviousProcessesList()
			throws Exception {

		when(batchProcess1.getId()).thenReturn("1");
		listWriter.addRecordBatchProcess("1", "zeQuery1", "zeUltimateCollection", new LocalDateTime(), 1000, action);
		listWriter.addRecordBatchProcess("2", "zeQuery2", "zeUltimateCollection", new LocalDateTime(), 1000, action);
		listWriter.markAllBatchProcessAsPending();
		listWriter.startNextBatchProcess(localDateTime);

		listWriter.markBatchProcessAsFinished(batchProcess1, 0);
		assertThat(rootElement.getChild(STANDBY_BATCH_PROCESSES).getChildren()).isEmpty();
		assertThat(rootElement.getChild(PREVIOUS_BATCH_PROCESSES).getChildren()).hasSize(1);
		assertThat(rootElement.getChild(PREVIOUS_BATCH_PROCESSES).getChild(BATCH_PROCESS).getAttributeValue(ID)).isEqualTo("1");
		assertThat(rootElement.getChild(CURRENT_BATCH_PROCESS).getContent()).isEmpty();
		assertThat(rootElement.getChild(PENDING_BATCH_PROCESSES).getChildren()).hasSize(1);
	}

	@Test
	public void givenABatchProcessInPendingBatchProcessListWhenMarkBatchProcessAsFinishedThenBatchProcessIsMovedToPreviousProcessesList()
			throws Exception {

		when(batchProcess2.getId()).thenReturn("2");
		listWriter.addRecordBatchProcess("1", "zeQuery1", "zeUltimateCollection", new LocalDateTime(), 1000, action);
		listWriter.addRecordBatchProcess("2", "zeQuery2", "zeUltimateCollection", new LocalDateTime(), 1000, action);
		listWriter.markBatchProcessAsPending("2");
		;
		listWriter.startNextBatchProcess(localDateTime);

		listWriter.markBatchProcessAsFinished(batchProcess2, 0);
		assertThat(rootElement.getChild(STANDBY_BATCH_PROCESSES).getChildren()).hasSize(1);
		assertThat(rootElement.getChild(PREVIOUS_BATCH_PROCESSES).getChildren()).hasSize(1);
		assertThat(rootElement.getChild(CURRENT_BATCH_PROCESS).getChild(BATCH_PROCESS)).isNull();
		assertThat(rootElement.getChild(PENDING_BATCH_PROCESSES).getContent()).hasSize(0);
	}

	@Test
	public void givenABatchProcessInPreviousBatchProcessListWhenMarkBatchProcessAsFinishedThenException()
			throws Exception {

		when(batchProcess2.getId()).thenReturn("2");
		listWriter.addRecordBatchProcess("1", "zeQuery1", "zeUltimateCollection", new LocalDateTime(), 1000, action);
		listWriter.addRecordBatchProcess("2", "zeQuery2", "zeUltimateCollection", new LocalDateTime(), 1000, action);
		listWriter.markBatchProcessAsPending("2");
		listWriter.startNextBatchProcess(localDateTime);
		listWriter.markBatchProcessAsFinished(batchProcess2, 0);

		try {
			listWriter.markBatchProcessAsFinished(batchProcess2, 0);
			fail("BatchProcessListWriterRuntimeException.BatchProcessAlreadyFinished expected");
		} catch (BatchProcessListWriterRuntimeException.BatchProcessAlreadyFinished e) {
			//OK
		}
		assertThat(rootElement.getChild(STANDBY_BATCH_PROCESSES).getChildren()).hasSize(1);
		assertThat(rootElement.getChild(PREVIOUS_BATCH_PROCESSES).getChildren()).hasSize(1);
		assertThat(rootElement.getChild(CURRENT_BATCH_PROCESS).getChild(BATCH_PROCESS)).isNull();
		assertThat(rootElement.getChild(PENDING_BATCH_PROCESSES).getContent()).isEmpty();
	}

	@Test(expected = BatchProcessListWriterRuntimeException.BatchProcessNotFound.class)
	public void givenAInexistentBatchProcessIdWhenMarkBatchProcessAsFinishedThenException()
			throws Exception {

		when(inexistentBatchProcess.getId()).thenReturn("3");
		listWriter.addRecordBatchProcess("1", "zeQuery1", "zeUltimateCollection", new LocalDateTime(), 1000, action);
		listWriter.addRecordBatchProcess("2", "zeQuery2", "zeUltimateCollection", new LocalDateTime(), 1000, action);
		listWriter.markBatchProcessAsPending("1");
		listWriter.markBatchProcessAsPending("2");

		try {
			listWriter.markBatchProcessAsFinished(inexistentBatchProcess, 0);
		} finally {
			assertThat(rootElement.getChild(PREVIOUS_BATCH_PROCESSES).getContent()).isEmpty();
			assertThat(rootElement.getChild(CURRENT_BATCH_PROCESS).getContent()).isEmpty();
			assertThat(rootElement.getChild(PENDING_BATCH_PROCESSES).getChildren()).hasSize(2);
		}
	}

	@Test
	public void whenIncrementProgressionThenHandledRecordsCountAndProgressionAreUpdated()
			throws Exception {

		when(batchProcess1.getId()).thenReturn("1");
		listWriter.addRecordBatchProcess("1", "zeQuery1", "zeUltimateCollection", new LocalDateTime(), 100, action);
		listWriter.markBatchProcessAsPending("1");
		listWriter.startNextBatchProcess(localDateTime);
		listWriter.incrementProgression(batchProcess1, 50, 0);

		assertThat(rootElement.getChild(CURRENT_BATCH_PROCESS).getChild(BATCH_PROCESS).getChild(PROGRESSION).getText())
				.isEqualTo("50");

		listWriter.incrementProgression(batchProcess1, 30, 0);

		assertThat(rootElement.getChild(CURRENT_BATCH_PROCESS).getChild(BATCH_PROCESS).getChild(PROGRESSION).getText())
				.isEqualTo("80");
	}

	@Test(expected = BatchProcessListWriterRuntimeException.BatchProcessNotFound.class)
	public void givenInexistentBatchProcessInListWhenIncrementProgressionThenException()
			throws Exception {

		when(batchProcess1.getId()).thenReturn("1");
		when(batchProcess2.getId()).thenReturn("2");
		listWriter.addRecordBatchProcess("1", "zeQuery1", "zeUltimateCollection", new LocalDateTime(), 100, action);
		listWriter.markBatchProcessAsPending("1");
		listWriter.startNextBatchProcess(localDateTime);

		listWriter.incrementProgression(batchProcess2, 50, 0);
	}

	@Test
	public void givenProgressionCountEqualToRecordsCountWhenIncrementProgressionThenMarkBatchAsFinished()
			throws Exception {

		when(batchProcess1.getId()).thenReturn("1");
		listWriter.addRecordBatchProcess("1", "zeQuery1", "zeUltimateCollection", new LocalDateTime(), 100, action);
		listWriter.markBatchProcessAsPending("1");
		listWriter.startNextBatchProcess(localDateTime);

		listWriter.incrementProgression(batchProcess1, 100, 0);

		verify(listWriter).markBatchProcessAsFinished(batchProcess1, 0);
		assertThat(batchProcess1.getStatus() == BatchProcessStatus.FINISHED);
	}

	@Test
	public void givenProgressionCountLessThanRecordsCountWhenIncrementProgressionThenBatchProcessNotFinished()
			throws Exception {

		when(batchProcess1.getId()).thenReturn("1");
		listWriter.addRecordBatchProcess("1", "zeQuery1", "zeUltimateCollection", new LocalDateTime(), 100, action);
		listWriter.markBatchProcessAsPending("1");
		listWriter.startNextBatchProcess(localDateTime);

		listWriter.incrementProgression(batchProcess1, 50, 0);

		verify(listWriter, never()).markBatchProcessAsFinished(batchProcess1, 0);
		assertThat(batchProcess1.getStatus() == BatchProcessStatus.CURRENT);
	}

	@Test
	public void givenErrorsWhenMarkBatchProcessAsFinishedThenErrosCountInErrorList()
			throws Exception {

		when(batchProcess1.getId()).thenReturn("1");
		listWriter.addRecordBatchProcess("1", "zeQuery1", "zeUltimateCollection", new LocalDateTime(), 1000, action);
		listWriter.markBatchProcessAsPending("1");
		listWriter.startNextBatchProcess(localDateTime);

		listWriter.markBatchProcessAsFinished(batchProcess1, 20);

		assertThat(rootElement.getChild(PREVIOUS_BATCH_PROCESSES).getChild(BATCH_PROCESS).getChild(ERRORS).getText()).isEqualTo(
				"20");
	}

	@Test
	public void givenErrorsWhenIncrementProgressionThenErrosCountInErrorList()
			throws Exception {

		when(batchProcess1.getId()).thenReturn("1");
		listWriter.addRecordBatchProcess("1", "zeQuery1", "zeUltimateCollection", new LocalDateTime(), 1000, action);
		listWriter.markBatchProcessAsPending("1");
		listWriter.startNextBatchProcess(localDateTime);

		listWriter.incrementProgression(batchProcess1, 1000, 100);

		assertThat(rootElement.getChild(PREVIOUS_BATCH_PROCESSES).getChild(BATCH_PROCESS).getChild(ERRORS).getText()).isEqualTo(
				"100");
	}

	@Test
	public void givenTwoBatchProcessPartWith10ErrorsEachWhenIncrementProgressionThenErrosIncremented()
			throws Exception {

		when(batchProcess1.getId()).thenReturn("1");
		listWriter.addRecordBatchProcess("1", "zeQuery1", "zeUltimateCollection", new LocalDateTime(), 1000, action);
		listWriter.markBatchProcessAsPending("1");
		listWriter.startNextBatchProcess(localDateTime);

		listWriter.incrementProgression(batchProcess1, 500, 10);
		listWriter.incrementProgression(batchProcess1, 500, 10);

		assertThat(rootElement.getChild(PREVIOUS_BATCH_PROCESSES).getChild(BATCH_PROCESS).getChild(ERRORS).getText()).isEqualTo(
				"20");
	}
}
