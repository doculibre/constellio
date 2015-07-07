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
package com.constellio.data.dao.services.contents;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.contents.ContentDaoException.ContentDaoException_NoSuchContent;
import com.constellio.data.dao.services.contents.ContentDaoRuntimeException.ContentDaoRuntimeException_CannotDeleteFolder;
import com.constellio.data.dao.services.contents.ContentDaoRuntimeException.ContentDaoRuntimeException_CannotMoveFolderTo;
import com.constellio.data.dao.services.contents.ContentDaoRuntimeException.ContentDaoRuntimeException_NoSuchFolder;
import com.constellio.data.dao.services.contents.FileSystemContentDaoRuntimeException.FileSystemContentDaoRuntimeException_DatastoreFailure;
import com.constellio.data.io.services.facades.IOServices;

public class FileSystemContentDao implements StatefulService, ContentDao {

	private static final String COPY_RECEIVED_STREAM_TO_FILE = "FileSystemContentDao-CopyReceivedStreamToFile";

	IOServices ioServices;

	File rootFolder;

	public FileSystemContentDao(File rootFolder, IOServices ioServices) {
		this.rootFolder = rootFolder;
		this.ioServices = ioServices;
	}

	@Override
	public void initialize() {

	}

	@Override
	public void add(String newContentId, InputStream newInputStream) {

		synchronized (FileSystemContentDao.class) {
			File content = getFileOf(newContentId);
			content.getParentFile().mkdirs();

			OutputStream out = null;
			try {
				out = ioServices.newFileOutputStream(content, COPY_RECEIVED_STREAM_TO_FILE);
				IOUtils.copy(newInputStream, out);
			} catch (IOException e) {
				throw new FileSystemContentDaoRuntimeException_DatastoreFailure(e);
			} finally {
				ioServices.closeQuietly(out);
			}
		}

	}

	@Override
	public void delete(List<String> contentIds) {
		synchronized (FileSystemContentDao.class) {
			for (String contentId : contentIds) {
				File file = getFileOf(contentId);
				file.delete();
			}
		}
	}

	@Override
	public InputStream getContentInputStream(String contentId, String streamName)
			throws ContentDaoException_NoSuchContent {
		synchronized (FileSystemContentDao.class) {
			try {
				return new BufferedInputStream(ioServices.newFileInputStream(getFileOf(contentId), streamName));
			} catch (FileNotFoundException e) {
				throw new ContentDaoException_NoSuchContent(contentId);
			}
		}
	}

	@Override
	public boolean isFolderExisting(String folderId) {
		File folder = new File(rootFolder, folderId.replace("/", File.separator));
		return folder.exists();
	}

	@Override
	public List<String> getFolderContents(String folderId) {
		File folder = new File(rootFolder, folderId.replace("/", File.separator));
		String[] fileArray = folder.list();
		List<String> files = new ArrayList<>();

		if (fileArray != null) {
			for (String file : fileArray) {
				files.add(folderId + "/" + file);
			}
		}

		return files;
	}

	@Override
	public long getContentLength(String vaultContentId) {
		return getFileOf(vaultContentId).length();
	}

	@Override
	public void moveFolder(String folderId, String newFolderId) {
		File folder = getFolder(folderId);
		if (!folder.exists()) {
			throw new ContentDaoRuntimeException_NoSuchFolder(folderId);
		}
		File newfolder = getFolder(newFolderId);
		newfolder.mkdirs();
		newfolder.delete();
		try {
			FileUtils.moveDirectory(folder, newfolder);
		} catch (IOException e) {
			throw new ContentDaoRuntimeException_CannotMoveFolderTo(folderId, newFolderId, e);
		}

	}

	private File getFolder(String folderId) {
		return new File(rootFolder, folderId.replace("/", File.separator));
	}

	@Override
	public void deleteFolder(String folderId) {

		File folder = getFolder(folderId);
		if (folder.exists()) {
			try {
				ioServices.deleteDirectory(getFolder(folderId));
			} catch (IOException e) {
				throw new ContentDaoRuntimeException_CannotDeleteFolder(folderId, e);
			}
		}
	}

	private File getFileOf(String contentId) {
		if (contentId.contains("/")) {
			return new File(rootFolder, contentId.replace("/", File.separator));

		} else {
			String folderName = contentId.substring(0, 2);
			File folder = new File(rootFolder, folderName);
			return new File(folder, contentId);
		}

	}

	@Override
	public void close() {

	}
}