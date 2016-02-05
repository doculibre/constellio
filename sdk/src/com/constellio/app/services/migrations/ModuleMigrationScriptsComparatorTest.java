package com.constellio.app.services.migrations;

import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.sdk.tests.ConstellioTest;

public class ModuleMigrationScriptsComparatorTest extends ConstellioTest {

	@Mock MigrationScript migrationScript1;
	@Mock MigrationScript migrationScript2;
	@Mock MigrationScript migrationScript3;
	@Mock MigrationScript migrationScript4;
	@Mock MigrationScript migrationScript5;
	@Mock MigrationScript migrationScript6;
	@Mock MigrationScript migrationScript7;

	String version111 = "1.1.1";
	String version1 = "1";
	String version213 = "2.1.3";
	String version254 = "2.5.4";
	String version4 = "4";
	String version011 = "0.1.1";
	String version001 = "0.0.1";

	@Test
	public void whenSortingModuleListThenOrderIsRight() {
		List<MigrationScript> migrationScripts = Arrays.asList(migrationScript3, migrationScript2, migrationScript4,
				migrationScript1, migrationScript6, migrationScript5, migrationScript7);
		when(migrationScript1.getVersion()).thenReturn(version001);
		when(migrationScript2.getVersion()).thenReturn(version011);
		when(migrationScript3.getVersion()).thenReturn(version1);
		when(migrationScript4.getVersion()).thenReturn(version111);
		when(migrationScript5.getVersion()).thenReturn(version213);
		when(migrationScript6.getVersion()).thenReturn(version254);
		when(migrationScript7.getVersion()).thenReturn(version4);

		Collections.sort(migrationScripts, new ModuleMigrationScriptsComparator());
	}
}
