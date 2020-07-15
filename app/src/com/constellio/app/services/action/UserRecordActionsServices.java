package com.constellio.app.services.action;


import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserServices;

public class UserRecordActionsServices {

	private RMSchemasRecordsServices rm;
	private RMModuleExtensions rmModuleExtensions;
	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private String collection;
	private UserServices userServices;

	public UserRecordActionsServices(String collection, AppLayerFactory appLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.userServices = modelLayerFactory.newUserServices();
		this.rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
	}

	public boolean isEditActionPossible(Record record, User user) {
		return userServices.canAddOrModifyUserAndGroup()
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS);
	}

	public boolean isDisplayActionPossible(Record record, User user) {
		return userServices.canAddOrModifyUserAndGroup()
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS);
	}

	public boolean isAddToGroupActionPossible(Record record, User user) {
		return userServices.canAddOrModifyUserAndGroup()
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS);
	}

	public boolean isAddToCollectionActionPossible(Record record, User user) {
		return userServices.canAddOrModifyUserAndGroup()
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS);
	}

	public boolean isChangeStatusActionPossible(Record record, User user) {
		return userServices.canAddOrModifyUserAndGroup()
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS);
	}

	public boolean isManageSecurityActionPossible(Record record, User user) {
		return userServices.canAddOrModifyUserAndGroup()
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS);
	}

	public boolean isManageRoleActionPossible(Record record, User user) {
		return userServices.canAddOrModifyUserAndGroup()
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS);
	}

	public boolean isDeleteActionPossible(Record record, User user) {
		return userServices.canAddOrModifyUserAndGroup()
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS);
	}

}
