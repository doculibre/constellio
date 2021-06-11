package com.constellio.app.modules.restapi.apis.v2.collection;

import com.constellio.app.modules.restapi.apis.v2.core.BaseDaoV2;
import com.constellio.model.entities.records.wrappers.Collection;

import java.util.List;
import java.util.stream.Collectors;

public class CollectionDaoV2 extends BaseDaoV2 {

	public List<Collection> getCollections() {
		return appLayerFactory.getCollectionsManager().getCollectionCodesExcludingSystem().stream()
				.map(code -> appLayerFactory.getCollectionsManager().getCollection(code))
				.collect(Collectors.toList());
	}

}
