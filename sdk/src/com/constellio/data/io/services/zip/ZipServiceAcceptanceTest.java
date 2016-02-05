package com.constellio.data.io.services.zip;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.sdk.tests.ConstellioTest;

public class ZipServiceAcceptanceTest extends ConstellioTest {

	private File tempFolder;
	private List<File> files;

	private ZipService zipService;

	private File inexistentFile;
	private File emptyFile;
	private File validZipFile;
	private File warFile;
	private File newZipFile;
	private File aFile;
	private File anotherFileInSameFolder;
	private File aThirdFileInSameFolder;
	private File aSubFolder;
	private File aFileInDifferentFolder;
	private File anotherFileInDifferentFolder;
	private File zipFileWithInvalidExtension;
	private File aFileInSubFolder;
	private File aFolder;
	private File aFileInFolder;
	private File unzipTempFolder;
	private File unzippedAFile;
	private File unzippednotherFileInSameFolder;
	private File unzippedThirdFileInSameFolder;
	private File unzippedFolder;
	private File unzippedSubFolder;
	private File unzippedFileInFolder;
	private File unzippedFileInSubFolder;
	private File codeStyleXml;
	private File prensentationDir;
	private File constellioDevTeamPreferencesEpf;
	private File sansContenuTxt;

	// private File accentsFile;

	@Before
	public void before() {
		IOServices ioServices = new IOServices(newTempFolder());
		zipService = new ZipService(ioServices);
		files = new ArrayList<File>();
		tempFolder = newTempFolder();

		inexistentFile = new File("invalid");
		emptyFile = new File(newTempFolder(), "test.zip");
		newZipFile = new File(tempFolder, "files.zip");
		validZipFile = getTestResourceFile("1.zip");
		warFile = getTestResourceFile("calendar.warz");
		aFile = modifyFileSystem().newFileInFolder(tempFolder, "file.txt");
		anotherFileInSameFolder = modifyFileSystem().newFileInFolder(tempFolder, "file2.txt");
		aThirdFileInSameFolder = modifyFileSystem().newFileInFolder(tempFolder, "file3.txt");
		aFolder = newTempFolderInFolder(tempFolder, "aFolder");
		aSubFolder = newTempFolderInFolder(aFolder, "subFolder");
		aFileInDifferentFolder = modifyFileSystem().newTempFileInNewTempFolder("file2.txt");
		anotherFileInDifferentFolder = modifyFileSystem().newTempFileInNewTempFolder("file3.txt");
		zipFileWithInvalidExtension = new File(tempFolder, "files");
		aFileInFolder = modifyFileSystem().newFileInFolder(aFolder, "fileInFolder.txt");
		aFileInSubFolder = modifyFileSystem().newFileInFolder(aSubFolder, "fileInSubFolder.txt");
		unzipTempFolder = newTempFolder();

		unzippedAFile = new File(unzipTempFolder, "file.txt");
		unzippednotherFileInSameFolder = new File(unzipTempFolder, "file2.txt");
		unzippedThirdFileInSameFolder = new File(unzipTempFolder, "file3.txt");
		unzippedFolder = new File(unzipTempFolder, "aFolder");
		unzippedSubFolder = new File(unzippedFolder, "subFolder");
		unzippedFileInFolder = new File(unzippedFolder, "fileInFolder.txt");
		unzippedFileInSubFolder = new File(unzippedSubFolder, "fileInSubFolder.txt");
		codeStyleXml = new File(unzipTempFolder, "codeStyle.xml");
		prensentationDir = new File(unzipTempFolder, "Presentation");
		constellioDevTeamPreferencesEpf = new File(prensentationDir, "Constellio dev team preferences.epf");
		sansContenuTxt = new File(prensentationDir, "sans-contenu.txt");
		// accentsFile = new File(prensentationDir, "éèêëàâî.txt");

	}

