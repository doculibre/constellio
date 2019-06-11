package com.constellio.app.ui.framework.components.contextmenu;

import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemFactory;
import com.constellio.app.services.menu.MenuItemServices;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.UserDocumentVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;

public class RecordVOContextMenu extends BaseContextMenu {
	private RecordVO recordVO;
	private SessionContext sessionContext;
	private String collection;

	private MenuItemServices menuItemServices;
	private MenuItemFactory menuItemFactory;
	private UserServices userServices;

	public RecordVOContextMenu(RecordVO recordVO) {
		super();
		this.recordVO = recordVO;

		sessionContext = ConstellioUI.getCurrentSessionContext();
		collection = sessionContext.getCurrentCollection();

		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		userServices = appLayerFactory.getModelLayerFactory().newUserServices();
		menuItemServices = new MenuItemServices(collection, appLayerFactory);
		menuItemFactory = new MenuItemFactory();
	}

	public void build() {
		List<MenuItemAction> menuItemActions = menuItemServices.getActionsForRecord(recordVO.getRecord(),
				new MenuItemActionBehaviorParams() {
					@Override
					public BaseView getView() {
						return (BaseView) ConstellioUI.getCurrent().getCurrentView();
					}

					@Override
					public RecordVO getRecordVO() {
						return recordVO;
					}

					@Override
					public ContentVersionVO getContentVersionVO() {
						if (recordVO instanceof DocumentVO) {
							DocumentVO documentVO = (DocumentVO) recordVO;
							return documentVO.getContent();
						} else if (recordVO instanceof UserDocumentVO) {
							UserDocumentVO userDocumentVO = (UserDocumentVO) recordVO;
							return userDocumentVO.getContent();
						}
						return null;
					}

					@Override
					public Map<String, String> getFormParams() {
						return MapUtils.emptyIfNull(ParamUtils.getCurrentParams());
					}

					@Override
					public User getUser() {
						return userServices.getUserInCollection(sessionContext.getCurrentUser().getUsername(), collection);
					}

					@Override
					public boolean isContextualMenu() {
						return true;
					}

					@Override
					public boolean isNestedView() {
						// FIXME how do we determine this?
						return false;
					}
				});

		menuItemFactory.buildContextMenu(this, menuItemActions);
	}
}
