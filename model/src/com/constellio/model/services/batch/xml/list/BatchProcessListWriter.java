package com.constellio.model.services.batch.xml.list;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;
import org.jdom2.util.IteratorIterable;
import org.joda.time.LocalDateTime;

import com.constellio.model.entities.batchprocess.AsyncTaskCreationRequest;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.batchprocess.RecordBatchProcess;
import com.constellio.model.utils.ParametrizedInstanceUtils;

public class BatchProcessListWriter {

	private static final String ID = "id";
	private static final String RECORDS = "records";
	private static final String QUERY = "query";
	private static final String ERRORS = "errors";
	private static final String REQUEST_DATE_TIME = "requestDateTime";
	private static final String ACTION = "action";
	private static final String RECORDS_COUNT = "recordsCount";
	private static final String PROGRESSION = "progression";
	private static final String COLLECTION = "collection";
	private static final String USERNAME = "username";
	private static final String TITLE = "title";
	private static final String BATCH_PROCESS = "batchProcess";
	private static final String STANDBY_BATCH_PROCESSES = "standbyBatchProcesses";
	private static final String PENDING_BATCH_PROCESSES = "pendingBatchProcesses";
	private static final String CURRENT_BATCH_PROCESS = "currentBatchProcess";
	private static final String PREVIOUS_BATCH_PROCESSES = "previousBatchProcesses";

	private final Document document;

	public BatchProcessListWriter(Document document) {
		this.document = document;
	}

	public void createEmptyProcessList() {
		document.setRootElement(new Element("root"));
		document.getRootElement().addContent(new Element(STANDBY_BATCH_PROCESSES));
		document.getRootElement().addContent(new Element(PREVIOUS_BATCH_PROCESSES));
		document.getRootElement().addContent(new Element(CURRENT_BATCH_PROCESS));
		document.getRootElement().addContent(new Element(PENDING_BATCH_PROCESSES));
	}

	public void addRecordBatchProcess(String id, String query, String collection, LocalDateTime requestDateTime, int recordsCount,
			BatchProcessAction batchProcessAction) {
		Element actionElement = newParametrizedInstanceUtils().toElement(batchProcessAction, ACTION);

		Element requestDateTimeElement = new Element(REQUEST_DATE_TIME).setText(requestDateTime.toString());
		Element recordsCountElement = new Element(RECORDS_COUNT).setText(String.valueOf(recordsCount));
		Element collectionElement = new Element(COLLECTION).setText(collection);
		Element queryElement = new Element(QUERY).setText(query);

		Element batchProcess = new Element(BATCH_PROCESS).setAttribute(ID, id);
		batchProcess.addContent(requestDateTimeElement);
		batchProcess.addContent(recordsCountElement);
		batchProcess.addContent(actionElement);
		batchProcess.addContent(queryElement);
		batchProcess.addContent(collectionElement);

		Element pendingBatchProcessesElement = document.getRootElement().getChild(STANDBY_BATCH_PROCESSES).detach();
		pendingBatchProcessesElement.addContent(batchProcess);
		document.getRootElement().addContent(pendingBatchProcessesElement);
	}

	public void addRecordBatchProcess(String id, String query, String collection, LocalDateTime requestDateTime, int recordsCount,
			BatchProcessAction batchProcessAction, String username, String title) {
		Element actionElement = newParametrizedInstanceUtils().toElement(batchProcessAction, ACTION);

		Element requestDateTimeElement = new Element(REQUEST_DATE_TIME).setText(requestDateTime.toString());
		Element recordsCountElement = new Element(RECORDS_COUNT).setText(String.valueOf(recordsCount));
		Element collectionElement = new Element(COLLECTION).setText(collection);
		Element queryElement = new Element(QUERY).setText(query);
		Element batchProcess = new Element(BATCH_PROCESS).setAttribute(ID, id);
		Element usernameElement = new Element(USERNAME).setText(username);
		Element titleElement = new Element(TITLE).setText(title);

		batchProcess.addContent(requestDateTimeElement);
		batchProcess.addContent(recordsCountElement);
		batchProcess.addContent(actionElement);
		batchProcess.addContent(queryElement);
		batchProcess.addContent(collectionElement);
		batchProcess.addContent(usernameElement);
		batchProcess.addContent(titleElement);

		Element pendingBatchProcessesElement = document.getRootElement().getChild(STANDBY_BATCH_PROCESSES).detach();
		pendingBatchProcessesElement.addContent(batchProcess);
		document.getRootElement().addContent(pendingBatchProcessesElement);
	}

