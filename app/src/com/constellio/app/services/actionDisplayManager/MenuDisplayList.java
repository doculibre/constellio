package com.constellio.app.services.actionDisplayManager;

import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MenuDisplayList implements Serializable {
	public static final int QUICK_ACTION_COUNT_DEFAULT = 3;

	private List<MenuDisplayItem> actionDisplays;

	public MenuDisplayList() {
		this.actionDisplays = Collections.unmodifiableList(new ArrayList<>());
	}

	public MenuDisplayList(List<MenuDisplayItem> actionDisplays) {
		this.actionDisplays = Collections.unmodifiableList(actionDisplays);
	}

	protected List<MenuDisplayItem> getRawMenus() {
		return Collections.unmodifiableList(this.actionDisplays);
	}

	public boolean hasChanges(List<MenuDisplayItem> menuDisplayItemList) {
		if (this.actionDisplays.size() != menuDisplayItemList.size()) {
			return true;
		}

		for (int i = 0; i < menuDisplayItemList.size(); i++) {
			if (!menuDisplayItemList.get(i).equals(this.actionDisplays.get(i))) {
				return true;
			}
		}

		return false;
	}

	public List<MenuDisplayItem> getRootMenuList() {
		List<MenuDisplayItem> menuDisplayItem = new ArrayList<>();

		for (MenuDisplayItem currentActionDisplay : this.actionDisplays) {
			if (currentActionDisplay.getParentCode() == null) {
				menuDisplayItem.add(currentActionDisplay);
			}
		}

		return Collections.unmodifiableList(menuDisplayItem);
	}

	public List<MenuDisplayItem> getActiveSubMenu(String menuActionCode) {
		List<MenuDisplayItem> menuDisplayItem = new ArrayList<>();

		for (MenuDisplayItem currentActionDisplay : this.actionDisplays) {
			if (menuActionCode.equals(currentActionDisplay.getParentCode()) && currentActionDisplay.isOfficiallyActive()) {
				menuDisplayItem.add(currentActionDisplay);
			}
		}

		return Collections.unmodifiableList(menuDisplayItem);
	}


	public List<MenuDisplayItem> getSubMenu(String menuActionCode) {
		List<MenuDisplayItem> menuDisplayItem = new ArrayList<>();

		for (MenuDisplayItem currentActionDisplay : this.actionDisplays) {
			if (menuActionCode.equals(currentActionDisplay.getParentCode())) {
				menuDisplayItem.add(currentActionDisplay);
			}
		}

		return Collections.unmodifiableList(menuDisplayItem);
	}

	public List<MenuDisplayItem> getActiveRootMenuList() {
		List<MenuDisplayItem> menuDisplayItem = new ArrayList<>();

		for (MenuDisplayItem currentActionDisplay : this.actionDisplays) {
			if (currentActionDisplay.getParentCode() == null && currentActionDisplay.isOfficiallyActive()) {
				menuDisplayItem.add(currentActionDisplay);
			}
		}

		return Collections.unmodifiableList(menuDisplayItem);
	}

	public List<MenuDisplayItem> getQuickActionList() {
		return getQuickActionList(null, QUICK_ACTION_COUNT_DEFAULT);
	}

	public List<MenuDisplayItem> getQuickActionList(int quickActionCount) {
		return getQuickActionList(null, quickActionCount);
	}

	public List<MenuDisplayItem> getQuickActionList(Map<String, MenuItemAction> actionsToDisplay) {
		return getQuickActionList(actionsToDisplay, QUICK_ACTION_COUNT_DEFAULT);
	}

	public List<MenuDisplayItem> getQuickActionList(Map<String, MenuItemAction> actionsToDisplay,
													int quickActionCount) {

		Stream<MenuDisplayItem> menuDisplayItemStream = getActiveRootMenuList().stream()
				.filter(Objects::nonNull)
				.flatMap(rootAction -> rootAction.isContainer() ? getActiveSubMenu(rootAction.getCode()).stream().filter(Objects::nonNull) : Stream.of(rootAction));

		if (actionsToDisplay != null) {
			menuDisplayItemStream = menuDisplayItemStream
					.filter(menuDisplayItem -> actionsToDisplay.containsKey(menuDisplayItem.getCode()))
					.filter(menuDisplayItem -> actionsToDisplay.get(menuDisplayItem.getCode()).getState().getStatus() == MenuItemActionStateStatus.VISIBLE);
		}

		return Collections.unmodifiableList(menuDisplayItemStream
				.limit(quickActionCount).collect(Collectors.toList()));
	}
}
