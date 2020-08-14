package com.constellio.app.modules.restapi.extensions;

import com.constellio.app.extensions.ModuleExtensions;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.frameworks.extensions.SingleValueExtension;
import com.constellio.model.entities.records.wrappers.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class RestApiModuleExtensions implements ModuleExtensions {
	private SingleValueExtension<FolderCopyExtension> folderCopyExtension = new SingleValueExtension<>();

	public abstract static class FolderCopyExtension {

		public abstract Folder copy(FolderCopyExtension.FolderCopyParams params);

		@AllArgsConstructor
		@Getter
		public static class FolderCopyParams {
			Folder folder;
			User currentUser;
		}

	}
}
