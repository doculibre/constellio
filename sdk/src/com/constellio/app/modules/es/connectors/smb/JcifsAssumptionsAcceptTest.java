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
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;

public class JcifsAssumptionsAcceptTest extends ConstellioTest {
	private static final String VALID_SERVER = "smb://192.168.1.208/";
	private static final String VALID_SHARE = VALID_SERVER + "share/";
	private static final String VALID_FILE = VALID_SHARE + "file.txt";
	private static final String VALID_FOLDER = VALID_SHARE + "folder/";
	private static final String INVALID_SHARE = VALID_SERVER + "invalidShare/";
	private static final String VALID_SHARE_WITH_NON_EXISTING_FILE = VALID_SHARE + "nofile";
	private static final String VALID_SHARE_WITH_NON_EXISTING_FILE_IN_SUB_FOLDER = VALID_SHARE + "subfolder/nofile";
	private static final String INVALID_SERVER = "smb://192.168.1.207/share/nofile";
	private String domain = "";
	private String username = "";
	private String password = "";
	private NtlmPasswordAuthentication auth;

	@Before
	public void before() {
		domain = "";
		username = SDKPasswords.testSmbUsername();
		password = SDKPasswords.testSmbPassword();
		auth = new NtlmPasswordAuthentication(domain, username, password);
	}

	@Test
	@InDevelopmentTest
	/**
	 * smbFile.exists() returns True if the file exists and is available.
	 */
	public void givenExistingAndAvailableFileWhenVerifyingExistenceOfFileThenGetTrue()
			throws MalformedURLException, SmbException {
		SmbFile smbFile = new SmbFile(VALID_SHARE, auth);
		assertThat(smbFile.exists()).isTrue();
	}

	@Test
	@InDevelopmentTest
	/**
	 * smbFile.exists() returns False if the file does not exist but the share/server is available.
	 */
	public void givenNonExistingFileWhenVerifyingExistenceOfFileThenGetFalse()
			throws MalformedURLException, SmbException {
		SmbFile smbFile = new SmbFile(VALID_SHARE_WITH_NON_EXISTING_FILE, auth);
		assertThat(smbFile.exists()).isFalse();
	}

	@Test
	@InDevelopmentTest
	/**
	 * smbFile.exists() returns False if the file does not exist but the share/server is available.
	 */
	public void givenNonExistingFileInSubFolderWhenVerifyingExistenceOfFileThenGetFalse()
			throws MalformedURLException, SmbException {
		SmbFile smbFile = new SmbFile(VALID_SHARE_WITH_NON_EXISTING_FILE_IN_SUB_FOLDER, auth);
		assertThat(smbFile.exists()).isFalse();
	}

	@Test(expected = SmbException.class)
	@InDevelopmentTest
	/**
	 * smbFile.exists() throws "jcifs.smb.SmbException: The network name cannot be found" if the share is invalid.
	 */
	public void givenUnavailableShareWhenVerifyingExistenceOfFileThenGetException()
			throws SmbException, MalformedURLException {
		SmbFile smbFile = new SmbFile(INVALID_SHARE, auth);
		smbFile.exists();
	}

	@Test(expected = SmbException.class)
	@InDevelopmentTest
	/**
	 * smbFile.exists() throws "jcifs.smb.SmbException: Failed to connect: 0.0.0.0<00>/192.168.1.207" on a wrong ip.
	 * It takes about 30 seconds to throw the exception.
	 */
	public void givenInvalidIPWhenVerifyingExistenceOfFileThenGetException()
			throws MalformedURLException, SmbException {
		SmbFile smbFile = new SmbFile(INVALID_SERVER, auth);
		smbFile.exists();
	}

	@Test(expected = SmbAuthException.class)
	@InDevelopmentTest
	/**
	 * smbFile.exists() throws "jcifs.smb.SmbAuthException: Logon failure: unknown user name or bad password." if the username is invalid
	 */
	public void givenInvalidUsernameWhenVerifyingExistenceOfFileThenGetException()
			throws MalformedURLException, SmbException {
		auth = new NtlmPasswordAuthentication(domain, "invalidUsername", password);
		SmbFile smbFile = new SmbFile(VALID_SHARE, auth);
		smbFile.exists();
	}

