package com.constellio.app.ui.pages.base;

import com.constellio.app.entities.system.SystemInfo;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.menuBar.BaseMenuBar;
import com.constellio.app.ui.pages.viewGroups.MenuViewGroup;
import com.constellio.app.ui.pages.viewGroups.MenuViewGroup.DisabledMenuViewGroup;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.FoldersLocatorMode;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
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
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
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
public class ConstellioMenuImpl extends CustomComponent implements ConstellioMenu, ViewChangeListener {

	public static final String ID = "dashboard-menu";
	private static final String STYLE_VISIBLE = "valo-menu-visible";
	public static final String STYLE_USER_SETTINGS = "user-settings";

	private ConstellioMenuPresenter presenter;

	private MenuItem userSettingsItem;

	private WindowButton systemStateButton;

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

	@Override
	public void attach() {
		super.attach();
		ConstellioUI.getCurrent().getNavigator().addViewChangeListener(this);
	}

	@Override
	public void detach() {
		ConstellioUI.getCurrent().getNavigator().removeViewChangeListener(this);
		super.detach();
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
		VerticalLayout menuContent = new VerticalLayout();
		menuContent.addStyleName("sidebar");
		menuContent.addStyleName(ValoTheme.MENU_PART);
		menuContent.addStyleName("no-vertical-drag-hints");
		menuContent.addStyleName("no-horizontal-drag-hints");
		menuContent.setWidth(null);
		menuContent.setHeight("100%");

		Component mainMenu = buildMainMenu();
		Component toggleButton = buildToggleButton();
		Component versionInfoLabel = buildVersionInfoComponent();
		Component userMenu = buildUserMenu();

		menuContent.addComponent(mainMenu);
		menuContent.addComponent(toggleButton);
		menuContent.addComponent(versionInfoLabel);
		menuContent.addComponent(userMenu);

		menuContent.setExpandRatio(mainMenu, 1);
		//		menuContent.setComponentAlignment(userMenu, Alignment.BOTTOM_CENTER);

		return menuContent;
	}

