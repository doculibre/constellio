package com.constellio.app.modules.restapi.user;

import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.core.service.BaseService;
import com.constellio.app.modules.restapi.user.dao.UserDao;
import com.constellio.app.modules.restapi.user.dto.UserCredentialsConfigDto;
import com.constellio.app.modules.restapi.user.dto.UserCredentialsContentDto;
import com.constellio.app.modules.restapi.user.dto.UserDto;
import com.constellio.app.modules.restapi.validation.ValidationService;
import com.constellio.app.modules.restapi.validation.dao.ValidationDao;
import com.constellio.app.modules.restapi.validation.exception.UnauthenticatedUserException;
import com.constellio.model.entities.records.wrappers.User;

import javax.inject.Inject;
import java.io.InputStream;

public class UserService extends BaseService {

	@Inject
	private UserDao userDao;

	@Inject
	private ValidationDao validationDao;

	@Inject
	private ValidationService validationService;

	@Override
	protected BaseDao getDao() {
		return userDao;
	}

	public UserDto getUser(String host, String token, String serviceKey, String collection) {
		validationService.validateHost(host);
		validationService.validateToken(token, serviceKey);

		User user;
		try {
			user = getUserByServiceKey(serviceKey, collection);
		} catch (Exception e) {
			throw new UnauthenticatedUserException();
		}
		return UserDto.builder().id(user.getId()).build();
	}

	public UserCredentialsContentDto getContent(String host, String token, String serviceKey, String metadataCode) {
		validationService.validateHost(host);
		validationService.validateToken(token, serviceKey);

		String username = validationDao.getUsernameByServiceKey(serviceKey);
		return userDao.getContent(username, metadataCode);
	}

	public void setContent(String host, String token, String serviceKey, String metadataCode,
						   UserCredentialsContentDto userSignature, InputStream fileStream) {
		validationService.validateHost(host);
		validationService.validateToken(token, serviceKey);

		String username = validationDao.getUsernameByServiceKey(serviceKey);
		userDao.setContent(username, metadataCode, userSignature.getFilename(), fileStream);
	}

	public void deleteContent(String host, String token, String serviceKey, String metadataCode) {
		validationService.validateHost(host);
		validationService.validateToken(token, serviceKey);

		String username = validationDao.getUsernameByServiceKey(serviceKey);
		userDao.deleteContent(username, metadataCode);
	}

	public UserCredentialsConfigDto getConfig(String host, String token, String serviceKey, String metadataCode) {
		validationService.validateHost(host);
		validationService.validateToken(token, serviceKey);

		String username = validationDao.getUsernameByServiceKey(serviceKey);
		return userDao.getConfig(username, metadataCode);
	}

	public void setConfig(String host, String token, String serviceKey, String metadataCode,
						  UserCredentialsConfigDto config) {
		validationService.validateHost(host);
		validationService.validateToken(token, serviceKey);

		String username = validationDao.getUsernameByServiceKey(serviceKey);
		userDao.setConfig(username, metadataCode, config);
	}
}
