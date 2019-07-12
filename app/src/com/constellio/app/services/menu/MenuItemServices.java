package com.constellio.app.services.menu;

import com.constellio.app.extensions.menu.MenuItemActionsExtension;
import com.constellio.app.extensions.menu.MenuItemActionsExtension.MenuItemActionExtensionAddMenuItemActionsForQueryParams;
import com.constellio.app.extensions.menu.MenuItemActionsExtension.MenuItemActionExtensionAddMenuItemActionsForRecordParams;
import com.constellio.app.extensions.menu.MenuItemActionsExtension.MenuItemActionExtensionAddMenuItemActionsForRecordsParams;
import com.constellio.app.extensions.menu.MenuItemActionsExtension.MenuItemActionExtensionGetActionStateForQueryParams;
import com.constellio.app.extensions.menu.MenuItemActionsExtension.MenuItemActionExtensionGetActionStateForRecordParams;
import com.constellio.app.extensions.menu.MenuItemActionsExtension.MenuItemActionExtensionGetActionStateForRecordsParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.HIDDEN;

public class MenuItemServices {

	private List<MenuItemActionsExtension> menuItemActionsExtensions;

	private UserCredentialMenuItemServices userCredentialMenuItemServices;
	private GlobalGroupMenuItemServices globalGroupMenuItemServices;
	private SchemaRecordMenuItemServices schemaRecordMenuItemServices;

	private SchemasRecordsServices schemasRecordsServices;

	public MenuItemServices(String collection, AppLayerFactory appLayerFactory) {
		this.menuItemActionsExtensions = appLayerFactory.getExtensions()
				.forCollection(collection).menuItemActionsExtensions.getExtensions();

		this.schemasRecordsServices = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());

