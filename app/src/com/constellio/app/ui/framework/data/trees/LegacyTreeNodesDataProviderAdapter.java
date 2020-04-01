package com.constellio.app.ui.framework.data.trees;

import com.constellio.app.ui.framework.data.TreeNode;
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

import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.model.entities.records.Record.GetMetadataOption.NO_SUMMARY_METADATA_VALIDATION;

/**
 * This class is temporary, replace with proper implementations
 */
public class LegacyTreeNodesDataProviderAdapter implements TreeNodesProvider<FastContinueInfos> {

	public static final String PROVIDER_ID = "records-legacyAdapter";

	private RecordTreeNodesDataProvider legacyDataProvider;

	public LegacyTreeNodesDataProviderAdapter(
			RecordTreeNodesDataProvider legacyDataProvider) {
		this.legacyDataProvider = legacyDataProvider;
	}

	@Override
	public boolean areNodesPossibleIn(TreeNode optionalParentTreeNode) {
		return optionalParentTreeNode == null || optionalParentTreeNode.isProviderType(PROVIDER_ID);
	}

	@Override
	public TreeNodesProviderResponse<FastContinueInfos> getNodes(TreeNode optionalParent, int start, int maxSize,
																 FastContinueInfos fastContinuationInfos) {

		if (optionalParent == null) {
			return adapt(start, legacyDataProvider.getRootNodes(start, maxSize, fastContinuationInfos));

		} else if (PROVIDER_ID.equals(optionalParent.getProviderType())) {
			return adapt(start, legacyDataProvider.getChildrenNodes(optionalParent.getId(), start, maxSize, fastContinuationInfos));

		} else {
			return TreeNodesProviderResponse.EMPTY();
		}

	}

	private TreeNodesProviderResponse<FastContinueInfos> adapt(int start,
															   LinkableTaxonomySearchResponse legacyResponse) {

		List<TreeNode> nodes = legacyResponse.getRecords().stream().map(this::toTreeNode).collect(Collectors.toList());
		boolean hasMoreNodes = start + nodes.size() < legacyResponse.getNumFound();
		return new TreeNodesProviderResponse<>(hasMoreNodes, nodes, legacyResponse.getFastContinueInfos());
	}


	protected TreeNode toTreeNode(TaxonomySearchRecord searchRecord) {
		Record record = searchRecord.getRecord();
		String type = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		String caption = SchemaCaptionUtils.getCaptionForRecord(record, i18n.getLocale());
		String description = record.get(Schemas.DESCRIPTION_STRING, NO_SUMMARY_METADATA_VALIDATION);
		if (description == null) {
			description = record.get(Schemas.DESCRIPTION_TEXT, NO_SUMMARY_METADATA_VALIDATION);
		}

		Resource collapsedIcon = getCollapsedIconOf(record);
		Resource expandedIcon = getExpandedIconOf(record);

		return new TreeNode(searchRecord.getId(), PROVIDER_ID, type, caption, description,
				collapsedIcon, expandedIcon, searchRecord.hasChildren());
	}

	protected Resource getExpandedIconOf(Record record) {
		return FileIconUtils.getIconForRecordId(record, true);
	}

	protected Resource getCollapsedIconOf(Record record) {
		return FileIconUtils.getIconForRecordId(record, false);
	}

}
