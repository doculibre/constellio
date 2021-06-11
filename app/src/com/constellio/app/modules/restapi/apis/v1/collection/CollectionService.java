package com.constellio.app.modules.restapi.apis.v1.collection;

import com.constellio.app.modules.restapi.apis.v1.collection.dao.CollectionDao;
import com.constellio.app.modules.restapi.apis.v1.collection.dto.CollectionDto;
import com.constellio.app.modules.restapi.apis.v1.core.BaseDao;
import com.constellio.app.modules.restapi.apis.v1.core.BaseService;
import com.constellio.app.modules.restapi.apis.v1.validation.ValidationService;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class CollectionService extends BaseService {

	@Inject
	private CollectionDao collectionDao;

	@Inject
	private ValidationService validationService;

	@Override
	protected BaseDao getDao() {
		return collectionDao;
	}

	public List<CollectionDto> getCollections(String host, String token, String serviceKey) {
		validationService.validateHost(host);
		validationService.validateToken(token, serviceKey);

		String username = this.getUsernameByServiceKey(serviceKey);

		return collectionDao.getCollections(username).stream()
				.map(collection -> CollectionDto.builder()
						.code(collection.getCode())
						.name(collection.getTitle())
						.languages(collection.getLanguages())
						.build())
				.collect(Collectors.toList());
	}
}
