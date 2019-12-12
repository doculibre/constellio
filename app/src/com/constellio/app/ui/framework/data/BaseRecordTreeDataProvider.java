package com.constellio.app.ui.framework.data;

import com.constellio.app.ui.framework.data.trees.RecordTreeNodesDataProvider;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.taxonomies.FastContinueInfos;
import com.constellio.model.services.taxonomies.LinkableTaxonomySearchResponse;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import com.vaadin.server.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseRecordTreeDataProvider extends AbstractDataProvider implements LazyTreeDataProvider<String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(BaseRecordTreeDataProvider.class);

	private int estimatedRootNodesCount = -1;
	private Map<String, String> parentCache = new HashMap<>();
	private Map<String, RecordDataTreeNode> nodesCache = new HashMap<>();
	RecordTreeNodesDataProvider nodesDataProvider;
	private Map<String, FastContinueInfos> fastContinueInfosMap = new HashMap<>();

	public BaseRecordTreeDataProvider(RecordTreeNodesDataProvider recordTreeNodesDataProvider) {
		this.nodesDataProvider = recordTreeNodesDataProvider;
	}

	@Override
	public final String getTaxonomyCode() {
		return nodesDataProvider.getTaxonomyCode();
	}

	public final RecordDataTreeNode getNode(String id) {
		return nodesCache.get(id);
	}

	@Override
	public final String getCaption(String id) {
		RecordDataTreeNode treeNode = getNode(id);
		return treeNode == null ? "" : treeNode.getCaption();
	}

	@Override
	public final String getDescription(String id) {
		RecordDataTreeNode treeNode = getNode(id);
		return treeNode == null ? null : treeNode.getDescription();
	}

	@Override
	public final Resource getIcon(String id, boolean expanded) {
		RecordDataTreeNode treeNode = getNode(id);
		return treeNode == null ? null : treeNode.getIcon(expanded);
	}

	@Override
	public final ObjectsResponse<String> getRootObjects(int start, int maxSize) {
		FastContinueInfos infos = fastContinueInfosMap.get("root-" + start);
		LinkableTaxonomySearchResponse response = nodesDataProvider.getRootNodes(start, maxSize, infos);

		List<String> recordIds = new ArrayList<>();
		for (TaxonomySearchRecord searchRecord : response.getRecords()) {
			saveResultInCache(searchRecord);
			recordIds.add(searchRecord.getId());
		}
		//LOGGER.info("getRoot(" + start + ", " + maxSize + ") => " + recordIds);

		estimatedRootNodesCount = Math.max(estimatedRootNodesCount, (int) response.getNumFound());

		int end = start + maxSize;
		FastContinueInfos responseFastContinueInfos = response.getFastContinueInfos();
		if (responseFastContinueInfos != null) {
			fastContinueInfosMap.put("root-" + end, responseFastContinueInfos);
		}

		return new ObjectsResponse<>(recordIds, response.getNumFound());
	}

	@Override
	public final String getParent(String child) {
		return parentCache.get(child);
	}

	@Override
	public ObjectsResponse<String> getChildren(String parent, int start, int maxSize) {

		FastContinueInfos infos = fastContinueInfosMap.get(parent + "-" + start);
		LinkableTaxonomySearchResponse response = nodesDataProvider.getChildrenNodes(parent, start, maxSize, infos);

		List<String> recordIds = new ArrayList<>();
		for (TaxonomySearchRecord searchRecord : response.getRecords()) {
			saveResultInCache(searchRecord);
			recordIds.add(searchRecord.getId());
			parentCache.put(searchRecord.getId(), parent);
		}
		//LOGGER.info("getChildren(" + parent + ", " + start + ", " + maxSize + ") => " + recordIds);

		RecordDataTreeNode parentTreeNode = nodesCache.get(parent);
		// FIXME Francis
		if (parentTreeNode != null) {
			parentTreeNode.estimatedChildrenCount = Math.max(parentTreeNode.estimatedChildrenCount, (int) response.getNumFound());
		}

		int end = start + maxSize;
		FastContinueInfos responseFastContinueInfos = response.getFastContinueInfos();
		if (responseFastContinueInfos != null) {
			fastContinueInfosMap.put(parent + "-" + end, responseFastContinueInfos);
		}

		return new ObjectsResponse<>(recordIds, response.getNumFound());
	}

	@Override
	public boolean hasChildren(String parent) {
		return getNode(parent).hasChildren();
	}

	@Override
	public boolean isLeaf(String parent) {
		return !getNode(parent).hasChildren();
	}

	@Override
	public final int getEstimatedRootNodesCount() {
		return estimatedRootNodesCount;
	}

	@Override
	public final int getEstimatedChildrenNodesCount(String parent) {
		RecordDataTreeNode treeNode = getNode(parent);
		return treeNode == null ? -1 : treeNode.estimatedChildrenCount;
	}

	protected RecordDataTreeNode toTreeNode(TaxonomySearchRecord searchRecord) {
		Record record = searchRecord.getRecord();
		String schemaType = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		String caption = getCaptionOf(record);
		String description = (String) record.getRecordDTO().getFields().get(Schemas.DESCRIPTION_STRING.getDataStoreCode());
		if (description == null) {
			description = (String) record.getRecordDTO().getFields().get(Schemas.DESCRIPTION_TEXT.getDataStoreCode());
		}

		Resource collapsedIcon = getCollapsedIconOf(record);
		Resource expandedIcon = getExpandedIconOf(record);

		return new RecordDataTreeNode(searchRecord.getId(), caption, description, schemaType,
				collapsedIcon, expandedIcon, searchRecord.hasChildren());
	}

	protected String getCaptionOf(Record record) {
		return SchemaCaptionUtils.getCaptionForRecord(record, i18n.getLocale());
	}

	protected Resource getExpandedIconOf(Record record) {
		return FileIconUtils.getIconForRecordId(record, true);
	}

	protected Resource getCollapsedIconOf(Record record) {
		return FileIconUtils.getIconForRecordId(record, false);
	}

	protected void saveResultInCache(TaxonomySearchRecord searchRecord) {
		RecordDataTreeNode treeNode = toTreeNode(searchRecord);
		nodesCache.put(searchRecord.getId(), treeNode);
	}

}
