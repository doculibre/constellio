package com.constellio.app.modules.es.connectors.smb.assumptions;

import com.constellio.app.modules.es.connectors.smb.testutils.SmbTestCommand;
import com.constellio.app.modules.es.connectors.smb.testutils.SmbTestCommandFactory;
import com.constellio.app.modules.es.connectors.smb.testutils.SmbTestCommandFactory.SmbTestCommandType;
import com.constellio.sdk.tests.ConstellioTest;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import java.io.IOException;
import java.net.MalformedURLException;

import static org.assertj.core.api.Assertions.assertThat;

public class JcifsAssumptionsRealTest extends ConstellioTest {
	private static String validServer;
	private static String validShare;
	private static String validFile;
	private static String validFolder;
	private static String invalidShare;
	private static String validShareWithNonExistingFile;
	private static String validShareWithNonExistingFileInSubFolder;
	private static String invalidServer;
	private static String domain;
	private static String username;
	private static String password;
	private static NtlmPasswordAuthentication auth;
	private static SmbTestCommandFactory commandFactory;

	//	@BeforeClass
	//	public static void before() {
	//		domain = SDKPasswords.testSmbDomain();
	//		username = SDKPasswords.testSmbUsername();
	//		password = SDKPasswords.testSmbPassword();
	//		auth = new NtlmPasswordAuthentication(domain, username, password);
	//
	//		validServer = SDKPasswords.testSmbVMHost();
	//		validShare = validServer + SDKPasswords.testSmbShare();
	//		validFile = validShare + SmbTestParams.FILE_NAME;
	//		validFolder = validShare + SmbTestParams.FOLDER_NAME;
	//		invalidShare = validServer + "invalidShare/";
	//		validShareWithNonExistingFile = validShare + "nofile";
	//		validShareWithNonExistingFileInSubFolder = validShare + "subfolder/nofile";
	//		invalidServer = "smb://192.168.1.207/share/nofile";
	//
	//		commandFactory = new SmbTestCommandFactory(auth);
	//		SmbTestCommand populateMinimalShare = commandFactory.get(SmbTestCommandType.POPULATE_MINIMAL_SHARE, validShare, "");
	//		populateMinimalShare.execute();
	//	}

	////@Test

	/**
	 * smbFile.exists() returns True if the file exists and is available.
	 */
	public void givenExistingAndAvailableFileWhenVerifyingExistenceOfFileThenGetTrue()
			throws MalformedURLException, SmbException {
		SmbFile smbFile = new SmbFile(validShare, auth);
		assertThat(smbFile.exists()).isTrue();
	}

	//@Test

	/**
	 * smbFile.exists() returns False if the file does not exist but the share/server is available.
	 */
	public void givenNonExistingFileWhenVerifyingExistenceOfFileThenGetFalse()
			throws MalformedURLException, SmbException {
		SmbFile smbFile = new SmbFile(validShareWithNonExistingFile, auth);
		assertThat(smbFile.exists()).isFalse();
	}

	//@Test

	/**
	 * smbFile.exists() returns False if the file does not exist but the share/server is available.
	 */
	public void givenNonExistingFileInSubFolderWhenVerifyingExistenceOfFileThenGetFalse()
			throws MalformedURLException, SmbException {
		SmbFile smbFile = new SmbFile(validShareWithNonExistingFileInSubFolder, auth);
		assertThat(smbFile.exists()).isFalse();
	}

	//@Test(expected = SmbException.class)

	/**
	 * smbFile.exists() throws "jcifs.smb.SmbException: The network name cannot be found" if the share is invalid.
	 */
	public void givenUnavailableShareWhenVerifyingExistenceOfFileThenGetException()
			throws SmbException, MalformedURLException {
		SmbFile smbFile = new SmbFile(invalidShare, auth);
		smbFile.exists();
	}

	//@Test(expected = SmbException.class)

