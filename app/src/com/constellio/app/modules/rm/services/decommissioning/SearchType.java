package com.constellio.app.modules.rm.services.decommissioning;

import com.constellio.app.modules.rm.model.enums.DecommissioningListType;

public enum SearchType {
	fixedPeriod, code888, code999, transfer, activeToDeposit, activeToDestroy, semiActiveToDeposit, semiActiveToDestroy,
	documentTransfer, documentActiveToDeposit, documentActiveToDestroy, documentSemiActiveToDeposit, documentSemiActiveToDestroy;

	public DecommissioningListType toDecomListType() {
		switch (this) {
		case fixedPeriod:
		case code888:
		case code999:
			return DecommissioningListType.FOLDERS_TO_CLOSE;
		case transfer:
			return DecommissioningListType.FOLDERS_TO_TRANSFER;
		case activeToDeposit:
		case semiActiveToDeposit:
			return DecommissioningListType.FOLDERS_TO_DEPOSIT;
		case activeToDestroy:
		case semiActiveToDestroy:
			return DecommissioningListType.FOLDERS_TO_DESTROY;
		case documentTransfer:
			return DecommissioningListType.DOCUMENTS_TO_TRANSFER;
		case documentActiveToDeposit:
		case documentSemiActiveToDeposit:
			return DecommissioningListType.DOCUMENTS_TO_DEPOSIT;
		case documentActiveToDestroy:
		case documentSemiActiveToDestroy:
			return DecommissioningListType.DOCUMENTS_TO_DESTROY;
		}
		return null;
	}

	public boolean isFromSemiActive() {
		return this == semiActiveToDeposit || this == semiActiveToDestroy ||
				this == documentSemiActiveToDeposit || this == documentSemiActiveToDestroy;
	}

	public boolean isFolderSearch() {
		return toDecomListType().isFolderList();
	}
}
