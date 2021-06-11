package com.constellio.app.modules.restapi.apis.v1.cart;

import com.constellio.app.modules.restapi.apis.v1.cart.dao.CartDao;
import com.constellio.app.modules.restapi.apis.v1.cart.dto.CartDto;
import com.constellio.app.modules.restapi.apis.v1.core.BaseDao;
import com.constellio.app.modules.restapi.apis.v1.core.BaseService;
import com.constellio.app.modules.restapi.apis.v1.validation.ValidationService;
import com.constellio.app.modules.restapi.apis.v1.validation.dao.ValidationDao;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnauthenticatedUserException;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnauthorizedAccessException;
import com.constellio.app.modules.restapi.core.exception.RequiredParameterException;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class CartService extends BaseService {

	@Inject
	private CartDao cartDao;

	@Inject
	private ValidationDao validationDao;

	@Inject
	private ValidationService validationService;

	@Override
	protected BaseDao getDao() {
		return cartDao;
	}

	public CartDto getCart(String host, String token, String serviceKey, String id) {
		validationService.validateHost(host);
		validationService.validateToken(token, serviceKey);

		Record cartRecord = getRecord(id, true);
		User user = getUserByServiceKey(serviceKey, cartRecord.getCollection());
		validateCartGroupPermission(user);

		return cartDao.getCart(user, cartRecord);
	}

	public CartDto createCart(String host, String token, String serviceKey, String collection, CartDto cartDto)
			throws Exception {
		validationService.validateHost(host);
		validationService.validateToken(token, serviceKey);

		User user;
		try {
			user = getUserByServiceKey(serviceKey, collection);
		} catch (Exception e) {
			throw new UnauthenticatedUserException();
		}
		validateCartGroupPermission(user);

		if (cartDto.getTitle() == null) {
			throw new RequiredParameterException("cart.title");
		}

		return cartDao.createCart(user, collection, cartDto);
	}

	public CartDto updateCart(String host, String token, String serviceKey, String id, CartDto cartDto)
			throws Exception {
		validationService.validateHost(host);
		validationService.validateToken(token, serviceKey);

		Record cartRecord = getRecord(id, true);
		User user = getUserByServiceKey(serviceKey, cartRecord.getCollection());
		validateCartGroupPermission(user);

		if (cartDto.getTitle() == null) {
			throw new RequiredParameterException("cart.title");
		}

		return cartDao.updateCart(user, cartRecord, cartDto);
	}

	public void deleteCart(String host, String token, String serviceKey, String id) {
		validationService.validateHost(host);
		validationService.validateToken(token, serviceKey);

		Record cartRecord = getRecord(id, false);
		User user = getUserByServiceKey(serviceKey, cartRecord.getCollection());
		validateCartGroupPermission(user);

		cartDao.deleteCart(user, cartRecord);
	}

	public void addCartContent(String host, String token, String serviceKey, String id, List<String> itemsToAdd)
			throws Exception {
		validationService.validateHost(host);
		validationService.validateToken(token, serviceKey);

		String username = validationDao.getUsernameByServiceKey(serviceKey);
		User user = cartDao.getUserForCart(username, id);

		List<Record> recordsToAdd = new ArrayList<>();
		for (String itemId : itemsToAdd) {
			Record item = getRecord(itemId, true);
			if (!user.hasReadAccess().on(item)) {
				throw new UnauthorizedAccessException();
			}
			if (!recordsToAdd.contains(item)) {
				recordsToAdd.add(item);
			}
		}

		cartDao.addCartContent(user, id, recordsToAdd);
	}

	public void removeCartContent(String host, String token, String serviceKey, String id, List<String> itemsToRemove)
			throws Exception {
		validationService.validateHost(host);
		validationService.validateToken(token, serviceKey);

		List<Record> recordsToRemove = new ArrayList<>();
		for (String itemId : itemsToRemove) {
			Record item = getRecord(itemId, true);
			if (!recordsToRemove.contains(item)) {
				recordsToRemove.add(item);
			}
		}

		String username = validationDao.getUsernameByServiceKey(serviceKey);
		User user = cartDao.getUserForCart(username, id);

		cartDao.removeCartContent(user, id, recordsToRemove);
	}

	public void deleteCartContent(String host, String token, String serviceKey, String id)
			throws Exception {
		validationService.validateHost(host);
		validationService.validateToken(token, serviceKey);

		String username = validationDao.getUsernameByServiceKey(serviceKey);
		User user = cartDao.getUserForCart(username, id);

		cartDao.deleteCartContent(user, id);
	}

	private void validateCartGroupPermission(User user) {
		if (!user.has(RMPermissionsTo.USE_GROUP_CART).globally()) {
			throw new UnauthorizedAccessException();
		}
	}
}
