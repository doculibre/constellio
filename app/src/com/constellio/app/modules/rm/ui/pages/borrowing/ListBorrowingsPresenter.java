package com.constellio.app.modules.rm.ui.pages.borrowing;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.ui.Component;

public class ListBorrowingsPresenter extends BasePresenter<ListBorrowingsView> {
	private ListBorrowingsTab documentTab;
	private ListBorrowingsTab folderTab;
	private ListBorrowingsTab containerTab;

	public ListBorrowingsPresenter(ListBorrowingsView view) {
		super(view);

		documentTab = new ListBorrowingsDocumentTab(appLayerFactory, view.getSessionContext());
		folderTab = new ListBorrowingsFolderTab(appLayerFactory, view.getSessionContext());
		containerTab = new ListBorrowingsContainerTab(appLayerFactory, view.getSessionContext());
	}

	public ListBorrowingsTab getDocumentTab() {
		return documentTab;
	}

	public ListBorrowingsTab getFolderTab() {
		return folderTab;
	}

	public ListBorrowingsTab getContainerTab() {
		return containerTab;
	}

	public void refreshTabs(Component selectedTab, String administrativeUnit, boolean showOnlyOverdue) {
		documentTab.refresh(selectedTab, administrativeUnit, showOnlyOverdue);
		folderTab.refresh(selectedTab, administrativeUnit, showOnlyOverdue);
		containerTab.refresh(selectedTab, administrativeUnit, showOnlyOverdue);
	}

	public void backButtonClicked() {
		view.navigate().to().adminModule();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.MANAGE_BORROWINGS).globally();
	}
}
