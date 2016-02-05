package com.constellio.app.services.schemas.bulkImport.authorization;

import java.util.List;

public class ImportedAuthorization {
	private String id;

	private String access;

	private List<String> roles;

	private List<ImportedAuthorizationPrincipal> principals;

	private List<ImportedAuthorizationTarget> targets;

	public String getId() {
		return id;
	}

	public ImportedAuthorization setId(String id) {
		this.id = id;
		return this;
	}

	public String getAccess() {
		return access;
	}

	public ImportedAuthorization setAccess(String access) {
		this.access = access;
		return this;
	}

	public List<String> getRoles() {
		return roles;
	}

	public ImportedAuthorization setRoles(List<String> roles) {
		this.roles = roles;
		return this;
	}

	public List<ImportedAuthorizationPrincipal> getPrincipals() {
		return principals;
	}

	public ImportedAuthorization setPrincipals(List<ImportedAuthorizationPrincipal> principals) {
		this.principals = principals;
		return this;
	}

	public List<ImportedAuthorizationTarget> getTargets() {
		return targets;
	}

	public ImportedAuthorization setTargets(List<ImportedAuthorizationTarget> targets) {
		this.targets = targets;
		return this;
	}

	public static class ImportedAuthorizationPrincipal {
		private final String type;
		private final String principalId;

		public ImportedAuthorizationPrincipal(String type, String principalId) {
			this.type = type;
			this.principalId = principalId;
		}

		public String getType() {
			return type;
		}

		public String getPrincipalId() {
			return principalId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof ImportedAuthorizationPrincipal))
				return false;

			ImportedAuthorizationPrincipal that = (ImportedAuthorizationPrincipal) o;

			if (principalId != null ? !principalId.equals(that.principalId) : that.principalId != null)
				return false;
			if (type != null ? !type.equals(that.type) : that.type != null)
				return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = type != null ? type.hashCode() : 0;
			result = 31 * result + (principalId != null ? principalId.hashCode() : 0);
			return result;
		}
	}

	public static class ImportedAuthorizationTarget {
		private final String type;
		private final String legacyId;

		public ImportedAuthorizationTarget(String type, String legacyId) {
			this.type = type;
			this.legacyId = legacyId;
		}

		public String getType() {
			return type;
		}

		public String getLegacyId() {
			return legacyId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof ImportedAuthorizationTarget))
				return false;

			ImportedAuthorizationTarget that = (ImportedAuthorizationTarget) o;

			if (legacyId != null ? !legacyId.equals(that.legacyId) : that.legacyId != null)
				return false;
			if (type != null ? !type.equals(that.type) : that.type != null)
				return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = type != null ? type.hashCode() : 0;
			result = 31 * result + (legacyId != null ? legacyId.hashCode() : 0);
			return result;
		}
	}

}
