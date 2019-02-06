package com.constellio.app.modules.rm.services.sip.zip;

import com.constellio.app.modules.rm.services.sip.ConstellioSIP;
import com.constellio.app.modules.rm.services.sip.mets.MetsContentFileReference;
import com.constellio.app.modules.rm.services.sip.mets.MetsDivisionInfo;
import com.constellio.app.modules.rm.services.sip.mets.MetsEADMetadataReference;
import com.constellio.app.modules.rm.services.sip.mets.MetsFileWriter;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.TimeProvider;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	private String sipFileName;

	private ZipArchiveOutputStream zipOutputStream;

	private List<String> manifestLines = new ArrayList<String>();
	private List<String> tagManifestLines = new ArrayList<String>();

	private String metsFilename;

	private Date sipCreationTime;

	private List<MetsContentFileReference> contentFileReferences = new ArrayList<>();

	private List<MetsEADMetadataReference> eadMetadataReferences = new ArrayList<>();

	private Map<String, MetsDivisionInfo> divisionsInfoMap = new HashMap<>();

	private File zipFile;

	public SIPZipWriter(IOServices ioServices, File zipFile, String sipFileName,
						Map<String, MetsDivisionInfo> divisionsInfoMap) {
		this.zipFile = zipFile;
		this.ioServices = ioServices;
		this.sipFileName = sipFileName;
		this.divisionsInfoMap = divisionsInfoMap;
		this.sipCreationTime = TimeProvider.getLocalDateTime().toDate();

		OutputStream zipFileOutputStream = null;
		try {
			zipFileOutputStream = new FileOutputStream(zipFile);
		} catch (FileNotFoundException e) {
			//TODO
			throw new RuntimeException(e);
		}
		zipOutputStream = new ZipArchiveOutputStream(zipFileOutputStream);
		zipOutputStream.setUseZip64(Zip64Mode.AsNeeded);

		metsFilename = sipFileName + ".xml";
	}


	public void close() throws IOException {

		File metsFile = File.createTempFile(ConstellioSIP.class.getSimpleName(), metsFilename);
		metsFile.deleteOnExit();


		MetsFileWriter metsFileWriter = new MetsFileWriter(metsFile, metsFilename, sipCreationTime, divisionsInfoMap);

		metsFileWriter.build(eadMetadataReferences, contentFileReferences);

		String metsFileZipPath = "/" + metsFilename;
		addToZip(metsFile, metsFileZipPath);
		metsFile.delete();

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
		final File tempFile = ioServices.newTemporaryFile("SIPZipWriter-" + tempFileMpnitorName);

		Runnable fileClosingAction = new Runnable() {
			@Override
			public void run() {
				try {
					addToZip(tempFile, path);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				ioServices.deleteQuietly(tempFile);
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
		final File tempFile = ioServices.newTemporaryFile("SIPZipWriter-tempFile");

		Runnable fileClosingAction = new Runnable() {
			@Override
			public void run() {
				try {
					addToZip(tempFile, path);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				ioServices.deleteQuietly(tempFile);
			}
		};

		try {
			return ioServices.newBufferedFileWriterWithFileClosingAction(tempFile, "SIPZipWriter-writing " + tempFileMonitorName, fileClosingAction);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public void addToZip(SIPZipWriterTransaction transaction) throws IOException {
		for (Map.Entry<String, File> entry : transaction.getFiles().entrySet()) {
			addToZip(entry.getValue(), entry.getKey());
		}

		contentFileReferences.addAll(transaction.getContentFileReferences());
		eadMetadataReferences.addAll(transaction.getEadMetadataReferences());
	}

	public void addToZip(File file, String path) throws IOException {
		System.out.println("Adding " + path + " with length " + file.length());
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

	public boolean containsEADMetadatasOf(String id) {
		for (MetsEADMetadataReference reference : eadMetadataReferences) {
			if (id.equals(reference.getId())) {
				return true;
			}
		}
		return false;
	}

}
