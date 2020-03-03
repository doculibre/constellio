package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.security.AuthorizationsServices;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;

public class ShareContentListPresenter extends BasePresenter<ShareContentListViewImpl> {

	private TabSheet tabSheet;

	private transient AuthorizationsServices authorizationsServices;

	public ShareContentListPresenter(ShareContentListViewImpl view) {
		super(view);

		tabSheet = new TabSheet();

		tabSheet.addTab(buildFolderTab());
		tabSheet.addTab(buildDocumentTab());
		//		tabSheet.addTab()
	}

	private Component buildFolderTab() {
		return null;
	}

	private Component buildDocumentTab() {
		return null;
	}

	private Component buildRecentItemsTab() {
		return null;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_SHARE).globally() || user.has(CorePermissions.MANAGE_GLOBAL_LINKS).globally();
	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		return true;
	}

	protected UserVO getUserVO() {
		return view.getSessionContext().getCurrentUser();
	}

	protected AuthorizationsServices authorizationsServices() {
		if (authorizationsServices == null) {
			authorizationsServices = modelLayerFactory.newAuthorizationsServices();
		}
		return authorizationsServices;
	}
}