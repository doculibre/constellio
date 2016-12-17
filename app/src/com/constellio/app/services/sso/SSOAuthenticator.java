package com.constellio.app.services.sso;

import java.io.Serializable;

public interface SSOAuthenticator extends Serializable {

	boolean isEnabled();

	boolean isEnabledForAgent();
	
	String acceptSecurityContext(final byte[] serviceTicket);
	
	String getPrincipalName();

}
