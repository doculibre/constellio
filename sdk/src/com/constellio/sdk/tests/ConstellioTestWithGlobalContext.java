package com.constellio.sdk.tests;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;

public class ConstellioTestWithGlobalContext extends AbstractConstellioTest {

	private static ConstellioTestSession testSession;
	//	@Rule
	//	public TestRule benchmarkRun = new BenchmarkRule();

	@Before
	public void verifyCorrectlyUsed() {

		MockitoAnnotations.initMocks(this);

		if (!getClass().equals(ConstellioTestWithGlobalContext.class)) {
			FixMethodOrder order = getClass().getAnnotation(FixMethodOrder.class);
			if (order == null || order.value() != MethodSorters.NAME_ASCENDING) {
				throw new RuntimeException(
						"Must declare '@FixMethodOrder(MethodSorters.NAME_ASCENDING)' in the class declaration");
			}

			boolean prepareTestsValid = false;
			try {
				Method method = getClass().getMethod("__prepareTests__");
				prepareTestsValid = method.getAnnotation(Test.class) != null;
			} catch (Exception e) {
			}

			if (!prepareTestsValid) {
				throw new RuntimeException("Must declare '@Test public void __prepareTests__()' test");
			}
		}

		if (testSession == null) {
			testSession = ConstellioTestSession.build(isUnitTest(), sdkProperties, skipTestRule, getClass(), checkRollback());
		}

	}

	@Test
	public void __teardownTests__() {
		testSession.close(false, false, false);
		testSession = null;
	}

	@Override
	protected ConstellioTestSession getCurrentTestSession() {
		return testSession;
	}

	@Override
	public void afterTest(boolean failed) {
		//Nothing
	}
}
