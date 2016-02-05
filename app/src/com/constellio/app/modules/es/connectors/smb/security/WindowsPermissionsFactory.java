package com.constellio.app.modules.es.connectors.smb.security;

import jcifs.smb.SmbFile;

public interface WindowsPermissionsFactory {
	public WindowsPermissions newWindowsPermissions(SmbFile file);
}