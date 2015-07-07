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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.contents.ContentDaoException.ContentDaoException_NoSuchContent;
import com.constellio.data.dao.services.contents.HadoopContentDaoRuntimeException.HadoopContentDaoRuntimeException_DatastoreFailure;

public class HadoopContentDao implements StatefulService, ContentDao {
	private Configuration hadoopConfig;
	private FileSystem hdfs;
	private String hadoopUrl;
	private String hadoopUser;

	public HadoopContentDao(String hadoopUrl, String hadoopUser) {
		this.hadoopUrl = hadoopUrl;
		this.hadoopUser = hadoopUser;
	}

	@Override
	public void initialize() {
		System.setProperty("HADOOP_USER_NAME", hadoopUser);
		this.hadoopConfig = new Configuration();

		if (hadoopUrl == null || hadoopUser == null) {
			throw new RuntimeException("No config");
		}

		this.hadoopConfig.set("fs.defaultFS", hadoopUrl);
		this.hadoopConfig.set("hadoop.job.ugi", hadoopUser);

		try {
			hdfs = FileSystem.get(hadoopConfig);
		} catch (IOException e) {
			throw new HadoopContentDaoRuntimeException_DatastoreFailure(e);
		}
	}

	@Override
	public void add(String newContentId, InputStream newInputStream) {
		try {
			newContentId = formatIDToHadoop(newContentId);
			FSDataOutputStream out = hdfs.create(new Path(newContentId));

			byte[] b = new byte[1024];
			int numBytes;
			while ((numBytes = newInputStream.read(b)) > 0) {
				out.write(b, 0, numBytes);
			}

			out.flush();
			out.close();
		} catch (IOException e) {
			throw new HadoopContentDaoRuntimeException_DatastoreFailure(e);
		}

	}

	@Override
	public void delete(List<String> hashes) {
		try {
			for (String hash : hashes) {
				String hadoopHash = formatIDToHadoop(hash);
				hdfs.delete(new Path(hadoopHash), true);
			}

		} catch (IOException e) {
		}
	}

	@Override
	public InputStream getContentInputStream(String contentId, String streamName)
			throws ContentDaoException_NoSuchContent {
		try {
			contentId = formatIDToHadoop(contentId);
			FileSystem hdfs = FileSystem.get(hadoopConfig);
			FSDataInputStream hadoopIn = hdfs.open(new Path(contentId));

			return hadoopIn.getWrappedStream();
		} catch (IOException e) {
			throw new ContentDaoException_NoSuchContent(contentId);
		}
	}

	@Override
	public List<String> getFolderContents(String folderId) {
		throw new UnsupportedOperationException("getFolderContents is not yet supported for HadoopContentDao");
	}

	@Override
	public boolean isFolderExisting(String folderId) {
		return false;
	}

	@Override
	public long getContentLength(String vaultContentId) {
		throw new UnsupportedOperationException("getContentLength is not yet supported for HadoopContentDao");
	}

	@Override
	public void moveFolder(String folderId, String newFolderId) {
		throw new UnsupportedOperationException("moveFolder is not yet supported for HadoopContentDao");
	}

	@Override
	public void deleteFolder(String folderId) {
		throw new UnsupportedOperationException("deleteFolder is not yet supported for HadoopContentDao");
	}

	private String formatIDToHadoop(String id) {
		return id.replace("/", "_");
	}

	@Override
	public void close() {

	}
}