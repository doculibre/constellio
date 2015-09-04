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

import org.junit.Before;
import org.mockito.MockitoAnnotations;

import com.constellio.sdk.tests.annotations.PreserveState;

public class ConstellioTest extends AbstractConstellioTest {

	private static boolean isCurrentPreservingState;

	private static boolean IS_FIRST_EXECUTED_TEST = true;

	private ConstellioTestSession testSession;

	@Override
	protected ConstellioTestSession getCurrentTestSession() {
		return testSession;
	}

	@Override
	public void afterTest(boolean failed) {
		testSession.close(false, failed);
	}

	@Before
	public void beforeConstellioTest() {
		MockitoAnnotations.initMocks(this);

		testSession = ConstellioTestSession.build(isUnitTest(), sdkProperties, skipTestRule, getClass());
		if (!isKeepingPreviousState() && testSession.getFactoriesTestFeatures() != null && IS_FIRST_EXECUTED_TEST) {
			testSession.getFactoriesTestFeatures().getConstellioFactories();
			testSession.close(true, false);
			testSession = ConstellioTestSession.build(isUnitTest(), sdkProperties, skipTestRule, getClass());
			IS_FIRST_EXECUTED_TEST = false;
		}
		if (isPreservingState()) {
			PreserveState preserveStateAnnotation = getClass().getAnnotation(PreserveState.class);
			if (preserveStateAnnotation.enabled()) {
				testSession.getFileSystemTestFeatures()
						.setPreservedState(getClass().getName() + "-" + preserveStateAnnotation.state());
			}
		}
	}

	protected void clearTestSession() {
		if (!isPreservingState()) {
			testSession.close(false, false);
			testSession = ConstellioTestSession.build(isUnitTest(), sdkProperties, skipTestRule, getClass());
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
}
