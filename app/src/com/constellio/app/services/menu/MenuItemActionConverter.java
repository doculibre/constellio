package com.constellio.app.services.menu;

import com.constellio.app.services.menu.MenuItemAction.MenuItemActionBuilder;
import com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;

import java.util.function.Function;

public class MenuItemActionConverter {
	public static MenuItemAction toMenuItemAction(final Object object) {
		return toMenuItemAction(object, menuItemActionBuilder -> menuItemActionBuilder != null ? menuItemActionBuilder.build() : null);
	}

	public static MenuItemAction toMenuItemAction(final Object object,
												  Function<MenuItemActionBuilder, MenuItemAction> lastModificationThenBuild) {
		MenuItemActionBuilder menuItemActionBuilder;

		if (object instanceof Component) {
			menuItemActionBuilder = toMenuItemActionBuilder((Component) object);
		} else {
			menuItemActionBuilder = null;
		}

		MenuItemAction menuItemAction;
		if (menuItemActionBuilder != null) {
			if (lastModificationThenBuild != null) {
				menuItemAction = lastModificationThenBuild.apply(menuItemActionBuilder);
			} else {
				menuItemAction = menuItemActionBuilder.build();
			}
		} else {
			menuItemAction = null;
		}

		return menuItemAction;
	}

	private static MenuItemActionBuilder toMenuItemActionBuilder(Component component) {
		if (component instanceof Button) {
			return toMenuItemActionBuilder((Button) component);
		} else {
			return null;
		}
	}

	private static MenuItemActionBuilder toMenuItemActionBuilder(Button button) {
		if (button != null) {
			return MenuItemAction.builder()
					.type(button.getId())
					.caption(button.getCaption())
					.icon(button.getIcon())
					.priority(100)
					.state(new MenuItemActionState(button.isEnabled() ? MenuItemActionStateStatus.VISIBLE : MenuItemActionStateStatus.DISABLED))
					.command(recordIds -> button.click());
		} else {
			return null;
		}
	}
}
