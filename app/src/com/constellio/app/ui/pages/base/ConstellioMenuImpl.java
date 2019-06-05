package com.constellio.app.ui.pages.base;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.menuBar.BaseMenuBar;
import com.constellio.app.ui.pages.viewGroups.MenuViewGroup;
import com.constellio.app.ui.pages.viewGroups.MenuViewGroup.DisabledMenuViewGroup;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

/**
 * A responsive menu component providing user information and the controls for
 * primary navigation between the views.
 */
@SuppressWarnings({"serial"})
public class ConstellioMenuImpl extends CustomComponent implements ConstellioMenu {

	public static final String ID = "dashboard-menu";
	private static final String STYLE_VISIBLE = "valo-menu-visible";
	public static final String STYLE_USER_SETTINGS = "user-settings";

	private ConstellioMenuPresenter presenter;

	private MenuItem userSettingsItem;

	private CssLayout menuContent;

	private MenuBar userMenu;

	private CssLayout menuItemsLayout;

	private List<ConstellioMenuButton> mainMenuButtons = new ArrayList<>();

	private Map<ConstellioMenuButton, Label> badgeLabels = new HashMap<>();

	public ConstellioMenuImpl() {
		this.presenter = new ConstellioMenuPresenter(this);

		addStyleName(ValoTheme.MENU_ROOT);
		setId(ID);
		setSizeUndefined();

		setCompositionRoot(buildContent());
		//		UI.getCurrent().addClickListener(new com.vaadin.event.MouseEvents.ClickListener() {
		//			@Override
		//			public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
		//				hideMenu();
		//			}
		//		});
	}

	protected void hideMenu() {
		Component compositionRoot = getCompositionRoot();
		if (compositionRoot.getStyleName().contains(STYLE_VISIBLE)) {
			compositionRoot.removeStyleName(STYLE_VISIBLE);
		}
	}

	protected void toggleMenuVisibility() {
		Component compositionRoot = getCompositionRoot();
		if (compositionRoot.getStyleName().contains(STYLE_VISIBLE)) {
			compositionRoot.removeStyleName(STYLE_VISIBLE);
		} else {
			compositionRoot.addStyleName(STYLE_VISIBLE);
		}
	}

	private Component buildContent() {
		menuContent = new CssLayout();
		menuContent.addStyleName("sidebar");
		menuContent.addStyleName(ValoTheme.MENU_PART);
		menuContent.addStyleName("no-vertical-drag-hints");
		menuContent.addStyleName("no-horizontal-drag-hints");
		menuContent.setWidth(null);
		menuContent.setHeight("100%");

		menuContent.addComponent(buildMainMenu());
		menuContent.addComponent(buildUserMenu());

		return menuContent;
	}

	protected Component buildUserMenu() {
		userMenu = new BaseMenuBar();
		userMenu.addStyleName("user-menu");
		buildUserMenuItems(userMenu);
		return userMenu;
	}

	private Component buildMainMenu() {
		menuItemsLayout = new CssLayout();
		menuItemsLayout.addStyleName("valo-menuitems");
		menuItemsLayout.setHeight(100.0f, Unit.PERCENTAGE);

		mainMenuButtons = buildMainMenuButtons();

		for (ConstellioMenuButton mainMenuButton : mainMenuButtons) {
			Button menuButton = mainMenuButton.getButton();
			menuButton.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					toggleMenuVisibility();
				}
			});

			Class<? extends MenuViewGroup> menuViewGroupClass = mainMenuButton.getMenuViewGroup();

			Component mainMenuItemComponent = menuButton;
			mainMenuItemComponent.setPrimaryStyleName(ValoTheme.MENU_ITEM);
			if (DisabledMenuViewGroup.class.isAssignableFrom(menuViewGroupClass)) {
				menuButton.addStyleName("disabled");
			}

			Label badgeLabel = new Label("1");
			mainMenuItemComponent = buildBadgeWrapper(menuButton, badgeLabel);
			badgeLabels.put(mainMenuButton, badgeLabel);

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

					refreshBadge(mainMenuButton);

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

	private void refreshBadge(ConstellioMenuButton mainMenuButton) {
		String badge = mainMenuButton.getBadge();
		Label badgeLabel = badgeLabels.get(mainMenuButton);
		if (StringUtils.isNotBlank(badge)) {
			badgeLabel.setValue(badge);
			badgeLabel.setVisible(true);
		} else {
			badgeLabel.setVisible(false);
		}
	}

	@Override
	public void refreshBadges() {
		for (ConstellioMenuButton mainMenuButton : mainMenuButtons) {
			refreshBadge(mainMenuButton);
		}
	}

	protected void buildUserMenuItems(MenuBar userMenu) {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		UserVO currentUser = sessionContext.getCurrentUser();
		String firstName = currentUser.getFirstName();
		String lastName = currentUser.getLastName();

		//		if (currentUser.getEmail() != null && currentUser.getEmail().startsWith("elizabeth.madera")) {
		//			userSettingsItem = userMenu.addItem("", new ThemeResource("images/profiles/egg2.jpg"), null);
		//
		//		} else
		if (!presenter.hasCurrentUserPhoto()) {
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
		}).setStyleName("modify-profil-item");
		//		userSettingsItem.addItem($("ConstellioMenu.preferences"), new Command() {
		//			@Override
		//			public void menuSelected(final MenuItem selectedItem) {
		//				presenter.preferencesButtonClicked();
		//			}
		//		});
		final String collection = sessionContext.getCurrentCollection();
		for (String language : presenter.getCollectionLanguages(collection)) {
			userSettingsItem.addSeparator();
			userSettingsItem.addItem(language, new Command() {
				@Override
				public void menuSelected(final MenuItem selectedItem) {
					presenter.languageSelected(selectedItem.getText(), collection);
				}
			}).setStyleName("language-item-" + language);
		}

		userSettingsItem.addSeparator();

		userSettingsItem.addItem($("ConstellioMenu.signOut"), new Command() {
			@Override
			public void menuSelected(final MenuItem selectedItem) {
				presenter.signOutButtonClicked();
			}
		}).setStyleName("disconnect-item");
	}

	public MenuItem getUserSettingsItem() {
		return userSettingsItem;
	}

	protected List<ConstellioMenuButton> buildMainMenuButtons() {
		List<ConstellioMenuButton> mainMenuButtons = new ArrayList<>();
		return mainMenuButtons;
	}

	private Component buildBadgeWrapper(final Component menuItemButton, final Label badgeLabel) {
		CssLayout dashboardWrapper = new CssLayout(menuItemButton);
		dashboardWrapper.addStyleName("badgewrapper");
		dashboardWrapper.addStyleName(ValoTheme.MENU_ITEM);
		dashboardWrapper.setWidth(100.0f, Unit.PERCENTAGE);
		badgeLabel.addStyleName(ValoTheme.MENU_BADGE);
		badgeLabel.setWidthUndefined();
		if (StringUtils.isBlank(badgeLabel.getValue())) {
			badgeLabel.setVisible(false);
		}
		dashboardWrapper.addComponent(badgeLabel);
		return dashboardWrapper;
	}

	@Override
	public CoreViews navigateTo() {
		return new CoreViews(UI.getCurrent().getNavigator());
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

		public String getBadge() {
			return null;
		}

	}

}
