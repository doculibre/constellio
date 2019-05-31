package com.constellio.app.modules.rm.services.menu;

import com.constellio.app.ui.framework.components.contextmenu.ConfirmDialogContextMenuItemClickListener;
import com.google.common.base.Strings;
import com.vaadin.ui.MenuBar;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class MenuItemFactory {

	public void buildContextMenu(ContextMenu rootMenu, List<MenuItemAction> menuItemActions) {
		for (MenuItemAction menuItemAction : menuItemActions) {
			ContextMenuItem menuItem = rootMenu.addItem($(menuItemAction.getCaption()), menuItemAction.getIcon());
			if (!Strings.isNullOrEmpty(menuItemAction.getConfirmMessage())) {
				menuItem.addItemClickListener(new ConfirmDialogContextMenuItemClickListener(menuItemAction.getDialogMode()) {
					@Override
					protected String getConfirmDialogMessage() {
						return menuItemAction.getConfirmMessage();
					}

					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						menuItemAction.getCommand().run();
					}
				});
			} else if (menuItemAction.getCommand() != null) {
				menuItem.addItemClickListener((event) -> {
					menuItemAction.getCommand().run();
				});
			} else if (menuItemAction.getButton() != null) {
				menuItem.addItemClickListener((event) -> {
					// TODO
				});
			}
		}
	}

	public void buildMenuBar(MenuBar rootMenu, List<MenuItemAction> menuItemActions) {
		for (MenuItemAction menuItemAction : menuItemActions) {
			rootMenu.addItem($(menuItemAction.getCaption()), menuItemAction.getIcon(),
					(selectedItem) -> menuItemAction.getCommand().run());
		}
	}
}
