package com.constellio.app.extensions.menu;

import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.model.entities.records.Record;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public abstract class MenuItemActionsExtension {

	public abstract void addMenuItemActions(MenuItemActionExtensionAddMenuItemActionsParams params);

	public abstract MenuItemActionState getStateForAction(MenuItemActionExtensionGetStateForActionParams params);

	@AllArgsConstructor
	@Getter
	public static class MenuItemActionExtensionAddMenuItemActionsParams {
		private Record record;
		private List<MenuItemAction> menuItemActions;
		private List<String> filteredActionTypes;
		private MenuItemActionBehaviorParams behaviorParams;
	}

	@AllArgsConstructor
	@Getter
	public static class MenuItemActionExtensionGetStateForActionParams {
		private Record record;
		private MenuItemAction menuItemAction;
		private MenuItemActionBehaviorParams behaviorParams;
	}

}