	public void addRecordBatchProcess(String id, List<String> records, String collection, LocalDateTime requestDateTime,
			int recordsCount,
			BatchProcessAction batchProcessAction, String username, String title) {
		Element actionElement = newParametrizedInstanceUtils().toElement(batchProcessAction, ACTION);

		Element requestDateTimeElement = new Element(REQUEST_DATE_TIME).setText(requestDateTime.toString());
		Element recordsCountElement = new Element(RECORDS_COUNT).setText(String.valueOf(recordsCount));
		Element collectionElement = new Element(COLLECTION).setText(collection);
		Element queryElement = new Element(RECORDS).setText(records.toString());
		Element batchProcess = new Element(BATCH_PROCESS).setAttribute(ID, id);
		Element usernameElement = new Element(USERNAME).setText(username);
		Element titleElement = new Element(TITLE).setText(title);

		batchProcess.addContent(requestDateTimeElement);
		batchProcess.addContent(recordsCountElement);
		batchProcess.addContent(actionElement);
		batchProcess.addContent(queryElement);
		batchProcess.addContent(collectionElement);
		batchProcess.addContent(usernameElement);
		batchProcess.addContent(titleElement);

		Element pendingBatchProcessesElement = document.getRootElement().getChild(STANDBY_BATCH_PROCESSES).detach();
		pendingBatchProcessesElement.addContent(batchProcess);
		document.getRootElement().addContent(pendingBatchProcessesElement);
	}

	@SuppressWarnings("unchecked")
	public void markBatchProcessAsFinished(BatchProcess batchProcess, int errors) {
		boolean found = false;

		Filter<Element> filters = Filters.element(BATCH_PROCESS);

		IteratorIterable<Element> batchProcesses = document.getRootElement().getDescendants(filters);
		List<Element> copyBatchProcesses = IteratorUtils.toList(batchProcesses);

		for (Element nextBatchProcess : copyBatchProcesses) {
			if (found = markBatchProcessAsFinished(batchProcess, errors, nextBatchProcess)) {
				break;
			}
		}
		if (!found) {
			throw new BatchProcessListWriterRuntimeException.BatchProcessNotFound(batchProcess.getId());
		}
	}

	@SuppressWarnings("unchecked")
	private Element getBatchProcessElementWithId(String id) {
		Filter<Element> filters = Filters.element(BATCH_PROCESS);
		IteratorIterable<Element> batchProcesses = document.getRootElement().getChild(CURRENT_BATCH_PROCESS)
				.getDescendants(filters);
		List<Element> copyBatchProcesses = IteratorUtils.toList(batchProcesses);
		for (Element nextBatchProcess : copyBatchProcesses) {
			if (nextBatchProcess.getAttributeValue(ID).equals(id)) {
				return nextBatchProcess;
			}
		}
		throw new BatchProcessListWriterRuntimeException.BatchProcessNotFound(id);
	}

	public void incrementProgression(RecordBatchProcess batchProcess, int progressionIncrement, int errorsIncrement) {
		String recordsCount = null;

		Element batchProcessElement = getBatchProcessElementWithId(batchProcess.getId());

		recordsCount = batchProcessElement.getChild(RECORDS_COUNT).getText();
		int progressionCount = incrementCounter(PROGRESSION, progressionIncrement, batchProcessElement);
		int errorsCount = incrementCounter(ERRORS, errorsIncrement, batchProcessElement);

		if (recordsCount != null && progressionCount == Integer.parseInt(recordsCount)) {
			markBatchProcessAsFinished(batchProcess, errorsCount);
		} else {
			batchProcessElement.detach();
			document.getRootElement().getChild(CURRENT_BATCH_PROCESS).addContent(batchProcessElement);
		}
	}

	private int incrementCounter(String tagName, int increment, Element batchProcessElement) {
		int count;
		Element counterElement = batchProcessElement.getChild(tagName);
		if (counterElement == null) {
			count = increment;
			counterElement = new Element(tagName).setText(String.valueOf(count));

		} else {
			counterElement = counterElement.detach();
			count = Integer.parseInt(counterElement.getText()) + increment;
			counterElement.setText(String.valueOf(count));
		}
		batchProcessElement.addContent(counterElement);
		return count;
	}

	public void startNextBatchProcess(LocalDateTime startDate) {
		Element nextBatchProcesses = document.getRootElement().getChild(PENDING_BATCH_PROCESSES).getChild(BATCH_PROCESS);
		if (nextBatchProcesses != null) {
			nextBatchProcesses = nextBatchProcesses.detach();
			Element startDateTime = new Element("startDateTime").setText(startDate.toString());
			nextBatchProcesses.addContent(startDateTime);
			Element currentBatchProcess = document.getRootElement().getChild(CURRENT_BATCH_PROCESS).detach();
			if (currentBatchProcess.getChildren().size() == 0) {
				currentBatchProcess.addContent(nextBatchProcesses);
				document.getRootElement().addContent(currentBatchProcess);
			} else {
				document.getRootElement().addContent(currentBatchProcess);
				document.getRootElement().getChild(PENDING_BATCH_PROCESSES).addContent(nextBatchProcesses);
				throw new BatchProcessListWriterRuntimeException.CannotHaveTwoBatchProcessInCurrentBatchProcessList();
			}
		} else {
			throw new BatchProcessListWriterRuntimeException.NoPendingBatchProcessesInList();
		}
	}

