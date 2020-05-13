package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentContainerBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;

public class ListContentAccessAndRoleAuthorizationsPresenter extends ListContentAccessAuthorizationsPresenter {

	private boolean isCurrentlyViewingAccesses = true;

	public ListContentAccessAndRoleAuthorizationsPresenter(ListContentAccessAndRoleAuthorizationsView view) {
		super(view);
	}

	@Override
	public boolean seeRolesField() {
		return !isCurrentlyViewingAccesses;
	}

	@Override
	public boolean seeAccessField() {
		return isCurrentlyViewingAccesses;
	}

	public void viewAccesses() {
		isCurrentlyViewingAccesses = true;
	}

	public void viewRoles() {
		isCurrentlyViewingAccesses = false;
	}

	public boolean isCurrentlyViewingAccesses() {
		return isCurrentlyViewingAccesses;
	}

	public BaseBreadcrumbTrail getBreadCrumbTrail() {
		String favGroupId = view.getUIContext().getAttribute(BaseBreadcrumbTrail.FAV_GROUP_ID);
		FolderDocumentContainerBreadcrumbTrail breadcrumbTrail =
				new FolderDocumentContainerBreadcrumbTrail(recordId, null, null, favGroupId, view);
		view.getUIContext().clearAttribute(BaseBreadcrumbTrail.RECORD_AUTHORIZATIONS_TYPE);
		view.getUIContext().clearAttribute(BaseBreadcrumbTrail.FAV_GROUP_ID);
		return breadcrumbTrail;
	}
}
