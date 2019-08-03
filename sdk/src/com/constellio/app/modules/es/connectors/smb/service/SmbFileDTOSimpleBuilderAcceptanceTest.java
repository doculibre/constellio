package com.constellio.app.modules.es.connectors.smb.service;

import com.constellio.app.modules.es.connectors.smb.security.TrusteeManager;
import com.constellio.app.modules.es.connectors.smb.security.WindowsPermissions;
import com.constellio.app.modules.es.connectors.smb.security.WindowsPermissionsFactory;
import com.constellio.app.modules.es.connectors.smb.security.WindowsPermissionsFactoryImpl;
import com.constellio.app.modules.es.connectors.smb.service.SmbFileDTO.SmbFileDTOStatus;
import com.constellio.app.modules.es.connectors.smb.testutils.SmbTestParams;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.sdk.tests.ConstellioTest;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class SmbFileDTOSimpleBuilderAcceptanceTest extends ConstellioTest {
	private ESSchemasRecordsServices es;
	@Mock TrusteeManager trusteeManager;
	@Mock ConnectorLogger logger;
	@Mock SmbFile smbFile;
	@Mock WindowsPermissions windowsPermissions;
	@Mock WindowsPermissionsFactory permissionsFactory;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		prepareSystem(withZeCollection().withConstellioESModule()
				.withAllTestUsers());

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
	}

	@Test
	public void givenGoodConditionsWhenBuildingDTOForFileThenGetFullDTO()
			throws IOException {
		// Given
		LocalDateTime time1 = new LocalDateTime();
		givenTimeIs(time1);

		when(smbFile.getCanonicalPath()).thenReturn(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE);
		SmbFileDTOSimpleBuilder builder = Mockito.spy(new SmbFileDTOSimpleBuilder(logger, es, permissionsFactory));

		when(windowsPermissions.getAllowTokenDocument()).thenReturn(SmbTestParams.ALLOW_TOKENS);
		when(windowsPermissions.getAllowTokenShare()).thenReturn(SmbTestParams.ALLOW_SHARE_TOKENS);
		when(windowsPermissions.getDenyTokenDocument()).thenReturn(SmbTestParams.DENY_TOKENS);
		when(windowsPermissions.getDenyTokenShare()).thenReturn(SmbTestParams.DENY_SHARE_TOKENS);
		when(windowsPermissions.getPermissionsHash()).thenReturn(SmbTestParams.EXISTING_FILE_PERMISSION_HASH);
		doReturn(windowsPermissions).when(builder)
				.getWindowsPermissions(any(SmbFile.class));

		when(smbFile.exists()).thenReturn(true);
		when(smbFile.isFile()).thenReturn(true);
		when(smbFile.isDirectory()).thenReturn(false);
		when(smbFile.getName()).thenReturn(SmbTestParams.EXISTING_FILE);
		when(smbFile.length()).thenReturn(SmbTestParams.EXISTING_FILE_LENGTH);
		when(smbFile.getInputStream()).thenReturn(new ByteArrayInputStream(SmbTestParams.EXISTING_FILE_CONTENT.getBytes()));
		when(smbFile.getLastModified()).thenReturn(123456L);

		// When
		SmbFileDTO smbFileDTO = builder.build(smbFile, true);

		// Then
		assertThat(smbFileDTO.getStatus()).isEqualTo(SmbFileDTOStatus.FULL_DTO);
		assertThat(smbFileDTO.getLastFetchAttempt()).isEqualTo(time1);
		assertThat(smbFileDTO.getUrl()).isEqualTo(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE);
		assertThat(smbFileDTO.getAllowTokens()).isEqualTo(Arrays.asList("r,ad,allowToken1", "r,ad,allowToken2"));
		assertThat(smbFileDTO.getAllowShareTokens()).isEqualTo(Arrays.asList("r,ad,allowShareToken1", "r,ad,allowShareToken2"));
		assertThat(smbFileDTO.getDenyTokens()).isEqualTo(Arrays.asList("r,ad,denyToken1", "r,ad,denyToken2"));
		assertThat(smbFileDTO.getDenyShareTokens()).isEqualTo(Arrays.asList("r,ad,denyShareToken1", "r,ad,denyShareToken2"));
		assertThat(smbFileDTO.getPermissionsHash()).isEqualTo(SmbTestParams.EXISTING_FILE_PERMISSION_HASH);
		assertThat(smbFileDTO.isFile()).isTrue();
		assertThat(smbFileDTO.isDirectory()).isFalse();
		assertThat(smbFileDTO.getExtension()).isEqualTo(SmbTestParams.EXISTING_FILE_EXT);
		assertThat(smbFileDTO.getName()).isEqualTo(SmbTestParams.EXISTING_FILE);
		assertThat(smbFileDTO.getLength()).isEqualTo(SmbTestParams.EXISTING_FILE_LENGTH);
		assertThat(smbFileDTO.getParsedContent()).isEqualTo(SmbTestParams.EXISTING_FILE_CONTENT);
		assertThat(smbFileDTO.getLanguage()).isEqualTo(SmbTestParams.EXISTING_FILE_LANG);
		assertThat(smbFileDTO.getErrorMessage()).isEmpty();
		assertThat(smbFileDTO.getLastModified()).isEqualTo(123456L);
	}

	@Test
	public void givenIssueWhenBuildingDTOForFileThenGetFailedDTOAndLog()
			throws IOException {
		// Given
		LocalDateTime time1 = new LocalDateTime();
		givenTimeIs(time1);

		when(smbFile.getCanonicalPath()).thenReturn(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE);
		SmbFileDTOSimpleBuilder builder = Mockito.spy(new SmbFileDTOSimpleBuilder(logger, es, permissionsFactory));

		when(windowsPermissions.getAllowTokenDocument()).thenReturn(SmbTestParams.ALLOW_TOKENS);
		when(windowsPermissions.getAllowTokenShare()).thenReturn(SmbTestParams.ALLOW_SHARE_TOKENS);
		when(windowsPermissions.getDenyTokenDocument()).thenReturn(SmbTestParams.DENY_TOKENS);
		when(windowsPermissions.getDenyTokenShare()).thenReturn(SmbTestParams.DENY_SHARE_TOKENS);
		when(windowsPermissions.getPermissionsHash()).thenReturn(SmbTestParams.EXISTING_FILE_PERMISSION_HASH);
		doReturn(windowsPermissions).when(builder)
				.getWindowsPermissions(any(SmbFile.class));

		when(smbFile.exists()).thenReturn(true);
		when(smbFile.isFile()).thenThrow(new SmbException(123, false));
		when(smbFile.isDirectory()).thenReturn(false);
		when(smbFile.getName()).thenReturn(SmbTestParams.EXISTING_FILE);
		when(smbFile.length()).thenReturn(SmbTestParams.EXISTING_FILE_LENGTH);
		when(smbFile.getInputStream()).thenReturn(new ByteArrayInputStream(SmbTestParams.EXISTING_FILE_CONTENT.getBytes()));
		when(smbFile.getLastModified()).thenReturn(123456L);

		// When
		SmbFileDTO smbFileDTO = builder.build(smbFile, true);

		// Then
		assertThat(smbFileDTO.getStatus()).isEqualTo(SmbFileDTOStatus.FAILED_DTO);
		assertThat(smbFileDTO.getLastFetchAttempt()).isEqualTo(time1);
		assertThat(smbFileDTO.getUrl()).isEqualTo(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE);
		assertThat(smbFileDTO.getAllowTokens()).isEqualTo(Arrays.asList("r,ad,allowToken1", "r,ad,allowToken2"));
		assertThat(smbFileDTO.getAllowShareTokens()).isEqualTo(Arrays.asList("r,ad,allowShareToken1", "r,ad,allowShareToken2"));
		assertThat(smbFileDTO.getDenyTokens()).isEqualTo(Arrays.asList("r,ad,denyToken1", "r,ad,denyToken2"));
		assertThat(smbFileDTO.getDenyShareTokens()).isEqualTo(Arrays.asList("r,ad,denyShareToken1", "r,ad,denyShareToken2"));
		assertThat(smbFileDTO.getPermissionsHash()).isEqualTo(SmbTestParams.EXISTING_FILE_PERMISSION_HASH);
		assertThat(smbFileDTO.isFile()).isFalse();
		assertThat(smbFileDTO.isDirectory()).isFalse();
		assertThat(smbFileDTO.getExtension()).isEmpty();
		assertThat(smbFileDTO.getName()).isEmpty();
		assertThat(smbFileDTO.getLength()).isEqualTo(-10);
		assertThat(smbFileDTO.getParsedContent()).isEmpty();
		assertThat(smbFileDTO.getLanguage()).isEmpty();
		assertThat(smbFileDTO.getErrorMessage()).isNotEmpty();
		assertThat(smbFileDTO.getLastModified()).isEqualTo(123456L);
	}

	@Test
	public void givenGoodConditionsWhenBuildingDTOForFolderThenGetFullDTO()
			throws IOException {
		// Given
		LocalDateTime time1 = new LocalDateTime();
		givenTimeIs(time1);

		when(smbFile.getCanonicalPath()).thenReturn(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FOLDER);
		SmbFileDTOSimpleBuilder builder = Mockito.spy(new SmbFileDTOSimpleBuilder(logger, es, permissionsFactory));

		when(windowsPermissions.getAllowTokenDocument()).thenReturn(SmbTestParams.ALLOW_TOKENS);
		when(windowsPermissions.getAllowTokenShare()).thenReturn(SmbTestParams.ALLOW_SHARE_TOKENS);
		when(windowsPermissions.getDenyTokenDocument()).thenReturn(SmbTestParams.DENY_TOKENS);
		when(windowsPermissions.getDenyTokenShare()).thenReturn(SmbTestParams.DENY_SHARE_TOKENS);
		when(windowsPermissions.getPermissionsHash()).thenReturn(SmbTestParams.EXISTING_FILE_PERMISSION_HASH);
		doReturn(windowsPermissions).when(builder)
				.getWindowsPermissions(any(SmbFile.class));

		when(smbFile.exists()).thenReturn(true);
		when(smbFile.isFile()).thenReturn(false);
		when(smbFile.isDirectory()).thenReturn(true);
		when(smbFile.getName()).thenReturn(SmbTestParams.EXISTING_FOLDER);
		when(smbFile.length()).thenReturn(SmbTestParams.EXISTING_FILE_LENGTH);
		when(smbFile.getLastModified()).thenReturn(123456L);

		// When
		SmbFileDTO smbFileDTO = builder.build(smbFile, true);

		// Then
		assertThat(smbFileDTO.getStatus()).isEqualTo(SmbFileDTOStatus.FULL_DTO);
		assertThat(smbFileDTO.getLastFetchAttempt()).isEqualTo(time1);
		assertThat(smbFileDTO.getUrl()).isEqualTo(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FOLDER);
		assertThat(smbFileDTO.getAllowTokens()).isEqualTo(Arrays.asList("r,ad,allowToken1", "r,ad,allowToken2"));
		assertThat(smbFileDTO.getAllowShareTokens()).isEqualTo(Arrays.asList("r,ad,allowShareToken1", "r,ad,allowShareToken2"));
		assertThat(smbFileDTO.getDenyTokens()).isEqualTo(Arrays.asList("r,ad,denyToken1", "r,ad,denyToken2"));
		assertThat(smbFileDTO.getDenyShareTokens()).isEqualTo(Arrays.asList("r,ad,denyShareToken1", "r,ad,denyShareToken2"));
		assertThat(smbFileDTO.getPermissionsHash()).isEqualTo(SmbTestParams.EXISTING_FILE_PERMISSION_HASH);
		assertThat(smbFileDTO.isFile()).isFalse();
		assertThat(smbFileDTO.isDirectory()).isTrue();
		assertThat(smbFileDTO.getExtension()).isEmpty();
		assertThat(smbFileDTO.getName()).isEqualTo(StringUtils.removeEnd(SmbTestParams.EXISTING_FOLDER, "/"));
		assertThat(smbFileDTO.getLength()).isZero();
		assertThat(smbFileDTO.getParsedContent()).isEmpty();
		assertThat(smbFileDTO.getLanguage()).isEmpty();
		assertThat(smbFileDTO.getErrorMessage()).isEmpty();
		assertThat(smbFileDTO.getLastModified()).isEqualTo(123456L);
	}

	@Test
	public void givenIssueWhenBuildingDTOForFolderThenGetFailedDTOAndLog()
			throws IOException {
		// Given
		LocalDateTime time1 = new LocalDateTime();
		givenTimeIs(time1);

		when(smbFile.getCanonicalPath()).thenReturn(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FOLDER);
		SmbFileDTOSimpleBuilder builder = Mockito.spy(new SmbFileDTOSimpleBuilder(logger, es, permissionsFactory));

		when(windowsPermissions.getAllowTokenDocument()).thenReturn(SmbTestParams.ALLOW_TOKENS);
		when(windowsPermissions.getAllowTokenShare()).thenReturn(SmbTestParams.ALLOW_SHARE_TOKENS);
		when(windowsPermissions.getDenyTokenDocument()).thenReturn(SmbTestParams.DENY_TOKENS);
		when(windowsPermissions.getDenyTokenShare()).thenReturn(SmbTestParams.DENY_SHARE_TOKENS);
		when(windowsPermissions.getPermissionsHash()).thenReturn(SmbTestParams.EXISTING_FILE_PERMISSION_HASH);
		doReturn(windowsPermissions).when(builder)
				.getWindowsPermissions(any(SmbFile.class));

		when(smbFile.exists()).thenReturn(true);
		when(smbFile.isFile()).thenReturn(false);
		when(smbFile.isDirectory()).thenThrow(new SmbException(123, false));
		when(smbFile.getName()).thenReturn(SmbTestParams.EXISTING_FOLDER);
		when(smbFile.length()).thenReturn(SmbTestParams.EXISTING_FILE_LENGTH);
		when(smbFile.getLastModified()).thenReturn(123456L);

		// When
		SmbFileDTO smbFileDTO = builder.build(smbFile, true);

		// Then
		assertThat(smbFileDTO.getStatus()).isEqualTo(SmbFileDTOStatus.FAILED_DTO);
		assertThat(smbFileDTO.getLastFetchAttempt()).isEqualTo(time1);
		assertThat(smbFileDTO.getUrl()).isEqualTo(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FOLDER);
		assertThat(smbFileDTO.getAllowTokens()).isEqualTo(Arrays.asList("r,ad,allowToken1", "r,ad,allowToken2"));
		assertThat(smbFileDTO.getAllowShareTokens()).isEqualTo(Arrays.asList("r,ad,allowShareToken1", "r,ad,allowShareToken2"));
		assertThat(smbFileDTO.getDenyTokens()).isEqualTo(Arrays.asList("r,ad,denyToken1", "r,ad,denyToken2"));
		assertThat(smbFileDTO.getDenyShareTokens()).isEqualTo(Arrays.asList("r,ad,denyShareToken1", "r,ad,denyShareToken2"));
		assertThat(smbFileDTO.getPermissionsHash()).isEqualTo(SmbTestParams.EXISTING_FILE_PERMISSION_HASH);
		assertThat(smbFileDTO.isFile()).isFalse();
		assertThat(smbFileDTO.isDirectory()).isFalse();
		assertThat(smbFileDTO.getExtension()).isEmpty();
		assertThat(smbFileDTO.getName()).isEmpty();
		assertThat(smbFileDTO.getLength()).isEqualTo(-10);
		assertThat(smbFileDTO.getParsedContent()).isEmpty();
		assertThat(smbFileDTO.getLanguage()).isEmpty();
		assertThat(smbFileDTO.getErrorMessage()).isNotEmpty();
		assertThat(smbFileDTO.getLastModified()).isEqualTo(123456L);
	}

	@Test
	public void givenFileTooBigWhenBuildingDTOForDocumentThenGetFailedDTO()
			throws IOException {
		// Given
		LocalDateTime time1 = new LocalDateTime();
		givenTimeIs(time1);

		when(smbFile.getCanonicalPath()).thenReturn(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE);
		SmbFileDTOSimpleBuilder builder = Mockito.spy(new SmbFileDTOSimpleBuilder(logger, es, permissionsFactory));

		when(windowsPermissions.getAllowTokenDocument()).thenReturn(SmbTestParams.ALLOW_TOKENS);
		when(windowsPermissions.getAllowTokenShare()).thenReturn(SmbTestParams.ALLOW_SHARE_TOKENS);
		when(windowsPermissions.getDenyTokenDocument()).thenReturn(SmbTestParams.DENY_TOKENS);
		when(windowsPermissions.getDenyTokenShare()).thenReturn(SmbTestParams.DENY_SHARE_TOKENS);
		when(windowsPermissions.getPermissionsHash()).thenReturn(SmbTestParams.EXISTING_FILE_PERMISSION_HASH);
		doReturn(windowsPermissions).when(builder)
				.getWindowsPermissions(any(SmbFile.class));

		when(smbFile.exists()).thenReturn(true);
		when(smbFile.isFile()).thenReturn(true);
		when(smbFile.isDirectory()).thenReturn(false);
		when(smbFile.getName()).thenReturn(SmbTestParams.EXISTING_FILE);
		when(smbFile.length()).thenReturn(20_000_000_000L);
		when(smbFile.getInputStream()).thenReturn(new ByteArrayInputStream(SmbTestParams.EXISTING_FILE_CONTENT.getBytes()));
		when(smbFile.getLastModified()).thenReturn(123456L);

		// When
		SmbFileDTO smbFileDTO = builder.build(smbFile, true);

		// Then
		assertThat(smbFileDTO.getStatus()).isEqualTo(SmbFileDTOStatus.FAILED_DTO);
		assertThat(smbFileDTO.getLastFetchAttempt()).isEqualTo(time1);
		assertThat(smbFileDTO.getUrl()).isEqualTo(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE);
		assertThat(smbFileDTO.getAllowTokens()).isEqualTo(Arrays.asList("r,ad,allowToken1", "r,ad,allowToken2"));
		assertThat(smbFileDTO.getAllowShareTokens()).isEqualTo(Arrays.asList("r,ad,allowShareToken1", "r,ad,allowShareToken2"));
		assertThat(smbFileDTO.getDenyTokens()).isEqualTo(Arrays.asList("r,ad,denyToken1", "r,ad,denyToken2"));
		assertThat(smbFileDTO.getDenyShareTokens()).isEqualTo(Arrays.asList("r,ad,denyShareToken1", "r,ad,denyShareToken2"));
		assertThat(smbFileDTO.getPermissionsHash()).isEqualTo(SmbTestParams.EXISTING_FILE_PERMISSION_HASH);
		assertThat(smbFileDTO.isFile()).isTrue();
		assertThat(smbFileDTO.isDirectory()).isFalse();
		assertThat(smbFileDTO.getExtension()).isEqualTo(SmbTestParams.EXISTING_FILE_EXT);
		assertThat(smbFileDTO.getName()).isEqualTo(SmbTestParams.EXISTING_FILE);
		assertThat(smbFileDTO.getLength()).isEqualTo(20_000_000_000L);
		assertThat(smbFileDTO.getParsedContent()).isEmpty();
		assertThat(smbFileDTO.getLanguage()).isEmpty();
		assertThat(smbFileDTO.getErrorMessage()).contains("exceeds maximum size");
		assertThat(smbFileDTO.getLastModified()).isEqualTo(123456L);
	}

	@Test
	public void givenFileSizeZeroWhenBuildingDTOForDocumentThenGetFullDTO()
			throws IOException {
		// Given
		LocalDateTime time1 = new LocalDateTime();
		givenTimeIs(time1);

		when(smbFile.getCanonicalPath()).thenReturn(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE);
		SmbFileDTOSimpleBuilder builder = Mockito.spy(new SmbFileDTOSimpleBuilder(logger, es, permissionsFactory));

		when(windowsPermissions.getAllowTokenDocument()).thenReturn(SmbTestParams.ALLOW_TOKENS);
		when(windowsPermissions.getAllowTokenShare()).thenReturn(SmbTestParams.ALLOW_SHARE_TOKENS);
		when(windowsPermissions.getDenyTokenDocument()).thenReturn(SmbTestParams.DENY_TOKENS);
		when(windowsPermissions.getDenyTokenShare()).thenReturn(SmbTestParams.DENY_SHARE_TOKENS);
		when(windowsPermissions.getPermissionsHash()).thenReturn(SmbTestParams.EXISTING_FILE_PERMISSION_HASH);
		doReturn(windowsPermissions).when(builder)
				.getWindowsPermissions(any(SmbFile.class));

		when(smbFile.exists()).thenReturn(true);
		when(smbFile.isFile()).thenReturn(true);
		when(smbFile.isDirectory()).thenReturn(false);
		when(smbFile.getName()).thenReturn(SmbTestParams.EXISTING_FILE);
		when(smbFile.length()).thenReturn(0L);
		when(smbFile.getInputStream()).thenReturn(new ByteArrayInputStream(SmbTestParams.EXISTING_FILE_CONTENT.getBytes()));
		when(smbFile.getLastModified()).thenReturn(123456L);

		// When
		SmbFileDTO smbFileDTO = builder.build(smbFile, true);

		// Then
		assertThat(smbFileDTO.getStatus()).isEqualTo(SmbFileDTOStatus.FULL_DTO);
		assertThat(smbFileDTO.getLastFetchAttempt()).isEqualTo(time1);
		assertThat(smbFileDTO.getUrl()).isEqualTo(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE);
		assertThat(smbFileDTO.getAllowTokens()).isEqualTo(Arrays.asList("r,ad,allowToken1", "r,ad,allowToken2"));
		assertThat(smbFileDTO.getAllowShareTokens()).isEqualTo(Arrays.asList("r,ad,allowShareToken1", "r,ad,allowShareToken2"));
		assertThat(smbFileDTO.getDenyTokens()).isEqualTo(Arrays.asList("r,ad,denyToken1", "r,ad,denyToken2"));
		assertThat(smbFileDTO.getDenyShareTokens()).isEqualTo(Arrays.asList("r,ad,denyShareToken1", "r,ad,denyShareToken2"));
		assertThat(smbFileDTO.getPermissionsHash()).isEqualTo(SmbTestParams.EXISTING_FILE_PERMISSION_HASH);
		assertThat(smbFileDTO.isFile()).isTrue();
		assertThat(smbFileDTO.isDirectory()).isFalse();
		assertThat(smbFileDTO.getExtension()).isEqualTo(SmbTestParams.EXISTING_FILE_EXT);
		assertThat(smbFileDTO.getName()).isEqualTo(SmbTestParams.EXISTING_FILE);
		assertThat(smbFileDTO.getLength()).isEqualTo(0L);
		assertThat(smbFileDTO.getParsedContent()).isEmpty();
		assertThat(smbFileDTO.getLanguage()).isEmpty();
		assertThat(smbFileDTO.getErrorMessage()).isEmpty();
		assertThat(smbFileDTO.getLastModified()).isEqualTo(123456L);
	}

	@Test
	public void givenNonExistingFileWhenBuildingDTOForFileThenGetComparativelyFastFailedDTO()
			throws IOException {
		// Given
		LocalDateTime time1 = new LocalDateTime();
		givenTimeIs(time1);

		when(smbFile.getCanonicalPath()).thenReturn(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE);
		SmbFileDTOSimpleBuilder builder = Mockito.spy(new SmbFileDTOSimpleBuilder(logger, es, permissionsFactory));

		when(windowsPermissions.getAllowTokenDocument()).thenReturn(SmbTestParams.ALLOW_TOKENS);
		when(windowsPermissions.getAllowTokenShare()).thenReturn(SmbTestParams.ALLOW_SHARE_TOKENS);
		when(windowsPermissions.getDenyTokenDocument()).thenReturn(SmbTestParams.DENY_TOKENS);
		when(windowsPermissions.getDenyTokenShare()).thenReturn(SmbTestParams.DENY_SHARE_TOKENS);
		when(windowsPermissions.getPermissionsHash()).thenReturn(SmbTestParams.EXISTING_FILE_PERMISSION_HASH);
		doReturn(windowsPermissions).when(builder)
				.getWindowsPermissions(any(SmbFile.class));

		when(smbFile.exists()).thenThrow(new SmbException(123, false));
		when(smbFile.isFile()).thenReturn(true);
		when(smbFile.isDirectory()).thenReturn(false);
		when(smbFile.getName()).thenReturn(SmbTestParams.EXISTING_FILE);
		when(smbFile.length()).thenReturn(SmbTestParams.EXISTING_FILE_LENGTH);
		when(smbFile.getInputStream()).thenReturn(new ByteArrayInputStream(SmbTestParams.EXISTING_FILE_CONTENT.getBytes()));
		when(smbFile.getLastModified()).thenReturn(123456L);

		// When
		SmbFileDTO smbFileDTO = builder.build(smbFile, true);

		// Then
		assertThat(smbFileDTO.getStatus()).isEqualTo(SmbFileDTOStatus.FAILED_DTO);
		assertThat(smbFileDTO.getLastFetchAttempt()).isEqualTo(time1);
		assertThat(smbFileDTO.getUrl()).isEqualTo(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE);
		assertThat(smbFileDTO.getAllowTokens()).isEmpty();
		assertThat(smbFileDTO.getAllowShareTokens()).isEmpty();
		assertThat(smbFileDTO.getDenyTokens()).isEmpty();
		assertThat(smbFileDTO.getDenyShareTokens()).isEmpty();
		assertThat(smbFileDTO.getPermissionsHash()).isNull();
		assertThat(smbFileDTO.isFile()).isFalse();
		assertThat(smbFileDTO.isDirectory()).isFalse();
		assertThat(smbFileDTO.getExtension()).isEmpty();
		assertThat(smbFileDTO.getName()).isEmpty();
		assertThat(smbFileDTO.getLength()).isEqualTo(-10L);
		assertThat(smbFileDTO.getParsedContent()).isEmpty();
		assertThat(smbFileDTO.getLanguage()).isEmpty();
		assertThat(smbFileDTO.getErrorMessage()).isNotEmpty();
		assertThat(smbFileDTO.getLastModified()).isEqualTo(-10L);
	}

	@Test
	public void givenPermissionsIssuesWhenBuildingDTOForFileThenGetFailedDTOWithErrorMessage()
			throws IOException {
		// Given
		LocalDateTime time1 = new LocalDateTime();
		givenTimeIs(time1);

		when(smbFile.getCanonicalPath()).thenReturn(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE);
		SmbFileDTOSimpleBuilder builder = Mockito.spy(new SmbFileDTOSimpleBuilder(logger, es, permissionsFactory));

		when(windowsPermissions.getErrors()).thenReturn("Permissions issue");
		when(windowsPermissions.getAllowTokenDocument()).thenReturn(SmbTestParams.ALLOW_TOKENS);
		when(windowsPermissions.getAllowTokenShare()).thenReturn(SmbTestParams.ALLOW_SHARE_TOKENS);
		when(windowsPermissions.getDenyTokenDocument()).thenReturn(SmbTestParams.DENY_TOKENS);
		when(windowsPermissions.getDenyTokenShare()).thenReturn(SmbTestParams.DENY_SHARE_TOKENS);
		when(windowsPermissions.getPermissionsHash()).thenReturn(SmbTestParams.EXISTING_FILE_PERMISSION_HASH);
		doReturn(windowsPermissions).when(builder)
				.getWindowsPermissions(any(SmbFile.class));

		when(smbFile.exists()).thenReturn(true);
		when(smbFile.isFile()).thenReturn(true);
		when(smbFile.isDirectory()).thenReturn(false);
		when(smbFile.getName()).thenReturn(SmbTestParams.EXISTING_FILE);
		when(smbFile.length()).thenReturn(SmbTestParams.EXISTING_FILE_LENGTH);
		when(smbFile.getInputStream()).thenReturn(new ByteArrayInputStream(SmbTestParams.EXISTING_FILE_CONTENT.getBytes()));
		when(smbFile.getLastModified()).thenReturn(123456L);

		// When
		SmbFileDTO smbFileDTO = builder.build(smbFile, true);

		// Then
		assertThat(smbFileDTO.getStatus()).isEqualTo(SmbFileDTOStatus.FAILED_DTO);
		assertThat(smbFileDTO.getLastFetchAttempt()).isEqualTo(time1);
		assertThat(smbFileDTO.getUrl()).isEqualTo(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE);
		assertThat(smbFileDTO.getAllowTokens()).isEmpty();
		assertThat(smbFileDTO.getAllowShareTokens()).isEmpty();
		assertThat(smbFileDTO.getDenyTokens()).isEmpty();
		assertThat(smbFileDTO.getDenyShareTokens()).isEmpty();
		assertThat(smbFileDTO.getPermissionsHash()).isNull();
		assertThat(smbFileDTO.isFile()).isFalse();
		assertThat(smbFileDTO.isDirectory()).isFalse();
		assertThat(smbFileDTO.getExtension()).isEmpty();
		assertThat(smbFileDTO.getName()).isEmpty();
		assertThat(smbFileDTO.getLength()).isEqualTo(-10L);
		assertThat(smbFileDTO.getParsedContent()).isEmpty();
		assertThat(smbFileDTO.getLanguage()).isEmpty();
		assertThat(smbFileDTO.getErrorMessage()).contains("Permissions issue");
		assertThat(smbFileDTO.getLastModified()).isEqualTo(123456L);
	}

	@Test
	public void givenPermissionsExceptionWhenBuildingDTOForDocumentThenGetFailedDTO()
			throws IOException {
		// Given
		LocalDateTime time1 = new LocalDateTime();
		givenTimeIs(time1);

		WindowsPermissionsFactory windowsPermissionsFactory = new WindowsPermissionsFactoryImpl(trusteeManager, false, false);
		when(smbFile.getCanonicalPath()).thenReturn(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE);
		SmbFileDTOSimpleBuilder builder = Mockito.spy(new SmbFileDTOSimpleBuilder(logger, es, windowsPermissionsFactory));


		when(smbFile.exists()).thenReturn(true);
		when(smbFile.isFile()).thenReturn(true);
		when(smbFile.isDirectory()).thenReturn(false);
		when(smbFile.getName()).thenReturn(SmbTestParams.EXISTING_FILE);
		when(smbFile.length()).thenReturn(10_000_000L);
		when(smbFile.getInputStream()).thenReturn(new ByteArrayInputStream(SmbTestParams.EXISTING_FILE_CONTENT.getBytes()));
		when(smbFile.getLastModified()).thenReturn(123456L);
		when(smbFile.getShareSecurity(true)).thenThrow(new IOException("Permissions Exception"));

		// When
		SmbFileDTO smbFileDTO = builder.build(smbFile, true);

		// Then
		assertThat(smbFileDTO.getStatus()).isEqualTo(SmbFileDTOStatus.FAILED_DTO);
		assertThat(smbFileDTO.getLastFetchAttempt()).isEqualTo(time1);
		assertThat(smbFileDTO.getUrl()).isEqualTo(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE);
		assertThat(smbFileDTO.getErrorMessage()).contains("Permissions Exception");
	}
}
