/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.base;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.pages.viewGroups.MenuViewGroup;
import com.constellio.app.ui.pages.viewGroups.MenuViewGroup.DisabledMenuViewGroup;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

/**
 * A responsive menu component providing user information and the controls for
 * primary navigation between the views.
 */
@SuppressWarnings({ "serial" })
public class ConstellioMenuImpl extends CustomComponent implements ConstellioMenu {

	public static final String ID = "dashboard-menu";
	private static final String STYLE_VISIBLE = "valo-menu-visible";
	public static final String STYLE_USER_SETTINGS = "user-settings";

	private ConstellioMenuPresenter presenter;

	private MenuItem userSettingsItem;

	private CssLayout menuContent;

	private Component titleComponent;

	private MenuBar userMenu;

	private Button valoMenuToggleButton;

	private CssLayout menuItemsLayout;

	private List<ConstellioMenuButton> mainMenuButtons = new ArrayList<ConstellioMenuButton>();

	private List<String> collections = new ArrayList<String>();

	public ConstellioMenuImpl() {
		this.presenter = new ConstellioMenuPresenter(this);

		addStyleName(ValoTheme.MENU_ROOT);
		setId(ID);
		setSizeUndefined();

		setCompositionRoot(buildContent());
	}

	@Override
	public void setCollections(List<String> collections) {
		this.collections = collections;
	}

	private Component buildContent() {
		menuContent = new CssLayout();
		menuContent.addStyleName("sidebar");
		menuContent.addStyleName(ValoTheme.MENU_PART);
		menuContent.addStyleName("no-vertical-drag-hints");
		menuContent.addStyleName("no-horizontal-drag-hints");
		menuContent.setWidth(null);
		menuContent.setHeight("100%");

		Component titleComponent = buildTitle();
		if (titleComponent != null) {
			menuContent.addComponent(titleComponent);
		}
		menuContent.addComponent(buildUserMenu());
		menuContent.addComponent(buildToggleButton());
		menuContent.addComponent(buildMainMenu());

		return menuContent;
	}

	protected Component buildTitle() {
		if (!collections.isEmpty()) {
			MenuBar collectionMenu = new MenuBar();
			collectionMenu.addStyleName("collection-menu");

			SessionContext sessionContext = getSessionContext();
			String currentCollection = sessionContext.getCurrentCollection();
			MenuItem collectionSubMenu = collectionMenu.addItem($("ConstellioMenu.collection"), null);
			for (final String collection : collections) {
				String collectionCaption = presenter.getCollectionCaption(collection);
				MenuItem collectionMenuItem = collectionSubMenu.addItem(collectionCaption, new Command() {
					@Override
					public void menuSelected(MenuItem selectedItem) {
						presenter.collectionClicked(collection);
						List<MenuItem> menuItems = selectedItem.getParent().getChildren();
						for (MenuItem menuItem : menuItems) {
							menuItem.setChecked(false);
						}
						selectedItem.setChecked(true);
					}
				});
				collectionMenuItem.setCheckable(true);
				collectionMenuItem.setChecked(currentCollection.equals(collection));
			}
			HorizontalLayout collectionMenuWrapper = new HorizontalLayout(collectionMenu);
			collectionMenuWrapper.setComponentAlignment(collectionMenu, Alignment.MIDDLE_CENTER);
			collectionMenuWrapper.addStyleName(ValoTheme.MENU_TITLE);
			titleComponent = collectionMenuWrapper;
		} else {
			titleComponent = null;
		}
		return titleComponent;
	}

	protected Component buildUserMenu() {
		userMenu = new MenuBar();
		userMenu.addStyleName("user-menu");
		buildUserMenuItems(userMenu);
		return userMenu;
	}

