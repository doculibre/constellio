/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
		folderDetailVO.setValidationUserId(detail.getValidationUserId());
		folderDetailVO.setValidationDate(detail.getValidationDate());
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
