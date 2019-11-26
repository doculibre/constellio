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
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.decorators.base.ActionMenuButtonsDecorator;
import com.constellio.app.ui.pages.home.HomeViewImpl;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.constellio.model.entities.records.wrappers.RecordWrapperRuntimeException;
import com.vaadin.event.UIEvents.PollEvent;
import com.vaadin.event.UIEvents.PollListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.pages.management.labels.ListLabelViewImpl.TYPE_TABLE;

@SuppressWarnings("serial")
public abstract class BaseViewImpl extends VerticalLayout implements View, BaseView, PollListener {

	public static final String CATEGORY_BUTTON = "seleniumCategoryButton";

	private static Logger LOGGER = LoggerFactory.getLogger(BaseViewImpl.class);

	public static final String BACK_BUTTON_CODE = "seleniumBackButtonCode";

	private I18NHorizontalLayout breadcrumbTrailLayout;

	private BaseBreadcrumbTrail breadcrumbTrail;

	private Label titleLabel;

	private BackButton backButton;

	private Boolean delayedBackButtonVisible;

	private I18NHorizontalLayout titleBackButtonLayout;

	private Component mainComponent;
	private Component actionMenu;
	private List<Button> actionMenuButtons;
	private Map<Button, MenuItem> actionMenuButtonsAndItems = new HashMap<>();
	protected I18NHorizontalLayout actionMenuBarLayout;

	private List<ViewEnterListener> viewEnterListeners = new ArrayList<>();

	private List<ActionMenuButtonsDecorator> actionMenuButtonsDecorators = new ArrayList<>();

	public BaseViewImpl() {
		this(ConstellioUI.getCurrent().getConstellioFactories().getAppLayerFactory());
	}

	public BaseViewImpl(AppLayerFactory appLayerFactory) {
		this(ConstellioUI.getCurrentSessionContext().getCurrentCollection(), appLayerFactory);
	}

	public BaseViewImpl(String collection, AppLayerFactory appLayerFactory) {
		DecorateMainComponentAfterInitExtensionParams params = new DecorateMainComponentAfterInitExtensionParams(this);

		appLayerFactory.getExtensions().getSystemWideExtensions().decorateMainComponentBeforeViewInstanciated(params);
		if (collection != null) {
			appLayerFactory.getExtensions().forCollection(collection).decorateMainComponentBeforeViewInstanciated(params);
		}
	}

