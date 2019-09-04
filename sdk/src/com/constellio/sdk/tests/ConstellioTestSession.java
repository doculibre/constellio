package com.constellio.sdk.tests;

import com.constellio.app.ui.i18n.i18n;
import com.constellio.data.io.services.facades.OpenedResourcesWatcher;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.TimeProvider.DefaultTimeProvider;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.sdk.tests.schemas.SchemaTestFeatures;
import com.constellio.sdk.tests.selenium.SeleniumTestFeatures;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConstellioTestSession {

	private static ConstellioTestSession session;
	private Map<String, String> sdkProperties;
	private FileSystemTestFeatures fileSystemTestFeatures;
	private SeleniumTestFeatures seleniumTestFeatures;
	private StreamsTestFeatures streamsTestFeatures;
	private SchemaTestFeatures schemaTestFeatures;
	private BatchProcessTestFeature batchProcessTestFeature;
	private FactoriesTestFeatures factoriesTestFeatures;
	private AfterTestValidationsTestFeature afterTestValidationsTestFeature;
	private SaveStateFeature saveStateFeature;
	private ToggleTestFeature toggleTestFeature;
	private SkipTestsRule skipTestsRule;

	//It is singletone pattern for all the test cases
	private ConstellioTestSession() {

	}

	public static ConstellioTestSession get() {
		return session;
	}

	public static ConstellioTestSession build(boolean isUniTest, Map<String, String> sdkProperties,
											  SkipTestsRule skipTestsRule,
											  Class<? extends AbstractConstellioTest> constellioTest,
											  boolean checkRollback) {
		session = new ConstellioTestSession();
		i18n.setLocale(Locale.FRENCH);
		TimeProvider.setTimeProvider(new DefaultTimeProvider());
		OpenedResourcesWatcher.logStackTraces = true;
		session.sdkProperties = sdkProperties;
		session.skipTestsRule = skipTestsRule;
		session.toggleTestFeature = new ToggleTestFeature(session.sdkProperties);
		OpenedResourcesWatcher.enabled = true;
		if (!isUniTest) {

			ensureLog4jAndRepositoryProperties();

			session.fileSystemTestFeatures = new FileSystemTestFeatures("temp-test", sdkProperties,
					constellioTest);
			session.factoriesTestFeatures = new FactoriesTestFeatures(session.fileSystemTestFeatures, sdkProperties,
					checkRollback);

			session.streamsTestFeatures = new StreamsTestFeatures();
			session.streamsTestFeatures.beforeTest(skipTestsRule);
			session.seleniumTestFeatures = new SeleniumTestFeatures();
			session.schemaTestFeatures = new SchemaTestFeatures(session.factoriesTestFeatures);
			session.batchProcessTestFeature = new BatchProcessTestFeature(session.factoriesTestFeatures);
			session.afterTestValidationsTestFeature = new AfterTestValidationsTestFeature(session.fileSystemTestFeatures,
					session.batchProcessTestFeature, session.factoriesTestFeatures, sdkProperties);
			session.seleniumTestFeatures.beforeTest(sdkProperties, session.factoriesTestFeatures, skipTestsRule);
			session.saveStateFeature = new SaveStateFeature(session.factoriesTestFeatures, session.fileSystemTestFeatures);
		} else {
			session.schemaTestFeatures = new SchemaTestFeatures();
		}
		if (TimeProvider.getLocalDate().getYear() < 2015) {
			throw new RuntimeException(
					"Cannot start the test, since the local date returned by the system is invalid : " + TimeProvider
							.getLocalDate());
		}
		return session;
	}

	private static boolean propertiesChecked = false;

	private static void ensureLog4jAndRepositoryProperties() {
		if (!propertiesChecked) {
			propertiesChecked = true;

			File sdkProject = new FoldersLocator().getSDKProject();
			File sdkResourcesFolder = new FoldersLocator().getSDKResourcesProject();
			File buildFolder = new File(sdkProject, "build");
			File classesFolder = new File(buildFolder, "classes");
			File classesTestFolder = new File(classesFolder, "test");

			File classesLog4J = new File(classesTestFolder, "log4j.properties");
			File sdkLog4J = new File(sdkResourcesFolder, "log4j.properties");
			if (!classesLog4J.exists()) {
				try {
					FileUtils.copyFile(sdkLog4J, classesLog4J);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			File classesRepository = new File(classesTestFolder, "repository.properties");
			File sdkRepository = new File(sdkProject, "repository.properties");
			if (!classesRepository.exists()) {
				try {
					FileUtils.copyFile(sdkRepository, classesRepository);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

		}
	}

	public static void closeAfterTestClass() {
		SeleniumTestFeatures.afterAllTests();
	}

	public void close(boolean firstClean, boolean failed) {
		Throwable exception = null;

		if (seleniumTestFeatures != null) {
			seleniumTestFeatures.afterTest(failed);
		}

		if (batchProcessTestFeature != null) {
			batchProcessTestFeature.afterTest();
		}

		if (saveStateFeature != null) {
			saveStateFeature.afterTest();
		}

		if (afterTestValidationsTestFeature != null) {
			exception = afterTestValidationsTestFeature.afterTest(firstClean, failed);
		}

		List<Runnable> runnables = new ArrayList<>();
		try {
			try {
				if (factoriesTestFeatures != null) {
					runnables = factoriesTestFeatures.afterTest();
				}
			} finally {

				if (streamsTestFeatures != null) {
					streamsTestFeatures.afterTest();
				}
				if (fileSystemTestFeatures != null) {
					fileSystemTestFeatures.close();
				}
				if (schemaTestFeatures != null) {
					schemaTestFeatures.afterTest(firstClean);
				}

				if (streamsTestFeatures != null) {
					List<String> unClosedResources = streamsTestFeatures.getUnClosedResources();
					if (!unClosedResources.isEmpty()) {
						throw new RuntimeException("Resources were not closed : " + unClosedResources.toString());
					}
				}

				TimeProvider.setTimeProvider(new DefaultTimeProvider());
				if (TimeProvider.getLocalDate().getYear() < 2015) {
					throw new RuntimeException(
							"The local date returned by the system is invalid : " + TimeProvider.getLocalDate());
				}

				if (exception != null) {
					throw new RuntimeException(exception);
				}
			}

		} finally {
			for (Runnable runnable : runnables) {
				runnable.run();
			}
		}
	}

	public AfterTestValidationsTestFeature getAfterTestValidationsTestFeature() {
		return afterTestValidationsTestFeature;
	}

	public FileSystemTestFeatures getFileSystemTestFeatures() {
		return fileSystemTestFeatures;
	}

	public SeleniumTestFeatures getSeleniumTestFeatures() {
		return seleniumTestFeatures;
	}

	public StreamsTestFeatures getStreamsTestFeatures() {
		return streamsTestFeatures;
	}

	public SchemaTestFeatures getSchemaTestFeatures() {
		return schemaTestFeatures;
	}

	public BatchProcessTestFeature getBatchProcessTestFeature() {
		return batchProcessTestFeature;
	}

	public FactoriesTestFeatures getFactoriesTestFeatures() {
		return factoriesTestFeatures;
	}

	public SaveStateFeature getSaveStateFeature() {
		return saveStateFeature;
	}

	public String getProperty(String key) {
		return sdkProperties.get(key);
	}

	public ToggleTestFeature getToggleTestFeature() {
		return toggleTestFeature;
	}

	public boolean isDeveloperTest() {
		return skipTestsRule.isMainTest() || skipTestsRule.isInDevelopmentTest();
	}

}
