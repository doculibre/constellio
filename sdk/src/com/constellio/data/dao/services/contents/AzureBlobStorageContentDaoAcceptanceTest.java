package com.constellio.data.dao.services.contents;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.constellio.data.dao.services.contents.AzureBlobStorageContentDaoRuntimeException.AzureBlobStorageContentDaoRuntimeException_FailedToAddFile;
import com.constellio.data.dao.services.contents.ContentDao.MoveToVaultOption;
import com.constellio.data.dao.services.contents.ContentDaoException.ContentDaoException_NoSuchContent;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.data.utils.hashing.HashingServiceException;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AzureBlobStorageContentDaoAcceptanceTest extends ConstellioTest {

	public static final String CONTAINER_NAME = "test";

	private IOServices ioServices;
	private HashingService hashingService;

	private String fileHash1;
	private String fileHash2;

	private AzureBlobStorageContentDao azureBlobStorageContentDao;

	@BeforeClass
	static public void createContainer() {
		getBlobServiceClient().createBlobContainer(CONTAINER_NAME);
	}

	@Before
	public void setUp() throws Exception {
		getDataLayerFactory().getDataLayerConfiguration().setAzureBlobStorageConnectionAccountName(SDKPasswords.testAzureAccountName());
		getDataLayerFactory().getDataLayerConfiguration().setAzureBlobStorageConnectionAccountKey(SDKPasswords.testAzureAccountPassword());
		getDataLayerFactory().getDataLayerConfiguration().setAzureBlobStorageConnectionString(SDKPasswords.testAzureConnectionString());
		getDataLayerFactory().getDataLayerConfiguration().setAzureBlobStorageContainerName(CONTAINER_NAME);

		prepareSystem(
				withZeCollection().withAllTestUsers()
		);

		ioServices = getModelLayerFactory().getIOServicesFactory().newIOServices();
		hashingService = getModelLayerFactory().getDataLayerFactory()
				.getIOServicesFactory().newHashingService(getModelLayerFactory()
						.getDataLayerFactory().getDataLayerConfiguration().getHashingEncoding());

		getDataLayerFactory().getDataLayerConfiguration().setContentDaoReplicatedVaultMountPoint(newTempFolder().getAbsolutePath());
		azureBlobStorageContentDao = spy(new AzureBlobStorageContentDao(getDataLayerFactory()));

		fileHash1 = hashingService.getHashFromFile(getTestResourceFile("1.docx"));
		fileHash2 = hashingService.getHashFromFile(getTestResourceFile("2.docx"));
	}

	@Test
	public void givenFileWhenAddInputStreamThenFileAddedToAzureContainer() {
		addFileToVault("1.docx", fileHash1);

		assertThat(azureBlobStorageContentDao.getFile(fileHash1).getId()).isEqualTo(fileHash1);
	}

	@Test
	public void givenFileAddedToAzureWhenDeleteFileoAzureContainer() {
		addFileToVault("2.docx", fileHash2);

		assertThat(azureBlobStorageContentDao.getFile(fileHash2).getId()).isEqualTo(fileHash2);

		azureBlobStorageContentDao.delete(asList(fileHash2));

		assertThat(azureBlobStorageContentDao.isDocumentExisting(fileHash2)).isFalse();
	}

	@Test
	public void whenMoveToVaultWithOnlyIfExistingParameterThenFileExistsInVault() {
		azureBlobStorageContentDao.delete(asList(fileHash1));
		File tempFile = newTempFileWithContent("azureBlobStorageContentDaoAcceptanceTest_temp", fileHash1);
		azureBlobStorageContentDao.moveFileToVault(fileHash1, tempFile, MoveToVaultOption.ONLY_IF_INEXISTING);
		assertThat(azureBlobStorageContentDao.isDocumentExisting(fileHash1)).isTrue();
		try (InputStream inputStream = new FileInputStream(tempFile)) {
			verify(azureBlobStorageContentDao, times(1)).add(fileHash1, inputStream);
		} catch (IOException e) {
		} finally {
			ioServices.deleteQuietly(tempFile);
		}

	}

	@Test
	public void whenMoveToVaultWithNoParameterThenFileExistsInVault() {
		File tempFile = newTempFileWithContent("azureBlobStorageContentDaoAcceptanceTest_temp", fileHash1);
		try (InputStream inputStream = new FileInputStream(tempFile)) {
			BlobContainerClient blobContainerClient = getBlobServiceClient().getBlobContainerClient(CONTAINER_NAME);
			add(inputStream, fileHash1, blobContainerClient);
			azureBlobStorageContentDao.moveFileToVault(fileHash1, tempFile);
			verify(azureBlobStorageContentDao, times(0)).add(fileHash1, inputStream);
		} catch (IOException e) {
		} finally {
			ioServices.deleteQuietly(tempFile);
		}
	}

	private void add(InputStream newInputStream, String newContentId, BlobContainerClient blobContainerClient) {
		try (BlobOutputStream blobOutputStream = blobContainerClient.getBlobClient(newContentId).getBlockBlobClient().getBlobOutputStream(true)) {
			IOUtils.copy(newInputStream, blobOutputStream);
		} catch (IOException e) {
			throw new AzureBlobStorageContentDaoRuntimeException_FailedToAddFile(newContentId);
		} finally {
			ioServices.closeQuietly(newInputStream);
		}
	}

	@Test
	public void whenCopyFileFromVaultThenFileExistsInVault() throws ContentDaoException_NoSuchContent {
		InputStream inputStream = getTestResourceInputStream("1.docx");

		azureBlobStorageContentDao.add(fileHash1, inputStream);
		File contentDaoFileSystemFolder = getDataLayerFactory().getDataLayerConfiguration().getContentDaoFileSystemFolder();
		File file = new File(contentDaoFileSystemFolder.getPath(), "copy.docx");
		assertThat(file.exists()).isFalse();
		azureBlobStorageContentDao.copyFileFromVault(fileHash1, file);
		assertThat(azureBlobStorageContentDao.isDocumentExisting(fileHash1)).isTrue();
		assertThat(file.exists()).isTrue();
		file.delete();
	}

	@Test
	public void givenToFilesInAzureWhenStreamVaultContentWithFilterThenFiltered() {
		addFileToVault("1.docx", fileHash1);
		addFileToVault("2.docx", fileHash2);
		Stream<DaoFile> daoFileStream = azureBlobStorageContentDao.streamVaultContent(file -> (file.getId().equals(fileHash1)));
		daoFileStream.forEach(file -> assertThat(file.getId()).isEqualTo(fileHash1));
	}

	@Test
	public void givenFilesInAzureWhenStreamVaultContentWithOrderThenFilesAreStreamedInTheDefinedOrder()
			throws FileNotFoundException, HashingServiceException {
		for (int i = 0; i < 100; i++) {
			File tempFile = newTempFileWithContent("file" + i + ".txt", "This is file " + i);
			InputStream inputStream = new FileInputStream(tempFile);
			String hashFromFile = hashingService.getHashFromFile(tempFile);
			azureBlobStorageContentDao.add(hashFromFile, inputStream);
		}

		Iterator<DaoFile> iterator = azureBlobStorageContentDao.streamVaultContent(file -> true).iterator();
		while (iterator.hasNext()) {
			String previousFileName = iterator.next().getName();
			if (iterator.hasNext()) {
				DaoFile file = iterator.next();
				System.out.println(previousFileName);
				System.out.println(file.getName());
				System.out.println(file.getName().compareTo(previousFileName));
			}
		}
	}

	@Test
	public void whenAddingParsedContentThenContentIsAddedInSystemFolder() {
		addFileToVault("1.docx", fileHash1 + "__parsed");

		File contentDaoFileSystemFolder = getDataLayerFactory().getDataLayerConfiguration().getContentDaoFileSystemFolder();
		File file = new File(contentDaoFileSystemFolder, fileHash1 + "__parsed");
		assertThat(file.exists());
	}


	@AfterClass
	static public void deleteContainer() {
		getBlobServiceClient().deleteBlobContainer(CONTAINER_NAME);
	}

	static private BlobServiceClient getBlobServiceClient() {
		StorageSharedKeyCredential storageSharedKeyCredential = new StorageSharedKeyCredential(SDKPasswords.testAzureAccountName(),
				SDKPasswords.testAzureAccountPassword());
		return new BlobServiceClientBuilder()
				.credential(storageSharedKeyCredential)
				.connectionString(SDKPasswords.testAzureConnectionString())
				.buildClient();
	}

	private void addFileToVault(String partialName, String fileHash) {
		try (InputStream inputStream = getTestResourceInputStream(partialName)) {
			azureBlobStorageContentDao.add(fileHash, inputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