	@Override
	public final void enter(ViewChangeEvent event) {
		try {
			if (event != null) {
				for (ViewEnterListener viewEnterListener : viewEnterListeners) {
					viewEnterListener.viewEntered(event.getParameters());
				}
			}

			DecorateMainComponentAfterInitExtensionParams params = new DecorateMainComponentAfterInitExtensionParams(this, event);
			AppLayerFactory appLayerFactory = ConstellioUI.getCurrent().getConstellioFactories().getAppLayerFactory();

			appLayerFactory.getExtensions().getSystemWideExtensions().decorateMainComponentBeforeViewAssembledOnViewEntered(params);
			String collection = ConstellioUI.getCurrentSessionContext().getCurrentCollection();
			if (collection != null) {
				((ConstellioUI) UI.getCurrent()).getHeader().setCurrentCollectionQuietly();
				appLayerFactory.getExtensions().forCollection(collection)
						.decorateMainComponentBeforeViewAssembledOnViewEntered(params);
			}

			try {
				initBeforeCreateComponents(event);
			} catch (Exception e) {
				if (e instanceof RecordWrapperRuntimeException.WrappedRecordAndTypesCollectionMustBeTheSame) {
					throw e;
				}
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

			addStyleName("base-view");
			addStyleName("main-component-wrapper");
			setSizeFull();

			removeAllComponents();

			breadcrumbTrailLayout = new I18NHorizontalLayout();
			breadcrumbTrailLayout.setWidth("100%");

			if (isBreadcrumbsVisible()) {
				breadcrumbTrail = buildBreadcrumbTrail();
			}

			titleBackButtonLayout = new I18NHorizontalLayout();
			titleBackButtonLayout.setWidth("100%");

			String title = getTitle();
			if (isBreadcrumbsVisible()) {
				if (breadcrumbTrail == null && title != null) {
					breadcrumbTrail = new TitleBreadcrumbTrail(this, title);
				} else if (title != null && breadcrumbTrail == null) {
					titleLabel = new Label(title);
					titleLabel.addStyleName(ValoTheme.LABEL_H1);
				}
			}

			backButton = new BackButton();
			ClickListener backButtonClickListener = getBackButtonClickListener();
			backButton.addStyleName(BACK_BUTTON_CODE);
			if (backButtonClickListener == null) {
				backButton.setVisible(false);
			} else {
				backButton.setVisible(!Boolean.FALSE.equals(delayedBackButtonVisible));
				backButton.addClickListener(backButtonClickListener);
			}

			actionMenu = buildActionMenu(event);
			if ((actionMenu != null  && !isActionMenuBar()) || !isFullWidthIfActionMenuAbsent()) {
				addStyleName("action-menu-wrapper");
			}

			mainComponent = buildMainComponent(event);
			mainComponent.setId("main-component");
			mainComponent.addStyleName(mainComponent.getId());

			if (breadcrumbTrail != null) {
				breadcrumbTrail.setWidth(null);
				breadcrumbTrailLayout.addComponent(breadcrumbTrail);
				breadcrumbTrailLayout.setComponentAlignment(breadcrumbTrail, Alignment.MIDDLE_LEFT);
			}

			if (breadcrumbTrailLayout.getComponentCount() != 0) {
				addComponent(breadcrumbTrailLayout);
			}

			if (actionMenu != null && isActionMenuBar()) {
				addComponent(actionMenu);
			}

			addComponent(mainComponent);
			if (actionMenu != null && !isActionMenuBar()) {
				addComponent(actionMenu);
			}

			if (titleLabel != null || backButton != null) {
				if (titleLabel != null) {
					titleBackButtonLayout.addComponents(titleLabel);
				}
				titleBackButtonLayout.addComponents(backButton);
			} else {
				titleBackButtonLayout.setVisible(false);
			}

			setExpandRatio(mainComponent, 1f);

			Label spacer = new Label("");
			spacer.addStyleName("base-view-footer-spacer");
			spacer.setHeight("30px");
			addComponent(spacer);

			if (isBackgroundViewMonitor()) {
				addBackgroundViewMonitor();
			}

			appLayerFactory.getExtensions().getSystemWideExtensions().decorateMainComponentAfterViewAssembledOnViewEntered(params);
			if (collection != null) {
				appLayerFactory.getExtensions().forCollection(collection)
						.decorateMainComponentAfterViewAssembledOnViewEntered(params);
			}

			afterViewAssembled(event);
			updateActionMenuItems();

			//			StringBuffer js = new StringBuffer();
			//			js.append("setTimeout(function() {setInterval(function() {\r\n");
			//			js.append("try {");
			//			js.append("\r\n");
			//			js.append("var req = new XMLHttpRequest();");
			//			js.append("\r\n");
			//			js.append("req.open('GET', 'http://localhost:7070/constellio/agent/test', false);");
			//			js.append("\r\n");
			//			js.append("req.send();");
			//			js.append("\r\n");
			//			js.append("} catch (Exception) { window.location='http://localhost:7070/constellio/#!adminModule'; }");
			//			js.append("}, 10000);}, 1000);");
			//			if (true) com.vaadin.ui.JavaScript.eval(js.toString());
		} catch (Exception e) {
			boolean exceptionHandled = false;
			if (event != null) {
				for (ViewEnterListener viewEnterListener : viewEnterListeners) {
					if (viewEnterListener.exception(e)) {
						exceptionHandled = true;
					}
				}
			}
			if (!exceptionHandled) {
				e.printStackTrace();
				LOGGER.error("Error when entering view", e);
				throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException(e);
			}
		}
	}

	public void refreshActionMenu() {
		if (actionMenu != null) {
			Component oldActionMenu = actionMenu;
			actionMenu = buildActionMenu(null);
			replaceComponent(oldActionMenu, actionMenu);
		}
	}

	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return null;
	}

