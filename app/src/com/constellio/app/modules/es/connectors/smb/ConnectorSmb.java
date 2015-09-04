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
package com.constellio.app.modules.es.connectors.smb;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class ConnectorSmb extends Connector {

	static final String START_OF_TRAVERSAL = "Start of traversal";
	static final String RESUME_OF_TRAVERSAL = "Resume of traversal";
	static final String END_OF_TRAVERSAL = "End of traversal";

	private static final int MAX_DOCUMENTS_PER_GET_JOBS_CALL = 100;

	ConnectorSmbInstance connectorInstance;
	ConnectorSmbUtils smbUtils;

	@Override
	protected void initialize(Record instanceRecord) {
		this.connectorInstance = getEs().wrapConnectorSmbInstance(instanceRecord);
		this.smbUtils = new ConnectorSmbUtils(getEs());

		setupMetadatasDisplay();
	}

	private void setupMetadatasDisplay() {
		// TODO Benoit. Update only once / if needed
		SchemasDisplayManager manager = getEs().getMetadataSchemasDisplayManager();

		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();

		// Connector SMB Config/Instance
		SchemaDisplayConfig schemaFormFolderTypeConfig = order(connectorInstance.getCollection(), getEs().getAppLayerFactory(), "form",
				manager.getSchema(getEs().getCollection(), getEs().getConnectorSmbDocumentSchemaCode(connectorInstance)), ConnectorDocument.TITLE,
				ConnectorSmbDocument.URL, ConnectorSmbDocument.PARENT);

		SchemaDisplayConfig schemaDisplayFolderTypeConfig = order(connectorInstance.getCollection(), getEs().getAppLayerFactory(), "display",
				manager.getSchema(getEs().getCollection(), getEs().getConnectorSmbDocumentSchemaCode(connectorInstance)), ConnectorDocument.TITLE,
				ConnectorSmbDocument.URL, ConnectorSmbDocument.PARENT);

		transaction.add(schemaDisplayFolderTypeConfig.withFormMetadataCodes(schemaFormFolderTypeConfig.getFormMetadataCodes()));

		manager.execute(transaction);

		SchemaTypesDisplayTransactionBuilder transactionBuilder = manager.newTransactionBuilderFor(connectorInstance.getCollection());
		transactionBuilder.in(ConnectorSmbDocument.SCHEMA_TYPE)
				.addToSearchResult(ConnectorSmbDocument.URL)
				.afterMetadata(ConnectorDocument.TITLE);

		transactionBuilder.in(ConnectorSmbDocument.SCHEMA_TYPE)
				.addToSearchResult(ConnectorSmbDocument.PARENT)
				.afterMetadata(ConnectorSmbDocument.URL);

		transactionBuilder.in(ConnectorSmbDocument.SCHEMA_TYPE)
				.addToSearchResult(ConnectorSmbDocument.EXTENSION)
				.afterMetadata(ConnectorSmbDocument.PARENT);

		transactionBuilder.in(ConnectorSmbDocument.SCHEMA_TYPE)
				.addToSearchResult(ConnectorSmbDocument.LANGUAGE)
				.afterMetadata(ConnectorSmbDocument.EXTENSION);

		transactionBuilder.in(ConnectorSmbDocument.SCHEMA_TYPE)
				.addToSearchResult(ConnectorSmbDocument.SIZE)
				.afterMetadata(ConnectorSmbDocument.LANGUAGE);

		transactionBuilder.in(ConnectorSmbDocument.SCHEMA_TYPE)
				.addToSearchResult(ConnectorSmbDocument.LAST_MODIFIED)
				.afterMetadata(ConnectorSmbDocument.SIZE);

		transactionBuilder.in(ConnectorSmbDocument.SCHEMA_TYPE)
				.addToSearchResult(ConnectorSmbDocument.LAST_FETCH_ATTEMPT)
				.afterMetadata(ConnectorSmbDocument.LAST_MODIFIED);

		transactionBuilder.in(ConnectorSmbDocument.SCHEMA_TYPE)
				.addToSearchResult(ConnectorSmbDocument.LAST_FETCH_ATTEMPT_STATUS)
				.afterMetadata(ConnectorSmbDocument.LAST_FETCH_ATTEMPT);

		transactionBuilder.in(ConnectorSmbDocument.SCHEMA_TYPE)
				.addToSearchResult(ConnectorSmbDocument.LAST_FETCH_ATTEMPT_DETAILS)
				.afterMetadata(ConnectorSmbDocument.LAST_FETCH_ATTEMPT_STATUS);

		manager.execute(transactionBuilder.build());
	}

	protected SchemaDisplayConfig order(String collection, AppLayerFactory appLayerFactory, String type, SchemaDisplayConfig schema, String... localCodes) {

		MetadataSchemaTypes schemaTypes = appLayerFactory.getModelLayerFactory()
				.getMetadataSchemasManager()
				.getSchemaTypes(collection);

		List<String> visibleMetadataCodes = new ArrayList<>();
		for (String localCode : localCodes) {
			visibleMetadataCodes.add(schema.getSchemaCode() + "_" + localCode);
		}
		List<String> metadataCodes = new ArrayList<>();
		metadataCodes.addAll(visibleMetadataCodes);
		List<String> otherMetadatas = new ArrayList<>();
		List<String> retrievedMetadataCodes;
		if ("form".equals(type)) {
			retrievedMetadataCodes = schema.getFormMetadataCodes();
		} else {
			retrievedMetadataCodes = schema.getDisplayMetadataCodes();
		}
		for (String retrievedMetadataCode : retrievedMetadataCodes) {
			int index = visibleMetadataCodes.indexOf(retrievedMetadataCode);
			if (index != -1) {
				metadataCodes.set(index, retrievedMetadataCode);
			} else if (!schemaTypes.getMetadata(retrievedMetadataCode)
					.isSystemReserved()) {
				otherMetadatas.add(retrievedMetadataCode);
			}
		}
		SchemaDisplayConfig newSchema;
		if ("form".equals(type)) {
			metadataCodes.addAll(otherMetadatas);
			newSchema = schema.withFormMetadataCodes(metadataCodes);

			SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
			for (String invisible : otherMetadatas) {
				manager.saveMetadata(manager.getMetadata(collection, invisible)
						.withInputType(MetadataInputType.HIDDEN));
			}
		} else {
			newSchema = schema.withDisplayMetadataCodes(metadataCodes);
		}

		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
		manager.saveMetadata(manager.getMetadata(collection, getEs().getConnectorSmbDocumentSchemaCode(connectorInstance),
				ConnectorSmbDocument.LAST_FETCH_ATTEMPT_STATUS)
				.withVisibleInAdvancedSearchStatus(true));
		return newSchema;
	}

	@Override
	public List<String> fetchTokens(String username) {
		// TODO Benoit
		return new ArrayList<>();
	}

	@Override
	public void start() {
		getLogger().info(START_OF_TRAVERSAL, "Current TraversalCode : " + connectorInstance.getTraversalCode(), new LinkedHashMap<String, String>());
		addUpdateSeeds();
	}

	private void addUpdateSeeds() {
		List<ConnectorDocument> documents = processSeeds();
		eventObserver.addUpdateEvents(documents);
	}

	private List<ConnectorDocument> processSeeds() {
		List<ConnectorDocument> documents = new ArrayList<>();

		for (String url : connectorInstance.getSeeds()) {
			if (smbUtils.isAccepted(url, connectorInstance)) {
				List<ConnectorDocument<?>> existingDocumentsOrFolders = smbUtils.getExistingDocumentsOrFoldersWithUrl(url, connectorInstance);

				if (existingDocumentsOrFolders.isEmpty()) {
					ConnectorDocument newDocumentOrFolder = newUnfetchedDocumentOrFolder(url);
					newDocumentOrFolder.setManualTokens(Record.PUBLIC_TOKEN);
					documents.add(newDocumentOrFolder);
				} else {
					ConnectorDocument<?> existingRecord = existingDocumentsOrFolders.get(0);
					documents.add(existingRecord.setFetched(false));
				}
			}
		}
		return documents;
	}

	private ConnectorDocument<?> newUnfetchedDocumentOrFolder(String url) {
		if (StringUtils.endsWith(url, "/")) {
			return getEs().newConnectorSmbFolder(connectorInstance)
					.setUrl(url)
					.setFetched(false);
		} else {
			return getEs().newConnectorSmbDocument(connectorInstance)
					.setUrl(url)
					.setFetched(false);
		}

	}

	@Override
	public void resume() {
		getLogger().info(START_OF_TRAVERSAL, "Current TraversalCode : " + connectorInstance.getTraversalCode(), new LinkedHashMap<String, String>());

		addUpdateSeeds();
	}

	@Override
	public List<ConnectorJob> getJobs() {
		eventObserver.flush();

		List<ConnectorDocument<?>> documentsToFetch = getDocumentsToFetch();

		List<ConnectorJob> jobs = new ArrayList<>();

		if (!documentsToFetch.isEmpty()) {

			Iterator<List<ConnectorDocument<?>>> documentBatchsIterator = new BatchBuilderIterator<>(documentsToFetch.iterator(),
					MAX_DOCUMENTS_PER_GET_JOBS_CALL);

			while (documentBatchsIterator.hasNext()) {
				jobs.add(new SmbFetchJob(this, documentBatchsIterator.next(), connectorInstance, eventObserver, getEs(), getLogger()));
			}
			// TODO Benoit. Check if we want to log the jobs in a given getJobs()
		} else {
			changeTraversalCodeToMarkEndOfTraversal();
		}

		return jobs;
	}

	private List<ConnectorDocument<?>> getDocumentsToFetch() {
		List<ConnectorDocument<?>> documentsToFetch = getEs().searchConnectorDocuments(getEs().connectorDocumentsToFetchQuery(connectorInstance)
				.setNumberOfRows(MAX_DOCUMENTS_PER_GET_JOBS_CALL));

		return documentsToFetch;
	}

	private void changeTraversalCodeToMarkEndOfTraversal() {
		// Delete record that are different form old traversal code

		String oldTraversalCode = connectorInstance.getTraversalCode();
		String newTraversalCode = UUID.randomUUID()
				.toString();

		connectorInstance.setTraversalCode(newTraversalCode);
		getEs().getRecordServices()
				.flush();

		getLogger().info(END_OF_TRAVERSAL, "TraversalCode : " + oldTraversalCode + " -> " + newTraversalCode, new LinkedHashMap<String, String>());

	}

	@Override
	public List<String> getConnectorDocumentTypes() {
		return asList(ConnectorSmbDocument.SCHEMA_TYPE, ConnectorSmbFolder.SCHEMA_TYPE);
	}

}