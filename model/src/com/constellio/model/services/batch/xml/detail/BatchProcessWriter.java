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
package com.constellio.model.services.batch.xml.detail;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;
import org.jdom2.util.IteratorIterable;
import org.joda.time.LocalDateTime;

import com.constellio.model.services.batch.xml.detail.BatchProcessWriterRuntimeException.ComputerNotFound;

public class BatchProcessWriter {

	private static final String COMPUTER_NAME = "computerName";
	private static final String BATCH_PROCESS_PART = "batchProcessPart";
	private static final String ERRORS = "errors";
	private static final String ID = "id";
	private static final String REQUEST_DATE_TIME = "requestDateTime";
	private static final String RECORDS = "records";
	private static final String RECORD = "record";
	private static final String BATCH_PROCESS = "batchProcess";
	private final Document document;

	public BatchProcessWriter(Document document) {
		this.document = document;
	}

	public void newBatchProcess(String id, LocalDateTime requestDateTime, List<String> records) {
		Element recordsElement = new Element(RECORDS);
		addErrorsToDocument(records, recordsElement);

		Element requestDateTimeElement = new Element(REQUEST_DATE_TIME).setText(requestDateTime.toString());
		Element idElement = new Element(ID).setText(id);
		Element errorsElement = new Element(ERRORS);

		Element batchProcessElement = new Element(BATCH_PROCESS);
		batchProcessElement.addContent(idElement);
		batchProcessElement.addContent(requestDateTimeElement);
		batchProcessElement.addContent(recordsElement);
		batchProcessElement.addContent(errorsElement);

		document.setRootElement(batchProcessElement);
	}

	public List<String> assignBatchProcessPartTo(String computerName, int quantityRecordsToAssign) {
		List<String> recordsBatchPart = new ArrayList<>();
		Element batchProcessElement = document.getRootElement();
		for (Element batchProcessPart : getBatchProcessPartElements()) {
			if (batchProcessPart.getAttributeValue(COMPUTER_NAME).equals(computerName)) {
				throw new BatchProcessWriterRuntimeException.AlreadyProcessingABatchProcessPart(computerName);
			}
		}

		Element recordsBatchPartElement = assignRecordsToBatchProcessPart(batchProcessElement, quantityRecordsToAssign,
				recordsBatchPart);

		if (!recordsBatchPart.isEmpty()) {
			Element batchProcessPartElement = new Element(BATCH_PROCESS_PART).setAttribute(COMPUTER_NAME, computerName);

			batchProcessPartElement.addContent(recordsBatchPartElement);
			batchProcessElement.addContent(batchProcessPartElement);
		}
		return recordsBatchPart;
	}

	private Element assignRecordsToBatchProcessPart(Element batchProcessElement, int quantityRecordsToAssign,
			List<String> recordsBatchPart) {
		List<Element> recordsElements = getRecordsElements(batchProcessElement);
		int toIndex = recordsElements.size() >= quantityRecordsToAssign ? quantityRecordsToAssign : recordsElements.size();
		recordsElements = recordsElements.subList(0, toIndex);
		Element recordsBatchPartElement = new Element(RECORDS);
		for (Element recordElement : recordsElements) {
			recordElement.detach();
			recordsBatchPartElement.addContent(recordElement.detach());
			recordsBatchPart.add(recordElement.getText());
		}
		return recordsBatchPartElement;
	}

	public void markHasDone(String computerName, List<String> recordsWithErrors)
			throws ComputerNotFound {
		Element batchProcessElement = document.getRootElement();
		removeBatchPartFromDocument(computerName);

		Element errorsElement = batchProcessElement.getChild(ERRORS);
		addErrorsToDocument(recordsWithErrors, errorsElement);
	}

	private void addErrorsToDocument(List<String> recordsWithErrors, Element errorsElement) {
		for (String recordWithError : recordsWithErrors) {
			Element record = new Element(RECORD).setText(recordWithError);
			errorsElement.addContent(record);
		}
	}

	private void removeBatchPartFromDocument(String computerName)
			throws ComputerNotFound {
		boolean found = false;
		for (Element batchProcessPart : getBatchProcessPartElements()) {
			if (batchProcessPart.getAttributeValue(COMPUTER_NAME).equals(computerName)) {
				found = true;
				batchProcessPart.detach();
			}
		}
		if (!found) {
			throw new BatchProcessWriterRuntimeException.ComputerNotFound(computerName);
		}
	}

	@SuppressWarnings("unchecked")
	private List<Element> getRecordsElements(Element batchProcessElement) {
		Filter<Element> filters = Filters.element(RECORD);
		IteratorIterable<Element> recordsElement = batchProcessElement.getChild(RECORDS).getDescendants(filters);
		List<Element> recordsElements = IteratorUtils.toList(recordsElement);
		return recordsElements;
	}

	private List<Element> getBatchProcessPartElements() {
		Element batchProcessElement = document.getRootElement();
		Filter<Element> filters = Filters.element(BATCH_PROCESS_PART);
		IteratorIterable<Element> batchProcessPartElement = batchProcessElement.getDescendants(filters);
		List<Element> batchProcessPartElements = IteratorUtils.toList(batchProcessPartElement);
		return batchProcessPartElements;
	}
}
