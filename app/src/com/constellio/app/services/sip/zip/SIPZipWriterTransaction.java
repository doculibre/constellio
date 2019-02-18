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
import java.util.HashMap;
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

	private IOServices ioServices;

	private SIPFileHasher sipFileHasher;

	private Map<String, String> computedHashesCache = new HashMap<>();

	public SIPZipWriterTransaction(File tempFolder, IOServices ioServices, SIPFileHasher sipFileHasher) {
		this.ioServices = ioServices;
		this.sipFileHasher = sipFileHasher;
		this.tempFolder = tempFolder;
	}

	public long length() {
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

	public Map<String, String> getComputedHashesCache() {
		return computedHashesCache;
	}

	public MetsContentFileReference moveFileToSIP(String zipFilePath, File file) throws IOException {

		File newTempFile = moveFileToSIPAsUnreferencedContentFile(zipFilePath, file);

		MetsContentFileReference reference = new MetsContentFileReference();
		reference.setSize(newTempFile.length());
		String hash = sipFileHasher.computeHash(file, zipFilePath);
		computedHashesCache.put(zipFilePath, hash);
		reference.setCheckSum(hash);
		reference.setCheckSumType(sipFileHasher.getFunctionName());
		reference.setPath(zipFilePath);

		files.put(zipFilePath, newTempFile);
		add(reference);
		return reference;

	}

	public File moveFileToSIPAsUnreferencedContentFile(String zipFilePath, File file) throws IOException {

		File tempFile = new File(tempFolder, zipFilePath.replace("/", File.separator));
		tempFile.getParentFile().mkdirs();

		file.renameTo(tempFile);
		files.put(zipFilePath, tempFile);
		return tempFile;
	}

	public MetsContentFileReference addContentFile(String zipFilePath, byte[] bytesArray) throws IOException {
		InputStream inputStream = new ByteArrayInputStream(bytesArray);
		try {
			return addContentFile(zipFilePath, inputStream);

		} finally {
			ioServices.closeQuietly(inputStream);
		}

	}

	public File addUnreferencedContentFile(String zipFilePath, byte[] bytesArray) throws IOException {
		InputStream inputStream = new ByteArrayInputStream(bytesArray);
		try {
			return addUnreferencedContentFile(zipFilePath, inputStream);

		} finally {
			ioServices.closeQuietly(inputStream);
		}

	}

	public MetsContentFileReference addContentFileFromVaultFile(String zipFilePath, File vaultFile)
			throws IOException {

		MetsContentFileReference reference = new MetsContentFileReference();
		reference.setSize(vaultFile.length());
		String hash = sipFileHasher.computeHash(vaultFile, zipFilePath);
		computedHashesCache.put(zipFilePath, hash);
		reference.setCheckSum(hash);
		reference.setCheckSumType(sipFileHasher.getFunctionName());
		reference.setPath(zipFilePath);

		files.put(zipFilePath, vaultFile);
		add(reference);
		return reference;

	}

	public MetsContentFileReference addContentFile(String zipFilePath, InputStream inputStream)
			throws IOException {
		File file = addUnreferencedContentFile(zipFilePath, inputStream);

		MetsContentFileReference reference = new MetsContentFileReference();
		reference.setSize(file.length());
		String hash = sipFileHasher.computeHash(file, zipFilePath);
		computedHashesCache.put(zipFilePath, hash);
		reference.setCheckSum(hash);
		reference.setCheckSumType(sipFileHasher.getFunctionName());
		reference.setPath(zipFilePath);

		files.put(zipFilePath, file);
		add(reference);
		return reference;

	}

	public File addUnreferencedContentFile(String zipFilePath, InputStream inputStream)
			throws IOException {
		File tempFile = new File(tempFolder, zipFilePath.replace("/", File.separator));
		tempFile.getParentFile().mkdirs();

		OutputStream outputStream = ioServices.newBufferedFileOutputStream(tempFile, WRITE_TEMP_FILE_STREAM_NAME);
		ioServices.copyAndClose(inputStream, outputStream);

		files.put(zipFilePath, tempFile);
		return tempFile;
	}

	public MetsContentFileReference addContentFile(String zipFilePath,
												   CloseableStreamFactory<InputStream> inputStreamFactory)
			throws IOException {
		InputStream inputStream = null;
		try {
			inputStream = inputStreamFactory.create(READ_INPUTSTREAM_STREAM_NAME);
			return addContentFile(zipFilePath, inputStream);

		} catch (IOException e) {
			throw new RuntimeException(e);

		} finally {
			ioServices.closeQuietly(inputStream);
		}

	}

	public File addUnreferencedContentFile(String zipFilePath,
										   CloseableStreamFactory<InputStream> inputStreamFactory)
			throws IOException {
		InputStream inputStream = null;
		try {
			inputStream = inputStreamFactory.create(READ_INPUTSTREAM_STREAM_NAME);
			return addUnreferencedContentFile(zipFilePath, inputStream);

		} catch (IOException e) {
			throw new RuntimeException(e);

		} finally {
			ioServices.closeQuietly(inputStream);
		}

	}

	public int filesCount() {
		return contentFileReferences.size() + eadMetadataReferences.size();
	}
}