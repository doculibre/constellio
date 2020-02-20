package com.constellio.app.ui.pages.base;

import com.constellio.app.api.extensions.params.PagesComponentsExtensionParams;
import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.modules.rm.ui.components.userDocument.UserDocumentsWindow;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.GuideConfigButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.mouseover.NiceTitle;
import com.constellio.app.ui.pages.base.ConstellioMenuImpl.ConstellioMenuButton;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.constellio.app.ui.util.ResponsiveUtils;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;
import elemental.json.JsonArray;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

/*
 * Dashboard MainView is a simple HorizontalLayout that wraps the menu on the
 * left and creates a simple container for the navigator on the right.
 */
@SuppressWarnings("serial")
@com.vaadin.annotations.JavaScript("theme://Constellio.js")
public class MainLayoutImpl extends VerticalLayout implements MainLayout {
	private MainLayoutPresenter presenter;

	private I18NHorizontalLayout mainMenuContentFooterLayout;
	private CssLayout contentFooterWrapperLayout;
	private VerticalLayout contentFooterLayout;
	private VerticalLayout footerLayout;
	private ConstellioHeaderImpl header;
	private ConstellioMenuImpl mainMenu;
	private SingleComponentContainer contentViewWrapper;
	private DragAndDropWrapper dragAndDropWrapper;
	private UserDocumentsWindow userDocumentsWindow;
	private List<NavigationItem> navigationItems;
	private VerticalLayout contentAndStaticFooterLayout;

	private VerticalLayout staticFooterLayout;
	private I18NHorizontalLayout staticFooterContentAndGuideLayout;
	private I18NHorizontalLayout staticFooterExtraComponentsLayout;
	private Component staticFooterContent;
	private BaseButton guideButton;
	private WindowButton guideButtonConfig;


	public MainLayoutImpl(final AppLayerFactory appLayerFactory) {
		this.presenter = new MainLayoutPresenter(this);

		addStyleName("main-layout");

		mainMenuContentFooterLayout = new I18NHorizontalLayout();
		mainMenuContentFooterLayout.setSizeFull();
		mainMenuContentFooterLayout.addStyleName("main-menu-content-footer");

		contentViewWrapper = new Panel();
		contentViewWrapper.addStyleName(ValoTheme.PANEL_BORDERLESS);

		Navigator navigator = new Navigator(UI.getCurrent(), contentViewWrapper);
		NavigatorConfigurationService navigatorConfigurationService = appLayerFactory.getNavigatorConfigurationService();
		navigatorConfigurationService.configure(navigator);
		UI.getCurrent().setNavigator(navigator);

		contentFooterWrapperLayout = new CssLayout();
		contentFooterWrapperLayout.setId("content-footer-wrapper");

		contentFooterLayout = new VerticalLayout();
		contentFooterLayout.addStyleName("content-footer");

		footerLayout = new VerticalLayout();
		footerLayout.setId("footer-layout");
		footerLayout.addStyleName(footerLayout.getId());
		footerLayout.setVisible(false);

		staticFooterLayout = new VerticalLayout();
		staticFooterLayout.addStyleName("static-footer-layout");
		staticFooterLayout.setWidth("100%");
		staticFooterLayout.setHeight("76px");
		staticFooterLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

		staticFooterContentAndGuideLayout = new I18NHorizontalLayout();
		staticFooterContentAndGuideLayout.addStyleName("static-footer-content-and-guide-layout");
		staticFooterContentAndGuideLayout.setWidth("100%");
		staticFooterContentAndGuideLayout.setSpacing(true);

		header = buildHeader();
		header.setWidth("100%");
		header.setHeight("63px");

		mainMenu = buildMainMenu();

		userDocumentsWindow = new UserDocumentsWindow();
		dragAndDropWrapper = new DragAndDropWrapper(mainMenuContentFooterLayout) {
			@Override
			public void setDropHandler(DropHandler dropHandler) {
				if (ResponsiveUtils.isDesktop()) {
					super.setDropHandler(dropHandler);
				}
			}
		};
		dragAndDropWrapper.setSizeFull();
		dragAndDropWrapper.setDropHandler(userDocumentsWindow);

		guideButton = new BaseButton($("guide"), new ThemeResource("images/icons/about.png")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				BaseViewImpl view = (BaseViewImpl) ConstellioUI.getCurrent().getViewChangeEvent().getNewView();
				String guideUrl = view.getGuideUrl();
				Page.getCurrent().open(guideUrl, "_blank", false);
			}
		};
		guideButton.addStyleName(ValoTheme.BUTTON_LINK);
		guideButton.addStyleName("guide-button");
		guideButton.setVisible(false);
		guideButton.addExtension(new NiceTitle($("guide.details")));

