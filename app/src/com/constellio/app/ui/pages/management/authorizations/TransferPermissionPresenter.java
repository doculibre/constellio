package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.entities.records.Record;

import java.util.List;

public interface TransferPermissionPresenter {
	void copyUserPermissions(RecordVO sourceUse, List<String> destUsers);

	RecordVO getUser();

	List<Record> getUserVOListFromIDs(List<String> idsList);

	String buildTransferRightsConfirmMessage(String sourceUser, String selectedUsersNames,
											 boolean multipleUsersSelected, boolean removeUserAccess);
}
