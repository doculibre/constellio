package com.constellio.model.entities.security;

import java.util.List;

public class SingletonSecurityModel implements SecurityModel {

	List<Authorization> authorizations;


	@Override
	public List<Authorization> getAuthorizationsOnTarget(String id) {
		return null;
	}
}
