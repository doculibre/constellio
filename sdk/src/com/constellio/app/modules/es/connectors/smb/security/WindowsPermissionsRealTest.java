package com.constellio.app.modules.es.connectors.smb.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.es.connectors.smb.config.SmbRetrievalConfiguration;
import com.constellio.app.modules.es.connectors.smb.service.SmbFileDTO;
import com.constellio.app.modules.es.connectors.smb.service.SmbService;
import com.constellio.app.modules.es.connectors.smb.service.SmbServiceSimpleImpl;
import com.constellio.app.modules.es.connectors.smb.utils.ConnectorSmbUtils;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.SlowTest;

public class WindowsPermissionsRealTest extends ConstellioTest {
	private TrusteeManager trusteeManager;

	private String share;
	private String domain;
	private String username;
	private String password;
	private String filename;
	private String folder;
	private SmbService smbService;
	private Credentials credentials;
	private SmbRetrievalConfiguration smbRetrievalConfiguration;
	private ESSchemasRecordsServices es;
	private ConsoleConnectorLogger logger;
	private ConnectorSmbUtils smbUtils;

	@Before
	public void setup() {

		share = SDKPasswords.testSmbServer() + "sharePermissions/";
		domain = SDKPasswords.testSmbDomain();
		username = SDKPasswords.testSmbUsername();
		password = SDKPasswords.testSmbPassword();
		credentials = new Credentials(domain, username, password);

		givenCollection(zeCollection);
		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		logger = new ConsoleConnectorLogger();
		filename = "file.txt";
		smbUtils = new ConnectorSmbUtils();

		// Using SysInternal psGetSid
		// All have at least Read
		// System S-1-5-18
		// Administrator S-1-5-21-3489979699-349065827-2066094650-500
		// Administrators S-1-5-32-544
		// Patrick S-1-5-21-3489979699-349065827-2066094650-1200

	}

	@Test
	@InDevelopmentTest
	@SlowTest
	public void givenAllowAndShareAllowOnlyWhenGettingSecurityTokensThenGetExpectedSecurityTokens() {
		String smbUrl = share + "fileWithAllowsAndNoDenies.txt";
		smbRetrievalConfiguration = new SmbRetrievalConfiguration(Arrays.asList(smbUrl), Arrays.asList(smbUrl), new ArrayList(), false);
		smbService = new SmbServiceSimpleImpl(credentials, smbRetrievalConfiguration, smbUtils, logger, es);

		SmbFileDTO fileDTO = smbService.getSmbFileDTO(smbUrl);

		assertThat(fileDTO.getAllowTokens()).containsOnly("r,ad,S-1-5-18", "r,ad,S-1-5-21-3489979699-349065827-2066094650-500", "r,ad,S-1-5-32-544");
		assertThat(fileDTO.getDenyTokens()).isNull();
		assertThat(fileDTO.getAllowShareTokens()).containsOnly("r,ad,S-1-1-0", "r,ad,S-1-5-32-544");
		assertThat(fileDTO.getDenyShareTokens()).isNull();
	}

	@Test
	@InDevelopmentTest
	@SlowTest
	public void givenDenyWhenGettingSecurityTokensThenGetExpectedSecurityTokens() {
		String smbUrl = share + "fileWithDeny.txt";
		smbRetrievalConfiguration = new SmbRetrievalConfiguration(Arrays.asList(smbUrl), Arrays.asList(smbUrl), new ArrayList(), false);
		smbService = new SmbServiceSimpleImpl(credentials, smbRetrievalConfiguration, smbUtils, logger, es);
		SmbFileDTO fileDTO = smbService.getSmbFileDTO(smbUrl);

		assertThat(fileDTO.getAllowTokens()).containsOnly("r,ad,S-1-5-18", "r,ad,S-1-5-21-3489979699-349065827-2066094650-500", "r,ad,S-1-5-32-544");
		assertThat(fileDTO.getDenyTokens()).containsOnly("r,ad,S-1-5-21-3489979699-349065827-2066094650-1200");
		assertThat(fileDTO.getAllowShareTokens()).containsOnly("r,ad,S-1-1-0", "r,ad,S-1-5-32-544");
		assertThat(fileDTO.getDenyShareTokens()).isNull();
	}

	@Test
	@InDevelopmentTest
	@SlowTest
	public void givenExplicitUserWhenGettingSecurityTokensThenGetExpectedSecurityTokens() {
		String smbUrl = share + "fileWithExplicitUser.txt";
		smbRetrievalConfiguration = new SmbRetrievalConfiguration(Arrays.asList(smbUrl), Arrays.asList(smbUrl), new ArrayList(), false);
		smbService = new SmbServiceSimpleImpl(credentials, smbRetrievalConfiguration, smbUtils, logger, es);
		SmbFileDTO fileDTO = smbService.getSmbFileDTO(smbUrl);

		assertThat(fileDTO.getAllowTokens()).containsOnly("r,ad,S-1-5-18", "r,ad,S-1-5-21-3489979699-349065827-2066094650-500", "r,ad,S-1-5-32-544",
				"r,ad,S-1-5-21-3489979699-349065827-2066094650-1200");
		assertThat(fileDTO.getDenyTokens()).isNull();
		assertThat(fileDTO.getAllowShareTokens()).containsOnly("r,ad,S-1-1-0", "r,ad,S-1-5-32-544");
		assertThat(fileDTO.getDenyShareTokens()).isNull();
	}
}