	@Test(expected = SmbAuthException.class)
	@InDevelopmentTest
	/**
	 * smbFile.exists() throws "jcifs.smb.SmbAuthException: Logon failure: unknown user name or bad password." if the password is invalid
	 */
	public void givenInvalidPasswordWhenVerifyingExistenceOfFileThenGetException()
			throws MalformedURLException, SmbException {
		auth = new NtlmPasswordAuthentication(domain, username, "invalidPassword");
		SmbFile smbFile = new SmbFile(VALID_SHARE, auth);
		smbFile.exists();
	}

//	@Test
//	@InDevelopmentTest
//	/**
//	 * smbFile.exists() throws "jcifs.smb.SmbAuthException: Logon failure: unknown user name or bad password." if the password is invalid
//	 */
//	public void givenInvalidDomainWhenVerifyingExistenceOfFileThen()
//			throws MalformedURLException, SmbException {
//		auth = new NtlmPasswordAuthentication("invalidDomain", username, password);
//		SmbFile smbFile = new SmbFile(VALID_SHARE, auth);
//		smbFile.exists();
//		fail("Inconclusive. Need to test with a real domain.");
//	}

	@Test
	@InDevelopmentTest
	/**
	 * smbFile.isFile() and smbFile.isDirectory() return respectively false and true.
	 */
	public void givenExistingAndAvailableFileWhenDeterminingFileOrDirectoryThenGetFileOrDirectoryBooleans()
			throws MalformedURLException, SmbException {
		SmbFile smbFile = new SmbFile(VALID_SHARE, auth);
		assertThat(smbFile.isFile()).isFalse();
		assertThat(smbFile.isDirectory()).isTrue();
	}

	@Test
	@InDevelopmentTest
	/**
	 * smbFile.getCanonicalPath() returns the valid share path as smb://ip/share/
	 */
	public void givenAValidShareWhenGettingCanonicalPathThenGetPath()
			throws MalformedURLException {
		SmbFile smbFile = new SmbFile(VALID_SHARE, auth);
		assertThat(smbFile.getCanonicalPath()).isEqualTo(VALID_SHARE);
	}

	@Test
	@InDevelopmentTest
	/**
	 * smbFile.getCanonicalPath() returns the invalid share path even if it is not valid
	 */
	public void givenAnInvalidShareWhenGettingCanonicalPathThenUnexpectedlyGetCanonicalPath()
			throws MalformedURLException {
		SmbFile smbFile = new SmbFile(INVALID_SHARE, auth);
		assertThat(smbFile.getCanonicalPath()).isEqualTo(INVALID_SHARE);
	}

	@Test
	@InDevelopmentTest
	/**
	 * smbFile.getCanonicalPath() returns the invalid server path even if it is not valid
	 */
	public void givenAnInvalidServerWhenGettingCanonicalPathThenUnexpectedlyGetCanonicalPath()
			throws MalformedURLException {
		SmbFile smbFile = new SmbFile(INVALID_SERVER, auth);
		assertThat(smbFile.getCanonicalPath()).isEqualTo(INVALID_SERVER);
	}

	@Test
	@InDevelopmentTest
	/**
	 * smbFile.getCanonicalPath() returns the invalid file path even if it is not valid
	 */
	public void givenAValidFileWhenGettingCanonicalPathThenGetCanonicalPath()
			throws MalformedURLException {
		SmbFile smbFile = new SmbFile(VALID_FILE, auth);
		assertThat(smbFile.getCanonicalPath()).isEqualTo(VALID_FILE);
	}

	@Test
	@InDevelopmentTest
	/**
	 * smbFile.getCanonicalPath() returns the invalid file path even if it is not valid
	 */
	public void givenAnInvalidFileWhenGettingCanonicalPathThenUnexpectedlyGetCanonicalPath()
			throws MalformedURLException {
		SmbFile smbFile = new SmbFile(VALID_SHARE_WITH_NON_EXISTING_FILE, auth);
		assertThat(smbFile.getCanonicalPath()).isEqualTo(VALID_SHARE_WITH_NON_EXISTING_FILE);
	}

