package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.pages.management.authorizations.TransferPermissionPresenterException.TransferPermissionPresenterException_CannotRemovePermission;
import com.constellio.app.ui.pages.management.authorizations.TransferPermissionPresenterException.TransferPermissionPresenterException_CannotSelectUser;
import com.constellio.app.ui.pages.management.authorizations.TransferPermissionPresenterException.TransferPermissionPresenterException_CannotUpdateUser;
import com.constellio.app.ui.pages.management.authorizations.TransferPermissionPresenterException.TransferPermissionPresenterException_EmptyDestinationList;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.security.AuthorizationsServices;
import com.vaadin.ui.Window;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class TransferPermissionPresenter extends SingleSchemaBasePresenter {
	private boolean removeUserAccess;
	private String recordId;

	public TransferPermissionPresenter(BaseView view, String selectedUserRecord) {
		super(view, User.DEFAULT_SCHEMA);
		recordId = selectedUserRecord;
	}

	public void copyUserAuthorizations(Record sourceUserRecord, List<String> destUsers)
			throws TransferPermissionPresenterException {
		validateAccessTransfer(sourceUserRecord, destUsers);
		List<Authorization> authorizationsList = getUserAuthorizationsList(sourceUserRecord);
		for (Authorization authorization : authorizationsList) {
			ArrayList<String> newPrincipalsList = new ArrayList<>();
			newPrincipalsList.addAll(authorization.getPrincipals());
			for (String destUserId : destUsers) {
				if (!authorization.getPrincipals().contains(destUserId)) {
					newPrincipalsList.add(destUserId);
				}
			}
			updateAuthorizationPrincipalsList(authorization, newPrincipalsList);
		}
	}

	public void forRequestParams(String parameters) {
		recordId = parameters;
	}

	public RecordVO getUser() {
		return presenterService().getRecordVO(recordId, VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	public String buildTransferRightsConfirmMessage(String sourceUserName, String selectedUsersString,
													boolean multipleUsersSelected, boolean removeUserAccess) {
		String confirmMessage;
		if (multipleUsersSelected) {
			if (removeUserAccess) {
				confirmMessage = $("TransferPermissionsButton.confirmMessageMultipleAndRemoveAccess", sourceUserName, selectedUsersString);
			} else {
				confirmMessage = $("TransferPermissionsButton.confirmMessageMultiple", sourceUserName, selectedUsersString);
			}
		} else {
			if (removeUserAccess) {
				confirmMessage = $("TransferPermissionsButton.confirmMessageSingleAndRemoveAccess", sourceUserName, selectedUsersString);
			} else {
				confirmMessage = $("TransferPermissionsButton.confirmMessageSingle", sourceUserName, selectedUsersString);
			}
		}
		return confirmMessage;
	}

	public List<String> convertUserIdListToUserNames(List<String> userIdList) {
		ArrayList<String> namesList = new ArrayList<>();
		for (String userId : userIdList) {
			Record userRecord = presenterService().getRecord(userId);
			namesList.add(userRecord.getTitle());
		}
		return namesList;
	}

	public void removeAllAuthorizationsOfUser(RecordVO userVO) {
		String userID = userVO.getId();
		List<Authorization> authorizationsList = getUserAuthorizationsList(userVO.getRecord());
		ArrayList<String> modifiedPrincipalsList = new ArrayList<String>();
		for (Authorization authorization : authorizationsList) {
			List<String> principalsList = authorization.getPrincipals();
			for (String principal : principalsList) {
				if (!principal.equals(userID)) {
					modifiedPrincipalsList.add(principal);
				}
			}
			updateAuthorizationPrincipalsList(authorization, modifiedPrincipalsList);
		}

	}

	public void transferAccessSaveButtonClicked(RecordVO sourceUser, List<String> destUsers, Window window) {
		try {
			validateAccessTransfer(sourceUser.getRecord(), destUsers);
			copyUserAuthorizations(sourceUser.getRecord(), destUsers);
			copyUserGroups(sourceUser, destUsers);
			if (removeUserAccess) {
				removeAllAuthorizationsOfUser(sourceUser);
			}
			window.close();
		} catch (TransferPermissionPresenterException e) {
			displayErrorMessage(e.getMessage());
		}
	}

	public void validateAccessTransfer(Record sourceUser, List<String> destUsers)
			throws TransferPermissionPresenterException {
		if (destUsers.isEmpty()) {
			throw new TransferPermissionPresenterException_EmptyDestinationList();
		}
		if (destUsers.contains(sourceUser.getId())) {
			throw new TransferPermissionPresenterException_CannotSelectUser(sourceUser.getTitle());
		}
		if (!isDeletionEnabled() && removeUserAccess) {
			throw new TransferPermissionPresenterException_CannotRemovePermission(sourceUser.getTitle());
		}
	}

	public void displayErrorMessage(String message) {
		view.showErrorMessage(message);
	}

	public List<Authorization> getUserAuthorizationsList(Record userVO) {
		AuthorizationsServices authorizationsServices = modelLayerFactory.newAuthorizationsServices();
		User userRecord = wrapUser(userVO);
		return authorizationsServices.getRecordAuthorizations(userRecord.getWrappedRecord());
	}

	public void setRemoveUserAccessCheckboxValue(boolean removeUserAccess) {
		this.removeUserAccess = removeUserAccess;
	}


	public boolean isDeletionEnabled() {
		return !getCurrentUser().getId().equals(recordId);
	}


	private void updateAuthorizationPrincipalsList(Authorization authorization, List<String> newPrincipalsList) {
		authorization.setPrincipals(newPrincipalsList);
		addOrUpdate(authorization.getWrappedRecord());
	}

	public void copyUserGroups(RecordVO sourceUserVO, List<String> destUsers)
			throws TransferPermissionPresenterException {
		User sourceUser = wrapUser(sourceUserVO.getRecord());
		List<String> groupsList = sourceUser.getUserGroups();
		Transaction transaction = new Transaction();
		for (String destUserId : destUsers) {
			User user = coreSchemas().getUser(destUserId);
			user.setUserGroups(groupsList);
			transaction.add(user.getWrappedRecord());
		}
		try {
			schemaPresenterUtils.recordServices().execute(transaction);
		} catch (RecordServicesException e) {
			throw new TransferPermissionPresenterException_CannotUpdateUser();
		}
	}


	@Override
	protected boolean hasPageAccess(String params, User user) {
		return false;
	}
}
