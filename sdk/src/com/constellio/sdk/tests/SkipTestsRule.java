package com.constellio.sdk.tests;

import com.constellio.data.conf.FoldersLocator;
import com.constellio.sdk.tests.annotations.CloudTest;
import com.constellio.sdk.tests.annotations.DoNotRunOnIntegrationServer;
import com.constellio.sdk.tests.annotations.DriverTest;
import com.constellio.sdk.tests.annotations.IgniteTest;
import com.constellio.sdk.tests.annotations.ImportTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.InternetTest;
import com.constellio.sdk.tests.annotations.LoadTest;
import com.constellio.sdk.tests.annotations.MainTest;
import com.constellio.sdk.tests.annotations.MainTestDefaultStart;
import com.constellio.sdk.tests.annotations.PerformanceTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import com.constellio.sdk.tests.annotations.UiTest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class SkipTestsRule implements TestRule {

	private static final Logger LOGGER = LoggerFactory.getLogger(SkipTestsRule.class);

	String currentTestName;
	boolean isUnitMode;
	boolean skipSlow;
	boolean skipReal;
	boolean skipLoad;
	boolean skipInDevelopment;
	boolean skipUI;
	boolean skipDriver;
	boolean runPerformance;
	boolean skipAllTests;
	boolean skipImportTests;
	boolean skipTestsWithGradle;
	boolean skipInternetTest;
	boolean checkRollback = false;
	boolean skipCloud;
	boolean skipIgnite;
	private boolean inDevelopmentTest;
	private boolean mainTest;

	private static boolean ignoredTestsFileHandled;
	private static List<String> whiteList;
	private static List<String> blackList;
	private Class<? extends AbstractConstellioTest> currentTestClass;
	private static String firstClassname;
	private static String firstTestname;

	private String sunJavaCommand;

	private boolean wasSkipped;

	public SkipTestsRule(SDKPropertiesLoader sdkPropertiesLoader, boolean isUnitMode) {

		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		Map<String, String> systemProperties = bean.getSystemProperties();

		sunJavaCommand = systemProperties.get("sun.java.command");
		this.isUnitMode = isUnitMode;
		if (!isUnitMode) {
			Map<String, String> properties = sdkPropertiesLoader.getSDKProperties();
			this.skipAllTests = "true".equals(properties.get("skip.alltests"));
			this.skipSlow = skipAllTests || "true".equals(properties.get("skip.slowtests"));
			this.skipImportTests = skipAllTests || !"false".equals(properties.get("skip.importtests"));
			this.skipReal = skipAllTests || "true".equals(properties.get("skip.realtests"));
			this.skipLoad = skipAllTests || "true".equals(properties.get("skip.loadtests"));
			this.checkRollback = "true".equals(properties.get("skip.checkRollback"));
			this.skipInternetTest = skipAllTests || "true".equals(properties.get("skip.internettests"));
			this.skipInDevelopment = skipAllTests || "true".equals(properties.get("skip.indevelopment")) || "true"
					.equals(properties.get("skip.indevelopmenttests"));
			this.skipDriver = skipAllTests || "true".equals(properties.get("skip.drivertests"));
			this.skipTestsWithGradle = skipAllTests || !("false".equals(properties.get("skip.testsWithGradle")));
			this.runPerformance = "true".equals(properties.get("run.performancetests"));
			this.skipUI = skipAllTests || "true".equals(properties.get("skip.uitests"));

			if (whiteList == null) {
				whiteList = getFilterList("tests.whitelist", properties);
				blackList = getFilterList("tests.blacklist", properties);

			}

			if (!ignoredTestsFileHandled) {
				try {
					File excludedTests = new File(new FoldersLocator().getPluginsSDKProject(), "ignored-tests.txt");
					if (excludedTests.exists()) {
						try {
							List<String> lines = FileUtils.readLines(excludedTests, "UTF-8");

							for (String line : lines) {
								line = line.trim();
								if (whiteList == null || !whiteList.contains(line)) {
									if (!blackList.contains(line)) {
										blackList.add(line);
									}
								}
							}
							ignoredTestsFileHandled = true;
						} catch (Throwable t) {
							LOGGER.warn("Error while reading ignored-tests.txt", t);
						}
					} else {
						LOGGER.warn("ignored-tests.txt does not exist");
					}
				} catch (Throwable t) {

				}
			}


			this.skipCloud = !"cloud".equals(properties.get("dao.records.type"));
			this.skipIgnite = !"ignite".equals(properties.get("dao.cache"));

			//			System.out.println("skipAllTests:" + skipAllTests);
			//			System.out.println("skipSlow:" + skipSlow);
			//			System.out.println("skipImportTests:" + skipImportTests);
			//			System.out.println("skipReal:" + skipReal);
			//			System.out.println("skipLoad:" + skipLoad);
			//			System.out.println("checkRollback:" + checkRollback);
			//			System.out.println("skipInternetTest:" + skipInternetTest);
			//			System.out.println("skipInDevelopment:" + skipInDevelopment);
			//			System.out.println("skipDriver:" + skipDriver);
			//			System.out.println("skipTestsWithGradle:" + skipTestsWithGradle);
			//			System.out.println("runPerformance:" + runPerformance);
			//			System.out.println("skipUI:" + skipUI);
			//			System.out.println("whiteList:" + whiteList);
			//			System.out.println("blackList:" + blackList);
			//			System.out.println("skipCloud:" + skipCloud);
			//			System.out.println("skipIgnite:" + skipIgnite);

		} else if (sdkPropertiesLoader != null) {
			Map<String, String> properties = sdkPropertiesLoader.getSDKProperties();
			this.skipTestsWithGradle = !("false".equals(properties.get("skip.testsWithGradle")));

		} else {
			this.skipTestsWithGradle = false;
		}
	}

	private List<String> getFilterList(String filterName, Map<String, String> properties) {
		String value = properties.get(filterName);
		List<String> filters = new ArrayList<>();

		if (StringUtils.isNotBlank(value)) {
			filters.addAll(asList(StringUtils.split(value, ",")));
		}

		return filters;
	}

	public boolean isSkipped(Class<?> testClass, Description description) {
		try {
			return wasSkipped = evaluateIfSkipped(testClass, description);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean evaluateIfSkipped(Class<?> testClass, Description description) {

		if (skipTestsWithGradle && isRunnedByGradle()) {
			System.out.println("Ignore 1");
			return true;
		}

		currentTestClass = (Class) testClass;
		currentTestName = description.getMethodName();
		if (firstClassname == null) {
			firstClassname = currentTestClass.getName();
		}
		if (firstTestname == null) {
			firstTestname = currentTestName;
		}

		boolean testClassDirectlyTargetted = isTestClassDirectlyTargetted(testClass);

		MainTest mainTestAnnotation = testClass.getAnnotation(MainTest.class);
		SlowTest slowTest = testClass.getAnnotation(SlowTest.class);
		LoadTest loadTest = testClass.getAnnotation(LoadTest.class);
		UiTest uiTest = testClass.getAnnotation(UiTest.class);
		ImportTest importTest = testClass.getAnnotation(ImportTest.class);
		InternetTest internetTest = testClass.getAnnotation(InternetTest.class);
		DriverTest driverTest = testClass.getAnnotation(DriverTest.class);
		PerformanceTest performanceTest = testClass.getAnnotation(PerformanceTest.class);
		InDevelopmentTest inDevelopmentTestAnnotation = testClass.getAnnotation(InDevelopmentTest.class);
		DoNotRunOnIntegrationServer doNotRunOnIntegrationServer = testClass.getAnnotation(DoNotRunOnIntegrationServer.class);
		CloudTest cloudTest = testClass.getAnnotation(CloudTest.class);
		IgniteTest igniteTest = testClass.getAnnotation(IgniteTest.class);

		if (importTest == null && testClass.getSuperclass() != null) {
			importTest = testClass.getSuperclass().getAnnotation(ImportTest.class);
		}

		boolean isRealTest = !ConstellioTest.isUnitTest(testClass.getSimpleName());
		inDevelopmentTest = inDevelopmentTestAnnotation != null || description.getAnnotation(InDevelopmentTest.class) != null;
		mainTest = mainTestAnnotation != null;

		if (isTestDirectlyTargetted(testClass, currentTestName)) {
			//No matter which parameters are defined, the test is runned
			return false;
		}

		if (!testClassDirectlyTargetted && isClassFiltered(testClass)) {
			System.out.println("Ignore 2");
			return true;
		}

		Class<?> testSuperClass = testClass.getSuperclass();
		if (!testClassDirectlyTargetted && testSuperClass != null && "ConstellioImportAcceptTest"
				.equals(testSuperClass.getSimpleName()) && skipImportTests) {
			System.out.println("Ignore 3a");
			return true;
		}

		if (!testClassDirectlyTargetted && importTest != null && skipImportTests) {
			System.out.println("Ignore 3b");
			return true;
		}

		if (mainTestAnnotation != null) {
			if (!testClassDirectlyTargetted) {
				System.out.println("Ignore 4");
				return true;

			} else if (hasMainTestOnlyOneStarter(testClass)) {
				return false;

			} else {
				return description.getAnnotation(MainTestDefaultStart.class) == null;
			}
		}

		if (internetTest == null) {
			internetTest = description.getAnnotation(InternetTest.class);
		}
		if (slowTest == null) {
			slowTest = description.getAnnotation(SlowTest.class);
		}

		if (cloudTest == null) {
			cloudTest = description.getAnnotation(CloudTest.class);
		}

		if (loadTest == null) {
			loadTest = description.getAnnotation(LoadTest.class);
		}
		if (doNotRunOnIntegrationServer == null) {
			doNotRunOnIntegrationServer = description.getAnnotation(DoNotRunOnIntegrationServer.class);
		}
		if (inDevelopmentTestAnnotation == null) {
			inDevelopmentTestAnnotation = description.getAnnotation(InDevelopmentTest.class);
		}

		if (doNotRunOnIntegrationServer != null && TestUtils.isIntegrationServer()) {
			System.out.println("Ignore 5");
			return true;
		} else if (loadTest != null && skipLoad) {
			System.out.println("Ignore 6");
			return true;
		} else if (!testClassDirectlyTargetted && uiTest != null && skipUI) {
			System.out.println("Ignore 7");
			return true;
		} else if (!testClassDirectlyTargetted && driverTest != null && skipDriver) {
			System.out.println("Ignore 8");
			return true;
		} else if (!testClassDirectlyTargetted && internetTest != null && skipInternetTest) {
			System.out.println("Ignore 9");
			return true;
		} else if (!testClassDirectlyTargetted && slowTest != null && skipSlow) {
			System.out.println("Ignore 10");
			return true;
		} else if (!testClassDirectlyTargetted && performanceTest != null && !runPerformance) {
			System.out.println("Ignore 11");
			return true;
		} else if (inDevelopmentTestAnnotation != null && skipInDevelopment) {
			System.out.println("Ignore 12");
			return true;
		} else if (!testClassDirectlyTargetted && isRealTest && skipReal) {
			System.out.println("Ignore 13");
			return true;
		} else if (!testClassDirectlyTargetted && cloudTest != null && skipCloud) {
			System.out.println("Ignore 14");
			return true;
		} else if (!testClassDirectlyTargetted && igniteTest != null && skipIgnite) {
			System.out.println("Ignore 15");
			return true;
		} else {
			return false;
		}
	}

	private boolean isRunnedByGradle() {
		return sunJavaCommand != null && sunJavaCommand.toLowerCase().contains("gradle");
	}

	private boolean hasMainTestOnlyOneStarter(Class<?> testClass) {

		int testMethodsCount = 0;

		for (Method method : testClass.getDeclaredMethods()) {
			if (method.getAnnotation(org.junit.Test.class) != null) {
				testMethodsCount++;
			}
		}

		return testMethodsCount == 1;
	}

	private boolean isTestDirectlyTargetted(Class<?> testClass, String currentTestName) {

		if (isIntelliJTestRunner()) {
			return sunJavaCommand.contains(testClass.getName() + "," + currentTestName);

		} else if (isEclipseTestRunner()) {
			return sunJavaCommand.contains(testClass.getName() + ":" + currentTestName);

		} else {
			return currentTestClass.getName().equals(firstClassname)
				   && currentTestName.equals(firstTestname);
		}
	}

	private boolean isTestClassDirectlyTargetted(Class<?> testClass) {

		if (isIntelliJTestRunner()) {
			return sunJavaCommand.contains(testClass.getName());

		} else if (isEclipseTestRunner()) {
			return sunJavaCommand.contains(testClass.getName() + ":" + currentTestName);

		} else {
			return currentTestClass.getName().equals(firstClassname);
		}
	}

	private boolean isEclipseTestRunner() {
		return sunJavaCommand.contains("org.eclipse.jdt.internal.junit.runner.RemoteTestRunner");
	}

	private boolean isIntelliJTestRunner() {
		return sunJavaCommand.contains("com.intellij.rt.execution.junit.JUnitStarter");
	}

	private boolean isClassFiltered(Class<?> testClass) {
		if (whiteList != null && !whiteList.isEmpty()) {
			for (String whiteFilter : whiteList) {
				if (isFilteredBy(testClass, whiteFilter)) {
					for (String blackFilter : blackList) {
						if (isFilteredBy(testClass, blackFilter)) {
							return true;
						}
					}
					return false;
				}
			}

			return true;
		} else if (blackList != null && !blackList.isEmpty()) {
			for (String blackFilter : blackList) {
				if (isFilteredBy(testClass, blackFilter)) {
					return true;
				}
			}

		}

		return false;
	}

	private boolean isFilteredBy(Class<?> testClass, String filter) {
		if (filter.startsWith("*") && filter.endsWith("*")) {
			return testClass.getName().contains(filter.replace("*", ""));

		} else if (filter.startsWith("*")) {
			return testClass.getName().endsWith(filter.replace("*", ""));

		} else if (filter.endsWith("*")) {
			return testClass.getName().startsWith(filter.replace("*", ""));

		} else {
			return filter.equals(testClass.getName());
		}
	}

	@Override
	public Statement apply(final Statement base, Description description) {

		//		if (isUnitMode) {
		//			return base;
		//		}

		if (isSkipped(description.getTestClass(), description)) {
			return newSkipStatement();
		} else {
			return base;
		}
	}

	//	private boolean isRunningNormallySkippedTest() {
	//		String javaCommand = ManagementFactory.getRuntimeMXBean().getSystemProperties().get("sun.java.command");
	//		return javaCommand.contains("eclipse") && javaCommand.contains("-classNames");
	//	}

	private Statement newSkipStatement() {
		return new Statement() {

			@Override
			public void evaluate()
					throws Throwable {
				throw new AssumptionViolatedException("Test is skipped");
			}
		};
	}

	public boolean isInDevelopmentTest() {
		return inDevelopmentTest;
	}

	public boolean isMainTest() {
		return mainTest;
	}

	public String getCurrentTestName() {
		return currentTestName;
	}

	public Class<? extends AbstractConstellioTest> getCurrentTestClass() {
		return currentTestClass;
	}

	public boolean wasSkipped() {
		return wasSkipped;
	}
}
