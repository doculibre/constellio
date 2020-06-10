package com.constellio.app.modules.restapi.cart.dao;

import com.constellio.app.modules.restapi.cart.dto.CartDto;
import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.core.exception.RecordLogicallyDeletedException;
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

import java.util.List;

public class CartDao extends BaseDao {

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

		if (Boolean.TRUE.equals(logicallyDeleted)) {
			throw new RecordLogicallyDeletedException(cartRecord.getId());
		}
		recordServices.logicallyDelete(cartRecord, user);
	}

	public void deleteCartContent(User user, Record cartRecord)
			throws Exception {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(cartRecord.getCollection(), appLayerFactory);
		CartUtil cartUtil = new CartUtil(cartRecord.getCollection(), appLayerFactory);

		List<Record> records = cartUtil.getCartRecords(cartRecord.getId());
		for (Record record : records) {
			removeFromFavorite(rm, record, cartRecord.getId());
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
}
