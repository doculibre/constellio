package com.constellio.app.modules.rm.ui.pages.shareManagement;

import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

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
		RecordVOTable recordVOTable = new RecordVOTable(presenter.getPublishedDocumentDataProvider());
		recordVOTable.setSizeFull();

		return recordVOTable;
	}

	private Component buildDocumentTab() {

		RecordVOTable recordVOTable = new RecordVOTable(presenter.getSharedDocumentDataProvider());
		recordVOTable.setSizeFull();

		return recordVOTable;
	}

	private Component buildFolderTab() {
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery();

		RecordVOTable recordVOTable = new RecordVOTable(presenter.getSharedFolderDataProvider());
		recordVOTable.setSizeFull();

		return recordVOTable;
	}


	public boolean isViewReadOnly() {
		return false;
	}
}
