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

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class CollectionUserPresenter extends SingleSchemaBasePresenter<CollectionUserView> implements TransferPermissionPresenter {
	String recordId;

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


	public void copyUserPermissions(RecordVO sourceUserVO, List<String> destUsers) {
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
		String confirmMessage = multipleUsersSelected ? $("TransferAccessRights.ConfirmMessageMultiple")
													  : $("TransferAccessRights.ConfirmMessageSingle");
		confirmMessage = String.format(confirmMessage, sourceUserName, selectedUsersString);
		if (removeUserAccess) {
			confirmMessage += String.format($("TransferAccessRights.confirmRemoveAccessRights"), sourceUserName);
		}
		confirmMessage += "?";
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


	private List<Authorization> getUserAuthorizationsList(RecordVO userVO) {
		AuthorizationsServices authorizationsServices = modelLayerFactory.newAuthorizationsServices();
		User userRecord = wrapUser(userVO.getRecord());
		return authorizationsServices.getRecordAuthorizations(userRecord.getWrappedRecord());
	}
	/*
	public List<Record> getUserVOListFromIDs(List<String> idsList) {
		ArrayList<Record> usersList = new ArrayList<>();
		for (String userID : idsList) {
			Record record = presenterService().getRecord(userID);
			usersList.add(record);
		}
		return usersList;
	}
	*/

}
