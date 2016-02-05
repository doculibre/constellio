package com.constellio.app.services.sso;

import java.io.Serializable;

public interface KerberosAuthenticator extends Serializable {

	boolean isEnabled();
	
	String acceptSecurityContext(final byte[] serviceTicket);
	
	String getPrincipalName();

}
