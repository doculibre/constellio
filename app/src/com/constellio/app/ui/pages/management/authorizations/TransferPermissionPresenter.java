package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.vaadin.ui.Window;

import java.util.List;

public interface TransferPermissionPresenter {
	void copyUserAuthorizations(Record sourceUse, List<String> destUsers) throws Exception;

	RecordVO getUser();

	String buildTransferRightsConfirmMessage(String sourceUser, String selectedUsersNames,
											 boolean multipleUsersSelected, boolean removeUserAccess);

	List<String> convertUserIdListToUserNames(List<String> userIdList);

	void removeAllAuthorizationsOfUser(RecordVO user);

	void transferAccessSaveButtonClicked(RecordVO sourceUser, List<String> destUsers, Window window);

	void validateAccessTransfer(Record sourceUser, List<String> destUsers) throws Exception;

	void displayErrorMessage();

	List<Authorization> getUserAuthorizationsList(Record userVO);

	void setRemoveUserAccess(boolean value);
	//void copyUserRoles(RecordVO sourceUserVO, List<String> destUsers);

	//void removeAllRolesOfUser(RecordVO userVO);
}
