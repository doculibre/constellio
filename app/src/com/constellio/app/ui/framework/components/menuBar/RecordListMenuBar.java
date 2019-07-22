package com.constellio.app.ui.framework.components.menuBar;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemFactory;
import com.constellio.app.services.menu.MenuItemFactory.CommandCallback;
import com.constellio.app.services.menu.MenuItemFactory.MenuItemRecordProvider;
import com.constellio.app.services.menu.MenuItemServices;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.users.UserServices;
import com.vaadin.navigator.View;
import com.vaadin.server.FontAwesome;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;

public class RecordListMenuBar extends BaseMenuBar {

	private SessionContext sessionContext;
	private String collection;

	private MenuItemServices menuItemServices;
	private MenuItemFactory menuItemFactory;
	private UserServices userServices;

	private MenuItemRecordProvider recordProvider;
	private String rootItemCaption;
	private List<String> excludedActionTypes;

	public RecordListMenuBar(MenuItemRecordProvider recordProvider, String rootItemCaption,
							 List<String> excludedActionTypes) {
		super(true, false);
		this.recordProvider = recordProvider;
		this.rootItemCaption = rootItemCaption;
		this.excludedActionTypes = excludedActionTypes;

		sessionContext = ConstellioUI.getCurrentSessionContext();
		collection = sessionContext.getCurrentCollection();

		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		userServices = appLayerFactory.getModelLayerFactory().newUserServices();
		menuItemServices = new MenuItemServices(collection, appLayerFactory);
		menuItemFactory = new MenuItemFactory();

		buildMenuItems();
	}

	public void buildMenuItems() {
		removeItems();

		MenuItem rootItem = addItem(rootItemCaption, FontAwesome.ELLIPSIS_V, null);

		List<MenuItemAction> menuItemActions = menuItemServices.getActionsForRecords(recordProvider.getRecords(), excludedActionTypes,
				new MenuItemActionBehaviorParams() {
					@Override
					public BaseView getView() {
						return (BaseView) ConstellioUI.getCurrent().getCurrentView();
					}

					@Override
					public Map<String, String> getFormParams() {
						return MapUtils.emptyIfNull(ParamUtils.getCurrentParams());
					}

					@Override
					public User getUser() {
						return userServices.getUserInCollection(sessionContext.getCurrentUser().getUsername(), collection);
					}
				});

		final View originalView = ConstellioUI.getCurrent().getCurrentView();
		menuItemFactory.buildMenuBar(rootItem, menuItemActions, recordProvider, new CommandCallback() {
			@Override
			public void actionExecuted(MenuItemAction menuItemAction) {
				View currentView = ConstellioUI.getCurrent().getCurrentView();
				// No point in refreshing menu if we left the original page
				if (currentView == originalView) {
					// Recursive call
					buildMenuItems();
				}
			}

		});
	}

}
