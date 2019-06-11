package com.constellio.app.extensions.menu;

import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.model.entities.records.Record;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public abstract class MenuItemActionsExtension {

	public void addMenuItemActionsForRecord(MenuItemActionExtensionAddMenuItemActionsForRecordParams params) {
	}

	public void addMenuItemActionsForRecords(MenuItemActionExtensionAddMenuItemActionsForRecordsParams params) {
	}

	public MenuItemActionState getActionStateForRecord(MenuItemActionExtensionGetActionStateForRecordParams params) {
		return null;
	}

	public MenuItemActionState getActionStateForRecords(MenuItemActionExtensionGetActionStateForRecordsParams params) {
		return null;
	}

	@AllArgsConstructor
	@Getter
	public static class MenuItemActionExtensionAddMenuItemActionsForRecordParams {
		private Record record;
		private List<MenuItemAction> menuItemActions;
		private List<String> filteredActionTypes;
		private MenuItemActionBehaviorParams behaviorParams;
	}

	@AllArgsConstructor
	@Getter
	public static class MenuItemActionExtensionAddMenuItemActionsForRecordsParams {
		private List<Record> records;
		private List<MenuItemAction> menuItemActions;
		private List<String> filteredActionTypes;
		private MenuItemActionBehaviorParams behaviorParams;
	}

	@AllArgsConstructor
	@Getter
	public static class MenuItemActionExtensionGetActionStateForRecordParams {
		private Record record;
		private MenuItemAction menuItemAction;
		private MenuItemActionBehaviorParams behaviorParams;
	}

	@AllArgsConstructor
	@Getter
	public static class MenuItemActionExtensionGetActionStateForRecordsParams {
		private List<Record> records;
		private MenuItemAction menuItemAction;
		private MenuItemActionBehaviorParams behaviorParams;
	}

}
