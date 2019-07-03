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
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.DISABLED;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.HIDDEN;


public class MenuItemServices {

	private SearchServices searchServices;
	private List<MenuItemActionsExtension> menuItemActionsExtensions;

	private UserCredentialMenuItemServices userCredentialMenuItemServices;
	private GlobalGroupMenuItemServices globalGroupMenuItemServices;

	public MenuItemServices(String collection, AppLayerFactory appLayerFactory) {
		searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();

		menuItemActionsExtensions = appLayerFactory.getExtensions()
				.forCollection(collection).menuItemActionsExtensions.getExtensions();

		this.userCredentialMenuItemServices = new UserCredentialMenuItemServices(appLayerFactory);
		this.globalGroupMenuItemServices = new GlobalGroupMenuItemServices(appLayerFactory);
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
						new ArrayList<>(), params));
			} else if (objectRecordVO instanceof GlobalGroupVO) {
				menuItemActions.addAll(globalGroupMenuItemServices.getActionsForRecord(globalGroupMenuItemServices
								.getGlobalGroup((GlobalGroupVO) objectRecordVO), params.getUser(),
						new ArrayList<>(), params));
			}
		}
		if (record != null) {
			addMenuItemActionsFromExtensions(record, filteredActionTypes, params, menuItemActions);
		}

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

		List<MenuItemAction> menuItemActions = new ArrayList<>();

		List<String> actionTypes = getMenuItemActionTypesForRecordList(filteredActionTypes);
		if (!actionTypes.isEmpty()) {
			Map<String, MenuItemActionState> actionStateByType = new HashMap<>();

			SearchResponseIterator<List<Record>> recordsIterator = searchServices.recordsIterator(query).inBatches();
			while (recordsIterator.hasNext()) {
				List<Record> records = recordsIterator.next();

				for (String actionType : actionTypes) {
					MenuItemActionState previousState = actionStateByType.get(actionType);
					if (previousState != null && previousState.getStatus() == DISABLED) {
						continue;
					}

					MenuItemActionState state = computeActionStateForRecords(actionType, records, params, previousState);
					if (actionStateByType.containsKey(actionType)) {
						actionStateByType.put(actionType, state);
					}
				}
			}

			for (String actionType : actionStateByType.keySet()) {
				addMenuItemAction(actionType, actionStateByType.get(actionType), menuItemActions);
			}
		}

		addMenuItemActionsFromExtensions(query, filteredActionTypes, params, menuItemActions);

		return menuItemActions;
	}

	public MenuItemActionState getStateForAction(String actionType, Record record,
												 MenuItemActionBehaviorParams params) {
		if (record.isOfSchemaType(User.SCHEMA_TYPE)) {
			// TODO
		} else if (record.isOfSchemaType(Group.SCHEMA_TYPE)) {
			// TODO
		} else {
			// TODO
		}

		return geStateForActionFromExtensions(actionType, record, params);
	}

	public MenuItemActionState getStateForAction(String actionType, List<Record> records,
												 MenuItemActionBehaviorParams params) {

		MenuItemActionState state = computeActionStateForRecords(actionType, records, params, null);
		if (state != null) {
			return state;
		}

		return geStateForActionFromExtensions(actionType, records, params);
	}

	public MenuItemActionState getStateForAction(String actionType, LogicalSearchQuery query,
												 MenuItemActionBehaviorParams params) {
		MenuItemActionState state = null;

		SearchResponseIterator<List<Record>> recordsIterator = searchServices.recordsIterator(query).inBatches();
		while (recordsIterator.hasNext()) {
			List<Record> records = recordsIterator.next();
			state = computeActionStateForRecords(actionType, records, params, state);
		}

		if (state != null) {
			return state;
		}

		return geStateForActionFromExtensions(actionType, query, params);
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

	private MenuItemActionState geStateForActionFromExtensions(String actionType, Record record,
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

	private MenuItemActionState geStateForActionFromExtensions(String actionType, List<Record> records,
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

	private MenuItemActionState geStateForActionFromExtensions(String actionType, LogicalSearchQuery query,
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

	private void addMenuItemAction(String actionType, MenuItemActionState state,
								   List<MenuItemAction> menuItemActions) {
	}

	private MenuItemActionState computeActionStateForRecords(String actionType,
															 List<Record> records,
															 MenuItemActionBehaviorParams params,
															 MenuItemActionState state) {
		return null;
	}

}
