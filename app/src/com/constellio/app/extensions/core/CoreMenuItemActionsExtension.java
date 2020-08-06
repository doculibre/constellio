package com.constellio.app.extensions.core;

import com.constellio.app.extensions.menu.MenuItemActionsExtension;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.GroupCollectionMenuItemServices;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.UserCollectionMenuItemServices;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.SchemasRecordsServices;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CoreMenuItemActionsExtension extends MenuItemActionsExtension {
	private AppLayerFactory appLayerFactory;
	private GroupCollectionMenuItemServices groupCollectionMenuItemServices;
	private UserCollectionMenuItemServices userCollectionMenuItemServices;
	private SchemasRecordsServices core;

	public CoreMenuItemActionsExtension(String collection, AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.userCollectionMenuItemServices = new UserCollectionMenuItemServices(collection, appLayerFactory);
		this.groupCollectionMenuItemServices = new GroupCollectionMenuItemServices(collection, appLayerFactory);
		this.core = new SchemasRecordsServices(collection, this.appLayerFactory.getModelLayerFactory());
	}

	@Override
	public void addMenuItemActionsForRecords(MenuItemActionExtensionAddMenuItemActionsForRecordsParams params) {
		List<Record> records = params.getRecords();
		User user = params.getBehaviorParams().getUser();
		List<MenuItemAction> menuItemActions = params.getMenuItemActions();
		List<String> excludedActionTypes = params.getExcludedActionTypes();
		MenuItemActionBehaviorParams behaviorParams = params.getBehaviorParams();


		if (records.size() > 0 && records.get(0).isOfSchemaType(User.SCHEMA_TYPE)) {
			menuItemActions.addAll(userCollectionMenuItemServices.getActionsForRecords(records.stream().map(x -> core.wrapUser(x)).collect(Collectors.toList()), user,
					excludedActionTypes, behaviorParams));
		}
		if (records.size() > 0 && records.get(0).isOfSchemaType(Group.SCHEMA_TYPE)) {
			menuItemActions.addAll(groupCollectionMenuItemServices.getActionsForRecords(records.stream().map(x -> core.wrapGroup(x)).collect(Collectors.toList()), user,
					excludedActionTypes, behaviorParams));
		}
	}

	@Override
	public void addMenuItemActionsForRecord(MenuItemActionExtensionAddMenuItemActionsForRecordParams params) {
		if (params.getRecord() != null) {
			if (params.getRecord().isOfSchemaType(User.SCHEMA_TYPE)) {
				params.getMenuItemActions().addAll(userCollectionMenuItemServices.getActionsForRecords(Arrays.asList(core.wrapUser(params.getRecord())), params.getBehaviorParams().getUser(),
						params.getExcludedActionTypes(), params.getBehaviorParams()));
			} else if (params.getRecord().isOfSchemaType(Group.SCHEMA_TYPE)) {
				params.getMenuItemActions().addAll(groupCollectionMenuItemServices.getActionsForRecords(Arrays.asList(core.wrapGroup(params.getRecord())), params.getBehaviorParams().getUser(),
						params.getExcludedActionTypes(), params.getBehaviorParams()));
			}
		}
	}
}
