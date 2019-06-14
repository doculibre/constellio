package com.constellio.app.modules.es.services.crawler;

import com.constellio.app.modules.es.ESConfigs;
import com.constellio.app.modules.es.connectors.spi.ConnectorEventObserver;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.services.ConnectorsUtils;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.mapping.ConnectorField;
import com.constellio.app.modules.es.services.mapping.ConnectorMappingService;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.BulkRecordTransactionHandler;
import com.constellio.model.services.records.BulkRecordTransactionHandlerOptions;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.users.UserServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class DefaultConnectorEventObserver implements ConnectorEventObserver {

	private static Logger LOGGER = LoggerFactory.getLogger(DefaultConnectorEventObserver.class);

	UserServices userServices;

	ESSchemasRecordsServices es;

	ConnectorLogger connectorLogger;

	String resourceName;

	BulkRecordTransactionHandler handler;

	ConnectorMappingService mappingService;

	Map<String, Map<String, ConnectorField>> fieldsDeclarationPerConnectorId = new HashMap<>();

	public DefaultConnectorEventObserver(ESSchemasRecordsServices es, ConnectorLogger connectorLogger,
										 String resourceName) {
		this.es = es;
		this.connectorLogger = connectorLogger;
		this.resourceName = resourceName;
		this.userServices = es.getModelLayerFactory().newUserServices();
		ESConfigs esConfigs = new ESConfigs(es.getModelLayerFactory());
		BulkRecordTransactionHandlerOptions options = new BulkRecordTransactionHandlerOptions()
				.showProgressionInConsole(false)
				.withRecordsPerBatch(esConfigs.getConnectorNumberOfRecordsPerBatch()).withNumberOfThreads(esConfigs.getConnectorNumberOfThreads()).setContinueOnExceptions(true);
		options.getTransactionOptions().setUnicityValidationsEnabled(false);
		this.handler = new BulkRecordTransactionHandler(es.getRecordServices(), resourceName, options);
		this.mappingService = new ConnectorMappingService(es);
	}

	@Override
	public void addUpdateEvents(ConnectorDocument... documents) {
		addUpdateEvents(asList(documents));
	}

	@Override
	public void addUpdateEvents(List<ConnectorDocument> documents) {
		List<Record> documentRecords = new ArrayList<>();
		for (ConnectorDocument document : documents) {
			//			if (document.isFetched()) {
			//				LOGGER.info("**** Received fetched document '" + document.getWrappedRecord().getIdTitle() + "'");
			//			} else {
			//				LOGGER.info("**** Received document to fetch  : '" + document.getId() + "'");
			//			}
			Map<String, ConnectorField> fieldDeclarations = applyMappedPropertiesToMetadata(document);
			addFieldDeclarations(document.getConnector(), fieldDeclarations);
			documentRecords.add(document.getWrappedRecord());
		}

		Transaction transaction = new Transaction(documentRecords);
		boolean flushNow = false;
		for (Record record : documentRecords) {
			if (flushNow || es.getModelLayerFactory().getRecordsCaches().getCache(record.getCollection())
									.getCacheConfigOf(record.getSchemaCode()) != null) {
				flushNow = true;
			}
		}
		transaction.setRecordFlushing(flushNow ? RecordsFlushing.NOW : RecordsFlushing.LATER());

		try {
			es.getRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	private synchronized void addFieldDeclarations(String connectorId,
												   Map<String, ConnectorField> fieldDeclarations) {

		Map<String, ConnectorField> connectorFields = fieldsDeclarationPerConnectorId.get(connectorId);
		if (connectorFields == null) {
			connectorFields = new HashMap<>();
			this.fieldsDeclarationPerConnectorId.put(connectorId, connectorFields);
		}

		for (ConnectorField connectorField : fieldDeclarations.values()) {
			if (!connectorFields.containsKey(connectorField.getId())) {
				connectorFields.put(connectorField.getId(), connectorField);
			}
		}

	}

	@Override
	public void push(List<ConnectorDocument> documents) {
		List<Record> documentRecords = new ArrayList<>();
		for (ConnectorDocument document : documents) {
			Map<String, ConnectorField> fieldDeclarations = applyMappedPropertiesToMetadata(document);
			addFieldDeclarations(document.getConnector(), fieldDeclarations);
			documentRecords.add(document.getWrappedRecord());
		}

		boolean flushNow = false;
		for (Record record : documentRecords) {
			if (flushNow || es.getModelLayerFactory().getRecordsCaches().getCache(record.getCollection())
									.getCacheConfigOf(record.getSchemaCode()) != null) {
				flushNow = true;
			}
		}
		List<Record> records = new RecordUtils().unwrap(documents);

		handler.append(records);
	}

	Map<String, ConnectorField> applyMappedPropertiesToMetadata(ConnectorDocument<?> document) {
		ConnectorInstance instance = es.getConnectorInstance(document.getConnector());
		String connectorDocumentSchemaType = new SchemaUtils().getSchemaTypeCode(document.getSchemaCode());
		Map<String, List<String>> mapping = mappingService.getMapping(instance, connectorDocumentSchemaType);

		for (Map.Entry<String, List<String>> entry : mapping.entrySet()) {
			List<Object> values = new ArrayList<>();

			for (String field : entry.getValue()) {
				String id = field.split(":")[1];
				Object fieldValues = document.getProperties().get(id);
				if (fieldValues != null) {
					if (fieldValues instanceof List) {
						values.addAll((List) fieldValues);
					} else {
						values.add(fieldValues);
					}
				}
			}
			document.set(entry.getKey(), values);
		}

		Map<String, ConnectorField> fields = new HashMap<>(document.getFieldsDeclarations());

		document.clearProperties();

		return fields;
	}

	@Override
	public void deleteEvents(ConnectorDocument... documents) {
		deleteEvents(new DeleteEventOptions(), asList(documents));
	}

	@Override
	public void deleteEvents(DeleteEventOptions options, ConnectorDocument... documents) {
		deleteEvents(options, asList(documents));
	}

	@Override
	public void close() {
		handler.closeAndJoin();
		saveNewDeclaredFields();
	}

	private void saveNewDeclaredFields() {
		Transaction modifiedConnectorInstancesTransaction = new Transaction();
		for (Map.Entry<String, Map<String, ConnectorField>> entry : fieldsDeclarationPerConnectorId.entrySet()) {
			ConnectorInstance<?> instance = es.getConnectorInstance(entry.getKey());
			Map<String, ConnectorField> declaredFields = entry.getValue();
			List<ConnectorField> connectorFields = new ArrayList<>(instance.getAvailableFields());
			boolean newField = false;
			for (ConnectorField connectorField : declaredFields.values()) {
				if (!hasDeclaredFieldsWithCode(connectorFields, connectorField.getId())) {
					connectorFields.add(connectorField);
					newField = true;
				}
			}

			if (newField) {
				instance.setAvailableFields(connectorFields);
				modifiedConnectorInstancesTransaction.add(instance);
			}

		}
		try {
			es.getModelLayerFactory().newRecordServices().execute(modifiedConnectorInstancesTransaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		fieldsDeclarationPerConnectorId.clear();
	}

	@Override
	public void deleteEvents(List<ConnectorDocument> documents) {
		this.deleteEvents(new DeleteEventOptions(), documents);
	}

	@Override
	public void deleteEvents(DeleteEventOptions options, List<ConnectorDocument> documents) {
		for (ConnectorDocument document : documents) {
			try {
				if (document.getSchemaCode().startsWith(ConnectorSmbFolder.SCHEMA_TYPE)
					|| document.getSchemaCode().startsWith(ConnectorSmbDocument.SCHEMA_TYPE)) {
					//					RecordDeleteServices recordDeleteServices = new RecordDeleteServices(es.getModelLayerFactory().getDataLayerFactory().newRecordDao(), es.getModelLayerFactory());
					//					recordDeleteServices.logicallyDelete(document.getWrappedRecord(), User.GOD, options.logicalDeleteOptions);
					//					es.getRecordServices().physicallyDelete(document.getWrappedRecord(), User.GOD, options.physicalDeleteOptions);
				} else {
					es.getRecordServices().logicallyDelete(document.getWrappedRecord(), User.GOD, options.logicalDeleteOptions);
					es.getRecordServices().physicallyDelete(document.getWrappedRecord(), User.GOD, options.physicalDeleteOptions);
				}
			} catch (RecordServicesRuntimeException e) {
				String title = "Cannot delete document '" + document.getWrappedRecord().getIdTitle() + "'";
				String description = ConnectorsUtils.getStackTrace(e);
				connectorLogger.error(title, description, new HashMap<String, String>());
			}
		}
	}

	private boolean hasDeclaredFieldsWithCode(List<ConnectorField> availableFields, String id) {
		for (ConnectorField field : availableFields) {
			if (id != null && id.equals(field.getId())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void flush() throws InterruptedException {
		handler.pushCurrent();
		try {
			handler.barrier();
		} finally {


			es.getRecordServices().flush();
			saveNewDeclaredFields();
		}
	}

	@Override
	public void cleanup() {
		handler.resetException();
	}

	public ModelLayerFactory getModelLayerFactory() {
		return es.getModelLayerFactory();
	}
}