	protected Component buildToggleButton() {
		valoMenuToggleButton = new Button("Menu", new ClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				if (getCompositionRoot().getStyleName().contains(STYLE_VISIBLE)) {
					getCompositionRoot().removeStyleName(STYLE_VISIBLE);
				} else {
					getCompositionRoot().addStyleName(STYLE_VISIBLE);
				}
			}
		});
		valoMenuToggleButton.setIcon(FontAwesome.LIST);
		valoMenuToggleButton.addStyleName("valo-menu-toggle");
		valoMenuToggleButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		valoMenuToggleButton.addStyleName(ValoTheme.BUTTON_SMALL);
		return valoMenuToggleButton;
	}

	private Component buildMainMenu() {
		menuItemsLayout = new CssLayout();
		menuItemsLayout.addStyleName("valo-menuitems");
		menuItemsLayout.setHeight(100.0f, Unit.PERCENTAGE);

		mainMenuButtons = buildMainMenuButtons();

		for (ConstellioMenuButton mainMenuButton : mainMenuButtons) {
			Button menuButton = mainMenuButton.getButton();
			Class<? extends MenuViewGroup> menuViewGroupClass = mainMenuButton.getMenuViewGroup();

			Component mainMenuItemComponent = menuButton;
			mainMenuItemComponent.setPrimaryStyleName(ValoTheme.MENU_ITEM);
			if (DisabledMenuViewGroup.class.isAssignableFrom(menuViewGroupClass)) {
				menuButton.addStyleName("disabled");
			}

			// FIXME Use the badge mechanism properly
			//			Label badgeLabel = new Label();
			//			buildBadgeWrapper(menuButton, badgeLabel);
			//			badgeLabel.setId("myBadgeId");
			//			mainMenuItemComponent = buildBadgeWrapper(menuButton, badgeLabel);

			menuItemsLayout.addComponent(mainMenuItemComponent);
		}

		UI.getCurrent().getNavigator().addViewChangeListener(new ViewChangeListener() {
			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {
				View newView = event.getNewView();
				final String selectedStyleName = "selected";

				boolean newSelection = false;
				Button lastSelectedButton = null;
				for (ConstellioMenuButton mainMenuButton : mainMenuButtons) {
					Button menuButton = mainMenuButton.getButton();
					Class<? extends MenuViewGroup> menuViewGroupClass = mainMenuButton.getMenuViewGroup();
					if (menuButton.getStyleName().contains(selectedStyleName)) {
						lastSelectedButton = menuButton;
					}
					if (menuViewGroupClass.isAssignableFrom(newView.getClass())) {
						menuButton.addStyleName(selectedStyleName);
						newSelection = true;
					} else {
						menuButton.removeStyleName(selectedStyleName);
					}
				}
				if (!newSelection && lastSelectedButton != null) {
					lastSelectedButton.addStyleName(selectedStyleName);
				}
				return true;
			}

			@Override
			public void afterViewChange(ViewChangeEvent event) {
			}
		});

		return menuItemsLayout;
	}

	protected void buildUserMenuItems(MenuBar userMenu) {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		UserVO currentUser = sessionContext.getCurrentUser();
		String firstName = currentUser.getFirstName();
		String lastName = currentUser.getLastName();

		if (currentUser.getEmail() != null && currentUser.getEmail().startsWith("elizabeth.madera")) {
			userSettingsItem = userMenu.addItem("", new ThemeResource("images/profiles/egg2.jpg"), null);

		} else if (!presenter.hasCurrentUserPhoto()) {
			userSettingsItem = userMenu.addItem("", new ThemeResource("images/profiles/default.jpg"), null);

		} else {
			StreamSource source = new StreamSource() {
				@Override
				public InputStream getStream() {
					return presenter.newUserPhotoInputStream();
				}
			};
			StreamResource resource = new StreamResource(source, currentUser.getUsername() + ".png");
			userSettingsItem = userMenu.addItem("", resource, null);
		}
		userSettingsItem.setText(firstName + " " + lastName);
		userSettingsItem.setStyleName(STYLE_USER_SETTINGS);

		userSettingsItem.addItem($("ConstellioMenu.editProfile"), new Command() {
			@Override
			public void menuSelected(final MenuItem selectedItem) {
				String params = Page.getCurrent().getUriFragment();
				if (params != null) {
					params = params.replace("!", "");
				}
				presenter.editProfileButtonClicked(params);
			}
		});
		//		userSettingsItem.addItem($("ConstellioMenu.preferences"), new Command() {
		//			@Override
		//			public void menuSelected(final MenuItem selectedItem) {
		//				presenter.preferencesButtonClicked();
		//			}
		//		});
		userSettingsItem.addSeparator();
		userSettingsItem.addItem($("ConstellioMenu.signOut"), new Command() {
			@Override
			public void menuSelected(final MenuItem selectedItem) {
				presenter.signOutButtonClicked();
			}
		});
	}

	public MenuItem getUserSettingsItem() {
		return userSettingsItem;
	}

	protected List<ConstellioMenuButton> buildMainMenuButtons() {
		List<ConstellioMenuButton> mainMenuButtons = new ArrayList<ConstellioMenuButton>();
		return mainMenuButtons;
	}

	private Component buildBadgeWrapper(final Component menuItemButton, final Component badgeLabel) {
		CssLayout dashboardWrapper = new CssLayout(menuItemButton);
		dashboardWrapper.addStyleName("badgewrapper");
		dashboardWrapper.addStyleName(ValoTheme.MENU_ITEM);
		dashboardWrapper.setWidth(100.0f, Unit.PERCENTAGE);
		badgeLabel.addStyleName(ValoTheme.MENU_BADGE);
		badgeLabel.setWidthUndefined();
		badgeLabel.setVisible(false);
		dashboardWrapper.addComponent(badgeLabel);
		return dashboardWrapper;
	}

	@Override
	public ConstellioNavigator navigateTo() {
		return new ConstellioNavigator(UI.getCurrent().getNavigator());
	}

	@Override
	public SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

	@Override
	public ConstellioFactories getConstellioFactories() {
		return ConstellioFactories.getInstance();
	}

	@Override
	public void updateUIContent() {
		ConstellioUI.getCurrent().updateContent();
	}

	public static class ConstellioMenuButton implements Serializable {

		private Class<? extends MenuViewGroup> menuViewGroupClass;

		private Button button;

		public ConstellioMenuButton(Class<? extends MenuViewGroup> menuViewGroupClass, Button button) {
			this.menuViewGroupClass = menuViewGroupClass;
			this.button = button;
		}

		public Class<? extends MenuViewGroup> getMenuViewGroup() {
			return menuViewGroupClass;
		}

		public Button getButton() {
			return button;
		}

	}

}
