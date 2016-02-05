package com.constellio.app.modules.rm.wrappers.structures;

public enum FolderDecommissioningType {
	CLOSURE, TRANSFER, DEPOSIT, DESTROYAL;

	public boolean isClosureOrDestroyal() {
		return this == CLOSURE || this == DESTROYAL;
	}
}
