package com.constellio.app.modules.rm.services.actions;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;

public class ContainerRecordActionsServices {

	private RMSchemasRecordsServices rm;
	private RMModuleExtensions rmModuleExtensions;
	private transient ModelLayerCollectionExtensions modelLayerCollectionExtensions;

	public ContainerRecordActionsServices(String collection, AppLayerFactory appLayerFactory) {
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		modelLayerCollectionExtensions = appLayerFactory.getModelLayerFactory().getExtensions().forCollection(collection);
		rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
	}

	public boolean isEditActionPossible(Record record, User user) {
		return user.hasWriteAccess().on(record) && rmModuleExtensions
				.isEditActionPossibleOnContainerRecord(rm.wrapContainerRecord(record), user);
	}

	public boolean isSlipActionPossible(Record record, User user) {

		return false;
	}

	public boolean isAddToCartActionPossible(Record record, User user) {
		return user.hasReadAccess().on(record) &&
			   (hasUserPermissionToUseCart(user) || hasUserPermissionToUseMyCart(user)) &&
			   rmModuleExtensions.isAddToCartActionPossibleOnContainerRecord(rm.wrapContainerRecord(record), user);
	}

	private boolean hasUserPermissionToUseCart(User user) {
		return user.has(RMPermissionsTo.USE_GROUP_CART).globally();
	}

	private boolean hasUserPermissionToUseMyCart(User user) {
		return user.has(RMPermissionsTo.USE_MY_CART).globally();
	}

	public boolean isLabelsActionPossible(Record record, User user) {
		return false;
	}

	public boolean isDeleteActionPossible(Record record, User user) {
		return false;
	}

	public boolean isEmptyTheBoxActionPossible(Record record, User user) {
		return false;
	}
}
