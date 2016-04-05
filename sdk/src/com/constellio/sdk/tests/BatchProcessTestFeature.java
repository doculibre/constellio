package com.constellio.sdk.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessStatus;
import com.constellio.model.services.batch.manager.BatchProcessesManager;

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
		BatchProcessesManager batchProcessesManager = factoriesTestFeatures.newModelServicesFactory().getBatchProcessesManager();
		boolean batchProcessRuntimeActionExecuted = false;

		List<BatchProcess> batchProcesses = batchProcessesManager.getAllNonFinishedBatchProcesses();

		for (BatchProcess batchProcess : batchProcesses) {
			if (batchProcess != null) {
				while (batchProcessesManager.get(batchProcess.getId()).getStatus() != BatchProcessStatus.FINISHED) {

					if (batchProcessRuntimeActionExecuted == false) {
						batchProcessRuntimeActionExecuted = true;
						if (batchProcessRuntimeAction != null) {
							batchProcessRuntimeAction.run();
						}
					}

					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
				assertThat(batchProcessesManager.get(batchProcess.getId()).getErrors()).isEqualTo(0);
			}
		}

		if (!acceptErrors) {
			for (BatchProcess batchProcess : batchProcessesManager.getFinishedBatchProcesses()) {
				assertThat(batchProcess.getErrors()).isZero()
						.describedAs("Errors during batch process '" + batchProcess.getId() + "'");
			}
		}

		factoriesTestFeatures.getConstellioFactories().getModelLayerFactory().newRecordServices().flush();
	}

	public void afterTest() {
		if (waitForBatchProcessAfterTest && factoriesTestFeatures.isInitialized()) {
			waitForAllBatchProcesses(null, false);
		}
		//		if (started) {
		//
		//			BatchProcessController controller = factoriesTestFeatures.getModelLayerFactory().getBatchProcessesController();
		//			controller.close();
		//			started = false;
		//		}
	}
}
