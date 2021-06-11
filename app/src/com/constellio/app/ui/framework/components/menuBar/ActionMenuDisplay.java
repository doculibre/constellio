package com.constellio.app.ui.framework.components.menuBar;

import com.constellio.app.services.actionDisplayManager.MenuDisplayItem;
import com.constellio.app.services.actionDisplayManager.MenuDisplayList;
import com.constellio.app.services.actionDisplayManager.MenuDisplayListBySchemaType;
import com.constellio.app.services.actionDisplayManager.MenusDisplayManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemAction.MenuItemActionBuilder;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus;
import com.constellio.app.services.menu.MenuItemFactory;
import com.constellio.app.services.menu.MenuItemFactory.MenuItemRecordProvider;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.utils.Pair;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.HIDDEN;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.VISIBLE;

public class ActionMenuDisplay extends CustomComponent {
	public static final boolean QUICK_ACTION_ARE_VISIBLE_DEFAULT = true;
	public static final boolean QUICK_ACTION_IS_REMOVED_FROM_MENU_BAR_WHEN_DISPLAYED_DEFAULT = true;
	public static final boolean HIDDEN_WHEN_NO_ACTION_OR_STATE_ARE_ALL_NOT_VISIBLE_DEFAULT = true;
	public static final boolean ONLY_QUICK_ACTION_ARE_VISIBLE_DEFAULT = false;
	public static final Supplier<List<MenuItemAction>> USE_THESE_ACTIONS_IN_MENU_BAR_INSTEAD_SUPPLIER_DEFAULT = null;
	public static final Supplier<List<MenuItemAction>> USE_THESE_ACTIONS_IN_QUICK_ACTION_INSTEAD_SUPPLIER_DEFAULT = null;
	public static final int QUICK_ACTION_COUNT_DEFAULT = MenuDisplayList.QUICK_ACTION_COUNT_DEFAULT;
	public static final Supplier<String> SCHEMA_TYPE_CODE_SUPPLIER_DEFAULT = null;

	public static final String MENU_BAR_ROOT_CAPTION_DEFAULT = "";

	private final AppLayerFactory appLayerFactory;
	private final SessionContext sessionContext;
	private final MenuItemFactory menuItemFactory;

	private final boolean quickActionsAreVisible;
	private final boolean quickActionIsRemovedFromMenuBarWhenDisplayed;
	private final boolean hiddenWhenNoActionOrStateAreAllNotVisible;
	private final boolean onlyQuickActionAreVisible;
	private final int quickActionCount;

	private final Supplier<List<MenuItemAction>> menuItemActionsSupplier;
	private final Supplier<MenuItemRecordProvider> menuItemRecordProviderSupplier;
	private final Supplier<String> schemaTypeCodeSupplier;

	private final String menuBarRootCaption;
	private final Supplier<List<MenuItemAction>> useTheseActionsInMenuBarInsteadSupplier;
	private final Supplier<List<MenuItemAction>> useTheseActionsInQuickActionInsteadSupplier;

	private final Map<String, MenuItemAction> menuItemActionMapping;
	private final Map<String, Object> menuItemActionComponentMapping;

	public ActionMenuDisplay(Supplier<List<MenuItemAction>> menuItemActionsSupplier,
							 Supplier<MenuItemRecordProvider> menuItemRecordProviderSupplier,
							 Supplier<String> schemaTypeCodeSupplier) {
		this(new ActionMenuDisplay() {
			@Override
			public Supplier<List<MenuItemAction>> getMenuItemActionsSupplier() {
				return menuItemActionsSupplier;
			}

			@Override
			public Supplier<MenuItemRecordProvider> getMenuItemRecordProviderSupplier() {
				return menuItemRecordProviderSupplier;
			}

			@Override
			public Supplier<String> getSchemaTypeCodeSupplier() {
				return schemaTypeCodeSupplier;
			}
		});
	}

	public ActionMenuDisplay(AppLayerFactory appLayerFactory, SessionContext sessionContext) {
		this(new ActionMenuDisplay() {
			@Override
			public AppLayerFactory getAppLayerFactory() {
				return appLayerFactory;
			}

			@Override
			public SessionContext getSessionContext() {
				return sessionContext;
			}
		});
	}

