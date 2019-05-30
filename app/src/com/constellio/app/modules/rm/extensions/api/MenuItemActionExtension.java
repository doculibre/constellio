package com.constellio.app.modules.rm.extensions.api;

import com.constellio.app.modules.rm.services.menu.MenuItemAction;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public class MenuItemActionExtension {

	public void addMenuItemActions(MenuItemActionExtensionAddMenuItemActionsParams params) {
	}

	@AllArgsConstructor
	@Getter
	public static class MenuItemActionExtensionAddMenuItemActionsParams {
		private Record record;
		private User user;
		private List<MenuItemAction> menuItemActions;
	}

}
