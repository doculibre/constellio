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
package com.constellio.sdk.tests;

import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasInExceptEvents;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.client.WebTarget;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.junitbenchmarks.BenchmarkOptionsSystemProperties;
import com.constellio.app.client.services.AdminServicesSession;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.tools.ServerThrowableContext;
import com.constellio.app.ui.tools.vaadin.TestContainerButtonListener;
import com.constellio.app.ui.tools.vaadin.TestEnterViewListener;
import com.constellio.app.ui.tools.vaadin.TestInitUIListener;
import com.constellio.client.cmis.client.CmisSessionBuilder;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.ConsoleLogger;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.TimeProvider.DefaultTimeProvider;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.FailureDetectionTestWatcher.FailureDetectionTestWatcherListener;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.concurrent.ConcurrentJob;
import com.constellio.sdk.tests.concurrent.OngoingConcurrentExecution;
import com.constellio.sdk.tests.schemas.SchemaTestFeatures;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

public abstract class AbstractConstellioTest implements FailureDetectionTestWatcherListener {

	public static final String SDK_STREAM = StreamsTestFeatures.SDK_STREAM;

	public static SDKPropertiesLoader sdkPropertiesLoader = new SDKPropertiesLoader();
	private static Logger LOGGER;
	private static boolean batchProcessControllerStarted = false;
	private static String[] notUnitTestSuffix = new String[] { "AcceptanceTest", "IntegrationTest", "RealTest", "LoadTest",
			"StressTest", "PerformanceTest", "AcceptTest" };
	private static Map<String, Long> previousMemoryUsage = new HashMap<>();
	// CHECKSTYLE:OFF
	// Junit requires this field to be public
	@Rule public SkipTestsRule skipTestRule;

	@Rule public FailureDetectionTestWatcher failureDetectionTestWatcher = new FailureDetectionTestWatcher(this);

	protected Map<String, String> sdkProperties = new HashMap<String, String>();

	protected String zeCollection = "zeCollection";
	protected String admin = "admin";
	protected String aliceWonderland = "alice";
	protected String bobGratton = "bob";
	protected String chuckNorris = "chuck";
	protected String charlesFrancoisXavier = "charles";
	protected String dakota = "dakota";
	protected String edouard = "edouard";
	protected String gandalf = "gandalf";
	protected String robin = "robin";
	protected String sasquatch = "sasquatch";

	private int printIndex = 0;
	private long time;

	public AbstractConstellioTest() {
		sdkPropertiesLoader.setLocked(isUnitTest());
		sdkProperties = sdkPropertiesLoader.getSDKProperties();
		skipTestRule = new SkipTestsRule(sdkPropertiesLoader, isUnitTest(getClass().getSimpleName()));
		ConsoleLogger.GLOBAL_PREFIX = getClass().getSimpleName();
	}

	@BeforeClass
	public static void beforeClass()
			throws Exception {

		LOGGER = null;

		System.setProperty(BenchmarkOptionsSystemProperties.BENCHMARK_ROUNDS_PROPERTY, "1");
		System.setProperty(BenchmarkOptionsSystemProperties.WARMUP_ROUNDS_PROPERTY, "0");
		if (System.getProperty("jub.consumers") == null) {
			System.setProperty("jub.consumers", "CONSOLE,H2");
		}
		if (System.getProperty("jub.db.file") == null) {
			System.setProperty("jub.db.file", "benckmarks/.benchmarks");
		}
		if (System.getProperty(BenchmarkOptionsSystemProperties.CHARTS_DIR_PROPERTY) == null) {
			System.setProperty(BenchmarkOptionsSystemProperties.CHARTS_DIR_PROPERTY, "benckmarks/charts");
		}
	}

	@AfterClass
	public static void afterClass() {
		ConstellioTestSession.closeAfterTestClass();
	}

	protected static LocalDateTime date(int day, int zeroBasedMonth, int year) {
		return new LocalDateTime(year, zeroBasedMonth, day, 0, 0);
	}

	public static boolean isUnitTest(String className) {
		for (String suffix : notUnitTestSuffix) {
			if (className.endsWith(suffix)) {
				return false;
			}
		}
		return true;
	}

	public static File aFile() {
		return TestUtils.aFile();
	}

	private static void ensureNotUnitTest() {
		if (isUnitTestStatic()) {
			String message = "Unit tests '" + TestClassFinder.findCurrentTest().getSimpleName()
					+ "' cannot use filesystem, rename this test to AcceptanceTest or IntegrationTest or RealTest";
			throw new RuntimeException(message);
		}
	}