	public ActionMenuDisplay(@NotNull ActionMenuDisplay copy) {
		this.appLayerFactory = copy.getAppLayerFactory();
		this.sessionContext = copy.getSessionContext();
		this.menuItemFactory = copy.getMenuItemFactory();
		this.menuItemActionsSupplier = copy.getMenuItemActionsSupplier();
		this.menuItemRecordProviderSupplier = copy.getMenuItemRecordProviderSupplier();
		this.schemaTypeCodeSupplier = copy.getSchemaTypeCodeSupplier();
		this.quickActionsAreVisible = copy.isQuickActionsAreVisible();
		this.quickActionIsRemovedFromMenuBarWhenDisplayed = copy.isQuickActionIsRemovedFromMenuBarWhenDisplayed();
		this.hiddenWhenNoActionOrStateAreAllNotVisible = copy.isHiddenWhenNoActionOrStateAreAllNotVisible();
		this.menuBarRootCaption = copy.getMenuBarRootCaption();
		this.useTheseActionsInMenuBarInsteadSupplier = copy.getUseTheseActionsInMenuBarInsteadSupplier();
		this.useTheseActionsInQuickActionInsteadSupplier = copy.getUseTheseActionsInQuickActionInsteadSupplier();
		this.onlyQuickActionAreVisible = copy.isOnlyQuickActionAreVisible();
		this.quickActionCount = copy.getQuickActionCount();
		this.menuItemActionMapping = new HashMap<>(copy.menuItemActionMapping);
		this.menuItemActionComponentMapping = new HashMap<>(copy.menuItemActionComponentMapping);
		addStyleName(copy.getStyleName());
	}

	protected ActionMenuDisplay() {
		this.appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		this.sessionContext = ConstellioUI.getCurrentSessionContext();
		this.menuItemFactory = new MenuItemFactory(getAppLayerFactory(), getSessionContext());
		this.menuItemActionsSupplier = () -> {
			throw new IllegalStateException("getMenuItemActionsSupplier must be overriden somewhere in the instanciation process");
		};
		this.menuItemRecordProviderSupplier = () -> {
			throw new IllegalStateException("getMenuItemRecordProviderSupplier must be overriden somewhere in the instanciation process");
		};
		this.schemaTypeCodeSupplier = SCHEMA_TYPE_CODE_SUPPLIER_DEFAULT;
		this.quickActionsAreVisible = QUICK_ACTION_ARE_VISIBLE_DEFAULT;
		this.quickActionIsRemovedFromMenuBarWhenDisplayed = QUICK_ACTION_IS_REMOVED_FROM_MENU_BAR_WHEN_DISPLAYED_DEFAULT;
		this.hiddenWhenNoActionOrStateAreAllNotVisible = HIDDEN_WHEN_NO_ACTION_OR_STATE_ARE_ALL_NOT_VISIBLE_DEFAULT;
		this.menuBarRootCaption = MENU_BAR_ROOT_CAPTION_DEFAULT;
		this.useTheseActionsInMenuBarInsteadSupplier = USE_THESE_ACTIONS_IN_MENU_BAR_INSTEAD_SUPPLIER_DEFAULT;
		this.useTheseActionsInQuickActionInsteadSupplier = USE_THESE_ACTIONS_IN_QUICK_ACTION_INSTEAD_SUPPLIER_DEFAULT;
		this.onlyQuickActionAreVisible = ONLY_QUICK_ACTION_ARE_VISIBLE_DEFAULT;
		this.quickActionCount = QUICK_ACTION_COUNT_DEFAULT;
		this.menuItemActionMapping = new HashMap<>();
		this.menuItemActionComponentMapping = new HashMap<>();
	}

	@Override
	public void attach() {
		super.attach();

		setCompositionRoot(buildMainComponent());
	}

