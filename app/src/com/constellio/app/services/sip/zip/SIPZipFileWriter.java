package com.constellio.app.services.sip.zip;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.sip.bagInfo.SIPZipBagInfoFactory;
import com.constellio.app.services.sip.bagInfo.SIPZipInfos;
import com.constellio.app.services.sip.mets.MetsContentFileReference;
import com.constellio.app.services.sip.mets.MetsDivisionInfo;
import com.constellio.app.services.sip.mets.MetsEADMetadataReference;
import com.constellio.app.services.sip.mets.MetsFileWriter;
import com.constellio.app.services.sip.zip.SIPZipWriterRuntimeException.SIPZipWriterRuntimeException_ErrorAddingToSIP;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.TimeProvider;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.io.FilenameUtils.getExtension;

public class SIPZipFileWriter implements SIPZipWriter {

	private static final String READ_FILE_STREAM_NAME = "SIPZipFileWriter-readFile";
	private static final String BAG_INFO_FILE_NAME = "bag-info.txt";

	private IOServices ioServices;

	private ZipArchiveOutputStream zipOutputStream;

	private List<String> manifestLines = new ArrayList<String>();
	private List<String> tagManifestLines = new ArrayList<String>();

	private List<MetsContentFileReference> contentFileReferences = new ArrayList<>();

	private List<MetsEADMetadataReference> eadMetadataReferences = new ArrayList<>();

	Map<String, MetsDivisionInfo> divisionsInfoMap = new HashMap<>();

	private SIPFileHasher sipFileHasher;

	private File zipFile;

	private SIPZipBagInfoFactory bagInfoFactory;

	private SIPZipInfos sipZipInfos;

	public SIPZipFileWriter(AppLayerFactory appLayerFactory, File zipFile, String sipFileName,
							SIPZipBagInfoFactory bagInfoFactory)
			throws IOException {
		this.ioServices = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newIOServices();
		this.sipZipInfos = new SIPZipInfos(sipFileName, TimeProvider.getLocalDateTime());
		this.sipFileHasher = new SIPFileHasher();
		this.zipFile = zipFile;
		this.bagInfoFactory = bagInfoFactory;

		FileUtils.touch(zipFile);
		OutputStream zipFileOutputStream = new FileOutputStream(zipFile);
		zipOutputStream = new ZipArchiveOutputStream(zipFileOutputStream);
		zipOutputStream.setUseZip64(Zip64Mode.AsNeeded);


	}

	public void setSipFileHasher(SIPFileHasher sipFileHasher) {
		this.sipFileHasher = sipFileHasher;
	}

	public Map<String, MetsDivisionInfo> getDivisionsInfoMap() {
		return divisionsInfoMap;
	}

	public void addDivisionsInfoMap(
			Map<String, MetsDivisionInfo> divisionsInfoMap) {
		this.divisionsInfoMap.putAll(divisionsInfoMap);
	}

	public SIPFileHasher getSipFileHasher() {
		return sipFileHasher;
	}

	public IOServices getIoServices() {
		return ioServices;
	}

	public void close() {

		try {
			addBagInfoFile();
			addMetsFile();
			addManifestFiles();

		} finally {
			IOUtils.closeQuietly(zipOutputStream);
		}

	}

	private void addBagInfoFile() {

		BufferedWriter bufferedWriter = newZipFileWriter("/" + BAG_INFO_FILE_NAME);
		try {
			IOUtils.write(bagInfoFactory.buildBagInfoContent(sipZipInfos), bufferedWriter);

		} catch (IOException e) {
			throw new SIPZipWriterRuntimeException_ErrorAddingToSIP(BAG_INFO_FILE_NAME, e);

		} finally {
			ioServices.closeQuietly(bufferedWriter);
		}
	}

	protected void addMetsFile() {
		String metsFilename = sipZipInfos.getSipName() + ".xml";
		MetsFileWriter metsFileWriter = new MetsFileWriter(ioServices, newZipFileOutputStream("/" + metsFilename),
				metsFilename, sipZipInfos.getCreationTime().toDate(), divisionsInfoMap);
		try {
			metsFileWriter.write(eadMetadataReferences, contentFileReferences);

		} finally {
			metsFileWriter.close();
		}

	}

