package com.constellio.app.modules.complementary.esRmRobots.services;

import static com.constellio.app.modules.es.connectors.ConnectorServicesFactory.forConnectorInstance;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.model.enums.ActionAfterClassification;
import com.constellio.app.modules.complementary.esRmRobots.services.ClassifyServicesRuntimeException.ClassifyServicesRuntimeException_CannotClassifyAsDocument;
import com.constellio.app.modules.es.connectors.ConnectorServicesFactory;
import com.constellio.app.modules.es.connectors.ConnectorServicesRuntimeException.ConnectorServicesRuntimeException_CannotDownloadDocument;
import com.constellio.app.modules.es.connectors.ConnectorUtilsServices;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.modules.robots.services.RobotSchemaRecordServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.utils.EnumWithSmallCodeUtils;

public class ClassifyConnectorRecordInTaxonomyExecutor {

	private static final String CLASSIFY_DOCUMENT = "ClassifyConnectorRecordInTaxonomyExecutor-ClassifyDocuments";

	private static final Logger LOGGER = LoggerFactory.getLogger(ClassifyConnectorRecordInTaxonomyExecutor.class);

	ConnectorDocument<?> connectorFolder;
	Record record;
	AppLayerFactory appLayerFactory;
	ModelLayerFactory modelLayerFactory;
	ClassifyConnectorFolderActionParameters params;
	RMSchemasRecordsServices rm;
	ESSchemasRecordsServices es;
	RobotSchemaRecordServices robots;
	RecordServices recordServices;
	ContentManager contentManager;
	SearchServices searchServices;
	ConnectorInstance connectorInstance;
	Transaction transaction;
	List<String> newExclusions = new ArrayList<>();
	User currentUser;
	String robotId;

	public ClassifyConnectorRecordInTaxonomyExecutor(Record record, ClassifyConnectorFolderActionParameters params,
			AppLayerFactory appLayerFactory, User currentUser, String robotId) {
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.appLayerFactory = appLayerFactory;
		this.record = record;
		this.params = params;
		this.currentUser = currentUser;
		this.rm = new RMSchemasRecordsServices(record.getCollection(), modelLayerFactory);
		this.es = new ESSchemasRecordsServices(record.getCollection(), appLayerFactory);
		this.robots = new RobotSchemaRecordServices(record.getCollection(), appLayerFactory);
		this.recordServices = modelLayerFactory.newRecordServices();
		this.contentManager = modelLayerFactory.getContentManager();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.robotId = robotId;
	}

	public void execute() {
		transaction = new Transaction();

		connectorFolder = es.wrapConnectorDocument(record);
		classifyConnectorFolderInTaxonomy();
		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new ClassifyServicesRuntimeException(e);
		}

		if (!newExclusions.isEmpty()) {
			addNewExclusionToConnector(0);
		}

