package com.constellio.app.modules.restapi.apis.v1.collection.dao;

import com.constellio.app.modules.restapi.apis.v1.core.BaseDao;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserServices;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CollectionDao extends BaseDao {

	public List<Collection> getCollections(String username) {

		UserServices userServices = appLayerFactory.getModelLayerFactory().newUserServices();

		SystemWideUserInfos systemWideUserInfos = userServices.getUserInfos(username);

		List<Collection> collections = appLayerFactory.getCollectionsManager().getCollectionCodesExcludingSystem().stream()
				.map(code -> appLayerFactory.getCollectionsManager().getCollection(code))
				.collect(Collectors.toList());

		List<Collection> userCollections = new ArrayList<>();

		for (Collection currentCollection : collections) {
			if (systemWideUserInfos.getStatus(currentCollection.getCode()) == UserCredentialStatus.ACTIVE) {
				userCollections.add(currentCollection);
			}
		}

		return userCollections;
	}
}
