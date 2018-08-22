package com.constellio.app.modules.es.connectors.smb.service;

import com.constellio.app.modules.es.connectors.smb.security.WindowsPermissionsFactory;
import com.constellio.app.modules.es.connectors.smb.service.SmbFileDTO.SmbFileDTOStatus;
import com.constellio.app.modules.es.connectors.smb.testutils.SmbServiceTestUtils;
import com.constellio.app.modules.es.connectors.smb.testutils.SmbTestParams;
import com.constellio.app.modules.es.connectors.smb.utils.ConnectorSmbUtils;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.sdk.tests.ConstellioTest;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class SmbServiceAcceptanceTest extends ConstellioTest {
	private ConnectorSmbUtils smbUtils;
	@Mock ConnectorLogger logger;
	private ESSchemasRecordsServices es;
	private SmbServiceTestUtils smbTestUtils;
	@Mock WindowsPermissionsFactory permissionsFactory;
	@Mock SmbFileFactory smbFactory;

	private SmbShareServiceSimpleImpl smbService;

	private String SHARE_URL = SmbTestParams.EXISTING_SHARE;
	private String FILE_URL = SHARE_URL + SmbTestParams.EXISTING_FILE;
	private String FOLDER_URL = SHARE_URL + SmbTestParams.EXISTING_FOLDER;

	@Before
	public void setup()
			throws IOException {
		MockitoAnnotations.initMocks(this);
		prepareSystem(withZeCollection().withConstellioESModule());
		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		smbTestUtils = new SmbServiceTestUtils();
		smbUtils = new ConnectorSmbUtils();
	}

	@Test
	public void givenGoodConditionsWhenGettingFileDTOThenGetFullDTO()
			throws MalformedURLException {
		when(permissionsFactory.newWindowsPermissions(any(SmbFile.class))).thenReturn(smbTestUtils.getWindowsPermissions());
		when(smbFactory.getSmbFile(anyString(), any(NtlmPasswordAuthentication.class)))
				.thenReturn(smbTestUtils.getValidSmbFile());
		smbService = new SmbShareServiceSimpleImpl(smbTestUtils.getValidCredentials(), smbTestUtils.getFetchValidFile(), smbUtils,
				logger, es, permissionsFactory,
				smbFactory);

		SmbFileDTO smbFileDTO = smbService.getSmbFileDTO(FILE_URL);

		assertThat(smbFileDTO.getStatus()).isEqualTo(SmbFileDTOStatus.FULL_DTO);
	}

	@Test
	public void givenGoodConditionsWhenGettingFolderDTOThenGetFullDTO()
			throws MalformedURLException {
		when(permissionsFactory.newWindowsPermissions(any(SmbFile.class))).thenReturn(smbTestUtils.getWindowsPermissions());
		when(smbFactory.getSmbFile(anyString(), any(NtlmPasswordAuthentication.class)))
				.thenReturn(smbTestUtils.getValidSmbFile());
		smbService = new SmbShareServiceSimpleImpl(smbTestUtils.getValidCredentials(), smbTestUtils.getFetchValidFolder(),
				smbUtils, logger, es, permissionsFactory,
				smbFactory);

		SmbFileDTO smbFileDTO = smbService.getSmbFileDTO(FOLDER_URL);

		assertThat(smbFileDTO.getStatus()).isEqualTo(SmbFileDTOStatus.FULL_DTO);
	}

	@Test
	public void givenInvalidUrlWhenGettingDTOThenGetFailedDTOAndLog()
			throws MalformedURLException {
		when(permissionsFactory.newWindowsPermissions(any(SmbFile.class))).thenReturn(smbTestUtils.getWindowsPermissions());
		when(smbFactory.getSmbFile(anyString(), any(NtlmPasswordAuthentication.class))).thenThrow(MalformedURLException.class);
		smbService = new SmbShareServiceSimpleImpl(smbTestUtils.getValidCredentials(), smbTestUtils.getFetchValidFolder(),
				smbUtils, logger, es, permissionsFactory,
				smbFactory);

		SmbFileDTO smbFileDTO = smbService.getSmbFileDTO("invalid url");

		assertThat(smbFileDTO.getStatus()).isEqualTo(SmbFileDTOStatus.FAILED_DTO);
		verify(logger, atLeastOnce()).error(anyString(), anyString(), anyMap());
	}

	@Test
	public void givenGoodConditionsWhenGettingChildrenThenGetSortedChildrenList()
			throws Exception {
		when(permissionsFactory.newWindowsPermissions(any(SmbFile.class))).thenReturn(smbTestUtils.getWindowsPermissions());
		when(smbFactory.getSmbFile(anyString(), any(NtlmPasswordAuthentication.class)))
				.thenReturn(smbTestUtils.getSmbFileWithChildren());
		smbService = spy(new SmbShareServiceSimpleImpl(smbTestUtils.getValidCredentials(), smbTestUtils.getFetchValidFolder(),
				smbUtils, logger, es, permissionsFactory, smbFactory));

		doReturn(null).when(smbService).getUNC(any(SmbFile.class), any(SmbFile.class));

		List<String> children = smbService.getChildrenUrlsFor(SHARE_URL);
		assertThat(children).containsSequence(FILE_URL, "smb://ip/file0", "smb://ip/file01", "smb://ip/file10", "smb://ip/filea",
				"smb://ip/filez", FOLDER_URL,
				"smb://ip/folder0/", "smb://ip/folder01/", "smb://ip/folder10/", "smb://ip/foldera/", "smb://ip/folderz/");

		verify(smbService, times(12)).getUNC(any(SmbFile.class), any(SmbFile.class));
	}

	@Test
	public void givenInvalidUrlWhenGettingChildrenThenGetEmptyListAndLog()
			throws MalformedURLException {
		when(permissionsFactory.newWindowsPermissions(any(SmbFile.class))).thenReturn(smbTestUtils.getWindowsPermissions());
		when(smbFactory.getSmbFile(anyString(), any(NtlmPasswordAuthentication.class))).thenThrow(MalformedURLException.class);
		smbService = new SmbShareServiceSimpleImpl(smbTestUtils.getValidCredentials(), smbTestUtils.getFetchValidFolder(),
				smbUtils, logger, es, permissionsFactory,
				smbFactory);

		List<String> children = smbService.getChildrenUrlsFor("invalid url");

		assertThat(children).isEmpty();
		verify(logger, atLeastOnce()).error(anyString(), anyString(), anyMap());
	}

	@Test
	public void givenIssueWhenGettingChildrenThenGetEmptyListAndLog()
			throws MalformedURLException, SmbException {
		when(permissionsFactory.newWindowsPermissions(any(SmbFile.class))).thenReturn(smbTestUtils.getWindowsPermissions());
		SmbFile smbFile = Mockito.mock(SmbFile.class);
		when(smbFile.listFiles(any(SmbFileFilter.class))).thenThrow(SmbException.class);
		when(smbFactory.getSmbFile(anyString(), any(NtlmPasswordAuthentication.class))).thenReturn(smbFile);
		smbService = new SmbShareServiceSimpleImpl(smbTestUtils.getValidCredentials(), smbTestUtils.getFetchValidFolder(),
				smbUtils, logger, es, permissionsFactory,
				smbFactory);

		List<String> children = smbService.getChildrenUrlsFor(SHARE_URL);

		assertThat(children).isEmpty();
		verify(logger, atLeastOnce()).error(anyString(), anyString(), anyMap());
	}

	@Test
	public void givenGoodConditionsWhenGettingModificationIndicatorsThenGetIndicators()
			throws MalformedURLException, SmbException {
		SmbFile smbFile = Mockito.mock(SmbFile.class);
		when(smbFile.getLastModified()).thenReturn(123L);
		when(smbFile.length()).thenReturn(456L);
		when(smbFactory.getSmbFile(anyString(), any(NtlmPasswordAuthentication.class))).thenReturn(smbFile);

		when(permissionsFactory.newWindowsPermissions(any(SmbFile.class))).thenReturn(smbTestUtils.getWindowsPermissions());

		smbService = new SmbShareServiceSimpleImpl(smbTestUtils.getValidCredentials(), smbTestUtils.getFetchValidFolder(),
				smbUtils, logger, es, permissionsFactory,
				smbFactory);
		SmbModificationIndicator modificationIndicator = smbService.getModificationIndicator(FILE_URL);

		assertThat(modificationIndicator.getLastModified()).isEqualTo(123L);
		assertThat(modificationIndicator.getSize()).isEqualTo(456L);
		assertThat(modificationIndicator.getPermissionsHash()).isEqualTo(SmbTestParams.EXISTING_FILE_PERMISSION_HASH);

	}

	@Test
	public void givenBadConditionsWhenGettingModificationIndicatorsThenReturnNull() {
		smbService = new SmbShareServiceSimpleImpl(smbTestUtils.getValidCredentials(), smbTestUtils.getFetchValidFolder(),
				smbUtils, logger, es, permissionsFactory,
				smbFactory);

		SmbModificationIndicator modificationIndicator = smbService.getModificationIndicator(FILE_URL);

		assertThat(modificationIndicator).isNull();
	}

	@Test
	public void whenSkipThenGetFullDTO()
			throws MalformedURLException {
		when(permissionsFactory.newWindowsPermissions(any(SmbFile.class))).thenReturn(smbTestUtils.getWindowsPermissions());
		when(smbFactory.getSmbFile(anyString(), any(NtlmPasswordAuthentication.class)))
				.thenReturn(smbTestUtils.getValidSmbFile());
		smbService = new SmbShareServiceSimpleImpl(smbTestUtils.getValidCredentials(), smbTestUtils.getFetchValidFile(), smbUtils,
				logger, es, permissionsFactory,
				smbFactory);

		SmbFileDTO smbFileDTO = smbService.getSmbFileDTO(FILE_URL);

		assertThat(smbFileDTO.getStatus()).isEqualTo(SmbFileDTOStatus.FULL_DTO);
	}
}