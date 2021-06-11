package com.constellio.model.services.records;

import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.schemas.ModificationImpact;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
public class TransactionResponse {

	@Getter
	String transactionId;

	@Getter
	List<BatchProcess> createdBatchProcesses;

	@Getter
	List<ModificationImpact> impactsToHandle;


}