	@Test
	public void givenEmptyFolderWhenZippingThenZipContainsFolder()
			throws Exception {
		File emptyFolder = newTempFolder();
		files.add(emptyFolder);
		zipService.zip(newZipFile, files);
		assertEquals(1, zipService.size(newZipFile));

		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(newZipFile);
			ZipEntry entry = zipFile.getEntry(emptyFolder.getName());
			assertThat(entry).isNotNull();
		} finally {
			IOUtils.closeQuietly(zipFile);
		}
	}

	@Test(expected = ZipServiceException.ZippedFilesInDifferentParentFolder.class)
	public void givenAFileInSubFolderWhenZippingThenThrowExcpetion()
			throws Exception {

		files.add(aFile);
		files.add(anotherFileInSameFolder);
		files.add(aThirdFileInSameFolder);
		files.add(aFileInSubFolder);

		zipService.zip(newZipFile, files);
	}

	@Test
	public void givenFilesAndFoldersInSameFolderWhenZippingThenFilesZippedAndUnzippedWithCorrectSize()
			throws Exception {

		files.add(aFile);
		files.add(anotherFileInSameFolder);
		files.add(aThirdFileInSameFolder);
		files.add(aFolder);

		zipService.zip(newZipFile, files);

		assertEquals(5, zipService.size(newZipFile));
		assertTrue(zipService.contains(newZipFile, aFileInFolder.getPath()));
		assertTrue(zipService.contains(newZipFile, aFileInSubFolder.getPath()));

		zipService.unzip(newZipFile, unzipTempFolder);

		assertTrue(unzippedAFile.exists());
		assertTrue(unzippednotherFileInSameFolder.exists());
		assertTrue(unzippedThirdFileInSameFolder.exists());
		assertTrue(unzippedFolder.exists());
		assertTrue(unzippedSubFolder.exists());

		assertTrue(unzippedFileInFolder.exists());
		assertTrue(unzippedFileInSubFolder.exists());

		assertEquals(aFile.length(), unzippedAFile.length());
		assertEquals(anotherFileInSameFolder.length(), unzippednotherFileInSameFolder.length());
		assertEquals(aThirdFileInSameFolder.length(), unzippedThirdFileInSameFolder.length());

		assertEquals(aFileInFolder.length(), unzippedFileInFolder.length());
		assertEquals(aFileInSubFolder.length(), unzippedFileInSubFolder.length());

	}

	@Test(expected = ZipServiceException.ZippedFilesInDifferentParentFolder.class)
	public void givenFilesInDifferentFolderWhenZippingThenThrowException()
			throws ZipServiceException, IOException {

		files.add(aFile);
		files.add(aFileInDifferentFolder);
		files.add(anotherFileInDifferentFolder);

		zipService.zip(newZipFile, files);
	}

	@Test
	public void givenFilesInSameFolderWhenZippingThenFilesZipped()
			throws Exception {

		files.add(aFile);
		files.add(anotherFileInSameFolder);
		files.add(aThirdFileInSameFolder);

		zipService.zip(newZipFile, files);
		assertEquals(3, zipService.size(newZipFile));
	}

	@Test(expected = ZipServiceException.FileToZipNotFound.class)
	public void givenInexistentFileToZipWhenZippingThenThrowException()
			throws Exception {

		files.add(inexistentFile);

		zipService.zip(newZipFile, files);
	}

	@Test(expected = ZipServiceException.ZipFileInvalidExtension.class)
	public void givenZipFileWithAInvalidExtensionWhenZippingThenThrowException()
			throws Exception {

		files.add(aFile);

		zipService.zip(zipFileWithInvalidExtension, files);
	}

	private File newInvalidZip()
			throws IOException {
		File invalidFile = new File(newTempFolder(), "test.zip");
		FileUtils.writeStringToFile(invalidFile, "this is not a zip file");
		return invalidFile;
	}

	@Test(expected = ZipServiceException.ZipFileHasNoContent.class)
	public void whenUnzippingEmptyFileThenThrowException()
			throws Exception {

		FileUtils.touch(emptyFile);

		zipService.unzip(emptyFile, newTempFolder());

	}

	@Test(expected = ZipServiceException.ZipFileCannotBeParsed.class)
	public void whenUnzippingInvalidFileThenThrowException()
			throws Exception {

		zipService.unzip(newInvalidZip(), newTempFolder());
	}

	@Test(expected = ZipServiceException.ZipFileNotFound.class)
	public void whenUnzippingUnfoundFileThenThrowException()
			throws Exception {

		zipService.unzip(inexistentFile, newTempFolder());
	}

	@Test()
	public void whenUnzippingValidZipFileThenUnzipCorrectly()
			throws Exception {

		zipService.unzip(validZipFile, unzipTempFolder);

		assertTrue(codeStyleXml.exists());
		assertTrue(prensentationDir.exists());
		assertTrue(constellioDevTeamPreferencesEpf.exists());
		assertTrue(sansContenuTxt.exists());
		// assertTrue(accentsFile.exists());

		assertEquals(30879L, codeStyleXml.length());
		assertEquals(33186L, constellioDevTeamPreferencesEpf.length());
		assertEquals(0L, sansContenuTxt.length());
		// assertEquals(4l, accentsFile.length());

	}

	@Test()
	public void whenUnzippingWarFileThenUnzipCorrectly()
			throws Exception {

		zipService.unzip(warFile, unzipTempFolder);
		File zipFile = getTestResourceFile("calendar.warz");
		File unzipFolder = newTempFolder();
		zipService.unzip(zipFile, unzipFolder);

		File file2600DDD38C65AB34515F38886564BB70CacheJs = new File(unzipFolder, "2600DDD38C65AB34515F38886564BB70.cache.js");
		File fileEB0FB65EB2508676B3068F7043DF88FCCacheXml = new File(unzipFolder, "EB0FB65EB2508676B3068F7043DF88FC.cache.xml");
		File webInf = new File(unzipFolder, "WEB-INF");
		File classes = new File(webInf, "classes");
		File gridExample = new File(classes, "GridExample.class");

		assertTrue(file2600DDD38C65AB34515F38886564BB70CacheJs.exists());
		assertTrue(fileEB0FB65EB2508676B3068F7043DF88FCCacheXml.exists());
		assertTrue(webInf.exists());
		assertTrue(classes.exists());
		assertTrue(gridExample.exists());

		assertEquals(46217L, file2600DDD38C65AB34515F38886564BB70CacheJs.length());
		assertEquals(1191L, fileEB0FB65EB2508676B3068F7043DF88FCCacheXml.length());
		assertEquals(1069L, gridExample.length());

	}
}
