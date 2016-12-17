package com.constellio.app.modules.complementary.esRmRobots.services;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.constellio.model.services.contents.icap.IcapException;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.complementary.esRmRobots.services.ClassifyServicesRuntimeException.ClassifyServicesRuntimeException_CannotClassifyAsDocument;
import com.constellio.app.modules.es.connectors.ConnectorServicesFactory;
import com.constellio.app.modules.es.connectors.ConnectorServicesRuntimeException;
import com.constellio.app.modules.es.connectors.ConnectorUtilsServices;
import com.constellio.app.modules.es.connectors.smb.ConnectorSmbRuntimeException;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentImpl;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class SmbClassifyServices {

	private static final Logger LOGGER = LoggerFactory.getLogger(SmbClassifyServices.class);
	private static final String CLASSIFY_DOCUMENT = "ClassifyServices-ClassifySmbDocument";

	private transient RMSchemasRecordsServices rmSchemasRecordsServices;
	private transient ESSchemasRecordsServices es;
	private transient RecordServices recordServices;
	private transient SearchServices searchServices;
	private transient ContentManager contentManager;
	private transient AppLayerFactory appLayerFactory;
	private User currentUser;
	private transient ConnectorServicesFactory connectorServicesFactory;

	public SmbClassifyServices(String collection, AppLayerFactory appLayerFactory, User currentUser) {
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
		es = new ESSchemasRecordsServices(collection, appLayerFactory);
		recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		contentManager = appLayerFactory.getModelLayerFactory().getContentManager();
		this.appLayerFactory = appLayerFactory;
		this.currentUser = currentUser;
		this.connectorServicesFactory = new ConnectorServicesFactory();
	}

	public SmbClassifyServices(String collection, AppLayerFactory appLayerFactory, User currentUser,
			ConnectorServicesFactory connectorServicesFactory) {
		this(collection, appLayerFactory, currentUser);
		this.connectorServicesFactory = connectorServicesFactory;
	}

	public List<String> classifyConnectorDocuments(String inRmFolder, String documentType, List<String> connectorDocumentIds,
			Boolean majorVersions,
			boolean excludeDocuments) {

		List<String> createdRecordIds = new ArrayList<>();
		for (String connectorDocumentId : connectorDocumentIds) {
			ConnectorDocument connectorDocument = es.getConnectorDocument(connectorDocumentId);
			try {
				createdRecordIds.add(classifyDocument(connectorDocument, inRmFolder, documentType, majorVersions,
						excludeDocuments, ""));
			} catch (ClassifyServicesRuntimeException_CannotClassifyAsDocument e) {
				LOGGER.warn("Cannot classify '" + connectorDocument.getURL() + "'", e);
			}
		}
		return createdRecordIds;
	}

	public String classifyDocument(ConnectorDocument connectorDocument, String inRmFolder, String documentTypeId,
			Boolean majorVersions, boolean excludeDocuments, String versions) {

		ContentVersionDataSummary newVersionDataSummary = null;

		ConnectorUtilsServices<?> connectorUtilsServices = connectorServicesFactory
				.forConnectorDocumentNonStatic(appLayerFactory, connectorDocument);
		try {
			Document document = rmSchemasRecordsServices.getDocumentByLegacyId(connectorDocument.getUrl());

			if (document == null) {
				document = rmSchemasRecordsServices.newDocumentWithType(documentTypeId);
				document.setLegacyId(connectorDocument.getUrl());
			}
			document.set(Schemas.LOGICALLY_DELETED_STATUS, false);

			document.setTitle(connectorDocument.getTitle());
			document.setFolder(inRmFolder);
			document.setLegacyId(connectorDocument.getUrl());

			Content content;
			if (StringUtils.isEmpty(versions)) {
				InputStream inputStream = connectorUtilsServices.newContentInputStream(connectorDocument, CLASSIFY_DOCUMENT);
				newVersionDataSummary = contentManager.upload(inputStream, false, true, connectorDocument.getTitle());
				//Content content;
				if (majorVersions) {
					content = contentManager.createMajor(currentUser, connectorDocument.getTitle(), newVersionDataSummary);
				} else {
					content = contentManager.createMinor(currentUser, connectorDocument.getTitle(), newVersionDataSummary);
				}
			} else {
				// Process versions? Info should come from config, empty is do not check for versions, possibly regex , ex for major .*\.0
				// If so, which ones?
				List<String> availableVersions = connectorUtilsServices
						.getAvailableVersions(connectorDocument.getConnector(), connectorDocument);
				Map<String, ContentVersion> historyMap = getContentVersions(connectorDocument, connectorUtilsServices,
						availableVersions);
				ContentVersion currentContentVersion = removeFromHistoryAndReturnCurrentContentVersion(historyMap);
				List<ContentVersion> history = new ArrayList<>(historyMap.values());

				DataLayerFactory dataLayerFactory = appLayerFactory.getModelLayerFactory().getDataLayerFactory();
				String contentId = dataLayerFactory.getUniqueIdGenerator().next();
				content = ContentImpl.create(contentId, currentContentVersion, history);
			}

			document.setContent(content);
			RecordUtils.copyMetadatas(connectorDocument, document);
			addUpdateRecordSkippingRequiredValueValidation(document);

			ConnectorInstance connectorInstance = es
					.getConnectorInstance(connectorDocument.getConnector());
			if (excludeDocuments) {
				connectorUtilsServices.addExcludedUrlsTo(asList(connectorDocument.getUrl()), connectorInstance);
				recordServices.update(connectorInstance);
				recordServices.update(connectorDocument.setFetched(false));
			}
			return document.getId();
		} catch (ConnectorSmbRuntimeException |
				RecordServicesException |
				ConnectorServicesRuntimeException |
				IcapException e) {

			Exception exception = e;

			if (e instanceof IcapException) {
				if (e instanceof IcapException.ThreatFoundException) {
					exception = new IcapException($(e, ((IcapException) e).getFileName(), ((IcapException.ThreatFoundException) e).getThreatName()));
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

	private Map<String, ContentVersion> getContentVersions(ConnectorDocument connectorDocument,
			ConnectorUtilsServices<?> connectorUtilsServices,
			List<String> availableVersions) {
		Map<String, ContentVersion> historyMap = new HashMap<>();
		for (String availableVersion : availableVersions) {
			// TODO Filter which version make it to the record

			InputStream availableVersionInputStream = connectorUtilsServices
					.newContentInputStream(connectorDocument, CLASSIFY_DOCUMENT, availableVersion);
			ContentVersionDataSummary contentVersionDataSummary = contentManager
					.upload(availableVersionInputStream, false, true, connectorDocument.getTitle());
			String filename = "zFileName";
			String version = availableVersion;
			String lastModifiedBy = currentUser.getUsername();
			LocalDateTime lastModificationDateTime = TimeProvider.getLocalDateTime();
			String comment = "";
			ContentVersion contentVersion = new ContentVersion(contentVersionDataSummary, filename, version, lastModifiedBy,
					lastModificationDateTime, comment);
			historyMap.put(version, contentVersion);
		}
		return historyMap;
	}

	private ContentVersion removeFromHistoryAndReturnCurrentContentVersion(Map<String, ContentVersion> historyMap) {
		LinkedList<String> keys = new LinkedList<>(historyMap.keySet());
		if (keys.isEmpty()) {
			// TODO Do something if empty
		}
		Collections.sort(keys);
		String currentVersion = keys.getLast();
		ContentVersion currentContentVersion = historyMap.remove(currentVersion);
		return currentContentVersion;
	}

	public List<String> classifySmbFolder(String smbFolderId, String rmFolderId, Boolean majorVersions) {

		List<String> createdDocumentsIds = new ArrayList<>();
		return classifyFolder(smbFolderId, rmFolderId, createdDocumentsIds, majorVersions);
	}

	private List<String> classifyFolder(String smbFolderId, String rmFolderId, List<String> createdDocumentsIds,
			Boolean majorVersions) {

		for (String childConnectorSmbFolderId : getChildrenConnectorSmbFolders(smbFolderId)) {
			List<String> newDocumentsRecordsIds = getChildrenDocuments(childConnectorSmbFolderId);
			ConnectorSmbFolder childConnectorSmbFolder = es
					.getConnectorSmbFolder(childConnectorSmbFolderId);

			Folder createdFolder = rmSchemasRecordsServices.getFolder(rmFolderId);
			Folder folder = rmSchemasRecordsServices.newFolder();
			folder.setTitle(childConnectorSmbFolder.getTitle());
			folder.setParentFolder(rmFolderId);
			folder.setAdministrativeUnitEntered(createdFolder.getAdministrativeUnitEntered());
			folder.setCategoryEntered(createdFolder.getCategoryEntered());
			folder.setRetentionRuleEntered(createdFolder.getRetentionRuleEntered());
			folder.setOpenDate(createdFolder.getOpenDate());
			folder.setCloseDateEntered(createdFolder.getCloseDateEntered());
			folder.setKeywords(createdFolder.getKeywords());
			folder.setCloseDateEntered(createdFolder.getCloseDateEntered());
			folder.setMediumTypes(createdFolder.getMediumTypes());
			folder.setType(createdFolder.getType());

			addUpdateRecordSkippingRequiredValueValidation(folder);

			createdDocumentsIds
					.addAll(classifyConnectorDocuments(folder.getId(), null, newDocumentsRecordsIds, majorVersions, true));

			classifyFolder(childConnectorSmbFolderId, folder.getId(), createdDocumentsIds, majorVersions);
		}

		List<String> newDocumentsRecordsIds = getChildrenDocuments(smbFolderId);
		createdDocumentsIds.addAll(classifyConnectorDocuments(rmFolderId, null, newDocumentsRecordsIds, majorVersions, true));

		ConnectorSmbFolder connectorSmbFolderToDelete = es
				.getConnectorSmbFolder(smbFolderId);
		ConnectorSmbInstance connectorSmbInstance = es
				.getConnectorSmbInstance(connectorSmbFolderToDelete.getConnector());
		List<String> exclusions = connectorSmbInstance.getExclusions();
		List<String> newExclusions = new ArrayList<>();
		newExclusions.addAll(exclusions);
		newExclusions.add(connectorSmbFolderToDelete.getUrl());
		connectorSmbInstance.setExclusions(newExclusions);
		try {
			recordServices.update(connectorSmbInstance);
			recordServices.update(connectorSmbFolderToDelete.setFetched(false));
		} catch (RecordServicesException | ConnectorSmbRuntimeException e) {
			throw new ClassifyServicesRuntimeException(e);
		}

		return createdDocumentsIds;
	}

	private List<String> getChildrenDocuments(String connectorSmbFolderId) {
		LogicalSearchQuery query = new LogicalSearchQuery(from(es.connectorSmbDocument.schemaType())
				.where(es.connectorSmbDocument.parent()).is(connectorSmbFolderId)
				.andWhere(Schemas.FETCHED).isTrue());
		return searchServices.searchRecordIds(query);
	}

	private List<String> getChildrenConnectorSmbFolders(String connectorSmbFolderId) {
		LogicalSearchQuery query = new LogicalSearchQuery(from(es.connectorSmbFolder.schemaType())
				.where(es.connectorSmbFolder.parent()).is(connectorSmbFolderId)
				.andWhere(Schemas.FETCHED).isTrue());
		return searchServices.searchRecordIds(query);
	}

	private void addUpdateRecordSkippingRequiredValueValidation(RecordWrapper record) {
		try {
			Transaction transaction = new Transaction();
			transaction.setSkippingRequiredValuesValidation(true);
			transaction.add(record);
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new ImpossibleRuntimeException(e);
		}

	}

}
