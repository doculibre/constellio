package com.constellio.app.modules.rm.ui.builders;

import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.entities.FolderComponent;
import com.constellio.app.modules.rm.ui.entities.FolderDetailVO;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailWithType;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.records.RecordServices;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.services.records.GetRecordOptions.RETURNING_SUMMARY;

public class FolderDetailToVOBuilder {
	private final RMSchemasRecordsServices rmRecordServices;
	private final RecordServices recordServices;

	public FolderDetailToVOBuilder(RMSchemasRecordsServices rmRecordServices, RecordServices recordServices) {
		this.rmRecordServices = rmRecordServices;
		this.recordServices = recordServices;
	}

	public FolderDetailVO build(FolderDetailWithType detailWithType, FolderComponent folderComponent) {
		DecomListFolderDetail detail = detailWithType.getDetail();
		Folder folder = rmRecordServices.getFolder(detail.getFolderId());
		Map<String, Object> summaryMetadatasMap = new HashMap<>();

		FolderDetailVO folderDetailVO = new FolderDetailVO();
		folderDetailVO.setFolderId(detail.getFolderId());
		folderDetailVO.setFolderLegacyId(folder.getLegacyId());
		folderDetailVO.setFolderDetailStatus(detail.getFolderDetailStatus());
		folderDetailVO.setContainerRecordId(detail.getContainerRecordId());
		folderDetailVO.setMediumType(detailWithType.getType());
		folderDetailVO.setRetentionRuleId(folder.getRetentionRule());
		folderDetailVO.setCategoryCode(folder.getCategoryCode());
		folderDetailVO.setPackageable(
				!detailWithType.getDecommissioningType().isClosureOrDestroyal() && !detail.isPlacedInContainer());
		folderDetailVO.setSortable(folder.getInactiveDisposalType() == DisposalType.SORT);
		folderDetailVO.setReversedSort(detail.isReversedSort());
		folderDetailVO.setSelected(false);
		folderDetailVO.setLinearSize(detailWithType.getDetail().getFolderLinearSize());
		folderDetailVO.setFolderComponent(folderComponent);
		List<Metadata> summaryMetadatas = folder.getSchema().getSummaryMetadatas();
		for (Metadata metadata : summaryMetadatas) {
			summaryMetadatasMap.put(metadata.getLocalCode(), recordServices.get(folder.getId(), RETURNING_SUMMARY).get(metadata));
		}
		folderDetailVO.setSummaryMetadatasMap(summaryMetadatasMap);
		return folderDetailVO;
	}
}
