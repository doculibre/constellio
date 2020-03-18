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

public class LegacyTreeNodesDataProviderAdapter implements TreeNodesProvider<FastContinueInfos> {

	RecordTreeNodesDataProvider legacyDataProvider;

	public LegacyTreeNodesDataProviderAdapter(
			RecordTreeNodesDataProvider legacyDataProvider) {
		this.legacyDataProvider = legacyDataProvider;
	}

	@Override
	public TreeNodesProviderResponse<FastContinueInfos> getNodes(String optionalParentId, int start, int maxSize,
																 FastContinueInfos fastContinuationInfos) {

		if (optionalParentId == null) {
			return adapt(start, legacyDataProvider.getRootNodes(start, maxSize, fastContinuationInfos));
		} else {
			return adapt(start, legacyDataProvider.getChildrenNodes(optionalParentId, start, maxSize, fastContinuationInfos));
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
		String schemaType = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		String caption = SchemaCaptionUtils.getCaptionForRecord(record, i18n.getLocale());
		String description = record.get(Schemas.DESCRIPTION_STRING, NO_SUMMARY_METADATA_VALIDATION);
		if (description == null) {
			description = record.get(Schemas.DESCRIPTION_TEXT, NO_SUMMARY_METADATA_VALIDATION);
		}

		Resource collapsedIcon = getCollapsedIconOf(record);
		Resource expandedIcon = getExpandedIconOf(record);

		return new TreeNode(searchRecord.getId(), caption, description, schemaType,
				collapsedIcon, expandedIcon, searchRecord.hasChildren());
	}

	protected Resource getExpandedIconOf(Record record) {
		return FileIconUtils.getIconForRecordId(record, true);
	}

	protected Resource getCollapsedIconOf(Record record) {
		return FileIconUtils.getIconForRecordId(record, false);
	}

}
