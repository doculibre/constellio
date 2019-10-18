package com.constellio.app.extensions.menu;

import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public abstract class MenuItemActionsExtension {

	public void addMenuItemActionsForRecord(MenuItemActionExtensionAddMenuItemActionsForRecordParams params) {
	}

	public void addMenuItemActionsForRecords(MenuItemActionExtensionAddMenuItemActionsForRecordsParams params) {
	}

	public void addMenuItemActionsForQuery(MenuItemActionExtensionAddMenuItemActionsForQueryParams params) {
	}

	public MenuItemActionState getActionStateForRecord(MenuItemActionExtensionGetActionStateForRecordParams params) {
		return null;
	}

	public MenuItemActionState getActionStateForRecords(MenuItemActionExtensionGetActionStateForRecordsParams params) {
		return null;
	}

	public MenuItemActionState getActionStateForQuery(MenuItemActionExtensionGetActionStateForQueryParams params) {
		return null;
	}

	protected MenuItemActionState toState(boolean actionPossible) {
		return new MenuItemActionState(actionPossible ? MenuItemActionStateStatus.VISIBLE : MenuItemActionStateStatus.HIDDEN);
	}

	@AllArgsConstructor
	@Getter
	public static class MenuItemActionExtensionAddMenuItemActionsForRecordParams {
		private Record record;
		private List<MenuItemAction> menuItemActions;
		private List<String> excludedActionTypes;
		private MenuItemActionBehaviorParams behaviorParams;
	}

	@AllArgsConstructor
	@Getter
	public static class MenuItemActionExtensionAddMenuItemActionsForRecordsParams {
		private List<Record> records;
		private List<MenuItemAction> menuItemActions;
		private List<String> excludedActionTypes;
		private MenuItemActionBehaviorParams behaviorParams;
	}

	@AllArgsConstructor
	@Getter
	public static class MenuItemActionExtensionAddMenuItemActionsForQueryParams {
		private LogicalSearchQuery query;
		private List<MenuItemAction> menuItemActions;
		private List<String> excludedActionTypes;
		private MenuItemActionBehaviorParams behaviorParams;
		private boolean returnedResults;
	}

	@AllArgsConstructor
	@Getter
	public static class MenuItemActionExtensionGetActionStateForRecordParams {
		private Record record;
		private String menuItemActionType;
		private MenuItemActionBehaviorParams behaviorParams;
	}

	@AllArgsConstructor
	@Getter
	public static class MenuItemActionExtensionGetActionStateForRecordsParams {
		private List<Record> records;
		private String menuItemActionType;
		private MenuItemActionBehaviorParams behaviorParams;
	}

	@AllArgsConstructor
	@Getter
	public static class MenuItemActionExtensionGetActionStateForQueryParams {
		private LogicalSearchQuery query;
		private String menuItemActionType;
		private MenuItemActionBehaviorParams behaviorParams;
	}

}
