package com.constellio.app.modules.rm.model.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum FolderStatus implements EnumWithSmallCode {

	ACTIVE("a"), SEMI_ACTIVE("s"), INACTIVE_DESTROYED("d"), INACTIVE_DEPOSITED("v");

	private String code;

	FolderStatus(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public boolean isActive() {
		return this == ACTIVE;
	}

	public boolean isSemiActive() {
		return this == SEMI_ACTIVE;
	}

	public boolean isInactive() {
		return this == INACTIVE_DEPOSITED || this == INACTIVE_DESTROYED;
	}

	public boolean isDeposited() {
		return this == INACTIVE_DEPOSITED;
	}

	public boolean isDestroyed() {
		return this == INACTIVE_DESTROYED;
	}

	public boolean isActiveOrSemiActive() {
		return isActive() || isSemiActive();
	}

	public boolean isSemiActiveOrInactive() {
		return isSemiActive() || isInactive();
	}

	public boolean isAfter(FolderStatus folderStatus) {
		switch (this) {

		case INACTIVE_DEPOSITED:
			return folderStatus == SEMI_ACTIVE || folderStatus == ACTIVE;

		case INACTIVE_DESTROYED:
			return folderStatus == SEMI_ACTIVE || folderStatus == ACTIVE;

		case SEMI_ACTIVE:
			return folderStatus == ACTIVE;

		default:
			return false;

		}
	}

	public boolean isBefore(FolderStatus folderStatus) {
		switch (this) {

		case ACTIVE:
			return folderStatus == SEMI_ACTIVE || folderStatus == INACTIVE_DEPOSITED || folderStatus == INACTIVE_DESTROYED;

		case SEMI_ACTIVE:
			return folderStatus == INACTIVE_DEPOSITED || folderStatus == INACTIVE_DESTROYED;

		default:
			return false;

		}
	}
}