	// Confirm @SlowTest
	/**
	 * smbFile.exists() throws "jcifs.smb.SmbException: Failed to connect: 0.0.0.0<00>/192.168.1.207" on a wrong ip.
	 * It takes about 30 seconds to throw the exception.
	 */
	public void givenInvalidIPWhenVerifyingExistenceOfFileThenGetException()
			throws MalformedURLException, SmbException {
		SmbFile smbFile = new SmbFile(invalidServer, auth);
		smbFile.exists();
	}

	//@Test(expected = SmbAuthException.class)

	/**
	 * smbFile.exists() throws "jcifs.smb.SmbAuthException: Logon failure: unknown user name or bad password." if the username is invalid
	 */
	public void givenInvalidUsernameWhenVerifyingExistenceOfFileThenGetException()
			throws MalformedURLException, SmbException {
		auth = new NtlmPasswordAuthentication(domain, "invalidUsername", password);
		SmbFile smbFile = new SmbFile(validShare, auth);
		smbFile.exists();
	}

	//@Test(expected = SmbAuthException.class)

	/**
	 * smbFile.exists() throws "jcifs.smb.SmbAuthException: Logon failure: unknown user name or bad password." if the password is invalid
	 */
	public void givenInvalidPasswordWhenVerifyingExistenceOfFileThenGetException()
			throws MalformedURLException, SmbException {
		auth = new NtlmPasswordAuthentication(domain, username, "invalidPassword");
		SmbFile smbFile = new SmbFile(validShare, auth);
		smbFile.exists();
	}

	// //@Test
	// 
	// /**
	// * smbFile.exists() throws "jcifs.smb.SmbAuthException: Logon failure: unknown user name or bad password." if the password
	// is invalid
	// */
	// public void givenInvalidDomainWhenVerifyingExistenceOfFileThen()
	// throws MalformedURLException, SmbException {
	// auth = new NtlmPasswordAuthentication("invalidDomain", username, password);
	// SmbFile smbFile = new SmbFile(VALID_SHARE, auth);
	// smbFile.exists();
	// fail("Inconclusive. Need to test with a real domain.");
	// }

	//@Test

	/**
	 * smbFile.isFile() and smbFile.isDirectory() return respectively false and true.
	 */
	public void givenExistingAndAvailableFileWhenDeterminingFileOrDirectoryThenGetFileOrDirectoryBooleans()
			throws MalformedURLException, SmbException {
		SmbFile smbFile = new SmbFile(validShare, auth);
		assertThat(smbFile.isFile()).isFalse();
		assertThat(smbFile.isDirectory()).isTrue();
	}

	//@Test

	/**
	 * smbFile.getCanonicalPath() returns the valid share path as smb://ip/share/
	 */
	public void givenAValidShareWhenGettingCanonicalPathThenGetPath()
			throws MalformedURLException {
		SmbFile smbFile = new SmbFile(validShare, auth);
		assertThat(smbFile.getCanonicalPath()).isEqualTo(validShare);
	}

	//@Test

	/**
	 * smbFile.getCanonicalPath() returns the invalid share path even if it is not valid
	 */
	public void givenAnInvalidShareWhenGettingCanonicalPathThenUnexpectedlyGetCanonicalPath()
			throws MalformedURLException {
		SmbFile smbFile = new SmbFile(invalidShare, auth);
		assertThat(smbFile.getCanonicalPath()).isEqualTo(invalidShare);
	}

	//@Test

	/**
	 * smbFile.getCanonicalPath() returns the invalid server path even if it is not valid
	 */
	public void givenAnInvalidServerWhenGettingCanonicalPathThenUnexpectedlyGetCanonicalPath()
			throws MalformedURLException {
		SmbFile smbFile = new SmbFile(invalidServer, auth);
		assertThat(smbFile.getCanonicalPath()).isEqualTo(invalidServer);
	}

	//@Test

	/**
	 * smbFile.getCanonicalPath() returns the invalid file path even if it is not valid
	 */
	public void givenAValidFileWhenGettingCanonicalPathThenGetCanonicalPath()
			throws MalformedURLException {
		SmbFile smbFile = new SmbFile(validFile, auth);
		assertThat(smbFile.getCanonicalPath()).isEqualTo(validFile);
	}

