package com.constellio.model.services.contents;

import java.util.List;

public class ContentModifications {

	private List<String> deletedHashes;

	private List<String> newHashes;

	public ContentModifications(List<String> deletedHashes,
			List<String> newHashes) {
		this.deletedHashes = deletedHashes;
		this.newHashes = newHashes;
	}

	public List<String> getDeletedContentsVersionsHashes() {
		return deletedHashes;
	}

	public List<String> getContentsWithNewVersion() {
		return newHashes;
	}
}
