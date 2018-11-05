package com.constellio.model.entities.calculators.dependencies;


import com.constellio.model.entities.records.wrappers.Authorization;

import java.util.List;

public class AllAuthorizationsTargettingRecordDependencyValue {

	private List<Authorization> authorizationDetailsOnRecord;

	private List<Authorization> authorizationDetailsOnMetadatasProvidingSecurity;

	private boolean inheritedAuthorizationsOverridenByMetadatasProvidingSecurity;

	private AllAuthorizationsTargettingRecordDependencyValue(
			List<Authorization> authorizationDetailsOnRecord,
			List<Authorization> authorizationDetailsOnMetadatasProvidingSecurity,
			boolean inheritedAuthorizationsOverridenByMetadatasProvidingSecurity) {
		this.authorizationDetailsOnRecord = authorizationDetailsOnRecord;
		this.authorizationDetailsOnMetadatasProvidingSecurity = authorizationDetailsOnMetadatasProvidingSecurity;
		this.inheritedAuthorizationsOverridenByMetadatasProvidingSecurity = inheritedAuthorizationsOverridenByMetadatasProvidingSecurity;
	}

	public List<Authorization> getAuthorizationDetailsOnRecord() {
		return authorizationDetailsOnRecord;
	}

	public List<Authorization> getAuthorizationDetailsOnMetadatasProvidingSecurity() {
		return authorizationDetailsOnMetadatasProvidingSecurity;
	}

	public boolean isInheritedAuthorizationsOverridenByMetadatasProvidingSecurity() {
		return inheritedAuthorizationsOverridenByMetadatasProvidingSecurity;
	}
}
