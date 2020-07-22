package com.constellio.data.io.services.facades;

import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.io.services.facades.FileServiceRuntimeException.CannotCreateTemporaryFolder;
import com.constellio.data.io.services.facades.FileServiceRuntimeException.FileServiceRuntimeException_CannotReadFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;

public class FileService {

	private static String REPLACE_FILE_CONTENT_TEMP_FILE = "replaceFileContentTempFile";

	private File tempFolder;

	public FileService(File tempFolder) {
		this.tempFolder = tempFolder;

		if (this.tempFolder != null) {
			this.tempFolder.mkdirs();
		}
	}

	public void copyDirectory(File srcDir, File destDir)
			throws IOException {
		FileUtils.copyDirectory(srcDir, destDir);
	}

	public void copyDirectoryWithoutExpectableIOException(File srcDir, File destDir) {
		try {
			FileUtils.copyDirectory(srcDir, destDir);
		} catch (IOException e) {
			throw new FileServiceRuntimeException.CannotCopyFile(srcDir.getPath(), destDir.getPath(), e);
		}
	}

	public void copyFile(File srcFile, File destFile)
			throws IOException {
		FileUtils.copyFile(srcFile, destFile);
	}

	public void copyFileWithoutExpectableIOException(File srcFile, File destFile) {
		try {
			FileUtils.copyFile(srcFile, destFile);
		} catch (IOException e) {
			throw new FileServiceRuntimeException.CannotCopyFile(srcFile.getPath(), destFile.getPath(), e);
		}
	}

	public String readFileToString(File file)
			throws IOException {
		return FileUtils.readFileToString(file);
	}

	public String readFileToStringWithoutExpectableIOException(File file) {
		try {
			return FileUtils.readFileToString(file);
		} catch (IOException e) {
			throw new FileServiceRuntimeException_CannotReadFile(file.getPath(), e);
		}
	}

	public List<String> readFileToLinesWithoutExpectableIOException(File file) {
		try {
			return FileUtils.readLines(file);
		} catch (IOException e) {
			throw new FileServiceRuntimeException_CannotReadFile(file.getPath(), e);
		}
	}

