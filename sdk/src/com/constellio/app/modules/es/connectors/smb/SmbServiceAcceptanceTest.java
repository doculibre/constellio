/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.es.connectors.smb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.constellio.app.modules.es.connectors.smb.SmbService.SmbStatus;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.SlowTest;

public class SmbServiceAcceptanceTest extends ConstellioTest {
	private String share;
	private String domain;
	private String username;
	private String password;
	private String filename = "file.txt";
	private String fileContent = "This file is not empty";
	private String foldername = "folder/";
	private long fileModified = 1440608437759L;
	private long folderModified = 1440450521080L;
	private String invalidShare;
	private String language = "en";
	private String extension = "txt";
	private long aboveFileSize = Long.MAX_VALUE;
	private long belowFileSize = 1024L;

	private SmbService smbService;

	private ESSchemasRecordsServices es;

	@Before
	public void setup() {
		share = SDKPasswords.testSmbShare();
		domain = SDKPasswords.testSmbDomain();
		username = SDKPasswords.testSmbUsername();
		password = SDKPasswords.testSmbPassword();
		invalidShare = "smb://192.168.1.208/invalidShare/file.txt";

		givenCollection(zeCollection).withConstellioESModule();
		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());

		smbService = new SmbService(domain, username, password, Arrays.asList(share), Arrays.asList(share), Arrays.asList(""), es, new ConsoleConnectorLogger());
	}

	@Test
	@InDevelopmentTest
	@SlowTest
	public void givenAValidUrlWhenGettingAFileThenGetAFile() {
		String smbUrl = share + filename;
		SmbFileDTO fileDTO = smbService.getSmbFileDTO(smbUrl);

		assertThat(fileDTO.getStatus()).isEqualTo(SmbStatus.OK);
		assertThat(fileDTO.isFile()).isTrue();
		assertThat(fileDTO.isDirectory()).isFalse();
		assertThat(fileDTO.getUrl()).isEqualTo(smbUrl);
		assertThat(fileDTO.getName()).isEqualTo(filename);
		assertThat(fileDTO.getParsedContent()).isEqualTo(fileContent);
		assertThat(fileDTO.getLength()).isEqualTo(fileContent.length());
		assertThat(fileDTO.getLastModified()).isEqualTo(fileModified);
		assertThat(fileDTO.getLastFetchAttempt()).isNotZero();
		assertThat(fileDTO.getPermissionsHash()).isNotEmpty();
		assertThat(fileDTO.getExtension()).isEqualTo(extension);
		assertThat(fileDTO.getLanguage()).isEqualTo(language);
		assertThat(fileDTO.getMissingMetadatas()).isEmpty();
		assertThat(fileDTO.getErrorMessage()).isEmpty();

		Method[] declaredMethods = fileDTO.getClass()
				.getDeclaredMethods();
		assertThat(declaredMethods.length - 1).isEqualTo(2 * 13);
	}

	@Test
	@InDevelopmentTest
	@SlowTest
	public void givenAValidUrlWhenGettingADirectoryThenGetADirectory() {
		String smbUrl = share + foldername;
		SmbFileDTO fileDTO = smbService.getSmbFileDTO(smbUrl);

		assertThat(fileDTO.getStatus()).isEqualTo(SmbStatus.OK);
		assertThat(fileDTO.isFile()).isFalse();
		assertThat(fileDTO.isDirectory()).isTrue();
		assertThat(fileDTO.getUrl()).isEqualTo(smbUrl);
		assertThat(fileDTO.getName()).isEqualTo(foldername);
		assertThat(fileDTO.getParsedContent()).isEqualTo("");
		assertThat(fileDTO.getLength()).isEqualTo(0L);
		assertThat(fileDTO.getLastModified()).isEqualTo(folderModified);
		assertThat(fileDTO.getLastFetchAttempt()).isNotZero();
		assertThat(fileDTO.getPermissionsHash()).isNotEmpty();
		assertThat(fileDTO.getExtension()).isEqualTo("");
		assertThat(fileDTO.getLanguage()).isEqualTo("");

		Method[] declaredMethods = fileDTO.getClass()
				.getDeclaredMethods();
		assertThat(declaredMethods.length - 1).isEqualTo(2 * 13);
	}

	@Test
	@InDevelopmentTest
	@SlowTest
	public void givenAValidUrlWhenGettingAShareThenGetAShare() {
		String smbUrl = share;
		SmbFileDTO fileDTO = smbService.getSmbFileDTO(smbUrl);

		assertThat(fileDTO.getStatus()).isEqualTo(SmbStatus.OK);
		assertThat(fileDTO.isFile()).isFalse();
		assertThat(fileDTO.isDirectory()).isTrue();
		assertThat(fileDTO.getUrl()).isEqualTo(smbUrl);
		assertThat(fileDTO.getName()).isEqualTo("share/");
		assertThat(fileDTO.getParsedContent()).isEqualTo("");
		assertThat(fileDTO.getLength()).isEqualTo(0L);
		assertThat(fileDTO.getLastModified()).isEqualTo(0L);
		assertThat(fileDTO.getLastFetchAttempt()).isNotZero();
		assertThat(fileDTO.getPermissionsHash()).isNotEmpty();
		assertThat(fileDTO.getExtension()).isEqualTo("");
		assertThat(fileDTO.getLanguage()).isEqualTo("");

		Method[] declaredMethods = fileDTO.getClass()
				.getDeclaredMethods();
		assertThat(declaredMethods.length - 1).isEqualTo(2 * 13);
	}

	@Test
	@InDevelopmentTest
	@SlowTest
	public void givenAValidSmbFileWhenGettingAFileThenGetAFile()
			throws MalformedURLException {
		String smbUrl = share + filename;
		SmbFile file = new SmbFile(smbUrl, new NtlmPasswordAuthentication(domain, username, password));
		SmbFileDTO fileDTO = smbService.getSmbFileDTO(file, true);

		assertThat(fileDTO.getStatus()).isEqualTo(SmbStatus.OK);
		assertThat(fileDTO.isFile()).isTrue();
		assertThat(fileDTO.isDirectory()).isFalse();
		assertThat(fileDTO.getUrl()).isEqualTo(smbUrl);
		assertThat(fileDTO.getName()).isEqualTo(filename);
		assertThat(fileDTO.getParsedContent()).isEqualTo(fileContent);
		assertThat(fileDTO.getLength()).isEqualTo(fileContent.length());
		assertThat(fileDTO.getLastModified()).isEqualTo(fileModified);
		assertThat(fileDTO.getLastFetchAttempt()).isNotZero();
		assertThat(fileDTO.getPermissionsHash()).isNotEmpty();
		assertThat(fileDTO.getExtension()).isEqualTo(extension);
		assertThat(fileDTO.getLanguage()).isEqualTo(language);
	}

	@Test
	@InDevelopmentTest
	@SlowTest
	public void givenAValidSmbFileWhenGettingADirectoryThenGetADirectory()
			throws MalformedURLException {
		String smbUrl = share;
		SmbFile file = new SmbFile(smbUrl, new NtlmPasswordAuthentication(domain, username, password));
		SmbFileDTO fileDTO = smbService.getSmbFileDTO(file, false);

		assertThat(fileDTO.getStatus()).isEqualTo(SmbStatus.OK);
		assertThat(fileDTO.isFile()).isFalse();
		assertThat(fileDTO.isDirectory()).isTrue();
		assertThat(fileDTO.getUrl()).isEqualTo(smbUrl);
		assertThat(fileDTO.getName()).isEqualTo("share/");
		assertThat(fileDTO.getParsedContent()).isEqualTo("");
		assertThat(fileDTO.getLength()).isEqualTo(0L);
		assertThat(fileDTO.getLastModified()).isEqualTo(0L);
		assertThat(fileDTO.getLastFetchAttempt()).isNotZero();
		assertThat(fileDTO.getPermissionsHash()).isNotEmpty();
		assertThat(fileDTO.getExtension()).isEqualTo("");
		assertThat(fileDTO.getLanguage()).isEqualTo("");
	}

	@Test
	@InDevelopmentTest
	@SlowTest
	public void givenValidShareButInvalidFileWhenGettingFileThenGetDoesNotExistStatus() {
		String smbUrl = share + "nonExistentFile.txt";
		SmbFileDTO fileDTO = smbService.getSmbFileDTO(smbUrl);

		assertThat(fileDTO.getStatus()).isEqualTo(SmbStatus.GONE);
		assertThat(fileDTO.getUrl()).isEqualTo(smbUrl);
		assertThat(fileDTO.getParsedContent()).isEqualTo("");
		assertThat(fileDTO.getExtension()).isEqualTo("");
		assertThat(fileDTO.getLanguage()).isEqualTo("");
		assertThat(fileDTO.getLastModified()).isZero();
		assertThat(fileDTO.getLastFetchAttempt()).isNotZero();
	}

	@Test
	@InDevelopmentTest
	@SlowTest
	public void givenConnectivityIssueWhenVerifyingFileExistenceThenGetExistenceUnknownStatus()
			throws SmbException {

		String smbUrl = share + "nonExistentFile.txt";

		SmbFile file = Mockito.mock(SmbFile.class);
		when(file.exists()).thenThrow(SmbException.class);
		when(file.getCanonicalPath()).thenReturn(smbUrl);
		when(file.getLastModified()).thenReturn(new Date().getTime());

		SmbFileDTO fileDTO = smbService.getSmbFileDTO(file, true);

		assertThat(fileDTO.getStatus()).isEqualTo(SmbStatus.UNKNOWN);
		assertThat(fileDTO.getUrl()).isEqualTo(smbUrl);
		assertThat(fileDTO.getParsedContent()).isEqualTo("");
		assertThat(fileDTO.getExtension()).isEqualTo("");
		assertThat(fileDTO.getLanguage()).isEqualTo("");
		assertThat(fileDTO.getLastModified()).isNotZero();
		assertThat(fileDTO.getLastFetchAttempt()).isNotZero();
	}

	@Test
	@InDevelopmentTest
	@SlowTest
	public void givenInvalidShareWhenVerifyingFileExistenceThen()
			throws SmbException {

		String smbUrl = invalidShare;
		SmbException mockException = Mockito.mock(SmbException.class);
		when(mockException.getMessage()).thenReturn("The network name cannot be found");
		SmbFile file = Mockito.mock(SmbFile.class);
		when(file.exists()).thenThrow(mockException);
		when(file.getCanonicalPath()).thenReturn(smbUrl);
		when(file.getLastModified()).thenReturn(new Date().getTime());

		SmbFileDTO fileDTO = smbService.getSmbFileDTO(file, true);

		assertThat(fileDTO.getStatus()).isEqualTo(SmbStatus.GONE);
		assertThat(fileDTO.getUrl()).isEqualTo(smbUrl);
		assertThat(fileDTO.getParsedContent()).isEqualTo("");
		assertThat(fileDTO.getExtension()).isEqualTo("");
		assertThat(fileDTO.getLanguage()).isEqualTo("");
		assertThat(fileDTO.getLastModified()).isNotZero();
		assertThat(fileDTO.getLastFetchAttempt()).isNotZero();

	}

	@Test
	@InDevelopmentTest
	@SlowTest
	public void givenConnectivityIssueWhenVerifyingIfFileThenGetPartialCreationStatus()
			throws SmbException {

		String smbUrl = share + "nonExistentFile.txt";

		SmbFile file = Mockito.mock(SmbFile.class);
		when(file.exists()).thenReturn(true);
		when(file.getCanonicalPath()).thenReturn(smbUrl);
		when(file.getLastModified()).thenReturn(new Date().getTime());
		when(file.isFile()).thenThrow(SmbException.class);

		SmbFileDTO fileDTO = smbService.getSmbFileDTO(file, true);

		assertThat(fileDTO.getStatus()).isEqualTo(SmbStatus.PARTIAL);
		assertThat(fileDTO.getUrl()).isEqualTo(smbUrl);
		assertThat(fileDTO.getParsedContent()).isEqualTo("");
		assertThat(fileDTO.getExtension()).isEqualTo("");
		assertThat(fileDTO.getLanguage()).isEqualTo("");
		assertThat(fileDTO.getLastModified()).isNotZero();
		assertThat(fileDTO.getLastFetchAttempt()).isNotZero();
	}

	@Test
	@InDevelopmentTest
	@SlowTest
	public void givenConnectivityIssueWhenVerifyingFileLengthThenGetPartialCreationStatus()
			throws SmbException {

		String smbUrl = share + "nonExistentFile.txt";

		SmbFile file = Mockito.mock(SmbFile.class);
		when(file.exists()).thenReturn(true);
		when(file.getCanonicalPath()).thenReturn(smbUrl);
		when(file.getLastModified()).thenReturn(new Date().getTime());
		when(file.isFile()).thenReturn(true);
		when(file.length()).thenThrow(SmbException.class);

		SmbFileDTO fileDTO = smbService.getSmbFileDTO(file, true);

		assertThat(fileDTO.getStatus()).isEqualTo(SmbStatus.PARTIAL);
		assertThat(fileDTO.getUrl()).isEqualTo(smbUrl);
		assertThat(fileDTO.getParsedContent()).isEqualTo("");
		assertThat(fileDTO.getExtension()).isEqualTo("");
		assertThat(fileDTO.getLanguage()).isEqualTo("");
		assertThat(fileDTO.getLastModified()).isNotZero();
		assertThat(fileDTO.getLastFetchAttempt()).isNotZero();
	}

	@Test
	@InDevelopmentTest
	@SlowTest
	public void givenConnectivityIssueWhenListingFilesThenReturnEmptyList()
			throws SmbException, MalformedURLException {

		String smbUrl = "smb://ip/badshare/";
		smbService = Mockito.spy(new SmbService(domain, username, password, Arrays.asList(share), Arrays.asList(share), Arrays.asList(""), es,
				new ConsoleConnectorLogger()));

		SmbFile file = Mockito.mock(SmbFile.class);
		when(file.listFiles()).thenThrow(SmbException.class);
		doReturn(file).when(smbService)
				.getSmbFile(anyString());

		SmbFileDTO startingSmbFileDTO = new SmbFileDTO();
		startingSmbFileDTO.setIsDirectory(true);
		startingSmbFileDTO.setUrl(smbUrl);

		List<SmbFileDTO> fileDTOs = smbService.getChildrenIn(startingSmbFileDTO);
		assertThat(fileDTOs).isEmpty();
	}

	@Test
	@InDevelopmentTest
	@SlowTest
	public void givenExclusionsWhenListingThenGetFilteredList()
			throws MalformedURLException {
		smbService = Mockito.spy(new SmbService(domain, username, password, Arrays.asList(share), Arrays.asList(share), Arrays.asList(".*folder.*"), es,
				new ConsoleConnectorLogger()));

		String smbUrl = share;
		SmbFile file = new SmbFile(smbUrl, new NtlmPasswordAuthentication(domain, username, password));
		SmbFileDTO fileDTO = smbService.getSmbFileDTO(file, false);
		List<SmbFileDTO> fileDTOs = smbService.getChildrenIn(fileDTO);
		assertThat(fileDTOs).extracting("url")
				.containsOnly(share + filename);
	}

	@Test
	@InDevelopmentTest
	@SlowTest
	public void givenInclusionsWhenListingThenGetFilteredList()
			throws MalformedURLException {
		smbService = Mockito.spy(new SmbService(domain, username, password, Arrays.asList(share), Arrays.asList(".*file.*"), Arrays.asList(""), es,
				new ConsoleConnectorLogger()));

		String smbUrl = share;
		SmbFile file = new SmbFile(smbUrl, new NtlmPasswordAuthentication(domain, username, password));
		SmbFileDTO fileDTO = smbService.getSmbFileDTO(file, false);
		List<SmbFileDTO> fileDTOs = smbService.getChildrenIn(fileDTO);
		assertThat(fileDTOs).extracting("url")
				.containsOnly(share + filename);
	}

	@Test
	@InDevelopmentTest
	@SlowTest
	public void givenOnlySeedsWhenListingThenGetFilteredList()
			throws MalformedURLException {
		smbService = Mockito.spy(new SmbService(domain, username, password, Arrays.asList(share), new ArrayList<String>(), new ArrayList<String>(), es,
				new ConsoleConnectorLogger()));

		String smbUrl = share;
		SmbFile file = new SmbFile(smbUrl, new NtlmPasswordAuthentication(domain, username, password));
		SmbFileDTO fileDTO = smbService.getSmbFileDTO(file, false);
		List<SmbFileDTO> fileDTOs = smbService.getChildrenIn(fileDTO);
		assertThat(fileDTOs).extracting("url")
				.containsOnly(share + filename, share + foldername);
	}

	@Test
	@InDevelopmentTest
	@SlowTest
	public void givenAFileAboveSizeThresholdWhenParsingContentThenSkipParsingContent()
			throws MalformedURLException, SmbException {

		String smbUrl = share + filename;
		SmbFile file = Mockito.spy(new SmbFile(smbUrl, new NtlmPasswordAuthentication(domain, username, password)));
		doReturn(aboveFileSize).when(file)
				.length();

		SmbFileDTO fileDTO = smbService.getSmbFileDTO(file, true);

		assertThat(fileDTO.getStatus()).isEqualTo(SmbStatus.PARTIAL);
		assertThat(fileDTO.isFile()).isTrue();
		assertThat(fileDTO.isDirectory()).isFalse();
		assertThat(fileDTO.getUrl()).isEqualTo(smbUrl);
		assertThat(fileDTO.getName()).isEqualTo(filename);
		assertThat(fileDTO.getParsedContent()).isEqualTo("");
		assertThat(fileDTO.getLength()).isEqualTo(aboveFileSize);
		assertThat(fileDTO.getLanguage()).isEqualTo("");
	}

	@Test
	@InDevelopmentTest
	@SlowTest
	public void givenAFileBelowSizeThresholdWhenParsingContentParseContent()
			throws MalformedURLException, SmbException {

		String smbUrl = share + filename;
		SmbFile file = Mockito.spy(new SmbFile(smbUrl, new NtlmPasswordAuthentication(domain, username, password)));
		doReturn(belowFileSize).when(file)
				.length();

		SmbFileDTO fileDTO = smbService.getSmbFileDTO(file, true);

		assertThat(fileDTO.getStatus()).isEqualTo(SmbStatus.OK);
		assertThat(fileDTO.isFile()).isTrue();
		assertThat(fileDTO.isDirectory()).isFalse();
		assertThat(fileDTO.getUrl()).isEqualTo(smbUrl);
		assertThat(fileDTO.getName()).isEqualTo(filename);
		assertThat(fileDTO.getParsedContent()).isEqualTo(fileContent);
		assertThat(fileDTO.getLength()).isEqualTo(belowFileSize);
		assertThat(fileDTO.getLanguage()).isEqualTo(language);
	}

	@Test
	@InDevelopmentTest
	@SlowTest
	public void whenFullyRetrievingDocumentAndMetadatasThenNoMissingDocumentOrMetadatas()
			throws MalformedURLException, SmbException {
		String smbUrl = share + filename;
		SmbFile file = new SmbFile(smbUrl, new NtlmPasswordAuthentication(domain, username, password));

		SmbFileDTO fileDTO = smbService.getSmbFileDTO(file, true);

		assertThat(fileDTO.getMissingMetadatas()).isEmpty();
	}

	@Test
	@InDevelopmentTest
	@SlowTest
	public void whenPartiallyRetrievingDocumentOrMetadatasThenMissingDocumentOrMetadatasAreIdentified()
			throws MalformedURLException, SmbException {
		String smbUrl = share + filename;
		SmbFile file = Mockito.spy(new SmbFile(smbUrl, new NtlmPasswordAuthentication(domain, username, password)));

		doThrow(Exception.class).when(file)
				.length();
		SmbFileDTO fileDTO = smbService.getSmbFileDTO(file, true);

		assertThat(fileDTO.getMissingMetadatas()).isNotEmpty();
	}

	@Test
	@InDevelopmentTest
	@SlowTest
	public void whenRetrievingFolderThen()
			throws MalformedURLException, SmbException {
		String smbUrl = share + foldername;
		SmbFile file = new SmbFile(smbUrl, new NtlmPasswordAuthentication(domain, username, password));

		SmbFileDTO fileDTO = smbService.getSmbFileDTO(file, true);

		assertThat(fileDTO.getMissingMetadatas()).isEmpty();
		;
	}
}