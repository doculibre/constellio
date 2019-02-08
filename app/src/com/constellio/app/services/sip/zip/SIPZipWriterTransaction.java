package com.constellio.app.services.sip.zip;

import com.constellio.app.services.sip.mets.MetsContentFileReference;
import com.constellio.app.services.sip.mets.MetsEADMetadataReference;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.CloseableStreamFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SIPZipWriterTransaction {

	private static final String READ_INPUTSTREAM_STREAM_NAME = SIPZipWriterTransaction.class.getSimpleName() + "-ReadInputStream";
	private static final String WRITE_TEMP_FILE_STREAM_NAME = SIPZipWriterTransaction.class.getSimpleName() + "-WriteTempFile";

	File tempFolder;

	private LinkedHashMap<String, File> files = new LinkedHashMap<>();

	private List<MetsContentFileReference> contentFileReferences = new ArrayList<>();

	private List<MetsEADMetadataReference> eadMetadataReferences = new ArrayList<>();

	private SIPZipWriter writer;

	public SIPZipWriterTransaction(File tempFolder, SIPZipWriter writer) {
		this.writer = writer;
		this.tempFolder = tempFolder;
	}

	private long length() {
		long length = 0;

		for (File file : files.values()) {
			length += file.length();
		}

		return length;
	}

	public Map<String, File> getFiles() {
		return files;
	}

	public void add(MetsContentFileReference contentFileReference) {
		contentFileReferences.add(contentFileReference);
	}

	public void add(MetsEADMetadataReference eadMetadataReference) {
		eadMetadataReferences.add(eadMetadataReference);
	}

	public List<MetsContentFileReference> getContentFileReferences() {
		return contentFileReferences;
	}

	public List<MetsEADMetadataReference> getEadMetadataReferences() {
		return eadMetadataReferences;
	}

	public boolean containsEADMetadatasOf(String id) {
		for (MetsEADMetadataReference reference : eadMetadataReferences) {
			if (id.equals(reference.getId())) {
				return true;
			}
		}
		return false;
	}

	public MetsContentFileReference addContentFileFromFile(String zipFilePath, File file) throws IOException {
		IOServices ioServices = writer.getIoServices();
		InputStream inputStream = null;
		try {
			inputStream = ioServices.newBufferedFileInputStream(file, READ_INPUTSTREAM_STREAM_NAME);
			return addContentFileFromInputStream(zipFilePath, inputStream);

		} catch (IOException e) {
			throw new RuntimeException(e);

		} finally {
			ioServices.closeQuietly(inputStream);
		}
	}

	public void addUnreferencedContentFileFromFile(String zipFilePath, File file) throws IOException {
		IOServices ioServices = writer.getIoServices();
		InputStream inputStream = null;
		try {
			inputStream = ioServices.newBufferedFileInputStream(file, READ_INPUTSTREAM_STREAM_NAME);
			addUnreferencedContentFileFromInputStream(zipFilePath, inputStream);

		} catch (IOException e) {
			throw new RuntimeException(e);

		} finally {
			ioServices.closeQuietly(inputStream);
		}


	}

	public MetsContentFileReference addContentFileFromBytes(String zipFilePath, byte[] bytesArray) throws IOException {
		IOServices ioServices = writer.getIoServices();
		InputStream inputStream = new ByteArrayInputStream(bytesArray);
		try {
			return addContentFileFromInputStream(zipFilePath, inputStream);

		} finally {
			ioServices.closeQuietly(inputStream);
		}

	}

	public File addUnreferencedContentFileFromBytes(String zipFilePath, byte[] bytesArray) throws IOException {
		IOServices ioServices = writer.getIoServices();
		InputStream inputStream = new ByteArrayInputStream(bytesArray);
		try {
			return addUnreferencedContentFileFromInputStream(zipFilePath, inputStream);

		} finally {
			ioServices.closeQuietly(inputStream);
		}

	}

	public MetsContentFileReference addContentFileFromInputStream(String zipFilePath, InputStream inputStream)
			throws IOException {
		File file = addUnreferencedContentFileFromInputStream(zipFilePath, inputStream);

		MetsContentFileReference reference = new MetsContentFileReference();
		reference.setSize(file.length());
		reference.setCheckSum(writer.getSipFileHasher().computeHash(file, zipFilePath));
		reference.setCheckSumType(writer.getSipFileHasher().getFunctionName());
		reference.setPath(zipFilePath);

		files.put(zipFilePath, file);
		add(reference);
		return reference;

	}

	public File addUnreferencedContentFileFromInputStream(String zipFilePath, InputStream inputStream)
			throws IOException {
		IOServices ioServices = writer.getIoServices();
		File tempFile = new File(tempFolder, zipFilePath.replace("/", File.separator));
		tempFile.getParentFile().mkdirs();

		OutputStream outputStream = ioServices.newBufferedFileOutputStream(tempFile, WRITE_TEMP_FILE_STREAM_NAME);
		ioServices.copyAndClose(inputStream, outputStream);

		files.put(zipFilePath, tempFile);
		return tempFile;
	}

	public MetsContentFileReference addContentFileFromInputStream(String zipFilePath,
																  CloseableStreamFactory<InputStream> inputStreamFactory)
			throws IOException {
		IOServices ioServices = writer.getIoServices();
		InputStream inputStream = null;
		try {
			inputStream = inputStreamFactory.create(READ_INPUTSTREAM_STREAM_NAME);
			return addContentFileFromInputStream(zipFilePath, inputStream);

		} catch (IOException e) {
			throw new RuntimeException(e);

		} finally {
			ioServices.closeQuietly(inputStream);
		}

	}

	public File addUnreferencedContentFileFromInputStream(String zipFilePath,
														  CloseableStreamFactory<InputStream> inputStreamFactory)
			throws IOException {
		IOServices ioServices = writer.getIoServices();
		InputStream inputStream = null;
		try {
			inputStream = inputStreamFactory.create(READ_INPUTSTREAM_STREAM_NAME);
			return addUnreferencedContentFileFromInputStream(zipFilePath, inputStream);

		} catch (IOException e) {
			throw new RuntimeException(e);

		} finally {
			ioServices.closeQuietly(inputStream);
		}

	}
}