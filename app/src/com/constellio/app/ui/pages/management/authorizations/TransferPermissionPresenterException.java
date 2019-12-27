package com.constellio.app.ui.pages.management.authorizations;

import static com.constellio.app.ui.i18n.i18n.$;

public class TransferPermissionPresenterException extends Exception {
	public TransferPermissionPresenterException(String message) {
		super(message);
	}

	public static class TransferPermissionPresenterException_EmptyDestinationList extends TransferPermissionPresenterException {
		public TransferPermissionPresenterException_EmptyDestinationList() {
			super($("TransferPermissionsPresenterException.emptyDestinationListError"));
		}
	}

	public static class TransferPermissionPresenterException_CannotSelectUser extends TransferPermissionPresenterException {
		public TransferPermissionPresenterException_CannotSelectUser(String user) {
			super($("TransferPermissionsPresenterException.userCannotBeSelected", user));
		}
	}

	public static class TransferPermissionPresenterException_CannotRemovePermission extends TransferPermissionPresenterException {
		public TransferPermissionPresenterException_CannotRemovePermission(String user) {
			super($("TransferPermissionsPresenterException.cannotRemovePermissions", user));
		}
	}

	public static class TransferPermissionPresenterException_CannotUpdateUser extends TransferPermissionPresenterException {
		public TransferPermissionPresenterException_CannotUpdateUser() {
			super($("TransferPermissionsPresenterException.cannotUpdateUser"));
		}
	}
}
