package com.constellio.model.services.extensions;

import java.util.HashMap;
import java.util.Map;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.model.entities.CollectionObject;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.extensions.ModelLayerSystemExtensions;

public class ModelLayerExtensions implements StatefulService {

	Map<String, ModelLayerCollectionExtensions> collectionExtensions = new HashMap<>();

	ModelLayerSystemExtensions systemWideExtensions = new ModelLayerSystemExtensions();

	@Override
	public void initialize() {
	}

	@Override
	public void close() {
	}

	public ModelLayerSystemExtensions getSystemWideExtensions() {
		return systemWideExtensions;
	}

	public final ModelLayerCollectionExtensions forCollectionOf(CollectionObject collectionObject) {
		return forCollection(collectionObject.getCollection());
	}

	public final ModelLayerCollectionExtensions forCollection(String collection) {
		if (!collectionExtensions.containsKey(collection)) {
			collectionExtensions.put(collection, new ModelLayerCollectionExtensions());
		}
		return collectionExtensions.get(collection);
	}

}
