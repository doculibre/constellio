package com.constellio.app.modules.es.connectors.smb.testutils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

public class SmbTestCommandFactory {
	public static enum SmbTestCommandType {
		CREATE_FILE, CREATE_FOLDER, DELETE, UPDATE_FILE, POPULATE_MINIMAL_SHARE, CLEAN_SHARE
	}

	private NtlmPasswordAuthentication auth;

	public SmbTestCommandFactory(NtlmPasswordAuthentication auth) {
		this.auth = auth;
	}

	public SmbTestCommand get(SmbTestCommandType commandType, String url, String content) {
		switch (commandType) {
		case CREATE_FILE:
			return new CreateFileCommand(url, content);
		case CREATE_FOLDER:
			return new CreateFolderCommand(url);
		case DELETE:
			return new DeleteFileCommand(url);
		case UPDATE_FILE:
			return new UpdateFileCommand(url, content);
		case POPULATE_MINIMAL_SHARE:
			return new PopulateMinimalShareCommand(url);
		case CLEAN_SHARE:
			return new CleanShareCommand(url);
		}
		return null;
	}

	public class CreateFileCommand implements SmbTestCommand {
		private String url;
		private String newFileContent;

		public CreateFileCommand(String url, String fileContent) {
			this.url = url;
			this.newFileContent = fileContent;
		}

		@Override
		public void execute() {
			try {
				createFile(url, newFileContent, auth);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void createFile(String url, String content, NtlmPasswordAuthentication auth)
			throws IOException {
		SmbFile smbFile = new SmbFile(url, auth);
		SmbFileOutputStream sfos = new SmbFileOutputStream(smbFile);
		sfos.write(content.getBytes());
		sfos.flush();
		sfos.close();
		//smbFile.createNewFile();

//		OutputStreamWriter osw = new OutputStreamWriter(smbFile.getOutputStream());
//		osw.write(content);
//		osw.flush();
//		osw.close();
	}

	public class CreateFolderCommand implements SmbTestCommand {
		private String url;

		public CreateFolderCommand(String url) {
			this.url = url;
		}

		@Override
		public void execute() {
			try {
				createFolder(url, auth);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void createFolder(String url, NtlmPasswordAuthentication auth)
			throws IOException {
		SmbFile smbFile = new SmbFile(url, auth);
		smbFile.mkdir();
	}

	public class DeleteFileCommand implements SmbTestCommand {
		private String url;

		public DeleteFileCommand(String url) {
			this.url = url;
		}

		@Override
		public void execute() {
			try {
				deleteFile(url, auth);
			} catch (SmbException | MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void deleteFile(String url, NtlmPasswordAuthentication auth)
			throws SmbException, MalformedURLException {
		SmbFile smbFile = new SmbFile(url, auth);
		if (smbFile.exists()) {
			smbFile.delete();
		}
	}

	public class UpdateFileCommand implements SmbTestCommand {
		private String url;
		private String newContent;

		public UpdateFileCommand(String url, String newContent) {
			this.url = url;
			this.newContent = newContent;
		}

		@Override
		public void execute() {
			try {
				deleteFile(url, auth);
				createFile(url, newContent, auth);
			} catch (SmbException | MalformedURLException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public class CleanShareCommand implements SmbTestCommand {
		private String url;

		public CleanShareCommand(String url) {
			this.url = url;
		}

		@Override
		public void execute() {
			try {
				cleanShare(url, auth);
			} catch (SmbException e) {
				throw new RuntimeException(e);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void cleanShare(String share, NtlmPasswordAuthentication auth)
			throws SmbException, MalformedURLException {
		SmbFile shareSmbFile = new SmbFile(share, auth);
		for (SmbFile smbFile : shareSmbFile.listFiles()) {
			smbFile.delete();
		}
	}

	public class PopulateMinimalShareCommand implements SmbTestCommand {
		private String baseUrlAndShare;

		public PopulateMinimalShareCommand(String baseUrlAndShare) {
			this.baseUrlAndShare = baseUrlAndShare;
		}

		@Override
		public void execute() {
			try {
				cleanShare(baseUrlAndShare, auth);
				createFile(baseUrlAndShare + SmbTestParams.FILE_NAME, SmbTestParams.FILE_CONTENT, auth);
				createFolder(baseUrlAndShare + SmbTestParams.FOLDER_NAME, auth);
				createFile(baseUrlAndShare + SmbTestParams.FOLDER_NAME + SmbTestParams.ANOTHER_FILE_NAME, SmbTestParams.ANOTHER_FILE_CONTENT, auth);
			} catch (SmbException e) {
				throw new RuntimeException(e);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
