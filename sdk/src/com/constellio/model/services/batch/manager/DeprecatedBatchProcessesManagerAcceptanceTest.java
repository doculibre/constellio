package com.constellio.model.services.batch.manager;

import com.constellio.model.entities.batchprocess.AsyncTask;
import com.constellio.model.entities.batchprocess.AsyncTaskExecutionParams;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.batchprocess.BatchProcessStatus;
import com.constellio.model.entities.batchprocess.RecordBatchProcess;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.batch.actions.ReindexMetadatasBatchProcessAction;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.utils.ParametrizedInstanceUtils;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class DeprecatedBatchProcessesManagerAcceptanceTest extends ConstellioTest {

	String aRecordId = aString();
	String aSecondRecordId = aString();
	String aThirdRecordId = aString();
	List<String> indexedRecords = asList(aRecordId, aSecondRecordId, aThirdRecordId);
	String aRecordIdThatIsNotReindexed = aString();
	String otherBatchFirstRecordId = aString();
	String otherBatchSecondRecordId = aString();
	List<String> otherIndexedRecords = asList(otherBatchFirstRecordId, otherBatchSecondRecordId);

	@Mock Metadata firstMetadata;
	@Mock Metadata secondMetadata;
	@Mock Metadata otherMetadata;
	BatchProcessAction action = new ReindexMetadatasBatchProcessAction(new ArrayList<String>(asList("a", "b")));
	List<Metadata> indexMetadatas;

	LocalDateTime currentDate = aDateTime();
	LocalDateTime startDate = aDateTime();

	String zeComputer = "zeComputer";
	String secondComputer = "secondComputer";
	String thirdComputer = "thirdComputer";
	BatchProcessesManager batchProcessManager;
	BatchProcessesManager secondComputerBatchProcessManager;
	BatchProcessesManager thirdComputerBatchProcessManager;

	List<String> noErrors = asList();

	LogicalSearchCondition indexedRecordsCondition, otherIndexedRecordsCondition;

	@Mock ParametrizedInstanceUtils parametrizedInstanceUtils;

	@Before
	public void setUp() {
		//Toggle.MIGRATING_LEGACY_SAVESTATE.enable();
		givenCollection(zeCollection);
		givenWaitForBatchProcessAfterTestIsDisabled();
		getModelLayerFactory().getBatchProcessesController().close();
		when(firstMetadata.getCode()).thenReturn("first");
		when(secondMetadata.getCode()).thenReturn("second");
		when(otherMetadata.getCode()).thenReturn("other");

		indexMetadatas = asList(firstMetadata, secondMetadata);

		batchProcessManager = newBatchProcessManager(zeComputer, 2);
		secondComputerBatchProcessManager = newBatchProcessManager(secondComputer, 2);
		thirdComputerBatchProcessManager = newBatchProcessManager(thirdComputer, 2);

		doReturn(currentDate).doReturn(startDate).when(batchProcessManager).getCurrentTime();
		indexedRecordsCondition = fromAllSchemasIn(zeCollection).where(IDENTIFIER).isIn(indexedRecords);
		otherIndexedRecordsCondition = fromAllSchemasIn(zeCollection).where(IDENTIFIER).isIn(otherIndexedRecords);
	}

	private BatchProcessesManager newBatchProcessManager(String computer, int partsSize) {
		BatchProcessesManager batchProcessesManager = spy(new BatchProcessesManager(getModelLayerFactory()));
		batchProcessesManager.initialize();
		return batchProcessesManager;
	}

	@Test
	public void givenXMLAlreadyExistingAndBatchProcessThenManagerLoadThem()
			throws Exception {

		BatchProcess batchProcess = batchProcessManager.addBatchProcessInStandby(indexedRecordsCondition, action, null);
		batchProcessManager.markAsPending(batchProcess);

		BatchProcessesManager newBatchProcessManager = newBatchProcessManager(zeComputer, 2);

		RecordBatchProcess loadedBatchProcess = (RecordBatchProcess) newBatchProcessManager.get(batchProcess.getId());

		assertThat(loadedBatchProcess.getId()).isNotNull();
		assertThat(loadedBatchProcess.getHandledRecordsCount()).isEqualTo(0);
		assertThat(loadedBatchProcess.getRequestDateTime()).isEqualTo(currentDate);
		assertThat(loadedBatchProcess.getStartDateTime()).isNull();
		assertThat(loadedBatchProcess.getErrors()).isZero();
		assertThat(loadedBatchProcess.getAction().getClass()).isEqualTo(ReindexMetadatasBatchProcessAction.class);
		assertThat(loadedBatchProcess.getStatus()).isEqualTo(BatchProcessStatus.PENDING);
	}

	@Test
	public void whenAddingBatchProcessThenAddedToBatchProcessList()
			throws Exception {

		BatchProcess batchProcess = batchProcessManager.addBatchProcessInStandby(indexedRecordsCondition, action, null);
		assertThat(batchProcess.getStatus()).isEqualTo(BatchProcessStatus.STANDBY);
		batchProcessManager.markAsPending(batchProcess);
		RecordBatchProcess loadedBatchProcess = (RecordBatchProcess) batchProcessManager.get(batchProcess.getId());

		assertThat(loadedBatchProcess.getId()).isNotNull();
		assertThat(loadedBatchProcess.getHandledRecordsCount()).isEqualTo(0);
		assertThat(loadedBatchProcess.getRequestDateTime()).isEqualTo(currentDate);
		assertThat(loadedBatchProcess.getStartDateTime()).isNull();
		assertThat(loadedBatchProcess.getErrors()).isZero();
		assertThat(loadedBatchProcess.getAction().getClass()).isEqualTo(ReindexMetadatasBatchProcessAction.class);
		assertThat(loadedBatchProcess.getStatus()).isEqualTo(BatchProcessStatus.PENDING);

	}

	@Test
	public void whenAddingMultipleBatchProcessThenAllAddedToListAndSortedByFIFO()
			throws Exception {
		BatchProcess batchProcess1 = batchProcessManager.addBatchProcessInStandby(indexedRecordsCondition, action, null);
		BatchProcess batchProcess2 = batchProcessManager.addBatchProcessInStandby(otherIndexedRecordsCondition, action, null);
		batchProcessManager.markAllStandbyAsPending();
		List<BatchProcess> loadedBatchProcess = batchProcessManager.getPendingBatchProcesses();

		assertThat(loadedBatchProcess.get(0).getId()).isEqualTo(batchProcess1.getId());
		assertThat(loadedBatchProcess.get(1).getId()).isEqualTo(batchProcess2.getId());
	}

	@Test
	public void givenNoCurrentBatchProcessAndBatchProcessesInTodoListWhenGetCurrentBatchProcessThenReturnNext()
			throws Exception {
		BatchProcess batchProcess = batchProcessManager.addBatchProcessInStandby(indexedRecordsCondition, action, null);
		batchProcessManager.markAsPending(batchProcess);
		assertThat(batchProcessManager.get(batchProcess.getId()).getStatus()).isEqualTo(BatchProcessStatus.PENDING);

		BatchProcess startedBatchProcess = batchProcessManager.getCurrentBatchProcess();
		BatchProcess loadedStartedBatchProcess = batchProcessManager.get(batchProcess.getId());
		assertThat(loadedStartedBatchProcess.getStatus()).isEqualTo(BatchProcessStatus.CURRENT);
		assertThat(loadedStartedBatchProcess.getStartDateTime()).isEqualTo(startDate);
		assertThat(loadedStartedBatchProcess).isEqualTo(startedBatchProcess);
	}
	//
	//	@Test
	//	public void givenOnlyStandByBatchProcessesWhenGetCurrentBatchProcessPartThenNull()
	//			throws Exception {
	//		BatchProcess batchProcess = batchProcessManager.add(indexedRecords, "zeCollection", action);
	//		assertThat(batchProcessManager.getCurrentBatchProcessPart()).isNull();
	//	}

	@Test
	public void givenStandByBatchProcessWhenGetStandbyBatchProcessesListThenOneElement()
			throws Exception {
		BatchProcess batchProcess = batchProcessManager.addBatchProcessInStandby(indexedRecordsCondition, action, null);
		assertThat(batchProcessManager.getStandbyBatchProcesses()).containsOnly(batchProcess);
		assertThat(batchProcessManager.getPendingBatchProcesses()).isEmpty();
	}

	@Test
	public void givenStandByBatchProcessWhenMarkAsPendingThenPending()
			throws Exception {
		BatchProcess batchProcess = batchProcessManager.addBatchProcessInStandby(indexedRecordsCondition, action, null);
		batchProcessManager.markAsPending(batchProcess);
		assertThat(batchProcessManager.getStandbyBatchProcesses()).isEmpty();
		assertThat(batchProcessManager.getPendingBatchProcesses()).hasSize(1);
	}

	@Test
	public void givenStandByBatchProcessWhenCancelThenListEmpty()
			throws Exception {
		BatchProcess batchProcess = batchProcessManager.addBatchProcessInStandby(indexedRecordsCondition, action, null);
		batchProcessManager.cancelStandByBatchProcess(batchProcess);
		assertThat(batchProcessManager.getStandbyBatchProcesses()).isEmpty();
		assertThat(batchProcessManager.getPendingBatchProcesses()).isEmpty();
	}

	@Test
	public void givenCurrentBatchProcessWhenGetCurrentBatchProcessThenReturnCurrent()
			throws Exception {

		BatchProcess batchProcess = batchProcessManager.addBatchProcessInStandby(indexedRecordsCondition, action, null);
		batchProcessManager.markAsPending(batchProcess);
		BatchProcess startedBatchProcess = batchProcessManager.getCurrentBatchProcess();

		BatchProcess currentBatchProcess = batchProcessManager.getCurrentBatchProcess();
		assertThat(currentBatchProcess).isEqualTo(startedBatchProcess);
	}

	public static String words = "";

	public static class WordAsyncTask implements AsyncTask {

		String wordsToAdd;

		public WordAsyncTask(String wordsToAdd) {
			this.wordsToAdd = wordsToAdd;
		}

		@Override
		public void execute(AsyncTaskExecutionParams params) {

			DeprecatedBatchProcessesManagerAcceptanceTest.words = DeprecatedBatchProcessesManagerAcceptanceTest.words + wordsToAdd;
		}

		@Override
		public Object[] getInstanceParameters() {
			return new Object[]{wordsToAdd};
		}
	}

	//	@Test
	//	public void givenCurrentBatchProcessWhenGetBatchProcessPartThenReturnCorrectRecords()
	//			throws Exception {
	//		BatchProcess theAddedBatchProcess = batchProcessManager.add(indexedRecords, "zeCollection", action);
	//
	//		// An an other batch process, that should not be started before the first is finished
	//		BatchProcess batchProcess = batchProcessManager.add(otherIndexedRecords, "zeCollection", action);
	//		batchProcessManager.markAllStandbyAsPending();
	//
	//		BatchProcessPart partOfComputer1 = batchProcessManager.getCurrentBatchProcessPart();
	//		assertThat(partOfComputer1.getRecordIds()).containsExactly(aRecordId, aSecondRecordId);
	//		assertThat(partOfComputer1.getBatchProcess().getId()).isEqualTo(theAddedBatchProcess.getId());
	//
	//		BatchProcessPart partOfComputer2 = secondComputerBatchProcessManager.getCurrentBatchProcessPart();
	//		assertThat(partOfComputer2.getRecordIds()).containsExactly(aThirdRecordId);
	//		assertThat(partOfComputer2.getBatchProcess().getId()).isEqualTo(theAddedBatchProcess.getId());
	//
	//		BatchProcessPart partOfComputer3 = thirdComputerBatchProcessManager.getCurrentBatchProcessPart();
	//		assertThat(partOfComputer3).isNull();
	//
	//		BatchProcessPart nextPartOfComputer2 = batchProcessManager.markBatchProcessPartAsFinishedAndGetAnotherPart(
	//				partOfComputer2, noErrors);
	//		assertThat(nextPartOfComputer2).isNull();
	//
	//	}

	//	@Test
	//	public void givenCurrentBatchProcessWhenFinishBatchProcessPartAndGetAnotherPartThenStatusUpdatedAndNewPartReceived()
	//			throws Exception {
	//		BatchProcess theAddedBatchProcess = batchProcessManager.add(indexedRecords, "zeCollection", action);
	//		BatchProcess otherBatchProcess = batchProcessManager.add(otherIndexedRecords, "zeCollection", action);
	//		batchProcessManager.markAllStandbyAsPending();
	//
	//		BatchProcessPart part1 = batchProcessManager.getCurrentBatchProcessPart();
	//		assertThat(part1.getRecordIds()).containsExactly(aRecordId, aSecondRecordId);
	//		assertThat(part1.getBatchProcess().getId()).isEqualTo(theAddedBatchProcess.getId());
	//		assertThat(batchProcessManager.get(theAddedBatchProcess.getId()).getHandledRecordsCount()).isEqualTo(0);
	//		assertThat(batchProcessManager.get(theAddedBatchProcess.getId()).getStatus()).isEqualTo(BatchProcessStatus.CURRENT);
	//
	//		BatchProcessPart part2 = batchProcessManager.markBatchProcessPartAsFinishedAndGetAnotherPart(part1, noErrors);
	//		assertThat(part2.getRecordIds()).containsExactly(aThirdRecordId);
	//		assertThat(part2.getBatchProcess().getId()).isEqualTo(theAddedBatchProcess.getId());
	//		assertThat(batchProcessManager.get(theAddedBatchProcess.getId()).getHandledRecordsCount()).isEqualTo(2);
	//		assertThat(batchProcessManager.get(theAddedBatchProcess.getId()).getStatus()).isEqualTo(BatchProcessStatus.CURRENT);
	//
	//		BatchProcessPart part3 = batchProcessManager.markBatchProcessPartAsFinishedAndGetAnotherPart(part2, noErrors);
	//		assertThat(part3.getRecordIds()).containsExactly(otherBatchFirstRecordId, otherBatchSecondRecordId);
	//		assertThat(part3.getBatchProcess().getId()).isEqualTo(otherBatchProcess.getId());
	//		assertThat(batchProcessManager.get(theAddedBatchProcess.getId()).getHandledRecordsCount()).isEqualTo(3);
	//		assertThat(batchProcessManager.get(theAddedBatchProcess.getId()).getStatus()).isEqualTo(BatchProcessStatus.FINISHED);
	//
	//	}

	//	@Test
	//	public void givenBatchProcessFinishedThenCurrentBatchProcessIsANewOneAndPreviousBatchProcessInFinishedList()
	//			throws Exception {
	//		batchProcessManager = newBatchProcessManager(zeComputer, 3);
	//		batchProcessManager.add(indexedRecords, "zeCollection", action);
	//		batchProcessManager.add(otherIndexedRecords, "zeCollection", action);
	//		batchProcessManager.markAllStandbyAsPending();
	//		BatchProcess startedBatchProcess = batchProcessManager.getCurrentBatchProcess();
	//
	//		BatchProcessPart firstPart = batchProcessManager.getCurrentBatchProcessPart();
	//		batchProcessManager.markBatchProcessPartAsFinishedAndGetAnotherPart(firstPart, noErrors);
	//		BatchProcess nextBatchProcess = batchProcessManager.getCurrentBatchProcess();
	//
	//		BatchProcess previousBatchProcess = batchProcessManager.get(startedBatchProcess.getId());
	//		assertThat(previousBatchProcess.getStatus()).isEqualTo(BatchProcessStatus.FINISHED);
	//		assertThat(batchProcessManager.getFinishedBatchProcesses()).containsOnly(previousBatchProcess);
	//		assertThat(nextBatchProcess.getTotalRecordsCount()).isEqualTo(2);
	//
	//	}
	//
	//	@Test
	//	public void givenErrorsWhileExecutingBatchSchemaThenErrorCounterSavedInBatchProcessListAndListOfRecordsWithErrorObtainable()
	//			throws Exception {
	//		batchProcessManager = newBatchProcessManager(zeComputer, 3);
	//		BatchProcess batchProcess = batchProcessManager.add(indexedRecords, "zeCollection", action);
	//		batchProcessManager.markAsPending(batchProcess);
	//		BatchProcess startedBatchProcess = batchProcessManager.getCurrentBatchProcess();
	//
	//		BatchProcessPart firstPart = batchProcessManager.getCurrentBatchProcessPart();
	//		batchProcessManager.markBatchProcessPartAsFinishedAndGetAnotherPart(firstPart, asList(aSecondRecordId, aThirdRecordId));
	//		assertThat(batchProcessManager.get(startedBatchProcess.getId()).getErrors()).isEqualTo(2);
	//		assertThat(batchProcessManager.getRecordsWithError(startedBatchProcess)).containsOnly(aSecondRecordId, aThirdRecordId);
	//	}

}