	//@Test

	/**
	 * smbFile.getCanonicalPath() returns the invalid file path even if it is not valid
	 */
	public void givenAnInvalidFileWhenGettingCanonicalPathThenUnexpectedlyGetCanonicalPath()
			throws MalformedURLException {
		SmbFile smbFile = new SmbFile(validShareWithNonExistingFile, auth);
		assertThat(smbFile.getCanonicalPath()).isEqualTo(validShareWithNonExistingFile);
	}

	//@Test

	/**
	 * smbFile.length() returns the size
	 */
	public void givenAValidFileWhenGettingLengthThenGetFileLength()
			throws MalformedURLException, SmbException {
		SmbFile smbFile = new SmbFile(validFile, auth);
		assertThat(smbFile.length()).isGreaterThan(0L);
	}

	//@Test

	/**
	 * smbFile.length() returns a large, non 0, long number. Not sure what it means.
	 */
	public void givenAValidShareWhenGettingLengthThenGetUnexpectedLargeLongNumber()
			throws MalformedURLException, SmbException {
		SmbFile smbFile = new SmbFile(validShare, auth);
		assertThat(smbFile.length()).isNotEqualTo(0L);
	}

	//@Test(expected = SmbException.class)

	/**
	 * smbFile.length() throws jcifs.smb.SmbException: The system cannot find the file specified.
	 */
	public void givenAnInvalidFileWhenGettingLengthThenGetAnException()
			throws MalformedURLException, SmbException {
		SmbFile smbFile = new SmbFile(validShareWithNonExistingFile, auth);
		smbFile.length();
	}

	//@Test(expected = SmbException.class)

	/**
	 * smbFile.length() throws jcifs.smb.SmbException: The network name cannot be found.
	 */
	public void givenAnInvalidShareWhenGettingLengthThenGetAnException()
			throws MalformedURLException, SmbException {
		SmbFile smbFile = new SmbFile(invalidShare, auth);
		smbFile.length();
	}

	//@Test(expected = SmbException.class)

	// Confirm @SlowTest
	/**
	 * smbFile.length() throws jcifs.smb.SmbException: Failed to connect: 0.0.0.0<00>/192.168.1.207
	 */
	public void givenAnInvalidServerWhenGettingLengthThenGetAnException()
			throws MalformedURLException, SmbException {
		SmbFile smbFile = new SmbFile(invalidServer, auth);
		smbFile.length();
	}

	//@Test

	/**
	 * smbFile.getInputStream() returns data from the inputstream
	 */
	public void givenAValidFileWhenGettingInputStreamThen()
			throws IOException {
		SmbFile smbFile = new SmbFile(validFile, auth);
		assertThat(smbFile.getInputStream()
				.read()).isNotNull()
				.isNotEqualTo(-1);
	}

	//@Test(expected = SmbException.class)

	/**
	 * smbFile.getInputSteam() throws jcifs.smb.SmbException: The system cannot find the file specified.
	 */
	public void givenAnInvalidFileWhenGettingInputStreamThen()
			throws IOException {
		SmbFile smbFile = new SmbFile(validShareWithNonExistingFile, auth);
		smbFile.getInputStream()
				.read();
	}

	//@Test

	/**
	 * smbFile.getLastModified() returns a non zero number.
	 */
	public void givenAFileWhenGettingLastModifiedThenGetLastModified()
			throws MalformedURLException {
		SmbFile smbFile = new SmbFile(validFile, auth);
		assertThat(smbFile.getLastModified()).isNotZero()
				.isBetween(0L, System.currentTimeMillis());
	}

	//@Test

	/**
	 * smbFile.getLastModified() unexpectedly returns 0.
	 */
	public void givenAShareWhenGettingLastModifiedThenGetZero()
			throws MalformedURLException {
		SmbFile smbFile = new SmbFile(validShare, auth);
		assertThat(smbFile.getLastModified()).isZero();
	}

	//@Test

