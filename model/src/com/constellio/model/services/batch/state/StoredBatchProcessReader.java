package com.constellio.model.services.batch.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;

public class StoredBatchProcessReader {

	private static final String INDEX = "index";
	private static final String BATCH_PROCESS_ID = "batchProcessId";
	private static final String FIRST_ID = "firstId";
	private static final String LAST_ID = "lastId";
	private static final String FINISHED = "finished";
	private static final String STARTED = "started";
	Document document;

	public StoredBatchProcessReader(Document document) {
		this.document = document;
	}

	public Map<String, List<StoredBatchProcessPart>> readAll() {
		Map<String, List<StoredBatchProcessPart>> batchProcessesParts = new HashMap<>();
		Element batchProcessesIdElements = document.getRootElement();
		for (Element batchProcessesIdElement : batchProcessesIdElements.getChildren(BATCH_PROCESS_ID)) {
			String batchProcessId = batchProcessesIdElement.getAttributeValue(BATCH_PROCESS_ID);
			List<StoredBatchProcessPart> storedBatchProcessParts = createBatchProcessList(batchProcessesIdElement);
			batchProcessesParts.put(batchProcessId, storedBatchProcessParts);
		}
		return batchProcessesParts;
	}

	private List<StoredBatchProcessPart> createBatchProcessList(Element batchProcessesIdElement) {

		List<StoredBatchProcessPart> storedBatchProcessParts = new ArrayList<>();
		String batchProcessId = batchProcessesIdElement.getAttributeValue(BATCH_PROCESS_ID);
		int index = Integer.valueOf(batchProcessesIdElement.getChild(INDEX).getAttributeValue(INDEX));
		for (Element batchProcessesIndexElement : batchProcessesIdElement.getChildren(INDEX)) {
			boolean started = Boolean.valueOf(batchProcessesIndexElement.getChildText(STARTED));
			boolean finished = Boolean.valueOf(batchProcessesIndexElement.getChildText(FINISHED));
			String firstId = batchProcessesIndexElement.getChildText(FIRST_ID);
			String lastId = batchProcessesIndexElement.getChildText(LAST_ID);

			StoredBatchProcessPart storedBatchProcessPart = new StoredBatchProcessPart(batchProcessId, index, firstId, lastId,
					finished, started);
			storedBatchProcessParts.add(storedBatchProcessPart);
		}
		return storedBatchProcessParts;
	}
}
