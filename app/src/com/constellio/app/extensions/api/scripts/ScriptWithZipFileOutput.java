package com.constellio.app.extensions.api.scripts;

import com.constellio.app.services.factories.AppLayerFactory;

import java.io.File;

public abstract class ScriptWithZipFileOutput extends ScriptWithFileOutput {

	public ScriptWithZipFileOutput(AppLayerFactory appLayerFactory, String category, String name) {
		super(appLayerFactory, category, name);
	}

	public ScriptWithZipFileOutput(AppLayerFactory appLayerFactory, String category, String name,
								   boolean saveOutputAsTemporaryRecord) {
		super(appLayerFactory, category, name, saveOutputAsTemporaryRecord);
	}


	@Override
	public ScriptOutput getScriptOutput() {
		return ScriptOutput.toZipFile(getClass().getSimpleName() + ".zip");
	}

	public File newOutputFile(String outputFilename) {
		File zippedFolder = outputLogger.getTempFile().getParentFile();
		return new File(zippedFolder, outputFilename);
	}
}
