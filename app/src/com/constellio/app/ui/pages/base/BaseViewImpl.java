package com.constellio.app.ui.pages.base;

import com.constellio.app.api.extensions.params.DecorateMainComponentAfterInitExtensionParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemAction.MenuItemActionBuilder;
import com.constellio.app.services.menu.MenuItemActionConverter;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus;
import com.constellio.app.services.menu.MenuItemFactory.MenuItemRecordProvider;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.framework.buttons.BackButton;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.framework.components.dialogs.ConfirmDialogProperties;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.menuBar.ActionMenuDisplay;
import com.constellio.app.ui.framework.decorators.base.ActionMenuButtonsDecorator;
import com.constellio.app.ui.pages.home.HomeViewImpl;
import com.constellio.app.ui.pages.home.PartialRefresh;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.constellio.data.dao.services.Stats;
import com.constellio.data.dao.services.Stats.CallStatCompiler;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapperRuntimeException;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.event.UIEvents.PollEvent;
import com.vaadin.event.UIEvents.PollListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	protected Component actionMenu;
	protected ActionMenuDisplay actionMenuDisplay;
	private List<ViewEnterListener> viewEnterListeners = new ArrayList<>();

	private List<ActionMenuButtonsDecorator> actionMenuButtonsDecorators = new ArrayList<>();
	private final AppLayerFactory appLayerFactory;

	public BaseViewImpl() {
		this(ConstellioUI.getCurrent().getConstellioFactories().getAppLayerFactory());
	}

	private transient CallStatCompiler callStatCompiler;

	public BaseViewImpl(AppLayerFactory appLayerFactory) {
		this(ConstellioUI.getCurrentSessionContext().getCurrentCollection(), appLayerFactory);
	}

	public BaseViewImpl(String collection, AppLayerFactory appLayerFactory) {
		DecorateMainComponentAfterInitExtensionParams params = new DecorateMainComponentAfterInitExtensionParams(this);

		this.appLayerFactory = appLayerFactory;

		statCompiler().log(() -> {
			appLayerFactory.getExtensions().getSystemWideExtensions().decorateMainComponentBeforeViewInstanciated(params);
			if (collection != null) {
				appLayerFactory.getExtensions().forCollection(collection).decorateMainComponentBeforeViewInstanciated(params);
			}
		});
	}

	private CallStatCompiler statCompiler() {
		if (callStatCompiler == null) {
			callStatCompiler = Stats.compilerFor(getClass().getSimpleName());
		}
		return callStatCompiler;
	}

	@Override
	public final void enter(ViewChangeEvent event) {
		statCompiler().log(() -> {
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
				if (collection != null && ((ConstellioUI) UI.getCurrent()).getHeader() != null) {
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
					}
					//TODO fix me. Else if should be on the first if?
					else if (title != null && breadcrumbTrail == null) {
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
				if (!isFullWidthIfActionMenuAbsent()) {
					addStyleName("action-menu-wrapper");
				}

				mainComponent = buildMainComponent(event);
				mainComponent.setId("main-component");
				mainComponent.addStyleName(mainComponent.getId());

				if (!ConstellioUI.getCurrent().isNested()) {
					if (breadcrumbTrail != null) {
						breadcrumbTrail.setWidth(null);
						breadcrumbTrailLayout.addComponent(breadcrumbTrail);
						breadcrumbTrailLayout.setComponentAlignment(breadcrumbTrail, Alignment.MIDDLE_LEFT);
					} else if (titleLabel != null) {
						titleLabel.setWidth(null);
						breadcrumbTrailLayout.addComponent(titleLabel);
						breadcrumbTrailLayout.setComponentAlignment(titleLabel, Alignment.TOP_LEFT);
					}
					if (breadcrumbTrailLayout.getComponentCount() != 0) {
						addComponent(breadcrumbTrailLayout);
					}
				}

				if (actionMenu != null) {
					addComponent(actionMenu);
				}

				addComponent(mainComponent);

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
		});
	}

	public void refreshActionMenu() {
		statCompiler().log(() -> {
			if (this.actionMenu != null) {
				Component oldActionMenu = this.actionMenu;
				Component placeHolder = new Label("");
				replaceComponent(oldActionMenu, placeHolder);
				this.actionMenu = buildActionMenu(null);
				replaceComponent(placeHolder, this.actionMenu);
			}
		});
	}

	public void updateMenuAction(String type,
								 Function<MenuItemActionBuilder, MenuItemAction> modifyThisActionThenReturn) {
		if (actionMenuDisplay != null) {
			actionMenuDisplay.updateAction(type, modifyThisActionThenReturn);
		}
	}

	protected void updateMenuActionBasedOnButton(final Button button) {
		updateMenuActionBasedOnButton(button, MenuItemActionBuilder::build);
	}

	protected void updateMenuActionBasedOnButton(final Button button,
												 final Function<MenuItemActionBuilder, MenuItemAction> lastModificationThenBuild) {
		if (button != null && button.getId() != null) {
			updateMenuAction(button.getId(), builder -> {

				builder
						.caption(button.getCaption())
						.icon(button.getIcon())
						.state(new MenuItemActionState(button.isEnabled() ? MenuItemActionStateStatus.VISIBLE : MenuItemActionStateStatus.DISABLED))
						.command(recordIds -> button.click());

				return lastModificationThenBuild != null ? lastModificationThenBuild.apply(builder) : builder.build();
			});
		}
	}

	public Set<String> getAllActionTypes() {
		Set<String> states;

		if (actionMenuDisplay != null) {
			states = actionMenuDisplay.getAllActionTypes();
		} else {
			states = Collections.emptySet();
		}

		return states;
	}

	public MenuItemAction getMenuItemActionByType(String type) {
		return actionMenuDisplay != null ? actionMenuDisplay.getMenuItemActionByType(type) : null;
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

	protected String getTitle() {
		return getClass().getSimpleName();
	}

	protected String getActionMenuBarCaption() {
		return $("actions");
	}

	protected boolean alwaysUseLayoutForActionMenu() {
		return false;
	}

	/**
	 * Adapted from https://vaadin.com/forum#!/thread/8150555/8171634
	 *
	 * @param event
	 * @return
	 */
	protected Component buildActionMenu(ViewChangeEvent event) {
		Component result;

		final List<MenuItemAction> menuItemActions = buildMenuItemActions(event);
		List<Button> actionMenuButtons = buildActionMenuButtons(event);
		for (ActionMenuButtonsDecorator actionMenuButtonsDecorator : actionMenuButtonsDecorators) {
			actionMenuButtonsDecorator.decorate(this, actionMenuButtons);
		}

		actionMenuDisplay = new ActionMenuDisplay(appLayerFactory, getSessionContext()) {
			@Override
			public String getMenuBarRootCaption() {
				return getActionMenuBarCaption();
			}

			@Override
			public boolean isOnlyQuickActionAreVisible() {
				return isOnlyQuickMenuActionVisible();
			}

			@Override
			public Supplier<List<MenuItemAction>> getMenuItemActionsSupplier() {
				return () -> buildMenuItemActions(event);
			}

			@Override
			public Supplier<MenuItemRecordProvider> getMenuItemRecordProviderSupplier() {
				return () -> new MenuItemRecordProvider() {
					@Override
					public LogicalSearchQuery getQuery() {
						return null;
					}

					@Override
					public List<Record> getRecords() {
						return Collections.emptyList();
					}
				};
			}
		};
		actionMenuDisplay.addStyleName("action-menu-bar-layout");


		if (actionMenuButtons != null && !actionMenuButtons.isEmpty()) {
			actionMenuDisplay = new ActionMenuDisplay(actionMenuDisplay) {
				@Override
				public Supplier<List<MenuItemAction>> getMenuItemActionsSupplier() {
					final AtomicInteger priority = new AtomicInteger(100);

					return () -> Stream.concat(menuItemActions.stream(),
							actionMenuButtons.stream().map(action -> {
								MenuItemActionBuilder menuItemActionBuilder = MenuItemAction
										.builder()
										.type(action.getId())
										.caption(action.getCaption())
										.icon(action.getIcon())
										.priority(priority.getAndAdd(100))
										.command(recordIds -> {
											action.click();
											updateActionMenuItems();
										});

								if (!action.isVisible()) {
									menuItemActionBuilder.state(new MenuItemActionState(MenuItemActionStateStatus.HIDDEN));
								} else if (!action.isEnabled()) {
									menuItemActionBuilder.state(new MenuItemActionState(MenuItemActionStateStatus.DISABLED));
								} else {
									menuItemActionBuilder.state(new MenuItemActionState(MenuItemActionStateStatus.VISIBLE));
								}

								return menuItemActionBuilder.build();
							})).collect(Collectors.toList());
				}
			};
		}

		actionMenuDisplay = buildActionMenuDisplay(actionMenuDisplay);

		if (actionMenuDisplay != null) {
			result = actionMenuDisplay;
		} else if (isOnlyQuickMenuActionVisible()) {
			result = null;
		} else if (actionMenuButtons == null || actionMenuButtons.isEmpty()) {
			result = null;
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

		return result;
	}

	public void setQuickActionButtonsVisible(boolean visible) {
		if (actionMenuDisplay != null) {
			actionMenuDisplay = new ActionMenuDisplay(actionMenuDisplay) {
				@Override
				public boolean isQuickActionsAreVisible() {
					return visible;
				}
			};

			replaceComponent(actionMenu, actionMenuDisplay);
			actionMenu = actionMenuDisplay;
		}
	}

	protected void actionButtonStateChanged(Button actionMenuButton) {
		boolean isVisible = actionMenuButton.isVisible() && actionMenuButton.isEnabled();
		updateMenuAction(actionMenuButton.getId(), builder ->
				builder
						.state(new MenuItemActionState(isVisible ? MenuItemActionStateStatus.VISIBLE : MenuItemActionStateStatus.HIDDEN))
						.build());
	}

	protected void updateActionMenuItems() {
		if (actionMenuDisplay != null) {
			boolean lastVisibility = actionMenuDisplay.isHiddenWhenNoActionOrStateAreAllNotVisible();
			Set<String> allActionTypes = actionMenuDisplay.getAllActionTypes();
			boolean currentVisibility = !allActionTypes.isEmpty() && allActionTypes.stream()
					.map(actionMenuDisplay::getMenuItemActionByType)
					.allMatch(menuItemAction -> menuItemAction.getState().getStatus() != MenuItemActionStateStatus.VISIBLE);

			if (currentVisibility != lastVisibility) {
				actionMenuDisplay = new ActionMenuDisplay(actionMenuDisplay) {
					@Override
					public boolean isHiddenWhenNoActionOrStateAreAllNotVisible() {
						return currentVisibility;
					}
				};

				replaceComponent(actionMenu, actionMenuDisplay);
				actionMenu = actionMenuDisplay;
			}
		}
	}

	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		return new ArrayList<>();
	}

	protected List<MenuItemAction> buildMenuItemActions(ViewChangeEvent event) {
		return new ArrayList<>();
	}

	protected ActionMenuDisplay buildActionMenuDisplay(final ActionMenuDisplay defaultActionMenuDisplay) {
		return defaultActionMenuDisplay;
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
	public void partialRefresh() {
		if (this instanceof PartialRefresh) {
			((PartialRefresh) this).doPartialRefresh();
		}
	}

	@Override
	public void showMessage(String message) {
		ConstellioUI.getCurrent().showMessage(message);
	}

	@Override
	public void showClickableMessage(String message) {
		ConstellioUI.getCurrent().showClickableMessage(message);
	}

	@Override
	public void showClickableMessage(VerticalLayout verticalLayout) {
		ConstellioUI.getCurrent().showClickableMessage(verticalLayout);
	}

	@Override
	public void showConfirmDialog(ConfirmDialogProperties properties) {
		ConstellioUI.getCurrent().showConfirmDialog(properties);
	}

	@Override
	public void showErrorMessage(String errorMessage) {
		ConstellioUI.getCurrent().showErrorMessage(errorMessage);
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

	protected void updateMenuActionBasedOnButton(final MenuItemAction menuItemAction, final Button button) {

		if (menuItemAction.getState().getStatus() == MenuItemActionStateStatus.VISIBLE) {
			if (!button.isVisible()) {
				menuItemAction.setState(new MenuItemActionState(MenuItemActionStateStatus.HIDDEN));
			} else if (!button.isEnabled()) {
				menuItemAction.setState(new MenuItemActionState(MenuItemActionStateStatus.DISABLED));
			} else {
				menuItemAction.setState(new MenuItemActionState(MenuItemActionStateStatus.VISIBLE));
			}
		}

		menuItemAction.setCommand(recordIds -> button.click());
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
		for (Window window : new ArrayList<>(ConstellioUI.getCurrent().getWindows())) {
			window.close();
		}
	}

	protected boolean isBreadcrumbsVisible() {
		return true;
	}

	protected boolean isTitleVisible() {
		return true;
	}

	protected boolean isOnlyQuickMenuActionVisible() {
		return false;
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
	public void doWhileRunningAsync(Runnable runnable) {
		ConstellioUI.getCurrent().access(runnable);
	}

	@Override
	public void openURL(String url) {
		Page.getCurrent().open(url, null);
	}

}
