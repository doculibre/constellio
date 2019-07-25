package com.constellio.app.services.action;

import com.constellio.app.extensions.AppLayerSystemExtensions;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.users.UserServices;

public class UserCredentialActionsServices {
	private SchemasRecordsServices core;
	private AppLayerSystemExtensions appLayerSystemExtensions;
	private ModelLayerFactory modelLayerFactory;
	private UserServices userServices;

	public UserCredentialActionsServices(AppLayerFactory appLayerFactory) {
		this.core = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, appLayerFactory.getModelLayerFactory());
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.appLayerSystemExtensions = appLayerFactory.getExtensions().getSystemWideExtensions();
		this.userServices = modelLayerFactory.newUserServices();
	}

	public boolean isEditActionPossible(Record record, User user) {
		return userServices.canAddOrModifyUserAndGroup() && userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS)
			   && appLayerSystemExtensions.isEditActionPossibleOnUserCredential(core.wrapUserCredential(record), user);
	}

	public boolean isGenerateTokenActionPossibe(Record record, User user) {
		return userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS)
			   && appLayerSystemExtensions.isGenerateTokenActionPossibleOnUserCredential(core.wrapUserCredential(record), user);
	}
}
