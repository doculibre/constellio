package com.constellio.app.modules.restapi.core.service;

import com.constellio.app.modules.restapi.RestApiConfigs;
import com.constellio.app.modules.restapi.core.exception.InvalidAuthenticationException;
import com.constellio.app.modules.restapi.core.exception.RequiredParameterException;
import com.constellio.app.modules.restapi.core.util.AuthorizationUtils;
import com.constellio.app.services.factories.ConstellioFactories;
import com.google.common.base.Strings;

public abstract class BaseRestfulService {

	protected void validateAuthentication(String authentication) {
		if (Strings.isNullOrEmpty(authentication)) {
			throw new InvalidAuthenticationException();
		}

		String scheme = AuthorizationUtils.getScheme(authentication);
		if (scheme == null || !scheme.equals(AuthorizationUtils.SCHEME)) {
			throw new InvalidAuthenticationException();
		}
	}

	protected void validateRequiredParameter(Object parameter, String parameterName) {
		if (parameter == null) {
			throw new RequiredParameterException(parameterName);
		}
	}

	protected boolean areExperimentalServicesEnabled() {
		// temporarily solution until rest api v2
		return new RestApiConfigs(ConstellioFactories.getInstance().getAppLayerFactory())
				.areExperimentalServicesEnabled();
	}

}
