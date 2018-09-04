package com.constellio.app.modules.rm.migrations;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(value = Parameterized.class)
public class RecentStatesRMMigrationsAcceptanceTest extends RMMigrationsAcceptanceTest {

	public RecentStatesRMMigrationsAcceptanceTest(String testCase) {
		super(testCase);
	}

	@Test
	public void testAllRecentVersions()
			throws Exception {
		setUp(false);
		validateZeCollectionState();
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> testCases() {
		List<Object[]> states = new ArrayList<>();
		states.add(new Object[]{"givenNewInstallation"});

		for (String state : getStatesFolder(false).list()) {
			if (state.endsWith(".zip") && (state.contains("_rm_") || state.contains(",rm_"))) {
				String name = state.replace(".zip", "");

				states.add(new Object[]{name});
			}
		}

		return states;

	}
}
