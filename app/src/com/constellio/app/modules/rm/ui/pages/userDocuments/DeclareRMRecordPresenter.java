package com.constellio.app.modules.rm.ui.pages.userDocuments;

import static com.constellio.model.entities.schemas.Schemas.LOGICALLY_DELETED_STATUS;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.builders.FolderToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.entities.FolderVO;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserDocumentVO;
import com.constellio.app.ui.framework.builders.ContentVersionToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentFactory;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.moreLikeThis.MoreLikeThisClustering;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class DeclareRMRecordPresenter extends BasePresenter<DeclareRMRecordView> {
	private static final long serialVersionUID = 1L;
	private static final double MIN_SIMILARITY = 0.03;
	private ContentVersionToVOBuilder contentVersionToVOBuilder;
	private DocumentToVOBuilder documentToVOBuilder;
	private FolderToVOBuilder folderToVOBuilder;
	private RecordVO recordVO;
	private ContentVersionVO contentVersionVO;
	private MetadataSchemaType schemaType;
	private SchemaPresenterUtils userDocumentPresenterUtils;
	private SchemaPresenterUtils documentPresenterUtils;

	private transient RMSchemasRecordsServices rmSchemasRecordsServices;

	public DeclareRMRecordPresenter(DeclareRMRecordView view) {
		super(view);
		initTransientObjects();

		UserDocumentVO userDocumentVO = view.getUserDocumentVO();
		recordVO = userDocumentVO;
		contentVersionVO = userDocumentVO.getContent();
		schemaType = schemaType(UserDocument.SCHEMA_TYPE);
		view.setFolderId(userDocumentVO.getFolder());

		Map<DocumentVO, ContentVersionVO> duplicateVOs = new LinkedHashMap<>();
		List<Document> duplicates = findDocumentsByHash();
		Set<String> duplicateIds = new HashSet<>();
		for (Document duplicate : duplicates) {
			DocumentVO duplicateVO = documentToVOBuilder
					.build(duplicate.getWrappedRecord(), VIEW_MODE.TABLE, view.getSessionContext());
			duplicateIds.add(duplicateVO.getId());

			String matchingVersionNumber = getMatchingVersionNumber(duplicate);
			Content existingDocumentContent = duplicate.getContent();
			ContentVersion matchingVersion = existingDocumentContent.getVersion(matchingVersionNumber);
			ContentVersionVO matchingVersionVO = contentVersionToVOBuilder.build(existingDocumentContent, matchingVersion);
			duplicateVOs.put(duplicateVO, matchingVersionVO);
		}
		view.setDuplicates(duplicateVOs);

		Map<DocumentVO, Double> similarDocumentsVOs = new LinkedHashMap<>();
		Map<Record, Double> similarDocuments = findSimilarDocuments(duplicateIds);
		for (Entry<Record, Double> documentAndSimilarity : similarDocuments.entrySet()) {
			Record similarDocument = documentAndSimilarity.getKey();
			Double similarity = documentAndSimilarity.getValue();
			DocumentVO similarDocumentVO = documentToVOBuilder.build(similarDocument, VIEW_MODE.TABLE, view.getSessionContext());
			similarDocumentsVOs.put(similarDocumentVO, similarity);
		}
		view.setSimilarDocuments(similarDocumentsVOs);

		Map<FolderVO, Double> suggestedFolderVOs = new LinkedHashMap<>();
		Map<String, Double> suggestedFolderIds = suggestFolderIds(similarDocuments);
		for (String suggestedFolderId : suggestedFolderIds.keySet()) {
			Record suggestedFolder = documentPresenterUtils.getRecord(suggestedFolderId);
			FolderVO suggestedFolderVO = folderToVOBuilder.build(suggestedFolder, VIEW_MODE.TABLE, view.getSessionContext());
			Double similarity = suggestedFolderIds.get(suggestedFolderId);
			suggestedFolderVOs.put(suggestedFolderVO, similarity);
		}
		view.setSuggestedFolders(suggestedFolderVOs);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, modelLayerFactory);
		contentVersionToVOBuilder = new ContentVersionToVOBuilder(modelLayerFactory);
		documentToVOBuilder = new DocumentToVOBuilder(modelLayerFactory);
		folderToVOBuilder = new FolderToVOBuilder();

		ConstellioFactories constellioFactories = view.getConstellioFactories();
		SessionContext sessionContext = view.getSessionContext();
		userDocumentPresenterUtils = new SchemaPresenterUtils(UserDocument.DEFAULT_SCHEMA, constellioFactories, sessionContext);
		documentPresenterUtils = new SchemaPresenterUtils(Document.DEFAULT_SCHEMA, constellioFactories, sessionContext);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	private List<Document> findDocumentsByHash() {
		List<Document> duplicates = new ArrayList<>();
		String recordId = recordVO.getId();
		String userDocumentHash = contentVersionVO.getHash();
		MetadataSchemaType documentSchemaType = rmSchemasRecordsServices.documentSchemaType();
		Metadata contentMetadata = types().getSchema(Document.DEFAULT_SCHEMA).getMetadata(Document.CONTENT);
		LogicalSearchQuery query = new LogicalSearchQuery(from(documentSchemaType)
				.where(contentMetadata).is(ContentFactory.isHash(userDocumentHash))
				.andWhere(LOGICALLY_DELETED_STATUS).isFalseOrNull());
		MetadataSchemaTypes types = types();

		SearchServices searchServices = modelLayerFactory.newSearchServices();
		List<Record> matches = searchServices.search(query);
		for (Record match : matches) {
			if (!match.getId().equals(recordId)) {
				duplicates.add(new Document(match, types));
			}
		}
		return duplicates;
	}

	private Map<Record, Double> findSimilarDocuments(Set<String> toFilterRecordIds) {
		String recordId = recordVO.getId();

		LogicalSearchQuery query = new LogicalSearchQuery(
				fromAllSchemasIn(collection).where(Schemas.COLLECTION).isEqualTo(collection));

		LogicalSearchCondition condition = from(schemaType)
				.where(Schemas.IDENTIFIER).isEqualTo(recordId)
				.andWhere(Schemas.COLLECTION).isEqualTo(collection);
		query.setQueryCondition(condition);
		query.setMoreLikeThis(true);

		SearchServices searchServices = modelLayerFactory.newSearchServices();
		Map<Record, Double> similarRecords = searchServices.searchWithMoreLikeThis(query).entrySet().iterator().next().getValue();
		for (Iterator<Entry<Record, Double>> it = similarRecords.entrySet().iterator(); it.hasNext(); ) {
			Entry<Record, Double> entry = it.next();
			Record similarRecord = entry.getKey();
			Double similarity = entry.getValue();
			boolean logicallyDeleted = Boolean.TRUE.equals(similarRecord.get(LOGICALLY_DELETED_STATUS));
			if (logicallyDeleted || similarity < MIN_SIMILARITY || !isDocument(similarRecord) || similarRecord.getId()
					.equals(recordId)
					|| !similarRecord.getCollection().equals(collection) || toFilterRecordIds.contains(similarRecord.getId())) {
				it.remove();
			}
		}
		return similarRecords;
	}

	private boolean isDocument(Record record) {
		String schemaCode = record.getSchemaCode();
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);
		return Document.SCHEMA_TYPE.equals(schemaTypeCode);
	}

	private Map<String, Double> suggestFolderIds(Map<Record, Double> similarRecords) {
		MoreLikeThisClustering moreLikeThisClustering = new MoreLikeThisClustering(similarRecords,
				new MoreLikeThisClustering.StringConverter<Record>() {
					@Override
					public String converToString(Record record) {
						String folder;
						if (isDocument(record)) {
							Document document = new Document(record, types());
							folder = document.getFolder();
						} else {
							folder = null;
						}
						return folder;
					}
				});

		Map<String, Double> clusterScores = moreLikeThisClustering.getClusterScore();
		for (Iterator<Entry<String, Double>> iterClusterScore = clusterScores.entrySet().iterator(); iterClusterScore
				.hasNext(); ) {
			Double score = iterClusterScore.next().getValue();
			if (score < MIN_SIMILARITY) {
				iterClusterScore.remove();
			}
		}
		return clusterScores;
	}

	private String getMatchingVersionNumber(Document matchingDocument) {
		String matchingVersionNumber;
		String contentVersionHash;
		contentVersionHash = contentVersionVO.getHash();
		Content matchingDocumentContent = matchingDocument.getContent();
		ContentVersion currentVersion = matchingDocumentContent.getCurrentVersion();
		if (contentVersionHash.equals(currentVersion.getHash())) {
			matchingVersionNumber = currentVersion.getVersion();
		} else {
			matchingVersionNumber = null;
			for (ContentVersion contentVersion : matchingDocumentContent.getHistoryVersions()) {
				if (contentVersionHash.equals(contentVersion.getHash())) {
					matchingVersionNumber = contentVersion.getVersion();
					break;
				}
			}
		}
		return matchingVersionNumber;
	}

	void deleteUserDocumentButtonClicked() {
		if (recordVO instanceof UserDocumentVO) {
			Record userDocumentRecord = userDocumentPresenterUtils.getRecord(recordVO.getId());
			userDocumentPresenterUtils.delete(userDocumentRecord, null);
			view.closeWindow();
		}
	}

	void declareButtonClicked() {
		view.closeWindow();
		String newVersionDocumentId = view.getNewVersionDocumentId();
		String folderId = view.getFolderId();
		UserDocumentVO userDocumentVO = (UserDocumentVO) recordVO;
		if (newVersionDocumentId != null) {
			view.navigateTo().editDocument(newVersionDocumentId, userDocumentVO.getId());
		} else {
			view.navigateTo().declareUserDocument(userDocumentVO.getId(), folderId);
		}
	}

	public void cancelButtonClicked() {
		view.closeWindow();
	}

	FolderVO getFolderVO(String id) {
		Record record = documentPresenterUtils.getRecord(id);
		return folderToVOBuilder.build(record, VIEW_MODE.TABLE, view.getSessionContext());
	}

}