		guideButtonConfig = new GuideConfigButton("guideconfig",
				"configurer les liens de doc pour la page " + UI.getCurrent().getPage().getUriFragment(),
				WindowConfiguration.modalDialog("600px",
						"300px"), appLayerFactory);



		addComponent(header);
		addComponent(dragAndDropWrapper);
		setExpandRatio(dragAndDropWrapper, 1);

		contentFooterLayout.addComponent(contentViewWrapper);
		contentFooterLayout.addComponent(footerLayout);
		contentFooterLayout.setExpandRatio(contentViewWrapper, 1);
		contentFooterWrapperLayout.addComponent(contentFooterLayout);

		contentAndStaticFooterLayout = new VerticalLayout(contentFooterWrapperLayout, staticFooterLayout);
		contentAndStaticFooterLayout.addStyleName("content-and-static-footer-layout");
		contentAndStaticFooterLayout.setSizeFull();
		contentAndStaticFooterLayout.setExpandRatio(contentFooterWrapperLayout, 1);

		mainMenuContentFooterLayout.addComponent(mainMenu);
		mainMenuContentFooterLayout.addComponent(contentAndStaticFooterLayout);
		mainMenuContentFooterLayout.setExpandRatio(contentAndStaticFooterLayout, 1);

		staticFooterExtraComponentsLayout = new I18NHorizontalLayout();
		staticFooterExtraComponentsLayout.addStyleName("static-footer-extra-components-layout");
		staticFooterExtraComponentsLayout.setWidth("100%");
		staticFooterExtraComponentsLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

		staticFooterContentAndGuideLayout.addComponent(guideButton);
		staticFooterContentAndGuideLayout.setComponentAlignment(guideButton, Alignment.MIDDLE_RIGHT);

		staticFooterContentAndGuideLayout.addComponent(guideButtonConfig);
		staticFooterContentAndGuideLayout.setComponentAlignment(guideButtonConfig, Alignment.MIDDLE_RIGHT);

		PagesComponentsExtensionParams params = new PagesComponentsExtensionParams(header, mainMenu, staticFooterExtraComponentsLayout, this,
				contentViewWrapper, contentFooterWrapperLayout, presenter.getUser());
		appLayerFactory.getExtensions().getSystemWideExtensions().decorateView(params);
		String collection = ConstellioUI.getCurrentSessionContext().getCurrentCollection();
		if (collection != null) {
			appLayerFactory.getExtensions().forCollection(collection).decorateView(params);
		}

		staticFooterLayout.addComponent(staticFooterContentAndGuideLayout);
		if (!isStaticFooterExtraComponentsLayoutEmpty()) {
			staticFooterLayout.addComponent(staticFooterExtraComponentsLayout);
			staticFooterLayout.setComponentAlignment(staticFooterExtraComponentsLayout, Alignment.BOTTOM_CENTER);
		}
		if (staticFooterContent != null) {
			setStaticFooterContent(staticFooterContent);
		}
		updateStaticFooterState();

		buildInitJavascript();

