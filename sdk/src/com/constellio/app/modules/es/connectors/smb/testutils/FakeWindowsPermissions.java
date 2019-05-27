package com.constellio.app.modules.es.connectors.smb.testutils;

import com.constellio.app.modules.es.connectors.smb.security.TrusteeManager;
import com.constellio.app.modules.es.connectors.smb.security.WindowsPermissions;
import jcifs.smb.SmbFile;

import java.util.List;

public class FakeWindowsPermissions extends WindowsPermissions {
	private List<String> allowTokens;
	private List<String> allowShareTokens;
	private List<String> denyTokens;
	private List<String> denyShareTokens;
	private String permissionsHash;

	public FakeWindowsPermissions(SmbFile file, TrusteeManager trusteeManager, List<String> allowTokens,
								  List<String> allowShareTokens,
								  List<String> denyTokens, List<String> denyShareTokens, String permissionsHash) {
		super(file, trusteeManager, false, false);

		this.allowTokens = allowTokens;
		this.allowShareTokens = allowShareTokens;
		this.denyTokens = denyTokens;
		this.denyShareTokens = denyShareTokens;
		this.permissionsHash = permissionsHash;
	}

	@Override
	public void process() {
		// Do nothing
	}

	@Override
	public List<String> getAllowTokenDocument() {
		return allowTokens;
	}

	public List<String> getAllowShareTokens() {
		return allowShareTokens;
	}

	@Override
	public List<String> getDenyTokenDocument() {
		return denyTokens;
	}

	@Override
	public List<String> getDenyTokenShare() {
		return denyShareTokens;
	}

	@Override
	public String getPermissionsHash() {
		return permissionsHash;
	}
}
