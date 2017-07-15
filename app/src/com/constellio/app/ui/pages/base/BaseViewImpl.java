package com.constellio.app.ui.pages.base;

import com.constellio.app.api.extensions.params.DecorateMainComponentAfterInitExtensionParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.framework.buttons.BackButton;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.framework.decorators.base.ActionMenuButtonsDecorator;
import com.constellio.app.ui.pages.home.HomeViewImpl;
import com.vaadin.event.UIEvents.PollEvent;
import com.vaadin.event.UIEvents.PollListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.themes.ValoTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

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
	private List<Button> actionMenuButtons;

	private List<ViewEnterListener> viewEnterListeners = new ArrayList<>();
	
	private List<ActionMenuButtonsDecorator> actionMenuButtonsDecorators = new ArrayList<>();

	public BaseViewImpl() {
		DecorateMainComponentAfterInitExtensionParams params = new DecorateMainComponentAfterInitExtensionParams(this);
		AppLayerFactory appLayerFactory = ConstellioUI.getCurrent().getConstellioFactories().getAppLayerFactory();

		appLayerFactory.getExtensions().getSystemWideExtensions().decorateMainComponentBeforeViewInstanciated(params);
		String collection = ConstellioUI.getCurrentSessionContext().getCurrentCollection();
		if (collection != null) {
			appLayerFactory.getExtensions().forCollection(collection).decorateMainComponentBeforeViewInstanciated(params);
		}
	}

	@Override
	public final void enter(ViewChangeEvent event) {
		if (event != null) {
			for (ViewEnterListener viewEnterListener : viewEnterListeners) {
				viewEnterListener.viewEntered(event.getParameters());
			}
		}

		DecorateMainComponentAfterInitExtensionParams params = new DecorateMainComponentAfterInitExtensionParams(this);
		AppLayerFactory appLayerFactory = ConstellioUI.getCurrent().getConstellioFactories().getAppLayerFactory();

		appLayerFactory.getExtensions().getSystemWideExtensions().decorateMainComponentBeforeViewAssembledOnViewEntered(params);
		String collection = ConstellioUI.getCurrentSessionContext().getCurrentCollection();
		if (collection != null) {
			appLayerFactory.getExtensions().forCollection(collection)
					.decorateMainComponentBeforeViewAssembledOnViewEntered(params);
		}

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

		if (event != null) {
			for (ViewEnterListener viewEnterListener : viewEnterListeners) {
				viewEnterListener.afterInit(event.getParameters());
			}
		}

		addStyleName("main-component-wrapper");
		setSizeFull();

		removeAllComponents();

		breadcrumbTrail = buildBreadcrumbTrail();

		titleBackButtonLayout = new HorizontalLayout();
		titleBackButtonLayout.setWidth("100%");

		String title = getTitle();
		if (breadcrumbTrail == null && title != null) {
			breadcrumbTrail = new TitleBreadcrumbTrail(this, title);
		} else if (title != null) {
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

		actionMenu = buildActionMenu(event);
		if (actionMenu != null || !isFullWidthIfActionMenuAbsent()) {
			addStyleName("action-menu-wrapper");
			if (actionMenu != null) {
				actionMenu.addStyleName("action-menu");
			}
		}

		mainComponent = buildMainComponent(event);
		mainComponent.addStyleName("main-component");

//		if (titleLabel != null || backButton.isVisible()) {
			addComponent(titleBackButtonLayout);
//		}

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

		appLayerFactory.getExtensions().getSystemWideExtensions().decorateMainComponentAfterViewAssembledOnViewEntered(params);
		if (collection != null) {
			appLayerFactory.getExtensions().forCollection(collection)
					.decorateMainComponentAfterViewAssembledOnViewEntered(params);
		}

		afterViewAssembled(event);
		
//		StringBuffer js = new StringBuffer();
//		js.append("setTimeout(function() {setInterval(function() {\r\n"); 
//		js.append("try {");
//		js.append("\r\n");
//		js.append("var req = new XMLHttpRequest();"); 
//		js.append("\r\n");
//		js.append("req.open('GET', 'http://localhost:7070/constellio/agent/test', false);"); 
//		js.append("\r\n");
//		js.append("req.send();");
//		js.append("\r\n");
//		js.append("} catch (Exception) { window.location='http://localhost:7070/constellio/#!adminModule'; }"); 
//		js.append("}, 10000);}, 1000);");
//		if (true) com.vaadin.ui.JavaScript.eval(js.toString());
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
		try {
			onBackgroundViewMonitor();
		} catch (Exception e) {
			UI.getCurrent().removePollListener(this);
		}
	}

	@Override
	public void invalidate() {
		if (isBackgroundViewMonitor()) {
			UI.getCurrent().removePollListener(this);
		}
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
		actionMenuButtons = buildActionMenuButtons(event);
		if (actionMenuButtons == null || actionMenuButtons.isEmpty()) {
			actionMenuLayout = null;
		} else {
			actionMenuLayout = new VerticalLayout();
			actionMenuLayout.setSizeUndefined();
			
			for (ActionMenuButtonsDecorator actionMenuButtonsDecorator : actionMenuButtonsDecorators) {
				actionMenuButtonsDecorator.decorate(this, actionMenuButtons);
			}
			
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
		List<Button> actionMenuButtons = new ArrayList<>();
		return actionMenuButtons;
	}

	@Override
	public String getCollection() {
		return ConstellioUI.getCurrentSessionContext().getCurrentCollection();
	}

	//@Override
	public CoreViews navigateTo() {
		return ConstellioUI.getCurrent().navigateTo();
	}

	@Override
	public Navigation navigate() {
		return ConstellioUI.getCurrent().navigate();
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
	public void showClickableMessage(String message) {
//		Notification notification = new Notification(message, Type.WARNING_MESSAGE);
//		notification.setDelayMsec(-1);
//		notification.setHtmlContentAllowed(true);
//		notification.show(Page.getCurrent());
		ClickableNotification.show(ConstellioUI.getCurrent(), "", message);
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

	@Override
	public UIContext getUIContext() {
		return ConstellioUI.getCurrent();
	}

	protected ClickListener getBackButtonClickListener() {
		return null;
	}
	
	public void addActionMenuButtonsDecorator(ActionMenuButtonsDecorator decorator) {
		this.actionMenuButtonsDecorators.add(decorator);
	}
	
	public List<ActionMenuButtonsDecorator> getActionMenuButtonsDecorators() {
		return actionMenuButtonsDecorators;
	}
	
	public void removeActionMenuButtonsDecorator(ActionMenuButtonsDecorator decorator) {
		this.actionMenuButtonsDecorators.remove(decorator);
	}

	protected abstract Component buildMainComponent(ViewChangeEvent event);

	public List<Button> getActionMenuButtons() {
		return actionMenuButtons;
	}
}
