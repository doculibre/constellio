package com.constellio.app.modules.rm.ui.pages.shareManagement;

import com.constellio.app.services.menu.MenuItemFactory.MenuItemRecordProvider;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.PlaceHolder;
import com.constellio.app.ui.framework.components.selection.SelectionComponent.SelectionManager;
import com.constellio.app.ui.framework.components.viewers.panel.ViewableRecordVOTablePanel;
import com.constellio.app.ui.framework.containers.RecordVOContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.home.PartialRefresh;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class ShareContentListViewImpl extends BaseViewImpl implements ShareContentListView, PartialRefresh {

	public static final String SHARED_FOLDERS = "SharedFolders";
	public static final String SHARED_DOCUMENTS = "SharedDocuments";
	public static final String PUBLISH_DOCUMENTS = "publishDocuments";

	private TabSheet tabSheet;
	private RecordVOContainer currentSharedFolderContainers;
	private RecordVOContainer currentSharedDocumentsContainers;
	private RecordVOContainer currentPublishDocumentContainers;

	private ShareContentListPresenter presenter;
	Map<Tab, String> codeByTabs = new HashMap<>();

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

		Tab sharedFolderTab = tabSheet.addTab(new PlaceHolder(), $("ShareContentListPresenter.sharedFolder"));
		codeByTabs.put(sharedFolderTab, SHARED_FOLDERS);
		codeByTabs.put(tabSheet.addTab(new PlaceHolder(), $("ShareContentListPresenter.sharedDocument")), SHARED_DOCUMENTS);
		codeByTabs.put(tabSheet.addTab(new PlaceHolder(), $("ShareContentListPresenter.publishDocuments")), PUBLISH_DOCUMENTS);

		selectTab(sharedFolderTab);

		tabSheet.addSelectedTabChangeListener(new SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				selectTab(tabSheet.getTab(tabSheet.getSelectedTab()));
			}
		});

		verticalLayout.addComponent(tabSheet);
		verticalLayout.setMargin(new MarginInfo(false, true, false, true));

		return verticalLayout;
	}

	private void selectTab(Tab tab) {
		PlaceHolder tabComponent = (PlaceHolder) tab.getComponent();
		tabComponent.setCompositionRoot(null);

		String code = codeByTabs.get(tab);

		Component newTab = null;

		switch (code) {
			case SHARED_FOLDERS:
				newTab = buildSharedFolderTab();
				break;
			case SHARED_DOCUMENTS:
				newTab = buildSharedDocumentTab();
				break;
			case PUBLISH_DOCUMENTS:
				newTab = buildPublishedDocumentTab();
				break;
		}

		tabComponent.setCompositionRoot(newTab);
	}

	private Component buildPublishedDocumentTab() {
		ShareContentViewableRecordVOTablePanel recordVOTable = new ShareContentViewableRecordVOTablePanel(presenter.getPublishedDocumentDataProvider());
		currentPublishDocumentContainers = recordVOTable.getRecordVOContainer();

		recordVOTable.setSelectionActionButtons();

		recordVOTable.setSizeFull();

		return recordVOTable;
	}

	private Component buildSharedDocumentTab() {

		ShareContentViewableRecordVOTablePanel recordVOTable = new ShareContentViewableRecordVOTablePanel(presenter.getSharedDocumentDataProvider());
		currentSharedDocumentsContainers = recordVOTable.getRecordVOContainer();

		recordVOTable.setSelectionActionButtons();
		recordVOTable.setSizeFull();

		return recordVOTable;
	}

	private Component buildSharedFolderTab() {

		ShareContentViewableRecordVOTablePanel recordVOTable = new ShareContentViewableRecordVOTablePanel(presenter.getSharedFolderDataProvider());
		currentSharedFolderContainers = recordVOTable.getRecordVOContainer();

		recordVOTable.setSelectionActionButtons();
		recordVOTable.setSizeFull();

		return recordVOTable;
	}

	@Override
	public void doPartialRefresh() {
		Tab tab = tabSheet.getTab(tabSheet.getSelectedTab());

		String code = codeByTabs.get(tab);

		switch (code) {
			case SHARED_FOLDERS:
				currentSharedFolderContainers.forceRefresh();
				break;
			case SHARED_DOCUMENTS:
				currentSharedDocumentsContainers.forceRefresh();
				break;
			case PUBLISH_DOCUMENTS:
				currentPublishDocumentContainers.forceRefresh();
				break;
		}
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

		@Override
		protected boolean isPagedInListMode() {
			return true;
		}
	}
}
