package com.constellio.app.services.menu;

import com.constellio.app.services.actionDisplayManager.MenuDisplayContainer;
import com.constellio.app.services.actionDisplayManager.MenuDisplayItem;
import com.constellio.app.services.actionDisplayManager.MenuDisplayList;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.icons.DefaultIconService;
import com.constellio.app.services.icons.IconService;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.contextmenu.ConfirmDialogContextMenuItemClickListener;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.dao.services.Stats;
import com.constellio.data.utils.Pair;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.google.common.base.Strings;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.HIDDEN;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.VISIBLE;
import static com.constellio.app.ui.i18n.i18n.$;

public class MenuItemFactory {
	private final AppLayerFactory appLayerFactory;
	private final SessionContext sessionContext;
	private final IconService iconService;

	public MenuItemFactory() {
		this(ConstellioFactories.getInstance().getAppLayerFactory(), ConstellioUI.getCurrentSessionContext());
	}

	public MenuItemFactory(AppLayerFactory appLayerFactory, SessionContext sessionContext) {
		this.appLayerFactory = appLayerFactory;
		this.sessionContext = sessionContext;
		this.iconService = new DefaultIconService(appLayerFactory, sessionContext);
	}


	public void buildContextMenu(ContextMenu rootMenu, List<MenuItemAction> menuItemActions,
								 final MenuItemRecordProvider recordProvider) {
		menuItemActions.sort(Comparator.comparing(MenuItemAction::getPriority));
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
							Stats.compilerFor(menuItemAction.getCaption() + ":click").log(() ->
									menuItemAction.getCommand().accept(getRecordIds(recordProvider.getRecords())));
						}
					}
				});
			} else if (menuItemAction.getCommand() != null) {
				menuItem.addItemClickListener(event -> menuItemAction.getCommand()
						.accept(getRecordIds(recordProvider.getRecords())));
			}
			menuItem.setEnabled(menuItemAction.getState().getStatus() == VISIBLE);
		}
	}

	public void buildContextMenu(final ContextMenu rootMenu,
								 final List<MenuItemAction> menuItemActions,
								 final MenuItemRecordProvider recordProvider,
								 final MenuDisplayList menuDisplayList) {
		final Locale locale = sessionContext.getCurrentLocale();
		final Map<String, MenuItemAction> menuItemActionMap = menuItemActions.stream().collect(Collectors.toMap(MenuItemAction::getType, menuItemAction -> menuItemAction,
				this::whenDuplicatedCodeKeepTheOneWithStateVisibleOrFirst));

		if (menuDisplayList != null) {
			menuDisplayList.getActiveRootMenuList().forEach(menuDisplayItem -> {
				if (menuDisplayItem.isContainer()) {
					final List<ContextMenuItem> enabledMenuItems = new ArrayList<>();
					MenuDisplayContainer container = (MenuDisplayContainer) menuDisplayItem;
					ContextMenuItem menuItem = rootMenu.addItem(container.getLabels().get(locale), iconService.getIconByName(container.getIcon()));

					menuDisplayList.getActiveSubMenu(container.getCode())
							.stream()
							.filter(subMenuAction -> menuItemActionMap.containsKey(subMenuAction.getCode()))
							.forEach(subMenuAction -> {
								final MenuItemAction menuItemAction = menuItemActionMap.remove(subMenuAction.getCode());
								final MenuItemInfos menuItemInfos = getMenuItemInfosFromDisplayMenuItemAndMenuItemAction(menuDisplayItem, menuItemAction, iconService, recordProvider, (menuItemActionClicked, component) -> {
								});

								if (menuItemAction.getState().getStatus() == VISIBLE) {
									finishBuildingContextMenuItem(menuItem.addItem(menuItemInfos.caption, menuItemInfos.icon), menuItemInfos, menuItemAction);
								}
							});

					if (!menuItem.hasSubMenu()) {
						rootMenu.removeItem(menuItem);
					}
				} else {
					if (menuItemActionMap.containsKey(menuDisplayItem.getCode())) {
						final MenuItemAction menuItemAction = menuItemActionMap.remove(menuDisplayItem.getCode());
						final MenuItemInfos menuItemInfos = getMenuItemInfosFromDisplayMenuItemAndMenuItemAction(menuDisplayItem, menuItemAction, iconService, recordProvider, (menuItemActionClicked, component) -> {
						});

						if (menuItemAction.getState().getStatus() == VISIBLE) {
							finishBuildingContextMenuItem(rootMenu.addItem(menuItemInfos.caption, menuItemInfos.icon), menuItemInfos, menuItemAction);
						}
					}
				}
			});

			menuItemActionMap.values().stream()
					.filter(actionNotAdded -> actionNotAdded.getState().getStatus() == VISIBLE)
					.sorted(Comparator.comparing(MenuItemAction::getPriority))
					.forEach(actionNotAdded -> {

						final MenuItemInfos menuItemInfos = new MenuItemInfos(
								actionNotAdded.getCaption(), actionNotAdded.getIcon(),
								actionNotAdded.getCommand() == null ? null : selectedItem -> {
									actionNotAdded.getCommand().accept(getRecordIds(recordProvider.getRecords()));
								}
						);

						finishBuildingContextMenuItem(rootMenu.addItem(menuItemInfos.caption, menuItemInfos.icon), menuItemInfos, actionNotAdded);
					});
		} else {
			buildContextMenu(rootMenu, menuItemActions, recordProvider);
		}
	}

	public void finishBuildingContextMenuItem(ContextMenuItem menuItem, MenuItemInfos menuItemInfos,
											  MenuItemAction menuItemAction) {
		if (!Strings.isNullOrEmpty(menuItemAction.getConfirmMessage())) {
			menuItem.addItemClickListener(new ConfirmDialogContextMenuItemClickListener(menuItemAction.getDialogMode()) {
				@Override
				protected String getConfirmDialogMessage() {
					return menuItemAction.getConfirmMessage();
				}

				@Override
				protected void confirmButtonClick(ConfirmDialog dialog) {
					if (menuItemInfos.command != null) {
						Stats.compilerFor(menuItemInfos.caption + ":click").log(() ->
								menuItemInfos.command.menuSelected(null));
					}
				}
			});
		} else if (menuItemInfos.command != null) {
			menuItem.addItemClickListener(event -> menuItemInfos.command.menuSelected(null));
		}

		menuItem.setEnabled(menuItemAction.getState().getStatus() == VISIBLE);
	}

	public void buildMenuBar(final MenuItem rootItem, final List<MenuItemAction> menuItemActions,
							 final MenuItemRecordProvider recordProvider, final CommandCallback callback,
							 final Consumer<Pair<MenuItemAction, Object>> menuItemCreated) {
		menuItemActions.sort(Comparator.comparing(MenuItemAction::getPriority));
		for (final MenuItemAction menuItemAction : menuItemActions) {
			Command menuItemCommand;
			if (menuItemAction.getCommand() != null) {
				menuItemCommand = selectedItem -> Stats.compilerFor(menuItemAction.getCaption() + ":click").log(() -> {
					menuItemAction.getCommand().accept(getRecordIds(recordProvider.getRecords()));
					callback.actionExecuted(menuItemAction, selectedItem);
				});
			} else {
				menuItemCommand = null;
			}
			MenuItem menuItem = rootItem.addItem(menuItemAction.getCaption(), menuItemAction.getIcon(), menuItemCommand);
			menuItem.setEnabled(menuItemAction.getState().getStatus() == VISIBLE);
			menuItem.setVisible(menuItemAction.getState().getStatus() != HIDDEN);
			menuItem.setDescription(menuItemAction.getState().getReason());

			if (menuItemCreated != null) {
				menuItemCreated.accept(new Pair<>(menuItemAction, menuItem));
			}
		}
	}

	public void buildMenuBar(final MenuItem rootItem, final List<MenuItemAction> menuItemActions,
							 final MenuItemRecordProvider recordProvider, final CommandCallback callback,
							 final MenuDisplayList menuDisplayList,
							 final Consumer<Pair<MenuItemAction, Object>> menuItemCreated) {

		final Locale locale = sessionContext.getCurrentLocale();
		final Map<String, MenuItemAction> menuItemActionMap = menuItemActions.stream().collect(Collectors.toMap(MenuItemAction::getType, menuItemAction -> menuItemAction,
				this::whenDuplicatedCodeKeepTheOneWithStateVisibleOrFirst));

		if (menuDisplayList != null) {
			menuDisplayList.getActiveRootMenuList().forEach(menuDisplayItem -> {
				if (menuDisplayItem.isContainer()) {
					MenuDisplayContainer container = (MenuDisplayContainer) menuDisplayItem;
					MenuItem menuItem = rootItem.addItem(container.getLabels().get(locale), iconService.getIconByName(container.getIcon()), null);

					menuDisplayList.getActiveSubMenu(container.getCode())
							.stream()
							.filter(subMenuAction -> menuItemActionMap.containsKey(subMenuAction.getCode()))
							.forEach(subMenuAction -> {
								final MenuItemAction menuItemAction = menuItemActionMap.remove(subMenuAction.getCode());

								MenuItemInfos menuItemInfos = getMenuItemInfosFromDisplayMenuItemAndMenuItemAction(subMenuAction, menuItemAction, iconService, recordProvider, callback);

								MenuItem subMenuItem = menuItem.addItem(menuItemInfos.caption, menuItemInfos.icon, menuItemInfos.command);


								finalizeInitilizationForMenuItem(subMenuItem, menuItemAction);

								if (menuItemCreated != null) {
									menuItemCreated.accept(new Pair<>(menuItemAction, subMenuItem));
								}
							});

					if (!menuItem.hasChildren() || menuItem.getChildren().stream().noneMatch(MenuItem::isVisible)) {
						menuItem.setVisible(false);
					}
				} else {
					if (menuItemActionMap.containsKey(menuDisplayItem.getCode())) {
						final MenuItemAction menuItemAction = menuItemActionMap.remove(menuDisplayItem.getCode());

						MenuItemInfos menuItemInfos = getMenuItemInfosFromDisplayMenuItemAndMenuItemAction(menuDisplayItem, menuItemAction, iconService, recordProvider, callback);

						MenuItem subMenuItem = rootItem.addItem(menuItemInfos.caption, menuItemInfos.icon, menuItemInfos.command);
						finalizeInitilizationForMenuItem(subMenuItem, menuItemAction);

						if (menuItemCreated != null) {
							menuItemCreated.accept(new Pair<>(menuItemAction, subMenuItem));
						}
					}
				}
			});

			menuItemActionMap.forEach((key, actionNotAdded) -> {
				Command menuItemCommand;
				if (actionNotAdded.getCommand() != null) {
					menuItemCommand = selectedItem -> Stats.compilerFor(actionNotAdded.getCaption() + ":click").log(() -> {
						actionNotAdded.getCommand().accept(getRecordIds(recordProvider.getRecords()));
						callback.actionExecuted(actionNotAdded, selectedItem);
					});
				} else {
					menuItemCommand = null;
				}
				MenuItem menuItem = rootItem.addItem(actionNotAdded.getCaption(), actionNotAdded.getIcon(), menuItemCommand);
				menuItem.setEnabled(actionNotAdded.getState().getStatus() == VISIBLE);
				menuItem.setVisible(actionNotAdded.getState().getStatus() != HIDDEN);
				menuItem.setDescription(actionNotAdded.getState().getReason());
				if (menuItemCreated != null) {
					menuItemCreated.accept(new Pair<>(actionNotAdded, menuItem));
				}
			});
		} else {
			buildMenuBar(rootItem, menuItemActions, recordProvider, callback, menuItemCreated);
		}


	}

	MenuItemInfos getMenuItemInfosFromDisplayMenuItemAndMenuItemAction(final MenuDisplayItem menuDisplayItem,
																	   final MenuItemAction menuItemAction,
																	   final IconService iconService,
																	   final MenuItemRecordProvider recordProvider,
																	   final CommandCallback callback) {
		return new MenuItemInfos(
				menuItemAction.getCaption() != null ? menuItemAction.getCaption() : $(menuDisplayItem.getI18nKey()),
				menuItemAction.getIcon() != null ? menuItemAction.getIcon() : iconService.getIconByName(menuDisplayItem.getIcon()),
				menuItemAction.getCommand() == null ? null : selectedItem -> {
					menuItemAction.getCommand().accept(getRecordIds(recordProvider.getRecords()));
					callback.actionExecuted(menuItemAction, selectedItem);
				}
		);
	}

	private void finalizeInitilizationForMenuItem(MenuItem menuItem, final MenuItemAction menuItemAction) {
		menuItem.setEnabled(menuItemAction.getState().getStatus() == VISIBLE);
		menuItem.setVisible(menuItemAction.getState().getStatus() != HIDDEN);
		menuItem.setDescription(menuItemAction.getState().getReason());
	}

	public Component buildQuickAction(MenuItemAction menuItemAction,
									  Function<String, MenuItemAction> getMostRecentMenuItemAction,
									  MenuDisplayItem menuDisplayItem,
									  MenuItemRecordProvider recordProvider, final CommandCallback callback) {
		String type = menuItemAction.getType();
		MenuItemInfos infos = getMenuItemInfosFromDisplayMenuItemAndMenuItemAction(menuDisplayItem, menuItemAction, iconService, recordProvider, callback);

		final BaseButton actionButton = new BaseButton(infos.caption, infos.icon) {
			@Override
			protected void buttonClick(ClickEvent event) {
				if (getMostRecentMenuItemAction != null) {
					MenuItemAction mostRecentMenuItemAction = getMostRecentMenuItemAction.apply(type);
					if (mostRecentMenuItemAction.getCommand() != null) {
						Stats.compilerFor(mostRecentMenuItemAction.getCaption() + ":click").log(() -> {
							mostRecentMenuItemAction.getCommand().accept(getRecordIds(recordProvider.getRecords()));
							callback.actionExecuted(mostRecentMenuItemAction, event.getComponent());
						});
					}
				}
			}
		};

		actionButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		actionButton.addStyleName(ValoTheme.BUTTON_LINK);
		actionButton.addStyleName("action-menu-bar-button");
		actionButton.setCaptionVisibleOnMobile(false);
		actionButton.setId(type);
		actionButton.setEnabled(menuItemAction.getState().getStatus() == VISIBLE);
		actionButton.setVisible(menuItemAction.getState().getStatus() != HIDDEN);
		actionButton.setDescription(menuItemAction.getState().getReason());

		return actionButton;
	}

	public Component buildQuickAction(MenuItemAction menuItemAction,
									  Function<String, MenuItemAction> getMostRecentMenuItemAction,
									  MenuItemRecordProvider recordProvider,
									  final CommandCallback callback) {
		String type = menuItemAction.getType();

		final BaseButton actionButton = new BaseButton(menuItemAction.getCaption(), menuItemAction.getIcon()) {
			@Override
			protected void buttonClick(ClickEvent event) {
				if (getMostRecentMenuItemAction != null) {
					MenuItemAction mostRecentMenuItemAction = getMostRecentMenuItemAction.apply(type);
					if (mostRecentMenuItemAction.getCommand() != null) {
						Stats.compilerFor(mostRecentMenuItemAction.getCaption() + ":click").log(() -> {
							mostRecentMenuItemAction.getCommand().accept(getRecordIds(recordProvider.getRecords()));
							callback.actionExecuted(mostRecentMenuItemAction, event.getComponent());
						});
					}
				}
			}
		};
		actionButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		actionButton.addStyleName(ValoTheme.BUTTON_LINK);
		actionButton.addStyleName("action-menu-bar-button");
		actionButton.setId(type);
		actionButton.setCaptionVisibleOnMobile(false);
		actionButton.setEnabled(menuItemAction.getState().getStatus() == VISIBLE);
		actionButton.setVisible(menuItemAction.getState().getStatus() != HIDDEN);
		actionButton.setDescription(menuItemAction.getState().getReason());

		return actionButton;
	}

	public List<Button> buildActionButtons(List<MenuItemAction> menuItemActions,
										   final MenuItemRecordProvider recordProvider,
										   final CommandCallback callback) {
		List<Button> actionButtons = new ArrayList<>();
		menuItemActions.sort(Comparator.comparing(MenuItemAction::getPriority));
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

	private MenuItemAction whenDuplicatedCodeKeepTheOneWithStateVisibleOrFirst(MenuItemAction first,
																			   MenuItemAction second) {
		return Stream.of(first, second)
				.filter(menuItemAction -> menuItemAction.getState().getStatus() == VISIBLE)
				.findFirst()
				.orElse(first);
	}

	public interface MenuItemRecordProvider {

		LogicalSearchQuery getQuery();

		List<Record> getRecords();

	}

	public interface CommandCallback {

		void actionExecuted(MenuItemAction menuItemAction, Object component);

	}


	private class MenuItemInfos {
		public final String caption;
		public final Resource icon;
		public final Command command;

		private MenuItemInfos(String caption, Resource icon, Command command) {
			this.caption = caption;
			this.icon = icon;
			this.command = command;
		}
	}
}
