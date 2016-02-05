package com.constellio.sdk.dev.tools;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

public class SynchronizeOpenSourceVersionMain {

	//The .git folder will be copied in this folder
	private static final File BACK_UP_GIT_DIR = new File("/Users/francisbaril/IdeaProjects/backupGit");
	private static final File PRIVATE_PROJECT_DIR = new File("/Users/francisbaril/IdeaProjects/intelligid-dev");
	private static final File PUBLIC_PROJECT_DIR = new File("/Users/francisbaril/IdeaProjects/constellio-open-source");

	public static void main(String argv[])
			throws Exception {

		BACK_UP_GIT_DIR.mkdirs();

		File backupGit = new File(BACK_UP_GIT_DIR, ".git");
		FileUtils.deleteDirectory(backupGit);

		File openSourceGitFolder = new File(PUBLIC_PROJECT_DIR, ".git");
		FileUtils.copyDirectory(openSourceGitFolder, backupGit);

		File headerFile = new File(SynchronizeOpenSourceVersionMain.class.getResource("header.txt").getFile());
		String header = FileUtils.readFileToString(headerFile);

		FileUtils.deleteDirectory(PUBLIC_PROJECT_DIR);

		PUBLIC_PROJECT_DIR.mkdirs();

		FileUtils.copyDirectory(PRIVATE_PROJECT_DIR, PUBLIC_PROJECT_DIR);

		for (File file : FileUtils.listFiles(PUBLIC_PROJECT_DIR, new String[] { "java" }, true)) {
			System.out.println(file.getAbsolutePath());
			String content = FileUtils.readFileToString(file);
			if (!content.startsWith("/*")) {
				FileUtils.write(file, "/*" + header + "\n*/\n" + content, false);
			}
		}

		IOFileFilter acceptAll = new IOFileFilter() {
			@Override
			public boolean accept(File file) {
				return true;
			}

			@Override
			public boolean accept(File dir, String name) {
				return true;
			}
		};

		for (File file : FileUtils.listFiles(PUBLIC_PROJECT_DIR, acceptAll, acceptAll)) {
			if (file.exists() && file.getName().startsWith(".")) {
				if (file.isDirectory()) {
					FileUtils.deleteDirectory(file);
				} else {
					file.delete();
				}
			}
		}

		delete("sdk/src/oasiq");
		delete("sdk/src/com/constellio/sdk/dev/tools/SynchronizeOpenSourceVersionMain.java");
		delete("data/src/com/constellio/data/test/ZeStamp.java");
		delete("sdk/src/com/constellio/app/ui/StartWithSaveStateAcceptTest.java");
		delete("sdk/src/com/constellio/app/ui/StartWithSaveStateAcceptTest.java");
		delete("alfresco-file-server");
		delete("benchmarks");
		delete("benckmarks");
		delete("bin");
		delete("build");
		delete("classes");
		delete("doc");
		delete("Constellio Desktop Agent");
		delete("connectors");
		delete("custom");
		delete("dist");
		delete("migration");
		delete("solrHome");
		delete("migration");
		delete("temp");
		delete("temp-test");
		delete("update-client");
		delete("web");
		delete("constellio.log");
		delete("constellio.log.1");
		delete("rebel.xml");
		delete("rules.checkstyle");
		delete("eclipse-preferences.epf");
		delete("intelligid-dev.iml");
		delete("infinitest.filters");
		delete("suppressions.xml");

		delete("suppressions.xml");
		delete("importation");
		delete("agent");
		delete("client");

		delete("app/build");
		delete("app/WebContent/WEB-INF/classes");
		delete("app/WebContent/WEB-INF/default-solr-cores");
		delete("app/constellio.log");
		delete("app/rules.checkstyle");
		delete("app/app.iml");
		delete("app/src/rebel.xml");
		delete("app/src/main/webapp/VAADIN/gwt-unitCache");
		delete("model/build");
		delete("model/bin");
		delete("model/rules.checkstyle");
		delete("model/model.iml");
		delete("model/src/rebel.xml");

		delete("data/build");
		delete("data/bin");
		delete("data/rules.checkstyle");
		delete("data/data.iml");
		delete("data/src/rebel.xml");

		delete("sdk/turboCache");
		delete("sdk/webapp/WEB-INF/lib");
		delete("sdk/intelligid-logo.png");
		delete("sdk/benchmarks");
		delete("sdk/benckmarks");
		delete("sdk/build");
		delete("sdk/bin");
		delete("sdk/rules.checkstyle");
		delete("sdk/sdk.iml");
		delete("sdk/constellio.log");
		delete("sdk/constellio.log.1");
		delete("sdk/doc");
		delete("sdk/generatedReports");
		delete("sdk/infinitest.filters");
		delete("sdk/check.png");
		delete("sdk/uncheck.png");
		delete("sdk/snapshots");
		delete("sdk/temp-test");
		delete("sdk/sdk.properties");
		delete("sdk/src/rebel.xml");
		delete("sdk/src/rebel-remote.xml");

		delete("sdk/src/main/webapp/VAADIN/");
		delete("sdk/sdk.properties.all");
		delete("sdk/sdk.properties.cloud");
		delete("sdk/sdk.properties.fast");
		delete("sdk/sdk.properties.jenkins");
		delete("sdk/sdk.properties.load");
		delete("sdk/sdk.properties.sonar");
		move("sdk/sdk.properties.opensource", "sdk/sdk.properties");

		delete("conf/constellio.properties");

		delete(".git");
		delete(".gradle");
		delete(".idea");
		delete(".settings");
		delete("model/.settings");
		delete("app/.settings");
		delete("data/.settings");
		delete("sdk/.settings");

		FileUtils.copyDirectory(backupGit, openSourceGitFolder);
	}

	private static void delete(String path)
			throws IOException {
		File file = new File(PUBLIC_PROJECT_DIR, path.replace("/", File.separator));
		if (file.isDirectory()) {
			FileUtils.deleteDirectory(file);
		} else {
			file.delete();
		}
	}

	private static void move(String path, String newPath)
			throws IOException {
		File file = new File(PUBLIC_PROJECT_DIR, path.replace("/", File.separator));
		File newFile = new File(PUBLIC_PROJECT_DIR, newPath.replace("/", File.separator));
		if (file.isDirectory()) {
			FileUtils.moveDirectory(file, newFile);

		} else {
			FileUtils.moveFile(file, newFile);
		}
	}

}