	public Component buildMainComponent() {
		menuItemActionMapping.clear();
		menuItemActionComponentMapping.clear();

		I18NHorizontalLayout layout = new I18NHorizontalLayout();

		validateSuppliersState();

		List<MenuItemAction> menuItemActions = getMenuItemActionsSupplier().get();
		menuItemActions = menuItemActions != null ? menuItemActions : new ArrayList<>();
		menuItemActions.sort(Comparator.comparing(MenuItemAction::getPriority));
		menuItemActions.stream().filter(action -> action.getType() == null).forEach(action -> action.setType("Type-was-null-so-set-random-type-" + UUID.randomUUID().toString()));


		final MenuDisplayList menuDisplayList = getMenuDisplayList();

		MenuItemRecordProvider menuItemRecordProvider = getMenuItemRecordProviderSupplier().get();
		menuItemRecordProvider = menuItemRecordProvider != null ? menuItemRecordProvider : buildDefaultMenuItemRecordProvider();

		final Map<String, MenuItemAction> menuItemActionMap = menuItemActions.stream().collect(Collectors.toMap(MenuItemAction::getType, menuItemAction -> menuItemAction,
				this::whenDuplicatedCodeKeepFirst));
		final Function<String, MenuItemAction> menuItemActionProvider = isQuickActionIsRemovedFromMenuBarWhenDisplayed() ? menuItemActionMap::remove : menuItemActionMap::get;

		final MenuBar menuBar = new BaseMenuBar(true, false);
		String menuBarRootCaption = getMenuBarRootCaption();
		final MenuItem rootItem = menuBar.addItem(menuBarRootCaption != null ? menuBarRootCaption : "", FontAwesome.ELLIPSIS_V, null);

		final Supplier<List<MenuItemAction>> useTheseActionsInMenuBarInsteadSupplier = getUseTheseActionsInMenuBarInsteadSupplier();
		final Supplier<List<MenuItemAction>> useTheseActionsInQuickActionInsteadSupplier = getUseTheseActionsInQuickActionInsteadSupplier();

		if (isQuickActionsAreVisible()) {

			if (useTheseActionsInQuickActionInsteadSupplier != USE_THESE_ACTIONS_IN_QUICK_ACTION_INSTEAD_SUPPLIER_DEFAULT) {
				List<MenuItemAction> useTheseActionsInQuickActionInstead = useTheseActionsInQuickActionInsteadSupplier.get();

				if (useTheseActionsInQuickActionInstead != null) {
					useTheseActionsInQuickActionInstead.sort(Comparator.comparing(MenuItemAction::getPriority));
					useTheseActionsInQuickActionInstead.stream().filter(action -> action.getType() == null).forEach(action -> action.setType("Type-was-null-so-set-random-type-" + UUID.randomUUID().toString()));

					Map<String, MenuItemAction> useTheseQuickActionMap = useTheseActionsInQuickActionInstead.stream().collect(Collectors.toMap(MenuItemAction::getType, menuItemAction -> menuItemAction, this::whenDuplicatedCodeKeepFirst));
					buildQuickActions(useTheseActionsInQuickActionInstead.stream().map(MenuItemAction::getType).collect(Collectors.toList()), useTheseQuickActionMap::get, menuItemRecordProvider, layout::addComponent);
				}
			} else if (menuDisplayList != null) {
				int quickActionCount = getQuickActionCount();
				quickActionCount = Math.max(quickActionCount, 0);

				this.buildQuickActionsWithMenuDisplayItems(menuDisplayList.getQuickActionList(menuItemActionMap, quickActionCount), menuItemActionProvider, menuItemRecordProvider, layout::addComponent);
			} else {
				buildQuickActions(menuItemActions.stream()
								.filter(menuItemAction -> menuItemAction.getState().getStatus() == MenuItemActionStateStatus.VISIBLE)
								.map(MenuItemAction::getType)
								.limit(QUICK_ACTION_COUNT_DEFAULT).collect(Collectors.toList()),
						menuItemActionProvider, menuItemRecordProvider, layout::addComponent);
			}
		}

		if (!isOnlyQuickActionAreVisible()) {
			if (useTheseActionsInMenuBarInsteadSupplier != USE_THESE_ACTIONS_IN_MENU_BAR_INSTEAD_SUPPLIER_DEFAULT) {
				List<MenuItemAction> useTheseActionsInMenuBarInstead = useTheseActionsInMenuBarInsteadSupplier.get();

				if (useTheseActionsInMenuBarInstead != null) {
					useTheseActionsInMenuBarInstead.sort(Comparator.comparing(MenuItemAction::getPriority));
					getMenuItemFactory().buildMenuBar(rootItem, useTheseActionsInMenuBarInstead, menuItemRecordProvider, this::actionExecuted, this::addCreateActionToMaps);
				}
			} else if (menuDisplayList != null) {
				getMenuItemFactory().buildMenuBar(
						rootItem,
						menuItemActionMap.entrySet().stream().map(Entry::getValue).collect(Collectors.toList()),
						menuItemRecordProvider,
						this::actionExecuted,
						menuDisplayList,

						this::addCreateActionToMaps);
			} else {
				getMenuItemFactory().buildMenuBar(rootItem, menuItemActionMap.entrySet().stream().map(Entry::getValue).collect(Collectors.toList()), menuItemRecordProvider, this::actionExecuted, this::addCreateActionToMaps);
			}


			if (rootItem.hasChildren()) {
				stylizeMenuBar(menuBar);
				layout.addComponent(menuBar);
			}
		}

		if (isHiddenWhenNoActionOrStateAreAllNotVisible()) {
			setVisible(layout.getComponentCount() > 0);
		}

		if (isVisible() && layout.getComponentCount() > 1) {
			layout.setSpacing(true);
		}

		setWidthUndefined();

		return layout;
	}

	public void refresh() {
		setCompositionRoot(buildMainComponent());
	}