	private boolean markBatchProcessAsFinished(BatchProcess batchProcess, int errors, Element nextBatchProcess) {
		if (nextBatchProcess.getAttributeValue(ID).equals(batchProcess.getId())
				&& nextBatchProcess.getParentElement().getName().equals(PREVIOUS_BATCH_PROCESSES)) {
			throw new BatchProcessListWriterRuntimeException.BatchProcessAlreadyFinished();
		} else if (nextBatchProcess.getAttributeValue(ID).equals(batchProcess.getId())) {
			Element errorsElement = setErrorsElementValue(errors, nextBatchProcess);
			nextBatchProcess.addContent(errorsElement);
			nextBatchProcess.detach();
			document.getRootElement().getChild(PREVIOUS_BATCH_PROCESSES).addContent(nextBatchProcess);
			return true;
		}
		return false;
	}

	private Element setErrorsElementValue(int errors, Element nextBatchProcess) {
		Element errorsElement = nextBatchProcess.getChild(ERRORS);
		if (errorsElement == null) {
			errorsElement = new Element(ERRORS).setText(String.valueOf(errors));
		} else {
			errorsElement = errorsElement.detach();
			errorsElement.setText(String.valueOf(errors));
		}
		return errorsElement;
	}

	public ParametrizedInstanceUtils newParametrizedInstanceUtils() {
		return new ParametrizedInstanceUtils();
	}

	public void markBatchProcessAsPending(String batchProcessId) {
		List<Element> standbyBatchProcesses = document.getRootElement().getChild(STANDBY_BATCH_PROCESSES)
				.getChildren(BATCH_PROCESS);
		for (Element standbyBatchProcess : standbyBatchProcesses) {
			if (standbyBatchProcess.getAttributeValue(ID).equals(batchProcessId)) {
				standbyBatchProcess.detach();
				document.getRootElement().getChild(PENDING_BATCH_PROCESSES).addContent(standbyBatchProcess);
				break;
			}
		}
	}

	public void cancelStandByBatchProcess(String batchProcessId) {
		List<Element> standbyBatchProcesses = document.getRootElement().getChild(STANDBY_BATCH_PROCESSES)
				.getChildren(BATCH_PROCESS);
		for (Element standbyBatchProcess : standbyBatchProcesses) {
			if (standbyBatchProcess.getAttributeValue(ID).equals(batchProcessId)) {
				standbyBatchProcess.detach();
				break;
			}
		}
	}

	public void markAllBatchProcessAsPending() {
		Element standbyListElement = document.getRootElement().getChild(STANDBY_BATCH_PROCESSES);

		List<Element> standbyBatchProcesses = new ArrayList<>(standbyListElement.getChildren(BATCH_PROCESS));
		for (Element standbyBatchProcess : standbyBatchProcesses) {
			standbyBatchProcess.detach();
			document.getRootElement().getChild(PENDING_BATCH_PROCESSES).addContent(standbyBatchProcess);
		}
	}

	public void deleteBatchProcessesAlteration(List<String> ids) {
		Element finishedBatchProcessesElement = document.getRootElement().getChild(PREVIOUS_BATCH_PROCESSES);
		List<Element> elementsToDelete = new ArrayList<>();
		for (Element batchProcessElement : finishedBatchProcessesElement.getChildren(BATCH_PROCESS)) {
			String batchProcessId = batchProcessElement.getAttributeValue(ID);
			if (ids.contains(batchProcessId)) {
				elementsToDelete.add(batchProcessElement);
			}
		}
		for (Element element : elementsToDelete) {
			element.detach();
		}
	}

	public void addAsyncTaskBatchProcess(String id, LocalDateTime requestDateTime, AsyncTaskCreationRequest request) {

		Element actionElement = newParametrizedInstanceUtils().toElement(request.getTask(), ACTION);

		Element requestDateTimeElement = new Element(REQUEST_DATE_TIME).setText(requestDateTime.toString());
		Element collectionElement = new Element(COLLECTION).setText(request.getCollection());
		Element batchProcess = new Element(BATCH_PROCESS);
		batchProcess.setAttribute(ID, id);
		batchProcess.setAttribute("type", "task");
		Element usernameElement = new Element(USERNAME).setText(request.getUsername());
		Element titleElement = new Element(TITLE).setText(request.getTitle());

		batchProcess.addContent(requestDateTimeElement);
		batchProcess.addContent(actionElement);
		batchProcess.addContent(collectionElement);
		batchProcess.addContent(usernameElement);
		batchProcess.addContent(titleElement);

		Element pendingBatchProcessesElement = document.getRootElement().getChild(STANDBY_BATCH_PROCESSES).detach();
		pendingBatchProcessesElement.addContent(batchProcess);
		document.getRootElement().addContent(pendingBatchProcessesElement);

	}
}
