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
package com.constellio.app.modules.es.sdk;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.es.connectors.spi.ConnectorEventObserver;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.model.entities.records.Transaction;

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
		deleteEvents(asList(documents));
	}

	@Override
	public void close() {
		nestedObserver.close();
	}

	@Override
	public void deleteEvents(List<ConnectorDocument> documents) {
		Transaction transaction = new Transaction();
		for (ConnectorDocument document : documents) {
			deleteDocumentCount++;
			events.add(TestConnectorEvent.deleteEvent(document));
		}

		nestedObserver.deleteEvents(documents);
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
		nestedObserver.flush();
	}

}
