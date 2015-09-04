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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.constellio.app.api.extensions.PagesComponentsExtensions;
import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.modules.rm.ui.components.userDocument.UserDocumentsWindow;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.pages.base.ConstellioMenuImpl.ConstellioMenuButton;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.constellio.data.utils.Factory;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Responsive;
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
@com.vaadin.annotations.JavaScript("Constellio.js")
public class MainLayoutImpl extends VerticalLayout implements MainLayout {

	private MainLayoutPresenter presenter;

	private HorizontalLayout mainMenuContentFooterLayout;
	private CssLayout contentFooterWrapperLayout;
	private VerticalLayout contentFooterLayout;
	private ConstellioHeaderImpl header;
	private Component mainMenu;
	private SingleComponentContainer contentViewWrapper;
	private Component footer;
	private Component license;

	private DragAndDropWrapper dragAndDropWrapper;
	private UserDocumentsWindow userDocumentsWindow;

	public MainLayoutImpl(AppLayerFactory appLayerFactory) {
		this.presenter = new MainLayoutPresenter(this);

		setSizeFull();
		addStyleName("constellio-layout");

		mainMenuContentFooterLayout = new HorizontalLayout();
		mainMenuContentFooterLayout.setSizeFull();
		mainMenuContentFooterLayout.addStyleName("main-menu-content-footer");

		contentViewWrapper = new Panel();
		contentViewWrapper.addStyleName("content-view-wrapper");
		contentViewWrapper.setSizeFull();

		Navigator navigator = new Navigator(UI.getCurrent(), contentViewWrapper);
		NavigatorConfigurationService navigatorConfigurationService = appLayerFactory.getNavigatorConfigurationService();
		navigatorConfigurationService.configure(navigator);
		UI.getCurrent().setNavigator(navigator);

		contentFooterWrapperLayout = new CssLayout();
		contentFooterWrapperLayout.addStyleName("content-footer-wrapper");
		contentFooterWrapperLayout.setId("content-footer-wrapper");
		contentFooterWrapperLayout.setSizeFull();
		Responsive.makeResponsive(contentFooterWrapperLayout);

		contentFooterLayout = new VerticalLayout();
		contentFooterLayout.addStyleName("content-footer");

		header = buildHeader();
		header.addStyleName("header");
		header.setSizeUndefined();

		mainMenu = buildMainMenu();
		mainMenu.addStyleName("main-menu");

		footer = buildFooter();
		if (footer != null) {
			footer.addStyleName("footer");
		}

		license = buildLicense();
		if (license != null) {
			license.addStyleName("license");
		}

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
		if (footer != null) {
			contentFooterLayout.addComponent(footer);
		}
		if (license != null) {
			contentFooterLayout.addComponent(license);
		}
		contentFooterLayout.setExpandRatio(contentViewWrapper, 1);

		buildInitJavascript();

		presenter.viewAssembled();
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

		for (NavigationItem item : presenter.getNavigationIems()) {
			mainMenuButtons.add(buildButton(item));
		}

		return mainMenuButtons;
	}

	protected Component buildFooter() {

		PagesComponentsExtensions extensions = presenter.getPagesComponentsExtensions();
		Factory<Component> footerFactory = extensions.getFooterComponentFactory();

		if (footerFactory == null) {

			Link poweredByConstellioLink = new Link($("MainLayout.footerAlt") + "  (" + presenter.getCurrentVersion() + ")",
					new ExternalResource("http://www.constellio.com"));
			poweredByConstellioLink.setTargetName("_blank");
			poweredByConstellioLink.addStyleName(ValoTheme.LINK_LARGE);
			return poweredByConstellioLink;
		} else {
			return footerFactory.get();
		}
	}

	protected Component buildLicense() {

		PagesComponentsExtensions extensions = presenter.getPagesComponentsExtensions();
		Factory<Component> licenseFactory = extensions.getLicenseComponentFactory();

		if (licenseFactory == null) {

			Label licenseLabel = new Label($("MainLayout.footerLicense"));
			licenseLabel.addStyleName(ValoTheme.LABEL_TINY);
			licenseLabel.setContentMode(ContentMode.HTML);
			licenseLabel.setVisible(presenter.t());
			return licenseLabel;
		} else {
			return licenseFactory.get();
		}
	}

	protected void buildInitJavascript() {
		JavaScript.getCurrent().addFunction("constellio_easter_egg_code", new JavaScriptFunction() {
			@Override
			public void call(JSONArray arguments)
					throws JSONException {
				((ConstellioMenuImpl) mainMenu).getUserSettingsItem().setIcon(new ThemeResource("images/profiles/egg.jpg"));
			}
		});
		//		JavaScript.getCurrent()
		//				.execute("constellio_registerKeyDownListener(\"" + contentFooterWrapperLayout.getId() + "\")");
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
				item.activate(navigateTo());
			}
		});
		return new ConstellioMenuButton(item.getViewGroup(), button);
	}

	@Override
	public ConstellioNavigator navigateTo() {
		return new ConstellioNavigator(getUI().getNavigator());
	}

	@Override
	public ConstellioHeaderImpl getHeader() {
		return header;
	}

}
