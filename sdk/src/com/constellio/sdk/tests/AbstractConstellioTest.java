package com.constellio.sdk.tests;

import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasInExceptEvents;
import static com.constellio.sdk.tests.SaveStateFeatureAcceptTest.verifySameContentOfUnzippedSaveState;
import static java.util.Arrays.asList;
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
import java.util.Collections;
import java.util.Comparator;
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
import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.junitbenchmarks.BenchmarkOptionsSystemProperties;
import com.constellio.app.client.services.AdminServicesSession;
import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.modules.es.ConstellioESModule;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.DemoTestRecords;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.robots.ConstellioRobotsModule;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.importExport.systemStateExport.SystemStateExportParams;
import com.constellio.app.services.importExport.systemStateExport.SystemStateExporter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.tools.ServerThrowableContext;
import com.constellio.app.ui.tools.vaadin.TestContainerButtonListener;
import com.constellio.app.ui.tools.vaadin.TestEnterViewListener;
import com.constellio.app.ui.tools.vaadin.TestInitUIListener;
import com.constellio.client.cmis.client.CmisSessionBuilder;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogReplayFilter;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.ConsoleLogger;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.TimeProvider.DefaultTimeProvider;
import com.constellio.data.utils.dev.Toggle.AvailableToggle;
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
import com.constellio.sdk.tests.ToggleTestFeature.ToggleCondition;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.concurrent.ConcurrentJob;
import com.constellio.sdk.tests.concurrent.OngoingConcurrentExecution;
import com.constellio.sdk.tests.schemas.SchemaTestFeatures;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.setups.TestsSpeedStats;
import com.constellio.sdk.tests.setups.Users;

public abstract class AbstractConstellioTest implements FailureDetectionTestWatcherListener {

	protected static boolean notAUnitItest = false;

	private static TestsSpeedStats stats = new TestsSpeedStats();

	public static final String SDK_STREAM = StreamsTestFeatures.SDK_STREAM;

	public static SDKPropertiesLoader sdkPropertiesLoader = new SDKPropertiesLoader();
	private static Logger LOGGER;
	//	private static boolean batchProcessControllerStarted = false;
	private static String[] notUnitTestSuffix = new String[] { "AcceptanceTest", "IntegrationTest", "RealTest", "LoadTest",
			"StressTest", "PerformanceTest", "AcceptTest" };
	private static Map<String, Long> previousMemoryUsage = new HashMap<>();
	// CHECKSTYLE:OFF
	// Junit requires this field to be public
	@Rule public SkipTestsRule skipTestRule;

	@Rule public FailureDetectionTestWatcher failureDetectionTestWatcher = new FailureDetectionTestWatcher(this);

	protected Map<String, String> sdkProperties = new HashMap<String, String>();

	protected String zeCollection = "zeCollection";
	protected String businessCollection = "LaCollectionDeRida";
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
	private File state1, state2;
	private TemporaryFolder folder;

	public AbstractConstellioTest() {
		sdkPropertiesLoader.setLocked(isUnitTest());
		sdkProperties = sdkPropertiesLoader.getSDKProperties();
		skipTestRule = new SkipTestsRule(sdkPropertiesLoader, isUnitTest(getClass().getSimpleName()));
		ConsoleLogger.GLOBAL_PREFIX = getClass().getSimpleName();
	}

	@BeforeClass
	public static void beforeClass()
			throws Exception {
		MetadataSchemasManager.cacheEnabled = true;
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

	protected void givenWaitForBatchProcessAfterTestIsDisabled() {
		getCurrentTestSession().getBatchProcessTestFeature().waitForBatchProcessAfterTest = false;
	}

	protected void givenBackgroundThreadsEnabled() {
		getCurrentTestSession().getFactoriesTestFeatures().givenBackgroundThreadsEnabled();
	}

	protected void givenSystemLanguageIs(String languageCode) {
		getCurrentTestSession().getFactoriesTestFeatures().setSystemLanguage(languageCode);
	}

	@AfterClass
	public static void afterClass() {
		ConstellioTestSession.closeAfterTestClass();
		//stats.printSummaries();
	}

	protected static LocalDateTime dateTime(int day, int zeroBasedMonth, int year) {
		return new LocalDateTime(year, zeroBasedMonth, day, 0, 0);
	}

	protected static LocalDate date(int year, int oneBasedMonth, int day) {
		return new LocalDate(year, oneBasedMonth, day);
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
			String message = "Unit tests '" + TestClassFinder.getTestClassName()
					+ "' cannot use filesystem, rename this test to AcceptanceTest or IntegrationTest or RealTest";
			throw new RuntimeException(message);
		}
	}

