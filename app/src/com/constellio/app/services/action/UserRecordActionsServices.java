package com.constellio.app.services.action;


import com.constellio.app.extensions.AppLayerSystemExtensions;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserSyncMode;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.users.UserServices;

public class UserRecordActionsServices {

	private SchemasRecordsServices core;
	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private String collection;
	private UserServices userServices;
	private AppLayerSystemExtensions appLayerSystemExtensions;

	public UserRecordActionsServices(String collection, AppLayerFactory appLayerFactory) {
		this.core = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.userServices = modelLayerFactory.newUserServices();
		this.appLayerSystemExtensions = appLayerFactory.getExtensions().getSystemWideExtensions();
	}

	public boolean isEditActionPossible(Record record, User user) {
		return userServices.canAddOrModifyUserAndGroup()
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS)
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SECURITY);
	}

	public boolean isDisplayActionPossible(Record record, User user) {
		return userServices.canAddOrModifyUserAndGroup()
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS)
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SECURITY);
	}

	public boolean isAddToGroupActionPossible(Record record, User user) {
		return userServices.canAddOrModifyUserAndGroup()
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS)
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SECURITY);
	}

	public boolean isAddToCollectionActionPossible(Record record, User user) {
		return userServices.canAddOrModifyUserAndGroup() && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_COLLECTIONS)
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS)
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SECURITY);
	}

	public boolean isChangeStatusActionPossible(Record record, User user) {
		return userServices.canAddOrModifyUserAndGroup()
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS)
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SECURITY);
	}

	public boolean isSynchroActionPossible(Record record, User user) {
		User targetUser = core.wrapUser(record);
		UserCredential credential = userServices.getUserCredential(targetUser.getUsername());

		return userServices.canAddOrModifyUserAndGroup()
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS)
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SECURITY)
			   && credential.getSyncMode() == UserSyncMode.NOT_SYNCED;
	}

	public boolean isDesynchroActionPossible(Record record, User user) {
		User targetUser = core.wrapUser(record);
		UserCredential credential = userServices.getUserCredential(targetUser.getUsername());

		return userServices.canAddOrModifyUserAndGroup()
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS)
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SECURITY)
			   && credential.getSyncMode() == UserSyncMode.SYNCED;
	}

	public boolean isManageSecurityActionPossible(Record record, User user) {
		return userServices.canAddOrModifyUserAndGroup()
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS)
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SECURITY);
	}

	public boolean isManageRoleActionPossible(Record record, User user) {
		return userServices.canAddOrModifyUserAndGroup()
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS)
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SECURITY);
	}

	public boolean isDeleteActionPossible(Record record, User user) {
		return userServices.canAddOrModifyUserAndGroup()
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS)
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SECURITY);
	}

	public boolean isGenerateTokenActionPossibe(Record record, User user) {
		String username = core.wrapUser(record).getUsername();

		return userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS)
			   && appLayerSystemExtensions.isGenerateTokenActionPossibleOnSystemWideUserInfos(userServices.getUserInfos(username), user)
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SECURITY);
	}

	public boolean isTransferPermissionActionPossible(Record record, User user) {
		return userServices.canAddOrModifyUserAndGroup()
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS)
			   && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SECURITY);
	}
}
