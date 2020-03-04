package com.constellio.app.modules.rm.ui.pages.shareManagement;

import com.constellio.app.services.menu.MenuItemFactory.MenuItemRecordProvider;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.selection.SelectionComponent.SelectionManager;
import com.constellio.app.ui.framework.components.viewers.panel.ViewableRecordVOTablePanel;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ShareContentListViewImpl extends BaseViewImpl implements ShareContentListView {

	private TabSheet tabSheet;

	private ShareContentListPresenter presenter;

	public ShareContentListViewImpl() {
		presenter = new ShareContentListPresenter(this);
	}

	@Override
	protected String getTitle() {
		UserVO userVO = presenter.getUserVO();
		return $("ListContentShareView.viewTitle", userVO.getFirstName() + " " + userVO.getLastName());
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout verticalLayout = new VerticalLayout();

		tabSheet = new TabSheet();

		tabSheet.addTab(buildFolderTab(), $("ShareContentListPresenter.sharedFolder"));
		tabSheet.addTab(buildDocumentTab(), $("ShareContentListPresenter.sharedDocument"));
		tabSheet.addTab(buildPublished(), $("ShareContentListPresenter.publishDocuments"));

		verticalLayout.addComponent(tabSheet);
		verticalLayout.setMargin(new MarginInfo(false, true, false, true));

		return verticalLayout;
	}

	private Component buildPublished() {
		ViewableRecordVOTablePanel recordVOTable = new ShareContentViewableRecordVOTablePanel(presenter.getPublishedDocumentDataProvider());

		recordVOTable.setSelectionActionButtons();

		recordVOTable.setSizeFull();

		return recordVOTable;
	}

	private Component buildDocumentTab() {

		ShareContentViewableRecordVOTablePanel recordVOTable = new ShareContentViewableRecordVOTablePanel(presenter.getSharedDocumentDataProvider());

		recordVOTable.setSelectionActionButtons();
		recordVOTable.setSizeFull();

		return recordVOTable;
	}

	private Component buildFolderTab() {

		ShareContentViewableRecordVOTablePanel recordVOTable = new ShareContentViewableRecordVOTablePanel(presenter.getSharedFolderDataProvider());
		recordVOTable.setSelectionActionButtons();
		recordVOTable.setSizeFull();

		return recordVOTable;
	}

	private class ShareContentViewableRecordVOTablePanel extends ViewableRecordVOTablePanel {
		public ShareContentViewableRecordVOTablePanel(RecordVODataProvider recordVODataProvider) {
			super(new RecordVOLazyContainer(recordVODataProvider));
		}

		@Override
		protected boolean isSelectColumn() {
			return true;
		}

		@Override
		protected SelectionManager newSelectionManager() {
			return new ShareContentSelectionManager(getRecordVOContainer());
		}

		@Override
		protected MenuItemRecordProvider getMenuItemProvider() {
			return new MenuItemRecordProvider() {
				@Override
				public List<Record> getRecords() {
					return getSelectedRecords();
				}

				@Override
				public LogicalSearchQuery getQuery() {
					return null;
				}
			};
		}
	}
}
