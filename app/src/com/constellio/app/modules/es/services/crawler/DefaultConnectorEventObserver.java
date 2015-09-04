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
package com.constellio.app.modules.es.services.crawler;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.es.connectors.spi.ConnectorEventObserver;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.services.ConnectorsUtils;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.BulkRecordTransactionHandlerOptions;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.users.UserServices;

public class DefaultConnectorEventObserver implements ConnectorEventObserver {

	private static Logger LOGGER = LoggerFactory.getLogger(ConnectorCrawler.class);

	UserServices userServices;

	ESSchemasRecordsServices es;

	ConnectorLogger connectorLogger;

	String resourceName;

	//BulkRecordTransactionHandler handler;

	public DefaultConnectorEventObserver(ESSchemasRecordsServices es, ConnectorLogger connectorLogger, String resourceName) {
		this.es = es;
		this.connectorLogger = connectorLogger;
		this.resourceName = resourceName;
		this.userServices = es.getModelLayerFactory().newUserServices();
		BulkRecordTransactionHandlerOptions options = new BulkRecordTransactionHandlerOptions();
		//this.handler = new BulkRecordTransactionHandler(es.getRecordServices(), resourceName, options);
	}

	@Override
	public void addUpdateEvents(ConnectorDocument... documents) {
		addUpdateEvents(asList(documents));
	}

	@Override
	public void addUpdateEvents(List<ConnectorDocument> documents) {
		List<Record> documentRecords = new ArrayList<>();
		for (ConnectorDocument document : documents) {
			if (document.isFetched()) {
				LOGGER.info("**** Received fetched document '" + document.getWrappedRecord().getIdTitle() + "'");
			} else {
				LOGGER.info("**** Received document to fetch  : '" + document.getId() + "'");
			}
			applyMappedPropertiesToMetadata(document);
			documentRecords.add(document.getWrappedRecord());
		}

		Transaction transaction = new Transaction(documentRecords);
		transaction.setRecordFlushing(RecordsFlushing.LATER());

		try {
			es.getRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	void applyMappedPropertiesToMetadata(ConnectorDocument document) {
		Map mapping = es.getConnectorManager().getConnectorInstance(document.getConnector()).getPropertiesMapping();
		if (mapping != null) {
			ConnectorDocumentPreparator preparator = new ConnectorDocumentPreparator(mapping, document.getSchema());
			preparator.applyProperties(document);
		}

		document.clearProperties();
	}

	@Override
	public void deleteEvents(ConnectorDocument... documents) {
		deleteEvents(asList(documents));
	}

	@Override
	public void close() {
		//handler.closeAndJoin();
	}

	@Override
	public void deleteEvents(List<ConnectorDocument> documents) {
		for (ConnectorDocument document : documents) {
			try {
				es.getRecordServices().logicallyDelete(document.getWrappedRecord(), User.GOD);
				es.getRecordServices().physicallyDelete(document.getWrappedRecord(), User.GOD);
			} catch (RecordServicesRuntimeException e) {
				String title = "Cannot delete document '" + document.getWrappedRecord().getIdTitle() + "'";
				String description = ConnectorsUtils.getStackTrace(e);
				connectorLogger.error(title, description, new HashMap<String, String>());
			}
		}
	}

	@Override
	public void flush() {
		es.getRecordServices().flush();
	}
}
