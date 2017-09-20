package com.constellio.model.services.batch.manager;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.batchprocess.AsyncTask;
import com.constellio.model.entities.batchprocess.AsyncTaskCreationRequest;
import com.constellio.model.entities.batchprocess.AsyncTaskExecutionParams;
import com.constellio.sdk.tests.ConstellioTest;

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
		batchProcessesManager.addAsyncTask(new AsyncTaskCreationRequest(new WordAsyncTask("Hell"), zeCollection, "first task"));
		batchProcessesManager.addAsyncTask(new AsyncTaskCreationRequest(new WordAsyncTask("o Wo"), zeCollection, "2nd task"));
		batchProcessesManager.addAsyncTask(new AsyncTaskCreationRequest(new WordAsyncTask("rld!"), zeCollection, "third task"));

		waitForBatchProcess();
		assertThat(words).isEqualTo("Hello World!");

	}

	public static class WordAsyncTask implements AsyncTask {

		String wordsToAdd;

		public WordAsyncTask(String wordsToAdd) {
			this.wordsToAdd = wordsToAdd;
		}

		@Override
		public void execute(AsyncTaskExecutionParams params) {

			System.out.println("Adding words '" + words + "'");
			BatchProcessesManagerWithAsyncTasksAcceptanceTest.words =
					BatchProcessesManagerWithAsyncTasksAcceptanceTest.words + wordsToAdd;
		}

		@Override
		public Object[] getInstanceParameters() {
			return new Object[] { wordsToAdd };
		}
	}

}