	/**
	 * smbFile.getLastModified() returns a non zero number.
	 */
	public void givenAFolderWhenGettingLastModifiedThenLastModified()
			throws MalformedURLException {
		SmbFile smbFile = new SmbFile(validFolder, auth);
		assertThat(smbFile.getLastModified()).isNotZero()
				.isBetween(0L, System.currentTimeMillis());
	}

	//@Test

	/**
	 * smbFile.listFiles() returns the file in this order
	 * smb://192.168.1.246/share/file0
	 * smb://192.168.1.246/share/file01
	 * smb://192.168.1.246/share/file10
	 * smb://192.168.1.246/share/filea
	 * smb://192.168.1.246/share/filez
	 * smb://192.168.1.246/share/folder0/
	 * smb://192.168.1.246/share/folder01/
	 * smb://192.168.1.246/share/folder10/
	 * smb://192.168.1.246/share/foldera/
	 * smb://192.168.1.246/share/folderz/
	 * <p>
	 * Also, cannot create both filea and fileA in the same location.
	 */
	public void whenListingShareContentThenGetSpecificOrder()
			throws MalformedURLException, SmbException, InterruptedException {
		Thread.sleep(3000);
		SmbTestCommand clean = commandFactory.get(SmbTestCommandType.CLEAN_SHARE, validShare, "");
		clean.execute();

		SmbTestCommand file0 = commandFactory.get(SmbTestCommandType.CREATE_FILE, validShare + "file0", "");
		file0.execute();
		SmbTestCommand file10 = commandFactory.get(SmbTestCommandType.CREATE_FILE, validShare + "file10", "");
		file10.execute();
		SmbTestCommand file01 = commandFactory.get(SmbTestCommandType.CREATE_FILE, validShare + "file01", "");
		file01.execute();
		SmbTestCommand filea = commandFactory.get(SmbTestCommandType.CREATE_FILE, validShare + "filea", "");
		filea.execute();
		SmbTestCommand filez = commandFactory.get(SmbTestCommandType.CREATE_FILE, validShare + "filez", "");
		filez.execute();

		SmbTestCommand folder0 = commandFactory.get(SmbTestCommandType.CREATE_FOLDER, validShare + "folder0/", "");
		folder0.execute();
		SmbTestCommand folder10 = commandFactory.get(SmbTestCommandType.CREATE_FOLDER, validShare + "folder10/", "");
		folder10.execute();
		SmbTestCommand folder01 = commandFactory.get(SmbTestCommandType.CREATE_FOLDER, validShare + "folder01/", "");
		folder01.execute();
		SmbTestCommand foldera = commandFactory.get(SmbTestCommandType.CREATE_FOLDER, validShare + "foldera/", "");
		foldera.execute();
		SmbTestCommand folderz = commandFactory.get(SmbTestCommandType.CREATE_FOLDER, validShare + "folderz/", "");
		folderz.execute();

		SmbFile smbFile = new SmbFile(validShare, auth);
		SmbFile[] files = smbFile.listFiles();
		assertThat(files).extracting("canonicalPath")
				.containsSequence("smb://192.168.1.246/share/file0", "smb://192.168.1.246/share/file01", "smb://192.168.1.246/share/file10",
						"smb://192.168.1.246/share/filea", "smb://192.168.1.246/share/filez", "smb://192.168.1.246/share/folder0/",
						"smb://192.168.1.246/share/folder01/", "smb://192.168.1.246/share/folder10/", "smb://192.168.1.246/share/foldera/",
						"smb://192.168.1.246/share/folderz/");

		clean.execute();

		SmbTestCommand populateMinimalShare = commandFactory.get(SmbTestCommandType.POPULATE_MINIMAL_SHARE, validShare, "");
		populateMinimalShare.execute();
	}

	//	@AfterClass
	//	public static void after()
	//			throws InterruptedException {
	//		Thread.sleep(1000);
	//		SmbTestCommand clean = commandFactory.get(SmbTestCommandType.CLEAN_SHARE, validShare, "");
	//		clean.execute();
	//	}
}
