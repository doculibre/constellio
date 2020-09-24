package com.constellio.data.io.services.zip;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipServiceException.CannotAddFileToZipException;
import com.constellio.data.io.services.zip.ZipServiceException.CannotCreateZipOutputStreamException;
import com.constellio.data.io.services.zip.ZipServiceException.FileToZipNotFound;
import com.constellio.data.io.services.zip.ZipServiceException.ZipFileCannotBeParsed;
import com.constellio.data.io.services.zip.ZipServiceException.ZipFileInvalidExtension;
import com.constellio.data.io.services.zip.ZipServiceException.ZippedFilesInDifferentParentFolder;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipService {

	private static final String WRITE_CONTENT_TO_ZIP_FILE = "ZipService-WriteContentToZipFile";
	private static final String UNZIP_FILE_ENTRY_TO_FILE = "ZipService-UnzipFileEntryToFile";
	private static final String ZIP_FILE_STREAM = "ZipService-ZipFileStream";

	private static final String[] VALID_ZIP_EXTENSIONS = {".zip", ".war", ".jar"};

	private final IOServices ioServices;

	public ZipService(IOServices ioServices) {
		this.ioServices = ioServices;
	}


	public void zip(File zipFile, List<File> zippedFiles)
			throws ZipServiceException {

		validateZipFileExtension(zipFile);

		validateFilesInSameFolder(zippedFiles);

		String parent = zippedFiles.get(0).getParentFile().getAbsolutePath();

		ZipOutputStream zipOutputStream;
		try {
			zipOutputStream = newZipOutputStream(zipFile);
		} catch (FileNotFoundException e) {
			throw new ZipServiceException.ZipFileNotFound(zipFile, e);
		}

		try {
			addFilestoZip(zippedFiles, zipOutputStream, parent);

		} catch (CannotCreateZipOutputStreamException e) {
			throw new CannotCreateZipOutputStreamException(zipFile, e);

		} catch (IOException e) {
			throw new ZipServiceRuntimeException(e);

		} finally {
			ioServices.closeQuietly(zipOutputStream);
		}
	}

	private ZipOutputStream newZipOutputStream(File zipFile)
			throws FileNotFoundException {
		ZipOutputStream zipOutputStream;
		OutputStream zipFileOutputStream = ioServices.newFileOutputStream(new File(zipFile.getPath()), WRITE_CONTENT_TO_ZIP_FILE);
		zipOutputStream = new ZipOutputStream(zipFileOutputStream);
		return zipOutputStream;
	}

	public void unzip(File zipFile, File zipFileContentDestinationDir)
			throws ZipServiceException {
		prevalidateZipFile(zipFile);
		ZipFile zipFileObject = toZipFileObject(zipFile);
		unzip(zipFile, zipFileObject, zipFileContentDestinationDir);
		try {
			zipFileObject.close();
		} catch (IOException e) {
			// This exception is thrown if the zip api is not used correctly
			throw new ZipServiceRuntimeException.CannotUnzip(zipFile.getPath(), zipFileContentDestinationDir.getPath(), e);
		}
	}

	public int size(File zipFile)
			throws ZipServiceException {
		ZipFile zippedFile = toZipFileObject(zipFile);
		try {
			return zippedFile.size();
		} finally {
			ioServices.closeQuietly(zippedFile);
		}
	}

	public boolean contains(File zipFile, String filePath)
			throws ZipServiceException {
		ZipFile zippedFile = toZipFileObject(zipFile);
		try {
			return zippedFile.getEntry(getRelativePath(filePath, zipFile.getParent())) != null;
		} finally {
			ioServices.closeQuietly(zippedFile);
		}
	}

	private void unzip(File zipFile, ZipFile zipFileObject, File zipFileContentDestinationDir)
			throws ZipServiceException {
		Enumeration<? extends ZipEntry> zipEntryEnum = zipFileObject.entries();
		while (zipEntryEnum.hasMoreElements()) {
			ZipEntry zipFileEntry = zipEntryEnum.nextElement();
			try {
				unzip(zipFileObject, zipFileEntry, zipFileContentDestinationDir);
			} catch (IOException e) {
				throw new ZipServiceException.ZipFileCorrupted(zipFile, zipFileEntry.getName(), e);
			}
		}
	}

	private void unzip(ZipFile zipFile, ZipEntry entry, File zipFileContentDestinationDir)
			throws IOException {
		if (entry.isDirectory() || entry.getName().endsWith("\\")) {
			new File(zipFileContentDestinationDir, entry.getName()).mkdirs();
		} else {
			File zipFileItemDestination = new File(zipFileContentDestinationDir, entry.getName().replace("\\", "/"));

			zipFileItemDestination.getParentFile().mkdirs();

			InputStream is = null;
			OutputStream os = null;
			try {
				is = zipFile.getInputStream(entry);
				os = ioServices.newFileOutputStream(zipFileItemDestination, UNZIP_FILE_ENTRY_TO_FILE);
				IOUtils.copy(is, os);
			} finally {

				ioServices.closeQuietly(is);
				ioServices.closeQuietly(os);
			}
		}
	}

	private ZipFile toZipFileObject(File zipFile)
			throws ZipFileCannotBeParsed {
		try {
			return new ZipFile(zipFile);
		} catch (ZipException e) {
			throw new ZipServiceException.ZipFileCannotBeParsed(zipFile, e);
		} catch (IOException e) {
			throw new ZipServiceException.ZipFileCannotBeParsed(zipFile, e);
		}
	}

	private void prevalidateZipFile(File zipFile)
			throws ZipServiceException {
		if (!zipFile.exists()) {
			throw new ZipServiceException.ZipFileNotFound(zipFile, null);
		}

		if (zipFile.length() == 0) {
			throw new ZipServiceException.ZipFileHasNoContent(zipFile);
		}
	}

	private void validateZipFileExtension(File zipFile)
			throws ZipFileInvalidExtension {
		String fileName = zipFile.getName();
		String fileExtension = null;
		if (fileName.contains(".")) {
			fileExtension = fileName.substring(fileName.lastIndexOf("."));
		}

		if (fileExtension == null || !Arrays.asList(VALID_ZIP_EXTENSIONS).contains(fileExtension)) {
			throw new ZipServiceException.ZipFileInvalidExtension(fileExtension);
		}

	}

	private void validateFilesInSameFolder(List<File> zippedFiles)
			throws ZippedFilesInDifferentParentFolder, FileToZipNotFound {

		File commonParentFile = null;
		for (File zippedFile : zippedFiles) {
			if (!zippedFile.exists()) {
				throw new ZipServiceException.FileToZipNotFound(zippedFile);
			}
			if (commonParentFile == null) {
				commonParentFile = zippedFile.getParentFile();
			} else if (!commonParentFile.getAbsolutePath().equals(zippedFile.getParentFile().getAbsolutePath())) {
				throw new ZipServiceException.ZippedFilesInDifferentParentFolder(commonParentFile, zippedFile.getParentFile());
			}
		}
	}

	private void addFilestoZip(List<File> zippedFiles, ZipOutputStream zipOutputStream, String parent)
			throws IOException, ZipServiceException {
		for (File zippedFile : zippedFiles) {
			addFileToZip(zipOutputStream, parent, zippedFile);
		}
	}

	private void addFileToZip(ZipOutputStream zipOutputStream, String parent, File zippedFile)
			throws IOException, ZipServiceException {
		try {
			if (zippedFile.isDirectory()) {
				File[] children = zippedFile.listFiles();
				if (children.length == 0) {
					createEmptyDirectoryInZip(zippedFile, zipOutputStream, parent);
				} else {
					addFilestoZip(Arrays.asList(children), zipOutputStream, parent);
				}
			} else {
				copyInputStreamToOutputStream(zippedFile, zipOutputStream, parent);
			}
		} catch (CannotAddFileToZipException e) {
			throw new CannotAddFileToZipException(zippedFile, e);
		}
	}

	protected void createEmptyDirectoryInZip(File zippedFile, ZipOutputStream zipOutputStream, String parent)
			throws ZipServiceException, IOException {
		prevalidateFile(zippedFile);

		try {
			zipOutputStream.putNextEntry(new ZipEntry(getRelativePath(zippedFile.getAbsolutePath(), parent)));

		} catch (IOException e) {
			throw new ZipServiceException.CannotAddFileToZipException(zippedFile, e);

		} finally {
			zipOutputStream.closeEntry();
		}
	}

	private void copyInputStreamToOutputStream(File zippedFile, ZipOutputStream zipOutputStream, String parent)
			throws IOException, ZipServiceException {

		prevalidateFile(zippedFile);

		InputStream fileInputStream = null;
		try {
			fileInputStream = ioServices.newFileInputStream(zippedFile, ZIP_FILE_STREAM);
			zipOutputStream.putNextEntry(new ZipEntry(getRelativePath(zippedFile.getAbsolutePath(), parent)));

			ioServices.copy(fileInputStream, zipOutputStream);

		} catch (IOException e) {
			throw new ZipServiceException.CannotAddFileToZipException(zippedFile, e);

		} finally {
			zipOutputStream.closeEntry();
			ioServices.closeQuietly(fileInputStream);
		}
	}

	private void prevalidateFile(File file)
			throws ZipServiceException {
		if (!file.exists()) {
			throw new ZipServiceException.FileToZipNotFound(file);
		}
	}

	private String getRelativePath(String filePath, String startPath) {
		String relativePath = filePath.replace(startPath + File.separator, "");
		String relativePathWithForcedForwardSlashes = relativePath.replace("\\", "/");

		return relativePathWithForcedForwardSlashes;
	}

}
