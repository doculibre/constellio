package com.constellio.app.modules.es.connectors.smb.security;

import jcifs.smb.SmbFile;

public class WindowsPermissionsFactoryImpl implements WindowsPermissionsFactory {
	private TrusteeManager trusteeManager;
	private boolean skipSharePermissions;

	public WindowsPermissionsFactoryImpl(TrusteeManager trusteeManager, boolean skipSharePermissions) {
		this.trusteeManager = trusteeManager;
		this.skipSharePermissions = skipSharePermissions;
	}

	@Override
	public WindowsPermissions newWindowsPermissions(SmbFile file) {
		return new WindowsPermissions(file, trusteeManager, skipSharePermissions);
	}

	@Override
	public boolean isSkipSharePermissions() {
		return skipSharePermissions;
	}
}
