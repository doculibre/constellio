package com.constellio.data.dao.services.contents;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.impl.CopyInputStreamFactory;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.sdk.tests.ConstellioTest;
import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;

public class FileSystemContentDaoAcceptanceTest extends ConstellioTest {
    public static final String FILE_SYSTEM_CONTENT_DAO_STREAM_NAME_1 = "FileSystemContentDaoStreamName1";
    public static final String FILE_SYSTEM_CONTENT_DAO_STREAM_NAME_2 = "FileSystemContentDaoStreamName2";
    public static final String FILE_SYSTEM_CONTENT_DAO_STREAM_NAME_3 = "FileSystemContentDaoStreamName3";

    public static final String FILE_NAME_1 = "FileName1.docx";
    public static final String FILE_NAME_2 = "FileName2.docx";
    public static final String FILE_NAME_3 = "FileName3.docx";

    public static final String FILE_STREAM = "FileSystemContentDaoAcceptanceTestFileStream";


    private FileSystemContentDao fileSystemContentDao;
    private IOServices ioServices;
    private HashingService hashingService;


    @Before
    public void setUp() {
        prepareSystem(
                withZeCollection().withAllTestUsers()
        );

        ioServices = getModelLayerFactory().getIOServicesFactory().newIOServices();
        hashingService = getModelLayerFactory().getDataLayerFactory()
                .getIOServicesFactory().newHashingService(getModelLayerFactory()
                        .getDataLayerFactory().getDataLayerConfiguration().getHashingEncoding());

        getDataLayerFactory().getDataLayerConfiguration().setContentDaoReplicatedVaultMountPoint(newTempFolder().getAbsolutePath());
        fileSystemContentDao = Mockito.spy(new FileSystemContentDao(getIOLayerFactory().newIOServices(), getDataLayerFactory().getDataLayerConfiguration()));
    }