		this.userCredentialMenuItemServices = new UserCredentialMenuItemServices(appLayerFactory);
		this.globalGroupMenuItemServices = new GlobalGroupMenuItemServices(appLayerFactory);
		this.schemaRecordMenuItemServices = new SchemaRecordMenuItemServices(collection, appLayerFactory);
	}

	public List<MenuItemAction> getActionsForRecord(Record record, MenuItemActionBehaviorParams params) {
		return getActionsForRecord(record, Collections.emptyList(), params);
	}

	public List<MenuItemAction> getActionsForRecord(Record record, List<String> filteredActionTypes,
													MenuItemActionBehaviorParams params) {
		if (params.getView() == null) {
			return Collections.emptyList();
		}

		List<MenuItemAction> menuItemActions = new ArrayList<>();
		Object objectRecordVO = params.getObjectRecordVO();
		if (objectRecordVO != null) {
			if (objectRecordVO instanceof UserCredentialVO) {
				menuItemActions.addAll(userCredentialMenuItemServices.getActionsForRecord(userCredentialMenuItemServices
								.getUserCredential((UserCredentialVO) objectRecordVO), params.getUser(),
						filteredActionTypes, params));
			} else if (objectRecordVO instanceof GlobalGroupVO) {
				menuItemActions.addAll(globalGroupMenuItemServices.getActionsForRecord(globalGroupMenuItemServices
								.getGlobalGroup((GlobalGroupVO) objectRecordVO), params.getUser(),
						filteredActionTypes, params));
			}
		}

		if (record.getSchemaCode().startsWith("ddv")) {
			menuItemActions.addAll(schemaRecordMenuItemServices.getActionsForRecord(record, params.getUser(), filteredActionTypes, params));
		}


		addMenuItemActionsFromExtensions(record, filteredActionTypes, params, menuItemActions);

		return menuItemActions;
	}

	public List<MenuItemAction> getActionsForRecords(List<Record> records, MenuItemActionBehaviorParams params) {
		return getActionsForRecords(records, Collections.emptyList(), params);
	}

	public List<MenuItemAction> getActionsForRecords(List<Record> records, List<String> filteredActionTypes,
													 MenuItemActionBehaviorParams params) {
		if (params.getView() == null) {
			return Collections.emptyList();
		}

		List<MenuItemAction> menuItemActions = new ArrayList<>();

		for (String actionType : getMenuItemActionTypesForRecordList(filteredActionTypes)) {
			MenuItemActionState state = getStateForAction(actionType, records, params);
			addMenuItemAction(actionType, state, menuItemActions);
		}

		addMenuItemActionsFromExtensions(records, filteredActionTypes, params, menuItemActions);

		return menuItemActions;
	}

	public List<MenuItemAction> getActionsForRecords(LogicalSearchQuery query, MenuItemActionBehaviorParams params) {
		return getActionsForRecords(query, Collections.emptyList(), params);
	}


	public List<MenuItemAction> getActionsForRecords(LogicalSearchQuery query, List<String> filteredActionTypes,
													 MenuItemActionBehaviorParams params) {
		if (params.getView() == null) {
			return Collections.emptyList();
		}

		// only used by extensions for now

		List<MenuItemAction> menuItemActions = new ArrayList<>();
		addMenuItemActionsFromExtensions(query, filteredActionTypes, params, menuItemActions);

		return menuItemActions;
	}

	public MenuItemActionState getStateForAction(String actionType, Record record,
												 MenuItemActionBehaviorParams params) {

		if (record.isOfSchemaType(User.SCHEMA_TYPE)) {
			return MenuItemActionState.visibleOrHidden(userCredentialMenuItemServices.isMenuItemActionPossible(actionType,
					schemasRecordsServices.wrapUserCredential(record), params.getUser(), params));
		} else if (record.isOfSchemaType(Group.SCHEMA_TYPE)) {
			return MenuItemActionState.visibleOrHidden(globalGroupMenuItemServices.isMenuItemActionPossible(actionType,
					schemasRecordsServices.wrapGlobalGroup(record), params.getUser(), params));
		} else if (record.getSchemaCode().startsWith("ddv")) {
			return MenuItemActionState.visibleOrHidden(schemaRecordMenuItemServices.isMenuItemActionPossible(actionType,
					record, params.getUser(), params));
		}

		return getStateForActionFromExtensions(actionType, record, params);
	}

	public MenuItemActionState getStateForAction(String actionType, List<Record> records,
												 MenuItemActionBehaviorParams params) {

		MenuItemActionState state = computeActionStateForRecords(actionType, records, params);
		if (state != null) {
			return state;
		}

		return getStateForActionFromExtensions(actionType, records, params);
	}

	public MenuItemActionState getStateForAction(String actionType, LogicalSearchQuery query,
												 MenuItemActionBehaviorParams params) {
		// only used by extensions for now

		return getStateForActionFromExtensions(actionType, query, params);
	}

	private void addMenuItemActionsFromExtensions(Record record, List<String> filteredActionTypes,
												  MenuItemActionBehaviorParams params,
												  List<MenuItemAction> menuItemActions) {
		for (MenuItemActionsExtension menuItemActionsExtension : menuItemActionsExtensions) {
			menuItemActionsExtension.addMenuItemActionsForRecord(
					new MenuItemActionExtensionAddMenuItemActionsForRecordParams(record, menuItemActions,
							filteredActionTypes, params));
		}
	}

	private void addMenuItemActionsFromExtensions(List<Record> records, List<String> filteredActionTypes,
												  MenuItemActionBehaviorParams params,
												  List<MenuItemAction> menuItemActions) {
		for (MenuItemActionsExtension menuItemActionsExtension : menuItemActionsExtensions) {
			menuItemActionsExtension.addMenuItemActionsForRecords(
					new MenuItemActionExtensionAddMenuItemActionsForRecordsParams(records, menuItemActions,
							filteredActionTypes, params));
		}
	}

	private void addMenuItemActionsFromExtensions(LogicalSearchQuery query, List<String> filteredActionTypes,
												  MenuItemActionBehaviorParams params,
												  List<MenuItemAction> menuItemActions) {
		for (MenuItemActionsExtension menuItemActionsExtension : menuItemActionsExtensions) {
			menuItemActionsExtension.addMenuItemActionsForQuery(
					new MenuItemActionExtensionAddMenuItemActionsForQueryParams(query, menuItemActions,
							filteredActionTypes, params));
		}
	}

	private MenuItemActionState getStateForActionFromExtensions(String actionType, Record record,
																MenuItemActionBehaviorParams behaviorParams) {
		MenuItemActionState state;
		for (MenuItemActionsExtension menuItemActionsExtension : menuItemActionsExtensions) {
			state = menuItemActionsExtension.getActionStateForRecord(
					new MenuItemActionExtensionGetActionStateForRecordParams(record, actionType, behaviorParams));
			if (state != null) {
				return state;
			}
		}
		return new MenuItemActionState(HIDDEN);
	}

	private MenuItemActionState getStateForActionFromExtensions(String actionType, List<Record> records,
																MenuItemActionBehaviorParams behaviorParams) {
		MenuItemActionState state;
		for (MenuItemActionsExtension menuItemActionsExtension : menuItemActionsExtensions) {
			state = menuItemActionsExtension.getActionStateForRecords(
					new MenuItemActionExtensionGetActionStateForRecordsParams(records, actionType, behaviorParams));
			if (state != null) {
				return state;
			}
		}
		return new MenuItemActionState(HIDDEN);
	}

	private MenuItemActionState getStateForActionFromExtensions(String actionType, LogicalSearchQuery query,
																MenuItemActionBehaviorParams behaviorParams) {
		MenuItemActionState state;
		for (MenuItemActionsExtension menuItemActionsExtension : menuItemActionsExtensions) {
			state = menuItemActionsExtension.getActionStateForQuery(
					new MenuItemActionExtensionGetActionStateForQueryParams(query, actionType, behaviorParams));
			if (state != null) {
				return state;
			}
		}
		return new MenuItemActionState(HIDDEN);
	}

	private List<String> getMenuItemActionTypesForRecordList(List<String> filteredActionTypes) {
		return Collections.emptyList();
	}

	private void addMenuItemAction(String actionType, MenuItemActionState state, List<MenuItemAction> menuItemActions) {
		// nothing to add for now
	}

	private MenuItemActionState computeActionStateForRecords(String actionType, List<Record> records,
															 MenuItemActionBehaviorParams params) {
		return null;
	}

}
