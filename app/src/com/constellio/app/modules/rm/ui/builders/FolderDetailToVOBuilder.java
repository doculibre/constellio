package com.constellio.app.modules.rm.ui.builders;

import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.entities.FolderDetailVO;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailWithType;

public class FolderDetailToVOBuilder {
	private final RMSchemasRecordsServices rmRecordServices;

	public FolderDetailToVOBuilder(RMSchemasRecordsServices rmRecordServices) {
		this.rmRecordServices = rmRecordServices;
	}

	public FolderDetailVO build(FolderDetailWithType detailWithType) {
		DecomListFolderDetail detail = detailWithType.getDetail();
		Folder folder = rmRecordServices.getFolder(detail.getFolderId());

		FolderDetailVO folderDetailVO = new FolderDetailVO();
		folderDetailVO.setFolderId(detail.getFolderId());
		folderDetailVO.setFolderIncluded(detail.isFolderIncluded());
		folderDetailVO.setContainerRecordId(detail.getContainerRecordId());
		folderDetailVO.setMediumType(detailWithType.getType());
		folderDetailVO.setRetentionRuleId(folder.getRetentionRule());
		folderDetailVO.setCategoryCode(folder.getCategoryCode());
		folderDetailVO.setPackageable(
				!detailWithType.getDecommissioningType().isClosureOrDestroyal());
		folderDetailVO.setSortable(folder.getInactiveDisposalType() == DisposalType.SORT);
		folderDetailVO.setReversedSort(detail.isReversedSort());
		folderDetailVO.setSelected(false);

		return folderDetailVO;
	}
}
