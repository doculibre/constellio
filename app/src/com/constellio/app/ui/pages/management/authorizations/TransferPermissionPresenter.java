package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.pages.collection.CollectionUserView;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.security.AuthorizationsServices;
import com.vaadin.ui.Window;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class TransferPermissionPresenter extends SingleSchemaBasePresenter {
	private ArrayList<String> errorsList;
	private boolean removeUserAccess;
	private String recordId;

	public TransferPermissionPresenter(CollectionUserView view, String selectedUserRecord) {
		super(view, User.DEFAULT_SCHEMA);
		recordId = selectedUserRecord;
	}

	public void copyUserAuthorizations(Record sourceUserRecord, List<String> destUsers) throws Exception {
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
		} catch (Exception e) {
			displayErrorMessage();
		}
	}

	public void validateAccessTransfer(Record sourceUser, List<String> destUsers) throws Exception {
		errorsList = new ArrayList<>();
		if (destUsers.isEmpty()) {
			errorsList.add($("TransferPermissionsButton.emptyDestinationListError"));
		}
		if (destUsers.contains(sourceUser.getId())) {
			errorsList.add($("TransferPermissionsButton.userCannotBeSelected", sourceUser.getTitle()));
		}
		if (!isDeletionEnabled() && removeUserAccess) {
			errorsList.add($("TransferPermissionsButton.cannotRemovePermissions", sourceUser.getTitle()));
		}

		if (errorsList.size() > 0) {
			throw new Exception(errorsList.toString());
		}
	}

	public void displayErrorMessage() {
		String errorString = "";
		for (String errorMessage : errorsList) {
			errorString += errorMessage;
		}
		view.showErrorMessage(errorString);
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


	public ArrayList<String> getErrorsList() {
		return errorsList;
	}

	private void updateAuthorizationPrincipalsList(Authorization authorization, List<String> newPrincipalsList) {
		authorization.setPrincipals(newPrincipalsList);
		addOrUpdate(authorization.getWrappedRecord());
	}

	public void copyUserGroups(RecordVO sourceUserVO, List<String> destUsers) {
		User sourceUser = wrapUser(sourceUserVO.getRecord());
		List<String> groupsList = sourceUser.getUserGroups();

		for (String destUserId : destUsers) {
			User user = coreSchemas().getUser(destUserId);
			user.setUserGroups(groupsList);
			addOrUpdate(user.getWrappedRecord());
		}
	}

	public void copyUserRoles(RecordVO sourceUserVO, List<String> destUsers) {
		User sourceUser = wrapUser(sourceUserVO.getRecord());
		List<String> rolesList = sourceUser.getUserRoles();

		for (String destUserId : destUsers) {
			User user = coreSchemas().getUser(destUserId);
			user.setUserRoles(rolesList);
			addOrUpdate(user.getWrappedRecord());
		}
	}

	private void removeAllRolesOfUser(RecordVO userVO) {
		User user = wrapUser(userVO.getRecord());
		user.setUserRoles("");
		addOrUpdate(user.getWrappedRecord());
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return false;
	}
}
