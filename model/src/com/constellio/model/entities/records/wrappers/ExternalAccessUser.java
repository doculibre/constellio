package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.structure.ExternalAccessUrlStatus;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.security.roles.Roles;

public class ExternalAccessUser extends User {

	private ExternalAccessUrl externalAccessUrl;

	public ExternalAccessUser(Record record, MetadataSchemaTypes types, Roles roles,
							  ExternalAccessUrl externalAccessUrl) {
		super(record, types, roles);
		this.externalAccessUrl = externalAccessUrl;
	}

	public ExternalAccessUrl getExternalAccessUrl() {
		return externalAccessUrl;
	}

	@Override
	public String getUsername() {
		return externalAccessUrl.getFullname();
	}

	@Override
	public UserPermissionsChecker hasReadAccess() {
		return new ExternalAccessPermissionsChecker(this, true, false, false);
	}

	@Override
	public UserPermissionsChecker hasWriteAccess() {
		boolean hasWriteAccess = externalAccessUrl.getStatus() == ExternalAccessUrlStatus.OPEN;
		return new ExternalAccessPermissionsChecker(this, false, hasWriteAccess, false);
	}

	private class ExternalAccessPermissionsChecker extends AccessUserPermissionsChecker {

		ExternalAccessPermissionsChecker(User user, boolean readAccess, boolean writeAccess, boolean deleteAccess) {
			super(user, readAccess, writeAccess, deleteAccess);
		}

		@Override
		public boolean on(Record record) {
			return record.getId().equals(externalAccessUrl.getAccessRecord());
		}

		@Override
		public boolean on(RecordWrapper recordWrapper) {
			return recordWrapper.getId().equals(externalAccessUrl.getAccessRecord());
		}

	}

}
