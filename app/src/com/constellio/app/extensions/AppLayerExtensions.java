package com.constellio.app.extensions;

import com.constellio.app.api.extensions.RecordExportExtension;
import com.constellio.model.entities.CollectionObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

	public Set<String> getHashsToIncludeInSystemExport() {
		Set<String> hashes = new HashSet<>();
		for (AppLayerCollectionExtensions extensions : collectionExtensions.values()) {
			for (RecordExportExtension extension : extensions.recordExportExtensions) {
				hashes.addAll(extension.getHashsToInclude());
			}
		}

		return hashes;
	}
}
