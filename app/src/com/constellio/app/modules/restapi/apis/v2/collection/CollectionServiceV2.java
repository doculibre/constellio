package com.constellio.app.modules.restapi.apis.v2.collection;

import com.constellio.app.modules.restapi.apis.v1.collection.dto.CollectionDto;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnauthenticatedUserException;
import com.constellio.app.modules.restapi.apis.v2.core.BaseDaoV2;
import com.constellio.app.modules.restapi.apis.v2.core.BaseServiceV2;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class CollectionServiceV2 extends BaseServiceV2 {

	@Inject
	CollectionDaoV2 collectionDao;

	@Override
	protected BaseDaoV2 getDao() {
		return collectionDao;
	}

	public List<CollectionDto> getCollections(String host, String token) {
		validateHost(host);

		collectionDao.getCollections().stream().filter(collection -> {
			try {
				getUserByToken(token, collection.getCode());
				return true;
			} catch (UnauthenticatedUserException e) {
				return false;
			}
		}).findAny().orElseThrow(UnauthenticatedUserException::new);

		return collectionDao.getCollections().stream()
				.map(collection -> CollectionDto.builder()
						.code(collection.getCode())
						.name(collection.getTitle())
						.languages(collection.getLanguages())
						.build())
				.collect(Collectors.toList());
	}
}
