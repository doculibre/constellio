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

import static com.constellio.sdk.tests.TestUtils.asList;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.constellio.sdk.tests.annotations.DoNotRunOnIntegrationServer;
import com.constellio.sdk.tests.annotations.DriverTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.LoadTest;
import com.constellio.sdk.tests.annotations.PerformanceTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import com.constellio.sdk.tests.annotations.UiTest;

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
	private boolean inDevelopmentTest;
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
			this.skipReal = skipAllTests || "true".equals(properties.get("skip.realtests"));
			this.skipLoad = skipAllTests || "true".equals(properties.get("skip.loadtests"));
			this.skipInDevelopment = skipAllTests || "true".equals(properties.get("skip.indevelopment")) || "true"
					.equals(properties.get("skip.indevelopmenttests"));
			this.skipDriver = skipAllTests || "true".equals(properties.get("skip.drivertests"));
			this.runPerformance = "true".equals(properties.get("run.performancetests"));
			this.skipUI = skipAllTests || "true".equals(properties.get("skip.uitests"));
			this.whiteList = getFilterList("tests.whitelist", properties);
			this.blackList = getFilterList("tests.blacklist", properties);
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
		return wasSkipped = evaluateIfSkipped(testClass, description);
	}

	public boolean evaluateIfSkipped(Class<?> testClass, Description description) {
		currentTestClass = (Class) testClass;
		currentTestName = description.getMethodName();
		SlowTest slowTest = testClass.getAnnotation(SlowTest.class);
		LoadTest loadTest = testClass.getAnnotation(LoadTest.class);
		UiTest uiTest = testClass.getAnnotation(UiTest.class);
		DriverTest driverTest = testClass.getAnnotation(DriverTest.class);
		PerformanceTest performanceTest = testClass.getAnnotation(PerformanceTest.class);
		InDevelopmentTest inDevelopmentTestAnnotation = testClass.getAnnotation(InDevelopmentTest.class);
		DoNotRunOnIntegrationServer doNotRunOnIntegrationServer = testClass.getAnnotation(DoNotRunOnIntegrationServer.class);

		boolean isRealTest = !ConstellioTest.isUnitTest(testClass.getSimpleName());
		inDevelopmentTest = inDevelopmentTestAnnotation != null;
		//
		//		if (sunJavaCommand.contains(testClass.getName())) {
		//			return false;
		//		}

		if (isClassSkipped(testClass)) {
			return true;
		}

		if (slowTest == null) {
			slowTest = description.getAnnotation(SlowTest.class);
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

		} else if (uiTest != null && skipUI) {
			return true;

		} else if (driverTest != null && skipDriver) {
			return true;

		} else if (slowTest != null && skipSlow) {
			return true;

		} else if (performanceTest != null && !runPerformance) {
			return true;

		} else if (inDevelopmentTestAnnotation != null && skipInDevelopment) {
			return true;

		} else if (isRealTest && skipReal) {
			return true;

		} else {
			return false;
		}
	}

	private boolean isClassSkipped(Class<?> testClass) {
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
	public Statement apply(Statement base, Description description) {

		boolean runNormallySkippedTests = isRunningNormallySkippedTest();

		if (runNormallySkippedTests || isUnitMode) {
			return base;
		}

		if (isSkipped(description.getTestClass(), description)) {
			return newSkipStatement();
		} else {
			return base;
		}
	}

	private boolean isRunningNormallySkippedTest() {
		String javaCommand = ManagementFactory.getRuntimeMXBean().getSystemProperties().get("sun.java.command");
		return javaCommand.contains("eclipse") && javaCommand.contains("-classNames");
	}

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
