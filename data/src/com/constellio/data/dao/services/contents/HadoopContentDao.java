package com.constellio.data.dao.services.contents;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.contents.ContentDaoException.ContentDaoException_NoSuchContent;
import com.constellio.data.dao.services.contents.HadoopContentDaoRuntimeException.HadoopContentDaoRuntimeException_DatastoreFailure;
import com.constellio.data.io.streamFactories.CloseableStreamFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
	public void moveFileToVault(File file, String newContentId) {
		throw new UnsupportedOperationException("moveFileToVault is not yet supported for HadoopContentDao");
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
	public LocalDateTime getLastModification(String contentId) {
		throw new UnsupportedOperationException("Unsupported");
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
	public boolean isDocumentExisting(String documentId) {
		throw new UnsupportedOperationException("isDocumentExisting is not yet supported for HadoopContentDao");
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

	@Override
	public CloseableStreamFactory<InputStream> getContentInputStreamFactory(String id)
			throws ContentDaoException_NoSuchContent {
		throw new UnsupportedOperationException("getContentInputStreamFactory is not yet supported for HadoopContentDao");
	}

	@Override
	public void readLogsAndRepairs() {
		throw new UnsupportedOperationException("readLogsAndRepairs is not supported for HadoopContentDao.");
	}

	private String formatIDToHadoop(String id) {
		return id.replace("/", "_");
	}

	@Override
	public File getFileOf(String contentId) {
		throw new UnsupportedOperationException("getFileOf(contentId) is not yet supported for HadoopContentDao");
	}

	@Override
	public void close() {

	}
}