package com.constellio.model.extensions.behaviors;

import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.extensions.params.CanAuthenticateUsingPasswordFileIfLDAPFailedParams;

public class UserAuthenticationExtension {

	public ExtensionBooleanResult canAuthenticateUsingPasswordFileIfLDAPFailed(CanAuthenticateUsingPasswordFileIfLDAPFailedParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

}
