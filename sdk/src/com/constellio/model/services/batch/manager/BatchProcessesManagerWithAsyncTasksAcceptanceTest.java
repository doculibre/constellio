package com.constellio.model.services.batch.manager;

import com.constellio.model.entities.batchprocess.AsyncTask;
import com.constellio.model.entities.batchprocess.AsyncTaskCreationRequest;
import com.constellio.model.entities.batchprocess.AsyncTaskExecutionParams;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.batch.controller.BatchProcessState;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.constellio.sdk.tests.TestUtils.extractingSimpleCodeAndParameters;
import static com.constellio.sdk.tests.TestUtils.extractingWarningsSimpleCodeAndParameters;
import static com.constellio.sdk.tests.TestUtils.linkEventBus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class BatchProcessesManagerWithAsyncTasksAcceptanceTest extends ConstellioTest {

	BatchProcessesManager batchProcessesManager;

	@Before
	public void setUp() {
		givenBackgroundThreadsEnabled();
		prepareSystem(withZeCollection());
		batchProcessesManager = getModelLayerFactory().getBatchProcessesManager();
	}

	public static String words = "";

	@Test
	public void whenAddingAsyncTaskInPendingThenSavedAndExecuted()
			throws Exception {
		words = "";
		givenBackgroundThreadsEnabled();

		ModelLayerFactory otherInstanceModelLayerFactory = getModelLayerFactory("other-instance");
		BatchProcessesManager otherBatchProcessManager = otherInstanceModelLayerFactory.getBatchProcessesManager();
		linkEventBus(getModelLayerFactory(), otherInstanceModelLayerFactory);

		String task1 = batchProcessesManager.addAsyncTask(
				new AsyncTaskCreationRequest(new WordAsyncTask("Hell"), zeCollection, "first task")).getId();
		String task2 = batchProcessesManager.addAsyncTask(
				new AsyncTaskCreationRequest(new WordAsyncTask("o Wo"), zeCollection, "2nd task")).getId();
		String task3 = batchProcessesManager.addAsyncTask(
				new AsyncTaskCreationRequest(new WordAsyncTask("rld!"), zeCollection, "third task")).getId();

		waitForBatchProcess();
		assertThat(words).isEqualTo("Hello World!");

		BatchProcessState task1State = batchProcessesManager.getBatchProcessState(task1);
		assertThat(extractingWarningsSimpleCodeAndParameters(task1State.getValidationErrors(), "words")).containsOnly(
				tuple("BatchProcessesManagerWithAsyncTasksAcceptanceTest$WordAsyncTask_hellIsNotARecommendedPlace", "Hell"));
		assertThat(extractingSimpleCodeAndParameters(task1State.getValidationErrors(), "words")).isEmpty();

		BatchProcessState task2State = batchProcessesManager.getBatchProcessState(task2);
		assertThat(extractingWarningsSimpleCodeAndParameters(task2State.getValidationErrors(), "words")).isEmpty();
		assertThat(extractingSimpleCodeAndParameters(task2State.getValidationErrors(), "words")).isEmpty();

		BatchProcessState task3State = batchProcessesManager.getBatchProcessState(task3);
		assertThat(extractingWarningsSimpleCodeAndParameters(task3State.getValidationErrors(), "words")).isEmpty();
		assertThat(extractingSimpleCodeAndParameters(task3State.getValidationErrors(), "words")).containsOnly(
				tuple("BatchProcessesManagerWithAsyncTasksAcceptanceTest$WordAsyncTask_ponctuationDetected", "rld!"));

		task1State = otherBatchProcessManager.getBatchProcessState(task1);
		assertThat(extractingWarningsSimpleCodeAndParameters(task1State.getValidationErrors(), "words")).containsOnly(
				tuple("BatchProcessesManagerWithAsyncTasksAcceptanceTest$WordAsyncTask_hellIsNotARecommendedPlace", "Hell"));
		assertThat(extractingSimpleCodeAndParameters(task1State.getValidationErrors(), "words")).isEmpty();

		task2State = otherBatchProcessManager.getBatchProcessState(task2);
		assertThat(extractingWarningsSimpleCodeAndParameters(task2State.getValidationErrors(), "words")).isEmpty();
		assertThat(extractingSimpleCodeAndParameters(task2State.getValidationErrors(), "words")).isEmpty();

		task3State = otherBatchProcessManager.getBatchProcessState(task3);
		assertThat(extractingWarningsSimpleCodeAndParameters(task3State.getValidationErrors(), "words")).isEmpty();
		assertThat(extractingSimpleCodeAndParameters(task3State.getValidationErrors(), "words")).containsOnly(
				tuple("BatchProcessesManagerWithAsyncTasksAcceptanceTest$WordAsyncTask_ponctuationDetected", "rld!"));

	}

	public static class WordAsyncTask implements AsyncTask {

		String wordsToAdd;

		public WordAsyncTask(String wordsToAdd) {
			this.wordsToAdd = wordsToAdd;
		}

		@Override
		public void execute(AsyncTaskExecutionParams params) {

			Map<String, Object> messageParams = new HashMap<>();
			messageParams.put("words", wordsToAdd);

			if (wordsToAdd.contains("Hell")) {
				params.logWarning("hellIsNotARecommendedPlace", messageParams);
			}

			if (wordsToAdd.contains("!")) {
				try {
					params.logError("ponctuationDetected", messageParams);
				} catch (ValidationException e) {
					throw new RuntimeException(e);
				}
			}

			System.out.println("Adding words '" + words + "'");
			BatchProcessesManagerWithAsyncTasksAcceptanceTest.words =
					BatchProcessesManagerWithAsyncTasksAcceptanceTest.words + wordsToAdd;
		}

		@Override
		public Object[] getInstanceParameters() {
			return new Object[]{wordsToAdd};
		}
	}

}
