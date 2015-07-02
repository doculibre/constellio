/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.data.io.services.facades;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import com.constellio.data.io.services.facades.FileServiceRuntimeException.CannotCreateTemporaryFolder;
import com.constellio.data.io.services.facades.FileServiceRuntimeException.FileServiceRuntimeException_CannotReadFile;

public class FileService {

	private File tempFolder;

	public FileService(File tempFolder) {
		this.tempFolder = tempFolder;
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
		FileUtils.writeStringToFile(file, data, false);
	}

	public synchronized void appendFileContent(File file, String data)
			throws IOException {
		FileUtils.writeStringToFile(file, data, true);
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

	public File newTemporaryFile(String resourceName) {
		final String name = resourceName + "_" + UUID.randomUUID().toString();
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
}