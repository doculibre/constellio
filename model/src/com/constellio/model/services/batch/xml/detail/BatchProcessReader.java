package com.constellio.model.services.batch.xml.detail;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

public class BatchProcessReader {

	private static final String ERRORS = "errors";
	private static final String RECORDS = "records";

	private final Document document;

	public BatchProcessReader(Document document) {
		this.document = document;
	}

	public List<String> getRecordsWithError() {
		List<String> recordsWithError = new ArrayList<>();
		Element batchProcessElement = document.getRootElement();
		List<Element> recordElements = batchProcessElement.getChild(ERRORS).getChildren();
		for (Element recordElement : recordElements) {
			recordsWithError.add(recordElement.getText());
		}
		return recordsWithError;
	}

	public List<String> getRecords() {
		List<String> records = new ArrayList<>();
		Element batchProcessElement = document.getRootElement();
		List<Element> recordElements = batchProcessElement.getChild(RECORDS).getChildren();
		for (Element recordElement : recordElements) {
			records.add(recordElement.getText());
		}
		return records;
	}
}