		if (params.getActionAfterClassification() == ActionAfterClassification.DELETE_DOCUMENTS_ON_ORIGINAL_SYSTEM) {
			for (Record documentInTransaction : transaction.getRecords()) {
				//FIXME sharepoint
				if (documentInTransaction.getSchemaCode().startsWith(ConnectorSmbDocument.SCHEMA_TYPE
						+ "_") /*|| documentInTransaction.getSchemaCode().startsWith(ConnectorSharepointDocument.SCHEMA_TYPE + "_")*/) {
					ConnectorDocument<?> connectorDocument = es.wrapConnectorDocument(documentInTransaction);
					connectorServices(connectorDocument).deleteDocumentOnRemoteComponent(connectorDocument);
					try {
						recordServices.add(robots.newRobotLog().setRobot(robotId)
								.setTitle("Document '" + connectorDocument.getURL()
										+ "' supprimé suite à sa classification dans Constellio"));
					} catch (RecordServicesException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	private void addNewExclusionToConnector(int i) {
		//TODO TEST!!!
		try {
			Transaction connectorInstanceTransaction = new Transaction();
			connectorInstanceTransaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
			connectorInstance = es.getConnectorInstance(connectorInstance.getId());
			ConnectorUtilsServices connectorUtilsServices = forConnectorInstance(appLayerFactory, connectorInstance);
			connectorUtilsServices.addExcludedUrlsTo(newExclusions, connectorInstance);
			connectorInstanceTransaction.add(connectorInstance);
			recordServices.execute(connectorInstanceTransaction);
		} catch (RecordServicesException.OptimisticLocking e) {
			if (i < 3) {
				addNewExclusionToConnector(i + 1);
			} else {
				throw new RuntimeException(e);
			}

		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	private void classifyConnectorFolderInTaxonomy() {

		String fullConnectorDocPath = connectorFolder.getURL();
		if (params.getInTaxonomy() != null) {
			String pathPrefix = params.getPathPrefix() == null ? null : params.getPathPrefix();
			String[] rawPathParts = fullConnectorDocPath.replace(pathPrefix, "").split("/");
			List<String> pathParts = new ArrayList<>();
			for (String rawPathPart : rawPathParts) {
				pathParts.add(rawPathPart.split(params.getDelimiter())[0]);
			}

			Taxonomy targetTaxonomy = modelLayerFactory.getTaxonomiesManager()
					.getEnabledTaxonomyWithCode(connectorFolder.getCollection(), params.getInTaxonomy());
			String conceptTypeCode = targetTaxonomy.getSchemaTypes().get(0);
			MetadataSchemaType conceptType = modelLayerFactory.getMetadataSchemasManager()
					.getSchemaTypes(connectorFolder.getCollection()).getSchemaType(conceptTypeCode);
			String metadataCode = conceptType.getDefaultSchema().getCode() + "_" + Schemas.CODE.getLocalCode();
			Metadata codeMetadata = conceptType.getMetadata(metadataCode);

			classifyForPath(fullConnectorDocPath, pathParts, targetTaxonomy, codeMetadata);
		} else {
			processFolderWithoutTaxonomy(connectorFolder.getTitle(), fullConnectorDocPath);
		}
	}

	private void classifyForPath(String fullConnectorDocPath, List<String> pathParts, Taxonomy targetTaxonomy,
			Metadata codeMetadata) {
		String parent = null;
		Iterator<String> iterator = pathParts.iterator();
		while (iterator.hasNext()) {
			String pathPart = iterator.next();
			Record concept = recordServices.getRecordByMetadata(codeMetadata, pathPart);
			if (concept != null && params.getInTaxonomy() != null) {
				parent = verifyConceptInPath(codeMetadata, parent, concept);
			} else {
				if (!iterator.hasNext()) {
					processFolder(fullConnectorDocPath, targetTaxonomy, codeMetadata, parent, pathPart);
				} else {
					parent = null;
				}
			}
		}
	}

	private void processFolder(String fullConnectorDocPath, Taxonomy targetTaxonomy,
			Metadata codeMetadata, String parent, String folderName) {
		MetadataSchema folderSchema = rm.defaultFolderSchema();
		Metadata legacyIdMetadata = folderSchema.getMetadata(Schemas.LEGACY_ID.getLocalCode());
		Record rmRecord = recordServices.getRecordByMetadata(legacyIdMetadata, fullConnectorDocPath);
		Folder rmFolder;
		if (rmRecord == null) {
			Record parentConcept = recordServices.getRecordByMetadata(codeMetadata, parent);
			if (parentConcept != null) {
				rmFolder = classifyFolderInConcept(fullConnectorDocPath, targetTaxonomy, folderName, folderSchema,
						parentConcept);
			} else if (targetTaxonomy != null) {
				rmFolder = classifyFolderInParentFolder(fullConnectorDocPath, folderName);
			} else {
				rmFolder = rm.newFolder();
			}
			RecordUtils.copyMetadatas(connectorFolder, rmFolder);
			mapFolderMetadataFromMappingFile(folderName, rmFolder, fullConnectorDocPath);
			classifyDocumentsFromFolder(rmFolder);
		} else {
			rmFolder = rm.wrapFolder(rmRecord);
			mapFolderMetadataFromMappingFile(folderName, rmFolder, fullConnectorDocPath);
			classifyDocumentsFromFolder(rmFolder);
		}
		//		if (params.getActionAfterClassification().isConnectorDocumentExcluded()) {
		//			markAsUnfetched(connectorFolder);
		//		}
	}

	private void processFolderWithoutTaxonomy(String folderName, String url) {
		MetadataSchema folderSchema = rm.defaultFolderSchema();
		Metadata legacyIdMetadata = folderSchema.getMetadata(Schemas.LEGACY_ID.getLocalCode());
		Record rmRecord = recordServices.getRecordByMetadata(legacyIdMetadata, url);
		Folder rmFolder;
		if (rmRecord == null) {
			rmFolder = rm.newFolder().setTitle(folderName);
			rmFolder.setLegacyId(url);
			mapFolderMetadataFromMappingFile(folderName, rmFolder, url);
			classifyDocumentsFromFolder(rmFolder);
		} else {
			rmFolder = rm.wrapFolder(rmRecord);
			mapFolderMetadataFromMappingFile(folderName, rmFolder, url);
			classifyDocumentsFromFolder(rmFolder);
		}
		//		if (params.getActionAfterClassification().isConnectorDocumentExcluded()) {
		//			markAsUnfetched(connectorFolder);
		//		}
	}

	private void markAsUnfetched(ConnectorDocument connectorDocument) {

		if (connectorInstance == null) {
			connectorInstance = es.getConnectorInstance(connectorDocument.getConnector());
		}
		connectorDocument.getWrappedRecord().set(Schemas.LOGICALLY_DELETED_STATUS, true);
		connectorDocument.setFetched(false);
		transaction.add(connectorDocument);
		newExclusions.add(connectorDocument.getURL());
	}

	private void mapFolderMetadataFromMappingFile(String folderName, Folder rmFolder, String folderURL) {
		Content folderMapping = params.getFolderMapping();
		if (folderMapping != null) {
			List<Map<String, String>> csvEntries = new CSVReader(contentManager).readCSVContent(folderMapping);
			Map<String, Map<String, String>> mappedEntries = mapEntriesOnFolderName(csvEntries);
			Map<String, String> folderEntry = mappedEntries.get(folderName);
			if (folderEntry != null) {
				MetadataSchema folderSchema = adjustFolderSchema(rmFolder, folderEntry);
				for (Map.Entry<String, String> mappingEntry : folderEntry.entrySet()) {
					if (!"foldername".equals(mappingEntry.getKey())) {
						if (StringUtils.isNotBlank(mappingEntry.getValue())) {
							setMetadataInRmRecord(rmFolder, folderSchema, mappingEntry.getKey(), mappingEntry.getValue());
						}
					}
				}
				useDefaultValuesInMissingFields(folderEntry, rmFolder);
			} else {
				useAllDefaultValuesFromParams(rmFolder);
			}
		} else {
			useAllDefaultValuesFromParams(rmFolder);
		}
		try {
			transaction.add(rmFolder);
		} catch (Exception ex) {
			throw new ClassifyServicesRuntimeException("Cannot classify folder with URL '" + folderURL + "' in taxonomy", ex);
		}
	}

	private Map<String, Map<String, String>> mapEntriesOnFolderName(List<Map<String, String>> csvEntries) {
		Map<String, Map<String, String>> mappedEntries = new HashMap<>();
		for (Map<String, String> entry : csvEntries) {
			mappedEntries.put(entry.get("foldername"), entry);
		}
		return mappedEntries;
	}

	private Folder classifyFolderInParentFolder(String fullConnectorDocPath, String pathPart) {
		String parentPath = getParentPath(fullConnectorDocPath, pathPart);

		Folder parentFolder = rm.getFolderByLegacyId(parentPath);
		Folder newRmFolder = rm.newFolder();
		if (parentFolder != null) {
			newRmFolder.setOpenDate(parentFolder.getOpenDate());
			newRmFolder.setCloseDateEntered(parentFolder.getCloseDateEntered());
		}
		newRmFolder.setParentFolder(parentFolder).setTitle(pathPart).setLegacyId(fullConnectorDocPath);
		return newRmFolder;
	}

	private Folder classifyFolderInConcept(String fullConnectorDocPath, Taxonomy targetTaxonomy, String pathPart,
			MetadataSchema folderSchema, Record parentConcept) {
		Folder newRmFolder = rm.newFolder();
		Metadata taxoMetadata = folderSchema.getTaxonomyRelationshipReferences(Arrays.asList(targetTaxonomy))
				.get(0);
		newRmFolder.set(taxoMetadata.getLocalCode(), parentConcept.getId()).setTitle(pathPart);
		newRmFolder.setLegacyId(fullConnectorDocPath);
		return newRmFolder;
	}

	private void classifyDocumentsFromFolder(Folder rmFolder) {

		List<ConnectorDocument<?>> children = connectorServices(connectorFolder).getChildren(connectorFolder);

		Map<String, ConnectorDocument> documentsMap = mapDocumentsByFilename(children);
		Map<String, ClassifiedDocument> createdRecordsByUrls = classifyDocuments(rmFolder.getId(), children, true);
		for (Map.Entry<String, ConnectorDocument> entry : documentsMap.entrySet()) {
			ClassifiedDocument classifiedDocument = createdRecordsByUrls.get(entry.getValue().getUrl());
			mapDocumentMetadataFromMappingFile(entry.getKey(), classifiedDocument);
		}
	}

	private Map<String, ConnectorDocument> mapDocumentsByFilename(List<ConnectorDocument<?>> docsToImport) {
		Map<String, ConnectorDocument> mappedDocuments = new HashMap<>();
		for (ConnectorDocument document : docsToImport) {
			String[] splitURL = document.getUrl().split("/");
			String fileName = splitURL[splitURL.length - 1];
			mappedDocuments.put(fileName, document);
		}
		return mappedDocuments;
	}

	private void mapDocumentMetadataFromMappingFile(String fileName, ClassifiedDocument classifiedDocument) {
		Document rmDocument = classifiedDocument.getRmDocument();
		ConnectorDocument<?> connectorDocument = classifiedDocument.getConnectorDocument();
		Content documentMapping = params.getDocumentMapping();
		if (documentMapping != null) {
			List<Map<String, String>> csvEntries = new CSVReader(contentManager).readCSVContent(documentMapping);
			Map<String, Map<String, String>> mappedEntries = mapEntriesOnFileName(csvEntries);
			Map<String, String> documentEntry = mappedEntries.get(fileName);
			if (documentEntry != null) {
				MetadataSchema documentSchema = adjustDocumentSchema(rmDocument, documentEntry);
				for (Map.Entry<String, String> mappingEntry : documentEntry.entrySet()) {
					if (!"filename".equals(mappingEntry.getKey())) {
						if (StringUtils.isNotBlank(mappingEntry.getValue())) {
							setMetadataInRmRecord(rmDocument, documentSchema, mappingEntry.getKey(), mappingEntry.getValue());
						}
					}
				}
			}
		}

		try {
			transaction.add(rmDocument);
			recordServices.validateRecordInTransaction(rmDocument.getWrappedRecord(), transaction);

			if (params.getActionAfterClassification().isConnectorDocumentExcluded()) {
				markAsUnfetched(connectorDocument);
			}
		} catch (ValidationException e) {
			transaction.remove(rmDocument);
		}
	}

	private MetadataSchema adjustDocumentSchema(Document rmDocument, Map<String, String> documentEntry) {
		MetadataSchema documentSchema = rm.defaultDocumentSchema();
		Metadata codeMetadata = rm.documentTypeSchema().getMetadata(Schemas.CODE.getLocalCode());
		String documentTypeCode = documentEntry.get(Document.TYPE);
		if (StringUtils.isNotBlank(documentTypeCode)) {
			Record documentTypeRecord = recordServices.getRecordByMetadata(codeMetadata, documentTypeCode);
			DocumentType documentType = rm.wrapDocumentType(documentTypeRecord);
			documentSchema = rm.documentSchemaFor(documentType);
			rmDocument.getWrappedRecord().changeSchema(rm.defaultDocumentSchema(), documentSchema);
		}
		return documentSchema;
	}

	private Map<String, Map<String, String>> mapEntriesOnFileName(List<Map<String, String>> csvEntries) {
		Map<String, Map<String, String>> mappedEntries = new HashMap<>();
		for (Map<String, String> entry : csvEntries) {
			mappedEntries.put(entry.get("filename"), entry);
		}
		return mappedEntries;
	}

	private String verifyConceptInPath(Metadata codeMetadata, String parent, Record concept) {
		if (parent == null) {
			if (concept.getParentId() != null) {
				throw new RuntimeException("Concept should be root");
			}
		} else if (!parent.equals(recordServices.getDocumentById(concept.getParentId()).get(codeMetadata))) {
			throw new RuntimeException("Concept is under the wrong parent");
		}
		parent = concept.get(codeMetadata);
		return parent;
	}

	private String getParentPath(String fullPath, String pathPart) {
		StringBuilder builder = new StringBuilder(fullPath);
		builder.replace(fullPath.lastIndexOf(pathPart + "/"), fullPath.lastIndexOf(pathPart + "/") + pathPart.length() + 1, "");
		return builder.toString();
	}

	private MetadataSchema adjustFolderSchema(Folder rmFolder, Map<String, String> folderEntry) {
		MetadataSchema folderSchema = rm.defaultFolderSchema();
		Metadata codeMetadata = rm.folderTypeSchema().getMetadata(Schemas.CODE.getLocalCode());
		String folderTypeCode = folderEntry.get(Folder.TYPE);
		if (StringUtils.isNotBlank(folderTypeCode)) {
			Record folderTypeRecord = recordServices.getRecordByMetadata(codeMetadata, folderTypeCode);
			FolderType folderType = rm.wrapFolderType(folderTypeRecord);
			folderSchema = rm.folderSchemaFor(folderType);
			rmFolder.getWrappedRecord().changeSchema(rm.defaultFolderSchema(), folderSchema);
		}
		return folderSchema;
	}

	private void useAllDefaultValuesFromParams(Folder rmFolder) {
		if (rmFolder.getParentFolder() == null && params.getDefaultParentFolder() != null) {
			rmFolder.setParentFolder(params.getDefaultParentFolder());
		}
		if (rmFolder.getAdministrativeUnitEntered() == null && params.getDefaultAdminUnit() != null) {
			rmFolder.setAdministrativeUnitEntered(params.getDefaultAdminUnit());
		}
		if (rmFolder.getCategoryEntered() == null && params.getDefaultCategory() != null) {
			rmFolder.setCategoryEntered(params.getDefaultCategory());
		}
		if (params.getDefaultRetentionRule() != null) {
			rmFolder.setRetentionRuleEntered(params.getDefaultRetentionRule());
		}
		if (params.getDefaultCopyStatus() != null) {
			rmFolder.setCopyStatusEntered(params.getDefaultCopyStatus());
		}
		if (params.getDefaultOpenDate() != null) {
			rmFolder.setOpenDate(params.getDefaultOpenDate());
		}
	}

	private void useDefaultValuesInMissingFields(Map<String, String> folderEntry, Folder rmFolder) {
		if (folderEntry.get(Folder.PARENT_FOLDER) == null && rmFolder.getParentFolder() == null
				&& params.getDefaultParentFolder() != null) {
			rmFolder.setParentFolder(params.getDefaultParentFolder());
		}
		if (rmFolder.getAdministrativeUnitEntered() == null && folderEntry.get(Folder.ADMINISTRATIVE_UNIT_ENTERED) == null
				&& params.getDefaultAdminUnit() != null) {
			rmFolder.setAdministrativeUnitEntered(params.getDefaultAdminUnit());
		}
		if (rmFolder.getCategoryEntered() == null && folderEntry.get(Folder.CATEGORY_ENTERED) == null
				&& params.getDefaultCategory() != null) {
			rmFolder.setCategoryEntered(params.getDefaultCategory());
		}
		if (folderEntry.get(Folder.RETENTION_RULE_ENTERED) == null && params.getDefaultRetentionRule() != null) {
			rmFolder.setRetentionRuleEntered(params.getDefaultRetentionRule());
		}
		if (folderEntry.get(Folder.COPY_STATUS_ENTERED) == null && params.getDefaultCopyStatus() != null) {
			rmFolder.setCopyStatusEntered(params.getDefaultCopyStatus());
		}
	}

	private void setMetadataInRmRecord(RecordWrapper rmRecord, MetadataSchema schema, String metadataCode, String value) {
		Metadata metadata = schema.getMetadata(metadataCode);
		switch (metadata.getType()) {
		case STRING:
			if (metadata.isMultivalue()) {
				rmRecord.set(metadataCode, Arrays.asList(value.split(";")));
			} else {
				rmRecord.set(metadataCode, value);
			}
			break;
		case REFERENCE:
			String collection = rmRecord.getCollection();
			MetadataSchemasManager schemasManager = rm.getModelLayerFactory().getMetadataSchemasManager();
			MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(collection);
			MetadataSchemaType schemaType = schemaTypes.getSchemaType(metadata.getAllowedReferences().getAllowedSchemaType());
			Metadata codeMetadata = schemaType.getDefaultSchema().get(Schemas.CODE.getLocalCode());
			Record referencedRecord = recordServices.getRecordByMetadata(codeMetadata, value);
			rmRecord.set(metadataCode, referencedRecord.getId());
			break;
		case ENUM:
			rmRecord.set(metadataCode, EnumWithSmallCodeUtils.toEnum(metadata.getEnumClass(), value));
			break;
		case DATE:
			rmRecord.set(metadataCode, parseDate(value));
			break;
		case DATE_TIME:
			rmRecord.set(metadataCode, parseDateTime(value));
			break;
		}
	}

	private LocalDate parseDate(String dateString) {
		DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
		return dateFormatter.parseLocalDate(dateString);
	}

	private LocalDateTime parseDateTime(String dateTimeString) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd hh:mm:ss");
		return dateTimeFormatter.parseLocalDateTime(dateTimeString);
	}

	public Map<String, ClassifiedDocument> classifyDocuments(String inRmFolder, List<ConnectorDocument<?>> documentsRecords,
			Boolean majorVersions) {
		Map<String, ClassifiedDocument> createdRecordsByUrls = new HashMap<>();
		for (ConnectorDocument document : documentsRecords) {
			try {
				ClassifiedDocument classifiedDocument = classifyDocument(document, inRmFolder, majorVersions);
				createdRecordsByUrls.put(document.getUrl(), classifiedDocument);
			} catch (ClassifyServicesRuntimeException_CannotClassifyAsDocument e) {
				LOGGER.warn("Cannot classify '" + document.getUrl() + "'", e);
			}
		}
		return createdRecordsByUrls;
	}

	private ClassifiedDocument classifyDocument(ConnectorDocument connectorDocument, String inRmFolder, Boolean majorVersions) {

		ContentVersionDataSummary newVersionDataSummary = null;
		try {
			Document document = rm.newDocument();
			document.setTitle(connectorDocument.getTitle());
			document.setFolder(inRmFolder);
			document.setLegacyId(connectorDocument.getUrl());
			RecordUtils.copyMetadatas(connectorDocument, document);
			InputStream inputStream = connectorServices(connectorDocument)
					.newContentInputStream(connectorDocument, CLASSIFY_DOCUMENT);

			newVersionDataSummary = contentManager.upload(inputStream, false, true, null);
			Content content;
			if (majorVersions) {
				content = contentManager.createMajor(currentUser, connectorDocument.getTitle(), newVersionDataSummary);
			} else {
				content = contentManager.createMinor(currentUser, connectorDocument.getTitle(), newVersionDataSummary);
			}
			document.setContent(content);

			return new ClassifiedDocument(connectorDocument, document);
		} catch (ConnectorServicesRuntimeException_CannotDownloadDocument e) {
			if (newVersionDataSummary != null) {
				contentManager.markForDeletionIfNotReferenced(newVersionDataSummary.getHash());
			}
			throw new ClassifyServicesRuntimeException_CannotClassifyAsDocument(connectorDocument, e);
		}

	}

	ConnectorUtilsServices connectorServices(ConnectorDocument document) {
		return ConnectorServicesFactory.forConnectorDocument(appLayerFactory, document);
	}

	ConnectorUtilsServices connectorServices(ConnectorInstance document) {
		return ConnectorServicesFactory.forConnectorInstance(appLayerFactory, document);
	}

	ConnectorUtilsServices connectorServices(Record record) {
		return ConnectorServicesFactory.forRecord(appLayerFactory, record);
	}

	private static class ClassifiedDocument {

		ConnectorDocument<?> connectorDocument;

		Document rmDocument;

		public ClassifiedDocument(ConnectorDocument<?> connectorDocument, Document rmDocument) {
			this.connectorDocument = connectorDocument;
			this.rmDocument = rmDocument;
		}

		public ConnectorDocument<?> getConnectorDocument() {
			return connectorDocument;
		}

		public Document getRmDocument() {
			return rmDocument;
		}
	}
}

