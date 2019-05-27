package com.constellio.app.modules.es.connectors.smb.security;

import com.constellio.sdk.tests.ConstellioTest;
import jcifs.smb.SmbFile;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class WindowsPermissionsTest extends ConstellioTest {
	private TrusteeManager trusteeManager;
	private SmbFile smbFile;

	@Before
	public void setup() {
		trusteeManager = new TrusteeManager();
		smbFile = mock(SmbFile.class);
	}

	@Test
	public void whenProcessPermissionsThenNovellNtfsShare() {
		WindowsPermissions windowsPermissions = spy(new WindowsPermissions(smbFile, trusteeManager, false, false));
		windowsPermissions.process();
		verify(windowsPermissions, times(1)).processNovellPermissions(smbFile, trusteeManager);
		verify(windowsPermissions, times(1)).processNTFSPermissions(smbFile);
		verify(windowsPermissions, times(1)).processSharePermissions(smbFile);
	}

	@Test
	public void givenSkipSharePermissionsWhenProcessPermissionsThenOnlyNovellNtfs() {
		WindowsPermissions windowsPermissions = spy(new WindowsPermissions(smbFile, trusteeManager, true, false));
		windowsPermissions.process();
		verify(windowsPermissions, times(1)).processNovellPermissions(smbFile, trusteeManager);
		verify(windowsPermissions, times(1)).processNTFSPermissions(smbFile);
		verify(windowsPermissions, times(0)).processSharePermissions(smbFile);
	}

	@Test
	public void givenSkipACLWhenProcessPermissionsThenNoCalls() {
		WindowsPermissions windowsPermissions = spy(new WindowsPermissions(smbFile, trusteeManager, false, true));
		windowsPermissions.process();
		verify(windowsPermissions, times(0)).processNovellPermissions(smbFile, trusteeManager);
		verify(windowsPermissions, times(0)).processNTFSPermissions(smbFile);
		verify(windowsPermissions, times(0)).processSharePermissions(smbFile);
	}
}