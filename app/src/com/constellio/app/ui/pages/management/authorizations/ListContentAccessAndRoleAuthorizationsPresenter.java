package com.constellio.app.ui.pages.management.authorizations;

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
}
