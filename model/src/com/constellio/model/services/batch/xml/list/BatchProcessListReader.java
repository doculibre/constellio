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
package com.constellio.model.services.batch.xml.list;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;
import org.jdom2.util.IteratorIterable;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.batchprocess.BatchProcessStatus;
import com.constellio.model.services.batch.xml.list.BatchProcessListReaderException.NoBatchProcessesInList;
import com.constellio.model.utils.ParametrizedInstanceUtils;

public class BatchProcessListReader {

	public static final BatchProcess NO_CURRENT_BATCH_PROCESS = null;
	public static final String ACTION = "action";
	private static final Logger LOGGER = LoggerFactory.getLogger(BatchProcessListReader.class);
	private static final String PROGRESSION = "progression";
	private static final String RECORDS_COUNT = "recordsCount";
	private static final String REQUEST_DATE_TIME = "requestDateTime";
	private static final String START_DATE_TIME = "startDateTime";
	private static final String ERRORS = "errors";
	private static final String ID = "id";
	private static final String COLLECTION = "collection";
	private static final String BATCH_PROCESS = "batchProcess";
	private static final String STANDBY_BATCH_PROCESSES = "standbyBatchProcesses";
	private static final String PENDING_BATCH_PROCESSES = "pendingBatchProcesses";
	private static final String CURRENT_BATCH_PROCESS = "currentBatchProcess";
	private static final String PREVIOUS_BATCH_PROCESSES = "previousBatchProcesses";
	private final Document document;

	public BatchProcessListReader(Document document) {
		this.document = document;
	}

	public BatchProcess read(String id) {
		BatchProcess batchProcess = null;
		Filter<Element> filters = Filters.element(BATCH_PROCESS);
		IteratorIterable<Element> batchProcesses = document.getRootElement().getDescendants(filters);
		while (batchProcess == null && batchProcesses.hasNext()) {
			Element nextBatchProcess = batchProcesses.next();
			if (nextBatchProcess.getAttributeValue(ID).equals(id)) {
				batchProcess = toBatchProcess(id, nextBatchProcess);
			}
		}
		if (batchProcess == null) {
			throw new BatchProcessListReaderRuntimeException.NoBatchProcessesInList();
		}
		return batchProcess;
	}

	private BatchProcess toBatchProcess(String id, Element nextBatchProcess) {
		BatchProcess batchProcess;
		BatchProcessStatus status = getStatus(nextBatchProcess);
		LocalDateTime requestDateTime = getRequestDateTime(nextBatchProcess);
		LocalDateTime startDateTime = getStartDateTime(nextBatchProcess);
		int totalRecordsCount = Integer.parseInt(nextBatchProcess.getChild(RECORDS_COUNT).getText());
		int errors = getErrors(nextBatchProcess);
		int handledRecordsCount = getHandledRecords(nextBatchProcess, status, totalRecordsCount);
		String collection = getCollection(nextBatchProcess);
		BatchProcessAction batchProcessAction = getBatchProcessActions(nextBatchProcess);
		batchProcess = new BatchProcess(id, status, requestDateTime, startDateTime, handledRecordsCount,
				totalRecordsCount, errors, batchProcessAction, collection);
		return batchProcess;
	}

	private int getHandledRecords(Element nextBatchProcess, BatchProcessStatus status, int totalRecordsCount) {
		int handledRecordsCount = 0;
		if (status == BatchProcessStatus.CURRENT) {
			if (nextBatchProcess.getChild(PROGRESSION) != null && nextBatchProcess.getChild(PROGRESSION).getText() != null) {
				handledRecordsCount = Integer.parseInt(nextBatchProcess.getChild(PROGRESSION).getText());
			} else {
				handledRecordsCount = 0;
			}
		} else if (status == BatchProcessStatus.FINISHED) {
			handledRecordsCount = totalRecordsCount;
		}
		return handledRecordsCount;
	}

	public BatchProcess readCurrent() {
		try {
			return readBatchProcesses(CURRENT_BATCH_PROCESS).get(0);
		} catch (BatchProcessListReaderException.NoBatchProcessesInList e) {
			LOGGER.debug("No batch process in list of '{}", CURRENT_BATCH_PROCESS, e);
			return null;
		}
	}

	public List<BatchProcess> readPendingBatchProcesses() {
		try {
			return readBatchProcesses(PENDING_BATCH_PROCESSES);
		} catch (BatchProcessListReaderException.NoBatchProcessesInList e) {
			LOGGER.debug("No batch process in list of '{}", PENDING_BATCH_PROCESSES, e);
			return new ArrayList<BatchProcess>();
		}
	}

