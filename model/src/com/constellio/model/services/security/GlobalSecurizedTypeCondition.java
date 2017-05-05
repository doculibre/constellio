package com.constellio.model.services.security;

import com.constellio.model.entities.records.wrappers.User;

public interface GlobalSecurizedTypeCondition {

	boolean hasGlobalAccess(User user, String access);
}