	public boolean isBackgroundViewMonitor() {
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
		return true;
	}

	protected boolean isActionMenuBar() {
		return false;
	}

	protected String getTitle() {
		return getClass().getSimpleName();
	}

	protected String getGuideUrl() {
		return null;
	}

	protected String getActionMenuBarCaption() {
		return null;
	}

	protected MenuBar newActionMenuBar() {
		actionMenuButtonsAndItems.clear();

		String menuBarCaption = getActionMenuBarCaption();
		if (menuBarCaption == null) {
			menuBarCaption = "";
		}

		MenuBar menuBar = new MenuBar();
		menuBar.addStyleName("action-menu-bar");
		menuBar.setAutoOpen(false);
		menuBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
		if (StringUtils.isBlank(menuBarCaption)) {
			menuBar.addStyleName("no-caption-action-menu-bar");
		}

		MenuItem rootItem = menuBar.addItem("", FontAwesome.ELLIPSIS_V, null);
		if (StringUtils.isNotBlank(menuBarCaption)) {
			rootItem.setText(menuBarCaption);
		}

		for (final Button actionMenuButton : actionMenuButtons) {
			Resource icon = actionMenuButton.getIcon();
			String caption = actionMenuButton.getCaption();
			MenuItem actionMenuItem = rootItem.addItem(caption, icon, new Command() {
				@Override
				public void menuSelected(MenuItem selectedItem) {
					actionMenuButton.click();
					updateActionMenuItems();
				}
			});
			actionMenuItem.setEnabled(actionMenuButton.isEnabled());
			actionMenuItem.setVisible(actionMenuButton.isVisible());
			actionMenuButtonsAndItems.put(actionMenuButton, actionMenuItem);
		}
		return menuBar;
	}

	protected List<Button> getQuickActionMenuButtons() {
		return Collections.emptyList();
	}

	/**
	 * Adapted from https://vaadin.com/forum#!/thread/8150555/8171634
	 *
	 * @param event
	 * @return
	 */
	protected Component buildActionMenu(ViewChangeEvent event) {
		Component result;
		actionMenuButtons = buildActionMenuButtons(event);
		for (ActionMenuButtonsDecorator actionMenuButtonsDecorator : actionMenuButtonsDecorators) {
			actionMenuButtonsDecorator.decorate(this, actionMenuButtons);
		}

		if (actionMenuButtons == null || actionMenuButtons.isEmpty()) {
			result = null;
		} else {
			if (isActionMenuBar()) {
				MenuBar menuBar = newActionMenuBar();
				List<Button> quickActionButtons = getQuickActionMenuButtons();
				if (quickActionButtons != null && !quickActionButtons.isEmpty()) {
					actionMenuBarLayout = new I18NHorizontalLayout();
					actionMenuBarLayout.addStyleName("action-menu-bar-layout");
					actionMenuBarLayout.setSpacing(true);

					int visibleButtons = 0;
					for (Button quickActionButton : quickActionButtons) {
						if (quickActionButton.isVisible()) {
							quickActionButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
							quickActionButton.addStyleName(ValoTheme.BUTTON_LINK);
							quickActionButton.addStyleName("action-menu-bar-button");
							actionMenuBarLayout.addComponent(quickActionButton);
							visibleButtons++;
						}
					}

					if (visibleButtons == 0) {
						result = menuBar;
					} else {
						actionMenuBarLayout.addComponent(menuBar);
						result = actionMenuBarLayout;
					}
				} else {
					result = menuBar;
				}
            } else {
                VerticalLayout actionMenuLayout = new VerticalLayout();
                actionMenuLayout.addStyleName("action-menu-layout");
                actionMenuLayout.setSizeUndefined();

                int visibleButtons = 0;
                for (Button actionMenuButton : actionMenuButtons) {
                    if (actionMenuButton.isVisible()) {
                        actionMenuButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
                        actionMenuButton.removeStyleName(ValoTheme.BUTTON_LINK);
                        actionMenuButton.addStyleName("action-menu-button");
                        actionMenuLayout.addComponent(actionMenuButton);

                        visibleButtons++;
                    }
                }

                if (visibleButtons == 0) {
                    actionMenuLayout = null;
                }
                result = actionMenuLayout;
				if (result != null) {
					result.addStyleName("action-menu");
				}
			}
		}
		//        if (result != null) {
		//			result.addStyleName("action-menu");
		//        }
        return result;
    }

