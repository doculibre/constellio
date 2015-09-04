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
package com.constellio.sdk.load;

import static java.util.Arrays.asList;

import java.io.File;
import java.util.List;

import demo.DemoUtils;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.sdk.load.script.SystemWithDataAndRMModuleScript;
import com.constellio.sdk.load.script.preparators.AdministrativeUnitTaxonomyPreparator;
import com.constellio.sdk.load.script.preparators.CategoriesTaxonomyPreparator;
import com.constellio.sdk.load.script.preparators.DefaultUsersPreparator;
import com.constellio.sdk.tests.TestPagesComponentsExtensions;

public class LoadSetup_SystemWithDataAndRMModuleMain {

	static List<String> COLLECTIONS = asList("zeCollection", "anotherCollection");
	static int NUMBER_OF_USERS = 10;
	static int NUMBER_OF_GROUPS = 10;

	public static void main(String[] argv)
			throws Exception {

		//		DemoUtils.clearData();

		DemoUtils.printConfiguration();

		SystemWithDataAndRMModuleScript script = new SystemWithDataAndRMModuleScript();
		script.setBigFilesFolder(new File("/Users/francisbaril/Workspaces/wiki-200000"));
		script.setNumberOfRootFolders(50000);
		//script.setNumberOfRootFolders(500);
		script.setSubFoldersPerFolder(10);
		script.setSubSubFoldersPerFolder(1);
		script.setNumberOfDocuments(250000);
		script.setCollections(COLLECTIONS);
		script.setUserPreparator(new DefaultUsersPreparator(COLLECTIONS, NUMBER_OF_USERS, NUMBER_OF_GROUPS));
		script.setAdministrativeUnitsTaxonomy(new AdministrativeUnitTaxonomyPreparator());
		script.setCategoriesTaxonomy(new CategoriesTaxonomyPreparator());
		DemoUtils.startDemoOn(8080, script);
		System.out.println("FINISHED!!!!");

		AppLayerFactory factory = ConstellioFactories.getInstance().getAppLayerFactory();
		factory.getExtensions().getSystemWideExtensions().pagesComponentsExtensions = new TestPagesComponentsExtensions(factory);
	}

}