		navigator.addViewChangeListener(new ViewChangeListener() {
			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {
				setStaticFooterContent(null);
				return true;
			}

			@Override
			public void afterViewChange(ViewChangeEvent event) {
				View newView = event.getNewView();
				if (newView instanceof NoDragAndDrop) {
					dragAndDropWrapper.setDropHandler(null);
				} else if (newView instanceof DropHandler) {
					dragAndDropWrapper.setDropHandler((DropHandler) newView);
				} else {
					List<DropHandler> viewDropHandlers = ComponentTreeUtils.getChildren((Component) newView, DropHandler.class);
					if (viewDropHandlers.size() > 1) {
						dragAndDropWrapper.setDropHandler(null);
					} else if (viewDropHandlers.size() == 1) {
						dragAndDropWrapper.setDropHandler(viewDropHandlers.get(0));
					} else if (dragAndDropWrapper.getDropHandler() != userDocumentsWindow) {
						dragAndDropWrapper.setDropHandler(userDocumentsWindow);
					}
				}
				updateHelpButtonState((BaseViewImpl) newView);
				updateStaticFooterState();
			}
		});
	}

	public Component getStaticFooterContent() {
		return staticFooterContent;
	}

	public void setStaticFooterContent(Component component) {
		if (staticFooterContent != null) {
			if (component == null) {
				staticFooterContentAndGuideLayout.removeComponent(staticFooterContent);
				staticFooterContent = null;
			} else {
				staticFooterContentAndGuideLayout.replaceComponent(staticFooterContent, staticFooterContent = component);
				staticFooterContentAndGuideLayout.setComponentAlignment(staticFooterContent, Alignment.MIDDLE_CENTER);
				staticFooterContentAndGuideLayout.setExpandRatio(staticFooterContent, 1);
			}
		} else if (component != null) {
			staticFooterContentAndGuideLayout.addComponent(staticFooterContent = component, 0);
			staticFooterContentAndGuideLayout.setComponentAlignment(staticFooterContent, Alignment.MIDDLE_CENTER);
			staticFooterContentAndGuideLayout.setExpandRatio(staticFooterContent, 1);
		} else {
			staticFooterContent = null;
		}
	}

	private boolean isStaticFooterEmpty() {
		boolean staticFooterEmpty;
		if (staticFooterContent == null && !guideButton.isVisible() && (!ResponsiveUtils.isDesktop() || isStaticFooterExtraComponentsLayoutEmpty())) {
			staticFooterEmpty = true;
		} else {
			staticFooterEmpty = false;
		}
		return staticFooterEmpty;
	}

	private boolean isStaticFooterExtraComponentsLayoutEmpty() {
		boolean staticFooterExtraComponentsLayoutEmpty;
		if (staticFooterExtraComponentsLayout.getComponentCount() == 0) {
			staticFooterExtraComponentsLayoutEmpty = true;
		} else {
			staticFooterExtraComponentsLayoutEmpty = true;
			for (int i = 0; i < staticFooterExtraComponentsLayout.getComponentCount(); i++) {
				Component staticFooterExtraComponentsLayoutComponent = staticFooterExtraComponentsLayout.getComponent(i);
				if (staticFooterExtraComponentsLayoutComponent.isVisible()) {
					staticFooterExtraComponentsLayoutEmpty = false;
					break;
				}
			}
		}
		return staticFooterExtraComponentsLayoutEmpty;
	}

	private void updateHelpButtonState(BaseViewImpl view) {
		String guideUrl = view.getGuideUrl();
		boolean guideButtonVisible = StringUtils.isNotBlank(guideUrl);
		guideButton.setVisible(guideButtonVisible);
		guideButtonConfig.setVisible(guideButtonVisible); // && userHasCorrectRole()
	}

	private void updateStaticFooterState() {
		boolean staticFooterEmpty = isStaticFooterEmpty();
		if (!staticFooterLayout.isVisible() && !staticFooterEmpty) {
			staticFooterLayout.setVisible(true);
		} else if (staticFooterLayout.isVisible() && staticFooterEmpty) {
			staticFooterLayout.setVisible(false);
		}
	}

	protected ConstellioHeaderImpl buildHeader() {
		return new ConstellioHeaderImpl();
	}

	protected ConstellioMenuImpl buildMainMenu() {
		ConstellioMenuImpl mainMenu = new ConstellioMenuImpl() {
			@Override
			protected List<ConstellioMenuButton> buildMainMenuButtons() {
				return MainLayoutImpl.this.buildMainMenuButtons();
			}
		};
		mainMenu.setHeight("100%");
		return mainMenu;
	}

	protected List<ConstellioMenuButton> buildMainMenuButtons() {
		List<ConstellioMenuButton> mainMenuButtons = new ArrayList<>();

		navigationItems = presenter.getNavigationItems();
		final Map<NavigationItem, ConstellioMenuButton> menuButtons = new HashMap<>();
		for (NavigationItem item : navigationItems) {
			ConstellioMenuButton menuButton = buildButton(item);
			mainMenuButtons.add(menuButton);
			menuButtons.put(item, menuButton);
		}
		ConstellioUI.getCurrent().getNavigator().addViewChangeListener(new ViewChangeListener() {
			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {
				return true;
			}

			@Override
			public void afterViewChange(ViewChangeEvent event) {
				View oldView = event.getOldView();
				View newView = event.getNewView();
				if (oldView instanceof BaseView && newView instanceof BaseView) {
					for (NavigationItem item : navigationItems) {
						ConstellioMenuButton menuButton = menuButtons.get(item);
						item.viewChanged((BaseView) oldView, (BaseView) newView);
						updateMenuButton(item, menuButton);
					}
				}
			}
		});

		return mainMenuButtons;
	}

	protected Component buildInstanceType(boolean isDistributed) {
		Link poweredByConstellioLink = new Link($("MainLayout.distributed." + isDistributed), null);
		poweredByConstellioLink.setTargetName("_blank");
		poweredByConstellioLink.addStyleName("footer");
		return poweredByConstellioLink;
	}

	protected Component buildLicense() {
		boolean showFooter = !"true".equals(System.getProperty("no_footer_message"));
		Label licenseLabel = new Label($("MainLayout.footerLicense"));
		licenseLabel.addStyleName(ValoTheme.LABEL_TINY);
		licenseLabel.setContentMode(ContentMode.HTML);
		licenseLabel.setVisible(showFooter);
		return licenseLabel;
	}

	protected void buildInitJavascript() {
		JavaScript.getCurrent().execute("constellio_registerScrollListener();");
		JavaScript.getCurrent().addFunction("constellio_easter_egg_code", new JavaScriptFunction() {
			@Override
			public void call(JsonArray arguments) {
				((ConstellioMenuImpl) mainMenu).getUserSettingsItem().setIcon(new ThemeResource("images/profiles/egg.jpg"));
			}
		});
	}

	private ConstellioMenuButton buildButton(final NavigationItem navigationItem) {
		Button button = new Button();
		if (navigationItem.getFontAwesome() != null) {
			button.setIcon(navigationItem.getFontAwesome());
		}
		button.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				navigationItem.activate(navigate());
			}
		});
		ConstellioMenuButton constellioMenuButton = new ConstellioMenuButton(navigationItem.getViewGroup(), button) {
			@Override
			public String getBadge() {
				return presenter.getBadge(navigationItem);
			}
		};
		updateMenuButton(navigationItem, constellioMenuButton);
		return constellioMenuButton;
	}

	private void updateMenuButton(NavigationItem navigationItem, ConstellioMenuButton constellioMenuButton) {
		Button button = constellioMenuButton.getButton();
		button.setCaption($("MainLayout." + navigationItem.getCode()));
		ComponentState state = presenter.getStateFor(navigationItem);
		button.setVisible(state.isVisible());
		button.setEnabled(state.isEnabled());
	}

	@Override
	public CoreViews navigateTo() {
		return ConstellioUI.getCurrent().navigateTo();
	}

	@Override
	public Navigation navigate() {
		return ConstellioUI.getCurrent().navigate();
	}

	@Override
	public ConstellioHeaderImpl getHeader() {
		return header;
	}

	@Override
	public ConstellioMenu getMenu() {
		return mainMenu;
	}

}
