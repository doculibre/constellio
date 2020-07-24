package com.constellio.app.extensions.restapi;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.frameworks.extensions.SingleValueExtension;
import com.constellio.model.entities.records.wrappers.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class FolderDuplicationExtension {

	private SingleValueExtension<FolderCopyExtension> folderCopyExtension = new SingleValueExtension<>();

	public abstract static class FolderCopyExtension {

		public abstract Folder copy(FolderCopyParams params);

		@AllArgsConstructor
		@Getter
		public static class FolderCopyParams {
			Folder folder;
			User currentUser;
		}

	}
}
