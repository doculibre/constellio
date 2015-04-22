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
package com.constellio.sdk.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessStatus;
import com.constellio.model.services.batch.manager.BatchProcessesManager;

public class BatchProcessTestFeature {

	private static int totalTime = 0;
	boolean started = false;
	FactoriesTestFeatures factoriesTestFeatures;

	public BatchProcessTestFeature(FactoriesTestFeatures factoriesTestFeatures) {
		this.factoriesTestFeatures = factoriesTestFeatures;
	}

	public void waitForAllBatchProcessesAndEnsureNoErrors(Runnable batchProcessRuntimeAction) {
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

		for (BatchProcess batchProcess : batchProcessesManager.getFinishedBatchProcesses()) {
			assertThat(batchProcess.getErrors()).isZero()
					.describedAs("Errors during batch process '" + batchProcess.getId() + "'");
		}

		factoriesTestFeatures.getConstellioFactories().getModelLayerFactory().newRecordServices().flush();
	}

	public void afterTest() {
		//		if (started) {
		//
		//			BatchProcessController controller = factoriesTestFeatures.getModelLayerFactory().getBatchProcessesController();
		//			controller.close();
		//			started = false;
		//		}
	}
}
