package com.constellio.app.extensions;

import com.constellio.model.entities.CollectionObject;

import java.util.HashMap;
import java.util.Map;

public class AppLayerExtensions {

	Map<String, AppLayerCollectionExtensions> collectionExtensions = new HashMap<>();

	AppLayerSystemExtensions systemWideExtensions = new AppLayerSystemExtensions();

	public AppLayerCollectionExtensions forCollection(String collection) {
		if (!collectionExtensions.containsKey(collection)) {
			collectionExtensions.put(collection, new AppLayerCollectionExtensions());
		}
		return collectionExtensions.get(collection);
	}

	public AppLayerCollectionExtensions forCollectionOf(CollectionObject collectionObject) {
		return forCollection(collectionObject.getCollection());
	}

	public AppLayerSystemExtensions getSystemWideExtensions() {
		return systemWideExtensions;
	}

}
