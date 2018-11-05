package com.constellio.app.utils.scripts;

import com.constellio.app.extensions.api.scripts.ScriptWithLogOutput;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.SolrGlobalGroup;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.users.GlobalGroupsManager;
import com.constellio.model.services.users.UserServices;

import java.util.ArrayList;
import java.util.List;

public class DeleteInactiveGroupsScript extends ScriptWithLogOutput {

	private GlobalGroupsManager globalGroupsManager;
	private UserServices userServices;
	private AuthorizationsServices authorizationsServices;

	public DeleteInactiveGroupsScript(AppLayerFactory appLayerFactory) {
		super(appLayerFactory, "Groups", "Delete inactive groups");

		globalGroupsManager = modelLayerFactory.getGlobalGroupsManager();
		userServices = modelLayerFactory.newUserServices();
		authorizationsServices = modelLayerFactory.newAuthorizationsServices();
	}

	@Override
	protected void execute() throws Exception {
		List<String> collections = modelLayerFactory.getCollectionsListManager().getCollections();

		List<GlobalGroup> globalGroups = globalGroupsManager.getAllGroups();
		for (GlobalGroup globalGroup : globalGroups) {
			if (globalGroup.isLocallyCreated()) {
				continue;
			}

			boolean used = false;
			for (String collection : collections) {
				Group group = userServices.getGroupInCollection(globalGroup.getCode(), collection);
				if (group == null) {
					continue;
				}

				if (!group.getAllAuthorizations().isEmpty()) {
					used = true;
					break;
				}
			}

			if (!used) {
				deleteInactiveGlobalGroup(globalGroup, collections);
			}
		}
	}

	private void deleteInactiveGlobalGroup(GlobalGroup globalGroup, List<String> collections)
			throws RecordServicesException {

		for (String collection : collections) {
			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

			Group group = userServices.getGroupInCollection(globalGroup.getCode(), collection);
			if (group == null) {
				continue;
			}
			List<User> users = rm.wrapUsers(authorizationsServices.getUserRecordsInGroup(group.getWrappedRecord()));
			for (User user : users) {
				List<String> userGroups = new ArrayList<>(user.getUserGroups());
				userGroups.remove(group.getId());

				recordServices.update(user.setUserGroups(!userGroups.isEmpty() ? userGroups : null));
			}

			recordServices.logicallyDelete(group.getWrappedRecord(), User.GOD);
			recordServices.physicallyDelete(group.getWrappedRecord(), User.GOD);
		}

		globalGroupsManager.logicallyRemoveGroup(globalGroup);
		recordServices.physicallyDelete(((SolrGlobalGroup) globalGroup).getWrappedRecord(), User.GOD);

		outputLogger.info(String.format("Global group '%s' deleted", globalGroup.getCode()));
	}
}