	protected void addManifestFiles() {
		String hashingType = sipFileHasher.getFunctionName().toLowerCase().replace("-", "");
		String manigestFilename = "/" + "manifest-" + hashingType + ".txt";
		BufferedWriter manifestWriter = newZipFileWriter(manigestFilename);
		try {
			IOUtils.writeLines(manifestLines, "\n", manifestWriter);

		} catch (IOException e) {
			throw new SIPZipWriterRuntimeException_ErrorAddingToSIP(manigestFilename, e);

		} finally {
			IOUtils.closeQuietly(manifestWriter);
		}

		String tagManifestFilename = "/" + "tagmanifest-" + hashingType + ".txt";
		BufferedWriter tagManifestWriter = newZipFileWriter(tagManifestFilename);
		try {
			IOUtils.writeLines(tagManifestLines, "\n", tagManifestWriter);

		} catch (IOException e) {
			throw new SIPZipWriterRuntimeException_ErrorAddingToSIP(tagManifestFilename, e);

		} finally {
			IOUtils.closeQuietly(tagManifestWriter);
		}
	}


	public OutputStream newZipFileOutputStream(final String path) {
		String tempFileMpnitorName = "temp file '" + path + "' in sip file '" + sipZipInfos.getSipName() + "'";
		final File tempFile = ioServices.newTemporaryFile("SIPZipWriter-" + tempFileMpnitorName);
		try {
			FileUtils.touch(tempFile);
		} catch (IOException e) {
			throw new SIPZipWriterRuntimeException_ErrorAddingToSIP(path, e);
		}
		Runnable fileClosingAction = new Runnable() {
			@Override
			public void run() {
				try {
					addToZip(tempFile, path);
				} catch (IOException e) {
					throw new SIPZipWriterRuntimeException_ErrorAddingToSIP(path, e);
				}
				ioServices.deleteQuietly(tempFile);
			}
		};

		try {
			return ioServices.newBufferedFileOutputStreamWithFileClosingAction(tempFile, "SIPZipWriter-writing " + tempFileMpnitorName, fileClosingAction);
		} catch (FileNotFoundException e) {
			throw new SIPZipWriterRuntimeException_ErrorAddingToSIP(path, e);
		}

	}


	public BufferedWriter newZipFileWriter(final String path) {
		String tempFileMonitorName = "temp file '" + path + "' in sip file '" + sipZipInfos.getSipName() + "'";
		final File tempFile = ioServices.newTemporaryFile("SIPZipWriter-tempFile");

		Runnable fileClosingAction = new Runnable() {
			@Override
			public void run() {
				try {
					addToZip(tempFile, path);
				} catch (IOException e) {
					throw new SIPZipWriterRuntimeException_ErrorAddingToSIP(path, e);
				}
				ioServices.deleteQuietly(tempFile);
			}
		};

		try {
			return ioServices.newBufferedFileWriterWithFileClosingAction(tempFile, "SIPZipWriter-writing " + tempFileMonitorName, fileClosingAction);
		} catch (IOException e) {
			throw new SIPZipWriterRuntimeException_ErrorAddingToSIP(path, e);
		}

	}

	public void insertAll(SIPZipWriterTransaction transaction) throws IOException {
		for (Map.Entry<String, File> entry : transaction.getFiles().entrySet()) {
			addFileWithoutFlushing(entry.getValue(), entry.getKey());
		}
		zipOutputStream.flush();

		contentFileReferences.addAll(transaction.getContentFileReferences());
		eadMetadataReferences.addAll(transaction.getEadMetadataReferences());
		ioServices.deleteQuietly(transaction.tempFolder);
	}

	public void addToZip(File file, String path) throws IOException {
		addFileWithoutFlushing(file, path);
		zipOutputStream.flush();
	}

	protected void addFileWithoutFlushing(File file, String path) throws IOException {
		String hash = sipFileHasher.computeHash(file, path);
		String pathWithoutSlash = path.startsWith("/") ? path.substring(1) : path;

		sipZipInfos.logFile(getExtension(path), file.length());

		ArchiveEntry entry = zipOutputStream.createArchiveEntry(file, pathWithoutSlash);
		zipOutputStream.putArchiveEntry(entry);
		InputStream inputStream = ioServices.newFileInputStream(file, READ_FILE_STREAM_NAME);
		try {
			ioServices.copy(inputStream, zipOutputStream);

		} finally {
			ioServices.closeQuietly(inputStream);
		}
		zipOutputStream.closeArchiveEntry();


		if (pathWithoutSlash.contains("/")) {
			manifestLines.add(hash + " " + pathWithoutSlash);
		} else {
			tagManifestLines.add(hash + " " + pathWithoutSlash);
		}
	}

	public SIPZipWriterTransaction newInsertTransaction() {
		return new SIPZipWriterTransaction(ioServices.newTemporaryFolder("ConstellioSIP-transaction"), ioServices, sipFileHasher);
	}

	public void discard(SIPZipWriterTransaction transaction) {
		ioServices.deleteQuietly(transaction.tempFolder);
	}

	@Override
	public long length() {
		return zipFile.length() + contentFileReferences.size() * 75L + eadMetadataReferences.size() * 25L;
	}
}
