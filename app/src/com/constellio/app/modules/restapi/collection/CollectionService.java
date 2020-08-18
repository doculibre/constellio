package com.constellio.app.modules.restapi.collection;

import com.constellio.app.modules.restapi.collection.dao.CollectionDao;
import com.constellio.app.modules.restapi.collection.dto.CollectionDto;
import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.core.service.BaseService;
import com.constellio.app.modules.restapi.validation.ValidationService;

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

		return collectionDao.getCollections().stream()
				.map(collection -> CollectionDto.builder()
						.code(collection.getCode())
						.name(collection.getTitle())
						.languages(collection.getLanguages())
						.build())
				.collect(Collectors.toList());
	}
}
