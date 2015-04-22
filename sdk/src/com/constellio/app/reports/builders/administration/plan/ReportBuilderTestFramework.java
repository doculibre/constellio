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
package com.constellio.app.reports.builders.administration.plan;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;

import com.constellio.sdk.tests.ConstellioTest;

public abstract class ReportBuilderTestFramework extends ConstellioTest {

	private static boolean firstBefore = true;

	private static String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghiklmnopqrstuvwxyz ";

	private static File outputFolder;

	@BeforeClass
	public static void ReportBuilderTestFrameworkClassSetUp()
			throws Exception {

		firstBefore = true;
	}

	@Before
	public void ReportBuilderTestFrameworkSetUp()
			throws Exception {
		if (firstBefore) {
			File allReportsFolder = new File(getFoldersLocator().getSDKProject(), "generatedReports");
			outputFolder = new File(allReportsFolder, getClass().getCanonicalName());
			if (outputFolder.exists()) {
				File[] files = outputFolder.listFiles();
				if (files != null) {
					for (File file : files) {
						FileUtils.forceDelete(file);
					}
				}
			}
			outputFolder.mkdirs();
			firstBefore = false;
		}

	}

	protected void build(ReportBuilder reportBuilder) {
		String name = getTestName();
		File outputFile = new File(outputFolder, name + "." + reportBuilder.getFileExtension());
		try {
			reportBuilder.build(new FileOutputStream(outputFile));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected String textOfLength1(int length) {
		//TODO Improve to simulate normal text
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < length; i++) {
			stringBuilder.append(alphabet.charAt(i % alphabet.length()));
		}
		return stringBuilder.toString();
	}

	protected String textOfLength(int length) {
		//		Random rand = new Random();
		//		//int randomNum = rand.nextInt(alphabet.length() + 1);
		//
		//		//TODO Improve to simulate normal text
		//		StringBuilder stringBuilder = new StringBuilder();
		//		for (int i = 0; i < length; i++) {
		//			int randomNum = rand.nextInt(alphabet.length());
		//			stringBuilder.append(alphabet.charAt(randomNum));
		//		}
		//		return stringBuilder.toString();

		String text =
				"Cette sous-série regroupe les dossiers relatifs à la planification stratégique et la veille. Les dossiers portant sur les\n"
						+ "objectifs de planification administrative, les orientations prises par Services Québec, les plans directeurs et les plans\n"
						+ "d'action sont classés dans cette sous-série.\n"
						+ "Elle comprend aussi les orientations en matière d'éthique.";
		return text;
	}
}
