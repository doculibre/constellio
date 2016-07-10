package com.constellio.sdk.tests;

import java.util.HashMap;
import java.util.Map;

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

		testSession = ConstellioTestSession.build(isUnitTest(), sdkProperties, skipTestRule, getClass(), checkRollback());
		if (!isKeepingPreviousState() && testSession.getFactoriesTestFeatures() != null && IS_FIRST_EXECUTED_TEST) {

			testSession.getFactoriesTestFeatures().clear();
			//			try {
			//				testSession.getFactoriesTestFeatures().getConstellioFactories();
			//			} catch (Exception e) {
			//
			//			}
			testSession.close(true, false);

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
	}

	public void resetTestSession() {
		testSession.close(true, false);

		testSession = ConstellioTestSession.build(isUnitTest(), sdkProperties, skipTestRule, getClass(), checkRollback());
	}

	protected void clearTestSession() {
		if (!isPreservingState()) {
			testSession.close(false, false);
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
}
