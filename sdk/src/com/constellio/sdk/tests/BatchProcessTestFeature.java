package com.constellio.sdk.tests;

import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessStatus;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.batch.xml.list.BatchProcessListReaderRuntimeException;

import java.util.List;

import static com.constellio.sdk.tests.SDKConstellioFactoriesInstanceProvider.DEFAULT_NAME;

public class BatchProcessTestFeature {

	public boolean waitForBatchProcessAfterTest = true;
	private static int totalTime = 0;
	boolean started = false;
	FactoriesTestFeatures factoriesTestFeatures;

	public BatchProcessTestFeature(FactoriesTestFeatures factoriesTestFeatures) {
		this.factoriesTestFeatures = factoriesTestFeatures;
	}

	public void waitForAllBatchProcessesAcceptingErrors(Runnable batchProcessRuntimeAction) {
		waitForAllBatchProcesses(batchProcessRuntimeAction, true);
	}

	public void waitForAllBatchProcessesAndEnsureNoErrors(Runnable batchProcessRuntimeAction) {
		waitForAllBatchProcesses(batchProcessRuntimeAction, false);
	}

	public void waitForAllBatchProcesses(Runnable batchProcessRuntimeAction, boolean acceptErrors) {
		waitForAllBatchProcesses(batchProcessRuntimeAction, acceptErrors, -1);
	}

	public void waitForAllBatchProcesses(Runnable batchProcessRuntimeAction, boolean acceptErrors, long timeout) {
		BatchProcessesManager batchProcessesManager = factoriesTestFeatures.newModelServicesFactory(DEFAULT_NAME)
				.getBatchProcessesManager();
		boolean batchProcessRuntimeActionExecuted = false;
		if (!ConstellioTest.IS_FIRST_EXECUTED_TEST) {
			batchProcessesManager.waitUntilAllFinished(timeout);
		}
		List<BatchProcess> batchProcesses = batchProcessesManager.getAllNonFinishedBatchProcesses();
		batchProcesses.addAll(batchProcessesManager.getFinishedBatchProcesses());

		//		batchProcessesManager.waitUntilAllFinished();

		int errorsCount = 0;
		for (BatchProcess batchProcess : batchProcesses) {
			if (batchProcess != null) {
				try {
					while (batchProcessesManager.get(batchProcess.getId()).getStatus() != BatchProcessStatus.FINISHED) {

						if (batchProcessRuntimeActionExecuted == false) {
							batchProcessRuntimeActionExecuted = true;
							if (batchProcessRuntimeAction != null) {
								batchProcessRuntimeAction.run();
							}
						}

						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					}
				} catch (BatchProcessListReaderRuntimeException.NoBatchProcessesInList e) {
					//Mysteriously disapeared
				}
				errorsCount += batchProcessesManager.get(batchProcess.getId()).getErrors();

			}
		}

		if (!acceptErrors) {

			if (errorsCount != 0) {
				ConstellioTest.getInstance().setFailMessage("Some batch processes have errors");
			}

			//assertThat(errorsCount).isZero();
			for (BatchProcess batchProcess : batchProcessesManager.getFinishedBatchProcesses()) {
				//				assertThat(batchProcess.getErrors()).isZero()
				//						.describedAs("Errors during batch process '" + batchProcess.getId() + "'");
			}
		}

		factoriesTestFeatures.getConstellioFactories().getModelLayerFactory().newRecordServices().flush();
	}

	public void afterTest() {
		if (waitForBatchProcessAfterTest && factoriesTestFeatures.isInitialized()) {
			factoriesTestFeatures.getConstellioFactories().getDataLayerFactory().getDataLayerLogger()
					.setPrintAllQueriesLongerThanMS(10000);
			factoriesTestFeatures.getConstellioFactories().getDataLayerFactory().getDataLayerLogger().setQueryDebuggingMode(false);
			waitForAllBatchProcesses(null, false, 2000);
		}
		//		if (started) {
		//
		//			BatchProcessController controller = factoriesTestFeatures.getModelLayerFactory().getBatchProcessesController();
		//			controller.close();
		//			started = false;
		//		}
	}
}
