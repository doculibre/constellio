package com.constellio.data.dao.services.contents;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.impl.CopyInputStreamFactory;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.constellio.data.dao.services.contents.ContentDao.MoveToVaultOption.ONLY_IF_INEXISTING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class FileSystemContentDaoAcceptanceTest extends ConstellioTest {
	public static final String FILE_NAME_1 = "FileName1.docx";
	public static final String FILE_NAME_2 = "FileName2.docx";
	public static final String FILE_NAME_3 = "FileName3.docx";

	private FileSystemContentDao fileSystemContentDao;
	private IOServices ioServices;
	private HashingService hashingService;

	private String fileHash1;
	private String fileHash2;
	private String fileHash3;

	private File fileOf1Vault;
	private File fileOf1Replicate;
	private File fileOf2Vault;
	private File fileOf2Replicate;
	private File fileOf3Vault;
	private File fileOf3Replicate;

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
		fileSystemContentDao = Mockito.spy(new FileSystemContentDao(getDataLayerFactory()));

		fileHash1 = hashingService.getHashFromFile(getTestResourceFile("1.docx"));
		fileHash2 = hashingService.getHashFromFile(getTestResourceFile("2.docx"));
		fileHash3 = hashingService.getHashFromFile(getTestResourceFile("3.docx"));

		fileOf1Vault = fileSystemContentDao.getFileOf(fileHash1);
		fileOf1Replicate = fileSystemContentDao.getReplicatedVaultFile(fileOf1Vault);

		fileOf2Vault = fileSystemContentDao.getFileOf(fileHash2);
		fileOf2Replicate = fileSystemContentDao.getReplicatedVaultFile(fileOf2Vault);

		fileOf3Vault = fileSystemContentDao.getFileOf(fileHash3);
		fileOf3Replicate = fileSystemContentDao.getReplicatedVaultFile(fileOf3Vault);
	}

	@Test
	public void givenVaultWihoutReplicationWhenMoveFileFailThenThrow() throws Exception {
		getDataLayerFactory().getDataLayerConfiguration().setContentDaoReplicatedVaultMountPoint(null);
		fileSystemContentDao = Mockito.spy(new FileSystemContentDao(getDataLayerFactory()));

		Mockito.doReturn(false).when(fileSystemContentDao).moveFile((File) Mockito.any(), (File) Mockito.any());

		File tempFile1 = ioServices.newTemporaryFile(FILE_NAME_1);
		FileUtils.copyToFile(getTestResourceInputStream("1.docx"), tempFile1);

		try {
			String fileHash1 = hashingService.getHashFromFile(tempFile1);
			fileSystemContentDao.moveFileToVault(fileHash1, tempFile1, ONLY_IF_INEXISTING);

			fail("Une exception doit être levé.");
		} catch (FileSystemContentDaoRuntimeException e) {
			// Ok Exception levé
		} finally {
			ioServices.deleteQuietly(tempFile1);
		}
	}

	@Test
	public void whenCopyFileWithExistingTargetAndOnlyIfExistingOptionThenMoveFileIsNotCalled() throws Exception {
		File tempFile1 = ioServices.newTemporaryFile(FILE_NAME_1);
		FileUtils.copyToFile(getTestResourceInputStream("1.docx"), tempFile1);
		InputStream inputStream = null;
		try {
			String fileHash1 = hashingService.getHashFromFile(tempFile1);
			inputStream = new FileInputStream(tempFile1);
			fileSystemContentDao.add(fileHash1, inputStream);
			fileSystemContentDao.moveFileToVault(fileHash1, tempFile1, ONLY_IF_INEXISTING);
			assertThat(fileSystemContentDao.isDocumentExisting(fileHash1)).isTrue();
			verify(fileSystemContentDao, times(0)).moveFile(Mockito.any(File.class), Mockito.any(File.class));
		} finally {
			ioServices.deleteQuietly(tempFile1);
			ioServices.closeQuietly(inputStream);
		}
	}

	@Test
	public void whenCopyFileWithInexistingTargetAndOnlyIfExistingOptionThenMoveFileIsCalled() throws Exception {
		File tempFile1 = ioServices.newTemporaryFile(FILE_NAME_1);
		FileUtils.copyToFile(getTestResourceInputStream("1.docx"), tempFile1);
		try {
			String fileHash1 = hashingService.getHashFromFile(tempFile1);
			fileSystemContentDao.moveFileToVault(fileHash1, tempFile1, ONLY_IF_INEXISTING);
			File fileOfFileHash1 = fileSystemContentDao.getFileOf(fileHash1);
			verify(fileSystemContentDao, times(1)).moveFile(tempFile1, fileOfFileHash1);
		} finally {
			ioServices.deleteQuietly(tempFile1);
		}
	}

	@Test
	public void whenCopyFileWithExistingTargetAndNoOptionThenMoveFileIsCalled() throws Exception {
		File tempFile1 = ioServices.newTemporaryFile(FILE_NAME_1);
		FileUtils.copyToFile(getTestResourceInputStream("1.docx"), tempFile1);
		InputStream inputStream = null;
		try {
			String fileHash1 = hashingService.getHashFromFile(tempFile1);
			inputStream = new FileInputStream(tempFile1);
			fileSystemContentDao.add(fileHash1, inputStream);
			fileSystemContentDao.moveFileToVault(fileHash1, tempFile1);
			assertThat(fileSystemContentDao.isDocumentExisting(fileHash1)).isTrue();
			File fileOfFileHash1 = fileSystemContentDao.getFileOf(fileHash1);
			verify(fileSystemContentDao, times(1)).moveFile(tempFile1, fileOfFileHash1);
		} finally {
			ioServices.deleteQuietly(tempFile1);
			ioServices.closeQuietly(inputStream);
		}
	}

	@Test
	public void givenMoveFileToVaultWithErrorsAddedToRecoveryFilesWhenReadLogAndRepairThenAllFileArePresent()
			throws Exception {

		File tempFile1, tempFile2, tempFile3;
		FileUtils.copyFile(getTestResourceFile("1.docx"), tempFile1 = ioServices.newTemporaryFile(FILE_NAME_1));
		FileUtils.copyFile(getTestResourceFile("2.docx"), tempFile2 = ioServices.newTemporaryFile(FILE_NAME_2));
		FileUtils.copyFile(getTestResourceFile("3.docx"), tempFile3 = ioServices.newTemporaryFile(FILE_NAME_3));

		Mockito.doReturn(false).doCallRealMethod().when(fileSystemContentDao)
				.moveFile((File) Mockito.any(), (File) Mockito.any());
		Mockito.doCallRealMethod().doReturn(false).doReturn(false).doCallRealMethod()
				.when(fileSystemContentDao).fileCopy((File) Mockito.any(), Mockito.anyString());

		fileSystemContentDao.moveFileToVault(fileHash1, tempFile1, ONLY_IF_INEXISTING);
		fileSystemContentDao.moveFileToVault(fileHash2, tempFile2, ONLY_IF_INEXISTING);
		fileSystemContentDao.moveFileToVault(fileHash3, tempFile3, ONLY_IF_INEXISTING);

		ioServices.deleteQuietly(tempFile1);
		ioServices.deleteQuietly(tempFile2);
		ioServices.deleteQuietly(tempFile3);

		assertThat(fileOf1Vault.exists()).isFalse();
		assertThat(fileOf1Replicate.exists()).isTrue();

		assertThat(fileOf2Vault.exists()).isTrue();
		assertThat(fileOf2Replicate.exists()).isFalse();

		assertThat(fileOf3Vault.exists()).isTrue();
		assertThat(fileOf3Replicate.exists()).isFalse();

		assertThatRecoveryFilesHaveCertainValues(fileSystemContentDao.getReplicationRootRecoveryFolder()
				.getAbsolutePath(), fileHash1);
		assertThatRecoveryFilesHaveCertainValues(fileSystemContentDao
				.getVaultRootRecoveryFolder().getAbsolutePath(), fileHash2, fileHash3);

		fileSystemContentDao.readLogsAndRepairs();

		assertThatRecoveryFileAreEmpty();

		assertThat(fileOf1Vault.exists()).isTrue();
		assertThat(fileOf1Replicate.exists()).isTrue();

		assertThat(fileOf2Vault.exists()).isTrue();
		assertThat(fileOf2Replicate.exists()).isTrue();

		assertThat(fileOf3Vault.exists()).isTrue();
		assertThat(fileOf3Replicate.exists()).isTrue();
	}

	@Test
	public void givenVaultAndReplicationFailToWriteWhenMoveFileToVaultThenFailAndThrow() throws Exception {
		File tempFile1 = ioServices.newTemporaryFile(FILE_NAME_1);

		FileUtils.copyToFile(getTestResourceInputStream("1.docx"), tempFile1);

		Mockito.doReturn(false).when(fileSystemContentDao).moveFile((File) Mockito.any(),
				(File) Mockito.any());
		Mockito.doReturn(false).when(fileSystemContentDao).fileCopy((File) Mockito.any(),
				Mockito.anyString());

		String fileHash1 = hashingService.getHashFromFile(tempFile1);

		try {
			fileSystemContentDao.moveFileToVault(fileHash1, tempFile1, ONLY_IF_INEXISTING);
			fail("The file vault move should fail.");
		} catch (FileSystemContentDaoRuntimeException e) {
			// Ok the exception is expected.
		} finally {
			ioServices.deleteQuietly(tempFile1);
		}
	}

	@Test
	public void givenAddFileToVaultWithErrorsAddedToRecoveryFilesWhenReadLogAndRepairThenAllFileArePresent()
			throws Exception {
		Mockito.doReturn(false).doCallRealMethod()
				.doCallRealMethod().doReturn(false)
				.doCallRealMethod().doReturn(false)
				.when(fileSystemContentDao).copy((CopyInputStreamFactory) Mockito.any(), (File) Mockito.any());

		fileSystemContentDao.add(fileHash1, getTestResourceInputStream("1.docx"));
		fileSystemContentDao.add(fileHash2, getTestResourceInputStream("2.docx"));
		fileSystemContentDao.add(fileHash3, getTestResourceInputStream("3.docx"));


		assertThat(fileOf1Vault.exists()).isFalse();
		assertThat(fileOf1Replicate.exists()).isTrue();

		assertThat(fileOf2Vault.exists()).isTrue();
		assertThat(fileOf2Replicate.exists()).isFalse();

		assertThat(fileOf3Vault.exists()).isTrue();
		assertThat(fileOf3Replicate.exists()).isFalse();

		assertThatRecoveryFilesHaveCertainValues(fileSystemContentDao.getReplicationRootRecoveryFolder()
				.getAbsolutePath(), fileHash1);
		assertThatRecoveryFilesHaveCertainValues(fileSystemContentDao
				.getVaultRootRecoveryFolder().getAbsolutePath(), fileHash2, fileHash3);

		fileSystemContentDao.readLogsAndRepairs();

		assertThatRecoveryFileAreEmpty();

		assertThat(fileOf1Vault.exists()).isTrue();
		assertThat(fileOf1Replicate.exists()).isTrue();

		assertThat(fileOf2Vault.exists()).isTrue();
		assertThat(fileOf2Replicate.exists()).isTrue();

		assertThat(fileOf3Vault.exists()).isTrue();
		assertThat(fileOf3Replicate.exists()).isTrue();
	}

	@Test
	public void givenAddFileToVaultWithErrorsAndDeleteOneReplicationFileWhenReadLogAndRepairThenNoExceptionAndFileIsEmpty()
			throws Exception {

		Mockito.doReturn(false).doCallRealMethod()
				.doCallRealMethod().doReturn(false)
				.doCallRealMethod().doReturn(false)
				.when(fileSystemContentDao).copy((CopyInputStreamFactory) Mockito.any(), (File) Mockito.any());

		fileSystemContentDao.add(fileHash1, getTestResourceInputStream("1.docx"));
		fileSystemContentDao.add(fileHash2, getTestResourceInputStream("2.docx"));
		fileSystemContentDao.add(fileHash3, getTestResourceInputStream("3.docx"));

		assertThat(fileOf1Vault.exists()).isFalse();
		assertThat(fileOf1Replicate.exists()).isTrue();

		assertThat(fileOf2Vault.exists()).isTrue();
		assertThat(fileOf2Replicate.exists()).isFalse();

		assertThat(fileOf3Vault.exists()).isTrue();
		assertThat(fileOf3Replicate.exists()).isFalse();

		// delete
		assertThat(fileOf2Vault.delete()).isTrue();

		assertThatRecoveryFilesHaveCertainValues(fileSystemContentDao.getReplicationRootRecoveryFolder()
				.getAbsolutePath(), fileHash1);
		assertThatRecoveryFilesHaveCertainValues(fileSystemContentDao
				.getVaultRootRecoveryFolder().getAbsolutePath(), fileHash2, fileHash3);

		fileSystemContentDao.readLogsAndRepairs();

		assertThatRecoveryFileAreEmpty();

		assertThat(fileOf1Vault.exists()).isTrue();
		assertThat(fileOf1Replicate.exists()).isTrue();

		assertThat(fileOf2Vault.exists()).isFalse();
		assertThat(fileOf2Replicate.exists()).isFalse();

		assertThat(fileOf3Vault.exists()).isTrue();
		assertThat(fileOf3Replicate.exists()).isTrue();
	}

	@Test
	public void givenAddFileToVaultWithErrorsAndDeleteOneVaultFileWhenReadLogAndRepairThenNoExceptionAndFileIsEmpty()
			throws Exception {
		Mockito.doReturn(false).doCallRealMethod()
				.doReturn(false).doCallRealMethod()
				.doCallRealMethod().doReturn(false)
				.when(fileSystemContentDao).copy((CopyInputStreamFactory) Mockito.any(), (File) Mockito.any());

		fileSystemContentDao.add(fileHash1, getTestResourceInputStream("1.docx"));
		fileSystemContentDao.add(fileHash2, getTestResourceInputStream("2.docx"));
		fileSystemContentDao.add(fileHash3, getTestResourceInputStream("3.docx"));

		assertThat(fileOf1Vault.exists()).isFalse();
		assertThat(fileOf1Replicate.exists()).isTrue();

		assertThat(fileOf2Vault.exists()).isFalse();
		assertThat(fileOf2Replicate.exists()).isTrue();

		assertThat(fileOf3Vault.exists()).isTrue();
		assertThat(fileOf3Replicate.exists()).isFalse();

		assertThatRecoveryFilesHaveCertainValues(fileSystemContentDao.getReplicationRootRecoveryFolder()
				.getAbsolutePath(), fileHash1, fileHash2);
		assertThatRecoveryFilesHaveCertainValues(fileSystemContentDao
				.getVaultRootRecoveryFolder().getAbsolutePath(), fileHash3);


		assertThat(fileOf1Replicate.delete()).isTrue();

		fileSystemContentDao.readLogsAndRepairs();

		assertThatRecoveryFileAreEmpty();

		assertThat(fileOf1Vault.exists()).isFalse();
		assertThat(fileOf1Replicate.exists()).isFalse();

		assertThat(fileOf2Vault.exists()).isTrue();
		assertThat(fileOf2Replicate.exists()).isTrue();

		assertThat(fileOf3Vault.exists()).isTrue();
		assertThat(fileOf3Replicate.exists()).isTrue();
	}

	@Test
	public void givenVaultAndReplicationFailToCopyWhenAddFileThenFailAndThrow() throws Exception {
		File tempFile1 = ioServices.newTemporaryFile(FILE_NAME_1);

		FileUtils.copyToFile(getTestResourceInputStream("1.docx"), tempFile1);

		Mockito.doReturn(false)
				.when(fileSystemContentDao)
				.copy((CopyInputStreamFactory) Mockito.any(), (File) Mockito.any());

		String fileHash1 = hashingService.getHashFromFile(tempFile1);
		InputStream tempFileInputStream = new FileInputStream(tempFile1);

		try {
			fileSystemContentDao.add(fileHash1, tempFileInputStream);
			fail("The file vault move should fail.");
		} catch (FileSystemContentDaoRuntimeException e) {
			assertThat(true).isTrue();
		} finally {
			ioServices.closeQuietly(tempFileInputStream);
			ioServices.deleteQuietly(tempFile1);
		}
	}

	private int getNumberOfNonEmptyLines(String folderPath) throws IOException {
		File folder = new File(folderPath);
		return folder.listFiles().length;
	}

	private void assertThatRecoveryFileAreEmpty() throws IOException {
		assertThat(getNumberOfNonEmptyLines(fileSystemContentDao.getReplicationRootRecoveryFolder()
				.getAbsolutePath())).isEqualTo(0);

		assertThat(getNumberOfNonEmptyLines(fileSystemContentDao.getVaultRootRecoveryFolder()
				.getAbsolutePath())).isEqualTo(0);
	}

	private void assertThatRecoveryFilesHaveCertainValues(String filePath, String... hashList) throws IOException {
		File file = new File(filePath);
		File[] fileList = file.listFiles();

		for (int i = 0; i < fileList.length; i++) {
			assertThat(isHashPresent(fileList[i].getName(), hashList)).isTrue();
		}

		assertThat(fileList.length).isEqualTo(hashList.length);
	}

	private boolean isHashPresent(String hash, String[] hashList) {
		for (String currentHash : hashList) {
			if (currentHash.equals(hash)) {
				return true;
			}
		}

		return false;
	}
}
