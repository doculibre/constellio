package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.model.entities.security.global.AuthorizationDeleteRequest.authorizationDeleteRequest;

public class ShareContentListPresenter extends ListAuthorizationsPresenter {

	public ShareContentListPresenter(ShareContentListViewImpl view) {
		super(view);
	}

	@Override
	protected List<String> getRestrictedRecordIds(String params) {
		return Arrays.asList(recordId);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_SHARE).globally() || user.has(CorePermissions.MANAGE_GLOBAL_LINKS).globally();
	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		return true;
	}

	@Override
	public void backButtonClicked(String schemaCode) {
		view.navigate().to().adminModule();
	}

	@Override
	public boolean isDetacheable() {
		return false;
	}

	public String getSchemaType() {
		return getRecordVO() == null || getRecordVO().getSchema() == null ? null : getRecordVO().getSchema().getTypeCode();
	}

	@Override
	public boolean isAttached() {
		Record record = recordServices().getDocumentById(recordId);
		return LangUtils.isFalseOrNull(record.get(Schemas.IS_DETACHED_AUTHORIZATIONS));
	}

	@Override
	public List<String> getAllowedAccesses() {
		List<String> results = new ArrayList<>(3);

		User user = getCurrentUser();
		Record record = presenterService().getRecord(recordId);
		if (user.has(CorePermissions.MANAGE_SHARE).globally() || user.has(CorePermissions.MANAGE_GLOBAL_LINKS).globally()) {
			results.addAll(Arrays.asList(Role.READ, Role.WRITE, Role.DELETE));
		} else {
			if (user.hasReadAccess().on(record)) {
				results.add(Role.READ);
			}
			if (user.hasWriteAccess().on(record)) {
				results.add(Role.WRITE);
			}
			if (user.hasDeleteAccess().on(record)) {
				results.add(Role.DELETE);
			}
		}

		return results;
	}

	@Override
	protected boolean isOwnAuthorization(Authorization authorization) {
		return authorization.getTarget().equals(recordId);
	}

	@Override
	protected void removeAuthorization(Authorization authorization) {
		authorizationsServices().execute(authorizationDeleteRequest(authorization).setExecutedBy(getCurrentUser()));
	}

	@Override
	public boolean seeRolesField() {
		return false;
	}

	@Override
	public boolean seeAccessField() {
		return false;
	}

	@Override
	public boolean seeSharedBy() {
		return true;
	}
}