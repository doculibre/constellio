package com.constellio.app.modules.tasks.data.trees;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.data.trees.RecordTreeNodesDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.FastContinueInfos;
import com.constellio.model.services.taxonomies.LinkableTaxonomySearchResponse;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import com.constellio.model.services.users.UserServices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class TaskFoldersTreeNodesDataProvider implements RecordTreeNodesDataProvider {
	
	private String collection;
	
	private AppLayerFactory appLayerFactory;
	
	private SessionContext sessionContext;
	
	private SearchServices searchServices;
	
	private RMSchemasRecordsServices rm;
	
	private RMTask task;

	public TaskFoldersTreeNodesDataProvider(Record taskRecord, AppLayerFactory appLayerFactory, SessionContext sessionContext) {
		this.appLayerFactory = appLayerFactory;
		this.sessionContext = sessionContext;
		
		this.collection = sessionContext.getCurrentCollection();
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.task = rm.wrapRMTask(taskRecord);
	}
	
	@Override
	public LinkableTaxonomySearchResponse getRootNodes(int start, int maxSize, FastContinueInfos infos) {
		LogicalSearchCondition condition = from(Arrays.asList(rm.folderSchemaType()))
				.where(Schemas.IDENTIFIER).isIn(task.getLinkedFolders());

		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		query.filteredByStatus(StatusFilter.ACTIVES);
		query.setStartRow(start);
		query.setNumberOfRows(maxSize);
		query.setReturnedMetadatas(returnedMetadatasForRecordsIn(collection,
				appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection)));
		query.sortAsc(TITLE);
		query.filteredWithUser(getCurrentUser(appLayerFactory.getModelLayerFactory()));

		SPEQueryResponse queryResponse = searchServices.query(query);
		List<TaxonomySearchRecord> taxonomySearchRecords = new ArrayList<>();

		for (Record record : queryResponse.getRecords()) {
			boolean hasChildren = record.getSchemaCode().startsWith(Folder.SCHEMA_TYPE);
			taxonomySearchRecords.add(new TaxonomySearchRecord(record, true, hasChildren));
		}

		return new LinkableTaxonomySearchResponse(queryResponse.getNumFound(), infos, taxonomySearchRecords);
	}

	@Override
	public LinkableTaxonomySearchResponse getChildrenNodes(String recordId, int start, int maxSize,
			FastContinueInfos infos) {
		LogicalSearchCondition condition = from(Arrays.asList(rm.folderSchemaType(), rm.documentSchemaType()))
				.where(rm.defaultFolderSchema().get(Folder.PARENT_FOLDER)).isEqualTo(recordId)
				.orWhere(rm.defaultDocumentSchema().get(Document.FOLDER)).isEqualTo(recordId)
				.orWhere(rm.defaultDocumentSchema().get(Document.LINKED_TO)).isContaining(Arrays.asList(recordId));

		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(condition);

		logicalSearchQuery.filteredWithUser(getCurrentUser(appLayerFactory.getModelLayerFactory()));
		logicalSearchQuery.setStartRow(start);
		logicalSearchQuery.setNumberOfRows(maxSize);
		logicalSearchQuery.sortDesc(Schemas.SCHEMA).sortAsc(TITLE);

		SPEQueryResponse queryResponse = searchServices.query(logicalSearchQuery);
		List<TaxonomySearchRecord> taxonomySearchRecords = new ArrayList<>();

		for (Record record : queryResponse.getRecords()) {
			boolean hasChildren = record.getSchemaCode().startsWith(Folder.SCHEMA_TYPE);
			taxonomySearchRecords.add(new TaxonomySearchRecord(record, true, hasChildren));
		}

		return new LinkableTaxonomySearchResponse(queryResponse.getNumFound(), infos, taxonomySearchRecords);
	}

	public ReturnedMetadatasFilter returnedMetadatasForRecordsIn(String collection, MetadataSchemaTypes types) {

		Set<String> metadatas = new HashSet<>();
		metadatas.add(Schemas.CODE.getDataStoreCode());
		metadatas.add(Schemas.TITLE.getDataStoreCode());
		metadatas.add(Schemas.LINKABLE.getDataStoreCode());
		metadatas.add(Schemas.VISIBLE_IN_TREES.getDataStoreCode());
		metadatas.add(Schemas.TOKENS.getDataStoreCode());
		metadatas.add(Schemas.ATTACHED_ANCESTORS.getDataStoreCode());

		return ReturnedMetadatasFilter.onlyFields(metadatas);
	}

	@Override
	public String getTaxonomyCode() {
		return null;
	}

	public User getCurrentUser(ModelLayerFactory modelLayerFactory) {
		UserVO userVO = sessionContext.getCurrentUser();
		UserServices userServices = modelLayerFactory.newUserServices();
		return userServices.getUserInCollection(userVO.getUsername(), collection);
	}

}
