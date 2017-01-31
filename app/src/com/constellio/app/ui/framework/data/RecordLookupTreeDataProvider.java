package com.constellio.app.ui.framework.data;

import static com.constellio.app.services.factories.ConstellioFactories.getInstance;
import static com.constellio.app.ui.application.ConstellioUI.getCurrentSessionContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.ui.framework.components.fields.lookup.LookupField.LookupTreeDataProvider;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField.TextInputDataProvider;
import com.constellio.app.ui.framework.data.trees.LinkableRecordTreeNodesDataProvider;
import com.constellio.app.ui.framework.data.trees.RecordTreeNodesDataProvider;
import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.taxonomies.LinkableTaxonomySearchResponse;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import com.vaadin.server.Resource;

public class RecordLookupTreeDataProvider implements LookupTreeDataProvider<String> {
	private int estimatedRootNodesCount = -1;
	private String schemaTypeCode;
	private Map<String, String> parentCache = new HashMap<>();
	private Map<String, Boolean> selectableCache = new HashMap<>();
	private boolean ignoreLinkability;
	private boolean writeAccess;
	private Map<String, RecordDataTreeNode> nodesCache = new HashMap<>();
	private RecordTreeNodesDataProvider nodesDataProvider;

	public RecordLookupTreeDataProvider(String schemaTypeCode, String taxonomyCode, boolean writeAccess) {
		this.writeAccess = writeAccess;
		this.schemaTypeCode = schemaTypeCode;
		this.nodesDataProvider = new LinkableRecordTreeNodesDataProvider(taxonomyCode, schemaTypeCode, writeAccess);
		ignoreLinkability = false;
	}

	public RecordLookupTreeDataProvider(String schemaTypeCode, boolean writeAccess,
			RecordTreeNodesDataProvider recordTreeNodesDataProvider) {
		this.writeAccess = writeAccess;
		this.schemaTypeCode = schemaTypeCode;
		this.nodesDataProvider = recordTreeNodesDataProvider;
		ignoreLinkability = false;
	}

	@Override
	public final String getTaxonomyCode() {
		return nodesDataProvider.getTaxonomyCode();
	}

	public RecordDataTreeNode getNode(String id) {
		return nodesCache.get(id);
	}

	@Override
	public String getCaption(String id) {
		RecordDataTreeNode treeNode = getNode(id);
		return treeNode == null ? "" : treeNode.getCaption();
	}

	@Override
	public String getDescription(String id) {
		RecordDataTreeNode treeNode = getNode(id);
		return treeNode == null ? null : treeNode.getDescription();
	}

	@Override
	public Resource getIcon(String id, boolean expanded) {
		RecordDataTreeNode treeNode = getNode(id);
		return treeNode == null ? null : treeNode.getIcon(expanded);
	}

	@Override
	public ObjectsResponse<String> getRootObjects(int start, int maxSize) {
		LinkableTaxonomySearchResponse response = nodesDataProvider.getRootNodes(start, maxSize);

		List<String> recordIds = new ArrayList<>();
		for (TaxonomySearchRecord searchRecord : response.getRecords()) {
			recordIds.add(saveResultInCacheAndReturnId(searchRecord));
		}
		estimatedRootNodesCount = Math.max(estimatedRootNodesCount, (int) response.getNumFound());
		return new ObjectsResponse<>(recordIds, response.getNumFound());
	}

	private String saveResultInCacheAndReturnId(TaxonomySearchRecord searchRecord) {
		RecordDataTreeNode treeNode = toTreeNode(searchRecord);
		boolean selectable = ignoreLinkability || searchRecord.isLinkable();

		nodesCache.put(searchRecord.getId(), treeNode);
		selectableCache.put(searchRecord.getId(), selectable);
		return searchRecord.getId();
	}

	private RecordDataTreeNode toTreeNode(TaxonomySearchRecord searchRecord) {
		Record record = searchRecord.getRecord();
		String schemaType = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		String caption = getCaptionOf(record);
		String description = record.get(Schemas.DESCRIPTION_STRING);
		if (description == null) {
			description = record.get(Schemas.DESCRIPTION_TEXT);
		}

		Resource collapsedIcon = getCollapsedIconOf(record);
		Resource expandedIcon = getExpandedIconOf(record);

		return new RecordDataTreeNode(searchRecord.getId(), caption, description, schemaType,
				collapsedIcon, expandedIcon, searchRecord.hasChildren());
	}

	@Override
	public String getParent(String child) {
		return parentCache.get(child);
	}

	@Override
	public ObjectsResponse<String> getChildren(String parent, int start, int maxSize) {
		LinkableTaxonomySearchResponse response = nodesDataProvider.getChildrenNodes(parent, start, maxSize);

		List<String> recordIds = new ArrayList<>();
		for (TaxonomySearchRecord searchRecord : response.getRecords()) {
			recordIds.add(saveResultInCacheAndReturnId(searchRecord));
			parentCache.put(searchRecord.getId(), parent);
		}
		RecordDataTreeNode parentTreeNode = nodesCache.get(parent);
		parentTreeNode.estimatedChildrenCount = Math.max(parentTreeNode.estimatedChildrenCount, (int) response.getNumFound());
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
	public boolean isSelectable(String selection) {
		return selectableCache.get(selection);
	}

	@Override
	public TextInputDataProvider<String> search() {
		return new RecordTextInputDataProvider(getInstance(), getCurrentSessionContext(), schemaTypeCode, writeAccess);
	}

	public void setIgnoreLinkability(boolean ignoreLinkability) {
		this.ignoreLinkability = ignoreLinkability;
	}

	@Override
	public int getEstimatedRootNodesCount() {
		return estimatedRootNodesCount;
	}

	@Override
	public int getEstimatedChildrenNodesCount(String parent) {
		RecordDataTreeNode treeNode = getNode(parent);
		return treeNode == null ? -1 : treeNode.estimatedChildrenCount;
	}

	public String getCaptionOf(Record record) {
		return SchemaCaptionUtils.getCaptionForRecord(record);
	}

	public Resource getExpandedIconOf(Record record) {
		return FileIconUtils.getIconForRecordId(record, true);
	}

	public Resource getCollapsedIconOf(Record record) {
		return FileIconUtils.getIconForRecordId(record, false);
	}
}
