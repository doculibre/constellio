package com.constellio.app.services.menu;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.contextmenu.ConfirmDialogContextMenuItemClickListener;
import com.constellio.data.dao.services.Stats;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.google.common.base.Strings;
import com.vaadin.ui.Button;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.HIDDEN;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.VISIBLE;

public class MenuItemFactory {

	public void buildContextMenu(ContextMenu rootMenu, List<MenuItemAction> menuItemActions,
								 final MenuItemRecordProvider recordProvider) {
		for (MenuItemAction menuItemAction : menuItemActions) {
			ContextMenuItem menuItem = rootMenu.addItem(menuItemAction.getCaption(), menuItemAction.getIcon());
			if (!Strings.isNullOrEmpty(menuItemAction.getConfirmMessage())) {
				menuItem.addItemClickListener(new ConfirmDialogContextMenuItemClickListener(menuItemAction.getDialogMode()) {
					@Override
					protected String getConfirmDialogMessage() {
						return menuItemAction.getConfirmMessage();
					}

					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						if (menuItemAction.getCommand() != null) {
							Stats.compilerFor(menuItemAction.getCaption() + ":click").log(() -> {
								menuItemAction.getCommand().accept(getRecordIds(recordProvider.getRecords()));
							});
						}
					}
				});
			} else if (menuItemAction.getCommand() != null) {
				menuItem.addItemClickListener((event) -> {
					menuItemAction.getCommand().accept(getRecordIds(recordProvider.getRecords()));
				});
			}
			menuItem.setEnabled(menuItemAction.getState().getStatus() == VISIBLE);
		}
	}

	public void buildMenuBar(final MenuItem rootItem, final List<MenuItemAction> menuItemActions,
							 final MenuItemRecordProvider recordProvider, final CommandCallback callback) {
		for (final MenuItemAction menuItemAction : menuItemActions) {
			Command menuItemCommand;
			if (menuItemAction.getCommand() != null) {
				menuItemCommand = new Command() {
					@Override
					public void menuSelected(MenuItem selectedItem) {
						Stats.compilerFor(menuItemAction.getCaption() + ":click").log(() -> {
							menuItemAction.getCommand().accept(getRecordIds(recordProvider.getRecords()));
							callback.actionExecuted(menuItemAction, selectedItem);
						});
					}
				};
			} else {
				menuItemCommand = null;
			}
			MenuItem menuItem = rootItem.addItem(menuItemAction.getCaption(), menuItemAction.getIcon(), menuItemCommand);
			menuItem.setEnabled(menuItemAction.getState().getStatus() == VISIBLE);
			menuItem.setVisible(menuItemAction.getState().getStatus() != HIDDEN);
			menuItem.setDescription(menuItemAction.getState().getReason());
		}
	}

	public List<Button> buildActionButtons(List<MenuItemAction> menuItemActions,
										   final MenuItemRecordProvider recordProvider,
										   final CommandCallback callback) {
		List<Button> actionButtons = new ArrayList<>();
		for (MenuItemAction menuItemAction : menuItemActions) {
			final BaseButton actionButton = new BaseButton(menuItemAction.getCaption(), menuItemAction.getIcon()) {
				@Override
				protected void buttonClick(ClickEvent event) {
					if (menuItemAction.getCommand() != null) {
						Stats.compilerFor(menuItemAction.getCaption() + ":click").log(() -> {
							menuItemAction.getCommand().accept(getRecordIds(recordProvider.getRecords()));
							callback.actionExecuted(menuItemAction, event.getComponent());
						});
					}
				}
			};
			actionButton.setId(menuItemAction.getType());
			actionButton.setEnabled(menuItemAction.getState().getStatus() == VISIBLE);
			actionButton.setVisible(menuItemAction.getState().getStatus() != HIDDEN);
			actionButton.setDescription(menuItemAction.getState().getReason());
			actionButtons.add(actionButton);
		}
		return actionButtons;
	}

	private List<String> getRecordIds(List<Record> records) {
		List<String> recordIds = new ArrayList<>();
		for (Record record : records) {
			recordIds.add(record.getId());
		}
		return recordIds;
	}

	public static interface MenuItemRecordProvider {

		LogicalSearchQuery getQuery();

		List<Record> getRecords();

	}

	public static interface CommandCallback {

		void actionExecuted(MenuItemAction menuItemAction, Object component);

	}

}
