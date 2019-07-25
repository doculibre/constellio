package com.constellio.app.services.menu;

import com.constellio.app.services.action.GlobalGroupActionsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.GlobalGroupMenuItemActionBehaviors;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.constellio.app.services.menu.GlobalGroupMenuItemServices.GlobalGroupMenuItemActionType.GROUP_ADD_SUB_GROUP;
import static com.constellio.app.services.menu.GlobalGroupMenuItemServices.GlobalGroupMenuItemActionType.GROUP_DELETE;
import static com.constellio.app.services.menu.GlobalGroupMenuItemServices.GlobalGroupMenuItemActionType.GROUP_EDIT;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.HIDDEN;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.VISIBLE;
import static com.constellio.app.ui.i18n.i18n.$;


public class GlobalGroupMenuItemServices {
	private AppLayerFactory appLayerFactory;
	private GlobalGroupActionsServices globalGroupActionsServices;
	private SchemasRecordsServices core;

	public GlobalGroupMenuItemServices(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.globalGroupActionsServices = new GlobalGroupActionsServices(appLayerFactory);
		this.core = new SchemasRecordsServices(null, appLayerFactory.getModelLayerFactory());
	}

	public List<MenuItemAction> getActionsForRecord(GlobalGroup globalGroup, User user,
													List<String> filteredActionTypes,
													MenuItemActionBehaviorParams params) {
		List<MenuItemAction> menuItemActions = new ArrayList<>();

		if (!filteredActionTypes.contains(GROUP_ADD_SUB_GROUP.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(GROUP_ADD_SUB_GROUP.name(),
					isMenuItemActionPossible(GROUP_ADD_SUB_GROUP.name(), globalGroup, user, params),
					$("DisplayGlobalGroupView.addSubGroup"), null, -1, 100,
					(ids) -> new GlobalGroupMenuItemActionBehaviors(appLayerFactory).groupAddSubGroup(params));
			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(GROUP_EDIT.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(GROUP_EDIT.name(),
					isMenuItemActionPossible(GROUP_EDIT.name(), globalGroup, user, params),
					$("edit"), FontAwesome.EDIT, -1, 200,
					(ids) -> new GlobalGroupMenuItemActionBehaviors(appLayerFactory).edit(params));
			menuItemActions.add(menuItemAction);
		}


		if (!filteredActionTypes.contains(GROUP_DELETE.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(GROUP_DELETE.name(),
					isMenuItemActionPossible(GROUP_DELETE.name(), globalGroup, user, params),
					$("delete"), FontAwesome.TRASH_O, -1, 300,
					(ids) -> new GlobalGroupMenuItemActionBehaviors(appLayerFactory).delete(params));

			menuItemAction.setConfirmMessage($("ConfirmDialog.confirmDelete"));

			menuItemActions.add(menuItemAction);
		}

		return menuItemActions;
	}

	public boolean isMenuItemActionPossible(String menuItemActionType, GlobalGroup globalGroup, User user,
											MenuItemActionBehaviorParams params) {
		Record record = globalGroup.getWrappedRecord();

		switch (GlobalGroupMenuItemActionType.valueOf(menuItemActionType)) {
			case GROUP_ADD_SUB_GROUP:
				return globalGroupActionsServices.isAddSubGroupActionPossible(record, user);
			case GROUP_EDIT:
				return globalGroupActionsServices.isEditActionPossible(record, user);
			case GROUP_DELETE:
				return globalGroupActionsServices.isDeleteActionPossible(record, user);
			default:
				throw new RuntimeException("Unknown MenuItemActionType : " + menuItemActionType);
		}
	}

	public GlobalGroup getGlobalGroup(GlobalGroupVO globalGroupVO) {
		return core.getGlobalGroupWithCode(globalGroupVO.getCode());
	}

	private MenuItemAction buildMenuItemAction(String type, boolean possible, String caption, Resource icon,
											   int group, int priority, Consumer<List<String>> command) {
		return MenuItemAction.builder()
				.type(type)
				.state(possible ? new MenuItemActionState(VISIBLE) : new MenuItemActionState(HIDDEN))
				.caption(caption)
				.icon(icon)
				.group(group)
				.priority(priority)
				.command(command)
				.recordsLimit(1)
				.build();
	}

	enum GlobalGroupMenuItemActionType {
		GROUP_ADD_SUB_GROUP,
		GROUP_EDIT,
		GROUP_DELETE,
	}
}
