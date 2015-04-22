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

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runners.MethodSorters;
import org.mockito.MockitoAnnotations;

import com.carrotsearch.junitbenchmarks.BenchmarkRule;

public class ConstellioTestWithGlobalContext extends AbstractConstellioTest {

	private static ConstellioTestSession testSession;
	@Rule
	public TestRule benchmarkRun = new BenchmarkRule();

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
			testSession = ConstellioTestSession.build(isUnitTest(), sdkProperties, skipTestRule, getClass());
		}

	}

	@Test
	public void __teardownTests__() {
		testSession.close(false, false);
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