	public void replaceFileContent(File file, String data)
			throws IOException {
		File tempFile = getAtomicWriteTempFileFor(file);
		FileUtils.writeStringToFile(tempFile, data, "UTF-8", false);
		//moveFile(tempFile, file);
		try {
			java.nio.file.Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE);

		} catch (java.nio.file.NoSuchFileException e) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				throw new RuntimeException(e1);
			}
			System.out.println(tempFile.getAbsolutePath() + "=>" + tempFile.exists());
			System.out.println(file.getAbsolutePath() + "=>" + file.exists());
			FileUtils.writeStringToFile(tempFile, data, "UTF-8", false);
			System.out.println(tempFile.getAbsolutePath() + "=>" + tempFile.exists());
			System.out.println(file.getAbsolutePath() + "=>" + file.exists());
			java.nio.file.Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE);
		}
	}

	public synchronized void appendFileContent(File file, String data)
			throws IOException {
		FileUtils.writeStringToFile(file, data, "UTF8", true);
	}

	public synchronized void appendFileContent(File file, String data, String encoding)
			throws IOException {
		FileUtils.writeStringToFile(file, data, encoding, true);
	}

	public void ensureWritePermissions(File file)
			throws IOException {
		if (!(file.canRead() && file.canWrite())) {
			throw new IOException();
		}
	}

	public Collection<File> listFiles(File directory, IOFileFilter fileFilter, IOFileFilter dirFilter) {
		return FileUtils.listFiles(directory, fileFilter, dirFilter);
	}

	public Collection<File> listRecursiveFiles(File directory) {
		return FileUtils.listFiles(directory, newAcceptAllFileFilter(), newAcceptAllFileFilter());
	}

	public Collection<File> listRecursiveFiles(File directory, IOFileFilter fileFilter) {
		return FileUtils.listFiles(directory, fileFilter, newAcceptAllFileFilter());
	}

	public Collection<File> listRecursiveFilesWithName(File directory, final String name) {
		return FileUtils.listFiles(directory, new IOFileFilter() {

			@Override
			public boolean accept(File dir, String fileName) {
				return fileName.equals(name);
			}

			@Override
			public boolean accept(File file) {
				return file.getName().equals(name);
			}
		}, newAcceptAllFileFilter());
	}

	private IOFileFilter newAcceptAllFileFilter() {
		return new IOFileFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return true;
			}

			@Override
			public boolean accept(File file) {
				return true;
			}
		};
	}

	public void deleteDirectory(File directory)
			throws IOException {
		FileUtils.deleteDirectory(directory);
	}

	public void deleteDirectoryWithoutExpectableIOException(File directory) {
		try {
			FileUtils.deleteDirectory(directory);
		} catch (IOException e) {
			throw new FileServiceRuntimeException.CannotDeleteFile(directory.getPath(), e);
		}
	}

	@Deprecated
	public File newTemporaryFolder(String resourceName) {
		File temporaryFolder = new File(tempFolder, "tmp" + System.nanoTime());
		temporaryFolder.mkdirs();
		if (!temporaryFolder.exists()) {
			throw new CannotCreateTemporaryFolder();
		}
		return temporaryFolder;
	}

	public String readStreamToStringWithoutExpectableIOException(InputStream inputStream) {
		try {
			return readStreamToString(inputStream);
		} catch (IOException e) {
			throw new FileServiceRuntimeException.CannotReadStreamToString(e);
		}
	}

	public String readStreamToString(InputStream inputStream)
			throws IOException {
		StringBuilder content = new StringBuilder();
		boolean first = true;
		for (String line : IOUtils.readLines(inputStream)) {
			if (!first) {
				content.append("\n");
			}
			first = false;
			content.append(line);
		}
		return content.toString();
	}

	public List<String> readStreamToLines(InputStream inputStream)
			throws IOException {
		return IOUtils.readLines(inputStream);
	}

	public void deleteQuietly(File file) {
		if (file != null) {
			OpenedResourcesWatcher.onClose(file);
		}
		FileUtils.deleteQuietly(file);
	}

	public File newTemporaryFile(String resourceName, String extension) {
		final String name = resourceName + "_" + UUIDV1Generator.newRandomId() + "." + extension;
		File file = new File(tempFolder, name) {
			@Override
			public String toString() {
				return name + "[" + getPath() + "]";
			}

		};
		OpenedResourcesWatcher.onOpen(file);
		return file;
	}

	public File newTemporaryFileWithoutGuid(String resourceName) {
		final String name = resourceName;
		File file = new File(tempFolder, name) {
			@Override
			public String toString() {
				return name + "[" + getPath() + "]";
			}

		};
		OpenedResourcesWatcher.onOpen(file);
		return file;
	}

	public File newTemporaryFile(String resourceName) {
		final String name = resourceName + "_" + UUIDV1Generator.newRandomId();
		File file = new File(tempFolder, name) {
			@Override
			public String toString() {
				return name + "[" + getPath() + "]";
			}

		};
		OpenedResourcesWatcher.onOpen(file);
		return file;
	}

	public void writeLinesToFile(File file, List<String> lines) {
		try {
			FileUtils.writeLines(file, lines);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public File getAtomicWriteTempFileFor(File file) {
		String filePath = file.getAbsoluteFile().getPath();
		int indexOfLastSlash = filePath.lastIndexOf(File.separator);

		String tempFilePath;

		if (indexOfLastSlash == -1) {
			tempFilePath = "." + filePath;
		} else {
			tempFilePath =
					filePath.substring(0, indexOfLastSlash) + File.separator + "." + filePath.substring(indexOfLastSlash + 1);
		}
		return new File(tempFilePath);
	}

	public void moveFolder(File src, File dest) {
		src.renameTo(dest);
	}

	public void moveFile(File src, File dest) {
		if (dest.exists() && !dest.delete()) {
			throw new FileServiceRuntimeException.CannotMoveFile(src.getAbsolutePath(), dest.getAbsolutePath(), null);
		}

		try {
			FileUtils.moveFile(src, dest);
		} catch (IOException e) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			try {
				FileUtils.moveFile(src, dest);
			} catch (IOException e2) {
				throw new FileServiceRuntimeException.CannotMoveFile(src.getAbsolutePath(), dest.getAbsolutePath(), e2);
			}
		}
	}
}