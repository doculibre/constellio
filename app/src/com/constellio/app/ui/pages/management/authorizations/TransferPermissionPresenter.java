package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.app.ui.entities.RecordVO;

import java.util.List;

public interface TransferPermissionPresenter {
	void copyUserPermissions(RecordVO sourceUse, List<String> destUsers);

	RecordVO getUser();

	String buildTransferRightsConfirmMessage(String sourceUser, String selectedUsersNames,
											 boolean multipleUsersSelected, boolean removeUserAccess);

	List<String> convertUserIdListToUserNames(List<String> userIdList);

	void removeAllAuthorizationsOfUser(RecordVO user);

}
