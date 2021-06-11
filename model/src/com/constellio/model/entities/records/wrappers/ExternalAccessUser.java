package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.structure.ExternalAccessUrlStatus;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.security.roles.Roles;
import org.joda.time.LocalDate;

public class ExternalAccessUser extends User {

	private ExternalAccessUrl externalAccessUrl;

	private String lastIPAddress;

	public ExternalAccessUser(Record record, MetadataSchemaTypes types, Roles roles,
							  ExternalAccessUrl externalAccessUrl, String lastIPAddress) {
		super(record, types, roles);
		this.externalAccessUrl = externalAccessUrl;
		this.lastIPAddress = lastIPAddress;
	}

	public ExternalAccessUrl getExternalAccessUrl() {
		return externalAccessUrl;
	}

	@Override
	public String getUsername() {
		return externalAccessUrl.getFullname() + " (" + getEmail() + ")";
	}

	@Override
	public String getEmail() {
		return externalAccessUrl.getEmail();
	}

	@Override
	public String getLastIPAddress() {
		return this.lastIPAddress;
	}

	@Override
	public UserPermissionsChecker hasReadAccess() {
		return new ExternalAccessPermissionsChecker(this, true, false, false);
	}

	@Override
	public UserPermissionsChecker hasWriteAccess() {
		return new ExternalAccessPermissionsChecker(this, false, true, false);
	}

	private class ExternalAccessPermissionsChecker extends AccessUserPermissionsChecker {

		ExternalAccessPermissionsChecker(User user, boolean readAccess, boolean writeAccess, boolean deleteAccess) {
			super(user, readAccess, writeAccess, deleteAccess);
		}

		@Override
		public boolean on(Record record) {
			return on(record.getId());
		}

		@Override
		public boolean on(RecordWrapper recordWrapper) {
			return on(recordWrapper.getId());
		}

		private boolean on(String recordId) {
			if (!recordId.equals(externalAccessUrl.getAccessRecord())) {
				return false;
			}

			if (externalAccessUrl.getStatus() != ExternalAccessUrlStatus.OPEN &&
				externalAccessUrl.getStatus() != ExternalAccessUrlStatus.TO_CLOSE) {
				return false;
			}

			if (externalAccessUrl.getExpirationDate().isBefore(LocalDate.now())) {
				return false;
			}

			if (readAccess) {
				return externalAccessUrl.getRoles().contains(Role.WRITE)
					   || externalAccessUrl.getRoles().contains(Role.READ);
			}

			if (writeAccess) {
				return externalAccessUrl.getStatus() == ExternalAccessUrlStatus.OPEN
					   && externalAccessUrl.getRoles().contains(Role.WRITE);
			}

			return false;
		}
	}

}
