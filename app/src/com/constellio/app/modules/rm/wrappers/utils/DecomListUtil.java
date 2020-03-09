package com.constellio.app.modules.rm.wrappers.utils;

import com.constellio.app.modules.rm.model.enums.DecomListStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailStatus;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.records.BulkRecordTransactionHandler;
import com.constellio.model.services.records.BulkRecordTransactionHandlerOptions;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

public class DecomListUtil {

	private static final int RECORDS_PER_BATCH = 5000;

	public static List<String> getDecomListsForFolder(Folder folder) {
		List<String> decomIds = new ArrayList<>();
		decomIds.addAll(folder.getPreviousDecommissioningLists());
		decomIds.add(folder.getCurrentDecommissioningList());
		return decomIds;
	}

	public static List<String> getDecomListsForDocument(Document document) {
		List<String> decomIds = new ArrayList<>();
		decomIds.addAll(document.getPreviousDecommissioningLists());
		decomIds.add(document.getCurrentDecommissioningList());
		return decomIds;
	}

	public static void removeReferencesInDecomList(String collection, AppLayerFactory appLayerFactory,
												   DecommissioningList decomList, List<Record> recordsToRemove) {
		List<String> documentsToRemove = new ArrayList<>();
		List<String> foldersToRemove = new ArrayList<>();

		for (Record record : recordsToRemove) {
			if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
				foldersToRemove.add(record.getId());
			} else if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
				documentsToRemove.add(record.getId());
			}
		}

		removeDocumentsInDecomList(collection, appLayerFactory, decomList, documentsToRemove);
		removeFolderDetailsInDecomList(collection, appLayerFactory, decomList, foldersToRemove);
	}

	public static void setFolderDetailsInDecomList(String collection, AppLayerFactory appLayerFactory,
												   DecommissioningList decomList,
												   List<DecomListFolderDetail> folderDetails) {
		List<String> folderIds =
				folderDetails.stream().map(DecomListFolderDetail::getFolderId).collect(Collectors.toList());
		setFoldersInDecomList(collection, appLayerFactory, decomList, folderIds);
		decomList.setFolderDetails(folderDetails);
	}

	public static void setFolderDetailsInDecomList(String collection, AppLayerFactory appLayerFactory,
												   DecommissioningList decomList, List<String> folderIds,
												   FolderDetailStatus status) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

		setFoldersInDecomList(collection, appLayerFactory, decomList, folderIds);
		decomList.setFolderDetailsFor(rm.getFolders(folderIds), status);
	}

	public static void removeFolderDetailsInDecomList(String collection, AppLayerFactory appLayerFactory,
													  DecommissioningList decomList, List<String> folderIds) {
		removeFoldersInDecomList(collection, appLayerFactory, decomList, folderIds);
		decomList.removeFolderDetails(folderIds);
	}

	public static void addFolderDetailsInDecomList(String collection, AppLayerFactory appLayerFactory,
												   DecommissioningList decomList, List<String> folderIds,
												   FolderDetailStatus status) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

		addFoldersInDecomList(collection, appLayerFactory, decomList, folderIds);
		decomList.addFolderDetailsFor(status, rm.getFolders(folderIds).toArray(new Folder[0]));
	}

	private static void setFoldersInDecomList(String collection, AppLayerFactory appLayerFactory,
											  DecommissioningList decomList, List<String> folderIds) {
		List<String> alreadyInDecomListIds = getFoldersInDecomList(collection, appLayerFactory, decomList);
		alreadyInDecomListIds.removeAll(folderIds);
		removeFoldersInDecomList(collection, appLayerFactory, decomList, alreadyInDecomListIds);

		addFoldersInDecomList(collection, appLayerFactory, decomList, folderIds);
	}

	private static void removeFoldersInDecomList(String collection, AppLayerFactory appLayerFactory,
												 DecommissioningList decomList, List<String> folderIds) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

		List<Folder> folders = rm.getFolders(folderIds);
		for (Folder folder : folders) {
			if (decomList.getStatus() == DecomListStatus.PROCESSED) {
				folder.removePreviousDecommissioningList(decomList.getId());
			} else {
				folder.setCurrentDecommissioningList(null);
			}
		}

		List<Record> records = folders.stream().map(Folder::getWrappedRecord).collect(Collectors.toList());
		updateRecords(appLayerFactory, "removeFoldersInDecomList", records);
	}

	private static void addFoldersInDecomList(String collection, AppLayerFactory appLayerFactory,
											  DecommissioningList decomList, List<String> folderIds) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

		List<Folder> folders = rm.getFolders(folderIds);
		for (Folder folder : folders) {
			if (decomList.getStatus() == DecomListStatus.PROCESSED) {
				folder.addPreviousDecommissioningList(decomList.getId());
			} else {
				folder.setCurrentDecommissioningList(decomList.getId());
			}
		}

		List<Record> records = folders.stream().map(Folder::getWrappedRecord).collect(Collectors.toList());
		updateRecords(appLayerFactory, "addFoldersInDecomList", records);
	}

	public static void setDocumentsInDecomList(String collection, AppLayerFactory appLayerFactory,
											   DecommissioningList decomList, List<String> documentIds) {
		List<String> alreadyInDecomListIds = getDocumentsInDecomList(collection, appLayerFactory, decomList);
		alreadyInDecomListIds.removeAll(documentIds);
		removeDocumentsInDecomList(collection, appLayerFactory, decomList, alreadyInDecomListIds);

		addDocumentsInDecomList(collection, appLayerFactory, decomList, documentIds);
	}

	public static void removeDocumentsInDecomList(String collection, AppLayerFactory appLayerFactory,
												  DecommissioningList decomList, List<String> documentIds) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

		List<Document> documents = rm.getDocuments(documentIds);
		for (Document document : documents) {
			if (decomList.getStatus() == DecomListStatus.PROCESSED) {
				document.removePreviousDecommissioningList(decomList.getId());
			} else {
				document.setCurrentDecommissioningList(null);
			}
		}

		List<Record> records = documents.stream().map(Document::getWrappedRecord).collect(Collectors.toList());
		updateRecords(appLayerFactory, "removeDocumentsInDecomList", records);
	}

	public static void addDocumentsInDecomList(String collection, AppLayerFactory appLayerFactory,
											   DecommissioningList decomList, List<String> documentIds) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

		List<Document> documents = rm.getDocuments(documentIds);
		for (Document document : documents) {
			if (decomList.getStatus() == DecomListStatus.PROCESSED) {
				document.addPreviousDecommissioningList(decomList.getId());
			} else {
				document.setCurrentDecommissioningList(decomList.getId());
			}
		}

		List<Record> records = documents.stream().map(Document::getWrappedRecord).collect(Collectors.toList());
		updateRecords(appLayerFactory, "addDocumentsInDecomList", records);
	}

	private static void updateRecords(AppLayerFactory appLayerFactory, String name, List<Record> records) {
		if (records.isEmpty()) {
			return;
		}

		BulkRecordTransactionHandlerOptions options =
				new BulkRecordTransactionHandlerOptions().withRecordsPerBatch(RECORDS_PER_BATCH);
		BulkRecordTransactionHandler transactionHandler = new BulkRecordTransactionHandler(
				appLayerFactory.getModelLayerFactory().newRecordServices(), "DecomListUtil_" + name, options);

		transactionHandler.append(records);
		transactionHandler.closeAndJoin();
	}

	public static boolean isInActiveDecomList(Folder folder) {
		return StringUtils.isNotBlank(folder.getCurrentDecommissioningList());
	}

	public static List<String> getDocumentsInDecomList(String collection, AppLayerFactory appLayerFactory,
													   DecommissioningList decomList,
													   LogicalSearchCondition... extraCondition) {
		return getInDecomList(collection, appLayerFactory, decomList, Document.SCHEMA_TYPE, extraCondition);
	}

	public static List<String> getFoldersInDecomList(String collection, AppLayerFactory appLayerFactory,
													 DecommissioningList decomList,
													 LogicalSearchCondition... extraCondition) {
		return getInDecomList(collection, appLayerFactory, decomList, Folder.SCHEMA_TYPE, extraCondition);
	}

	public static LogicalSearchQuery getDocumentsInDecomListQuery(String collection, AppLayerFactory appLayerFactory,
																  DecommissioningList decomList,
																  LogicalSearchCondition... extraConditions) {
		return getInDecomListQuery(collection, appLayerFactory, decomList, Document.SCHEMA_TYPE, extraConditions);
	}

	private static List<String> getInDecomList(String collection, AppLayerFactory appLayerFactory,
											   DecommissioningList decomList, String schemaType,
											   LogicalSearchCondition... extraCondition) {
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		LogicalSearchQuery query =
				getInDecomListQuery(collection, appLayerFactory, decomList, schemaType, extraCondition);
		return searchServices.searchRecordIds(query);
	}

	private static LogicalSearchQuery getInDecomListQuery(String collection, AppLayerFactory appLayerFactory,
														  DecommissioningList decomList, String schemaType,
														  LogicalSearchCondition... extraConditions) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

		MetadataSchemaType type = schemaType.equals(Folder.SCHEMA_TYPE)
								  ? rm.folderSchemaType()
								  : rm.documentSchemaType();
		Metadata previousMetadata = schemaType.equals(Folder.SCHEMA_TYPE)
									? rm.folder.previousDecommissioningLists()
									: rm.document.previousDecommissioningLists();
		Metadata currentMetadata = schemaType.equals(Folder.SCHEMA_TYPE)
								   ? rm.folder.currentDecommissioningList()
								   : rm.document.currentDecommissioningList();

		List<LogicalSearchCondition> conditions = new ArrayList<>();
		if (decomList.getStatus() == DecomListStatus.PROCESSED) {
			conditions.add(where(previousMetadata).isContaining(Collections.singletonList(decomList.getId())));
		} else {
			conditions.add(where(currentMetadata).isEqualTo(decomList.getId()));
		}

		Collections.addAll(conditions, extraConditions);

		return new LogicalSearchQuery(from(type).whereAllConditions(conditions));
	}
}
