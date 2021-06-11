package com.constellio.model.services.search.zipContents;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasInExceptEvents;

public class ZipContentsService {
	private static Logger LOGGER = Logger.getLogger(ZipContentsService.class);
	private static final String TMP_CONTENTS_FOLDER = "ZipContentsService-zipContentsOfRecords";
	private static final String CONTENTS_FOLDER = "ZipContentsService-getContents";
	ModelLayerFactory modelLayerFactory;
	ZipService zipService;
	RecordServices recordServices;
	SearchServices searchServices;

	ContentManager contentManager;
	private IOServices ioServices;
	private MetadataSchemasManager metadataSchemaManager;
	private String collection;

	ZipContentsService() {
	}

	public ZipContentsService(ModelLayerFactory modelLayerFactory, String collection) {
		this.modelLayerFactory = modelLayerFactory;
		this.zipService = modelLayerFactory.getIOServicesFactory().newZipService();
		this.recordServices = modelLayerFactory.newRecordServices();
		contentManager = modelLayerFactory.getContentManager();
		this.collection = collection;
		metadataSchemaManager = modelLayerFactory.getMetadataSchemasManager();
		this.ioServices = modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newIOServices();
		this.searchServices = modelLayerFactory.newSearchServices();

	}

	public void zipContentsOfRecords(List<String> selectedRecordIds, File destinationFile)
			throws IOException {
		File newTempFolder = null;
		try {
			newTempFolder = ioServices.newTemporaryFile(TMP_CONTENTS_FOLDER);
			List<File> filesToZip = getContents(newTempFolder, selectedRecordIds);
			if (filesToZip.size() != 0) {
				zipService.zip(destinationFile, filesToZip);
			} else {
				throw new NoContentToZipRuntimeException();
			}
		} catch (ZipServiceException e) {
			throw new RuntimeException(e);
		} finally {
			ioServices.deleteQuietly(newTempFolder);
		}
	}

	List<File> getContents(File newTempFolder, List<String> recordIds)
			throws IOException {
		List<File> returnFiles = new ArrayList<>();

		recordIds = removeRedundantRecords(recordIds);

		List<RecordToZipNode> recordsToZipTrees = new ArrayList<>();
		for (String recordId : recordIds) {
			Record record = recordServices.getDocumentById(recordId);
			List<RelatedContent> allRecordContents = getRecordContents(record);
			if (!allRecordContents.isEmpty()) {
				RecordToZipNode tree = generateTreeOfContents(record, allRecordContents);
				giveUniqueNamesInHierarchy(tree);
				recordsToZipTrees.add(tree);
			}
		}
		giveUniqueNameToRootNodes(recordsToZipTrees);
		for (RecordToZipNode tree : recordsToZipTrees) {
			saveTree(tree, newTempFolder);
		}

		String[] listOfFilesToZip = newTempFolder.list();
		if (listOfFilesToZip == null || listOfFilesToZip.length == 0) {
			return returnFiles;
		}

		for (String filePath : listOfFilesToZip) {
			returnFiles.add(new File(newTempFolder.getPath() + File.separator + filePath));
		}
		return returnFiles;
	}

	private void saveTree(RecordToZipNode node, File parentFolder)
			throws IOException {
		File nodeFile;
		if (node.isCanHaveChildren()) {
			nodeFile = new File(parentFolder, node.getUniqueNameInHierarchy());
		} else {
			nodeFile = parentFolder;
		}
		for (NodeContent nodeContent : node.getContents()) {
			ContentVersion currentContentVersion = nodeContent.getContentCurrentVersion();
			String contentTitle = nodeContent.getUniqueName();
			if (contentTitle.length() > 120) {
				contentTitle = contentTitle.substring(0, 97) + "..." + contentTitle.substring(contentTitle.length() - 20);
			}
			InputStream contentInputStream = contentManager
					.getContentInputStream(currentContentVersion.getHash(), CONTENTS_FOLDER);
			File currentFile = new File(nodeFile, contentTitle);
			FileUtils.copyInputStreamToFile(contentInputStream,
					currentFile);
			ioServices.closeQuietly(contentInputStream);
		}
		for (RecordToZipNode child : node.getChildren()) {
			saveTree(child, nodeFile);
		}
	}

	private void giveUniqueNameToRootNodes(List<RecordToZipNode> recordsToZipTrees) {
		Set<String> topRedundantNames = getRedundantNamesFromRoots(recordsToZipTrees);
		for (RecordToZipNode node : recordsToZipTrees) {
			if (node.isCanHaveChildren()) {
				if (topRedundantNames.contains(node.getUniqueNameInHierarchy())) {
					node.setUniqueNameInHierarchy(true);
				}
			} else {
				String nodeRecordId = node.getRecordId();
				for (NodeContent nodeContent : node.getContents()) {
					if (topRedundantNames.contains(nodeContent.getContentName())) {
						nodeContent.rename(nodeRecordId);
					}
				}
			}
		}
	}

