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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.constellio.sdk.tests.SolrSDKToolsServices.VaultSnapshot;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;

@UiTest
public class InitialStateSaverAcceptTest extends ConstellioTest {

	public static final LocalDateTime DUMMY_LOCAL_DATE = new LocalDateTime(2015, 4, 21, 16, 42, 42);

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	//@Test
	//This is not a test, but a tool to build states
	//@Test
	public void saveCurrentInitialState()
			throws Exception {

		givenTransactionLogIsEnabled();
		givenCollection(zeCollection).withConstellioRMModule();

		getSaveStateFeature().saveCurrentStateToInitialStatesFolder();
	}

	//This is not a test, but a tool to build states
	@Test
	@InDevelopmentTest
	public void saveModifiedState()
			throws Exception {

		givenTransactionLogIsEnabled();
		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();

		getSaveStateFeature().saveStateAfterTestWithTitle("with_medium_types_created_manually");

		newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		waitUntilICloseTheBrowsers();

	}

	@Test
	public void givenInCurrentVersionWhenSavingStateTwiceThenSameResult()
			throws Exception {

		givenDisabledAfterTestValidations();
		givenTransactionLogIsEnabled();
		givenTimeIs(DUMMY_LOCAL_DATE);
		givenCollection(zeCollection).withConstellioRMModule();

		File state1 = folder.newFile("state1.zip");
		File state2 = folder.newFile("state2.zip");
		getSaveStateFeature().saveCurrentStateTo(state1);
		getSaveStateFeature().saveCurrentStateTo(state2);
		assertThatStatesAreEqual(state1, state2);
		VaultSnapshot expectedSnapshot = new SolrSDKToolsServices(getDataLayerFactory().newRecordDao()).snapshot();

		clearTestSession();
		givenTransactionLogIsEnabled();
		getSaveStateFeature().loadStateFrom(state1);

		File state3 = folder.newFile("state3.zip");
		getSaveStateFeature().saveCurrentStateTo(state3);
		assertThatStatesAreEqual(state1, state3);

		VaultSnapshot newSnapshot = new SolrSDKToolsServices(getDataLayerFactory().newRecordDao()).snapshot();
		new SolrSDKToolsServices(getDataLayerFactory().newRecordDao()).ensureSameSnapshots("test", expectedSnapshot, newSnapshot);

		System.out.println(folder.getRoot().getAbsolutePath());

	}

	private void assertThatStatesAreEqual(File state1, File state2)
			throws Exception {
		File state1TempFolder = newTempFolder();
		File state2TempFolder = newTempFolder();

		getIOLayerFactory().newZipService().unzip(state1, state1TempFolder);
		getIOLayerFactory().newZipService().unzip(state2, state2TempFolder);

		verifySameContent(state1TempFolder, state2TempFolder);
	}

	private void verifySameContent(File state1TempFolder, File state2TempFolder)
			throws IOException {
		if (state1TempFolder.isDirectory()) {

			List<String> filesInFolder1 = getFilesIn(state1TempFolder);
			List<String> filesInFolder2 = getFilesIn(state2TempFolder);

			assertThat(filesInFolder1).isEqualTo(filesInFolder2);

			for (String file : filesInFolder1) {
				verifySameContent(new File(state1TempFolder, file), new File(state2TempFolder, file));
			}

		} else {
			String file1Content = FileUtils.readFileToString(state1TempFolder);
			String file2Content = FileUtils.readFileToString(state2TempFolder);
			assertThat(file1Content).describedAs("File " + state1TempFolder.getAbsolutePath()).isEqualTo(file2Content);
		}
	}

	private List<String> getFilesIn(File folder) {
		String[] files = folder.list();
		if (files == null) {
			return new ArrayList<>();
		} else {
			List<String> fileList = new ArrayList<>(asList(files));
			Collections.sort(fileList);
			return fileList;
		}

	}

}
