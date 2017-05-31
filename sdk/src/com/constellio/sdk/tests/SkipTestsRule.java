package com.constellio.sdk.tests;

import static com.constellio.sdk.tests.TestUtils.asList;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.constellio.sdk.tests.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class SkipTestsRule implements TestRule {

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
	private boolean inDevelopmentTest;
	private boolean mainTest;
	private List<String> whiteList;
	private List<String> blackList;
	private Class<? extends AbstractConstellioTest> currentTestClass;

	private String sunJavaCommand;

	private boolean wasSkipped;

	public SkipTestsRule(SDKPropertiesLoader sdkPropertiesLoader, boolean isUnitMode) {

		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		Map<String, String> systemProperties = bean.getSystemProperties();
		sunJavaCommand = systemProperties.get("sun.java.command");
		//		String bootClasspath = bean.getBootClassPath();
		//		String classpath = bean.getClassPath();
		//		String libraryPath = bean.getLibraryPath();
		//		List<String> inputArguments = bean.getInputArguments();
		//		System.out.println(inputArguments);
		//		System.out.println(systemProperties);
		//		System.out.println(bootClasspath);
		//		System.out.println(classpath);
		//		System.out.println(libraryPath);
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
			this.skipTestsWithGradle = skipAllTests || "true".equals(properties.get("skip.testsWithGradle"));
			this.runPerformance = "true".equals(properties.get("run.performancetests"));
			this.skipUI = skipAllTests || "true".equals(properties.get("skip.uitests"));
			this.whiteList = getFilterList("tests.whitelist", properties);
			this.blackList = getFilterList("tests.blacklist", properties);
			this.skipCloud = !"cloud".equals(properties.get("dao.records.type"));
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

		currentTestClass = (Class) testClass;
		currentTestName = description.getMethodName();

		boolean testClassDirectlyTargetted = isTestClassDirectlyTargetted(testClass);

		MainTest mainTestAnnotation = testClass.getAnnotation(MainTest.class);
		SlowTest slowTest = testClass.getAnnotation(SlowTest.class);
		LoadTest loadTest = testClass.getAnnotation(LoadTest.class);
		UiTest uiTest = testClass.getAnnotation(UiTest.class);
		InternetTest internetTest = testClass.getAnnotation(InternetTest.class);
		DriverTest driverTest = testClass.getAnnotation(DriverTest.class);
		PerformanceTest performanceTest = testClass.getAnnotation(PerformanceTest.class);
		InDevelopmentTest inDevelopmentTestAnnotation = testClass.getAnnotation(InDevelopmentTest.class);
		DoNotRunOnIntegrationServer doNotRunOnIntegrationServer = testClass.getAnnotation(DoNotRunOnIntegrationServer.class);
		CloudTest cloudTest = testClass.getAnnotation(CloudTest.class);

		boolean isRealTest = !ConstellioTest.isUnitTest(testClass.getSimpleName());
		inDevelopmentTest = inDevelopmentTestAnnotation != null || description.getAnnotation(InDevelopmentTest.class) != null;
		mainTest = mainTestAnnotation != null;

		if (skipTestsWithGradle && isRunnedByGradle()) {
			return true;
		}

		if (isTestDirectlyTargetted(testClass, currentTestName)) {
			//No matter which parameters are defined, the test is runned
			return false;
		}

		if (!testClassDirectlyTargetted && isClassFiltered(testClass)) {
			return true;
		}

		Class<?> testSuperClass = testClass.getSuperclass();
		if (!testClassDirectlyTargetted && testSuperClass != null && "ConstellioImportAcceptTest"
				.equals(testSuperClass.getSimpleName()) && skipImportTests) {
			return true;
		}

		if (mainTestAnnotation != null) {
			if (!testClassDirectlyTargetted) {
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

		if(cloudTest == null) {
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
			return true;

		} else if (loadTest != null && skipLoad) {
			return true;

		} else if (!testClassDirectlyTargetted && uiTest != null && skipUI) {
			return true;

		} else if (!testClassDirectlyTargetted && driverTest != null && skipDriver) {
			return true;

		} else if (!testClassDirectlyTargetted && internetTest != null && skipInternetTest) {
			return true;

		} else if (!testClassDirectlyTargetted && slowTest != null && skipSlow) {
			return true;

		} else if (!testClassDirectlyTargetted && performanceTest != null && !runPerformance) {
			return true;

		} else if (inDevelopmentTestAnnotation != null && skipInDevelopment) {
			return true;

		} else if (!testClassDirectlyTargetted && isRealTest && skipReal) {
			return true;
		} else if (!testClassDirectlyTargetted && cloudTest != null && skipCloud) {
			return true;

		} else {
			return false;
		}
	}

	private boolean isRunnedByGradle() {
		return sunJavaCommand.toLowerCase().contains("gradle");
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
			return false;
		}
	}

	private boolean isTestClassDirectlyTargetted(Class<?> testClass) {

		if (isIntelliJTestRunner()) {
			return sunJavaCommand.contains(testClass.getName());

		} else if (isEclipseTestRunner()) {
			return sunJavaCommand.contains(testClass.getName() + ":" + currentTestName);

		} else {
			return false;
		}
	}

	private boolean isEclipseTestRunner() {
		return sunJavaCommand.contains("org.eclipse.jdt.internal.junit.runner.RemoteTestRunner");
	}

	private boolean isIntelliJTestRunner() {
		return sunJavaCommand.contains("com.intellij.rt.execution.junit.JUnitStarter");
	}

	private boolean isClassFiltered(Class<?> testClass) {
		if (!whiteList.isEmpty()) {
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
		} else if (!blackList.isEmpty()) {
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
