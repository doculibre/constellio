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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class FileSystemContentDaoAcceptanceTest extends ConstellioTest {
    public static final String FILE_NAME_1 = "FileName1.docx";
    public static final String FILE_NAME_2 = "FileName2.docx";
    public static final String FILE_NAME_3 = "FileName3.docx";

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
    public void givenVaultWihoutReplicationWhenMoveFileFailThenThrow() throws Exception {
        getDataLayerFactory().getDataLayerConfiguration().setContentDaoReplicatedVaultMountPoint(null);
        fileSystemContentDao = Mockito.spy(new FileSystemContentDao(getIOLayerFactory().newIOServices(), getDataLayerFactory().getDataLayerConfiguration()));

        Mockito.doReturn(false).when(fileSystemContentDao).moveFile((File) Mockito.any(), (File) Mockito.any());

        File tempFile1 = ioServices.newTemporaryFile(FILE_NAME_1);
        FileUtils.copyToFile(getTestResourceInputStream("1.docx"), tempFile1);

        try {
            String fileHash1 = hashingService.getHashFromFile(tempFile1);
            fileSystemContentDao.moveFileToVault(tempFile1, fileHash1);

            fail("Une exception doit être levé.");
        }
        catch(FileSystemContentDaoRuntimeException e) {
            // Ok Exception levé
        } finally {
            ioServices.deleteQuietly(tempFile1);
        }
    }

    @Test
    public void givenMoveFileToVaultWithErrorsAddedToRecoveryFilesWhenReadLogAndRepairThenAllFileArePresent() throws Exception {

        File  tempFile1, tempFile2, tempFile3;
        FileUtils.copyFile(getTestResourceFile("1.docx"), tempFile1=ioServices.newTemporaryFile(FILE_NAME_1));
        FileUtils.copyFile(getTestResourceFile("2.docx"), tempFile2=ioServices.newTemporaryFile(FILE_NAME_2));
        FileUtils.copyFile(getTestResourceFile("3.docx"), tempFile3=ioServices.newTemporaryFile(FILE_NAME_3));
        String fileHash1 = hashingService.getHashFromFile(tempFile1);
        String fileHash2 = hashingService.getHashFromFile(tempFile2);
        String fileHash3 = hashingService.getHashFromFile(tempFile3);

        Mockito.doReturn(false).doCallRealMethod().when(fileSystemContentDao)
                .moveFile((File) Mockito.any(), (File) Mockito.any());
        Mockito.doCallRealMethod().doReturn(false).doReturn(false).doCallRealMethod()
                .when(fileSystemContentDao).fileCopy((File) Mockito.any(), Mockito.anyString());

        fileSystemContentDao.moveFileToVault(tempFile1, fileHash1);
        fileSystemContentDao.moveFileToVault(tempFile2, fileHash2);
        fileSystemContentDao.moveFileToVault(tempFile3, fileHash3);

        ioServices.deleteQuietly(tempFile1);
        ioServices.deleteQuietly(tempFile2);
        ioServices.deleteQuietly(tempFile3);

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

        assertThatRecoveryFilesHaveCertainValues(fileHash1, fileHash2, fileHash3);

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
            fileSystemContentDao.moveFileToVault(tempFile1, fileHash1);
            fail("The file vault move should fail.");
        } catch (FileSystemContentDaoRuntimeException e) {
            // Ok the exception is expected.
        } finally {
            ioServices.deleteQuietly(tempFile1);
        }
    }

    @Test
    public void givenAddFileToVaultWithErrorsAddedToRecoveryFilesWhenReadLogAndRepairThenAllFileArePresent() throws Exception {

        String fileHash1 = hashingService.getHashFromFile(getTestResourceFile("1.docx"));
        String fileHash2 = hashingService.getHashFromFile(getTestResourceFile("2.docx"));
        String fileHash3 = hashingService.getHashFromFile(getTestResourceFile("3.docx"));

        Mockito.doReturn(false).doCallRealMethod()
                .doCallRealMethod().doReturn(false)
                .doCallRealMethod().doReturn(false)
                .when(fileSystemContentDao).copy((CopyInputStreamFactory) Mockito.any(), (File) Mockito.any());

        fileSystemContentDao.add(fileHash1, getTestResourceInputStream("1.docx"));
        fileSystemContentDao.add(fileHash2, getTestResourceInputStream("2.docx"));
        fileSystemContentDao.add(fileHash3, getTestResourceInputStream("3.docx"));

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

        assertThatRecoveryFilesHaveCertainValues(fileHash1, fileHash2, fileHash3);

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

    private int getNumberOfNonEmptyLines(String filePath) throws IOException {
        List<String> lineList = Files.readAllLines(
                Paths.get(filePath)
                , StandardCharsets.UTF_8);

        int numberOfNonEmptyLine = 0;
        for(String line : lineList) {
            if(Strings.isNullOrEmpty(line)){
                numberOfNonEmptyLine++;
            }
        }

        return numberOfNonEmptyLine;
    }

    private void assertThatRecoveryFileAreEmpty() throws IOException {
        assertThat(getNumberOfNonEmptyLines(fileSystemContentDao.getReplicationRootRecoveryFile()
                .getAbsolutePath())).isEqualTo(0);

        assertThat(getNumberOfNonEmptyLines(fileSystemContentDao.getVaultRootRecoveryFile()
                .getAbsolutePath())).isEqualTo(0);
    }

    private void assertThatRecoveryFilesHaveCertainValues(String fileHash1, String fileHash2, String fileHash3) throws IOException {
        List<String> lineListOfReplicationRootRevoveryFile = Files.readAllLines(
                Paths.get(fileSystemContentDao.getReplicationRootRecoveryFile().getAbsolutePath())
                , StandardCharsets.UTF_8);

        assertThat(lineListOfReplicationRootRevoveryFile.get(0)).isEqualTo(fileHash1);
        assertThat(lineListOfReplicationRootRevoveryFile.size()).isEqualTo(1);

        List<String> lineListOfVaultRootRevoveryFile = Files.readAllLines(
                Paths.get(fileSystemContentDao.getVaultRootRecoveryFile().getAbsolutePath())
                , StandardCharsets.UTF_8);

        assertThat(lineListOfVaultRootRevoveryFile.get(0)).isEqualTo(fileHash2);
        assertThat(lineListOfVaultRootRevoveryFile.get(1)).isEqualTo(fileHash3);
        assertThat(lineListOfVaultRootRevoveryFile.size()).isEqualTo(2);
    }
}
