package com.constellio.app.modules.restapi.cart.dao;

import com.constellio.app.modules.restapi.cart.dto.CartDto;
import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.core.exception.RecordNotFoundException;
import com.constellio.app.modules.restapi.validation.exception.UnauthorizedAccessException;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.utils.CartUtil;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.users.SystemWideUserInfos;

import java.util.List;

public class CartDao extends BaseDao {

	public CartDto getCart(User user, Record cartRecord) {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(cartRecord.getCollection(), appLayerFactory);
		Cart cart = rm.wrapCart(cartRecord);

		CartDto cartDto = CartDto.builder()
				.id(cart.getId())
				.title(cart.getTitle())
				.owner(cart.getOwner())
				.build();

		return cartDto;
	}

	public CartDto createCart(User user, String collection, CartDto cartDto)
			throws Exception {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		Cart cart = rm.newCart();
		cart.setTitle(cartDto.getTitle());
		cart.setOwner(user);

		recordServices.execute(new Transaction(cart.getWrappedRecord()).setUser(user));

		cartDto.setId(cart.getId());
		cartDto.setOwner(cart.getOwner());
		return cartDto;
	}

	public CartDto updateCart(User user, Record cartRecord, CartDto cartDto)
			throws Exception {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(cartRecord.getCollection(), appLayerFactory);
		Cart cart = rm.wrapCart(cartRecord);
		cart.setTitle(cartDto.getTitle());

		recordServices.update(cart.getWrappedRecord(), user);

		cartDto.setId(cart.getId());
		cartDto.setOwner(cart.getOwner());
		return cartDto;
	}

	public void deleteCart(User user, Record cartRecord) {
		Boolean logicallyDeleted = cartRecord.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS);
		if (!Boolean.TRUE.equals(logicallyDeleted)) {
			recordServices.logicallyDelete(cartRecord, user);
		}
		recordServices.physicallyDelete(cartRecord, user);
	}

	public void deleteCartContent(String username, String cartId)
			throws Exception {

		// Delete "My cart" content
		SystemWideUserInfos userCredentials = userServices.getUserInfos(username);
		List<String> collections = userCredentials.getCollections();
		for (String collection : collections) {
			User user = getUserByUsername(username, collection);
			if (user.getId().equals(cartId)) {
				validateMyCartPermission(user);
				removeAllFromFavorite(user, cartId);
				return;
			}
		}

		// Delete custom group cart content
		Record cartRecord = getRecordById(cartId);
		if (cartRecord == null) {
			throw new RecordNotFoundException(cartId);
		}

		User user = getUserByUsername(username, cartRecord.getCollection());
		validateCartGroupPermission(user);
		removeAllFromFavorite(user, cartId);
	}

	private void removeAllFromFavorite(User user, String cartId)
			throws Exception {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(user.getCollection(), appLayerFactory);
		CartUtil cartUtil = new CartUtil(user.getCollection(), appLayerFactory);

		List<Record> records = cartUtil.getCartRecords(cartId);
		for (Record record : records) {
			removeFromFavorite(rm, record, cartId);
		}

		Transaction transaction = new Transaction();
		transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		transaction.setOptions(RecordUpdateOptions.validationExceptionSafeOptions());
		transaction.setUser(user);
		transaction.addUpdate(records);

		recordServices.execute(transaction);
	}

	private void removeFromFavorite(RMSchemasRecordsServices rm, Record record, String cartId) {
		String schemaCode = record.getSchemaCode();

		if (schemaCode.startsWith(Folder.SCHEMA_TYPE)) {
			Folder folder = rm.wrapFolder(record);
			folder.removeFavorite(cartId);
		} else if (schemaCode.startsWith(Document.SCHEMA_TYPE)) {
			Document document = rm.wrapDocument(record);
			document.removeFavorite(cartId);
		} else if (schemaCode.startsWith(ContainerRecord.SCHEMA_TYPE)) {
			ContainerRecord containerRecord = rm.wrapContainerRecord(record);
			containerRecord.removeFavorite(cartId);
		}
	}

	private void validateCartGroupPermission(User user) {
		if (!user.has(RMPermissionsTo.USE_GROUP_CART).globally()) {
			throw new UnauthorizedAccessException();
		}
	}

	private void validateMyCartPermission(User user) {
		if (!user.has(RMPermissionsTo.USE_MY_CART).globally()) {
			throw new UnauthorizedAccessException();
		}
	}
}
