package com.constellio.app.modules.restapi.category;

import com.constellio.app.modules.restapi.category.dao.CategoryDao;
import com.constellio.app.modules.restapi.category.dto.CategoryDto;
import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.core.exception.RequiredParameterException;
import com.constellio.app.modules.restapi.core.service.BaseService;
import com.constellio.app.modules.restapi.validation.ValidationService;
import com.constellio.app.modules.restapi.validation.exception.UnauthenticatedUserException;
import com.constellio.model.entities.records.wrappers.User;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.List;

public class CategoryService extends BaseService {

	@Inject
	private CategoryDao categoryDao;

	@Inject
	private ValidationService validationService;

	@Override
	protected BaseDao getDao() {
		return categoryDao;
	}

	public List<CategoryDto> search(String host, String token, String serviceKey, String collection, String expression)
			throws Exception {
		validationService.validateHost(host);
		validationService.validateToken(token, serviceKey);

		User user;
		try {
			user = getUserByServiceKey(serviceKey, collection);
		} catch (Exception e) {
			throw new UnauthenticatedUserException();
		}

		if (StringUtils.isBlank(expression)) {
			throw new RequiredParameterException("expression");
		}

		return categoryDao.search(user, expression);
	}
}
