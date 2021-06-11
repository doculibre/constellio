package com.constellio.app.servlet.userSecurity;

import com.constellio.app.servlet.BaseServletDao;
import com.constellio.app.servlet.BaseServletService;
import com.constellio.app.servlet.userSecurity.dto.UserSecurityInfoDto;

public class UserSecurityInfoService extends BaseServletService {

	private UserSecurityInfoDao dao;

	@Override
	protected BaseServletDao getDao() {
		return dao;
	}

	public UserSecurityInfoService() {
		dao = new UserSecurityInfoDao();
	}

	public UserSecurityInfoDto getSecurityInfo(String token, String serviceKey) {
		validateToken(token, serviceKey);

		String username = getUsernameByServiceKey(serviceKey);
		return dao.getSecurityInfo(username);
	}
}