	private void validateSuppliersState() {
		if (getMenuItemActionsSupplier() == null) {
			throw new IllegalStateException("getMenuItemActionsSupplier cannot return null");
		} else if (getMenuItemRecordProviderSupplier() == null) {
			throw new IllegalStateException("getMenuItemRecordProviderSupplier cannot return null");
		}
	}

	private MenuDisplayList getMenuDisplayList() {
		final AppLayerFactory appLayerFactory = getAppLayerFactory();
		final SessionContext sessionContext = getSessionContext();

		final Supplier<String> schemaTypeCodeSupplier = getSchemaTypeCodeSupplier();
		final String schemaTypeCode = schemaTypeCodeSupplier != null ? schemaTypeCodeSupplier.get() : null;

		MenuDisplayList menuDisplayList;

		if (appLayerFactory != null && sessionContext != null && schemaTypeCode != null) {
			MenusDisplayManager menusDisplayManager = appLayerFactory.getMenusDisplayManager();
			if (menusDisplayManager != null) {
				MenuDisplayListBySchemaType menuDisplayListBySchemaType = menusDisplayManager.getMenuDisplayList(sessionContext.getCurrentCollection());

				if (menuDisplayListBySchemaType != null) {
					menuDisplayList = menuDisplayListBySchemaType.getActionDisplayList(schemaTypeCode);
				} else {
					menuDisplayList = null;
				}
			} else {
				menuDisplayList = null;
			}
		} else {
			menuDisplayList = null;
		}

		return menuDisplayList;
	}

	private void stylizeMenuBar(MenuBar menuBar) {
		menuBar.setAutoOpen(false);
		menuBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
		if (StringUtils.isBlank(getMenuBarRootCaption())) {
			menuBar.addStyleName("no-caption-action-menu-bar");
		}
	}