	public static boolean isUnitTestStatic() {
		return isUnitTest(TestClassFinder.getTestClassName()) && !notAUnitItest;
	}

	protected String getTestName() {
		return skipTestRule.currentTestName;
	}

	@org.junit.Before
	public void invalidateStaticCaches() {
		FoldersLocator.invalidateCaches();
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

	private void assertThatStatesAreEqual(File state1, File state2)
			throws Exception {
		File state1TempFolder = newTempFolder();
		File state2TempFolder = newTempFolder();

		getIOLayerFactory().newZipService().unzip(state1, state1TempFolder);
		getIOLayerFactory().newZipService().unzip(state2, state2TempFolder);

		verifySameContentOfUnzippedSaveState(state1TempFolder, state2TempFolder);
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
				FileUtils.write(new File("constellio.log"), "Test '" + getTestName() + "' has ended", true);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected boolean checkRollback() {
		return skipTestRule.checkRollback;
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

	public static File getResourcesDir() {
		File resourcesDir = new File("sdk-resources");
		if (!resourcesDir.getAbsolutePath().contains(File.separator + "sdk" + File.separator)) {
			resourcesDir = new File("sdk" + File.separator + "sdk-resources");
		}
		return resourcesDir;
	}

	protected File getTestResourceFile(String partialName) {
		ensureNotUnitTest();
		String completeName = getClass().getCanonicalName().replace(".", File.separator) + "-" + partialName;
		File resourcesDir = getResourcesDir();
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
		return new ArrayList<I>(asList(items));
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

	protected String startApplicationWithWebServices() {
		getCurrentTestSession().getSeleniumTestFeatures().disableAllServices();
		System.setProperty("driverEnabled", "true");
		return getCurrentTestSession().getSeleniumTestFeatures().startApplication();
	}

	protected String startApplication() {
		getCurrentTestSession().getSeleniumTestFeatures().disableAllServices();
		return getCurrentTestSession().getSeleniumTestFeatures().startApplication();
	}

	protected String startApplicationWithSSL(boolean keepAlive) {
		getCurrentTestSession().getSeleniumTestFeatures().disableAllServices();
		return getCurrentTestSession().getSeleniumTestFeatures().startApplicationWithSSL(keepAlive);
	}

	protected void stopApplication() {
		getCurrentTestSession().getSeleniumTestFeatures().stopApplication();
	}

	protected ConstellioWebDriver newWebDriver() {
		return newWebDriver(null, false);
	}

	protected ConstellioWebDriver newWebDriverSSL() {
		return newWebDriver(null, true);
	}

	protected ConstellioWebDriver newWebDriver(SessionContext sessionContext) {
		return newWebDriver(sessionContext, false);
	}

	protected ConstellioWebDriver newWebDriver(SessionContext sessionContext, boolean useSSL) {
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

		return getCurrentTestSession().getSeleniumTestFeatures()
				.newWebDriver(skipTestRule.isInDevelopmentTest() || skipTestRule.isMainTest(), useSSL);
	}

	private void ensureUITest() {
		if (getClass().getAnnotation(UiTest.class) == null) {
			throw new RuntimeException("The test class must have declared @UITest annotation");
		}
	}

	private void ensureInDevelopmentTest() {

		if (!skipTestRule.isInDevelopmentTest() && !skipTestRule.isMainTest()) {
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

	protected ModulesAndMigrationsTestFeatures givenCollectionWithTitle(String collection, List<String> languages,
			String collectionTitle) {
		ModulesAndMigrationsTestFeatures features = givenCollection(collection, languages);

		Record collectionRecord = getModelLayerFactory().newRecordServices().getDocumentById(collection);
		try {
			getModelLayerFactory().newRecordServices().update(collectionRecord.set(Schemas.TITLE, collectionTitle));
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		return features;
	}

	protected ModulesAndMigrationsTestFeatures givenCachedCollection(String collection) {
		return givenCollection(collection);
	}

	protected ModulesAndMigrationsTestFeatures givenSpecialCollection(String collection) {
		return givenCollection(collection);
	}

	protected ModulesAndMigrationsTestFeatures givenCollection(String collection) {
		return givenCollection(collection, asList("fr"));
	}

	protected ModulesAndMigrationsTestFeatures givenSpecialCollection(String collection, List<String> languages) {
		return givenCollection(collection, languages);
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

	protected void waitForBatchProcessAcceptingErrors()
			throws InterruptedException {
		long batchProcessStart = new Date().getTime();
		ensureNotUnitTest();
		getCurrentTestSession().getBatchProcessTestFeature().waitForAllBatchProcessesAcceptingErrors(null);
		long batchProcessEnd = new Date().getTime();
		stats.add(this, getTestName(), "waitForBatchProcess", batchProcessEnd - batchProcessStart);
	}

	protected void waitForBatchProcess()
			throws InterruptedException {
		long batchProcessStart = new Date().getTime();
		ensureNotUnitTest();
		getCurrentTestSession().getBatchProcessTestFeature().waitForAllBatchProcessesAndEnsureNoErrors(null);
		long batchProcessEnd = new Date().getTime();
		stats.add(this, getTestName(), "waitForBatchProcess", batchProcessEnd - batchProcessStart);
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

	protected void safeAfterTest(boolean failed) {
		afterTest(failed);
	}

	public void failed(Throwable e, Description description) {
		if (!skipTestRule.wasSkipped()) {
			safeAfterTest(true);
		}
	}

	public void finished(Description description) {
		if (!skipTestRule.wasSkipped()) {
			safeAfterTest(false);
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
		givenTransactionLogIsEnabled(null);
	}

	protected void givenRollbackCheckDisabled() {
		getCurrentTestSession().getFactoriesTestFeatures().withoutCheckForRollback();
	}

	protected void givenTransactionLogIsEnabled(final SecondTransactionLogReplayFilter filter) {
		final File logTempFolder = getCurrentTestSession().getFileSystemTestFeatures().newTempFolderWithName("tLog");
		configure(new DataLayerConfigurationAlteration() {
			@Override
			public void alter(DataLayerConfiguration configuration) {

				doReturn(true).when(configuration).isSecondTransactionLogEnabled();
				doReturn(logTempFolder).when(configuration).getSecondTransactionLogBaseFolder();
				if (filter != null) {
					doReturn(filter).when(configuration).getSecondTransactionLogReplayFilter();
				}
			}
		});
	}

	private static boolean firstPreparation = true;

	public static interface CustomSystemPreparation {
		void prepare();

		void initializeFromCache();
	}

	private File getTurboCacheFolder() {
		File turboCacheFolder = new File(new FoldersLocator().getSDKProject(), "turboCache");

		HyperTurboMode mode = getHyperturboMode();

		if (mode == HyperTurboMode.AUTO && turboCacheFolder.exists() && firstPreparation) {
			try {
				FileUtils.deleteDirectory(turboCacheFolder);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			firstPreparation = false;
		}

		turboCacheFolder.mkdirs();
		return turboCacheFolder;
	}

	public void customSystemPreparation(CustomSystemPreparation preparation) {
		HyperTurboMode mode = getHyperturboMode();

		if (mode.isEnabled()) {
			givenTransactionLogIsEnabled();
		}
		File stateFolder = new File(getTurboCacheFolder(), getClass().getSimpleName());

		if (mode.isEnabled() && stateFolder.exists()) {
			getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(stateFolder);
			getModelLayerFactory();
			preparation.initializeFromCache();
		} else {

			preparation.prepare();

			if (mode.isEnabled()) {
				SystemStateExportParams params = new SystemStateExportParams().setExportAllContent();
				new SystemStateExporter(getAppLayerFactory()).exportSystemToFolder(stateFolder, params);
			}

		}
	}

	private HyperTurboMode getHyperturboMode() {
		String mode = sdkProperties.get("hyperTurbo");
		if ("manual".equalsIgnoreCase(mode)) {
			return HyperTurboMode.MANUAL;

		} else if ("off".equalsIgnoreCase(mode)) {
			return HyperTurboMode.OFF;

		} else {
			return HyperTurboMode.AUTO;
		}

	}

	private enum HyperTurboMode {
		AUTO, MANUAL, OFF;

		boolean isEnabled() {
			return this != OFF;
		}

	}

	private static Map<Integer, String> preparationNames = new HashMap<>();

	public void prepareSystem(CollectionPreparator... collectionPreparator) {

		HyperTurboMode mode = getHyperturboMode();
		prepareSystem(mode, collectionPreparator);

	}

	public void prepareSystemWithoutHyperTurbo(CollectionPreparator... collectionPreparator) {

		HyperTurboMode mode = HyperTurboMode.OFF;
		prepareSystem(mode, collectionPreparator);

	}

	private void prepareSystem(HyperTurboMode mode, CollectionPreparator... collectionPreparator) {

		List<CollectionPreparator> preparators = new ArrayList<>(asList(collectionPreparator));
		Collections.sort(preparators, new Comparator<CollectionPreparator>() {

			@Override
			public int compare(CollectionPreparator o1, CollectionPreparator o2) {
				return o1.collection.compareTo(o2.collection);
			}
		});

		if (mode.isEnabled()) {
			givenTransactionLogIsEnabled();
		}

		File stateFolder = new File(getTurboCacheFolder(), "" + preparators.hashCode());

		String taskName;
		long start = new Date().getTime();
		if (mode.isEnabled() && stateFolder.exists()) {
			getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(stateFolder);

			for (CollectionPreparator preparator : preparators) {
				if (preparator.rmTestRecords) {
					preparator.rmTestRecordsObject.alreadySettedUp(getModelLayerFactory());
				}
				if (preparator.demoTestRecords) {
					preparator.demoTestRecordsObject.alreadySettedUp(getModelLayerFactory());
				}
				if (preparator.users != null) {
					preparator.users.setUp(getModelLayerFactory().newUserServices());
				}
			}
		} else {

			stateFolder.mkdirs();
			for (CollectionPreparator preparator : preparators) {
				ModulesAndMigrationsTestFeatures modulesAndMigrationsTestFeatures = givenCollection(preparator.collection);
				modulesAndMigrationsTestFeatures.withMockedAvailableModules(false);
				if (preparator.modules.contains(ConstellioRMModule.ID)) {
					modulesAndMigrationsTestFeatures = modulesAndMigrationsTestFeatures.withConstellioRMModule();
				}
				if (preparator.modules.contains(ConstellioESModule.ID)) {
					modulesAndMigrationsTestFeatures = modulesAndMigrationsTestFeatures.withConstellioESModule();
				}
				if (preparator.modules.contains(TaskModule.ID)) {
					modulesAndMigrationsTestFeatures = modulesAndMigrationsTestFeatures.withTaskModule();
				}
				if (preparator.modules.contains(ConstellioRobotsModule.ID)) {
					modulesAndMigrationsTestFeatures = modulesAndMigrationsTestFeatures.withRobotsModule();
				}

				ModelLayerFactory modelLayerFactory = getModelLayerFactory();
				if (preparator.allTestUsers) {
					modulesAndMigrationsTestFeatures = modulesAndMigrationsTestFeatures.withAllTestUsers();
					if (preparator.users != null) {
						preparator.users.setUp(modelLayerFactory.newUserServices());
					}
				}

				if (preparator.rmTestRecords) {
					try {
						RMTestRecords records = preparator.rmTestRecordsObject.setup(modelLayerFactory);
						if (preparator.foldersAndContainersOfEveryStatus) {
							records = records.withFoldersAndContainersOfEveryStatus();
						}
						if (preparator.documentsDecommissioningList) {
							records = records.withDocumentDecommissioningLists();
						}
						if (preparator.documentsHavingContent) {
							records = records.withDocumentsHavingContent();
						}
						if (preparator.events) {
							records = records.withEvents();
						}
					} catch (RecordServicesException e) {
						throw new RuntimeException(e);
					}
				}
				if (preparator.demoTestRecords) {
					try {
						DemoTestRecords records = preparator.demoTestRecordsObject.setup(modelLayerFactory);
						if (preparator.foldersAndContainersOfEveryStatus) {
							records = records.withFoldersAndContainersOfEveryStatus();
						}
					} catch (RecordServicesException e) {
						throw new RuntimeException(e);
					}
				}

			}
			if (mode.isEnabled()) {
				try {
					waitForBatchProcess();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

				//Reindexing doesn't improve test performance, but maybe one day it will
				//getModelLayerFactory().newReindexingServices().reindexCollections(ReindexationMode.REWRITE);

				SystemStateExportParams params = new SystemStateExportParams().setExportAllContent();
				new SystemStateExporter(getAppLayerFactory()).exportSystemToFolder(stateFolder, params);
			}
		}
		long end = new Date().getTime();
		taskName = preparationNames.get(preparators.hashCode());
		if (taskName == null) {
			taskName = "Collections preparation similar to '" + getClass().getSimpleName() + "#" + getTestName() + "'";
			preparationNames.put(preparators.hashCode(), taskName);
		}
		stats.add(this, getTestName(), taskName, end - start);
		getCurrentTestSession().getAfterTestValidationsTestFeature().startRollbackNow();
	}

	public static class CollectionPreparator {

		Users users;
		RMTestRecords rmTestRecordsObject;
		DemoTestRecords demoTestRecordsObject;
		boolean rmTestRecords;
		boolean demoTestRecords;
		boolean foldersAndContainersOfEveryStatus;
		boolean documentsDecommissioningList;
		boolean events;
		boolean documentsHavingContent;

		boolean allTestUsers;

		String collection;

		List<String> modules = new ArrayList<>();

		public CollectionPreparator(String collection) {
			this.collection = collection;
		}

		public CollectionPreparator withConstellioRMModule() {
			modules.add(ConstellioRMModule.ID);
			Collections.sort(modules);
			return this;
		}

		public CollectionPreparator withConstellioESModule() {
			modules.add(ConstellioESModule.ID);
			Collections.sort(modules);
			return this;
		}

		public CollectionPreparator withTasksModule() {
			modules.add(TaskModule.ID);
			Collections.sort(modules);
			return this;
		}

		public CollectionPreparator withRobotsModule() {
			modules.add(ConstellioRobotsModule.ID);
			Collections.sort(modules);
			return this;
		}

		public CollectionPreparator withAllTest(Users users) {
			allTestUsers = true;
			this.users = users;
			return this;
		}

		public CollectionPreparator withAllTestUsers() {
			allTestUsers = true;
			return this;
		}

		public CollectionPreparator withRMTest(RMTestRecords records) {
			rmTestRecordsObject = records;
			rmTestRecords = true;
			return this;
		}

		public CollectionPreparator withRMTest(DemoTestRecords records) {
			demoTestRecordsObject = records;
			demoTestRecords = true;
			return this;
		}

		public CollectionPreparator withFoldersAndContainersOfEveryStatus() {
			foldersAndContainersOfEveryStatus = true;
			return this;
		}

		public CollectionPreparator withDocumentsDecommissioningList() {
			documentsDecommissioningList = true;
			return this;
		}

		public CollectionPreparator withEvents() {
			events = true;
			return this;
		}

		public CollectionPreparator withDocumentsHavingContent() {
			documentsHavingContent = true;
			return this;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof CollectionPreparator)) {
				return false;
			}

			CollectionPreparator that = (CollectionPreparator) o;

			if (allTestUsers != that.allTestUsers) {
				return false;
			}
			if (demoTestRecords != that.demoTestRecords) {
				return false;
			}
			if (documentsHavingContent != that.documentsHavingContent) {
				return false;
			}
			if (events != that.events) {
				return false;
			}
			if (documentsDecommissioningList != that.documentsDecommissioningList) {
				return false;
			}
			if (foldersAndContainersOfEveryStatus != that.foldersAndContainersOfEveryStatus) {
				return false;
			}
			if (rmTestRecords != that.rmTestRecords) {
				return false;
			}
			if (!collection.equals(that.collection)) {
				return false;
			}
			if (!modules.equals(that.modules)) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			int result = (rmTestRecords ? 1 : 0);
			result = 31 * result + (demoTestRecords ? 1 : 0);
			result = 31 * result + (foldersAndContainersOfEveryStatus ? 1 : 0);
			result = 31 * result + (documentsDecommissioningList ? 1 : 0);
			result = 31 * result + (events ? 1 : 0);
			result = 31 * result + (documentsHavingContent ? 1 : 0);
			result = 31 * result + (allTestUsers ? 1 : 0);
			result = 31 * result + collection.hashCode();
			result = 31 * result + modules.hashCode();
			return result;
		}
	}

	public CollectionPreparator withZeCollection() {
		return new CollectionPreparator(zeCollection);
	}

	public CollectionPreparator withCollection(String collection) {
		return new CollectionPreparator(collection);
	}

	public CollectionTestHelper inCollection(String collectionName) {
		return new CollectionTestHelper(asList(collectionName), getAppLayerFactory(),
				getCurrentTestSession().getFileSystemTestFeatures());
	}

	protected ModuleEnabler givenInstalledModule(Class<? extends InstallableModule> installableModuleClass) {
		ensureNotUnitTest();
		return ModuleEnabler.givenInstalledModule(getAppLayerFactory(), installableModuleClass);
	}

	public ToggleCondition onlyWhen(AvailableToggle toggle) {
		getCurrentTestSession().getToggleTestFeature().onlyWhen(toggle);
		ToggleCondition toggleCondition = new ToggleCondition();
		toggleCondition.toggle = toggle;
		return toggleCondition;
	}
}
