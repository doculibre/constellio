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
package com.constellio.app.ui.application;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.rm.ui.contextmenu.RMRecordContextMenuHandler;
import com.constellio.app.modules.rm.ui.navigation.RMRecordNavigationHandler;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.contextmenu.RecordContextMenuHandler;
import com.constellio.app.ui.framework.navigation.RecordNavigationHandler;
import com.constellio.app.ui.handlers.ConstellioErrorHandler;
import com.constellio.app.ui.pages.base.ConstellioHeader;
import com.constellio.app.ui.pages.base.EnterViewListener;
import com.constellio.app.ui.pages.base.InitUIListener;
import com.constellio.app.ui.pages.base.MainLayoutImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.VaadinSessionContext;
import com.constellio.app.ui.pages.login.LoginViewImpl;
import com.constellio.app.utils.ConstellioSerializationUtils;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
@Theme("constellio")
public class ConstellioUI extends UI {

	private static Logger LOGGER = LoggerFactory.getLogger(ConstellioUI.class);

	private SessionContext sessionContext;
	private MainLayoutImpl mainLayout;

	private List<RecordNavigationHandler> recordNavigationHandlers = new ArrayList<>();

	private List<RecordContextMenuHandler> recordContextMenuHandlers = new ArrayList<>();

	@Override
	protected void init(VaadinRequest request) {
		// Important to allow update of components in current UI from another Thread
		UI.getCurrent().setPollInterval(1000);
		
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();

		// TODO instantiate in the RM layer
		addRecordNavigationHandler(new RMRecordNavigationHandler(constellioFactories));
		addRecordContextMenuHandler(new RMRecordContextMenuHandler(constellioFactories));

		List<InitUIListener> initUIListeners = appLayerFactory.getInitUIListeners();
		for (InitUIListener initUIListener : initUIListeners) {
			initUIListener.beforeInitialize(this);
		}

		Responsive.makeResponsive(this);
		addStyleName(ValoTheme.UI_WITH_MENU);

		// Some views need to be aware of browser resize events so a
		// BrowserResizeEvent gets fired to the event but on every occasion.
		Page.getCurrent().addBrowserWindowResizeListener(
				new BrowserWindowResizeListener() {
					@Override
					public void browserWindowResized(final BrowserWindowResizeEvent event) {
					}
				});

		if (getErrorHandler() == null) {
			setErrorHandler(new ConstellioErrorHandler());
		}

		if (sessionContext == null) {
			sessionContext = new VaadinSessionContext();
		}

		updateContent();

		for (InitUIListener initUIListener : initUIListeners) {
			initUIListener.afterInitialize(this);
		}
	}

	public void updateContent() {
		UserVO currentUser = sessionContext.getCurrentUser();
		if (currentUser != null) {
			// Authenticated user
			ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
			AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();

			mainLayout = new MainLayoutImpl(appLayerFactory);

			setContent(mainLayout);

			Navigator navigator = getNavigator();
			navigator.addViewChangeListener(new ViewChangeListener() {
				@Override
				public boolean beforeViewChange(ViewChangeEvent event) {
					return true;
				}

				@Override
				public void afterViewChange(ViewChangeEvent event) {
					View newView = event.getNewView();
					ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
					AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
					List<EnterViewListener> enterViewListeners = appLayerFactory.getEnterViewListeners();
					for (EnterViewListener enterViewListener : enterViewListeners) {
						enterViewListener.enterView(newView);
					}

					if (enterViewListeners.isEmpty() && !isProductionMode()) {
						try {
							ConstellioSerializationUtils.validateSerializable(event.getOldView());
						} catch (Exception e) {
							LOGGER.warn(e.getMessage(), e);
						}
						try {
							ConstellioSerializationUtils.validateSerializable(event.getNewView());
						} catch (Exception e) {
							LOGGER.warn(e.getMessage(), e);
						}
					}
				}
			});

			removeStyleName("loginview");
			navigator.navigateTo(navigator.getState());
		} else {
			LoginViewImpl loginView = new LoginViewImpl();
			setContent(loginView);
			addStyleName("loginview");
		}
	}

	public ConstellioHeader getHeader() {
		return mainLayout.getHeader();
	}

	public SessionContext getSessionContext() {
		return sessionContext;
	}

	public void setSessionContext(SessionContext sessionContext) {
		this.sessionContext = sessionContext;
	}

	public static SessionContext getCurrentSessionContext() {
		return getCurrent().getSessionContext();
	}

	public boolean isProductionMode() {
		VaadinService service = VaadinService.getCurrent();
		return service.getDeploymentConfiguration().isProductionMode();
	}

	public ConstellioNavigator navigateTo() {
		return new ConstellioNavigator(getNavigator());
	}

	public void addRecordNavigationHandler(RecordNavigationHandler recordNavigationHandler) {
		this.recordNavigationHandlers.add(recordNavigationHandler);
	}

	public void removeRecordNavigationHandler(RecordNavigationHandler recordNavigationHandler) {
		this.recordNavigationHandlers.remove(recordNavigationHandler);
	}

	public List<RecordNavigationHandler> getRecordNavigationHandlers() {
		return recordNavigationHandlers;
	}

	public void addRecordContextMenuHandler(RecordContextMenuHandler recordContextMenuHandler) {
		this.recordContextMenuHandlers.add(recordContextMenuHandler);
	}

	public void removeRecordContextMenuHandler(RecordContextMenuHandler recordContextMenuHandler) {
		this.recordContextMenuHandlers.remove(recordContextMenuHandler);
	}

	public List<RecordContextMenuHandler> getRecordContextMenuHandlers() {
		return recordContextMenuHandlers;
	}

	public static ConstellioUI getCurrent() {
		return (ConstellioUI) UI.getCurrent();
	}

}
