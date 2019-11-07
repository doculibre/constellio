package com.constellio.app.ui.framework.components.buttons;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus;
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
import com.vaadin.ui.Button;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;

public class RecordListActionButtonFactory {

	private MenuItemRecordProvider recordProvider;
	private List<String> excludedActionTypes;

	private SessionContext sessionContext;
	private String collection;

	private MenuItemServices menuItemServices;
	private MenuItemFactory menuItemFactory;
	private UserServices userServices;
	private BaseView view;

	public RecordListActionButtonFactory(MenuItemRecordProvider recordProvider, BaseView view,
										 List<String> excludedActionTypes) {
		super();
		this.recordProvider = recordProvider;
		this.view = view;
		this.excludedActionTypes = excludedActionTypes;
		initialize();
	}

	private void initialize() {
		sessionContext = ConstellioUI.getCurrentSessionContext();
		collection = sessionContext.getCurrentCollection();

		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		userServices = appLayerFactory.getModelLayerFactory().newUserServices();
		menuItemServices = new MenuItemServices(collection, appLayerFactory);
		menuItemFactory = new MenuItemFactory();
	}

	public List<Button> build() {
		List<MenuItemAction> menuItemActions = menuItemServices.getActionsForRecords(recordProvider.getRecords(), excludedActionTypes,
				new MenuItemActionBehaviorParams() {
					@Override
					public BaseView getView() {
						if (view == null) {
							return (BaseView) ConstellioUI.getCurrent().getCurrentView();
						} else {
							return view;
						}
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
		return menuItemFactory.buildActionButtons(menuItemActions, recordProvider, new CommandCallback() {
			@Override
			public void actionExecuted(MenuItemAction menuItemAction, Object component) {
				Button button = (Button) component;
				button.setEnabled(menuItemAction.getState().getStatus() != MenuItemActionStateStatus.DISABLED);
				button.setEnabled(menuItemAction.getState().getStatus() == MenuItemActionStateStatus.VISIBLE);
				//				View currentView = ConstellioUI.getCurrent().getCurrentView();
				//				// No point in refreshing menu if we left the original page
				//				if (currentView == originalView) {
				//					// Recursive call
				//					buildMenuItems();
				//				}
			}

		});
	}

}