	protected Component buildToggleButton() {
		Button valoMenuToggleButton = new Button($("ConstellioMenu.menuToggle"), new ClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				toggleMenuVisibility();
			}
		});
		valoMenuToggleButton.setIcon(FontAwesome.LIST);
		valoMenuToggleButton.addStyleName("valo-menu-toggle");
		valoMenuToggleButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		valoMenuToggleButton.addStyleName(ValoTheme.BUTTON_SMALL);
		return valoMenuToggleButton;
	}

	protected Component buildVersionInfoComponent() {
		Link poweredByConstellioLink = new Link($("MainLayout.footerAlt") + "  (" + presenter.getCurrentVersion() + ")",
				new ExternalResource("http://www.constellio.com"));
		poweredByConstellioLink.addStyleName("valo-menu-constellio-version");
		poweredByConstellioLink.setTargetName("_blank");
		return poweredByConstellioLink;
	}

	protected Component buildUserMenu() {
		MenuBar userMenuBar = new BaseMenuBar();
		userMenuBar.addStyleName("user-menu");
		buildUserMenuItems(userMenuBar);
		return userMenuBar;
	}

	private WindowButton buildSystemStateButton() {
		systemStateButton = new WindowButton($("SystemInfo.systemStateButtonTitle"), $("SystemInfo.systemStateWindowTitle"), WindowConfiguration.modalDialog("75%", "75%")) {
			@Override
			protected Component buildWindowContent() {
				VerticalLayout mainLayout = new VerticalLayout();
				mainLayout.addStyleName("system-state-window-layout");

				SystemInfo systemInfo = SystemInfo.getInstance();
				Label updateTimeLabel = new Label($("SystemInfo.lastTimeUpdated", systemInfo.getLastTimeUpdated().toString("HH:mm:ss")));
				updateTimeLabel.addStyleName("system-state-window-update-time");
				mainLayout.addComponent(updateTimeLabel);
				mainLayout.setComponentAlignment(updateTimeLabel, Alignment.TOP_LEFT);

				ValidationErrors validationErrors = systemInfo.getValidationErrors();

				if (!validationErrors.getValidationErrors().isEmpty()) {
					mainLayout.addComponent(buildStatesComponent("errors", validationErrors.getValidationErrors()));
				}

				if (!validationErrors.getValidationWarnings().isEmpty()) {
					mainLayout.addComponent(buildStatesComponent("warnings", validationErrors.getValidationWarnings()));
				}

				if (!validationErrors.getValidationLogs().isEmpty()) {
					mainLayout.addComponent(buildStatesComponent("logs", validationErrors.getValidationLogs()));
				}

				Component systemStateWarningMessage = buildSystemsStateImportantMessage();
				mainLayout.addComponent(systemStateWarningMessage);
				mainLayout.setComponentAlignment(systemStateWarningMessage, Alignment.BOTTOM_CENTER);
				return mainLayout;
			}

			private Component buildStatesComponent(String criticity, List<ValidationError> validationErrors) {
				VerticalLayout mainLayout = new VerticalLayout();
				mainLayout.addStyleName("system-state-component");
				mainLayout.addStyleName("system-state-component-" + criticity);
				mainLayout.setCaption($("SystemInfo." + criticity));

				for (ValidationError error : validationErrors) {
					Label validationErrorLabel = new Label($(error));
					validationErrorLabel.addStyleName("system-state-component-validation-error");
					mainLayout.addComponent(validationErrorLabel);
				}
				return mainLayout;
			}
		};
		systemStateButton.setPrimaryStyleName(ValoTheme.MENU_ITEM);
		//		systemStateButton.addStyleName(ValoTheme.BUTTON_TINY);
		//		systemStateButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		systemStateButton.addStyleName("constellio-menu-system-state-button");
		systemStateButton.setVisible(presenter.hasUserRightToViewSystemState());
		refreshSystemStateButton();
		return systemStateButton;
	}

	private Component buildMainMenu() {
		CssLayout menuItemsLayout = new CssLayout();
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

		systemStateButton = buildSystemStateButton();
		menuItemsLayout.addComponent(systemStateButton);

		return menuItemsLayout;
	}

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
		refreshSystemStateButton();
		return true;
	}

	@Override
	public void afterViewChange(ViewChangeEvent event) {
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

	private void refreshSystemStateButton() {
		if (new FoldersLocator().getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER) {
			ValidationErrors validationErrors = SystemInfo.getInstance().getValidationErrors();
			if (!validationErrors.isEmpty() || buildSystemsStateImportantMessage().isVisible()) {
				systemStateButton.setIcon(new ThemeResource("images/commun/error.gif"));
			} else if (!validationErrors.isEmptyErrorAndWarnings()) {
				systemStateButton.setIcon(new ThemeResource("images/commun/warning.png"));
			} else {
				systemStateButton.setIcon(new ThemeResource("images/commun/greenCircle.png"));
			}
		} else if (buildSystemsStateImportantMessage().isVisible()) {
			systemStateButton.setIcon(new ThemeResource("images/commun/error.gif"));
		} else {
			systemStateButton.setIcon(new ThemeResource("images/commun/greenCircle.png"));
		}
	}

	private Component buildSystemsStateImportantMessage() {
		String messageText = presenter.getSystemStateImportantMessage();
		Label message = new Label(messageText);
		message.addStyleName("system-state-component-important-message");
		message.setVisible(StringUtils.isNotEmpty(messageText));
		return message;
	}

	@Override
	public void refreshBadges() {
		for (ConstellioMenuButton mainMenuButton : mainMenuButtons) {
			refreshBadge(mainMenuButton);
		}
	}

	protected void buildUserMenuItems(MenuBar userMenu) {
		userMenu.setHtmlContentAllowed(true);

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
		userSettingsItem.setText("<span class=\"user-caption\">" + firstName + " " + lastName + "</span>");
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
		for (String language : presenter.getCollectionLanguagesExceptCurrent(collection)) {
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
		CssLayout badgeWrapper = new CssLayout(menuItemButton);
		badgeWrapper.addStyleName("badgewrapper");
		badgeWrapper.addStyleName(ValoTheme.MENU_ITEM);
		badgeWrapper.setWidth(100.0f, Unit.PERCENTAGE);
		badgeLabel.addStyleName(ValoTheme.MENU_BADGE);
		badgeLabel.setWidthUndefined();
		if (StringUtils.isBlank(badgeLabel.getValue())) {
			badgeLabel.setVisible(false);
		}
		badgeWrapper.addComponent(badgeLabel);
		return badgeWrapper;
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
