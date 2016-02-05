package com.constellio.app.services.extensions.plugins;

import static com.constellio.app.services.extensions.plugins.JSPFConstellioPluginManager.PREVIOUS_PLUGINS;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class InvalidJarsTest {
	static String text1 = "I am not a jar",
			text2 = "I am not a jar 2",
			text3 = "I am not a jar 3";

	public static void assertThatJarsLoadedCorrectly(File exportedPluginsFolder)
			throws IOException {
		File text1File = FileUtils.getFile(exportedPluginsFolder.getPath() + "/jar1.jar");
		assertThat(text1File).isNotNull();
		assertThat(FileUtils.readLines(text1File)).containsOnly(text1);

		File text2File = FileUtils.getFile(exportedPluginsFolder.getPath() +  "/jar2.jar.new");
		assertThat(text2File).isNotNull();
		assertThat(FileUtils.readLines(text2File)).containsOnly(text2);

		File text3File = FileUtils.getFile(exportedPluginsFolder.getPath() +  "/" + PREVIOUS_PLUGINS + "/jar3.jar");
		assertThat(text3File).isNotNull();
		assertThat(FileUtils.readLines(text3File)).containsOnly(text3);
	}

	public static void loadJarsToPluginsFolder(File pluginsFolder)
			throws IOException {
		InputStream text1InputStream = null, text2InputStream = null, text3InputStream = null;
		try {
			FileUtils.copyInputStreamToFile(text1InputStream = new ByteArrayInputStream(text1.getBytes()),
					new File(pluginsFolder, "jar1.jar"));
			FileUtils.copyInputStreamToFile(text2InputStream = new ByteArrayInputStream(text2.getBytes()),
					new File(pluginsFolder, "jar2.jar.new"));
			FileUtils.copyInputStreamToFile(text3InputStream = new ByteArrayInputStream(text3.getBytes()),
					new File(pluginsFolder + "/" + PREVIOUS_PLUGINS, "jar3.jar"));
		} finally {
			IOUtils.closeQuietly(text1InputStream);
			IOUtils.closeQuietly(text2InputStream);
			IOUtils.closeQuietly(text3InputStream);
		}
	}
}
