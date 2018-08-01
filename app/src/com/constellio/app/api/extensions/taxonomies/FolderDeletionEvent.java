package com.constellio.app.api.extensions.taxonomies;

import com.constellio.app.modules.rm.wrappers.Folder;

/**
 * Created by Constelio on 2016-10-19.
 */
public class FolderDeletionEvent {
	Folder folder;

	public FolderDeletionEvent(Folder folder) {
		this.folder = folder;
	}

	public Folder getFolder() {
		return this.folder;
	}
}
