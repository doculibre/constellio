//package com.constellio.data.dao.services.contents;
//
//import static com.constellio.sdk.tests.TestUtils.frenchPangram;
//import static org.assertj.core.api.Assertions.assertThat;
//
//import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Map;
//import java.util.concurrent.ConcurrentLinkedQueue;
//import java.util.concurrent.LinkedBlockingQueue;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.atomic.AtomicReference;
//
//import org.apache.tika.io.IOUtils;
//import org.junit.Before;
//import org.junit.FixMethodOrder;
//import org.junit.Test;
//import org.junit.runners.MethodSorters;
//
//import com.constellio.data.dao.services.DaoBehaviors;
//import com.constellio.data.utils.Octets;
//import com.constellio.data.utils.TwoValues;
//import com.constellio.sdk.tests.ConstellioTestWithGlobalContext;
//import com.constellio.sdk.tests.ThreadList;
//import com.constellio.sdk.tests.annotations.PerformanceTest;
//import com.constellio.sdk.tests.concurrent.ConcurrentJob;
//
//@PerformanceTest
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//public class BigVaultContentDaoPerformanceTest extends ConstellioTestWithGlobalContext {
//
//	private ContentDao vaultDao;
//
//	private final File idx50000Records = getTestResourceFile("50000files.idx");
//	private final File dat50000Records = getTestResourceFile("50000files.dat");
//
//	private static ConcurrentLinkedQueue<File> file100MoQueue = new ConcurrentLinkedQueue<File>();
//
//	private final String theParsedContent = frenchPangram();
//
//	private static File file10Mo;
//
//	private static File file1Mo;
//	private static File file10Ko;
//	private static File file100Ko;
//
//	@Test
//	public void __prepareTests__() throws Exception {
//		// for (int i = 0; i < 100; i++) {
//		// file100MoQueue.add(modifyFileSystemForAllTests().newTextFileWithSizeInKO(100 * 1024));
//		// }
//		file10Mo = modifyFileSystemForAllTests().creatingBinaryFileWithSize(Octets.megaoctets(10));
//		file1Mo = modifyFileSystemForAllTests().creatingBinaryFileWithSize(Octets.megaoctets(1));
//		file100Ko = modifyFileSystemForAllTests().creatingBinaryFileWithSize(Octets.kilooctets(100));
//		file10Ko = modifyFileSystemForAllTests().creatingBinaryFileWithSize(Octets.kilooctets(10));
//	}
//
//	LinkedBlockingQueue<TwoValues<String, InputStream>> documents;
//
//	@Test
//	public void whenSaving100FilesOf10KOThenCanHandleLoad() throws Exception {
//		for (int i = 0; i < 100; i++) {
//			InputStream stream = new BufferedInputStream(newFileInputStream(file10Ko));
//			vaultDao.add(aString(), theParsedContent, stream);
//			stream.close();
//		}
//	}
//
//	@Test
//	public void whenSaving100FilesOf100KOThenCanHandleLoad() throws Exception {
//		for (int i = 0; i < 100; i++) {
//			InputStream stream = new BufferedInputStream(newFileInputStream(file100Ko));
//			vaultDao.add(aString(), theParsedContent, stream);
//			stream.close();
//		}
//	}
//
//	@Test
//	public void whenSaving100FilesOf1MOThenCanHandleLoad() throws Exception {
//		for (int i = 0; i < 100; i++) {
//			InputStream stream = new BufferedInputStream(newFileInputStream(file1Mo));
//			vaultDao.add(aString(), theParsedContent, stream);
//			stream.close();
//		}
//	}
//
//	@Test
//	public void whenSaving100FilesOf10MOThenCanHandleLoad() throws Exception {
//		for (int i = 0; i < 100; i++) {
//			InputStream stream = new BufferedInputStream(newFileInputStream(file10Mo));
//			vaultDao.add(aString(), theParsedContent, stream);
//			stream.close();
//		}
//	}
//
//	// @Test
//	public void whenSaving10FilesOf100MOWithMultipleThreadsThenCanHandleLoad() throws Exception {
//		ThreadList<Thread> threads = new ThreadList<Thread>();
//
//		final AtomicReference<String> lastDocumentId = new AtomicReference<String>();
//		final AtomicReference<File> lastFile = new AtomicReference<File>();
//		final AtomicInteger count = new AtomicInteger();
//		for (int worker = 0; worker < 3; worker++) {
//			threads.addAndStart(new Thread() {
//				@Override
//				public void run() {
//					for (int i = 0; i < 33; i++) {
//						try {
//							File file = file100MoQueue.poll();
//							InputStream stream = new BufferedInputStream(newFileInputStream(file));
//							String id = aString();
//							vaultDao.add(id, theParsedContent, stream);
//							stream.close();
//
//							synchronized (BigVaultContentDaoPerformanceTest.this) {
//								if (count.get() == 100) {
//									lastDocumentId.set(id);
//									lastFile.set(file);
//								}
//							}
//
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
//				}
//			});
//		}
//		threads.joinAll();
//		validateContentEqualTo(lastDocumentId, lastFile.get());
//
//		// for (int i = 0; i < 100; i++) {
//		// InputStream stream = new BufferedInputStream(newFileInputStream(file100Mo));
//		// vaultDao.add(aString(), stream);
//		// stream.close();
//		// }
//	}
//
//	private void validateContentEqualTo(final AtomicReference<String> lastDocumentId, File expectedFile)
//			throws FileNotFoundException, IOException, ContentDaoException {
//		File tempFile = new File(newTempFolder(), "lastDoc.bin");
//		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tempFile));
//		IOUtils.copy(vaultDao.getContentInputStream(lastDocumentId.get()), bos);
//		bos.close();
//		assertThat(tempFile).hasContentEqualTo(expectedFile);
//	}
//
//	@Before
//	public void setup() throws Exception {
//		vaultDao = getDataLayerFactory().getContentsDao(new DaoBehaviors());
//
//		//prepareData();
//		saveNextContent();
//		saveNextContent();
//		saveNextContent();
//		vaultDao.flush();
//
//		//prepareData();
//	}
//
//	private void prepareData() throws IOException {
//		documents = new LinkedBlockingQueue<TwoValues<String, InputStream>>(1000000);
//		BigFileSplitter splitter = new BigFileSplitter();
//		splitter.split(idx50000Records, dat50000Records, new FileReadedListener() {
//
//			@Override
//			public void fileRead(String name, InputStream stream) {
//				documents.add(new TwoValues<String, InputStream>(name, stream));
//			}
//		});
//	}
//
//	private void setAutoCommit(int autoCommit) {
//		DaoBehaviors daoBehaviors = new DaoBehaviors();
//		daoBehaviors.setAutoCommitEveryXDocuments(autoCommit);
//		vaultDao = getDataLayerFactory().getContentsDao(daoBehaviors);
//	}
//
//	@Test
//	public void save24000DocsWithAutocommitOf500DocsUsing10Threads() throws Exception {
//		setAutoCommit(500);
//		runConcurrently(saveContent()).withThreads(10).untilTotalInvokationOf(24000);
//	}
//
//	@Test
//	public void save24000DocsWithAutocommitOf1000DocsUsing10Threads() throws Exception {
//		setAutoCommit(1000);
//		runConcurrently(saveContent()).withThreads(10).untilTotalInvokationOf(24000);
//	}
//
//	@Test
//	public void save24000DocsWithAutocommitOf2000DocsUsing10Threads() throws Exception {
//		setAutoCommit(2000);
//		runConcurrently(saveContent()).withThreads(10).untilTotalInvokationOf(24000);
//	}
//
//	@Test
//	public void save24000DocsWithAutocommitOf4000DocsUsing10Threads() throws Exception {
//		setAutoCommit(4000);
//		runConcurrently(saveContent()).withThreads(10).untilTotalInvokationOf(24000);
//	}
//
//	@Test
//	public void save24000DocsWithAutocommitOf12000DocsUsing10Threads() throws Exception {
//		setAutoCommit(12000);
//		runConcurrently(saveContent()).withThreads(10).untilTotalInvokationOf(24000);
//	}
//
//	@Test
//	public void save24000DocsWithAutocommitOf4000DocsUsing4Threads() throws Exception {
//		setAutoCommit(4000);
//		runConcurrently(saveContent()).withThreads(4).untilTotalInvokationOf(24000);
//	}
//
//	@Test
//	public void save500DocsWithAutocommitOf1DocsUsing1Threads() throws Exception {
//		setAutoCommit(1);
//		runConcurrently(saveContent()).withThreads(1).untilTotalInvokationOf(500);
//	}
//
//	@Test
//	public void save1000DocsWithAutocommitOf1DocsUsing100Threads() throws Exception {
//		setAutoCommit(1);
//		runConcurrently(saveContent()).withThreads(100).untilTotalInvokationOf(1000);
//	}
//
//	private ConcurrentJob saveContent() {
//		return new ConcurrentJob() {
//
//			@Override
//			public void run(Map<String, Object> context, int worker) throws Exception {
//				saveNextContent();
//			}
//		};
//	}
//
//	private void saveNextContent() throws Exception {
//		TwoValues<String, InputStream> document = documents.take();
//		String id = vaultDao.newContentId();
//		vaultDao.add(id, theParsedContent, document.getSecond());
//	}
// }
