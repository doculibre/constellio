package com.constellio.app.services.sso;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SSOServices implements Serializable {

	private boolean debug = false;

	private static SSOServices instance;

	private boolean enabled;
	
	private boolean enabledForAgent;
	
	private SSOAuthenticator authenticator;
	
	private SSOServices() {
	}
	
	public String getPrincipalName() {
		return authenticator != null ? authenticator.getPrincipalName() : null;
	}

	public String acceptSecurityContext(final byte[] serviceTicket) {
		String clientName;
		if (enabled && authenticator != null) {
			clientName = authenticator.acceptSecurityContext(serviceTicket);
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

	public boolean isEnabledForAgent() {
		return enabledForAgent;
	}
	
	public void setEnabledForAgent(boolean enabledForAgent) {
		this.enabledForAgent = enabledForAgent;
	}

	public boolean isDebug() {
		return debug;
	}

	public static SSOServices init(SSOAuthenticator authenticator, boolean debug) {
		instance = new SSOServices();
		instance.authenticator = authenticator;
		instance.debug = debug;
		instance.enabled = authenticator != null && authenticator.isEnabled();
		instance.enabledForAgent = authenticator != null && authenticator.isEnabledForAgent();
		return instance;
	}
	
	public static SSOServices getInstance() {
		if (instance == null) {
			// Disabled instance
			instance = new SSOServices();
		}
		return instance;
	}
	
}
