package com.constellio.app.modules.rm.services.sip.zip;

import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.facades.IOServices;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public abstract class SIPZipWriter {

	//TODO
	public long sipFilesLength;
	public int sipFilesCount;

	private static final long SIP_MAX_FILES_LENGTH = (6 * FileUtils.ONE_GB);

	private static final int SIP_MAX_FILES = 9000;

	private static final char[] RESERVED_PATH_CHARS = {
			';',
			'/',
			'\\',
			'?',
			':',
			'@',
			'&',
			'=',
			'+',
			'$',
			',',
			'{',
			'}',
			'|',
			'^',
			'[',
			']',
			};

	private static List<String> METS_XSDs = asList("xlink.xsd", "mets.xsd");

	private static final String BAG_INFO_FILE_NAME = "bag-info.txt";

	private static final String HASH_TYPE = "sha256";

	private static final String TAGMANIFEST_FILE_NAME = "tagmanifest-" + HASH_TYPE + ".txt";

	private static final String MANIFEST_FILE_NAME = "manifest-" + HASH_TYPE + ".txt";

	private IOServices ioServices;

	private FileService fileService;

	private String sipFileName;

	private ZipArchiveOutputStream zipOutputStream;

	private List<String> manifestLines = new ArrayList<String>();
	private List<String> tagManifestLines = new ArrayList<String>();

	public SIPZipWriter(IOServicesFactory ioServicesFactory, File zipFile, String sipFileName) {
		this.ioServices = ioServicesFactory.newIOServices();
		this.fileService = ioServicesFactory.newFileService();
		this.sipFileName = sipFileName;

		OutputStream zipFileOutputStream = null;
		try {
			zipFileOutputStream = new FileOutputStream(zipFile);
		} catch (FileNotFoundException e) {
			//TODO
			throw new RuntimeException(e);
		}
		zipOutputStream = new ZipArchiveOutputStream(zipFileOutputStream);
		zipOutputStream.setUseZip64(Zip64Mode.AsNeeded);
	}

	public void close() throws IOException {

		//TODO Improve stream safety

		BufferedWriter manifestWriter = newZipFileWriter("/" + MANIFEST_FILE_NAME);
		IOUtils.writeLines(manifestLines, "\n", manifestWriter);
		IOUtils.closeQuietly(manifestWriter);

		BufferedWriter tagManifestWriter = newZipFileWriter("/" + TAGMANIFEST_FILE_NAME);
		IOUtils.writeLines(tagManifestLines, "\n", tagManifestWriter);
		IOUtils.closeQuietly(tagManifestWriter);

		IOUtils.closeQuietly(zipOutputStream);
	}


	public OutputStream newZipFileOutputStream(final String path) {
		String tempFileMpnitorName = "temp file '" + path + "' in sip file '" + sipFileName + "'";
		final File tempFile = fileService.newTemporaryFile("SIPZipWriter-" + tempFileMpnitorName);

		Runnable fileClosingAction = new Runnable() {
			@Override
			public void run() {
				try {
					addToZip(tempFile, path);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				fileService.deleteQuietly(tempFile);
			}
		};

		try {
			return ioServices.newBufferedFileOutputStreamWithFileClosingAction(tempFile, "SIPZipWriter-writing " + tempFileMpnitorName, fileClosingAction);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}

	}


	public BufferedWriter newZipFileWriter(final String path) {
		String tempFileMonitorName = "temp file '" + path + "' in sip file '" + sipFileName + "'";
		final File tempFile = fileService.newTemporaryFile("SIPZipWriter-tempFile");

		Runnable fileClosingAction = new Runnable() {
			@Override
			public void run() {
				try {
					addToZip(tempFile, path);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				fileService.deleteQuietly(tempFile);
			}
		};

		try {
			return ioServices.newBufferedFileWriterWithFileClosingAction(tempFile, "SIPZipWriter-writing " + tempFileMonitorName, fileClosingAction);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public void addToZip(File file, String path) throws IOException {
		String hash = computeHashOfFile(file, path);
		if (path.startsWith("/")) {
			path = path.substring(1);
		}

		sipFilesLength += file.length();
		sipFilesCount++;

		ArchiveEntry entry = zipOutputStream.createArchiveEntry(file, path);
		zipOutputStream.putArchiveEntry(entry);
		InputStream fis = new FileInputStream(file);
		IOUtils.copy(fis, zipOutputStream);
		fis.close();
		zipOutputStream.closeArchiveEntry();


		String pathWithoutSlash = path.startsWith("/") ? path.substring(1) : path;
		if (pathWithoutSlash.contains("/")) {
			manifestLines.add(hash + " " + pathWithoutSlash);
		} else {
			//Root file
			tagManifestLines.add(hash + " " + pathWithoutSlash);
		}

	}

	protected abstract String computeHashOfFile(File file, String filePath)
			throws IOException;
}
