package com.constellio.model.entities.calculators.dependencies;

import com.constellio.model.entities.security.global.AuthorizationDetails;

import java.util.List;

public class AllAuthorizationsTargettingRecordDependencyValue {

	private List<AuthorizationDetails> authorizationDetailsOnRecord;

	private List<AuthorizationDetails> authorizationDetailsOnMetadatasProvidingSecurity;

	private boolean inheritedAuthorizationsOverridenByMetadatasProvidingSecurity;

	public AllAuthorizationsTargettingRecordDependencyValue(List<AuthorizationDetails> authorizationDetailsOnRecord,
															List<AuthorizationDetails> authorizationDetailsOnMetadatasProvidingSecurity,
															boolean inheritedAuthorizationsOverridenByMetadatasProvidingSecurity) {
		this.authorizationDetailsOnRecord = authorizationDetailsOnRecord;
		this.authorizationDetailsOnMetadatasProvidingSecurity = authorizationDetailsOnMetadatasProvidingSecurity;
		this.inheritedAuthorizationsOverridenByMetadatasProvidingSecurity = inheritedAuthorizationsOverridenByMetadatasProvidingSecurity;
	}

	public List<AuthorizationDetails> getAuthorizationDetailsOnRecord() {
		return authorizationDetailsOnRecord;
	}

	public List<AuthorizationDetails> getAuthorizationDetailsOnMetadatasProvidingSecurity() {
		return authorizationDetailsOnMetadatasProvidingSecurity;
	}

	public boolean isInheritedAuthorizationsOverridenByMetadatasProvidingSecurity() {
		return inheritedAuthorizationsOverridenByMetadatasProvidingSecurity;
	}
}
