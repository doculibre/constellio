package com.constellio.app.reports.builders.administration.plan;

import com.constellio.app.ui.framework.reports.ReportWriter;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
			File allReportsFolder = new File(new FoldersLocator().getSDKProject(), "generatedReports");
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

	protected File write(ReportWriter reportWriter) {
		return build(reportWriter);
	}

	protected File build(ReportWriter reportWriter) {
		String name = getTestName();
		File outputFile = new File(outputFolder, name + "." + reportWriter.getFileExtension());
		try {
			reportWriter.write(new FileOutputStream(outputFile));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return outputFile;
	}

	protected void buildAndOpen(ReportWriter reportWriter) {
		File outputFile = write(reportWriter);
		open(outputFile.getAbsolutePath());
	}

	protected void open(String filepath) {

		try {
			String openCommand;
			if (SystemUtils.IS_OS_WINDOWS) {
				openCommand = "explorer.exe \"" + filepath + "\"";
			} else if (SystemUtils.IS_OS_LINUX) {
				openCommand = "xdg-open " + "\"" + filepath + "\"";
			} else if (SystemUtils.IS_OS_MAC) {
				openCommand = "open " + "\"" + filepath + "\"";
			} else {
				openCommand = null;
			}
			executeCommand(openCommand);
		} catch (Exception e) {

		}

	}

	private Process executeCommand(String command)
			throws IOException {
		if (SystemUtils.IS_OS_WINDOWS) {
			return Runtime.getRuntime().exec(command);
		} else {
			String[] arguments = new String[]{"/bin/sh", "-c", command};
			return Runtime.getRuntime().exec(arguments);
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
