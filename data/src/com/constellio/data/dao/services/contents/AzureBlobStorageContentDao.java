package com.constellio.data.dao.services.contents;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.specialized.BlobInputStream;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.contents.AzureBlobStorageContentDaoRuntimeException.AzureBlobStorageContentDaoRuntimeException_FailedToWriteVault;
import com.constellio.data.dao.services.contents.ContentDaoException.ContentDaoException_NoSuchContent;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.CloseableStreamFactory;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

@Slf4j
public class AzureBlobStorageContentDao implements StatefulService, ContentDao {

	DataLayerFactory dataLayerFactory;
	DataLayerConfiguration configuration;
	IOServices ioServices;
	FileSystemContentDao fileSystemContentDao;

	@VisibleForTesting
	File rootFolder;

	public AzureBlobStorageContentDao(DataLayerFactory dataLayerFactory) {
		this.dataLayerFactory = dataLayerFactory;
		this.ioServices = dataLayerFactory.getIOServicesFactory().newIOServices();
		this.configuration = dataLayerFactory.getDataLayerConfiguration();
		rootFolder = configuration.getContentDaoFileSystemFolder();
		fileSystemContentDao = new FileSystemContentDao(dataLayerFactory);
	}

	@Override
	public void initialize() {

	}

	@Override
	public void close() {

	}

	@Override
	public void add(String newContentId, InputStream newInputStream) {
		if (typeStoredInAzure(newContentId)) {
			BlobOutputStream blobOutputStream = null;
			try {
				BlobClient blobClient = getBlobClient(newContentId);

				blobOutputStream = blobClient.getBlockBlobClient().getBlobOutputStream();

				int next = newInputStream.read();
				while (next != -1) {
					blobOutputStream.write(next);
					next = newInputStream.read();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				ioServices.closeQuietly(blobOutputStream);
				ioServices.closeQuietly(newInputStream);
			}
		} else {
			fileSystemContentDao.add(newContentId, newInputStream);
		}
	}

	@Override
	public void delete(List<String> contentIds) {
		for (String contentId : contentIds) {
			if (typeStoredInAzure(contentId)) {
				BlobClient blobClient = getBlobClient(contentId);

				blobClient.delete();
			} else {
				fileSystemContentDao.delete(asList(contentId));
			}
		}
	}

	@Override
	public void moveFileToVault(String id, File file, MoveToVaultOption... options) {
		if (typeStoredInAzure(id)) {
			throw new AzureBlobStorageContentDaoRuntimeException_FailedToWriteVault(id);
		} else {
			fileSystemContentDao.moveFileToVault(id, file, options);
		}
	}

	@Override
	public String getLocalRelativePath(String id) {
		if (typeStoredInAzure(id)) {
			return null;
		}
		return fileSystemContentDao.getLocalRelativePath(id);
	}

	@Override
	public IOServices getIOServices() {
		return ioServices;
	}

	@Override
	public InputStream getContentInputStream(String contentId, String streamName)
			throws ContentDaoException_NoSuchContent {
		if (typeStoredInAzure(contentId)) {
			BlobClient blobClient = getBlobClient(contentId);
			return blobClient.openInputStream();
		} else {
			return fileSystemContentDao.getContentInputStream(contentId, streamName);
		}
	}

	@Override
	public List<String> getFolderContents(String folderId) {
		return fileSystemContentDao.getFolderContents(folderId);
	}

	@Override
	public boolean isFolderExisting(String folderId) {
		return fileSystemContentDao.isFolderExisting(folderId);
	}

	@Override
	public boolean isDocumentExisting(String documentId) {
		BlobClient blobClient = getBlobClient(documentId);
		return blobClient.exists();
	}

	@Override
	public long getContentLength(String vaultContentId) {
		DaoFile file = getFile(vaultContentId);
		return file.length();
	}

	@Override
	public void moveFolder(String folderId, String newFolderId) {

	}

	@Override
	public void deleteFolder(String folderId) {

	}

	@Override
	public CloseableStreamFactory<InputStream> getContentInputStreamFactory(String id)
			throws ContentDaoException_NoSuchContent {
		DaoFile file = getFile(id);
		InputStream contentInputStream = getContentInputStream(id, null);
		if (typeStoredInAzure(id)) {
			return new CloseableStreamFactory<InputStream>() {
				@Override
				public void close()
						throws IOException {
				}

				@Override
				public long length() {
					return file.length();
				}

				@Override
				public InputStream create(String name) {
					return new BufferedInputStream(contentInputStream);
				}
			};
		} else {
			return fileSystemContentDao.getContentInputStreamFactory(id);
		}
	}

	@Override
	public File getFileOf(String contentId) {
		if (typeStoredInAzure(contentId)) {
			throw new AzureBlobStorageContentDaoRuntimeException_FailedToWriteVault(contentId);
		} else {
			return fileSystemContentDao.getFileOf(contentId);
		}
	}

	@Override
	public DaoFile getFile(String contentId) {
		if (typeStoredInAzure(contentId)) {
			BlobClient blobClient = getBlobClient(contentId);
			BlobInputStream blobInputStream = null;
			DaoFile file;
			try {
				blobInputStream = blobClient.openInputStream();
				BlobProperties properties = blobInputStream.getProperties();

				file = new DaoFile(contentId, contentId, properties.getBlobSize(), properties.getLastModified().toInstant().toEpochMilli(), this);
			} catch (BlobStorageException e) {
				throw new AzureBlobStorageContentDaoRuntimeException_FailedToWriteVault(contentId);
			} finally {
				ioServices.closeQuietly(blobInputStream);
			}

			return file;
		} else {
			File file = getFileOf(contentId);
			return new DaoFile(contentId, file.getName(), file.length(), file.lastModified(), this);
		}
	}

	@Override
	public void readLogsAndRepairs() {
		fileSystemContentDao.readLogsAndRepairs();
	}

	@Override
	public Stream<Path> streamVaultContent(Predicate<? super Path> filter) {
		return null;
	}

	protected BlobClient getBlobClient(String contentId) {
		StorageSharedKeyCredential storageSharedKeyCredential = new StorageSharedKeyCredential(configuration.getAzureBlobStorageConnectionAccountName(),
				configuration.getAzureBlobStorageConnectionAccountKey());
		BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
				.credential(storageSharedKeyCredential)
				.connectionString(configuration.getAzureBlobStorageConnectionString())
				.buildClient();


		BlobContainerClient containerClient;
		try {
			containerClient = blobServiceClient.getBlobContainerClient(dataLayerFactory.getDataLayerConfiguration().getAzureBlobStorageContainerName());

			BlobClient blobClient = containerClient.getBlobClient(contentId);

			return blobClient;
		} catch (BlobStorageException e) {
			log.error("Unable to get blob client with id : " + contentId);
			throw new RuntimeException(e);
		}
	}

	boolean typeStoredInAzure(String contentId) {
		return !contentId.endsWith("._parsed") || !contentId.contains("/");
	}

}
