package com.constellio.app.ui.pages.collection;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.RoleVO;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.pages.management.authorizations.TransferPermissionPresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.users.UserServices;
import com.vaadin.ui.Window;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class CollectionUserPresenter extends SingleSchemaBasePresenter<CollectionUserView> implements TransferPermissionPresenter {
	String recordId;
	ArrayList<String> errorsList;

	public CollectionUserPresenter(CollectionUserView view) {
		super(view, User.DEFAULT_SCHEMA);
	}

	public void forRequestParams(String parameters) {
		recordId = parameters;
	}

	public RecordVO getUser() {
		return presenterService().getRecordVO(recordId, VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	public String getRoleTitle(String roleCode) {
		return roleManager().getRole(view.getCollection(), roleCode).getTitle();
	}

	public List<RoleVO> getRoles() {
		List<RoleVO> result = new ArrayList<>();
		for (Role role : roleManager().getAllRoles(view.getCollection())) {
			result.add(new RoleVO(role.getCode(), role.getTitle(), role.getOperationPermissions()));
		}
		return result;
	}

	public void authorizationsButtonClicked() {
		view.navigate().to().listPrincipalAccessAuthorizations(recordId);
	}

	public void rolesButtonClicked() {
		view.navigate().to().editCollectionUserRoles(recordId);
	}

	public boolean isDeletionEnabled() {
		return !getCurrentUser().getId().equals(recordId);
	}

	public void deleteButtonClicked() {
		UserServices userServices = modelLayerFactory.newUserServices();
		User user = coreSchemas().getUser(recordId);
		UserCredential userCredential = userServices.getUserCredential(user.getUsername());
		userServices.removeUserFromCollection(userCredential, view.getCollection());
	}

	private RolesManager roleManager() {
		return modelLayerFactory.getRolesManager();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_SECURITY).globally();
	}

	public boolean isRMModuleEnabled() {
		return this.isSchemaExisting(AdministrativeUnit.DEFAULT_SCHEMA);
	}


	public void copyUserAuthorizations(RecordVO sourceUserVO, List<String> destUsers) {
		List<Authorization> authorizationsList = getUserAuthorizationsList(sourceUserVO);
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


	public void copyUserRoles(RecordVO sourceUserVO, List<String> destUsers) {
		User sourceUser = wrapUser(sourceUserVO.getRecord());
		List<String> rolesList = sourceUser.getUserRoles();

		for (String destUserId : destUsers) {
			User user = coreSchemas().getUser(destUserId);
			user.setUserRoles(rolesList);
			addOrUpdate(user.getWrappedRecord());
		}
	}

	//TODO
	public void removeAllRolesOfUser(RecordVO userVO) {
		User user = wrapUser(userVO.getRecord());
		user.setUserRoles("");
		addOrUpdate(user.getWrappedRecord());
	}


	public void removeAllAuthorizationsOfUser(RecordVO userVO) {
		String userID = userVO.getId();
		List<Authorization> authorizationsList = getUserAuthorizationsList(userVO);
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

	private void updateAuthorizationPrincipalsList(Authorization authorization, List<String> newPrincipalsList) {
		authorization.setPrincipals(newPrincipalsList);
		addOrUpdate(authorization.getWrappedRecord());
	}

	public String buildTransferRightsConfirmMessage(String sourceUserName, String selectedUsersString,
													boolean multipleUsersSelected, boolean removeUserAccess) {
		String confirmMessage;
		if (multipleUsersSelected) {
			if (removeUserAccess) {
				confirmMessage = $("TransferAccessRights.ConfirmMessageMultipleAndRemoveAccess", sourceUserName, selectedUsersString);
			} else {
				confirmMessage = $("TransferAccessRights.ConfirmMessageMultiple", sourceUserName, selectedUsersString);
			}
		} else {
			if (removeUserAccess) {
				confirmMessage = $("TransferAccessRights.ConfirmMessageMultipleAndRemoveAccess", sourceUserName, selectedUsersString);
			} else {
				confirmMessage = $("TransferAccessRights.ConfirmMessageMultiple", sourceUserName, selectedUsersString);
			}
		}
		return confirmMessage;
	}

	public void transferAccessSaveButtonClicked(RecordVO sourceUser, List<String> destUsers, boolean removeUserAccess,
												Window window) {
		errorsList = new ArrayList<>();
		if (validateAccessTransfer(sourceUser, destUsers)) {
			copyUserAuthorizations(sourceUser, destUsers);
			//copyUserRoles(sourceUser, destUsers);		TODO Demander a Rida, si on inclu les rôles
			if (removeUserAccess) {
				removeAllAuthorizationsOfUser(sourceUser);
				//removeAllRolesOfUser(sourceUser);		TODO Demander a Rida, si on inclu les rôles
			}
			window.close();
		} else {
			displayErrorsList();
		}

	}

	public void displayErrorsList() {
		String errorMessages = "";
		for (String msg : errorsList) {
			errorMessages += msg;
			errorMessages += "\n";
		}
		view.showErrorMessage(errorMessages);
	}

	public boolean validateAccessTransfer(RecordVO sourceUser, List<String> destUsers) {
		errorsList = new ArrayList<>();
		if (destUsers.isEmpty()) {
			errorsList.add($("TransferAccessRights.emptyDestinationListError"));
			return false;
		}
		return true;
	}

	public List<String> convertUserIdListToUserNames(List<String> userIdList) {
		ArrayList<String> namesList = new ArrayList<>();
		for (String userId : userIdList) {
			Record userRecord = presenterService().getRecord(userId);
			namesList.add(userRecord.getTitle());
		}
		return namesList;
	}


	private List<Authorization> getUserAuthorizationsList(RecordVO userVO) {
		AuthorizationsServices authorizationsServices = modelLayerFactory.newAuthorizationsServices();
		User userRecord = wrapUser(userVO.getRecord());
		return authorizationsServices.getRecordAuthorizations(userRecord.getWrappedRecord());
	}
}
