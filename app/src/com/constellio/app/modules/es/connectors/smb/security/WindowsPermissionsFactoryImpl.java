package com.constellio.app.modules.es.connectors.smb.security;

import jcifs.smb.SmbFile;

public class WindowsPermissionsFactoryImpl implements WindowsPermissionsFactory {
	private TrusteeManager trusteeManager;

	public WindowsPermissionsFactoryImpl(TrusteeManager trusteeManager) {
		this.trusteeManager = trusteeManager;
	}

	@Override
	public WindowsPermissions newWindowsPermissions(SmbFile file) {
		return new WindowsPermissions(file, trusteeManager);
	}
}
