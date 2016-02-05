package com.constellio.app.modules.rm.model.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum DecommissioningListType implements EnumWithSmallCode {
	FOLDERS_TO_CLOSE("X"), FOLDERS_TO_TRANSFER("T"), FOLDERS_TO_DEPOSIT("C"), FOLDERS_TO_DESTROY("D"),
	DOCUMENTS_TO_TRANSFER("Td"), DOCUMENTS_TO_DEPOSIT("Cd"), DOCUMENTS_TO_DESTROY("Dd");

	private String code;

	DecommissioningListType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public DecommissioningType getDecommissioningType() {
		if (isTransfert()) {
			return DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE;
		} else if (isDeposit()) {
			return DecommissioningType.DEPOSIT;
		} else if (isDestroyal()) {
			return DecommissioningType.DESTRUCTION;
		}
		return null;
	}

	public boolean isDocumentList() {
		return this == DOCUMENTS_TO_TRANSFER || this == DOCUMENTS_TO_DEPOSIT || this == DOCUMENTS_TO_DESTROY;
	}

	public boolean isFolderList() {
		return !isDocumentList();
	}

	public boolean isClosing() {
		return this == FOLDERS_TO_CLOSE;
	}

	public boolean isTransfert() {
		return this == FOLDERS_TO_TRANSFER || this == DOCUMENTS_TO_TRANSFER;
	}

	public boolean isDeposit() {
		return this == FOLDERS_TO_DEPOSIT || this == DOCUMENTS_TO_DEPOSIT;
	}

	public boolean isDestroyal() {
		return this == FOLDERS_TO_DESTROY || this == DOCUMENTS_TO_DESTROY;
	}

	public boolean isClosingOrDestroyal() {
		return isClosing() || isDestroyal();
	}

	public boolean isDepositOrDestroyal() {
		return isDeposit() || isDestroyal();
	}
}