    @Test
    public void moveFileToVaultWhileOneWritingFailAtATimeThenRepair() throws Exception {
        File testRessourceFile1 = getTestResourceFile("1.docx");
        File testRessourceFile2 = getTestResourceFile("2.docx");
        File testRessourceFile3 = getTestResourceFile("3.docx");

        InputStream inputStream1FromFile = ioServices.newFileInputStream(testRessourceFile1, FILE_SYSTEM_CONTENT_DAO_STREAM_NAME_1);
        InputStream inputStream2FromFile = ioServices.newFileInputStream(testRessourceFile2, FILE_SYSTEM_CONTENT_DAO_STREAM_NAME_2);
        InputStream inputStream3FromFile = ioServices.newFileInputStream(testRessourceFile3, FILE_SYSTEM_CONTENT_DAO_STREAM_NAME_2);

        File tempFile1 = ioServices.newTemporaryFile(FILE_NAME_1);
        File tempFile2 = ioServices.newTemporaryFile(FILE_NAME_2);
        File tempFile3 = ioServices.newTemporaryFile(FILE_NAME_3);

        FileUtils.copyToFile(inputStream1FromFile, tempFile1);
        FileUtils.copyToFile(inputStream1FromFile, tempFile2);
        FileUtils.copyToFile(inputStream3FromFile, tempFile3);

        String fileHash1 = hashingService.getHashFromFile(tempFile1);
        String fileHash2 = hashingService.getHashFromFile(tempFile2);
        String fileHash3 = hashingService.getHashFromFile(tempFile3);

        ioServices.closeQuietly(inputStream1FromFile);
        ioServices.closeQuietly(inputStream2FromFile);
        ioServices.closeQuietly(inputStream3FromFile);

        Mockito.doReturn(false).doCallRealMethod().when(fileSystemContentDao).moveFile((File) Mockito.any(), (File) Mockito.any());
        Mockito.doCallRealMethod().doReturn(false).doReturn(false).doCallRealMethod().when(fileSystemContentDao).fileCopy((File) Mockito.any(), Mockito.anyString());

        fileSystemContentDao.moveFileToVault(tempFile1, fileHash1);
        fileSystemContentDao.moveFileToVault(tempFile2, fileHash2);
        fileSystemContentDao.moveFileToVault(tempFile3, fileHash3);

        File fileOf1Vault = fileSystemContentDao.getFileOf(fileHash1);
        File fileOf1Replicate = fileSystemContentDao.getReplicatedVaultFile(fileOf1Vault);

        File fileOf2Vault = fileSystemContentDao.getFileOf(fileHash2);
        File fileOf2Replicate = fileSystemContentDao.getReplicatedVaultFile(fileOf2Vault);

        File fileOf3Vault = fileSystemContentDao.getFileOf(fileHash3);
        File fileOf3Replicate = fileSystemContentDao.getReplicatedVaultFile(fileOf3Vault);

        assertThat(fileOf1Vault.exists()).isFalse();
        assertThat(fileOf1Replicate.exists()).isTrue();

        assertThat(fileOf2Vault.exists()).isTrue();
        assertThat(fileOf2Replicate.exists()).isFalse();

        assertThat(fileOf3Vault.exists()).isTrue();
        assertThat(fileOf3Replicate.exists()).isFalse();

        File recoveryReplicationFile = fileSystemContentDao.getReplicationRootRecoveryFile();
        BufferedReader b = ioServices.newBufferedFileReader(recoveryReplicationFile, FILE_STREAM);
        String lineRead;
        int i = 0;
        try {
            while ((lineRead = b.readLine()) != null) {
                i++;
                assertThat(lineRead).isEqualTo(fileHash1);
            }
        }
        finally {
            ioServices.closeQuietly(b);
        }

        assertThat(i).isEqualTo(1);

        File vaultRecoveryFile = fileSystemContentDao.getVaultRootRecoveryFile();
        int y = 0;
        try {
            b = ioServices.newBufferedFileReader(vaultRecoveryFile, FILE_STREAM);

            while ((lineRead = b.readLine()) != null) {
                y++;
                if(y == 1) {
                    assertThat(lineRead).isEqualTo(fileHash2);
                } else if (y == 2) {
                    assertThat(lineRead).isEqualTo(fileHash3);
                }
            }
        } finally {
            ioServices.closeQuietly(b);
        }

        assertThat(y).isEqualTo(2);

        fileSystemContentDao.readLogsAndRepairs();

        //Vérifier que les fichiers de recovery sont vide.
        int z = 0;
        try {
            b = ioServices.newBufferedFileReader(recoveryReplicationFile, FILE_STREAM);
            while ((lineRead = b.readLine()) != null) {
                if (!Strings.isNullOrEmpty(lineRead)) {
                    z++;
                }
            }
        } finally {
            ioServices.closeQuietly(b);
        }

        assertThat(z).isEqualTo(0);

        int j = 0;
        try {
            b = ioServices.newBufferedFileReader(vaultRecoveryFile, FILE_STREAM);
            while ((lineRead = b.readLine()) != null) {
                if (!Strings.isNullOrEmpty(lineRead)) {
                    j++;
                }
            }
        } finally {
            ioServices.closeQuietly(b);
        }

        assertThat(j).isEqualTo(0);

        ioServices.deleteQuietly(tempFile1);
        ioServices.deleteQuietly(tempFile2);
        ioServices.deleteQuietly(tempFile3);

        assertThat(fileOf1Vault.exists()).isTrue();
        assertThat(fileOf1Replicate.exists()).isTrue();

        assertThat(fileOf2Vault.exists()).isTrue();
        assertThat(fileOf2Replicate.exists()).isTrue();

        assertThat(fileOf3Vault.exists()).isTrue();
        assertThat(fileOf3Replicate.exists()).isTrue();
    }

