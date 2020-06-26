package com.constellio.data.dao.services.contents;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.constellio.data.dao.services.contents.ContentDao.MoveToVaultOption;
import com.constellio.data.dao.services.contents.ContentDaoException.ContentDaoException_NoSuchContent;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Iterator;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

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
	public void whenMoveToVaultThenFileExistsInVaultAndRemovedFromInitialPath() {
		File tempFile = newTempFileWithContent("azureBlobStorageContentDaoAcceptanceTest_temp", fileHash1);
		azureBlobStorageContentDao.moveFileToVault(fileHash1, tempFile, MoveToVaultOption.ONLY_IF_INEXISTING);
		assertThat(azureBlobStorageContentDao.isDocumentExisting(fileHash1)).isTrue();
		assertThat(tempFile.exists()).isFalse();
	}

	@Test
	public void whenCopyFileFromVaultThenFileExistsInVault() throws ContentDaoException_NoSuchContent {
		InputStream inputStream = getTestResourceInputStream("1.docx");

		azureBlobStorageContentDao.add(fileHash1, inputStream);
		File file = new File("C:\\Workspace\\dev-constellio-java8\\constellio\\sdk\\sdk-resources\\com\\constellio\\data\\dao\\services\\contents", "copy.docx");
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
		Stream<DaoFile> daoFileStream = azureBlobStorageContentDao.streamVaultContent(file -> (file.getId().equals(fileHash1)), (file1, file2) -> 0);
		daoFileStream.forEach(file -> assertThat(file.getId()).isEqualTo(fileHash1));
	}

	@Test
	public void givenToFilesInAzureWhenStreamVaultContentWithOrderThenFilesAreStreamedInTheDefinedOrder() {
		addFileToVault("1.docx", fileHash1);
		addFileToVault("2.docx", fileHash2);
		Stream<DaoFile> daoFileStream = azureBlobStorageContentDao.streamVaultContent(file -> true, Comparator.comparing(DaoFile::getName));
		Iterator<DaoFile> iterator = daoFileStream.iterator();
		String previousFileName = iterator.next().getName();
		while (iterator.hasNext()) {
			assertThat(iterator.next().getName().compareTo(previousFileName)).isGreaterThan(1);
		}
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
