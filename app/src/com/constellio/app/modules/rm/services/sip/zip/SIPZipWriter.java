package com.constellio.app.modules.rm.services.sip.zip;

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

public class SIPZipWriter {

	//TODO
	public long sipFilesLength;
	public int sipFilesCount;

	private IOServices ioServices;

	private String sipFileName;

	private ZipArchiveOutputStream zipOutputStream;

	private List<String> manifestLines = new ArrayList<String>();
	private List<String> tagManifestLines = new ArrayList<String>();

	private Date sipCreationTime;

	private List<MetsContentFileReference> contentFileReferences = new ArrayList<>();

	private List<MetsEADMetadataReference> eadMetadataReferences = new ArrayList<>();

	private Map<String, MetsDivisionInfo> divisionsInfoMap = new HashMap<>();

	private SIPFileHasher sipFileHasher;

	public SIPZipWriter(IOServices ioServices, SIPFileHasher sipFileHasher, File zipFile, String sipFileName,
						Map<String, MetsDivisionInfo> divisionsInfoMap) throws FileNotFoundException {
		this.sipFileHasher = sipFileHasher;
		this.ioServices = ioServices;
		this.sipFileName = sipFileName;
		this.divisionsInfoMap = divisionsInfoMap;
		this.sipCreationTime = TimeProvider.getLocalDateTime().toDate();

		OutputStream zipFileOutputStream = new FileOutputStream(zipFile);
		zipOutputStream = new ZipArchiveOutputStream(zipFileOutputStream);
		zipOutputStream.setUseZip64(Zip64Mode.AsNeeded);


	}


	public void close() throws IOException {

		try {
			addMetsFile();
			addManifestFiles();

		} finally {
			IOUtils.closeQuietly(zipOutputStream);
		}

	}

	protected void addMetsFile() {
		String metsFilename = sipFileName + ".xml";
		MetsFileWriter metsFileWriter = new MetsFileWriter(ioServices, newZipFileOutputStream("/" + metsFilename),
				metsFilename, sipCreationTime, divisionsInfoMap);
		try {
			metsFileWriter.write(eadMetadataReferences, contentFileReferences);

		} finally {
			metsFileWriter.close();
		}
	}

	protected void addManifestFiles() throws IOException {
		String hashingType = sipFileHasher.getFunctionName().toLowerCase().replace("-", "");
		BufferedWriter manifestWriter = newZipFileWriter("/" + "manifest-" + hashingType + ".txt");
		try {
			IOUtils.writeLines(manifestLines, "\n", manifestWriter);
		} finally {
			IOUtils.closeQuietly(manifestWriter);
		}

		BufferedWriter tagManifestWriter = newZipFileWriter("/" + "tagmanifest-" + hashingType + ".txt");
		try {
			IOUtils.writeLines(tagManifestLines, "\n", tagManifestWriter);
		} finally {
			IOUtils.closeQuietly(tagManifestWriter);
		}
	}


	public OutputStream newZipFileOutputStream(final String path) {
		String tempFileMpnitorName = "temp file '" + path + "' in sip file '" + sipFileName + "'";
		final File tempFile = ioServices.newTemporaryFile("SIPZipWriter-" + tempFileMpnitorName);
		try {
			FileUtils.touch(tempFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
		String hash = sipFileHasher.computeHash(file, path);
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


	public boolean containsEADMetadatasOf(String id) {
		for (MetsEADMetadataReference reference : eadMetadataReferences) {
			if (id.equals(reference.getId())) {
				return true;
			}
		}
		return false;
	}

}
