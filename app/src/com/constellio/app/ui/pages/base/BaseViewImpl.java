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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.buttons.BackButton;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.pages.home.HomeViewImpl;
import com.vaadin.event.UIEvents.PollEvent;
import com.vaadin.event.UIEvents.PollListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public abstract class BaseViewImpl extends VerticalLayout implements View, BaseView, PollListener {

	private static Logger LOGGER = LoggerFactory.getLogger(BaseViewImpl.class);

	public static final String BACK_BUTTON_CODE = "seleniumBackButtonCode";
	
	private BaseBreadcrumbTrail breadcrumbTrail;

	private Label titleLabel;

	private BackButton backButton;

	private HorizontalLayout titleBackButtonLayout;

	private Component mainComponent;
	private Component actionMenu;

	private List<ViewEnterListener> viewEnterListeners = new ArrayList<>();

	@Override
	public final void enter(ViewChangeEvent event) {
		for (ViewEnterListener viewEnterListener : viewEnterListeners) {
			viewEnterListener.viewEntered(event.getParameters());
		}

		//if (!(this instanceof RecordsManagementViewImpl)) {
		try {
			initBeforeCreateComponents(event);
		} catch (Exception e) {
			e.printStackTrace();

			LOGGER.error(e.getMessage(), e);
			// TODO Obtain home without hard-coding the class
			if (!(this instanceof HomeViewImpl)) {
				navigateTo().home();
			}
			return;
		}
		//}

		for (ViewEnterListener viewEnterListener : viewEnterListeners) {
			viewEnterListener.afterInit(event.getParameters());
		}

		addStyleName("main-component-wrapper");
		setSizeFull();

		removeAllComponents();
		
		breadcrumbTrail = buildBreadcrumbTrail();

		titleBackButtonLayout = new HorizontalLayout();
		titleBackButtonLayout.setWidth("100%");

		String title = getTitle();
		if (title != null) {
			titleLabel = new Label(title);
			titleLabel.addStyleName(ValoTheme.LABEL_H1);
		}

		backButton = new BackButton();
		ClickListener backButtonClickListener = getBackButtonClickListener();
		if (backButtonClickListener != null) {
			backButton.setVisible(true);
			backButton.addStyleName(BACK_BUTTON_CODE);
			backButton.addClickListener(backButtonClickListener);
		} else {
			backButton.setVisible(false);
		}

		mainComponent = buildMainComponent(event);
		mainComponent.addStyleName("main-component");

		actionMenu = buildActionMenu(event);
		if (actionMenu != null || !isFullWidthIfActionMenuAbsent()) {
			addStyleName("action-menu-wrapper");
			if (actionMenu != null) {
				actionMenu.addStyleName("action-menu");
			}
		}
		
		if (titleLabel != null || backButton.isVisible()) {
			addComponent(titleBackButtonLayout);
		}

		if (breadcrumbTrail != null) {
			addComponent(breadcrumbTrail);
		}

		addComponent(mainComponent);
		if (actionMenu != null) {
			addComponent(actionMenu);
		}

		if (titleLabel != null || backButton.isVisible()) {
			if (titleLabel != null) {
				titleBackButtonLayout.addComponents(titleLabel);
			}
			titleBackButtonLayout.addComponents(backButton);
		}

		setExpandRatio(mainComponent, 1f);
		if (titleLabel != null) {
			titleBackButtonLayout.setExpandRatio(titleLabel, 1);
		}

		if (isBackgroundViewMonitor()) {
			addBackgroundViewMonitor();
		}

		afterViewAssembled(event);
	}
	
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return null;
	}

	protected boolean isBackgroundViewMonitor() {
		return false;
	}

	protected void onBackgroundViewMonitor() {
	}

	protected void addBackgroundViewMonitor() {
		UI.getCurrent().addPollListener(this);
	}

	@Override
	public void poll(PollEvent event) {
		onBackgroundViewMonitor();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		if (isBackgroundViewMonitor()) {
			UI.getCurrent().addPollListener(this);
		}
	}

	private void writeObject(ObjectOutputStream out)
			throws IOException {
		if (isBackgroundViewMonitor()) {
			UI.getCurrent().removePollListener(this);
		}
		out.defaultWriteObject();
	}

	@Override
	public void addViewEnterListener(ViewEnterListener listener) {
		viewEnterListeners.add(listener);
	}

	@Override
	public List<ViewEnterListener> getViewEnterListeners() {
		return viewEnterListeners;
	}

	@Override
	public void removeViewEnterListener(ViewEnterListener listener) {
		viewEnterListeners.remove(listener);
	}

	protected void initBeforeCreateComponents(ViewChangeEvent event) {
	}

	protected void afterViewAssembled(ViewChangeEvent event) {
	}

	protected boolean isFullWidthIfActionMenuAbsent() {
		return false;
	}

	protected String getTitle() {
		return getClass().getSimpleName();
	}

	/**
	 * Adapted from https://vaadin.com/forum#!/thread/8150555/8171634
	 *
	 * @param event
	 * @return
	 */
	protected Component buildActionMenu(ViewChangeEvent event) {
		VerticalLayout actionMenuLayout;
		List<Button> actionMenuButtons = buildActionMenuButtons(event);
		if (actionMenuButtons == null || actionMenuButtons.isEmpty()) {
			actionMenuLayout = null;
		} else {
			actionMenuLayout = new VerticalLayout();
			actionMenuLayout.setSizeUndefined();
			for (Button actionMenuButton : actionMenuButtons) {
				actionMenuButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
				actionMenuButton.removeStyleName(ValoTheme.BUTTON_LINK);
				actionMenuButton.addStyleName("action-menu-button");
				actionMenuLayout.addComponent(actionMenuButton);
			}
		}

		return actionMenuLayout;
	}

	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> actionMenuButtons = new ArrayList<Button>();
		return actionMenuButtons;
	}

	@Override
	public String getCollection() {
		return ConstellioUI.getCurrentSessionContext().getCurrentCollection();
	}

	@Override
	public ConstellioNavigator navigateTo() {
		return ConstellioUI.getCurrent().navigateTo();
	}

	@Override
	public void updateUI() {
		ConstellioUI.getCurrent().updateContent();
	}

	@Override
	public void showMessage(String message) {
		Notification notification = new Notification(message, Type.WARNING_MESSAGE);
		notification.setHtmlContentAllowed(true);
		notification.show(Page.getCurrent());
	}

	@Override
	public void showErrorMessage(String errorMessage) {
		Notification notification = new Notification(errorMessage + "<br/><br/>" + $("clickToClose"), Type.WARNING_MESSAGE);
		notification.setHtmlContentAllowed(true);
		notification.show(Page.getCurrent());
	}

	@Override
	public SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

	@Override
	public ConstellioFactories getConstellioFactories() {
		return ConstellioFactories.getInstance();
	}

	protected ClickListener getBackButtonClickListener() {
		return null;
	}

	protected abstract Component buildMainComponent(ViewChangeEvent event);

}
