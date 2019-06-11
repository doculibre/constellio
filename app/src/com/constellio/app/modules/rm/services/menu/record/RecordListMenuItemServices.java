package com.constellio.app.modules.rm.services.menu.record;

import com.constellio.app.modules.rm.services.menu.MenuItemAction;
import com.constellio.app.modules.rm.services.menu.MenuItemActionState;
import com.constellio.app.modules.rm.services.menu.MenuItemActionType;
import com.constellio.app.modules.rm.services.menu.behaviors.DocumentMenuItemActionBehaviors;
import com.constellio.app.modules.rm.services.menu.behaviors.MenuItemActionBehaviorParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.MULTIPLE_ADD_CART;

public class RecordListMenuItemServices {

	private String collection;
	private AppLayerFactory appLayerFactory;

	public RecordListMenuItemServices(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	public List<MenuItemAction> getActionsForRecords(List<Record> records, User user, List<String> filteredActionTypes,
													 MenuItemActionBehaviorParams params) {
		List<MenuItemAction> menuItemActions = new ArrayList<>();

		if (!filteredActionTypes.contains(MULTIPLE_ADD_CART.name())) {
			menuItemActions.add(buildMenuItemAction(MULTIPLE_ADD_CART,
					getMenuItemActionState(MULTIPLE_ADD_CART, records, user, params),
					"TODO", FontAwesome.FILE_O, -1, 100,
					() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).display(params)));
		}

		// TODO extensions

		return menuItemActions;
	}

	public MenuItemActionState getMenuItemActionState(MenuItemActionType menuItemActionType, List<Record> records,
													  User user, MenuItemActionBehaviorParams params) {
		long recordWithSupportedSchemaTypeCount = getRecordWithSupportedSchemaTypeCount(records, menuItemActionType);
		if (recordWithSupportedSchemaTypeCount != records.size()) {
			return MenuItemActionState.HIDDEN;
		}

		long recordsWithActionPossibleCount =
				getRecordsWithMenuItemActionPossibleCount(menuItemActionType, records, user, params);
		if (recordsWithActionPossibleCount == 0) {
			return MenuItemActionState.HIDDEN;
		}

		return recordsWithActionPossibleCount == recordWithSupportedSchemaTypeCount ?
			   MenuItemActionState.VISIBLE : MenuItemActionState.DISABLED;
	}

	private String getSchemaType(Record record) {
		return record.getSchemaCode().substring(0, record.getSchemaCode().indexOf("_"));
	}

	private Set<String> getSchemaTypes(List<Record> records) {
		return records.stream().map(this::getSchemaType).collect(Collectors.toSet());
	}

	private long getRecordWithSupportedSchemaTypeCount(List<Record> records, MenuItemActionType type) {
		return records.stream()
				.filter(r -> type.getSchemaTypes().isEmpty() || type.getSchemaTypes().contains(getSchemaType(r)))
				.count();
	}

	private long getRecordsWithMenuItemActionPossibleCount(MenuItemActionType menuItemActionType, List<Record> records,
														   User user,
														   MenuItemActionBehaviorParams params) {
		return records.stream().filter(r -> isMenuItemActionPossible(menuItemActionType, r, user, params)).count();
	}

	private boolean isMenuItemActionPossible(MenuItemActionType menuItemActionType, Record record, User user,
											 MenuItemActionBehaviorParams params) {
		SessionContext sessionContext = params.getView().getSessionContext();

		switch (menuItemActionType) {
			case MULTIPLE_ADD_CART:
				// TODO
			case MULTIPLE_MOVE:
				// TODO
			case MULTIPLE_COPY:
				// TODO
			case MULTIPLE_GENERATE_REPORT:
				// TODO
			case MULTIPLE_SIP_ARCHIVE:
				// TODO
			case MULTIPLE_SEND_EMAIL:
				// TODO
			case MULTIPLE_CREATE_PDF:
				// TODO
			case MULTIPLE_LABEL:
				// TODO
			case MULTIPLE_ADD_SELECTION:
				// TODO
			case MULTIPLE_DOWNLOAD:
				// TODO
			case MULTIPLE_BATCH:
				// TODO
			default:
				throw new RuntimeException("Unknown MenuItemActionType : " + menuItemActionType);
		}
	}

	private MenuItemAction buildMenuItemAction(MenuItemActionType type, MenuItemActionState state, String caption,
											   Resource icon, int group, int priority, Runnable command) {
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
}
