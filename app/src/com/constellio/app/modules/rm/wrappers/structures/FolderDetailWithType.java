package com.constellio.app.modules.rm.wrappers.structures;

import java.io.Serializable;

import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.enums.FolderMediaType;

public class FolderDetailWithType implements Serializable {
	private final DecomListFolderDetail detail;
	private final FolderMediaType type;
	private final DecommissioningListType listType;

	public FolderDetailWithType(DecomListFolderDetail detail, FolderMediaType type,
			DecommissioningListType listType) {
		this.detail = detail;
		this.type = type;
		this.listType = listType;
	}

	public String getFolderId() {
		return detail.getFolderId();
	}

	public DecomListFolderDetail getDetail() {
		return detail;
	}

	public FolderMediaType getType() {
		return type;
	}

	public FolderDecommissioningType getDecommissioningType() {
		switch (listType) {
		case FOLDERS_TO_CLOSE:
			return FolderDecommissioningType.CLOSURE;
		case FOLDERS_TO_TRANSFER:
			return FolderDecommissioningType.TRANSFER;
		case FOLDERS_TO_DEPOSIT:
			return (detail.isReversedSort()) ?
					FolderDecommissioningType.DESTROYAL :
					FolderDecommissioningType.DEPOSIT;
		case FOLDERS_TO_DESTROY:
			return (detail.isReversedSort()) ?
					FolderDecommissioningType.DEPOSIT :
					FolderDecommissioningType.DESTROYAL;
		}
		return null;
	}

	public boolean isIncluded() {
		return detail.isFolderIncluded();
	}

	public boolean isExcluded() {
		return detail.isFolderExcluded();
	}
}
