package com.constellio.data.dao.services.contents;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class AzureBlobStorageContentDaoAcceptanceTest extends ConstellioTest {

	public static final String CONTAINER_NAME = "test";

	public static final String FILE_NAME_1 = "FileName1.docx";
	public static final String FILE_NAME_2 = "FileName2.docx";
	public static final String FILE_NAME_3 = "FileName3.docx";

	private FileSystemContentDao fileSystemContentDao;
	private IOServices ioServices;
	private HashingService hashingService;

	private String fileHash1;
	private String fileHash2;

	@BeforeClass
	static public void createContainer() {
		getBlobServiceClient().createBlobContainer(CONTAINER_NAME);
	}

	@Before
	public void setUp() throws Exception {
		prepareSystem(
				withZeCollection().withAllTestUsers()
		);

		ioServices = getModelLayerFactory().getIOServicesFactory().newIOServices();
		hashingService = getModelLayerFactory().getDataLayerFactory()
				.getIOServicesFactory().newHashingService(getModelLayerFactory()
						.getDataLayerFactory().getDataLayerConfiguration().getHashingEncoding());

		getDataLayerFactory().getDataLayerConfiguration().setContentDaoReplicatedVaultMountPoint(newTempFolder().getAbsolutePath());
		fileSystemContentDao = new FileSystemContentDao(getDataLayerFactory());

		fileHash1 = hashingService.getHashFromFile(getTestResourceFile("1.docx"));
		fileHash2 = hashingService.getHashFromFile(getTestResourceFile("2.docx"));


		getDataLayerFactory().getDataLayerConfiguration().setAzureBlobStorageConnectionAccountName(SDKPasswords.testAzureAccountName());
		getDataLayerFactory().getDataLayerConfiguration().setAzureBlobStorageConnectionAccountKey(SDKPasswords.testAzureSynchApplicationKey());
		getDataLayerFactory().getDataLayerConfiguration().setAzureBlobStorageConnectionString(SDKPasswords.testAzureConnectionString());
		getDataLayerFactory().getDataLayerConfiguration().setAzureBlobStorageContainerName(CONTAINER_NAME);
	}

	@Test
	public void givenFileWhenAddInputStreamThenFileAddedToAzureContainer() {
		InputStream inputStream = null;
		InputStream contentInputStream = null;
		try {
			AzureBlobStorageContentDao azureBlobStorageContentDao = new AzureBlobStorageContentDao(getDataLayerFactory());

			inputStream = getTestResourceInputStream("1.docx");

			azureBlobStorageContentDao.add(fileHash1, inputStream);

			assertThat(azureBlobStorageContentDao.getFile(fileHash1).getId()).isEqualTo(fileHash1);

		} finally {
			ioServices.closeQuietly(inputStream);
			ioServices.closeQuietly(contentInputStream);
		}
	}

	@Test(expected = AzureBlobStorageContentDaoRuntimeException.class)
	public void givenFileAddedToAzureWhenDeleteFileoAzureContainer() {
		InputStream inputStream = null;
		InputStream contentInputStream = null;
		try {
			AzureBlobStorageContentDao azureBlobStorageContentDao = new AzureBlobStorageContentDao(getDataLayerFactory());

			inputStream = getTestResourceInputStream("2.docx");

			azureBlobStorageContentDao.add(fileHash2, inputStream);

			assertThat(azureBlobStorageContentDao.getFile(fileHash2).getId()).isEqualTo(fileHash2);

			azureBlobStorageContentDao.delete(asList(fileHash2));

			azureBlobStorageContentDao.getFile(fileHash2);

		} finally {
			ioServices.closeQuietly(inputStream);
			ioServices.closeQuietly(contentInputStream);
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


}
