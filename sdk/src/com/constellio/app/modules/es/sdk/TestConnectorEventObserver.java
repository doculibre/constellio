package com.constellio.app.modules.es.sdk;

import com.constellio.app.modules.es.connectors.spi.ConnectorEventObserver;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.crawler.DeleteEventOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class TestConnectorEventObserver implements ConnectorEventObserver {

	ConnectorEventObserver nestedObserver;

	ESSchemasRecordsServices es;

	private List<TestConnectorEvent> events = new ArrayList<>();
	private int addNewDocumentCount;
	private int updateDocumentCount;
	private int deleteDocumentCount;

	public TestConnectorEventObserver(ESSchemasRecordsServices es, ConnectorEventObserver nestedObserver) {
		this.nestedObserver = nestedObserver;
		this.es = es;
	}

	public List<TestConnectorEvent> getEvents() {
		return events;
	}

	int quantityBeforeClosing = -1;

	public int getQuantityBeforeClosing() {
		return quantityBeforeClosing;
	}

	public TestConnectorEventObserver setQuantityBeforeClosing(int quantityBeforeClosing) {
		this.quantityBeforeClosing = quantityBeforeClosing;
		return this;
	}

	@Override
	public void addUpdateEvents(ConnectorDocument... documents) {
		addUpdateEvents(asList(documents));
	}

	@Override
	public void addUpdateEvents(List<ConnectorDocument> documents) {
		for (ConnectorDocument document : documents) {
			if (document.getWrappedRecord().isSaved()) {
				events.add(TestConnectorEvent.modifyEvent(document));
				updateDocumentCount++;
			} else {
				events.add(TestConnectorEvent.addEvent(document));
				addNewDocumentCount++;
			}
		}
		nestedObserver.addUpdateEvents(documents);
	}

	@Override
	public void deleteEvents(ConnectorDocument... documents) {
		deleteEvents(new DeleteEventOptions(), asList(documents));
	}

	@Override
	public void deleteEvents(DeleteEventOptions deleteEventOptions, ConnectorDocument... documents) {
		deleteEvents(deleteEventOptions, asList(documents));
	}

	@Override
	public void push(List<ConnectorDocument> documents) {
		for (ConnectorDocument document : documents) {
			if (document.getWrappedRecord().isSaved()) {
				events.add(TestConnectorEvent.modifyEvent(document));
				updateDocumentCount++;
			} else {
				events.add(TestConnectorEvent.addEvent(document));
				addNewDocumentCount++;
			}
		}
		nestedObserver.push(documents);
	}

	@Override
	public void close() {
		nestedObserver.close();
	}

	@Override
	public void deleteEvents(List<ConnectorDocument> documents) {
		deleteEvents(new DeleteEventOptions(), documents);
	}

	@Override
	public void deleteEvents(DeleteEventOptions deleteEventOptions, List<ConnectorDocument> documents) {
		Transaction transaction = new Transaction();
		for (ConnectorDocument document : documents) {
			deleteDocumentCount++;
			events.add(TestConnectorEvent.deleteEvent(document));
		}

		nestedObserver.deleteEvents(deleteEventOptions, documents);
	}

	public int getAddNewDocumentCount() {
		return addNewDocumentCount;
	}

	public int getUpdateDocumentCount() {
		return updateDocumentCount;
	}

	public int getDeleteDocumentCount() {
		return deleteDocumentCount;
	}

	@Override
	public void flush() {
		try {
			nestedObserver.flush();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public List<TestConnectorEvent> newEvents() {

		List<TestConnectorEvent> newEvents = new ArrayList<>(events);
		events.clear();
		return newEvents;
	}

	@Override
	public void cleanup() {
		nestedObserver.cleanup();
	}

	@Override
	public ModelLayerFactory getModelLayerFactory() {
		return null;
	}
}
