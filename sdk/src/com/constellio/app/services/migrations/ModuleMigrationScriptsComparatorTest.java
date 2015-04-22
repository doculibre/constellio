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
