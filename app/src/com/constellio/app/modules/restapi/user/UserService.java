package com.constellio.app.modules.restapi.user;

import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.core.service.BaseService;
import com.constellio.app.modules.restapi.user.dao.UserDao;
import com.constellio.app.modules.restapi.user.dto.UserSignatureContentDto;
import com.constellio.app.modules.restapi.user.dto.UserSignatureDto;
import com.constellio.app.modules.restapi.validation.ValidationService;
import com.constellio.app.modules.restapi.validation.dao.ValidationDao;
import com.constellio.model.entities.security.global.UserCredential;

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

	public UserSignatureContentDto getSignature(String host, String token, String serviceKey) {

		validationService.validateHost(host);
		validationService.validateToken(token, serviceKey);

		String username = validationDao.getUsernameByServiceKey(serviceKey);
		return userDao.getContent(username, UserCredential.ELECTRONIC_SIGNATURE);
	}

	public void setSignature(String host, String token, String serviceKey, UserSignatureDto userSignature,
							 InputStream fileStream) {

		validationService.validateHost(host);
		validationService.validateToken(token, serviceKey);

		String username = validationDao.getUsernameByServiceKey(serviceKey);
		userDao.setContent(username, UserCredential.ELECTRONIC_SIGNATURE, userSignature.getFilename(), fileStream);
	}

	public UserSignatureContentDto getInitials(String host, String token, String serviceKey) {

		validationService.validateHost(host);
		validationService.validateToken(token, serviceKey);

		String username = validationDao.getUsernameByServiceKey(serviceKey);
		return userDao.getContent(username, UserCredential.ELECTRONIC_INITIALS);
	}

	public void setInitials(String host, String token, String serviceKey, UserSignatureDto userInitials,
							InputStream fileStream) {

		validationService.validateHost(host);
		validationService.validateToken(token, serviceKey);

		String username = validationDao.getUsernameByServiceKey(serviceKey);
		userDao.setContent(username, UserCredential.ELECTRONIC_INITIALS, userInitials.getFilename(), fileStream);
	}
}
