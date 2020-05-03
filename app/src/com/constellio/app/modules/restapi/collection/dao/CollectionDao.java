package com.constellio.app.modules.restapi.collection.dao;

import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.model.entities.records.wrappers.Collection;

import java.util.List;
import java.util.stream.Collectors;

public class CollectionDao extends BaseDao {

	public List<Collection> getCollections() {
		return appLayerFactory.getCollectionsManager().getCollectionCodesExcludingSystem().stream()
				.map(code -> appLayerFactory.getCollectionsManager().getCollection(code))
				.collect(Collectors.toList());
	}

}