	@NotNull
	private MenuItemRecordProvider buildDefaultMenuItemRecordProvider() {
		return new MenuItemRecordProvider() {
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

	private void buildQuickActionsWithMenuDisplayItems(List<MenuDisplayItem> quickActionList,
													   Function<String, MenuItemAction> menuItemActionProvider,
													   MenuItemRecordProvider menuItemRecordProvider,
													   Consumer<Component> componentCreatedCallback) {

		final List<MenuItemAction> quickActionMenuItemActions = quickActionList.stream()
				.map(MenuDisplayItem::getCode)
				.map(menuItemActionProvider)
				.filter(Objects::nonNull).collect(Collectors.toList());

		quickActionList
				.stream()
				.map(quickAction -> buildQuickActionComponent(quickActionMenuItemActions, quickAction, menuItemRecordProvider))
				.filter(Objects::nonNull)
				.forEach(componentCreatedCallback);
	}

	private void buildQuickActions(List<String> quickActionCodes,
								   Function<String, MenuItemAction> menuItemActionProvider,
								   MenuItemRecordProvider menuItemRecordProvider,
								   Consumer<Component> componentCreatedCallback) {
		quickActionCodes.stream()
				.map(menuItemActionProvider)
				.filter(Objects::nonNull)
				.map(menuItemAction -> {
					Component component = getMenuItemFactory().buildQuickAction(menuItemAction, menuItemActionMapping::get, menuItemRecordProvider, this::actionExecuted);
					addCreateActionToMaps(new Pair<>(menuItemAction, component));

					return component;
				})
				.filter(Objects::nonNull)
				.forEach(componentCreatedCallback);
	}

	@Nullable
	private Component buildQuickActionComponent(List<MenuItemAction> quickActionMenuItemActions,
												MenuDisplayItem quickAction,
												MenuItemRecordProvider menuItemRecordProvider) {
		MenuItemAction menuItemAction = quickActionMenuItemActions
				.stream()
				.filter(possibleMenuItemAction -> possibleMenuItemAction.getType().equals(quickAction.getCode()))
				.findFirst().orElse(null);

		Component component;
		if (menuItemAction != null) {
			component = getMenuItemFactory().buildQuickAction(menuItemAction, menuItemActionMapping::get, quickAction, menuItemRecordProvider, this::actionExecuted);
			addCreateActionToMaps(new Pair<>(menuItemAction, component));
		} else {
			component = null;
		}

		return component;
	}

	private void addCreateActionToMaps(Pair<MenuItemAction, Object> pair) {
		MenuItemAction menuItemAction = pair.getKey();
		String key = menuItemAction.getType();

		menuItemActionMapping.put(key, menuItemAction);
		menuItemActionComponentMapping.put(key, pair.getValue());
	}

	private <T> T whenDuplicatedCodeKeepFirst(T first, T second) {
		return first;
	}

	public boolean isQuickActionsAreVisible() {
		return quickActionsAreVisible;
	}

	public boolean isQuickActionIsRemovedFromMenuBarWhenDisplayed() {
		return quickActionIsRemovedFromMenuBarWhenDisplayed;
	}

	public boolean isHiddenWhenNoActionOrStateAreAllNotVisible() {
		return hiddenWhenNoActionOrStateAreAllNotVisible;
	}

	public Supplier<List<MenuItemAction>> getMenuItemActionsSupplier() {
		return menuItemActionsSupplier;
	}

	public Supplier<MenuItemRecordProvider> getMenuItemRecordProviderSupplier() {
		return menuItemRecordProviderSupplier;
	}

	public Supplier<String> getSchemaTypeCodeSupplier() {
		return schemaTypeCodeSupplier;
	}

	public String getMenuBarRootCaption() {
		return menuBarRootCaption;
	}

	public AppLayerFactory getAppLayerFactory() {
		return appLayerFactory;
	}

	public SessionContext getSessionContext() {
		return sessionContext;
	}

	public MenuItemFactory getMenuItemFactory() {
		return menuItemFactory;
	}

	public Supplier<List<MenuItemAction>> getUseTheseActionsInMenuBarInsteadSupplier() {
		return useTheseActionsInMenuBarInsteadSupplier;
	}

	public Supplier<List<MenuItemAction>> getUseTheseActionsInQuickActionInsteadSupplier() {
		return useTheseActionsInQuickActionInsteadSupplier;
	}

	public boolean isOnlyQuickActionAreVisible() {
		return onlyQuickActionAreVisible;
	}

	public int getQuickActionCount() {
		return quickActionCount;
	}

	public void actionExecuted(MenuItemAction menuItemAction, Object component) {
	}

	public Set<String> getAllActionTypes() {
		return Collections.unmodifiableSet(menuItemActionMapping.keySet());
	}

	public MenuItemAction getMenuItemActionByType(String type) {
		MenuItemActionBuilder copyBuilder = createMenuItemActionCopyBuilder(menuItemActionMapping.get(type));
		return copyBuilder != null ? copyBuilder.build() : null;
	}

	public final void updateAction(String type,
								   Function<MenuItemActionBuilder, MenuItemAction> modifyThisActionThenReturn) {
		if (menuItemActionMapping.containsKey(type)) {
			final MenuItemAction menuItemAction = menuItemActionMapping.get(type);
			final MenuItemAction updatedMenuItemAction = modifyThisActionThenReturn.apply(createMenuItemActionCopyBuilder(menuItemAction));

			if (updatedMenuItemAction != null) {
				final Object object = menuItemActionComponentMapping.get(type);
				MenuItemActionState state = updatedMenuItemAction.getState();

				if (object instanceof BaseButton) {
					final BaseButton button = (BaseButton) object;
					button.setCaption(updatedMenuItemAction.getCaption());
					button.setIcon(updatedMenuItemAction.getIcon());

					if (state != null) {
						button.setEnabled(state.getStatus() == VISIBLE);
						button.setVisible(state.getStatus() != HIDDEN);
						button.setDescription(state.getReason());
					} else {
						button.setVisible(true);
						button.setDescription(null);
					}

				} else if (object instanceof MenuItem) {
					final MenuItem menuItem = (MenuItem) object;

					menuItem.setText(updatedMenuItemAction.getCaption());
					menuItem.setIcon(updatedMenuItemAction.getIcon());

					if (state != null) {
						menuItem.setEnabled(state.getStatus() == VISIBLE);
						menuItem.setVisible(state.getStatus() != HIDDEN);
						menuItem.setDescription(state.getReason());
					} else {
						menuItem.setVisible(true);
						menuItem.setDescription(null);
					}
				}

				menuItemActionMapping.put(type, updatedMenuItemAction);
			}
		}
	}

	private MenuItemActionBuilder createMenuItemActionCopyBuilder(MenuItemAction menuItemAction) {
		return menuItemAction == null ? null : MenuItemAction
				.builder()
				.caption(menuItemAction.getCaption())
				.icon(menuItemAction.getIcon())
				.type(menuItemAction.getType())
				.state(new MenuItemActionState(menuItemAction.getState().getStatus(), menuItemAction.getState().getReason()))
				.command(menuItemAction.getCommand())
				.priority(menuItemAction.getPriority())
				.confirmMessage(menuItemAction.getConfirmMessage())
				.dialogMode(menuItemAction.getDialogMode())
				.group(menuItemAction.getGroup())
				.recordsLimit(menuItemAction.getRecordsLimit());

	}
}
