package com.constellio.app.ui.framework.components.menuBar;

import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemFactory;
import com.constellio.app.services.menu.MenuItemFactory.CommandCallback;
import com.constellio.app.services.menu.MenuItemFactory.MenuItemRecordProvider;
import com.constellio.app.services.menu.MenuItemServices;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.UserDocumentVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServices;
import com.vaadin.server.FontAwesome;
import org.apache.commons.collections4.MapUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RecordVOMenuBar extends BaseMenuBar {

	private RecordVO recordVO;
	private List<String> excludedActionTypes;
	
	private SessionContext sessionContext;
	private String collection;

	private MenuItemServices menuItemServices;
	private MenuItemFactory menuItemFactory;
	private UserServices userServices;

	public RecordVOMenuBar(RecordVO recordVO, List<String> excludedActionTypes) {
		super(true, false);
		this.recordVO = recordVO;
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

		MenuItem rootItem = addItem("", FontAwesome.ELLIPSIS_V, null);

		List<MenuItemAction> menuItemActions = menuItemServices.getActionsForRecord(recordVO.getRecord(), excludedActionTypes,
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
						} else if (recordVO.get(Document.CONTENT) != null) {
							return recordVO.get(Document.CONTENT);
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
				});

		menuItemFactory.buildMenuBar(rootItem, menuItemActions, new MenuItemRecordProvider() {
			@Override
			public List<Record> getRecords() {
				return Arrays.asList(recordVO.getRecord());
			}

			@Override
			public LogicalSearchQuery getQuery() {
				return null;
			}
		}, new CommandCallback() {
			@Override
			public void actionExecuted(MenuItemAction menuItemAction, Object component) {
				if (isAttached()) {
					// Recursive call
					buildMenuItems();
				}
			}
		});
	}

}
