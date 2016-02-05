package com.constellio.data.extensions;

import java.util.HashMap;
import java.util.Map;

public class DataLayerExtensions {

	Map<String, DataLayerCollectionExtensions> collectionExtensions = new HashMap<>();

	DataLayerSystemExtensions systemWideExtensions = new DataLayerSystemExtensions();

	public DataLayerSystemExtensions getSystemWideExtensions() {
		return systemWideExtensions;
	}

	public DataLayerCollectionExtensions getCollectionListeners(String collection) {
		return collectionExtensions.get(collection);
	}

}
