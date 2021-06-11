package com.constellio.app.services.migrations;

import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.complementary.ESRMRobotsModule;
import com.constellio.app.modules.es.ConstellioESModule;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.migrations.RMMigrationFrom9_3_UpdateTokensCalculator;
import com.constellio.app.modules.robots.ConstellioRobotsModule;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class ModuleMigrationScriptsComparatorRemovalAcceptanceTest extends ConstellioTest {

	@Test
	public void validateAllMigrationScriptsPriorTo9_3AreSortedInTheListAccordingToTheirVersion() {
		validateScriptsPriorToVersion9_3AreInOrder(new ConstellioRMModule().getMigrationScripts());
		validateScriptsPriorToVersion9_3AreInOrder(new ConstellioESModule().getMigrationScripts());
		validateScriptsPriorToVersion9_3AreInOrder(new TaskModule().getMigrationScripts());
		validateScriptsPriorToVersion9_3AreInOrder(new ConstellioRobotsModule().getMigrationScripts());
		validateScriptsPriorToVersion9_3AreInOrder(new ESRMRobotsModule().getMigrationScripts());
	}

	public static void validateScriptsPriorToVersion9_3AreInOrder(List<MigrationScript> scripts) {

		scripts = scripts.stream().filter(s -> new VersionsComparator().compare(s.getVersion(), "9.4") < 0
											   && !s.getClass().equals(RMMigrationFrom9_3_UpdateTokensCalculator.class)).collect(toList());

		//		List<MigrationScript> sortedScripts = new ArrayList<>(scripts);
		//		sortedScripts.sort(new ModuleMigrationScriptsComparator());

		assertThat(scripts).isSortedAccordingTo(new ModuleMigrationScriptsComparator());


	}

	private static class ModuleMigrationScriptsComparator implements Comparator<MigrationScript> {

		@Override
		public int compare(MigrationScript script1, MigrationScript script2) {
			return new VersionsComparator().compare(script1.getVersion(), script2.getVersion());
		}

	}


}
