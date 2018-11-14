package com.constellio.app.extensions.api.scripts;

import com.constellio.app.services.factories.AppLayerFactory;

public abstract class ScriptWithLogOutput extends ScriptWithFileOutput {

	public ScriptWithLogOutput(AppLayerFactory appLayerFactory, String category, String name) {
		super(appLayerFactory, category, name);
	}

	public ScriptWithLogOutput(AppLayerFactory appLayerFactory, String category, String name,
							   boolean saveOutputAsTemporaryRecord) {
		super(appLayerFactory, category, name, saveOutputAsTemporaryRecord);
	}

	@Override
	protected String getFilename() {
		return getClass().getSimpleName() + ".log";
	}
}