    @Test
    public void moveFileToVaultAndFailToWriteThenThrow() throws Exception {
        File testRessourceFile1 = getTestResourceFile("1.docx");

        File tempFile1 = ioServices.newTemporaryFile(FILE_NAME_1);
        InputStream inputStream1FromFile = ioServices.newFileInputStream(testRessourceFile1, FILE_SYSTEM_CONTENT_DAO_STREAM_NAME_1);

        FileUtils.copyToFile(inputStream1FromFile, tempFile1);

        ioServices.closeQuietly(inputStream1FromFile);

        Mockito.doReturn(false).when(fileSystemContentDao).moveFile((File) Mockito.any(), (File) Mockito.any());
        Mockito.doReturn(false).when(fileSystemContentDao).fileCopy((File) Mockito.any(), Mockito.anyString());

        String fileHash1 = hashingService.getHashFromFile(tempFile1);

        try {
            fileSystemContentDao.moveFileToVault(tempFile1, fileHash1);
            fail("The file vault move should fail.");
        } catch (FileSystemContentDaoRuntimeException e) {
            assertThat(true).isTrue();
        } finally {
            ioServices.deleteQuietly(tempFile1);
        }
    }

    @Test
    public void addFileToVaultAndReplicationWhileWritingFailOneAtATimeThenRepair() throws Exception {
        File testRessourceFile1 = getTestResourceFile("1.docx");
        File testRessourceFile2 = getTestResourceFile("2.docx");
        File testRessourceFile3 = getTestResourceFile("3.docx");

        InputStream inputStream1FromFile = ioServices.newFileInputStream(testRessourceFile1, FILE_SYSTEM_CONTENT_DAO_STREAM_NAME_1);
        InputStream inputStream2FromFile = ioServices.newFileInputStream(testRessourceFile2, FILE_SYSTEM_CONTENT_DAO_STREAM_NAME_2);
        InputStream inputStream3FromFile = ioServices.newFileInputStream(testRessourceFile3, FILE_SYSTEM_CONTENT_DAO_STREAM_NAME_2);

        File tempFile1 = ioServices.newTemporaryFile(FILE_NAME_1);
        File tempFile2 = ioServices.newTemporaryFile(FILE_NAME_2);
        File tempFile3 = ioServices.newTemporaryFile(FILE_NAME_3);

        FileUtils.copyToFile(inputStream1FromFile, tempFile1);
        FileUtils.copyToFile(inputStream1FromFile, tempFile2);
        FileUtils.copyToFile(inputStream3FromFile, tempFile3);

        String fileHash1 = hashingService.getHashFromFile(tempFile1);
        String fileHash2 = hashingService.getHashFromFile(tempFile2);
        String fileHash3 = hashingService.getHashFromFile(tempFile3);

        ioServices.closeQuietly(inputStream1FromFile);
        ioServices.closeQuietly(inputStream2FromFile);
        ioServices.closeQuietly(inputStream3FromFile);

        Mockito.doReturn(false).doCallRealMethod()
                .doCallRealMethod().doReturn(false)
                .doCallRealMethod().doReturn(false)
                .when(fileSystemContentDao).copy((CopyInputStreamFactory) Mockito.any(), (File) Mockito.any());

        InputStream inputStream1FromTempFile = ioServices.newFileInputStream(tempFile1, FILE_SYSTEM_CONTENT_DAO_STREAM_NAME_1);
        InputStream inputStream2FromTempFile = ioServices.newFileInputStream(tempFile2, FILE_SYSTEM_CONTENT_DAO_STREAM_NAME_2);
        InputStream inputStream3FromTempFile = ioServices.newFileInputStream(tempFile3, FILE_SYSTEM_CONTENT_DAO_STREAM_NAME_2);

        fileSystemContentDao.add(fileHash1, inputStream1FromTempFile);
        fileSystemContentDao.add(fileHash2, inputStream2FromTempFile);
        fileSystemContentDao.add(fileHash3, inputStream3FromTempFile);

        File fileOf1Vault = fileSystemContentDao.getFileOf(fileHash1);
        File fileOf1Replicate = fileSystemContentDao.getReplicatedVaultFile(fileOf1Vault);

        File fileOf2Vault = fileSystemContentDao.getFileOf(fileHash2);
        File fileOf2Replicate = fileSystemContentDao.getReplicatedVaultFile(fileOf2Vault);

        File fileOf3Vault = fileSystemContentDao.getFileOf(fileHash3);
        File fileOf3Replicate = fileSystemContentDao.getReplicatedVaultFile(fileOf3Vault);

        assertThat(fileOf1Vault.exists()).isFalse();
        assertThat(fileOf1Replicate.exists()).isTrue();

        assertThat(fileOf2Vault.exists()).isTrue();
        assertThat(fileOf2Replicate.exists()).isFalse();

        assertThat(fileOf3Vault.exists()).isTrue();
        assertThat(fileOf3Replicate.exists()).isFalse();

        File recoveryReplicationFile = fileSystemContentDao.getReplicationRootRecoveryFile();
        BufferedReader b = ioServices.newBufferedFileReader(recoveryReplicationFile, FILE_STREAM);
        String lineRead;
        int i = 0;
        try {
            while ((lineRead = b.readLine()) != null) {
                i++;
                assertThat(lineRead).isEqualTo(fileHash1);
            }
        }
        finally {
            ioServices.closeQuietly(b);
        }

        assertThat(i).isEqualTo(1);

        File vaultRecoveryFile = fileSystemContentDao.getVaultRootRecoveryFile();
        int y = 0;
        try {
            b = ioServices.newBufferedFileReader(vaultRecoveryFile, FILE_STREAM);

            while ((lineRead = b.readLine()) != null) {
                y++;
                if(y == 1) {
                    assertThat(lineRead).isEqualTo(fileHash2);
                } else if (y == 2) {
                    assertThat(lineRead).isEqualTo(fileHash3);
                }
            }
        } finally {
            ioServices.closeQuietly(b);
        }

        assertThat(y).isEqualTo(2);

        fileSystemContentDao.readLogsAndRepairs();

        //Vérifier que les fichiers de recovery sont vide.
        int z = 0;
        try {
            b = ioServices.newBufferedFileReader(recoveryReplicationFile, FILE_STREAM);
            while ((lineRead = b.readLine()) != null) {
                if (!Strings.isNullOrEmpty(lineRead)) {
                    z++;
                }
            }
        } finally {
            ioServices.closeQuietly(b);
        }

        assertThat(z).isEqualTo(0);

        int j = 0;
        try {
            b = ioServices.newBufferedFileReader(vaultRecoveryFile, FILE_STREAM);
            while ((lineRead = b.readLine()) != null) {
                if (!Strings.isNullOrEmpty(lineRead)) {
                    j++;
                }
            }
        } finally {
            ioServices.closeQuietly(b);
        }

        assertThat(j).isEqualTo(0);

        ioServices.deleteQuietly(tempFile1);
        ioServices.deleteQuietly(tempFile2);
        ioServices.deleteQuietly(tempFile3);

        assertThat(fileOf1Vault.exists()).isTrue();
        assertThat(fileOf1Replicate.exists()).isTrue();

        assertThat(fileOf2Vault.exists()).isTrue();
        assertThat(fileOf2Replicate.exists()).isTrue();

        assertThat(fileOf3Vault.exists()).isTrue();
        assertThat(fileOf3Replicate.exists()).isTrue();
    }

    @Test
    public void addFileToVaultAndFailToWriteThenThrow() throws Exception {
        File testRessourceFile1 = getTestResourceFile("1.docx");

        File tempFile1 = ioServices.newTemporaryFile(FILE_NAME_1);
        InputStream inputStream1FromFile = ioServices.newFileInputStream(testRessourceFile1, FILE_SYSTEM_CONTENT_DAO_STREAM_NAME_1);

        FileUtils.copyToFile(inputStream1FromFile, tempFile1);

        ioServices.closeQuietly(inputStream1FromFile);

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

}
