package com.constellio.data.dao.services.contents;

import com.constellio.data.conf.DigitSeparatorMode;
import com.constellio.data.dao.services.contents.ContentDaoException.ContentDaoException_NoSuchContent;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import static com.constellio.data.conf.HashingEncoding.BASE64_URL_ENCODED;
import static com.constellio.sdk.tests.TestUtils.frenchPangram;
import static java.io.File.separator;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class ContentDaoRealTest extends ConstellioTest {

	static String givenContentDaoIsTheConfiguredOne = "givenContentDaoIsTheConfiguredOne";
	static String givenContentDaoIsTheFileSystemImpl = "givenContentDaoIsTheFileSystemImpl";
	static String givenContentDaoWithReplication = "givenContentDaoWithReplication";
	static String givenContentDaoWithSubvaults = "givenContentDaoWithSubvaults";

	String theContent = aString();

	String theId = aString() + aString() + aString() + aString();

	String theParsedContent = frenchPangram();

	ContentDao vaultDao;

	String testCase;

	public ContentDaoRealTest(String testCase) {
		this.testCase = testCase;
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> testCases() {
		return asList(new Object[][]{{givenContentDaoIsTheConfiguredOne}, {givenContentDaoIsTheFileSystemImpl}, {givenContentDaoWithReplication}, {givenContentDaoWithSubvaults}});
	}

	@Before
	public void setup() {

		if (testCase.equals(givenContentDaoIsTheConfiguredOne)) {
			vaultDao = getDataLayerFactory().getContentsDao();

		} else if (testCase.equals(givenContentDaoIsTheFileSystemImpl)) {
			getDataLayerFactory().getDataLayerConfiguration().setContentDaoFileSystemFolder(newTempFolder());
			vaultDao = new FileSystemContentDao(getDataLayerFactory());

		} else if (testCase.equals(givenContentDaoWithReplication)) {
			getDataLayerFactory().getDataLayerConfiguration().setContentDaoReplicatedVaultMountPoint(newTempFolder().getAbsolutePath());
			vaultDao = new FileSystemContentDao(getDataLayerFactory());

		} else if (testCase.equals(givenContentDaoWithSubvaults)) {
			vaultDao = new FileSystemContentDao(getDataLayerFactory(), asList("subvault1", "subvault2"));

		}
	}

	@Test
	public void givenContentDaoConfiguredWithTwoDigitSeperatorThenOK()
			throws Exception {

		getDataLayerFactory().getDataLayerConfiguration()
				.setContentDaoFileSystemDigitsSeparatorMode(DigitSeparatorMode.TWO_DIGITS);

		assertThat(getDataLayerFactory().getDataLayerConfiguration().getContentDaoFileSystemDigitsSeparatorMode())
				.isEqualTo(DigitSeparatorMode.TWO_DIGITS);

		vaultDao.add("anIdWithoutSlash", newInputStreamOfTextContent("test1"));
		vaultDao.add("anIdWithA/Slash", newInputStreamOfTextContent("test2"));
		vaultDao.add("z+", newInputStreamOfTextContent("test3"));
		vaultDao.add("z+q=1", newInputStreamOfTextContent("test4"));
		vaultDao.add("z+q", newInputStreamOfTextContent("test5"));
		vaultDao.add("z+/z+q=1k", newInputStreamOfTextContent("test6"));

		assertThat(vaultDao.getContentInputStream("anIdWithoutSlash", SDK_STREAM)).hasContentEqualTo(
				newInputStreamOfTextContent("test1"));

		assertThat(vaultDao.getContentInputStream("anIdWithA/Slash", SDK_STREAM)).hasContentEqualTo(
				newInputStreamOfTextContent("test2"));

		assertThat(vaultDao.getContentInputStream("z+", SDK_STREAM)).hasContentEqualTo(
				newInputStreamOfTextContent("test3"));

		assertThat(vaultDao.getContentInputStream("z+q=1", SDK_STREAM)).hasContentEqualTo(
				newInputStreamOfTextContent("test4"));

		assertThat(vaultDao.getContentInputStream("z+q", SDK_STREAM)).hasContentEqualTo(
				newInputStreamOfTextContent("test5"));
		assertThat(vaultDao.getContentInputStream("z+/z+q=1k", SDK_STREAM)).hasContentEqualTo(
				newInputStreamOfTextContent("test6"));

		if (vaultDao instanceof FileSystemContentDao) {
			File root = ((FileSystemContentDao) vaultDao).rootFolder;
			assertThat(root.listFiles()).extracting("name", "file").containsOnly(
					tuple("an", false),
					tuple("anIdWithA", false),
					tuple("z+", false),
					tuple("vaultrecoveryfolder", false)
			);

			assertThat(new File(root, "an").listFiles()).extracting("name", "file").containsOnly(
					tuple("anIdWithoutSlash", true)
			);

			assertThat(new File(root, "anIdWithA").listFiles()).extracting("name", "file").containsOnly(
					tuple("Slash", true)
			);

			assertThat(new File(root, "z+").listFiles()).extracting("name", "file").containsOnly(
					tuple("z+q=1", true),
					tuple("z+q=1k", true),
					tuple("z+q", true),
					tuple("z+", true)
			);
		}
	}

	@Test
	public void givenContentDaoConfiguredWithThreeOneDigitSeperatorThenOK()
			throws Exception {

		getDataLayerFactory().getDataLayerConfiguration()
				.setHashingEncoding(BASE64_URL_ENCODED);

		getDataLayerFactory().getDataLayerConfiguration()
				.setContentDaoFileSystemDigitsSeparatorMode(DigitSeparatorMode.THREE_LEVELS_OF_ONE_DIGITS);

		vaultDao.add("anIdWithoutSlash", newInputStreamOfTextContent("test1"));
		vaultDao.add("anotherId", newInputStreamOfTextContent("test2"));
		vaultDao.add("z", newInputStreamOfTextContent("test4"));
		vaultDao.add("zz", newInputStreamOfTextContent("test5"));
		vaultDao.add("zzz", newInputStreamOfTextContent("test6"));
		vaultDao.add("zzzz", newInputStreamOfTextContent("test7"));

		assertThat(vaultDao.getContentInputStream("anIdWithoutSlash", SDK_STREAM)).hasContentEqualTo(
				newInputStreamOfTextContent("test1"));

		assertThat(vaultDao.getContentInputStream("anotherId", SDK_STREAM)).hasContentEqualTo(
				newInputStreamOfTextContent("test2"));

		assertThat(vaultDao.getContentInputStream("z", SDK_STREAM)).hasContentEqualTo(
				newInputStreamOfTextContent("test4"));

		assertThat(vaultDao.getContentInputStream("zz", SDK_STREAM)).hasContentEqualTo(
				newInputStreamOfTextContent("test5"));
		assertThat(vaultDao.getContentInputStream("zzz", SDK_STREAM)).hasContentEqualTo(
				newInputStreamOfTextContent("test6"));
		assertThat(vaultDao.getContentInputStream("zzzz", SDK_STREAM)).hasContentEqualTo(
				newInputStreamOfTextContent("test7"));

		if (vaultDao instanceof FileSystemContentDao) {
			File root = ((FileSystemContentDao) vaultDao).rootFolder;
			assertThat(root.listFiles()).extracting("name", "file").containsOnly(
					tuple("a", false),
					tuple("vaultrecoveryfolder", false),
					tuple("z", false)
			);

			assertThat(new File(root, "a").listFiles()).extracting("name", "file").containsOnly(
					tuple("an", false)
			);

			assertThat(new File(root, "a" + separator + "an").listFiles()).extracting("name", "file").containsOnly(
					tuple("anI", false),
					tuple("ano", false)
			);

			assertThat(new File(root, "a" + separator + "an" + separator + "anI").listFiles()).extracting("name", "file")
					.containsOnly(tuple("anIdWithoutSlash", true));

			assertThat(new File(root, "a" + separator + "an" + separator + "ano").listFiles()).extracting("name", "file")
					.containsOnly(tuple("anotherId", true));

			assertThat(new File(root, "z").listFiles()).extracting("name", "file").containsOnly(
					tuple("z", true),
					tuple("zz", false)
			);

			assertThat(new File(root, "z" + separator + "zz").listFiles()).extracting("name", "file").containsOnly(
					tuple("zz", true),
					tuple("zzz", false)
			);

			assertThat(new File(root, "z" + separator + "zz" + separator + "zzz").listFiles()).extracting("name", "file")
					.containsOnly(tuple("zzz", true), tuple("zzzz", true));

		}
	}

	@Test
	public void whenAddingRegularRecordContentThenCanBeRetreivedWithHisNewId()
			throws IOException, ContentDaoException {
		InputStream inputStreamFromTestFile = newFileInputStream(getTestResourceFile("image.png"));

		vaultDao.add(theId, inputStreamFromTestFile);
		inputStreamFromTestFile.close();

		IOServices ioServices = getIOLayerFactory().newIOServices();
		byte[] bytesFromVault = ioServices.readBytes(vaultDao.getContentInputStream(theId, SDK_STREAM));
		byte[] bytesFromTestFile = ioServices.readBytes(newFileInputStream(getTestResourceFile("image.png")));
		assertThat(bytesFromVault).isEqualTo(bytesFromTestFile);
	}

	@Test(expected = ContentDaoException_NoSuchContent.class)
	public void givenContentDoesNotExistWhenGetInputStreamThenThrowNoSuchContent()
			throws IOException, ContentDaoException {

		vaultDao.getContentInputStream("anInvalidId", SDK_STREAM);
	}

	@Test
	public void whenAddingSmallRecordContentThenCanBeRetreivedWithHisId()
			throws IOException, ContentDaoException {
		theId = "a/b/c.txt";
		InputStream theContentInputStream = newInputStreamOfTextContent(theContent);

		vaultDao.add(theId, theContentInputStream);

		InputStream inputStream = vaultDao.getContentInputStream(theId, SDK_STREAM);
		String theOutputContent = new FileService(null).readStreamToStringWithoutExpectableIOException(inputStream);
		assertEquals(theContent, theOutputContent);

		if (vaultDao instanceof FileSystemContentDao) {
			FileSystemContentDao fileSystemContentDao = (FileSystemContentDao) vaultDao;
			File folderA = new File(fileSystemContentDao.rootFolder, "a");
			File folderB = new File(folderA, "b");
			File fileC = new File(folderB, "c.txt");
			assertThat(folderA).exists();
			assertThat(folderB).exists();
			assertThat(fileC).exists();
		}
	}

	@Test
	public void givenContentsInFolderWhenGetFolderContentsThenReturnValidContent()
			throws IOException, ContentDaoException {
		vaultDao.add("a/b/c.txt", newInputStreamOfTextContent(theContent));
		vaultDao.add("a/b/e.txt", newInputStreamOfTextContent(theContent));
		vaultDao.add("a/d.txt", newInputStreamOfTextContent(theContent));
		vaultDao.add("a/f.txt", newInputStreamOfTextContent(theContent));

		assertThat(vaultDao.getFolderContents("a")).containsOnly("a/b", "a/d.txt", "a/f.txt");
		assertThat(vaultDao.getFolderContents("a/b")).containsOnly("a/b/c.txt", "a/b/e.txt");
		assertThat(vaultDao.getFolderContents("t")).isEmpty();

	}

	@Test
	public void whenAddingRecordContentUsingAnIdWithASlashThenCanBeRetreivedWithHisId()
			throws IOException, ContentDaoException {
		InputStream theContentInputStream = newInputStreamOfTextContent(theContent);

		vaultDao.add(theId, theContentInputStream);

		InputStream inputStream = vaultDao.getContentInputStream(theId, SDK_STREAM);
		String theOutputContent = new FileService(null).readStreamToStringWithoutExpectableIOException(inputStream);
		assertEquals(theContent, theOutputContent);
	}

	// Confirm @SlowTest
	@Test
	public void whenAddingContentsWithMultipleThreadsThenSurvive()
			throws Exception {
		vaultDao = getDataLayerFactory().getContentsDao();
		File tempFolder = createRandomTextFilesInTempFolder(100, 30 * 1024);
		final LinkedBlockingQueue<File> queue = new LinkedBlockingQueue<File>(tempFolder.list().length);
		queue.addAll(asList(tempFolder.listFiles()));

		final Map<File, String> synchronizedMap = java.util.Collections.synchronizedMap(new HashMap<File, String>());

		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < 10; i++) {
			threads.add(new Thread() {
				@Override
				public void run() {
					File nextFile;

					while ((nextFile = queue.poll()) != null) {
						InputStream stream = null;
						try {
							String contentId = UUIDV1Generator.newRandomId();
							stream = newFileInputStream(nextFile);
							vaultDao.add(contentId, stream);
							synchronizedMap.put(nextFile, contentId);
						} catch (Exception e) {
							e.printStackTrace();

						} finally {
							IOUtils.closeQuietly(stream);
						}

					}

				}
			});
		}

		for (Thread t : threads) {
			t.start();
		}

		for (Thread t : threads) {
			t.join();
		}

		for (Map.Entry<File, String> entry : synchronizedMap.entrySet()) {
			InputStream expectedInputStream = null;
			InputStream savedInputStream = null;
			try {
				expectedInputStream = newFileInputStream(entry.getKey());
				savedInputStream = vaultDao.getContentInputStream(entry.getValue(), SDK_STREAM);
				assertInputStreamEquals(expectedInputStream, savedInputStream);
			} finally {
				IOUtils.closeQuietly(expectedInputStream);
				IOUtils.closeQuietly(savedInputStream);
			}
		}
	}

	private InputStream newInputStreamOfTextContent(String aContent)
			throws IOException {
		File file = newTempFileWithContent("tempFile.txt", aContent);
		return newFileInputStream(file);
	}

	private InputStream newFailingInputStreamOfTextContent(String aContent)
			throws IOException {
		throw new FileSystemContentDaoRuntimeException.FileSystemContentDaoRuntimeException_DatastoreFailure(new IOException("fail"));
	}

	@Test
	public void testMoveFileToVault() throws Exception {
		// Given
		File fileToMove = newTempFileWithContent("fileToMove.txt", theContent);

		//When
		vaultDao.moveFileToVault(fileToMove, theId);

		// Then
		assertThat(listFilesRecursively(((FileSystemContentDao) vaultDao).rootFolder.toPath())).isNotEmpty();
		if (((FileSystemContentDao) vaultDao).replicatedRootFolder != null) {
			assertThat(listFilesRecursively(((FileSystemContentDao) vaultDao).replicatedRootFolder.toPath())).isNotEmpty();
		}
	}

	@Test
	public void testMoveFileToVault_FailureOnReplicatedVaultPrimaryMountPoint() throws Exception {
		// Given
		File fileToMove = newTempFileWithContent("fileToMove.txt", theContent);
		// And forced failure
		FileUtils.deleteQuietly(fileToMove);

		try {
			//When
			vaultDao.moveFileToVault(fileToMove, theId);
			// Then
			fail("FileSystemContentDaoRuntimeException_DatastoreFailure expected but not thrown !");
		} catch (Exception e) {
			// Then
			if (((FileSystemContentDao) vaultDao).replicatedRootFolder != null) {
				assertThat(e).isInstanceOf(FileSystemContentDaoRuntimeException.FileSystemContentDaoRuntimeException_FailedToWriteVaultAndReplication.class);
			} else {
				assertThat(e).isInstanceOf(FileSystemContentDaoRuntimeException.FileSystemContentDaoRuntimeException_FailedToWriteVault.class);
			}

			assertVaultAndReplicationVaultAreEmpty();
		}
	}

	private List<Path> listFilesRecursively(Path path) throws IOException {
		final List<Path> files = new ArrayList<>();

		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				files.add(file);
				return FileVisitResult.CONTINUE;
			}
		});

		return files;
	}

	@Test
	public void testAdd() throws Exception {
		//When
		vaultDao.add(theId, newInputStreamOfTextContent(theContent));

		// Then
		assertThat(listFilesRecursively(((FileSystemContentDao) vaultDao).rootFolder.toPath())).isNotEmpty();
		if (((FileSystemContentDao) vaultDao).replicatedRootFolder != null) {
			assertThat(listFilesRecursively(((FileSystemContentDao) vaultDao).replicatedRootFolder.toPath())).isNotEmpty();
		}
	}

	@Test
	public void testAdd_FailureOnReplicatedVaultPrimaryMountPoint() throws Exception {
		try {
			//When
			vaultDao.add(theId, newFailingInputStreamOfTextContent(theContent));
			// Then
			fail("FileSystemContentDaoRuntimeException_DatastoreFailure expected but not thrown !");
		} catch (Exception e) {
			// Then
			assertThat(e).isInstanceOf(FileSystemContentDaoRuntimeException.FileSystemContentDaoRuntimeException_DatastoreFailure.class);

			assertVaultAndReplicationVaultAreEmpty();
		}
	}

	@Test
	public void testGetContentInputStream_FailureOnReplicatedVaultPrimaryMountPoint() throws Exception {
		// Given
		vaultDao.add(theId, newInputStreamOfTextContent(theContent));
		// And forced failure
		FileUtils.deleteQuietly((((FileSystemContentDao) vaultDao).rootFolder));

		// When
		InputStream inputStream = null;
		try {
			inputStream = vaultDao.getContentInputStream(theId, SDK_STREAM);

			if (((FileSystemContentDao) vaultDao).replicatedRootFolder == null) {
				fail("ContentDaoException_NoSuchContent expected but not thrown !");
			} else {
				assertThat(inputStream).isNotNull();
			}
		} catch (Exception e) {
			if (((FileSystemContentDao) vaultDao).replicatedRootFolder == null) {
				assertThat(e).isInstanceOf(ContentDaoException_NoSuchContent.class);
			} else {
				fail("No exception is expected !");
			}
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	@Test
	public void testGetContentLength_FailureOnReplicatedVaultPrimaryMountPoint() throws Exception {
		// Given
		vaultDao.add(theId, newInputStreamOfTextContent(theContent));
		// And forced failure
		FileUtils.deleteQuietly((((FileSystemContentDao) vaultDao).rootFolder));

		// When
		long length = vaultDao.getContentLength(theId);

		// Then
		if (((FileSystemContentDao) vaultDao).replicatedRootFolder == null) {
			assertThat(length).isEqualTo(0);
		} else {
			assertThat(length).isGreaterThan(0);
		}
	}

	@Test
	public void testIsDocumentExisting_FailureOnReplicatedVaultPrimaryMountPoint() throws Exception {
		// Given
		vaultDao.add(theId, newInputStreamOfTextContent(theContent));
		// And forced failure
		FileUtils.deleteQuietly((((FileSystemContentDao) vaultDao).rootFolder));

		// When
		boolean exists = vaultDao.isDocumentExisting(theId);

		// Then
		if (((FileSystemContentDao) vaultDao).replicatedRootFolder == null) {
			assertThat(exists).isFalse();
		} else {
			assertThat(exists).isTrue();
		}
	}

	@Test
	public void testDelete() throws Exception {
		// Given
		vaultDao.add(theId, newInputStreamOfTextContent(theContent));

		// When
		vaultDao.delete(asList(theId));
		assertVaultAndReplicationVaultAreEmpty();


	}

	@Test
	public void testGetFromSubvault() throws Exception {

		File vaultFile1WhichDoesNotExist = vaultDao.getFileOf("ABCDEFILE0001");
		File vaultFile2WhichDoesNotExist = vaultDao.getFileOf("ABCDEFILE0002");
		assertThat(vaultFile1WhichDoesNotExist.exists()).isFalse();
		assertThat(vaultFile2WhichDoesNotExist.exists()).isFalse();

		File vault = getDataLayerFactory().getDataLayerConfiguration().getContentDaoFileSystemFolder();
		File subvault1 = new File(vault, "subvault1");
		File subvault2 = new File(vault, "subvault2");

		FileUtils.write(new File(subvault1, "A/AB/ABC/ABCDEFILE0001".replace("/", separator)), "thisIsTheFile1");
		FileUtils.write(new File(subvault2, "A/AB/ABC/ABCDEFILE0002".replace("/", separator)), "thisIsTheFile2");

		File subvaultFile1 = vaultDao.getFileOf("ABCDEFILE0001");
		File subvaultFile2 = vaultDao.getFileOf("ABCDEFILE0002");
		if (testCase.equals(givenContentDaoWithSubvaults)) {

			assertThat(subvaultFile1.exists()).isTrue();
			assertThat(subvaultFile2.exists()).isTrue();
			assertThat(subvaultFile1.getAbsolutePath()).isNotEqualTo(vaultFile1WhichDoesNotExist.getAbsolutePath());
			assertThat(subvaultFile2.getAbsolutePath()).isNotEqualTo(vaultFile2WhichDoesNotExist.getAbsolutePath());


		} else {
			assertThat(subvaultFile1.exists()).isFalse();
			assertThat(subvaultFile2.exists()).isFalse();
			assertThat(subvaultFile1.getAbsolutePath()).isEqualTo(vaultFile1WhichDoesNotExist.getAbsolutePath());
			assertThat(subvaultFile2.getAbsolutePath()).isEqualTo(vaultFile2WhichDoesNotExist.getAbsolutePath());

		}


	}

	@Test
	public void testDeleteFromSubvault() {

	}

	private void assertVaultAndReplicationVaultAreEmpty() throws IOException {
		// Then
		List<Path> filePresentInRootFolder = listFilesRecursively(((FileSystemContentDao) vaultDao).rootFolder.toPath());
		assertThat(filePresentInRootFolder.size()).isEqualTo(0);
		if (((FileSystemContentDao) vaultDao).replicatedRootFolder != null) {
			assertThat(listFilesRecursively(((FileSystemContentDao) vaultDao).replicatedRootFolder.toPath())).isEmpty();
		}
	}
}