	@Test
	@InDevelopmentTest
	/**
	 * smbFile.length() returns the size
	 */
	public void givenAValidFileWhenGettingLengthThenGetFileLength()
			throws MalformedURLException, SmbException {
		SmbFile smbFile = new SmbFile(VALID_FILE, auth);
		assertThat(smbFile.length()).isGreaterThan(0L);
	}

	@Test
	@InDevelopmentTest
	/**
	 * smbFile.length() returns a large, non 0, long number. Not sure what it means.
	 */
	public void givenAValidShareWhenGettingLengthThenGetUnexpectedLargeLongNumber()
			throws MalformedURLException, SmbException {
		SmbFile smbFile = new SmbFile(VALID_SHARE, auth);
		assertThat(smbFile.length()).isNotEqualTo(0L);
	}

	@Test(expected = SmbException.class)
	@InDevelopmentTest
	/**
	 * smbFile.length() throws jcifs.smb.SmbException: The system cannot find the file specified.
	 */
	public void givenAnInvalidFileWhenGettingLengthThenGetAnException()
			throws MalformedURLException, SmbException {
		SmbFile smbFile = new SmbFile(VALID_SHARE_WITH_NON_EXISTING_FILE, auth);
		smbFile.length();
	}

	@Test(expected = SmbException.class)
	@InDevelopmentTest
	/**
	 * smbFile.length() throws jcifs.smb.SmbException: The network name cannot be found.
	 */
	public void givenAnInvalidShareWhenGettingLengthThenGetAnException()
			throws MalformedURLException, SmbException {
		SmbFile smbFile = new SmbFile(INVALID_SHARE, auth);
		smbFile.length();
	}

	@Test(expected = SmbException.class)
	@InDevelopmentTest
	/**
	 * smbFile.length() throws jcifs.smb.SmbException: Failed to connect: 0.0.0.0<00>/192.168.1.207
	 */
	public void givenAnInvalidServerWhenGettingLengthThenGetAnException()
			throws MalformedURLException, SmbException {
		SmbFile smbFile = new SmbFile(INVALID_SERVER, auth);
		smbFile.length();
	}

	@Test
	@InDevelopmentTest
	/**
	 * smbFile.getInputStream() returns data from the inputstream
	 */
	public void givenAValidFileWhenGettingInputStreamThen()
			throws IOException {
		SmbFile smbFile = new SmbFile(VALID_FILE, auth);
		assertThat(smbFile.getInputStream()
				.read()).isNotNull()
				.isNotEqualTo(-1);
	}

	@Test(expected = SmbException.class)
	@InDevelopmentTest
	/**
	 * smbFile.getInputSteam() throws jcifs.smb.SmbException: The system cannot find the file specified.
	 */
	public void givenAnInvalidFileWhenGettingInputStreamThen()
			throws IOException {
		SmbFile smbFile = new SmbFile(VALID_SHARE_WITH_NON_EXISTING_FILE, auth);
		smbFile.getInputStream()
				.read();
	}

	@Test
	@InDevelopmentTest
	/**
	 * smbFile.getLastModified() returns a non zero number.
	 */
	public void givenAFileWhenGettingLastModifiedThenGetLastModified()
			throws MalformedURLException {
		SmbFile smbFile = new SmbFile(VALID_FILE, auth);
		assertThat(smbFile.getLastModified()).isNotZero()
				.isBetween(0L, System.currentTimeMillis());
	}

	@Test
	@InDevelopmentTest
	/**
	 * smbFile.getLastModified() unexpectedly returns 0.
	 */
	public void givenAShareWhenGettingLastModifiedThenGetZero()
			throws MalformedURLException {
		SmbFile smbFile = new SmbFile(VALID_SHARE, auth);
		assertThat(smbFile.getLastModified()).isZero();
	}

	@Test
	@InDevelopmentTest
	/**
	 * smbFile.getLastModified() returns a non zero number.
	 */
	public void givenAFolderWhenGettingLastModifiedThenLastModified()
			throws MalformedURLException {
		SmbFile smbFile = new SmbFile(VALID_FOLDER, auth);
		assertThat(smbFile.getLastModified()).isNotZero()
				.isBetween(0L, System.currentTimeMillis());
	}
}