	public List<BatchProcess> readStandbyBatchProcesses() {
		try {
			return readBatchProcesses(STANDBY_BATCH_PROCESSES);
		} catch (BatchProcessListReaderException.NoBatchProcessesInList e) {
			LOGGER.debug("No batch process in list of '{}", STANDBY_BATCH_PROCESSES, e);
			return new ArrayList<BatchProcess>();
		}
	}

	public int getAllBatchProcessesCount() {
		int standbyCount = document.getRootElement().getChild(STANDBY_BATCH_PROCESSES).getChildren().size();
		int pendingCount = document.getRootElement().getChild(PENDING_BATCH_PROCESSES).getChildren().size();
		int finishedCount = document.getRootElement().getChild(PREVIOUS_BATCH_PROCESSES).getChildren().size();
		int currentCount = document.getRootElement().getChild(CURRENT_BATCH_PROCESS).getChildren().size();
		return standbyCount + pendingCount + finishedCount + currentCount;
	}

	public List<BatchProcess> readFinishedBatchProcesses() {
		try {
			return readBatchProcesses(PREVIOUS_BATCH_PROCESSES);
		} catch (BatchProcessListReaderException.NoBatchProcessesInList e) {
			LOGGER.debug("No batch process in list of '{}", PREVIOUS_BATCH_PROCESSES, e);
			return new ArrayList<BatchProcess>();
		}
	}

	private List<BatchProcess> readBatchProcesses(String batchProcessStatusTag)
			throws NoBatchProcessesInList {
		List<BatchProcess> batchProcesses = new ArrayList<>();
		Element pendingBatchProcessElement = document.getRootElement().getChild(batchProcessStatusTag);
		BatchProcessStatus status = getStatus(batchProcessStatusTag);

		for (Element batchProcessElement : pendingBatchProcessElement.getChildren()) {
			String id = batchProcessElement.getAttributeValue(ID);
			batchProcesses.add(toBatchProcess(id, batchProcessElement));
		}
		if (batchProcesses.isEmpty()) {
			throw new BatchProcessListReaderException.NoBatchProcessesInList(batchProcessStatusTag);
		}
		return batchProcesses;
	}

	private BatchProcessAction getBatchProcessActions(Element nextBatchProcess) {
		Element actionElement = nextBatchProcess.getChild(ACTION);
		return newParametrizedInstanceUtils().toObject(actionElement, BatchProcessAction.class);
	}

	private String getCollection(Element nextBatchProcess) {
		Element collectionElement = nextBatchProcess.getChild(COLLECTION);
		return collectionElement.getText();
	}

	private int getErrors(Element nextBatchProcess) {
		Element errorsElement = nextBatchProcess.getChild(ERRORS);
		int errorsCount = 0;
		if (errorsElement != null) {
			errorsCount = Integer.parseInt(errorsElement.getText());
		}
		return errorsCount;
	}

	private LocalDateTime getStartDateTime(Element nextBatchProcess) {
		Element startDateTimeElement = nextBatchProcess.getChild(START_DATE_TIME);
		LocalDateTime startDateTime = null;
		if (startDateTimeElement != null) {
			startDateTime = LocalDateTime.parse(startDateTimeElement.getText());
		}
		return startDateTime;
	}

	private LocalDateTime getRequestDateTime(Element nextBatchProcess) {
		Element requestDateTimeElement = nextBatchProcess.getChild(REQUEST_DATE_TIME);
		LocalDateTime requestDateTime = LocalDateTime.parse(requestDateTimeElement.getText());
		return requestDateTime;
	}

	private BatchProcessStatus getStatus(Element nextBatchProcess) {
		return getStatus(nextBatchProcess.getParentElement().getName());
	}

	private BatchProcessStatus getStatus(String statusStr) {
		BatchProcessStatus status;
		switch (statusStr) {
		case PENDING_BATCH_PROCESSES:
			status = BatchProcessStatus.PENDING;
			break;
		case CURRENT_BATCH_PROCESS:
			status = BatchProcessStatus.CURRENT;
			break;
		case PREVIOUS_BATCH_PROCESSES:
			status = BatchProcessStatus.FINISHED;
			break;
		case STANDBY_BATCH_PROCESSES:
			status = BatchProcessStatus.STANDBY;
			break;
		default:
			status = null;
			break;
		}
		return status;
	}

	ParametrizedInstanceUtils newParametrizedInstanceUtils() {
		return new ParametrizedInstanceUtils();
	}

}
