package com.constellio.app.ui.framework.components.fields.download;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;

public class TempFileDownload implements Serializable {

	private String fileName;

	private String mimeType;

	private long length;

	private File tempFile;

	private URL fileURL;

	private URLConnection con;

	public TempFileDownload(URL url) throws InvalidWarUrlException, IOException {
		super();
		this.fileURL = url;

		URLConnection con;

		try {
			con = fileURL.openConnection();
		} catch (IOException ex) {
			throw new InvalidWarUrlException(
					String.format("Connexion impossible vers <%s>.", url.toString())
			);
		}

		if (con.getHeaderFields().containsKey("Content-Length")) {

			if (con.getHeaderFields().get("Content-Length").size() > 0) {

				long fileSize = Long.parseLong(con.getHeaderFields().get("Content-Length").get(0));

				if (fileSize <= 0) {
					throw new InvalidWarUrlException(
							String.format("Taille du war égale ou inférieur à zéro. %d " +
										  "(Si égale à -1, elle est indisponible.)", fileSize)
					);
				} else {
					fileName = fileURL.getFile().substring(1); //substring(1) pour retirer le / au debut
					mimeType = con.getContentType();
					length = fileSize;
					tempFile = createFile();
					this.con = con;

					return;
				}
			}
		}

		throw new InvalidWarUrlException("Unexpected error during war download.");
	}

	public TempFileDownload(String fileName, String mimeType, long length, File file) {

		this.fileName = fileName;

		this.mimeType = mimeType;

		this.length = length;

		this.tempFile = file;

		this.fileURL = null;

		this.con = null;
	}

	public final URLConnection getCon() {
		return this.con;
	}

	public final String getFilePath() {
		return fileName;
	}

	public final String getFileName() {
		String[] strList = fileName.split("/");
		String fileName = strList[strList.length - 1];
		return fileName;
	}

	public final String getMimeType() {
		return mimeType;
	}

	public final long getLength() {
		return length;
	}

	public final File getTempFile() {
		return tempFile;
	}

	public void delete() {
		FileUtils.deleteQuietly(tempFile);
	}

	private File createFile() throws IOException {
		final String tempFileName = "download_tmpfile_" + System.currentTimeMillis();
		return File.createTempFile(tempFileName, null);
	}

}