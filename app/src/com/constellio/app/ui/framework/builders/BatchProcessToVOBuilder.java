package com.constellio.app.ui.framework.builders;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.batchprocess.*;
import com.constellio.model.services.batch.controller.BatchProcessState;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import org.joda.time.LocalDateTime;

import com.constellio.app.ui.entities.BatchProcessVO;

public class BatchProcessToVOBuilder implements Serializable {

	BatchProcessesManager batchProcessesManager;

	public BatchProcessToVOBuilder(BatchProcessesManager batchProcessesManager) {
		this.batchProcessesManager = batchProcessesManager;
	}

	public BatchProcessVO build(BatchProcess batchProcess) {
		String id = batchProcess.getId();
		BatchProcessStatus status = batchProcess.getStatus();
		LocalDateTime requestDateTime = batchProcess.getRequestDateTime();
		LocalDateTime startDateTime = batchProcess.getStartDateTime();

		int handledRecordsCount = 0;
		int totalRecordsCount = 0;
		String query = null;
		List<String> records = new ArrayList<>();

		if (batchProcess instanceof RecordBatchProcess) {
			RecordBatchProcess recordBatchProcess = (RecordBatchProcess) batchProcess;
			handledRecordsCount = recordBatchProcess.getHandledRecordsCount();
			totalRecordsCount = recordBatchProcess.getTotalRecordsCount();
			query = recordBatchProcess.getQuery();
			records = recordBatchProcess.getRecords();
		} else if(batchProcess instanceof AsyncTaskBatchProcess) {
			BatchProcessState batchProcessState = batchProcessesManager.getBatchProcessState(batchProcess.getId());
			handledRecordsCount = Long.valueOf(batchProcessState.getCurrentlyProcessed()).intValue();
			totalRecordsCount = Long.valueOf(batchProcessState.getTotalToProcess()).intValue();
		}
		int errors = batchProcess.getErrors();
		String username = batchProcess.getUsername();
		String title = batchProcess.getTitle();
		String collection = batchProcess.getCollection();
		return new BatchProcessVO(id, status, requestDateTime, startDateTime, handledRecordsCount, totalRecordsCount, errors,
				collection, query, records, username, title);
	}

}
