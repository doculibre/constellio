package com.constellio.model.entities.security;

import java.util.List;

public interface SecurityModel {

	List<Authorization> getAuthorizationsOnTarget(String id);

}
