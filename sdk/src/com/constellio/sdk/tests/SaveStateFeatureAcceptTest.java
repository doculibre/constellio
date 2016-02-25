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

public class SaveStateFeatureAcceptTest extends ConstellioTest {

	public static final LocalDateTime DUMMY_LOCAL_DATE = new LocalDateTime(2015, 4, 21, 16, 42, 42);

	public static void verifySameContentOfUnzippedSaveState(File state1TempFolder, File state2TempFolder)
			throws IOException {
		if (state1TempFolder.isDirectory()) {

			List<String> filesInFolder1 = getFilesIn(state1TempFolder);
			List<String> filesInFolder2 = getFilesIn(state2TempFolder);

			assertThat(filesInFolder1).isEqualTo(filesInFolder2);

			for (String file : filesInFolder1) {
				verifySameContentOfUnzippedSaveState(new File(state1TempFolder, file), new File(state2TempFolder, file));
			}

		} else {
			String file1Content = FileUtils.readFileToString(state1TempFolder);
			String file2Content = FileUtils.readFileToString(state2TempFolder);
			assertThat(file1Content).describedAs("File " + state1TempFolder.getAbsolutePath()).isEqualTo(file2Content);
		}
	}

	public static List<String> getFilesIn(File folder) {
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
