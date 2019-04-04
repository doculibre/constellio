package com.constellio.app.modules.es.connectors.smb.security;

import jcifs.smb.SmbFile;

public class WindowsPermissionsFactoryImpl implements WindowsPermissionsFactory {
	private TrusteeManager trusteeManager;
	private boolean skipSharePermissions;
	private boolean skipACL;

	public WindowsPermissionsFactoryImpl(TrusteeManager trusteeManager, boolean skipSharePermissions, boolean skipACL) {
		this.trusteeManager = trusteeManager;
		this.skipSharePermissions = skipSharePermissions;
		this.skipACL = skipACL;
	}

	@Override
	public WindowsPermissions newWindowsPermissions(SmbFile file) {
		return new WindowsPermissions(file, trusteeManager, skipSharePermissions, skipACL);
	}
}