	protected void actionButtonStateChanged(Button actionMenuButton) {
		if (isActionMenuBar()) {
			MenuItem actionMenuItem = actionMenuButtonsAndItems.get(actionMenuButton);
			if (actionMenuItem != null) {
				actionMenuItem.setVisible(actionMenuButton.isVisible() && actionMenuButton.isEnabled());
			}
		}
	}

	protected void updateActionMenuItems() {
		for (Button actionMenuButton : actionMenuButtonsAndItems.keySet()) {
			MenuItem actionMenuItem = actionMenuButtonsAndItems.get(actionMenuButton);
			actionMenuItem.setVisible(actionMenuButton.isVisible() && actionMenuButton.isEnabled());
		}
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


	protected Button createLink(String caption, final Button.ClickListener listener, String iconName) {
		Button returnLink = new Button(caption, new ThemeResource("images/icons/" + iconName + ".png"));
		returnLink.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		returnLink.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		returnLink.addStyleName(CATEGORY_BUTTON);
		returnLink.addClickListener(listener);
		return returnLink;
	}

	protected Button createLink(String caption, final Button.ClickListener listener, String iconName,
								boolean hasAccess) {
		if (!hasAccess) {
			return null;
		}
		return createLink(caption, listener, iconName);
	}

	protected Table setTableProperty(Table table, int maxSize) {
		table.setSizeFull();
		table.setPageLength(Math.min(15, maxSize));
		table.setColumnHeader("buttons", "");
		table.setColumnHeader("caption", $("ListSchemaTypeView.caption"));
		table.setColumnExpandRatio("caption", 1);
		table.addStyleName(TYPE_TABLE);
		return table;
	}

	@Override
	public boolean isInWindow() {
		return ComponentTreeUtils.findParent(this, Window.class) != null;
	}

	@Override
	public void closeAllWindows() {
		for (Window window : new ArrayList<Window>(ConstellioUI.getCurrent().getWindows())) {
			window.close();
		}
	}

	protected boolean isBreadcrumbsVisible() {
		return true;
	}

	@Override
	public MainLayout getMainLayout() {
		return ConstellioUI.getCurrent().getMainLayout();
	}

	@Override
	public void setBackButtonVisible(boolean visible) {
		if (backButton != null) {
			backButton.setVisible(visible);
		} else {
			delayedBackButtonVisible = visible;
		}
	}

	public BaseBreadcrumbTrail getBreadcrumbTrail() {
		return breadcrumbTrail;
	}

	public void replaceBreadcrumbTrail(BaseBreadcrumbTrail newBreadcrumbTrail) {
		if (breadcrumbTrail != null) {
			breadcrumbTrailLayout.replaceComponent(breadcrumbTrail, breadcrumbTrail = newBreadcrumbTrail);
		}
	}

	public class CustomCssLayout extends CssLayout {
		@Override
		public void addComponents(Component... components) {
			for (Component component : components) {

				if (component != null) {
					super.addComponent(component);
				}
			}
		}
	}

	@Override
	public void runAsync(Runnable runnable) {
		runAsync(runnable, 1000);
	}

	@Override
	public void runAsync(final Runnable runnable, final int pollInterval) {
		ConstellioUI.getCurrent().runAsync(runnable, pollInterval, this);
	}

	@Override
	public void openURL(String url) {
		Page.getCurrent().open(url, null);
	}

}
