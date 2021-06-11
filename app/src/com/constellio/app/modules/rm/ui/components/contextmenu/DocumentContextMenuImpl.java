package com.constellio.app.modules.rm.ui.components.contextmenu;

import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.actionDisplayManager.MenuDisplayList;
import com.constellio.app.services.actionDisplayManager.MenusDisplayManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemFactory;
import com.constellio.app.services.menu.MenuItemFactory.MenuItemRecordProvider;
import com.constellio.app.services.menu.MenuItemServices;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.content.UpdateContentVersionWindowImpl;
import com.constellio.app.ui.framework.components.content.UpdateContentVersionWindowImpl.ValidateFileName;
import com.constellio.app.ui.framework.components.contextmenu.RecordContextMenu;
import com.constellio.app.ui.framework.containers.RefreshableContainer;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.data.Container;
import com.vaadin.server.ClientConnector;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.modules.rm.services.menu.behaviors.util.DocumentUtil.getEmailDocumentFileNameValidator;
import static com.constellio.app.ui.i18n.i18n.$;

public class DocumentContextMenuImpl extends RecordContextMenu implements DocumentContextMenu {

	private boolean visible;
	private RecordVO recordVO;
	private ContentVersionVO contentVersionVO;
	private UpdateContentVersionWindowImpl updateWindow;
	private String borrowedMessage;

	protected DocumentContextMenuPresenter presenter;

	public DocumentContextMenuImpl() {
		this(null);
	}

	public DocumentContextMenuImpl(DocumentVO documentVO) {
		presenter = newPresenter();
		setRecordVO(documentVO);
		if (documentVO != null) {
			presenter.setRecordVO(documentVO);
		}
	}

	protected DocumentContextMenuPresenter newPresenter() {
		return new DocumentContextMenuPresenter(this);
	}

	public final boolean isVisible() {
		return visible;
	}

	public final void setVisible(boolean visible) {
		if (this.visible && !visible) {
			removeAllItems();
		}
		this.visible = visible;
	}

	@Override
	public boolean openFor(String recordId) {
		return presenter.openForRequested(recordId);
	}

	@Override
	public boolean openFor(RecordVO recordVO) {
		return presenter.openForRequested(recordVO);
	}

	@Override
	public void buildMenuItems() {
		removeAllItems();

		if (StringUtils.isNotBlank(borrowedMessage)) {
			addItem(borrowedMessage);
		}

		// FIXME quick test only
		final AppLayerFactory appLayerFactory = getConstellioFactories().getAppLayerFactory();
		final MenusDisplayManager menusDisplayManager = appLayerFactory.getMenusDisplayManager();
		final MenuDisplayList menuDisplayList = menusDisplayManager.getMenuDisplayList(presenter.getCollection()).getActionDisplayList(recordVO.getSchema().getTypeCode());


		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		Record record = recordServices.getDocumentById(recordVO.getId());

		List<MenuItemAction> menuItemActions = new MenuItemServices(record.getCollection(), appLayerFactory)
				.getActionsForRecord(record, new MenuItemActionBehaviorParams() {
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
						return contentVersionVO;
					}

					@Override
					public Map<String, String> getFormParams() {
						return MapUtils.emptyIfNull(ParamUtils.getCurrentParams());
					}

					@Override
					public User getUser() {
						return presenter.getCurrentUser();
					}

					@Override
					public boolean isContextualMenu() {
						return true;
					}
				});

		new MenuItemFactory().buildContextMenu(this, menuItemActions, new MenuItemRecordProvider() {
			@Override
			public List<Record> getRecords() {
				return Collections.singletonList(recordVO.getRecord());
			}

			@Override
			public LogicalSearchQuery getQuery() {
				return null;
			}
		}, menuDisplayList);
	}

	@Override
	public ConstellioFactories getConstellioFactories() {
		return ConstellioFactories.getInstance();
	}

	@Override
	public CoreViews navigateTo() {
		return ConstellioUI.getCurrent().navigateTo();
	}

	@Override
	public Navigation navigate() {
		Navigation navigation = ConstellioUI.getCurrent().navigate();
		for (Window window : new ArrayList<>(ConstellioUI.getCurrent().getWindows())) {
			window.close();
		}
		return navigation;
	}

	@Override
	public void showMessage(String message) {
		Notification.show(message, Type.WARNING_MESSAGE);
	}

	@Override
	public void showErrorMessage(String errorMessage) {
		Notification.show(errorMessage, Type.ERROR_MESSAGE);
	}

	@Override
	public SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

	@Override
	public UIContext getUIContext() {
		return ConstellioUI.getCurrent();
	}

	@Override
	public void setRecordVO(RecordVO documentVO) {
		this.recordVO = documentVO;
	}

	private void initUploadWindow() {
		Map<RecordVO, MetadataVO> record = new HashMap<>();
		record.put(recordVO, recordVO.getMetadata(Document.CONTENT));

		ValidateFileName validateFileName = getEmailDocumentFileNameValidator(recordVO.getSchemaCode());

		updateWindow = new UpdateContentVersionWindowImpl(record, false, validateFileName) {
			@Override
			public void close() {
				super.close();
				presenter.updateWindowClosed();
				postClose();
			}
		};
	}

	public void postClose() {
		refreshParent();
	}

	@Override
	public void openUploadWindow(boolean checkingIn) {
		initUploadWindow();
		updateWindow.open(checkingIn);
	}



	@Override
	public void setContentVersionVO(ContentVersionVO contentVersionVO) {
		this.contentVersionVO = contentVersionVO;
	}

	@Override
	public void setBorrowedMessage(String borrowedMessageKey, String... args) {
		if (StringUtils.isNotBlank(borrowedMessageKey)) {
			borrowedMessage = $(borrowedMessageKey, (Object[]) args);
		} else {
			borrowedMessage = null;
		}
	}

	@Override
	public void hide() {
		super.hide();
	}

	@Override
	public void open(Component component) {
		super.open(component);
	}

	@Override
	public void open(int x, int y) {
		if (visible) {
			super.open(x, y);
		}
	}

	@Override
	public void openAgentURL(String agentURL) {
		Page.getCurrent().open(agentURL, "_top");
	}

	@Override
	public void refreshParent() {
		ClientConnector parent = getParent();
		if (parent instanceof Table) {
			Container container = ((Table) parent).getContainerDataSource();
			if (container instanceof RefreshableContainer) {
				((RefreshableContainer) container).forceRefresh();
			}
		}
	}

}
