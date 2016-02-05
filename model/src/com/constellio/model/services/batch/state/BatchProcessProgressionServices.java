package com.constellio.model.services.batch.state;

import java.util.List;

import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.services.batch.state.BatchProcessProgressionServicesException.BatchProcessProgressionServicesException_OptimisticLocking;

public interface BatchProcessProgressionServices {

	void markNewPartAsStarted(StoredBatchProcessPart part)
			throws BatchProcessProgressionServicesException_OptimisticLocking;

	void markPartAsFinished(StoredBatchProcessPart part);

	void markStartedPartsHasInStandby();

	List<StoredBatchProcessPart> getPartsInStandby(BatchProcess batchProcess);

	StoredBatchProcessPart getLastBatchProcessPart(BatchProcess batchProcess);

}
