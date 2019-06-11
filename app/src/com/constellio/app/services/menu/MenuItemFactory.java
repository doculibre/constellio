package com.constellio.app.services.menu;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.contextmenu.ConfirmDialogContextMenuItemClickListener;
import com.google.common.base.Strings;
import com.vaadin.ui.Button;
import com.vaadin.ui.MenuBar.MenuItem;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import java.util.ArrayList;
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
			menuItem.setEnabled(menuItemAction.getState() == MenuItemActionState.VISIBLE);
		}
	}

	public void buildMenuBar(MenuItem rootItem, List<MenuItemAction> menuItemActions) {
		for (MenuItemAction menuItemAction : menuItemActions) {
			MenuItem menuItem = rootItem.addItem($(menuItemAction.getCaption()), menuItemAction.getIcon(),
					(selectedItem) -> menuItemAction.getCommand().run());
			menuItem.setEnabled(menuItemAction.getState() == MenuItemActionState.VISIBLE);
			menuItem.setVisible(menuItemAction.getState() != MenuItemActionState.HIDDEN);
		}
	}

	public List<Button> buildActionButtons(List<MenuItemAction> menuItemActions) {
		List<Button> actionButtons = new ArrayList<>();
		for (MenuItemAction menuItemAction : menuItemActions) {
			BaseButton actionButton = new BaseButton($(menuItemAction.getCaption()), menuItemAction.getIcon()) {
				@Override
				protected void buttonClick(ClickEvent event) {
					menuItemAction.getCommand().run();
				}
			};
			actionButton.setEnabled(menuItemAction.getState() == MenuItemActionState.VISIBLE);
			actionButton.setVisible(menuItemAction.getState() != MenuItemActionState.HIDDEN);
			actionButtons.add(actionButton);
		}
		return actionButtons;
	}
}
