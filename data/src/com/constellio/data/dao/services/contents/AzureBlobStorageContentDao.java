package com.constellio.data.dao.services.contents;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.specialized.BlobInputStream;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.contents.AzureBlobStorageContentDaoRuntimeException.AzureBlobStorageContentDaoRuntimeException_FailedToAddFile;
import com.constellio.data.dao.services.contents.AzureBlobStorageContentDaoRuntimeException.AzureBlobStorageContentDaoRuntimeException_FailedToDeleteFileFromAzure;
import com.constellio.data.dao.services.contents.AzureBlobStorageContentDaoRuntimeException.AzureBlobStorageContentDaoRuntimeException_FailedToGetFile;
import com.constellio.data.dao.services.contents.AzureBlobStorageContentDaoRuntimeException.AzureBlobStorageContentDaoRuntimeException_FailedToMoveFileToVault;
import com.constellio.data.dao.services.contents.ContentDaoException.ContentDaoException_NoSuchContent;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.CloseableStreamFactory;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

@Slf4j
public class AzureBlobStorageContentDao implements StatefulService, ContentDao {

	private static final String RECOVERY_FOLDER = "vaultrecoveryfolder";
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
			try (BlobOutputStream blobOutputStream = getBlobClient(newContentId).getBlockBlobClient().getBlobOutputStream(true)) {
				IOUtils.copy(newInputStream, blobOutputStream);
			} catch (IOException e) {
				throw new AzureBlobStorageContentDaoRuntimeException_FailedToAddFile(newContentId);
			} finally {
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
				try {
					blobClient.delete();
				} catch (BlobStorageException e) {
					throw new AzureBlobStorageContentDaoRuntimeException_FailedToDeleteFileFromAzure(contentId);
				}

			} else {
				fileSystemContentDao.delete(asList(contentId));
			}
		}
	}


	@Override
	public void moveFileToVault(String relativePath, File file, MoveToVaultOption... options) {
		if (!isDocumentExisting(relativePath)) {
			try (InputStream inputStream = new FileInputStream(file)) {

				add(relativePath, inputStream);
				FileUtils.deleteQuietly(file);
			} catch (IOException e) {
				throw new AzureBlobStorageContentDaoRuntimeException_FailedToMoveFileToVault(relativePath);
			}
		} else {
			log.info("Document " + relativePath + " already exists");
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
			InputStream inputStream = blobClient.openInputStream();
			return inputStream;
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
			return null;
		} else {
			return fileSystemContentDao.getFileOf(contentId);
		}
	}

	@Override
	public DaoFile getFile(String contentId) {
		if (typeStoredInAzure(contentId)) {
			BlobClient blobClient = getBlobClient(contentId);
			DaoFile file;
			try (BlobInputStream blobInputStream = blobClient.openInputStream();) {
				BlobProperties properties = blobInputStream.getProperties();

				file = new DaoFile(contentId, contentId, properties.getBlobSize(), properties.getLastModified().toInstant().toEpochMilli(), false, this);
			} catch (BlobStorageException e) {
				throw new AzureBlobStorageContentDaoRuntimeException_FailedToGetFile(contentId);
			}

			return file;
		} else {
			File file = getFileOf(contentId);
			return new DaoFile(contentId, file.getName(), file.length(), file.lastModified(), file.isDirectory(), this);
		}
	}

	@Override
	public void readLogsAndRepairs() {
		fileSystemContentDao.readLogsAndRepairs();
	}

	@Override
	public Stream<DaoFile> streamVaultContent(Predicate<? super DaoFile> filter,
											  Comparator<? super DaoFile> orderComparator) {
		Stream<DaoFile> localContentStream = fileSystemContentDao.streamVaultContent(filter, orderComparator);
		PagedIterable<BlobItem> blobItems = getBlobContainerClient().listBlobs();

		Stream<DaoFile> azureContentStream = blobItems.stream()
				.map(blobItem -> new DaoFile(blobItem.getName(), blobItem.getName(), blobItem.getProperties().getContentLength(), blobItem.getProperties().getLastModified().toInstant().toEpochMilli(), false, this));
		Stream<DaoFile> contentStream = Stream.concat(localContentStream, azureContentStream).filter(filter).sorted(orderComparator);
		return contentStream;
	}

	protected BlobClient getBlobClient(String contentId) {
		BlobContainerClient containerClient = getBlobContainerClient();
		BlobClient blobClient = containerClient.getBlobClient(contentId);

		return blobClient;
	}

	protected BlobContainerClient getBlobContainerClient() {
		StorageSharedKeyCredential storageSharedKeyCredential = new StorageSharedKeyCredential(configuration.getAzureBlobStorageConnectionAccountName(),
				configuration.getAzureBlobStorageConnectionAccountKey());
		BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
				.credential(storageSharedKeyCredential)
				.connectionString(configuration.getAzureBlobStorageConnectionString())
				.buildClient();


		BlobContainerClient containerClient;
		String azureBlobStorageContainerName = dataLayerFactory.getDataLayerConfiguration().getAzureBlobStorageContainerName();
		try {
			containerClient = blobServiceClient.getBlobContainerClient(azureBlobStorageContainerName);

			return containerClient;
		} catch (BlobStorageException e) {
			log.error("Unable to get blob container client : " + azureBlobStorageContainerName);
			throw new RuntimeException(e);
		}
	}

	boolean typeStoredInAzure(String contentId) {
		return !contentId.endsWith("__parsed") && !contentId.contains("/");
	}

	public void deleteContainer() {
		BlobContainerClient blobContainerClient = getBlobContainerClient();
		if (blobContainerClient.exists()) {
			blobContainerClient.delete();
		}
	}
}
