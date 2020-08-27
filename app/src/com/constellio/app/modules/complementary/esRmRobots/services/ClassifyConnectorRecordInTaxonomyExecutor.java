package com.constellio.app.modules.complementary.esRmRobots.services;

import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.model.enums.ActionAfterClassification;
import com.constellio.app.modules.complementary.esRmRobots.services.ClassifyConnectorHelper.ClassifiedRecordPathInfo;
import com.constellio.app.modules.complementary.esRmRobots.services.ClassifyServicesRuntimeException.ClassifyServicesRuntimeException_CannotClassifyAsDocument;
import com.constellio.app.modules.es.connectors.ConnectorServicesFactory;
import com.constellio.app.modules.es.connectors.ConnectorServicesRuntimeException.ConnectorServicesRuntimeException_CannotDownloadDocument;
import com.constellio.app.modules.es.connectors.ConnectorUtilsServices;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
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
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.frameworks.validation.ValidationRuntimeException;
import com.constellio.model.services.contents.ContentImpl;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentManager.UploadOptions;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.contents.icap.IcapException;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.utils.EnumWithSmallCodeUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.constellio.app.modules.es.connectors.ConnectorServicesFactory.forConnectorInstance;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.schemas.SchemaUtils.getMetadataUsedByCalculatedReferenceWithTaxonomyRelationship;
import static java.util.Arrays.asList;

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
	List<Record> processedRecords;
	boolean dryRun;

	public ClassifyConnectorRecordInTaxonomyExecutor(Record record, ClassifyConnectorFolderActionParameters params,
													 AppLayerFactory appLayerFactory, User currentUser, String robotId,
													 List<Record> processedRecords, boolean dryRun) {
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.appLayerFactory = appLayerFactory;
		this.record = record;
		this.params = params;
		this.currentUser = currentUser;
		this.rm = new RMSchemasRecordsServices(record.getCollection(), appLayerFactory);
		this.es = new ESSchemasRecordsServices(record.getCollection(), appLayerFactory);
		this.robots = new RobotSchemaRecordServices(record.getCollection(), appLayerFactory);
		this.recordServices = modelLayerFactory.newRecordServices();
		this.contentManager = modelLayerFactory.getContentManager();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.robotId = robotId;
		this.processedRecords = processedRecords;
		this.dryRun = dryRun;
	}

	public void execute() {
		transaction = new Transaction();
		transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);

		// TODO Test
		// transaction.setSkippingRequiredValuesValidation(true);

		connectorFolder = es.wrapConnectorDocument(record);
		classifyConnectorFolderInTaxonomy();

		if (!dryRun) {
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
					// FIXME sharepoint
					if (documentInTransaction.getSchemaCode().startsWith(ConnectorSmbDocument.SCHEMA_TYPE
																		 + "_") /*|| documentInTransaction.getSchemaCode().startsWith(ConnectorSharepointDocument.SCHEMA_TYPE + "_")*/) {
						ConnectorDocument<?> connectorDocument = es.wrapConnectorDocument(documentInTransaction);
						connectorServices(connectorDocument).deleteDocumentOnRemoteComponent(connectorDocument);
						logguerMessage(String.format("Document '%s' supprimé suite à sa classification dans Constellio",
								connectorDocument.getURL()));
					}
				}
			}
		}
	}

	private void logguerMessage(String message) {
		if (!dryRun) {
			try {
				LOGGER.info(message);
				recordServices.add(robots.newRobotLog().setTitle(message).setRobot(robotId));
			} catch (RecordServicesException e1) {
				LOGGER.error("Failed to create the robot error log", e1);
				throw new RuntimeException("Failed to create the robot error log", e1);
			}
		}
	}

	private void addNewExclusionToConnector(int i) {
		// TODO TEST!!!
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

	// TODO Test

	private void classifyConnectorFolderInTaxonomy() {

		String fullConnectorDocPath = connectorFolder.getURL();
		String legacyId = fullConnectorDocPath;
		if (connectorFolder.getSchemaCode().startsWith(ConnectorSmbFolder.SCHEMA_TYPE + "_")) {
			legacyId = connectorFolder.get(ConnectorSmbFolder.CONNECTOR_URL);
		}
		if (params.getInTaxonomy() != null) {

			Taxonomy targetTaxonomy = modelLayerFactory.getTaxonomiesManager()
					.getEnabledTaxonomyWithCode(connectorFolder.getCollection(), params.getInTaxonomy());
			String conceptTypeCode = targetTaxonomy.getSchemaTypes().get(0);
			MetadataSchemaType conceptType = modelLayerFactory.getMetadataSchemasManager()
					.getSchemaTypes(connectorFolder.getCollection()).getSchemaType(conceptTypeCode);
			String metadataCode = conceptType.getDefaultSchema().getCode() + "_" + Schemas.CODE.getLocalCode();
			Metadata codeMetadata = conceptType.getMetadata(metadataCode);

			classifyForPath(fullConnectorDocPath, targetTaxonomy, codeMetadata, legacyId);
		} else {
			processFolderWithoutTaxonomy(connectorFolder.getTitle(), legacyId);
		}
	}

	private void classifyForPath(String fullConnectorDocPath, Taxonomy targetTaxonomy, Metadata codeMetadata,
								 String legacyId) {

		ClassifiedRecordPathInfo recordPathInfo = new ClassifyConnectorHelper(modelLayerFactory)
				.extractInfoFromPath(fullConnectorDocPath, params.getPathPrefix(), params.getDelimiter(), codeMetadata);

		if (recordPathInfo != null) {
			processFolder(legacyId, targetTaxonomy, codeMetadata, recordPathInfo);
		}
	}

	private void processFolder(String fullConnectorDocPath, Taxonomy targetTaxonomy, Metadata codeMetadata,
							   ClassifiedRecordPathInfo recordUrlInfo) {
		LOGGER.info("Process Folder : [" + fullConnectorDocPath + ", " + targetTaxonomy + ", " + codeMetadata.getLocalCode()
					+ ", " + recordUrlInfo + "]");
		String folderName = recordUrlInfo.getLastPathSegment();
		MetadataSchema folderSchema = rm.folder.schema();
		Metadata legacyIdMetadata = folderSchema.getMetadata(Schemas.LEGACY_ID.getLocalCode());
		Record rmRecord = recordServices.getRecordByMetadata(legacyIdMetadata, fullConnectorDocPath);
		Folder rmFolder;
		if (rmRecord == null) {
			String folderTypeId = params.getFolderTypeId();
			Record parentConcept = recordUrlInfo.getConceptWhereRecordIsCreated();
			List<String> calculatedMetadatas = new ArrayList<>();
			if (parentConcept != null) {
				rmFolder = classifyFolderInConcept(fullConnectorDocPath, targetTaxonomy, folderName, folderSchema, parentConcept,
						folderTypeId, calculatedMetadatas);
			} else if (targetTaxonomy != null) {
				rmFolder = classifyFolderInParentFolder(fullConnectorDocPath, folderName, folderTypeId, calculatedMetadatas);
			} else {
				if (folderTypeId != null) {
					rmFolder = rm.newFolderWithType(folderTypeId);
				} else {
					rmFolder = rm.newFolder();
				}

				rmFolder.setCreatedByRobot(robotId);
			}
			RecordUtils.copyMetadatas(connectorFolder, rmFolder);
			mapFolderMetadataFromMappingFile(folderName, rmFolder, fullConnectorDocPath, calculatedMetadatas, true);
			try {
				recordServices.validateRecordInTransaction(rmFolder.getWrappedRecord(), transaction);
			} catch (ValidationException e) {
				throw new ValidationRuntimeException(e.getErrors());
			}
			classifyDocumentsFromFolder(rmFolder);
		} else {
			rmFolder = rm.wrapFolder(rmRecord);
			mapFolderMetadataFromMappingFile(folderName, rmFolder, fullConnectorDocPath, new ArrayList<String>(), false);
			classifyDocumentsFromFolder(rmFolder);
		}
		// if (params.getActionAfterClassification().isConnectorDocumentExcluded()) {
		// markAsUnfetched(connectorFolder);
		// }
	}

	private void processFolderWithoutTaxonomy(String folderName, String url) {
		LOGGER.info("Process Folder Without Taxonomy : [" + folderName + ", " + url + "]");
		MetadataSchema folderSchema = rm.folder.schema();
		Metadata legacyIdMetadata = folderSchema.getMetadata(Schemas.LEGACY_ID.getLocalCode());
		Record rmRecord = recordServices.getRecordByMetadata(legacyIdMetadata, url);
		Folder rmFolder;
		String parentPath = getParentPath(url, folderName);
		Folder parentFolder = rm.getFolderWithLegacyId(parentPath);
		boolean isANewFolder;

		if (rmRecord == null) {
			String folderTypeId = params.getFolderTypeId();
			if (folderTypeId != null) {
				rmFolder = rm.newFolderWithType(folderTypeId);
			} else {
				rmFolder = rm.newFolder();
			}
			rmFolder.setTitle(folderName);
			rmFolder.setLegacyId(url);
			isANewFolder = true;

		} else {
			rmFolder = rm.wrapFolder(rmRecord);
			rmFolder.getWrappedRecord().set(Schemas.LOGICALLY_DELETED_STATUS, false);
			isANewFolder = false;
		}

		rmFolder.setCreatedByRobot(robotId);
		mapFolderMetadataFromMappingFile(folderName, rmFolder, url, new ArrayList<String>(), isANewFolder);
		if (rmFolder.getParentFolder() == null) {
			rmFolder.setParentFolder(parentFolder);
			if (params.getDefaultParentFolder() != null && parentFolder == null) {
				rmFolder.setParentFolder(params.getDefaultParentFolder());
			}
		}
		recordServices.recalculate(rmFolder);
		rmFolder.setFormModifiedOn(connectorFolder.getLastModified());
		rmFolder.setFormCreatedOn(connectorFolder.getCreatedOn());
		classifyDocumentsFromFolder(rmFolder);

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

	private void mapFolderMetadataFromMappingFile(String folderName, Folder rmFolder, String folderURL,
												  List<String> previouslyCalculatedMetadatas, boolean isANewFolder) {
		Content folderMapping = params.getFolderMapping();

		Map<String, Object> defaultValuesOfSchema = new HashMap<>();
		if (isANewFolder) {
			MetadataSchema schemaOfFolder = modelLayerFactory.getMetadataSchemasManager().getSchemaOf(rmFolder.getWrappedRecord());
			for (Metadata metadata : schemaOfFolder.getMetadatas().onlyWithDefaultValue()) {
				if (!previouslyCalculatedMetadatas.contains(metadata.getLocalCode())) {
					defaultValuesOfSchema.put(metadata.getLocalCode(), metadata.getDefaultValue());
				}
			}
		}

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
				setOpeningDateFromCreatedOnOrLastModifiedIfNull(rmFolder);
				useDefaultValuesInMissingFields(folderEntry, rmFolder, defaultValuesOfSchema);
			} else {
				setOpeningDateFromCreatedOnOrLastModifiedIfNull(rmFolder);
				useAllDefaultValuesFromParams(rmFolder, defaultValuesOfSchema);
			}
		} else {
			setOpeningDateFromCreatedOnOrLastModifiedIfNull(rmFolder);
			useAllDefaultValuesFromParams(rmFolder, defaultValuesOfSchema);
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

	private Folder classifyFolderInParentFolder(String fullConnectorDocPath, String pathPart, String folderTypeId,
												List<String> calculatedMetadatas) {
		String parentPath = getParentPath(fullConnectorDocPath, pathPart);

		Folder parentFolder = rm.getFolderWithLegacyId(parentPath);
		Folder newRmFolder;
		if (folderTypeId != null) {
			newRmFolder = rm.newFolderWithType(folderTypeId);
		} else {
			newRmFolder = rm.newFolder();
		}
		newRmFolder.setCreatedByRobot(robotId);
		newRmFolder.setFormModifiedOn(connectorFolder.getLastModified());
		newRmFolder.setFormCreatedOn(connectorFolder.getCreatedOn());
		if (parentFolder != null) {
			newRmFolder.setOpenDate(parentFolder.getOpenDate());
			newRmFolder.setCloseDateEntered(parentFolder.getCloseDateEntered());
		}
		newRmFolder.setParentFolder(parentFolder).setTitle(pathPart).setLegacyId(fullConnectorDocPath);
		calculatedMetadatas.addAll(asList(Folder.CREATED_BY_ROBOT, Folder.PARENT_FOLDER, Folder.FORM_MODIFIED_ON, Folder.FORM_CREATED_ON, Schemas.LEGACY_ID.getLocalCode()));
		return newRmFolder;
	}

	private Folder classifyFolderInConcept(String fullConnectorDocPath, Taxonomy targetTaxonomy, String pathPart,
										   MetadataSchema folderSchema, Record parentConcept, String folderTypeId,
										   List<String> calculatedMetadatas) {
		Folder newRmFolder;
		if (folderTypeId != null) {
			newRmFolder = rm.newFolderWithType(folderTypeId);
		} else {
			newRmFolder = rm.newFolder();
		}

		newRmFolder.setCreatedByRobot(robotId);
		Metadata taxoMetadata = folderSchema.getTaxonomyRelationshipReferences(asList(targetTaxonomy)).get(0);
		if (taxoMetadata.getDataEntry().getType() == DataEntryType.CALCULATED) {
			taxoMetadata = getMetadataUsedByCalculatedReferenceWithTaxonomyRelationship(folderSchema, taxoMetadata);
		}

		newRmFolder.set(taxoMetadata.getLocalCode(), parentConcept.getId()).setTitle(pathPart);
		newRmFolder.setFormModifiedOn(connectorFolder.getLastModified());
		newRmFolder.setFormCreatedOn(connectorFolder.getCreatedOn());
		newRmFolder.setLegacyId(fullConnectorDocPath);
		calculatedMetadatas.addAll(asList(Folder.CREATED_BY_ROBOT, taxoMetadata.getLocalCode(), Folder.FORM_MODIFIED_ON, Folder.FORM_CREATED_ON, Schemas.LEGACY_ID.getLocalCode()));
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
			e.printStackTrace();
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


	private String getParentPath(String fullPath, String pathPart) {
		StringBuilder builder = new StringBuilder(fullPath);
		int lastSlash = fullPath.lastIndexOf(pathPart + "/");

		if (lastSlash == -1) {
			int indexOfSlash = fullPath.indexOf("/");
			int indexOfBackslash = fullPath.indexOf("\\");
			if (indexOfSlash > indexOfBackslash) {
				return fullPath.substring(indexOfSlash);
			} else {
				return fullPath.substring(indexOfBackslash);
			}

		} else {
			builder.replace(lastSlash, lastSlash + pathPart.length() + 1, "");
			return builder.toString();
		}
		//
		// if (fullPath.endsWith("/")) {
		// fullPath = fullPath.substring(0, fullPath.length() - 1);
		// }
		//
		// return org.apache.commons.lang3.StringUtils.substringBeforeLast(fullPath, "/");
	}

	private MetadataSchema adjustFolderSchema(Folder rmFolder, Map<String, String> folderEntry) {
		MetadataSchema folderSchema = rm.folder.schema();
		Metadata codeMetadata = rm.folderTypeSchema().getMetadata(Schemas.CODE.getLocalCode());
		String folderTypeCode = folderEntry.get(Folder.TYPE);
		if (StringUtils.isNotBlank(folderTypeCode)) {
			Record folderTypeRecord = recordServices.getRecordByMetadata(codeMetadata, folderTypeCode);
			FolderType folderType = rm.wrapFolderType(folderTypeRecord);
			folderSchema = rm.folderSchemaFor(folderType);
			rmFolder.getWrappedRecord().changeSchema(rm.folder.schema(), folderSchema);
		}
		return folderSchema;
	}

	private void setOpeningDateFromCreatedOnOrLastModifiedIfNull(Folder rmFolder) {
		if (rmFolder.getOpeningDate() == null) {
			LocalDateTime connectorFolderCreatedOn = connectorFolder.getCreatedOn();
			LocalDateTime connectorFolderLastModified = connectorFolder.getLastModified();
			if (connectorFolderCreatedOn != null) {
				rmFolder.setOpenDate(connectorFolderCreatedOn.toLocalDate());
			} else if (connectorFolderLastModified != null) {
				rmFolder.setOpenDate(connectorFolderLastModified.toLocalDate());
			}
		}
	}

	private void useAllDefaultValuesFromParams(Folder rmFolder, Map<String, Object> defaultValuesOfSchema) {

		// rmFolder.setParentFolder(params.getDefaultParentFolder());
		String taxonomy = params.getInTaxonomy();
		if (rmFolder.getAdministrativeUnitEntered() == null || isMetadataAtDefaultValue(rmFolder, defaultValuesOfSchema, Folder.ADMINISTRATIVE_UNIT_ENTERED)) {
			rmFolder.setAdministrativeUnitEntered(params.getDefaultAdminUnit());
		}

		if (rmFolder.getCategoryEntered() == null || isMetadataAtDefaultValue(rmFolder, defaultValuesOfSchema, Folder.CATEGORY_ENTERED)) {
			rmFolder.setCategoryEntered(params.getDefaultCategory());
		}
		if (rmFolder.getUniformSubdivisionEntered() == null || isMetadataAtDefaultValue(rmFolder, defaultValuesOfSchema, Folder.UNIFORM_SUBDIVISION_ENTERED)) {
			rmFolder.setUniformSubdivisionEntered(params.getDefaultUniformSubdivision());
		}
		if (rmFolder.getRetentionRuleEntered() == null || isMetadataAtDefaultValue(rmFolder, defaultValuesOfSchema, Folder.RETENTION_RULE_ENTERED)) {
			rmFolder.setRetentionRuleEntered(params.getDefaultRetentionRule());

			if (rmFolder.getRetentionRuleEntered() == null && rmFolder.getCategoryEntered() != null) {
				Category defaultCategory = rm.getCategory(rmFolder.getCategoryEntered());
				List<String> retentionRules = defaultCategory.getRententionRules();
				if (!retentionRules.isEmpty()) {
					rmFolder.setRetentionRuleEntered(retentionRules.get(0));
				}
			}
		}
		if (rmFolder.getCopyStatusEntered() == null || isMetadataAtDefaultValue(rmFolder, defaultValuesOfSchema, Folder.COPY_STATUS_ENTERED)) {
			rmFolder.setCopyStatusEntered(params.getDefaultCopyStatus());
		}
		if (rmFolder.getOpenDate() == null || (!rmFolder.getWrappedRecord().isSaved() && params.getDefaultOpenDate() != null)
			|| isMetadataAtDefaultValue(rmFolder, defaultValuesOfSchema, Folder.OPENING_DATE)) {
			rmFolder.setOpenDate(params.getDefaultOpenDate());
		}

		addMediumTypeToFolder(rmFolder);
	}

	private boolean isMetadataAtDefaultValue(Folder rmFolder, Map<String, Object> defaultValuesOfSchema,
											 String localCode) {
		if (defaultValuesOfSchema.containsKey(localCode)) {
			return Objects.equals(rmFolder.get(localCode), defaultValuesOfSchema.get(localCode));
		} else {
			return false;
		}
	}

	private void addMediumTypeToFolder(Folder folder) {
		Metadata metadata = folder.getSchema().get(Folder.MEDIUM_TYPES);
		String referencedTypeCode = metadata.getAllowedReferences().getTypeWithAllowedSchemas();
		Metadata codeMetadata = robots.getTypes().getDefaultSchema(referencedTypeCode).getMetadata("code");

		Record mediumType = recordServices.getRecordByMetadata(codeMetadata, "MD");
		if (mediumType == null) {
			mediumType = recordServices.getRecordByMetadata(codeMetadata, "DM");
		}

		if (mediumType != null) {
			folder.setMediumTypes(recordServices.getRecordsById(robots.getCollection(), asList(mediumType.getId())));
		}
	}

	private void useDefaultValuesInMissingFields(Map<String, String> folderEntry, Folder rmFolder,
												 Map<String, Object> defaultValuesOfSchema) {
		if ((folderEntry.get(Folder.PARENT_FOLDER) == null || isMetadataAtDefaultValue(rmFolder, defaultValuesOfSchema, Folder.ADMINISTRATIVE_UNIT_ENTERED))
			&& rmFolder.getParentFolder() == null
			&& params.getDefaultParentFolder() != null) {
			rmFolder.setParentFolder(params.getDefaultParentFolder());
		}
		if ((rmFolder.getAdministrativeUnitEntered() == null || isMetadataAtDefaultValue(rmFolder, defaultValuesOfSchema, Folder.ADMINISTRATIVE_UNIT_ENTERED))
			&& folderEntry.get(Folder.ADMINISTRATIVE_UNIT_ENTERED) == null
			&& params.getDefaultAdminUnit() != null) {
			rmFolder.setAdministrativeUnitEntered(params.getDefaultAdminUnit());
		}
		if ((rmFolder.getCategoryEntered() == null || isMetadataAtDefaultValue(rmFolder, defaultValuesOfSchema, Folder.ADMINISTRATIVE_UNIT_ENTERED))
			&& folderEntry.get(Folder.CATEGORY_ENTERED) == null
			&& params.getDefaultCategory() != null) {
			rmFolder.setCategoryEntered(params.getDefaultCategory());
		}
		if ((folderEntry.get(Folder.RETENTION_RULE_ENTERED) == null || isMetadataAtDefaultValue(rmFolder, defaultValuesOfSchema, Folder.ADMINISTRATIVE_UNIT_ENTERED))
			&& params.getDefaultRetentionRule() != null) {
			rmFolder.setRetentionRuleEntered(params.getDefaultRetentionRule());
		}
		if ((folderEntry.get(Folder.COPY_STATUS_ENTERED) == null || isMetadataAtDefaultValue(rmFolder, defaultValuesOfSchema, Folder.ADMINISTRATIVE_UNIT_ENTERED))
			&& params.getDefaultCopyStatus() != null) {
			rmFolder.setCopyStatusEntered(params.getDefaultCopyStatus());
		}
		if ((folderEntry.get(Folder.UNIFORM_SUBDIVISION) == null || isMetadataAtDefaultValue(rmFolder, defaultValuesOfSchema, Folder.ADMINISTRATIVE_UNIT_ENTERED))
			&& params.getDefaultUniformSubdivision() != null) {
			rmFolder.setUniformSubdivisionEntered(params.getDefaultUniformSubdivision());
		}
	}

	private void setMetadataInRmRecord(RecordWrapper rmRecord, MetadataSchema schema, String metadataCode,
									   String value) {
		Metadata metadata = schema.getMetadata(metadataCode);
		switch (metadata.getType()) {
			case STRING:
				if (metadata.isMultivalue()) {
					rmRecord.set(metadataCode, asList(value.split(";")));
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

	public Map<String, ClassifiedDocument> classifyDocuments(String inRmFolder,
															 List<ConnectorDocument<?>> documentsRecords,
															 Boolean majorVersions) {
		Map<String, ClassifiedDocument> createdRecordsByUrls = new HashMap<>();
		for (ConnectorDocument document : documentsRecords) {
			try {
				ClassifiedDocument classifiedDocument = classifyDocument(document, inRmFolder, majorVersions);
				createdRecordsByUrls.put(document.getUrl(), classifiedDocument);
				processedRecords.add(document.getWrappedRecord());
			} catch (ClassifyServicesRuntimeException_CannotClassifyAsDocument e) {
				LOGGER.warn("Cannot classify '" + document.getUrl() + "'", e);
			}
		}
		return createdRecordsByUrls;
	}

	private ClassifiedDocument classifyDocument(ConnectorDocument connectorDocument, String inRmFolder,
												Boolean majorVersions) {

		ContentVersionDataSummary newVersionDataSummary = null;
		try {
			String fullUrl;
			if (connectorDocument.getSchemaCode().startsWith(ConnectorSmbDocument.SCHEMA_TYPE + "_")) {
				fullUrl = connectorDocument.get(ConnectorSmbDocument.CONNECTOR_URL);
			} else {
				fullUrl = connectorDocument.getURL();
			}
			Document document = rm.getDocumentByLegacyId(fullUrl);

			if (document == null) {
				String documentTypeId = params.getDocumentTypeId();
				if (documentTypeId != null) {
					document = rm.newDocumentWithType(documentTypeId);
				} else {
					document = rm.newDocument();
				}
				document.setCreatedByRobot(robotId);
				document.setLegacyId(fullUrl);
			}
			document.set(Schemas.LOGICALLY_DELETED_STATUS, false);
			document.setTitle(connectorDocument.getTitle());
			document.setFolder(inRmFolder);
			document.setFormModifiedOn(connectorDocument.getLastModified());
			document.setFormCreatedOn(connectorDocument.getCreatedOn());

			RecordUtils.copyMetadatas(connectorDocument, document);
			try {
				List<String> availableVersions = connectorServices(connectorDocument)
						.getAvailableVersions(connectorDocument.getConnector(), connectorDocument);
				for (String availableVersion : availableVersions) {
					InputStream versionStream = connectorServices(connectorDocument).newContentInputStream(connectorDocument,
							CLASSIFY_DOCUMENT, availableVersion);

					UploadOptions options = new UploadOptions(connectorDocument.getTitle())
							.setHandleDeletionOfUnreferencedHashes(false);
					newVersionDataSummary = contentManager.upload(versionStream, options)
							.getContentVersionDataSummary();
					addVersionToDocument(connectorDocument, availableVersion, newVersionDataSummary, document);
				}
			} catch (UnsupportedOperationException ex) {
				InputStream inputStream = connectorServices(connectorDocument).newContentInputStream(connectorDocument,
						CLASSIFY_DOCUMENT);

				String version;
				if (document.getContent() != null) {
					version = ContentImpl.getVersionAfter(document.getContent().getCurrentVersion().getVersion(), majorVersions);
				} else {
					version = (majorVersions ? "1.0" : "0.1");
				}

				UploadOptions options = new UploadOptions(connectorDocument.getTitle())
						.setHandleDeletionOfUnreferencedHashes(false);
				newVersionDataSummary = contentManager.upload(inputStream, options).getContentVersionDataSummary();
				addVersionToDocument(connectorDocument, version, newVersionDataSummary, document);
			}

			return new ClassifiedDocument(connectorDocument, document);
		} catch (ConnectorServicesRuntimeException_CannotDownloadDocument | IcapException e) {
			Exception exception = e;

			if (e instanceof IcapException) {
				if (e instanceof IcapException.ThreatFoundException) {
					exception = new IcapException(
							$(e, ((IcapException) e).getFileName(), ((IcapException.ThreatFoundException) e).getThreatName()));
				} else {
					if (e.getCause() == null) {
						exception = new IcapException($(e, ((IcapException) e).getFileName()));
					} else {
						exception = new IcapException($(e, ((IcapException) e).getFileName()), e.getCause());
					}
				}
			}

			if (newVersionDataSummary != null) {
				contentManager.markForDeletionIfNotReferenced(newVersionDataSummary.getHash());
			}
			throw new ClassifyServicesRuntimeException_CannotClassifyAsDocument(connectorDocument, exception);
		}

	}

	private void addVersionToDocument(ConnectorDocument connectorDocument, String versionNumber,
									  ContentVersionDataSummary newVersionDataSummary, Document document) {
		if (document.getContent() != null) {
			if (!newVersionDataSummary.getHash().equals(document.getContent().getCurrentVersion().getHash())) {
				document.getContent().updateContentWithVersionAndName(currentUser, newVersionDataSummary, versionNumber,
						connectorDocument.getTitle());
				document.setContent(document.getContent());
			}
		} else {
			document.setContent(contentManager.createWithVersion(currentUser, connectorDocument.getTitle(), newVersionDataSummary,
					versionNumber));
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