/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.data.dao.services.contents;

import static com.constellio.sdk.tests.TestUtils.frenchPangram;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.constellio.data.dao.services.contents.ContentDaoException.ContentDaoException_NoSuchContent;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;

@RunWith(Parameterized.class)
public class ContentDaoRealTest extends ConstellioTest {

	static String givenContentDaoIsTheConfiguredOne = "givenContentDaoIsTheConfiguredOne";
	static String givenContentDaoIsTheFileSystemImpl = "givenContentDaoIsTheFileSystemImpl";

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
		return Arrays.asList(new Object[][] { { givenContentDaoIsTheConfiguredOne }, { givenContentDaoIsTheFileSystemImpl } });
	}

	@Before
	public void setup() {

		if (testCase.equals(givenContentDaoIsTheConfiguredOne)) {
			vaultDao = getDataLayerFactory().getContentsDao();

		} else if (testCase.equals(givenContentDaoIsTheFileSystemImpl)) {
			vaultDao = new FileSystemContentDao(newTempFolder(), getIOLayerFactory().newIOServices());
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

	@SlowTest
	@Test
	public void whenAddingContentsWithMultipleThreadsThenSurvive()
			throws Exception {
		vaultDao = getDataLayerFactory().getContentsDao();
		File tempFolder = createRandomTextFilesInTempFolder(100, 30 * 1024);
		final LinkedBlockingQueue<File> queue = new LinkedBlockingQueue<File>(tempFolder.list().length);
		queue.addAll(Arrays.asList(tempFolder.listFiles()));

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

}