	private Set<String> getRedundantNamesFromRoots(List<RecordToZipNode> recordsToZipTrees) {
		Set<String> rootNamesWithoutRedundancy = new HashSet<>();
		List<String> allRootNames = new ArrayList<>();
		for (RecordToZipNode node : recordsToZipTrees) {
			if (node.isCanHaveChildren()) {
				rootNamesWithoutRedundancy.add(node.getRecordName());
				allRootNames.add(node.getRecordName());
			} else {
				for (NodeContent nodeContent : node.getContents()) {
					rootNamesWithoutRedundancy.add(nodeContent.getContentName());
					allRootNames.add(nodeContent.getContentName());
				}
			}
		}
		return new HashSet<>(CollectionUtils.subtract(allRootNames, rootNamesWithoutRedundancy));
	}

	private void giveUniqueNamesInHierarchy(RecordToZipNode node) {
		giveUniqueNameToContents(node);
		Set<String> childrenToRename = node.getRedundantChildrenNames();

		for (RecordToZipNode child : node.getChildren()) {
			if (childrenToRename.contains(child.getRecordName())) {
				child.setUniqueNameInHierarchy(true);
			} else {
				child.setUniqueNameInHierarchy(false);
			}
			giveUniqueNamesInHierarchy(child);
		}
	}

	private void giveUniqueNameToContents(RecordToZipNode node) {
		Set<String> contentsToRename = node.getRedundantContentsNames();
		Map<String, Integer> contentsNamesAndTheirCount = new HashMap<>();
		for (NodeContent content : node.getContents()) {
			String currentName = content.getContentName();
			if (contentsToRename.contains(currentName)) {
				Integer currentNameFoundCount = contentsNamesAndTheirCount.get(currentName);
				if (currentNameFoundCount == null) {
					currentNameFoundCount = 0;
					contentsNamesAndTheirCount.put(currentName, 1);
				} else {
					contentsNamesAndTheirCount.put(currentName, currentNameFoundCount + 1);
				}
				content.rename(currentNameFoundCount.toString());
			}
		}
	}

	private RecordToZipNode generateTreeOfContents(Record record, List<RelatedContent> allRecordContents) {
		RecordToZipNode returnNode = newRecordToZipNode(record);
		String recordPrincipalPath = record.get(Schemas.PRINCIPAL_PATH);
		if (recordPrincipalPath == null) {
			putAllContentsInRootNode(returnNode, allRecordContents);
		} else {
			for (RelatedContent relatedContent : allRecordContents) {
				String relativePathInTree = getRelativePathInRecord(relatedContent.getContainerPrincipalPath(),
						recordPrincipalPath,
						record.getId());
				putContentInAdequateNode(returnNode, relatedContent, relativePathInTree);
			}
		}
		return returnNode;
	}

	private void putAllContentsInRootNode(RecordToZipNode rootNode, List<RelatedContent> allRecordContents) {
		for (RelatedContent content : allRecordContents) {
			rootNode.addContent(content);
		}
	}

	//TODO test me
	private void putContentInAdequateNode(RecordToZipNode tree, RelatedContent relatedContent,
										  String relativePathInTree) {
		String[] recordsIds = StringUtils.split(relativePathInTree, "/");
		RecordToZipNode currentNode = tree;
		for (String recordId : recordsIds) {
			if (!currentNode.getRecordId().equals(recordId)) {
				currentNode = getOrCreateChild(currentNode, recordId);
			}
		}
		currentNode.addContent(relatedContent);
	}

	private RecordToZipNode getOrCreateChild(RecordToZipNode node, String childRecordId) {
		for (RecordToZipNode child : node.getChildren()) {
			if (child.getRecordId().equals(childRecordId)) {
				return child;
			}
		}
		Record childRecord = recordServices.getDocumentById(childRecordId);
		RecordToZipNode childNode = newRecordToZipNode(childRecord);
		childNode.setParent(node);
		node.addChild(childNode);
		return childNode;
	}

	private RecordToZipNode newRecordToZipNode(Record record) {
		String recordId = record.getId();
		String recordName = record.get(Schemas.TITLE);
		String recordAbbreviation = null;

		try {
			recordAbbreviation = record.get(Schemas.ABBREVIATION);
		} catch (Throwable t) {
			t.printStackTrace();
		}

		boolean canHaveChildren = canHaveChildren(
				metadataSchemaManager.getSchemaTypes(collection).getSchema(record.getSchemaCode()));
		return new RecordToZipNode(recordId, recordAbbreviation, recordName, canHaveChildren);
	}

	private List<String> removeRedundantRecords(List<String> recordIds) {
		RecordDescriptionList recordDescriptionList = getRecordsDescriptionList(recordIds);
		recordDescriptionList.removeSubRecords();
		return recordDescriptionList.getRecordsIds();
	}

	private RecordDescriptionList getRecordsDescriptionList(List<String> recordIds) {
		RecordDescriptionList returnList = new RecordDescriptionList();
		List<Record> allRecords = recordServices.getRecordsById(collection, recordIds);
		for (Record record : allRecords) {
			String recordPrincipalPath = record.get(Schemas.PRINCIPAL_PATH);
			returnList.add(new RecordDescription(record.getId(), recordPrincipalPath));
		}
		return returnList;
	}

