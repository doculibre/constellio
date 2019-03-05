package com.constellio.app.modules.es.connectors.smb.testutils;

import com.constellio.app.modules.es.connectors.smb.config.SmbRetrievalConfiguration;
import com.constellio.app.modules.es.connectors.smb.security.Credentials;
import com.constellio.app.modules.es.connectors.smb.security.WindowsPermissions;
import jcifs.smb.ACE;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class SmbServiceTestUtils {
	private Credentials validCredentials;
	private Credentials credentialsWithInvalidDomain;
	private Credentials credentialsWithInvalidUsername;
	private Credentials credentialsWithInvalidPassword;

	private SmbRetrievalConfiguration fetchValidShare;
	private SmbRetrievalConfiguration fetchValidFolder;
	private SmbRetrievalConfiguration fetchValidFile;

	private String validShare = "smb://host/share/";
	private String validFolder = validShare + "folder/";
	private String validFileName = "file.ext";
	private String validFileContent = "Content of the valid file";
	private String validFile = validShare + validFileName;

	private String malformedUrl = "localhost/share/";

	private SmbFile validSmbFile;

	private boolean existsValue;
	private Exception existsException;

	private String canonicalPathValue;
	private Exception canonicalPathException;

	private long lastModified;
	private Exception lastModifiedException;

	private String name;
	private Exception nameException;

	private long length;
	private Exception lengthException;

	private InputStream inputStream;
	private Exception inputStreamException;

	private long contentLength;
	private Exception contentLengthException;

	private ACE[] ntfsSEcurity;
	private Exception ntfsSecurityException;

	private boolean isFile;
	private Exception isFileException;

	private boolean isFolder;
	private Exception isFolderException;

	private WindowsPermissions windowsPermissions;

	public SmbServiceTestUtils()
			throws IOException {
		validCredentials = new Credentials("validDomain", "validUsername", "validPassword");
		credentialsWithInvalidDomain = new Credentials("invalidDomain", "validUsername", "validPassword");
		credentialsWithInvalidUsername = new Credentials("validDomain", "invalidUsername", "validPassword");
		credentialsWithInvalidPassword = new Credentials("validDomain", "validUsername", "invalidPassword");

		fetchValidShare = new SmbRetrievalConfiguration(Arrays.asList(validShare), Arrays.asList(validShare), new ArrayList<String>(), false, false);
		fetchValidFolder = new SmbRetrievalConfiguration(Arrays.asList(validFolder), Arrays.asList(validFolder), new ArrayList<String>(), false, false);
		fetchValidFile = new SmbRetrievalConfiguration(Arrays.asList(validFile), Arrays.asList(validFile), new ArrayList<String>(), false, false);

		// ///////////////////////////////////////////////////
		// Valid Smb File
		// ///////////////////////////////////////////////////
		validSmbFile = Mockito.mock(SmbFile.class);

		existsValue = true;
		when(validSmbFile.exists()).thenAnswer(new ExistsAnswer());

		canonicalPathValue = validFile;
		when(validSmbFile.getCanonicalPath()).thenAnswer(new CanonicalPathAnswer());

		lastModified = System.currentTimeMillis();
		when(validSmbFile.getLastModified()).thenAnswer(new LastModifiedAnswer());

		name = validFileName;
		when(validSmbFile.getName()).thenAnswer(new NameAnswer());

		length = new Long(validFileContent.length());
		doAnswer(new LengthAnswer()).when(validSmbFile)
				.length();

		inputStream = new ByteArrayInputStream(validFileContent.getBytes());
		when(validSmbFile.getInputStream()).thenAnswer(new InputStreamAnswer());

		contentLength = validFileContent.length();
		doAnswer(new ContentLengthAnswer()).when(validSmbFile)
				.getContentLength();

		ntfsSEcurity = new ACE[0];
		when(validSmbFile.getSecurity(anyBoolean())).thenAnswer(new NtfsSecurityAnswer());

		isFile = true;
		when(validSmbFile.isFile()).thenAnswer(new IsFileAnswer());

		isFolder = false;
		when(validSmbFile.isDirectory()).thenAnswer(new IsFolderAnswer());

		windowsPermissions = new FakeWindowsPermissions(null, null, SmbTestParams.ALLOW_TOKENS, SmbTestParams.ALLOW_SHARE_TOKENS, SmbTestParams.DENY_TOKENS,
				SmbTestParams.DENY_SHARE_TOKENS, SmbTestParams.EXISTING_FILE_PERMISSION_HASH);

		when(validSmbFile.listFiles(any(SmbFileFilter.class))).thenAnswer(new ListFilesAnswer());
	}

	private class ExistsAnswer implements Answer<Boolean> {
		@Override
		public Boolean answer(InvocationOnMock invocation)
				throws Throwable {
			if (existsException != null) {
				throw existsException;
			}
			return existsValue;
		}
	}

	private class CanonicalPathAnswer implements Answer<String> {
		@Override
		public String answer(InvocationOnMock invocation)
				throws Throwable {
			if (canonicalPathException != null) {
				throw canonicalPathException;
			}
			return canonicalPathValue;
		}
	}

	private class LastModifiedAnswer implements Answer<Long> {
		@Override
		public Long answer(InvocationOnMock invocation)
				throws Throwable {
			if (lastModifiedException != null) {
				throw lastModifiedException;
			}
			return lastModified;
		}
	}

	private class NameAnswer implements Answer<String> {
		@Override
		public String answer(InvocationOnMock invocation)
				throws Throwable {
			if (nameException != null) {
				throw nameException;
			}
			return name;
		}
	}

	private class LengthAnswer implements Answer<Long> {
		@Override
		public Long answer(InvocationOnMock invocation)
				throws Throwable {
			if (lengthException != null) {
				throw lengthException;
			}
			return new Long(length);
		}
	}

	private class InputStreamAnswer implements Answer<InputStream> {
		@Override
		public InputStream answer(InvocationOnMock invocation)
				throws Throwable {
			if (inputStreamException != null) {
				throw inputStreamException;
			}
			return inputStream;
		}
	}

	private class ContentLengthAnswer implements Answer<Long> {
		@Override
		public Long answer(InvocationOnMock invocation)
				throws Throwable {
			if (contentLengthException != null) {
				throw contentLengthException;
			}
			return new Long(contentLength);
		}
	}

	private class NtfsSecurityAnswer implements Answer<ACE[]> {
		@Override
		public ACE[] answer(InvocationOnMock invocation)
				throws Throwable {
			if (ntfsSecurityException != null) {
				throw ntfsSecurityException;
			}
			return ntfsSEcurity;
		}
	}

	private class IsFileAnswer implements Answer<Boolean> {
		@Override
		public Boolean answer(InvocationOnMock invocation)
				throws Throwable {
			if (isFileException != null) {
				throw isFileException;
			}
			return isFile;
		}
	}

	private class IsFolderAnswer implements Answer<Boolean> {
		@Override
		public Boolean answer(InvocationOnMock invocation)
				throws Throwable {
			if (isFolderException != null) {
				throw isFolderException;
			}
			return isFolder;
		}
	}

	private class ListFilesAnswer implements Answer<SmbFile[]> {
		@Override
		public SmbFile[] answer(InvocationOnMock invocation)
				throws Throwable {
			SmbFile childFile = Mockito.mock(SmbFile.class);
			when(childFile.getCanonicalPath()).thenReturn(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FILE);
			SmbFile childFolder = Mockito.mock(SmbFile.class);
			when(childFolder.getCanonicalPath()).thenReturn(SmbTestParams.EXISTING_SHARE + SmbTestParams.EXISTING_FOLDER);

			SmbFile file0 = Mockito.mock(SmbFile.class);
			when(file0.getCanonicalPath()).thenReturn(SmbTestParams.EXISTING_SHARE + "file0");
			SmbFile file01 = Mockito.mock(SmbFile.class);
			when(file01.getCanonicalPath()).thenReturn(SmbTestParams.EXISTING_SHARE + "file01");
			SmbFile file10 = Mockito.mock(SmbFile.class);
			when(file10.getCanonicalPath()).thenReturn(SmbTestParams.EXISTING_SHARE + "file10");
			SmbFile filea = Mockito.mock(SmbFile.class);
			when(filea.getCanonicalPath()).thenReturn(SmbTestParams.EXISTING_SHARE + "filea");
			SmbFile filez = Mockito.mock(SmbFile.class);
			when(filez.getCanonicalPath()).thenReturn(SmbTestParams.EXISTING_SHARE + "filez");

			SmbFile folder0 = Mockito.mock(SmbFile.class);
			when(folder0.getCanonicalPath()).thenReturn(SmbTestParams.EXISTING_SHARE + "folder0/");
			SmbFile folder01 = Mockito.mock(SmbFile.class);
			when(folder01.getCanonicalPath()).thenReturn(SmbTestParams.EXISTING_SHARE + "folder01/");
			SmbFile folder10 = Mockito.mock(SmbFile.class);
			when(folder10.getCanonicalPath()).thenReturn(SmbTestParams.EXISTING_SHARE + "folder10/");
			SmbFile foldera = Mockito.mock(SmbFile.class);
			when(foldera.getCanonicalPath()).thenReturn(SmbTestParams.EXISTING_SHARE + "foldera/");
			SmbFile folderz = Mockito.mock(SmbFile.class);
			when(folderz.getCanonicalPath()).thenReturn(SmbTestParams.EXISTING_SHARE + "folderz/");

			return new SmbFile[]{childFile, childFolder, file0, file01, file10, filea, filez, folder0, folder01, folder10, foldera, folderz};
		}
	}

	public Credentials getValidCredentials() {
		return validCredentials;
	}

	public Credentials getCredentialsWithInvalidDomain() {
		return credentialsWithInvalidDomain;
	}

	public Credentials getCredentialsWithInvalidUsername() {
		return credentialsWithInvalidUsername;
	}

	public Credentials getCredentialsWithInvalidPassword() {
		return credentialsWithInvalidPassword;
	}

	public SmbRetrievalConfiguration getFetchValidShare() {
		return fetchValidShare;
	}

	public SmbRetrievalConfiguration getFetchValidFolder() {
		return fetchValidFolder;
	}

	public SmbRetrievalConfiguration getFetchValidFile() {
		return fetchValidFile;
	}

	public String getValidShare() {
		return validShare;
	}

	public String getValidFolder() {
		return validFolder;
	}

	public String getValidFile() {
		return validFile;
	}

	public String getMalformedUrl() {
		return malformedUrl;
	}

	public SmbFile getValidSmbFile() {
		return validSmbFile;
	}

	public SmbFile getGoneSmbFile() {
		existsValue = false;
		return validSmbFile;
	}

	public SmbFile getUnknownSmbFile() {
		existsException = new Exception("Unknown Exception");
		return validSmbFile;
	}

	public SmbFile getSmbFileWithCanonicalPathException() {
		canonicalPathException = new Exception("Canonical Path Exception");
		return validSmbFile;
	}

	public SmbFile getSmbFileWithNtfsSecurityException() {
		ntfsSecurityException = new Exception("NTFS Security Exception");
		return validSmbFile;
	}

	public SmbFile getSmbFileWithBlankUrlAndIsFileException() {
		canonicalPathValue = "";
		isFileException = new Exception("Is File Exception");
		return validSmbFile;
	}

	public SmbFile getSmbFileWithIsFolderException() {
		isFile = false;
		isFolder = true;
		canonicalPathValue = "";
		isFolderException = new Exception("Is Folder Exception");
		return validSmbFile;
	}

	public SmbFile getSmbFileWithLastModifiedException() {
		lastModifiedException = new Exception("Last Modified Exception");
		return validSmbFile;
	}

	public WindowsPermissions getWindowsPermissions() {
		return windowsPermissions;
	}

	public SmbFile getSmbFileWithChildren() {
		return validSmbFile;
	}

}