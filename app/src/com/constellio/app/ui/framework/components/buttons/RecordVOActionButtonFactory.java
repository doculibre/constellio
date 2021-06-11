package com.constellio.app.ui.framework.components.buttons;

import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus;
import com.constellio.app.services.menu.MenuItemFactory;
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
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServices;
import com.vaadin.ui.Button;
import org.apache.commons.collections4.MapUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RecordVOActionButtonFactory {

	private RecordVO recordVO;
	private BaseView view;
	private List<String> excludedActionTypes;
	
	private SessionContext sessionContext;
	private String collection;

	private MenuItemServices menuItemServices;
	private MenuItemFactory menuItemFactory;
	private UserServices userServices;
	private RecordServices recordServices;
	private Object objectRecordVO = null;

	public RecordVOActionButtonFactory(RecordVO recordVO, List<String> excludedActionTypes) {
		this(recordVO, null, excludedActionTypes);
	}

	public RecordVOActionButtonFactory(RecordVO recordVO, BaseView view, List<String> excludedActionTypes) {
		super();
		this.recordVO = recordVO;
		this.view = view;
		this.excludedActionTypes = excludedActionTypes;
		initialize();
	}

	private void initialize() {
		sessionContext = ConstellioUI.getCurrentSessionContext();
		collection = sessionContext.getCurrentCollection();

		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		userServices = appLayerFactory.getModelLayerFactory().newUserServices();
		recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		menuItemServices = new MenuItemServices(collection, appLayerFactory);
		menuItemFactory = new MenuItemFactory();
	}

	public RecordVOActionButtonFactory(Object objectRecordVO) {
		this.objectRecordVO = objectRecordVO;
		initialize();
	}

	public List<Button> build() {
		List<MenuItemAction> menuItemActions = buildMenuItemActions();

		return menuItemFactory.buildActionButtons(menuItemActions, buildMenuItemRecordProvider(), (menuItemAction, component) -> {
			Button button = (Button) component;
			button.setEnabled(menuItemAction.getState().getStatus() != MenuItemActionStateStatus.DISABLED);
			button.setEnabled(menuItemAction.getState().getStatus() == MenuItemActionStateStatus.VISIBLE);
		});
	}

	@NotNull
	public MenuItemRecordProvider buildMenuItemRecordProvider() {
		return new MenuItemRecordProvider() {
			@Override
			public List<Record> getRecords() {
				if (recordVO == null) {
					return Collections.emptyList();
				}
				return Collections.singletonList(recordVO.getRecord());
			}

			@Override
			public LogicalSearchQuery getQuery() {
				return null;
			}
		};
	}

	public List<MenuItemAction> buildMenuItemActions() {
		Record record = null;

		if (recordVO != null) {
			record = recordServices.getDocumentById(recordVO.getId());
		}

		return menuItemServices.getActionsForRecord(record, excludedActionTypes,
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
					public Object getObjectRecordVO() {
						return objectRecordVO;
					}
				});
	}

}
