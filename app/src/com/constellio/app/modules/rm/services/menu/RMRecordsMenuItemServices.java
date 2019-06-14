package com.constellio.app.modules.rm.services.menu;

import com.constellio.app.modules.rm.services.menu.behaviors.RMRecordsMenuItemBehaviors;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.server.Resource;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_ADD_CART;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.DISABLED;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.HIDDEN;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class RMRecordsMenuItemServices {

	private String collection;
	private AppLayerFactory appLayerFactory;

	private SearchServices searchServices;

	public RMRecordsMenuItemServices(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;

		searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
	}

	public List<MenuItemAction> getActionsForRecords(List<Record> records, User user, List<String> filteredActionTypes,
													 MenuItemActionBehaviorParams params) {
		List<MenuItemAction> menuItemActions = new ArrayList<>();

		List<RMRecordsMenuItemActionType> actionTypes = getRMRecordsMenuItemActionTypes(filteredActionTypes);
		for (RMRecordsMenuItemActionType actionType : actionTypes) {
			MenuItemActionState state = getMenuItemActionStateForRecords(actionType, records, user, params);
			addMenuItemAction(actionType, state, params, menuItemActions);
		}

		return menuItemActions;
	}

	public List<MenuItemAction> getActionsForQuery(LogicalSearchQuery query, User user,
												   List<String> filteredActionTypes,
												   MenuItemActionBehaviorParams params) {
		List<MenuItemAction> menuItemActions = new ArrayList<>();

		List<RMRecordsMenuItemActionType> actionTypes = getRMRecordsMenuItemActionTypes(filteredActionTypes);
		if (!actionTypes.isEmpty()) {
			Map<RMRecordsMenuItemActionType, MenuItemActionState> actionStateByType = new HashMap<>();

			SearchResponseIterator<List<Record>> recordsIterator = searchServices.recordsIterator(query).inBatches();
			while (recordsIterator.hasNext()) {
				List<Record> records = recordsIterator.next();

				Iterator<RMRecordsMenuItemActionType> actionTypesIterator = actionTypes.iterator();
				while (actionTypesIterator.hasNext()) {
					RMRecordsMenuItemActionType actionType = actionTypesIterator.next();
					MenuItemActionState previousState = actionStateByType.get(actionType);

					MenuItemActionState state = computeActionState(actionType, records, user, params, previousState);
					if (actionStateByType.containsKey(actionType)) {
						actionStateByType.put(actionType, state);
					}

					if (state.getStatus() == DISABLED) {
						actionTypesIterator.remove();
					}
				}
			}

			for (RMRecordsMenuItemActionType actionType : actionStateByType.keySet()) {
				addMenuItemAction(actionType, actionStateByType.get(actionType), params, menuItemActions);
			}
		}

		return menuItemActions;
	}

	public MenuItemActionState getMenuItemActionStateForRecords(RMRecordsMenuItemActionType menuItemActionType,
																List<Record> records, User user,
																MenuItemActionBehaviorParams params) {
		return computeActionState(menuItemActionType, records, user, params, null);
	}

	public MenuItemActionState getMenuItemActionStateForQuery(RMRecordsMenuItemActionType actionType,
															  LogicalSearchQuery query, User user,
															  MenuItemActionBehaviorParams params) {
		MenuItemActionState state = null;

		SearchResponseIterator<List<Record>> recordsIterator = searchServices.recordsIterator(query).inBatches();
		while (recordsIterator.hasNext()) {
			List<Record> records = recordsIterator.next();

			state = computeActionState(actionType, records, user, params, state);

			if (state.getStatus() == DISABLED) {
				return state;
			}
		}
		return state;
	}

	public MenuItemActionState computeActionState(RMRecordsMenuItemActionType menuItemActionType, List<Record> records,
												  User user, MenuItemActionBehaviorParams params,
												  MenuItemActionState previousState) {
		long recordWithSupportedSchemaTypeCount = getRecordWithSupportedSchemaTypeCount(records, menuItemActionType);
		if (recordWithSupportedSchemaTypeCount == 0) {
			return new MenuItemActionState(HIDDEN);
		} else if (recordWithSupportedSchemaTypeCount != records.size()) {
			return new MenuItemActionState(DISABLED, "TODO : only following schemas supported");
		}

		switch (menuItemActionType) {
			case RMRECORDS_ADD_CART:
				for (Record record : records) {
					// TODO
					// if old is VISIBLE and 1 not possible then DISABLED with reason
					// if old is HIDDEN and 1 is possible then DISABLED with reason from old
					// if old is empty and all possible then VISIBLE
					// if old is empty and all not possible then HIDDEN with reason
					// if 1 possible and 1 not possible then DISABLED with reason
				}
				return new MenuItemActionState(DISABLED, "TODO : missing permission or whatever");
			case RMRECORDS_MOVE:
				// TODO
			case RMRECORDS_COPY:
				// TODO
			case RMRECORDS_GENERATE_REPORT:
				// TODO
			case RMRECORDS_SIP_ARCHIVE:
				// TODO
			case RMRECORDS_SEND_EMAIL:
				// TODO
			case RMRECORDS_CREATE_PDF:
				// TODO
			case RMRECORDS_BATCH:
				// TODO
			case RMRECORDS_LABEL:
				// TODO
			case RMRECORDS_ADD_SELECTION:
				// TODO
			case RMRECORDS_DOWNLOAD_ZIP:
				// TODO
		}

		return new MenuItemActionState(HIDDEN);
	}

	private void addMenuItemAction(RMRecordsMenuItemActionType actionType, MenuItemActionState state,
								   MenuItemActionBehaviorParams params, List<MenuItemAction> menuItemActions) {
		MenuItemAction menuItemAction = null;

		switch (actionType) {
			case RMRECORDS_ADD_CART:
				menuItemAction = buildMenuItemAction(RMRECORDS_ADD_CART, state, "TODO", null, -1, 100,
						() -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).todo(params));
				break;
			case RMRECORDS_MOVE:
				// TODO
			case RMRECORDS_COPY:
				// TODO
			case RMRECORDS_GENERATE_REPORT:
				// TODO
			case RMRECORDS_SIP_ARCHIVE:
				// TODO
			case RMRECORDS_SEND_EMAIL:
				// TODO
			case RMRECORDS_CREATE_PDF:
				// TODO
			case RMRECORDS_BATCH:
				// TODO
			case RMRECORDS_LABEL:
				// TODO
			case RMRECORDS_ADD_SELECTION:
				// TODO
			case RMRECORDS_DOWNLOAD_ZIP:
				// TODO
		}

		menuItemActions.add(menuItemAction);
	}

	private String getSchemaType(Record record) {
		return record.getSchemaCode().substring(0, record.getSchemaCode().indexOf("_"));
	}

	private long getRecordWithSupportedSchemaTypeCount(List<Record> records, RMRecordsMenuItemActionType type) {
		return records.stream()
				.filter(r -> type.getSchemaTypes().contains(getSchemaType(r)))
				.count();
	}

	private MenuItemAction buildMenuItemAction(RMRecordsMenuItemActionType type, MenuItemActionState state,
											   String caption, Resource icon, int group, int priority,
											   Runnable command) {
		return MenuItemAction.builder()
				.type(type.name())
				.state(state)
				.caption(caption)
				.icon(icon)
				.group(group)
				.priority(priority)
				.command(command)
				.build();
	}

	private List<RMRecordsMenuItemActionType> getRMRecordsMenuItemActionTypes(List<String> filteredActionTypes) {
		return Arrays.stream(RMRecordsMenuItemActionType.values())
				.filter(t -> !filteredActionTypes.contains(t.name()))
				.collect(Collectors.toList());
	}

	@AllArgsConstructor
	@Getter
	public enum RMRecordsMenuItemActionType {
		RMRECORDS_ADD_CART(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE)),
		RMRECORDS_MOVE(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE)),
		RMRECORDS_COPY(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE)),
		RMRECORDS_GENERATE_REPORT(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE)),
		RMRECORDS_SIP_ARCHIVE(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE)),
		RMRECORDS_SEND_EMAIL(singletonList(Document.SCHEMA_TYPE)),
		RMRECORDS_CREATE_PDF(singletonList(Document.SCHEMA_TYPE)),
		RMRECORDS_BATCH(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE)),
		RMRECORDS_LABEL(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE)),
		RMRECORDS_ADD_SELECTION(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE)),
		RMRECORDS_DOWNLOAD_ZIP(singletonList(Document.SCHEMA_TYPE));

		private final List<String> schemaTypes;

		public static boolean contains(String typeAsString) {
			for (RMRecordsMenuItemActionType type : RMRecordsMenuItemActionType.values()) {
				if (type.name().equals(typeAsString)) {
					return true;
				}
			}
			return false;
		}
	}
}