	private List<RelatedContent> getRecordContents(Record record) {
		String recordPrincipalPath = record.get(Schemas.PRINCIPAL_PATH);
		if (StringUtils.isBlank(recordPrincipalPath)) {
			return getRecordDirectContents(record);
		}
		List<RelatedContent> returnList = new ArrayList<>();
		List<Record> recordsWithContent = getRecordsHavingContentFromHierarchy(record);
		for (Record recordInHierarchy : recordsWithContent) {
			returnList.addAll(getRecordDirectContents(recordInHierarchy));
		}
		return returnList;
	}

	private List<Record> getRecordsHavingContentFromHierarchy(Record record) {
		String recordPrincipalPath = record.get(Schemas.PRINCIPAL_PATH);
		List<DataStoreField> contentDataStoreFields = new ArrayList<>();
		for (Metadata metadata : metadataSchemaManager.getSchemaTypes(collection).getAllContentMetadatas()) {
			contentDataStoreFields.add(metadata);
		}
		if (contentDataStoreFields.isEmpty()) {
			return new ArrayList<>();
		}
		LogicalSearchCondition recordContentQuery = fromAllSchemasInExceptEvents(collection).where(Schemas.PRINCIPAL_PATH)
				.isContainingText(recordPrincipalPath).andWhereAny(contentDataStoreFields).isNotNull();
		return searchServices.search(new LogicalSearchQuery(recordContentQuery).filteredByStatus(StatusFilter.ACTIVES));
	}

	String getRelativePathInRecord(String subRecordPrincipalPath, String recordPrincipalPath, String parentId) {
		if (!subRecordPrincipalPath.contains(recordPrincipalPath) || subRecordPrincipalPath.equals(recordPrincipalPath)) {
			return parentId;
		} else {
			return parentId + StringUtils.removeStart(subRecordPrincipalPath, recordPrincipalPath);
		}
	}

	public boolean canHaveChildren(MetadataSchema schema) {
		String schemaType = new SchemaUtils().getSchemaTypeCode(schema.getCode());
		return !metadataSchemaManager.getSchemaTypes(schema.getCollection()).getAllMetadatas()
				.onlyParentReferenceToSchemaType(schemaType).isEmpty();
	}

	List<RelatedContent> getRecordDirectContents(Record record) {
		List<RelatedContent> returnList = new ArrayList<>();
		MetadataSchema schema = metadataSchemaManager.getSchemaTypes(collection)
				.getSchema(record.getSchemaCode());
		String containerRecordId, containerRecordPrincipalPath;
		if (canHaveChildren(schema)) {
			containerRecordId = record.getId();
			containerRecordPrincipalPath = record.get(Schemas.PRINCIPAL_PATH);
		} else {
			containerRecordId = record.getParentId(schema);
			Record parentRecord = recordServices.getDocumentById(containerRecordId);
			containerRecordPrincipalPath = parentRecord.get(Schemas.PRINCIPAL_PATH);
		}

		for (Metadata metadata : new MetadataList(schema.getMetadatas()).onlyWithType(MetadataValueType.CONTENT)) {
			if (metadata.isMultivalue()) {
				List<Content> metadataContents = record.getList(metadata);
				for (Content metadataContent : metadataContents) {
					returnList.add(new RelatedContent(metadataContent, containerRecordPrincipalPath, containerRecordId));
				}
			} else {
				Content metadataContent = record.get(metadata);
				if (metadataContent != null) {
					returnList.add(new RelatedContent(metadataContent, containerRecordPrincipalPath, containerRecordId));
				}
			}
		}
		return returnList;
	}

	public static class NoContentToZipRuntimeException extends RuntimeException {
	}

	private class RecordDescription {
		String id;
		String principalPath;

		public RecordDescription(String id, String recordPrincipalPath) {
			this.id = id;
			this.principalPath = recordPrincipalPath;
		}

		public String getPrincipalPath() {
			return principalPath;
		}

		public String getId() {
			return id;
		}
	}

	private class RecordDescriptionList {
		List<RecordDescription> recordDescriptions = new ArrayList<>();

		public void add(RecordDescription recordDescription) {
			this.recordDescriptions.add(recordDescription);
		}

		public void removeSubRecords() {
			CollectionUtils.filter(this.recordDescriptions, new Predicate() {
				@Override
				public boolean evaluate(Object object) {
					RecordDescription elementToEvaluate = (RecordDescription) object;
					if (elementToEvaluate.getPrincipalPath() == null) {
						return true;
					}
					for (RecordDescription recordDescription : recordDescriptions) {
						if (!elementToEvaluate.getId().equals(recordDescription.getId()) &&
							elementToEvaluate.getPrincipalPath().contains(recordDescription.getPrincipalPath())) {
							return false;
						}
					}
					return true;
				}
			});
		}

		public List<RecordDescription> getRecordDescriptionList() {
			return new ArrayList<>(recordDescriptions);
		}

		public List<String> getRecordsIds() {
			List<String> recordIds = new ArrayList<>();
			for (RecordDescription recordDescription : this.recordDescriptions) {
				recordIds.add(recordDescription.getId());
			}
			return recordIds;
		}
	}
}

