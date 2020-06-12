package com.constellio.sdk.tests;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.data.utils.dev.Toggle.AvailableToggle;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.cache.RecordsCache2IntegrityDiagnosticService;
import com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.utils.TenantUtils;
import com.constellio.sdk.tests.annotations.PreserveState;
import com.constellio.sdk.tests.setups.SchemaShortcuts;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.junit.After;
import org.junit.Before;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.sdk.tests.TestUtils.englishMessages;
import static org.assertj.core.api.Assertions.assertThat;

public class ConstellioTest extends AbstractConstellioTest {
	protected Transaction tx;
	public static final String ANSI_RESET = "\u001B[0m";
	private static boolean isCurrentPreservingState;

	public static boolean IS_FIRST_EXECUTED_TEST = true;

	private ConstellioTestSession testSession;
	private String failMessage;

	@Override
	public ConstellioTestSession getCurrentTestSession() {
		return testSession;
	}

	@Override
	public void afterTest(boolean failed) {
		testSession.close(false, failed, false);
	}

	private static ConstellioTest currentInstance;

	protected boolean cacheIntegrityCheckedAfterTest;

	@Before
	public void beforeConstellioTest() {
		System.out.println("Allocated memory before test : " + OffHeapMemoryAllocator.getAllocatedMemory() + " bytes");
		System.out.println("Allocated memory before test - OffHeapByteArrayList_ID : " + OffHeapMemoryAllocator.getAllocatedMemory(OffHeapMemoryAllocator.OffHeapByteArrayList_ID));
		System.out.println("Allocated memory before test - OffHeapByteList_ID: " + OffHeapMemoryAllocator.getAllocatedMemory(OffHeapMemoryAllocator.OffHeapByteList_ID));
		System.out.println("Allocated memory before test - OffHeapIntList_ID: " + OffHeapMemoryAllocator.getAllocatedMemory(OffHeapMemoryAllocator.OffHeapIntList_ID));
		System.out.println("Allocated memory before test - OffHeapLongList_ID: " + OffHeapMemoryAllocator.getAllocatedMemory(OffHeapMemoryAllocator.OffHeapLongList_ID));
		System.out.println("Allocated memory before test - OffHeapShortList_ID: " + OffHeapMemoryAllocator.getAllocatedMemory(OffHeapMemoryAllocator.OffHeapShortList_ID));
		System.out.println("Allocated memory before test - SortedIntIdsList_ID: " + OffHeapMemoryAllocator.getAllocatedMemory(OffHeapMemoryAllocator.SortedIntIdsList_ID));
		System.out.println("Allocated memory before test - SDK: " + OffHeapMemoryAllocator.getAllocatedMemory(OffHeapMemoryAllocator.SDK));
		System.out.println("Allocated memory before test - OffHeapByteArrayListArea_ID: " + OffHeapMemoryAllocator.getAllocatedMemory(OffHeapMemoryAllocator.OffHeapByteArrayListArea_ID));

		MockitoAnnotations.initMocks(this);
		cacheIntegrityCheckedAfterTest = true;

		for (AvailableToggle toggle : Toggle.getAllAvailable()) {
			toggle.reset();
		}

		///Toggle.VALIDATE_CACHE_EXECUTION_SERVICE_USING_SOLR.enable();
		Toggle.USE_FILESYSTEM_DB_FOR_LARGE_METADATAS_CACHE.disable();
		if (SystemUtils.IS_OS_WINDOWS) {
			Toggle.USE_MMAP_WITHMAP_DB_FOR_RUNTIME.disable();
			Toggle.USE_MMAP_WITHMAP_DB_FOR_LOADING.disable();
		}
		Toggle.ROLES_WITH_NEW_7_2_PERMISSIONS.enable();
		Toggle.STRUCTURE_CACHE_BASED_ON_EXISTING_IDS.disable();
		testSession = ConstellioTestSession.build(isUnitTest(), sdkProperties, skipTestRule, getClass(), checkRollback());
		if (!isKeepingPreviousState() && testSession.getFactoriesTestFeatures() != null && IS_FIRST_EXECUTED_TEST) {

			//			testSession.getFactoriesTestFeatures().clear();
			SDKConstellioFactoriesInstanceProvider.firstTest = true;
			try {
				testSession.getFactoriesTestFeatures().getConstellioFactories();
			} catch (Exception ignored) {
			}

			testSession.getFactoriesTestFeatures().addTenants();
			testSession.getFactoriesTestFeatures().getTenants().forEach(tenant -> {
				try {
					TenantUtils.setTenant(String.valueOf(tenant.getId()));
					testSession.getFactoriesTestFeatures().getConstellioFactories().getDataLayerFactory().getSolrServers();
				} catch (Exception ignored) {
				} finally {
					TenantUtils.setTenant(null);
				}
			});
			testSession.getFactoriesTestFeatures().clearTenants();

			testSession.close(true, false, false);
			ReindexingServices.markReindexingHasFinished();

			System.out.print("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

			try {
				Class.forName("com.constellio.sdk.SDKPluginsTestUtils").getMethod("init").invoke(null);
			} catch (Exception e) {
				//OK
			}

			// allow to change restricted http headers such as "host"
			System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

			System.out.print(ANSI_RESET + "\t\t*** Exceptions displayed before this line are OK ***\n\n");
			testSession = ConstellioTestSession.build(isUnitTest(), sdkProperties, skipTestRule, getClass(), checkRollback());
			IS_FIRST_EXECUTED_TEST = false;
		}
		if (isPreservingState()) {
			PreserveState preserveStateAnnotation = getClass().getAnnotation(PreserveState.class);
			if (preserveStateAnnotation.enabled()) {
				testSession.getFileSystemTestFeatures()
						.setPreservedState(getClass().getName() + "-" + preserveStateAnnotation.state());
			}
		}
		currentInstance = this;
	}

	protected void execute(Transaction tx) throws RecordServicesException {
		getModelLayerFactory().newRecordServices().execute(tx);
	}

	protected Record newRecord(SchemaShortcuts schemaShortcuts) {
		return getModelLayerFactory().newRecordServices().newRecordWithSchema(schemaShortcuts.instance());
	}


	protected Record newRecord(SchemaShortcuts schemaShortcuts, String id) {
		return getModelLayerFactory().newRecordServices().newRecordWithSchema(schemaShortcuts.instance(), id);
	}


	protected Record newRecord(MetadataSchema schema) {
		return getModelLayerFactory().newRecordServices().newRecordWithSchema(schema);
	}


	protected Record newRecord(MetadataSchema schema, String id) {
		return getModelLayerFactory().newRecordServices().newRecordWithSchema(schema, id);
	}

	public static ConstellioTest getInstance() {
		return currentInstance;
	}

	public void resetTestSession() {
		testSession.close(true, false, false);

		testSession = ConstellioTestSession.build(isUnitTest(), sdkProperties, skipTestRule, getClass(), checkRollback());
	}

	protected void clearTestSession() {
		if (!isPreservingState()) {
			testSession.close(false, false, false);
			testSession = ConstellioTestSession.build(isUnitTest(), sdkProperties, skipTestRule, getClass(), checkRollback());
		}
	}

	private boolean isKeepingPreviousState() {
		PreserveState preserveStateAnnotation = getClass().getAnnotation(PreserveState.class);
		if (preserveStateAnnotation == null) {
			return isCurrentPreservingState = false;
		} else {
			String lastPreservedState = testSession.getFileSystemTestFeatures().getLastPreservedState();
			String currentState = getClass().getName() + "-" + preserveStateAnnotation.state();
			return isCurrentPreservingState = (preserveStateAnnotation.enabled() && currentState.equals(lastPreservedState));
		}
	}

	private boolean isPreservingState() {
		PreserveState preserveStateAnnotation = getClass().getAnnotation(PreserveState.class);
		return preserveStateAnnotation != null && preserveStateAnnotation.enabled();
	}

	public static boolean isCurrentPreservingState() {
		return isCurrentPreservingState;
	}

	public static Map<String, String> fr_en(String fr, String en) {
		Map<String, String> map = new HashMap<>();
		map.put("fr", fr);
		map.put("en", en);
		return map;
	}

	public static Map<String, String> en(String en) {
		Map<String, String> map = new HashMap<>();
		map.put("en", en);
		return map;
	}

	public static Map<String, String> fr(String fr) {
		Map<String, String> map = new HashMap<>();
		map.put("fr", fr);
		return map;
	}

	public static void disableCleanStartup() {
		IS_FIRST_EXECUTED_TEST = false;
	}

	public void startABrowserAndWaitUntilICloseIt() {

	}

	public void setFailMessage(String failMessage) {
		this.failMessage = failMessage;
	}

	public String getFailMessage() {
		return failMessage;
	}

	public void checkCache() throws Exception {

		ModelLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();

		RecordsCache2IntegrityDiagnosticService service = new RecordsCache2IntegrityDiagnosticService(modelLayerFactory);
		ValidationErrors errors = service.validateIntegrity(false, true);
		//List<String> messages = englishMessages(errors).stream().map((s) -> substringBefore(s, " :")).collect(toList());

		List<String> messages = englishMessages(errors);
		assertThat(messages).isEmpty();

	}

	@After
	public void checkCacheAfterTest() throws Exception {

		if (!failureDetectionTestWatcher.isFailed() && isUnitTestStatic() && ConstellioFactories.isInitialized()
			&& cacheIntegrityCheckedAfterTest && Toggle.SDK_CACHE_INTEGRITY_VALIDATION.isEnabled()) {

			ConstellioFactories.getInstance().getDataLayerFactory()
					.getDataLayerLogger().setPrintAllQueriesLongerThanMS(10000);
			ValidationErrors errors = checkCacheAndReturnErrors(false, false);

			if (!errors.isEmptyErrorAndWarnings()
				&& getCurrentTestSession().getBatchProcessTestFeature().waitForBatchProcessAfterTest) {
				errors = checkCacheAndReturnErrors(true, true);
			} else {
				errors = checkCacheAndReturnErrors(false, true);
			}

			List<String> messages = englishMessages(errors);
			if (!messages.isEmpty()) {
				setFailMessage("Cache problems : \n" + StringUtils.join(messages, "\n"));
			}
		}
	}

	public void restartLayers() {
		getCurrentTestSession().closeForRestarting();
		testSession = ConstellioTestSession.build(isUnitTest(), sdkProperties, skipTestRule, getClass(), checkRollback());
	}

	private ValidationErrors checkCacheAndReturnErrors(boolean waitForBatchProcesses, boolean runTwice) {
		ModelLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();

		if (waitForBatchProcesses) {
			modelLayerFactory.getBatchProcessesManager().waitUntilAllFinished();
		}

		RecordsCache2IntegrityDiagnosticService service = new RecordsCache2IntegrityDiagnosticService(modelLayerFactory);
		return service.validateIntegrity(false, runTwice);
	}
}
