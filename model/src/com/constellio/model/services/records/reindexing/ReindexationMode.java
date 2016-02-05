package com.constellio.model.services.records.reindexing;

public enum ReindexationMode {

	RECALCULATE, REWRITE, RECALCULATE_AND_REWRITE;

	public boolean isFullRecalculation() {
		return this == RECALCULATE || this == RECALCULATE_AND_REWRITE;
	}

	public boolean isFullRewrite() {
		return this == REWRITE || this == RECALCULATE_AND_REWRITE;
	}
}
