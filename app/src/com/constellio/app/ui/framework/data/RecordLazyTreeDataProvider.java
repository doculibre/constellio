package com.constellio.app.ui.framework.data;

import static com.constellio.app.services.factories.ConstellioFactories.getInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.taxonomies.LinkableTaxonomySearchResponse;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import com.constellio.model.services.users.UserServices;
import com.vaadin.server.Resource;

public class RecordLazyTreeDataProvider implements LazyTreeDataProvider<String> {
	private int estimatedRootNodesCount = -1;
	private String taxonomyCode;
	private Map<String, String> parentCache = new HashMap<>();
	private Map<String, RecordDataTreeNode> nodesCache = new HashMap<>();

	public RecordLazyTreeDataProvider(String taxonomyCode) {
		super();
		this.taxonomyCode = taxonomyCode;
	}

	public final String getTaxonomyCode() {
		return taxonomyCode;
	}

	@Override
	public String getCaption(String id) {
		return getNode(id).getCaption();
	}

	@Override
	public String getDescription(String id) {
		return getNode(id).getDescription();
	}

	@Override
	public Resource getIcon(String id, boolean expanded) {
		return getNode(id).getIcon(expanded);
	}

	@Override
	public int getEstimatedRootNodesCount() {
		return estimatedRootNodesCount;
	}

	@Override
	public int getEstimatedChildrenNodesCount(String parent) {
		return getNode(parent).estimatedChildrenCount;
	}

	@Override
	public ObjectsResponse<String> getRootObjects(int start, int maxSize) {
		ModelLayerFactory modelLayerFactory = getInstance().getModelLayerFactory();
		User currentUser = getCurrentUser(modelLayerFactory);

		System.out.println("getRootObjects(" + taxonomyCode + "," + start + "," + maxSize + ")");
		TaxonomiesSearchOptions taxonomiesSearchOptions = newTaxonomiesSearchOptions(start, maxSize);
		LinkableTaxonomySearchResponse response = modelLayerFactory.newTaxonomiesSearchService().getVisibleRootConceptResponse(
				currentUser, currentUser.getCollection(), taxonomyCode, taxonomiesSearchOptions);

		List<String> recordIds = new ArrayList<>();
		for (TaxonomySearchRecord searchRecord : response.getRecords()) {
			RecordDataTreeNode node = toTreeNode(searchRecord);
			nodesCache.put(searchRecord.getId(), node);
			recordIds.add(searchRecord.getId());
		}
		estimatedRootNodesCount = Math.max(estimatedRootNodesCount, (int) response.getNumFound());
		return new ObjectsResponse<>(recordIds, response.getNumFound());
	}

	@Override
	public String getParent(String child) {
		return parentCache.get(child);
	}

	@Override
	public ObjectsResponse<String> getChildren(String parent, int start, int maxSize) {
		ModelLayerFactory modelLayerFactory = getInstance().getModelLayerFactory();
		User currentUser = getCurrentUser(modelLayerFactory);
		Record record = getRecord(modelLayerFactory, parent);

		TaxonomiesSearchOptions taxonomiesSearchOptions = newTaxonomiesSearchOptions(start, maxSize);
		LinkableTaxonomySearchResponse response = modelLayerFactory.newTaxonomiesSearchService()
				.getVisibleChildConceptResponse(currentUser, taxonomyCode, record, taxonomiesSearchOptions);

		System.out.println("getRootObjects(" + taxonomyCode + "," + record.getId() + "," + start + "," + maxSize + ")");
		List<String> recordIds = new ArrayList<>();
		for (TaxonomySearchRecord searchRecord : response.getRecords()) {
			RecordDataTreeNode node = toTreeNode(searchRecord);
			nodesCache.put(searchRecord.getId(), node);
			recordIds.add(searchRecord.getId());
			parentCache.put(searchRecord.getId(), parent);
		}
		RecordDataTreeNode parentTreeNode = nodesCache.get(parent);
		parentTreeNode.estimatedChildrenCount = Math.max(parentTreeNode.estimatedChildrenCount, (int) response.getNumFound());
		return new ObjectsResponse<>(recordIds, response.getNumFound());
	}

	@Override
	public boolean hasChildren(String parentId) {
		RecordDataTreeNode parent = nodesCache.get(parentId);
		return parent.hasChildren();
	}

	@Override
	public boolean isLeaf(String parentId) {
		RecordDataTreeNode parent = nodesCache.get(parentId);
		return !parent.hasChildren();
	}

	public RecordDataTreeNode getNode(String id) {
		return nodesCache.get(id);
	}

	private User getCurrentUser(ModelLayerFactory modelLayerFactory) {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		String currentCollection = sessionContext.getCurrentCollection();
		UserVO currentUserVO = sessionContext.getCurrentUser();
		UserServices userServices = modelLayerFactory.newUserServices();

		return userServices.getUserInCollection(currentUserVO.getUsername(), currentCollection);
	}

	private Record getRecord(ModelLayerFactory modelLayerFactory, String id) {
		RecordServices recordServices = modelLayerFactory.newRecordServices();

		return recordServices.getDocumentById(id);
	}

	private RecordDataTreeNode toTreeNode(TaxonomySearchRecord searchRecord) {
		Record record = searchRecord.getRecord();
		String schemaType = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		String caption = SchemaCaptionUtils.getCaptionForRecord(record);
		String description = record.get(Schemas.DESCRIPTION_STRING);

		if (description == null) {
			description = record.get(Schemas.DESCRIPTION_TEXT);
		}

		Resource collapsedIcon = FileIconUtils.getIconForRecordId(record, false);
		Resource expandedIcon = FileIconUtils.getIconForRecordId(record, true);

		return new RecordDataTreeNode(searchRecord.getId(), caption, description, schemaType,
				collapsedIcon, expandedIcon, searchRecord.hasChildren());
	}

	private TaxonomiesSearchOptions newTaxonomiesSearchOptions(int start, int maxSize) {
		return new TaxonomiesSearchOptions(maxSize, start, StatusFilter.ACTIVES).setReturnedMetadatasFilter(
				ReturnedMetadatasFilter.idVersionSchemaTitlePath().withIncludedMetadata(Schemas.CODE)
						.withIncludedMetadata(Schemas.DESCRIPTION_TEXT).withIncludedMetadata(Schemas.DESCRIPTION_STRING));
	}
}
