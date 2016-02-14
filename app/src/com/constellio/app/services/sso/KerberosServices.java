package com.constellio.app.services.sso;

import java.io.Serializable;

@SuppressWarnings("serial")
public class KerberosServices implements Serializable {

	private boolean debug = false;

	private static KerberosServices instance;

	private boolean enabled;
	
	private KerberosAuthenticator authenticator;
	
	private KerberosServices() {
	}
	
	public String getPrincipalName() {
		return authenticator != null ? authenticator.getPrincipalName() : null;
	}

	public String acceptSecurityContext(final byte[] serviceTicket) {
		String clientName;
		if (enabled && authenticator != null) {
			clientName = authenticator.acceptSecurityContext(serviceTicket);;
		} else {
			clientName = null;
		}
		return clientName;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isDebug() {
		return debug;
	}

	public static KerberosServices init(KerberosAuthenticator authenticator, boolean debug) {
		instance = new KerberosServices();
		instance.debug = debug;
		instance.enabled = authenticator != null && authenticator.isEnabled();
		return instance;
	}
	
	public static KerberosServices getInstance() {
		if (instance == null) {
			// Disabled instance
			instance = new KerberosServices();
		}
		return instance;
	}
	
}
