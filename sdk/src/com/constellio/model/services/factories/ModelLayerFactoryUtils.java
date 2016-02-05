package com.constellio.model.services.factories;

import java.security.Key;

public class ModelLayerFactoryUtils {

	public static void setApplicationEncryptionKey(ModelLayerFactory modelLayerFactory, Key key) {
		modelLayerFactory.setEncryptionKey(key);
	}

}
