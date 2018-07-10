package com.constellio.model.entities.security;

import java.util.List;

public interface SecurityModel {

	List<SecurityModelAuthorization> getAuthorizationsOnTarget(String id);
}
