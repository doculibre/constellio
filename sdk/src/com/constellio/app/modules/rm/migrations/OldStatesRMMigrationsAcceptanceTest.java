package com.constellio.app.modules.rm.migrations;

import com.constellio.sdk.tests.annotations.SlowTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// Confirm @SlowTest
@RunWith(value = Parameterized.class)
public class OldStatesRMMigrationsAcceptanceTest extends RMMigrationsAcceptanceTest {

	public OldStatesRMMigrationsAcceptanceTest(String testCase) {
		super(testCase);
	}

	@Test
	public void testAllOldVersions()
			throws Exception {
		setUp(true);

		validateZeCollectionState();
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> testCases() {
		List<Object[]> states = new ArrayList<>();
		states.add(new Object[]{"givenNewInstallation"});

		for (String state : getStatesFolder(true).list()) {
			if (state.endsWith(".zip") && (state.contains("_rm_") || state.contains(",rm_"))) {
				states.add(new Object[]{state.replace(".zip", "")});
			}
		}

		return states;

	}
}
