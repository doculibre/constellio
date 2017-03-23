package com.constellio.app.ui.framework.builders;

import java.io.Serializable;
import java.util.List;

import org.joda.time.LocalDateTime;

import com.constellio.app.ui.entities.BatchProcessVO;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessStatus;

public class BatchProcessToVOBuilder implements Serializable {

	public BatchProcessVO build(BatchProcess batchProcess) {
		String id = batchProcess.getId();
		BatchProcessStatus status = batchProcess.getStatus();
		LocalDateTime requestDateTime = batchProcess.getRequestDateTime();
		LocalDateTime startDateTime = batchProcess.getStartDateTime();
		int handledRecordsCount = batchProcess.getHandledRecordsCount();
		int totalRecordsCount = batchProcess.getTotalRecordsCount();
		int errors = batchProcess.getErrors();
		String username = batchProcess.getUsername();
		String title = batchProcess.getTitle();
		String collection = batchProcess.getCollection();
		String query = batchProcess.getQuery();
		List<String> records = batchProcess.getRecords();
		return new BatchProcessVO(id, status, requestDateTime, startDateTime, handledRecordsCount, totalRecordsCount, errors, collection, query, records, username, title);
	}

}
