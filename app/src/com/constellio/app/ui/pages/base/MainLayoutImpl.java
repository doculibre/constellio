package com.constellio.app.ui.pages.base;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;

import elemental.json.JsonArray;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.api.extensions.params.PagesComponentsExtensionParams;
import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.modules.rm.ui.components.userDocument.UserDocumentsWindow;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.pages.base.ConstellioMenuImpl.ConstellioMenuButton;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.SingleComponentContainer;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/*
 * Dashboard MainView is a simple HorizontalLayout that wraps the menu on the
 * left and creates a simple container for the navigator on the right.
 */
@SuppressWarnings("serial")
@com.vaadin.annotations.JavaScript("theme://Constellio.js")
public class MainLayoutImpl extends VerticalLayout implements MainLayout {
	private MainLayoutPresenter presenter;

	private HorizontalLayout mainMenuContentFooterLayout;
	private CssLayout contentFooterWrapperLayout;
	private VerticalLayout contentFooterLayout;
	private ConstellioHeaderImpl header;
	private Component mainMenu;
	private SingleComponentContainer contentViewWrapper;
	private DragAndDropWrapper dragAndDropWrapper;
	private UserDocumentsWindow userDocumentsWindow;

	public MainLayoutImpl(AppLayerFactory appLayerFactory) {
		this.presenter = new MainLayoutPresenter(this);

		mainMenuContentFooterLayout = new HorizontalLayout();
		mainMenuContentFooterLayout.setSizeFull();
		mainMenuContentFooterLayout.addStyleName("main-menu-content-footer");

		contentViewWrapper = new Panel();

		Navigator navigator = new Navigator(UI.getCurrent(), contentViewWrapper);
		NavigatorConfigurationService navigatorConfigurationService = appLayerFactory.getNavigatorConfigurationService();
		navigatorConfigurationService.configure(navigator);
		UI.getCurrent().setNavigator(navigator);

		contentFooterWrapperLayout = new CssLayout();
		contentFooterWrapperLayout.setId("content-footer-wrapper");

		contentFooterLayout = new VerticalLayout();
		contentFooterLayout.addStyleName("content-footer");

		header = buildHeader();
		header.setSizeUndefined();

		mainMenu = buildMainMenu();

		userDocumentsWindow = new UserDocumentsWindow();
		dragAndDropWrapper = new DragAndDropWrapper(mainMenuContentFooterLayout);
		dragAndDropWrapper.setSizeFull();
		dragAndDropWrapper.setDropHandler(userDocumentsWindow);
		navigator.addViewChangeListener(new ViewChangeListener() {
			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {
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
				//				SerializationUtils.clone(event.getOldView());
				//				SerializationUtils.clone(newView);
			}
		});

		addComponent(header);
		addComponent(dragAndDropWrapper);
		setExpandRatio(dragAndDropWrapper, 1);

		mainMenuContentFooterLayout.addComponent(mainMenu);
		mainMenuContentFooterLayout.addComponent(contentFooterWrapperLayout);
		mainMenuContentFooterLayout.setExpandRatio(contentFooterWrapperLayout, 1);

		contentFooterWrapperLayout.addComponent(contentFooterLayout);

		contentFooterLayout.addComponent(contentViewWrapper);

		Component message = buildMessage();
		if (message != null) {
			message.addStyleName("message");
			contentFooterLayout.addComponent(message);
		}

		contentFooterLayout.setExpandRatio(contentViewWrapper, 1);

		Component footer = buildFooter();
		if (footer != null) {
			contentFooterLayout.addComponent(footer);
		}

		Component license = buildLicense();
		if (license != null) {
			license.addStyleName("license");
		}

		PagesComponentsExtensionParams params = new PagesComponentsExtensionParams(header, mainMenu, contentFooterLayout, this,
				contentViewWrapper, contentFooterWrapperLayout, presenter.getUser());
		appLayerFactory.getExtensions().getSystemWideExtensions().decorateView(params);
		String collection = ConstellioUI.getCurrentSessionContext().getCurrentCollection();
		if (collection != null) {
			appLayerFactory.getExtensions().forCollection(collection).decorateView(params);
		}

		buildInitJavascript();
	}

	protected ConstellioHeaderImpl buildHeader() {
		return new ConstellioHeaderImpl();
	}

	protected Component buildMainMenu() {
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

		for (NavigationItem item : presenter.getNavigationItems()) {
			mainMenuButtons.add(buildButton(item));
		}

		return mainMenuButtons;
	}

	private Component buildMessage() {
		String messageText = presenter.getMessage();
		if (StringUtils.isEmpty(messageText)) {
			return null;
		}
		Label message = new Label(messageText);
		message.addStyleName("footer-warning");
		message.addStyleName(ValoTheme.LABEL_LARGE);
		message.addStyleName(ValoTheme.LABEL_BOLD);
		return message;
	}

	protected Component buildFooter() {

		Link poweredByConstellioLink = new Link($("MainLayout.footerAlt") + "  (" + presenter.getCurrentVersion() + ")",
				new ExternalResource("http://www.constellio.com"));
		poweredByConstellioLink.setTargetName("_blank");
		poweredByConstellioLink.addStyleName(ValoTheme.LINK_LARGE);
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
		JavaScript.getCurrent().addFunction("constellio_easter_egg_code", new JavaScriptFunction() {
			@Override
			public void call(JsonArray arguments) {
				((ConstellioMenuImpl) mainMenu).getUserSettingsItem().setIcon(new ThemeResource("images/profiles/egg.jpg"));
			}
		});
	}

	private ConstellioMenuButton buildButton(final NavigationItem item) {
		ComponentState state = presenter.getStateFor(item);
		Button button = new Button($("MainLayout." + item.getCode()));
		button.setVisible(state.isVisible());
		button.setEnabled(state.isEnabled());
		button.addStyleName(item.getCode());
		button.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				item.activate(navigate());
			}
		});
		return new ConstellioMenuButton(item.getViewGroup(), button);
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

}
