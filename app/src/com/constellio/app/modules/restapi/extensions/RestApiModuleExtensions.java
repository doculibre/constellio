package com.constellio.app.modules.restapi.extensions;

import com.constellio.app.extensions.ModuleExtensions;
import com.constellio.app.extensions.restapi.FolderDuplicationExtension;

public class RestApiModuleExtensions implements ModuleExtensions {
	public FolderDuplicationExtension folderDuplicationExtension = new FolderDuplicationExtension();
}
