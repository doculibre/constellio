package com.constellio.model.services.batch.state;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

public class StoredBatchProcessWriter {

	private static final String INDEX = "index";
	private static final String BATCH_PROCESS_ID = "batchProcessId";
	private static final String FIRST_ID = "firstId";
	private static final String LAST_ID = "lastId";
	private static final String FINISHED = "finished";
	private static final String STARTED = "started";
	public static final String ROOT = "root";
	Document document;

	public StoredBatchProcessWriter(Document document) {
		this.document = document;
	}

	public void createEmptyBatchProcessProgression() {
		Element root = new Element(ROOT);
		document.setRootElement(root);
	}

	public void addUpdate(StoredBatchProcessPart storedBatchProcessPart) {
		Element root = document.getRootElement();
		removeOtherBatchProcesses(storedBatchProcessPart, root);
		removeIfExists(storedBatchProcessPart, root);
		add(storedBatchProcessPart);
	}

	private void removeOtherBatchProcesses(StoredBatchProcessPart storedBatchProcessPart, Element root) {
		List<Element> elementsToRemove = new ArrayList<>();
		List<Element> batchProcessIdElements = root.getChildren(BATCH_PROCESS_ID);
		if (batchProcessIdElements != null) {
			for (Element batchProcessIdElement : batchProcessIdElements) {
				if (!batchProcessIdElement.getAttributeValue(BATCH_PROCESS_ID)
						.equals(storedBatchProcessPart.getBatchProcessId())) {
					elementsToRemove.add(batchProcessIdElement);
				}
			}
		}
		for (Element elementToRemove : elementsToRemove) {
			elementToRemove.detach();
		}
	}

	private void removeIfExists(StoredBatchProcessPart storedBatchProcessPart, Element root) {
		Element elementToRemove = null;
		Element batchProcessIdElement = root.getChild(BATCH_PROCESS_ID);
		if (batchProcessIdElement != null) {
			for (Element element : batchProcessIdElement.getChildren(INDEX)) {
				if (element.getAttributeValue(INDEX).equals(String.valueOf(storedBatchProcessPart.getIndex()))) {
					elementToRemove = element;
					break;
				}
			}
		}
		if (elementToRemove != null) {
			elementToRemove.detach();
		}
	}

	private void add(StoredBatchProcessPart storedBatchProcessPart) {

		Element firstIdElement = new Element(FIRST_ID);
		firstIdElement.setText(storedBatchProcessPart.getFirstId());

		Element lastIdElement = new Element(LAST_ID);
		lastIdElement.setText(storedBatchProcessPart.getFirstId());

		Element startedElement = new Element(STARTED);
		startedElement.setText(String.valueOf(storedBatchProcessPart.isStarted()));

		Element finishedElement = new Element(FINISHED);
		finishedElement.setText(String.valueOf(storedBatchProcessPart.isFinished()));

		Element indexElement = new Element(INDEX);
		indexElement.setAttribute(INDEX, String.valueOf(storedBatchProcessPart.getIndex()));

		indexElement.addContent(firstIdElement);
		indexElement.addContent(lastIdElement);
		indexElement.addContent(startedElement);
		indexElement.addContent(finishedElement);

		Element batchProcessIdElement = null;
		boolean exists = false;
		for (Element element : document.getRootElement().getChildren(BATCH_PROCESS_ID)) {
			if (element.getAttributeValue(BATCH_PROCESS_ID).equals(storedBatchProcessPart.getBatchProcessId())) {
				exists = true;
				batchProcessIdElement = element;
				break;
			}
		}
		if (!exists) {
			batchProcessIdElement = new Element(BATCH_PROCESS_ID);
			batchProcessIdElement.setAttribute(BATCH_PROCESS_ID, storedBatchProcessPart.getBatchProcessId());
		}
		batchProcessIdElement.addContent(indexElement);

		if (!exists) {
			document.getRootElement().addContent(batchProcessIdElement);
		}
	}
}
