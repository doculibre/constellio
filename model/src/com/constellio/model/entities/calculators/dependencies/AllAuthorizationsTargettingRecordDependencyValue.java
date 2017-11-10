package com.constellio.model.entities.calculators.dependencies;

import java.util.List;

import com.constellio.model.entities.security.global.AuthorizationDetails;

public class AllAuthorizationsTargettingRecordDependencyValue {

	private List<AuthorizationDetails> authorizationDetails;

	public AllAuthorizationsTargettingRecordDependencyValue(List<AuthorizationDetails> authorizationDetails) {
		this.authorizationDetails = authorizationDetails;
	}

	public List<AuthorizationDetails> getAuthorizationDetails() {
		return authorizationDetails;
	}
}
