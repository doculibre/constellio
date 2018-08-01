package com.constellio.app.ui.framework.builders;

import com.constellio.app.ui.entities.BatchProcessVO;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessStatus;
import com.constellio.model.entities.batchprocess.RecordBatchProcess;
import org.joda.time.LocalDateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BatchProcessToVOBuilder implements Serializable {

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
		}
		int errors = batchProcess.getErrors();
		String username = batchProcess.getUsername();
		String title = batchProcess.getTitle();
		String collection = batchProcess.getCollection();
		return new BatchProcessVO(id, status, requestDateTime, startDateTime, handledRecordsCount, totalRecordsCount, errors,
				collection, query, records, username, title);
	}

}