	public static boolean isUnitTestStatic() {
		return isUnitTest(TestClassFinder.findCurrentTest().getSimpleName());
	}

	protected String getTestName() {
		return skipTestRule.currentTestName;
	}

	@org.junit.Before
	public void logTest() {
		if (LOGGER == null) {
			LOGGER = LoggerFactory.getLogger(getClass());
		}
		try {
			FileUtils.write(new File("constellio.log"), "Test '" + getTestName() + "' has started", true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@After
	public void printMemoryUsage() {

		boolean memoryReport = false;
		if (sdkPropertiesLoader.sdkProperties != null) {
			memoryReport = "true".equals(sdkPropertiesLoader.sdkProperties.get("memoryReport"));
		}

		if (memoryReport) {

			printDeltaMemoryUsage(getTestName());

			System.gc();
			System.runFinalization();
			previousMemoryUsage = getMemoryUsage();
		} else {
			try {
				FileUtils.write(new File("constellio.log"), "Test '" + getTestName() + "' has eneded", true);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private Map<String, Long> getMemoryUsage() {
		Map<String, Long> memoryUsage = new HashMap<>();
		for (MemoryPoolMXBean item : ManagementFactory.getMemoryPoolMXBeans()) {
			memoryUsage.put(item.getName(), item.getUsage().getUsed());
		}
		return memoryUsage;
	}

	private void printDeltaMemoryUsage(String stepName) {

		Map<String, Long> currentMemoryUsage = getMemoryUsage();
		List<String> keys = new ArrayList<>(currentMemoryUsage.keySet());
		Collections.sort(keys);

		StringBuilder deltas = new StringBuilder(". Memory deltas : ");

		boolean hasOneDelta = false;
		for (String key : keys) {
			long previous = !previousMemoryUsage.containsKey(key) ? 0 : previousMemoryUsage.get(key);
			long current = currentMemoryUsage.get(key);
			long delta = current - previous;
			if (delta > 10000000) {
				long deltaInMo = delta / 1000000;
				deltas.append("\n\t\t" + key + ": " + deltaInMo + "Mo");
				hasOneDelta = true;
			}
		}

		try {
			FileUtils.write(new File("constellio.log"), "Test '" + getTestName() + "' has ended" + (hasOneDelta ? deltas : ""),
					true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		previousMemoryUsage = currentMemoryUsage;
	}

	protected byte[] aByteArray() {
		return TestUtils.aByteArray();
	}

	protected LocalDateTime aDateTime() {
		return TestUtils.aDateTime();
	}

	protected LocalDate aDate() {
		return TestUtils.aDate();
	}

	protected File aFile(File parent) {
		return TestUtils.aFile(parent);
	}

	protected long aLong() {
		return TestUtils.aLong();
	}

	protected int anInteger() {
		return TestUtils.anInteger();
	}

	protected String aString() {
		return TestUtils.aString();
	}

	protected String[] aStringArray() {
		return TestUtils.aStringArray();
	}

	protected <T extends Closeable> T closeAfterTest(T closeable) {
		return getCurrentTestSession().getStreamsTestFeatures().closeAfterTest(closeable);
	}

	protected File createTempCopy(File originalFile) {
		ensureNotUnitTest();
		return getCurrentTestSession().getFileSystemTestFeatures().createTempCopy(originalFile);
	}

	protected InputStream getTestResourceInputStream(String partialName) {
		ensureNotUnitTest();
		File testResourceFile = getTestResourceFile(partialName);
		InputStream inputStream;
		try {
			inputStream = newFileInputStream(testResourceFile);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		closeAfterTest(inputStream);
		return inputStream;
	}

	protected String getTestResourceContent(String partialName) {
		try {
			return FileUtils.readFileToString(getTestResourceFile(partialName));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected File getTestResourceFile(String partialName) {
		ensureNotUnitTest();
		String completeName = getClass().getCanonicalName().replace(".", File.separator) + "-" + partialName;
		File resourcesDir = new File("sdk-resources");
		if (!resourcesDir.getAbsolutePath().contains(File.separator + "sdk" + File.separator)) {
			resourcesDir = new File("sdk" + File.separator + "sdk-resources");
		}
		File file = new File(resourcesDir, completeName);

		if (!file.exists()) {
			throw new RuntimeException("No such file '" + file.getAbsolutePath() + "'");
		}
		return file;
	}

	protected StreamFactory<InputStream> getTestResourceInputStreamFactory(final String partialName) {
		return getCurrentTestSession().getStreamsTestFeatures()
				.ensureAllCreatedCloseableAreClosed(new StreamFactory<InputStream>() {

					@Override
					public InputStream create(String name)
							throws IOException {
						return newFileInputStream(getTestResourceFile(partialName));
					}
				});
	}

	protected StreamFactory<InputStream> getTestResourceInputStreamFactory(final File file) {
		return getCurrentTestSession().getStreamsTestFeatures()
				.ensureAllCreatedCloseableAreClosed(new StreamFactory<InputStream>() {

					@Override
					public InputStream create(String name)
							throws IOException {
						return newFileInputStream(file);
					}
				});
	}

	protected StreamFactory<OutputStream> getTestResourceOutputStreamFactory(final File file) {
		return getCurrentTestSession().getStreamsTestFeatures()
				.ensureAllCreatedCloseableAreClosed(new StreamFactory<OutputStream>() {

					@Override
					public OutputStream create(String name)
							throws IOException {
						return new FileOutputStream(file);
					}
				});
	}

	protected StreamFactory<InputStream> getTestResourceInputStreamFactory(final byte[] bytes) {
		return getCurrentTestSession().getStreamsTestFeatures()
				.ensureAllCreatedCloseableAreClosed(new StreamFactory<InputStream>() {

					@Override
					public InputStream create(String name)
							throws IOException {
						return new ByteArrayInputStream(bytes);
					}
				});
	}

	protected File getUnzippedResourceFile(String partialName) {
		File zipFile = getTestResourceFile(partialName);
		return unzipInTempFolder(zipFile);
	}

	protected boolean isUnitTest() {
		return isUnitTest(getClass().getSimpleName());
	}

	@SuppressWarnings("unchecked")
	protected <I> List<I> newArrayList(I... items) {
		return new ArrayList<I>(Arrays.asList(items));
	}

	protected void configure(DataLayerConfigurationAlteration dataLayerConfigurationAlteration) {
		ensureNotUnitTest();
		getCurrentTestSession().getFactoriesTestFeatures().configure(dataLayerConfigurationAlteration);

	}

	protected void configure(ModelLayerConfigurationAlteration modelLayerConfigurationAlteration) {
		ensureNotUnitTest();
		getCurrentTestSession().getFactoriesTestFeatures().configure(modelLayerConfigurationAlteration);
	}

	protected void configure(AppLayerConfigurationAlteration appLayerConfigurationAlteration) {
		ensureNotUnitTest();
		getCurrentTestSession().getFactoriesTestFeatures().configure(appLayerConfigurationAlteration);
	}

	protected AppLayerFactory getAppLayerFactory() {
		ensureNotUnitTest();
		return getCurrentTestSession().getFactoriesTestFeatures().newAppServicesFactory();
	}

	protected DataLayerFactory getDataLayerFactory() {
		ensureNotUnitTest();
		return getCurrentTestSession().getFactoriesTestFeatures().newDaosFactory();
	}

	protected IOServicesFactory getIOLayerFactory() {
		ensureNotUnitTest();
		return getCurrentTestSession().getFactoriesTestFeatures().newIOServicesFactory();
	}

	protected ConstellioFactories getConstellioFactories() {
		ensureNotUnitTest();
		return getCurrentTestSession().getFactoriesTestFeatures().getConstellioFactories();
	}

	protected ModelLayerFactory getModelLayerFactory() {
		ensureNotUnitTest();
		return getCurrentTestSession().getFactoriesTestFeatures().newModelServicesFactory();
	}

	protected void withSpiedServices(Class<?>... classes) {
		ensureNotUnitTest();
		getCurrentTestSession().getFactoriesTestFeatures().withSpiedServices(classes);
	}

	protected FoldersLocator getFoldersLocator() {
		return getCurrentTestSession().getFactoriesTestFeatures().getFoldersLocator();
	}

	protected File newTempFileWithContent(String fileName, String content) {
		ensureNotUnitTest();
		return getCurrentTestSession().getFileSystemTestFeatures().newTempFileWithContent(newTempFolder(), fileName, content);
	}

	protected File newTempFileWithContentInFolder(File tempFolder, String fileName, String content) {
		ensureNotUnitTest();
		return getCurrentTestSession().getFileSystemTestFeatures().newTempFileWithContent(tempFolder, fileName, content);
	}

	protected File newTempFolder() {
		ensureNotUnitTest();
		return getCurrentTestSession().getFileSystemTestFeatures().newTempFolder();
	}

	protected File newTempFolderInFolder(File tempParentFolder, String tempFolderName) {
		File tempFolder = new File(tempParentFolder, tempFolderName);
		tempFolder.mkdirs();
		assertTrue(tempFolder.exists());
		return tempFolder;
	}

	protected CmisSessionBuilder newCmisSessionBuilder() {
		ensureNotUnitTest();
		getCurrentTestSession().getFactoriesTestFeatures().load();
		return getCurrentTestSession().getSeleniumTestFeatures().newCmisSessionBuilder();
	}

	protected AdminServicesSession newRestClient(String serviceKey, String username, String password) {
		ensureNotUnitTest();
		getCurrentTestSession().getFactoriesTestFeatures().load();
		return getCurrentTestSession().getSeleniumTestFeatures().newRestClient(serviceKey, username, password);
	}

	protected WebTarget newWebTarget() {
		ensureNotUnitTest();
		getCurrentTestSession().getFactoriesTestFeatures().load();
		return getCurrentTestSession().getSeleniumTestFeatures().newWebTarget();
	}

	protected WebTarget newWebTarget(String path) {
		ensureNotUnitTest();
		getCurrentTestSession().getFactoriesTestFeatures().load();
		return getCurrentTestSession().getSeleniumTestFeatures().newWebTarget(path);
	}

	protected SolrClient newSearchClient() {
		ensureNotUnitTest();
		getCurrentTestSession().getFactoriesTestFeatures().load();
		return getCurrentTestSession().getSeleniumTestFeatures().newSearchClient();
	}

	protected ConstellioWebDriver newWebDriver(SessionContext sessionContext) {
		ensureNotUnitTest();
		ensureUITest();
		if (sessionContext instanceof FakeSessionContext) {
			if (((FakeSessionContext) sessionContext).fake) {
				throw new RuntimeException("Web driver requires a valid session context. Use 'loggedAsUserInCollection' instead");
			}
		}
		getCurrentTestSession().getFactoriesTestFeatures().load();

		AppLayerFactory factory = getAppLayerFactory();
		factory.getInitUIListeners().add(new TestInitUIListener(sessionContext));
		factory.getEnterViewListeners().add(new TestEnterViewListener());
		factory.getContainerButtonListeners().add(new TestContainerButtonListener());
		ServerThrowableContext.LAST_THROWABLE.set(null);

		return getCurrentTestSession().getSeleniumTestFeatures().newWebDriver(skipTestRule.isInDevelopmentTest());
	}

	private void ensureUITest() {
		if (getClass().getAnnotation(UiTest.class) == null) {
			throw new RuntimeException("The test class must have declared @UITest annotation");
		}
	}

	private void ensureInDevelopmentTest() {

		if (!skipTestRule.isInDevelopmentTest()) {
			throw new RuntimeException("The test class must have declared @InDevelopmentTest annotation");
		}
	}

	protected void waitUntilICloseTheBrowsers() {
		ensureNotUnitTest();
		ensureInDevelopmentTest();
		getCurrentTestSession().getSeleniumTestFeatures().waitUntilICloseTheBrowsers();
	}

	protected File unzipInTempFolder(File zipFile) {
		File tempFolder = newTempFolder();
		try {
			new ZipService(new IOServices(newTempFolder())).unzip(zipFile, tempFolder);
			return tempFolder;
		} catch (ZipServiceException e) {
			throw new RuntimeException(e);
		}
	}

	protected File createRandomTextFilesInTempFolder(int numberOfFiles, int charactersPerFile) {
		return getCurrentTestSession().getFileSystemTestFeatures()
				.createRandomTextFilesInTempFolder(numberOfFiles, charactersPerFile);
	}

	protected void assertInputStreamEquals(InputStream i1, InputStream i2) {
		try {
			byte[] i1Bytes = IOUtils.toByteArray(i1);
			byte[] i2Bytes = IOUtils.toByteArray(i2);
			assertThat(i2Bytes).isEqualTo(i1Bytes);
		} catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}

	}

	protected OngoingConcurrentExecution runConcurrently(ConcurrentJob job) {
		return new OngoingConcurrentExecution(job);
	}

	protected FileSystemTestFeatures modifyFileSystem() {
		ensureNotUnitTest();
		return getCurrentTestSession().getFileSystemTestFeatures();
	}

	protected SchemaTestFeatures defineSchemasManager() {
		if (isUnitTestStatic()) {
			throw new RuntimeException("Must use defineSchemas(mockedMetadataSchemasManager)");
		}
		return getCurrentTestSession().getSchemaTestFeatures().use();
	}

	protected SchemaTestFeatures define(MetadataSchemasManager metadataSchemaManager) {
		return getCurrentTestSession().getSchemaTestFeatures().useWithMockedSchemaManager(metadataSchemaManager);
	}

	protected ModulesAndMigrationsTestFeatures givenCollectionInVersion(String collection, List<String> languages,
			String version) {
		ensureNotUnitTest();
		getAppLayerFactory().getCollectionsManager().createCollectionInVersion(collection, languages, version);
		return new ModulesAndMigrationsTestFeatures(getCurrentTestSession().getFactoriesTestFeatures(), collection);
	}

	protected ModulesAndMigrationsTestFeatures givenCollectionWithTitle(String collection, String collectionTitle) {
		ModulesAndMigrationsTestFeatures features = givenCollection(collection);

		Record collectionRecord = getModelLayerFactory().newRecordServices().getDocumentById(collection);
		try {
			getModelLayerFactory().newRecordServices().update(collectionRecord.set(Schemas.TITLE, collectionTitle));
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		return features;
	}

	protected ModulesAndMigrationsTestFeatures givenCollection(String collection) {
		return givenCollection(collection, Arrays.asList("fr"));
	}

	protected ModulesAndMigrationsTestFeatures givenCollection(String collection, List<String> languages) {
		ensureNotUnitTest();
		getAppLayerFactory().getCollectionsManager().createCollectionInCurrentVersion(collection, languages);
		return new ModulesAndMigrationsTestFeatures(getCurrentTestSession().getFactoriesTestFeatures(), collection);
	}

	protected void givenTimeIs(final LocalDateTime localDateTime) {
		final LocalDate localDate = new LocalDate(localDateTime);
		TimeProvider.setTimeProvider(new TimeProvider() {
			@Override
			public LocalDateTime getTimeProviderLocalDateTime() {
				return localDateTime;
			}

			@Override
			public LocalDate getTimeProviderLocalDate() {
				return localDate;
			}
		});
	}

	protected void givenTimeIs(final LocalDate localDate) {
		final LocalDateTime localDateTime = localDate.toDateMidnight().toDateTime().toLocalDateTime();
		TimeProvider.setTimeProvider(new TimeProvider() {
			@Override
			public LocalDateTime getTimeProviderLocalDateTime() {
				return localDateTime;
			}

			@Override
			public LocalDate getTimeProviderLocalDate() {
				return localDate;
			}
		});
	}

	protected void givenActualTime() {
		TimeProvider.setTimeProvider(new DefaultTimeProvider());
	}

	protected void waitForBatchProcess()
			throws InterruptedException {
		ensureNotUnitTest();
		getCurrentTestSession().getBatchProcessTestFeature().waitForAllBatchProcessesAndEnsureNoErrors(null);
	}

	protected void waitForBatchProcessAndDoSomethingWhenTheFirstBatchProcessIsStarted(Runnable runtimeAction)
			throws InterruptedException {
		ensureNotUnitTest();
		getCurrentTestSession().getBatchProcessTestFeature().waitForAllBatchProcessesAndEnsureNoErrors(runtimeAction);
	}

	public void runSubTest(SubTest subTest) {
		System.out.println("\n\n=====================- " + subTest.getClass().getSimpleName() + " -===================== \n");
		long before = new Date().getTime();
		subTest.run();
		long after = new Date().getTime();
		System.out.println("\nsub test duration : " + (after - before) + "ms");
	}

	protected void givenConstellioProperties(Map<String, String> configMap) {
		getCurrentTestSession().getFactoriesTestFeatures().givenConstellioProperties(configMap);
	}

	protected void printTimeElapsedSinceLastCall() {
		printTimeElapsedSinceLastCall(null);
	}

	protected void printTimeElapsedSinceLastCall(String comment) {
		if (comment == null) {
			comment = (printIndex - 1) + "-" + printIndex;
		}

		System.out.println("= = = = = = = = = = = = = = = = = = = = = = =\n" + "" +
				"Segment '" + comment + "' took " + (new Date().getTime() - time) + "ms\n" +
				"= = = = = = = = = = = = = = = = = = = = = = =\n");

		time = new Date().getTime();
	}

	protected abstract ConstellioTestSession getCurrentTestSession();

	public abstract class SubTest {

		public abstract void run();

	}

	protected InputStream newFileInputStream(File file)
			throws FileNotFoundException {
		return getModelLayerFactory().getIOServicesFactory().newIOServices().newFileInputStream(file, SDK_STREAM);
	}

	protected Reader getTestResourceReader(String resourceName)
			throws FileNotFoundException {
		return getModelLayerFactory().getIOServicesFactory().newIOServices()
				.newFileReader(getTestResourceFile(resourceName), SDK_STREAM);
	}

	protected Reader getTestResourceReader(File file)
			throws FileNotFoundException {
		return getModelLayerFactory().getIOServicesFactory().newIOServices().newFileReader(file, SDK_STREAM);
	}

	protected StreamFactory<Reader> getTestResourceReaderFactory(String resourceName)
			throws FileNotFoundException {
		File file = getTestResourceFile(resourceName);
		return getModelLayerFactory().getIOServicesFactory().newIOServices().newFileReaderFactory(file);
	}

	protected void givenConfig(SystemConfiguration config, Object value) {
		ensureNotUnitTest();
		getModelLayerFactory().getSystemConfigurationsManager().setValue(config, value);
	}

	protected void waitUntilTrue(AtomicBoolean atomicBoolean) {
		while (!atomicBoolean.get()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected void pokeChuckNorris() {

		System.out.println("You poke Chuck Norris...");
		System.out.println("Chuck Norris roundhouse kicked you in the face.");
		System.exit(0);
	}

	protected final User wrapUser(Record userRecord) {
		ensureNotUnitTest();
		getModelLayerFactory().newRecordServices().refresh(userRecord);
		if (userRecord == null) {
			return null;
		} else {
			return new SchemasRecordsServices(userRecord.getCollection(), getModelLayerFactory()).wrapUser(userRecord);
		}
	}

	protected final Group wrapGroup(Record userRecord) {
		ensureNotUnitTest();
		getModelLayerFactory().newRecordServices().refresh(userRecord);
		if (userRecord == null) {
			return null;
		} else {
			return new SchemasRecordsServices(userRecord.getCollection(), getModelLayerFactory()).wrapGroup(userRecord);
		}
	}

	protected SessionContext loggedAsUserInCollection(String username, String collection) {
		ensureNotUnitTest();
		User user = getModelLayerFactory().newUserServices().getUserInCollection(username, collection);
		return FakeSessionContext.forRealUserIncollection(user);
	}

	protected Record record(String id) {
		ensureNotUnitTest();
		return getModelLayerFactory().newRecordServices().getDocumentById(id);
	}

	protected Record withId(String id) {
		ensureNotUnitTest();
		return getModelLayerFactory().newRecordServices().getDocumentById(id);
	}

	public abstract void afterTest(boolean failed);

	public void failed(Throwable e, Description description) {
		if (!skipTestRule.wasSkipped()) {
			afterTest(true);
		}
	}

	public void finished(Description description) {
		if (!skipTestRule.wasSkipped()) {
			afterTest(false);
		}
	}

	protected void givenDisabledAfterTestValidations() {
		getCurrentTestSession().getAfterTestValidationsTestFeature().disableInCurrentTest();
	}

	protected String recordIdWithTitleInCollection(String title, String collection) {
		Record record = getModelLayerFactory().newSearchServices().searchSingleResult(
				fromAllSchemasInExceptEvents(collection).where(TITLE).isEqualTo(title));
		return record.getId();
	}

	protected SaveStateFeature getSaveStateFeature() {
		return getCurrentTestSession().getSaveStateFeature();
	}

	protected void givenTransactionLogIsEnabled() {
		final File logTempFolder = getCurrentTestSession().getFileSystemTestFeatures().newTempFolderWithName("tLog");
		configure(new DataLayerConfigurationAlteration() {
			@Override
			public void alter(DataLayerConfiguration configuration) {

				doReturn(true).when(configuration).isSecondTransactionLogEnabled();
				doReturn(logTempFolder).when(configuration).getSecondTransactionLogBaseFolder();
			}
		});
	}

}
