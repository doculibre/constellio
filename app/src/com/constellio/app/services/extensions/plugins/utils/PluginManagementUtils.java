package com.constellio.app.services.extensions.plugins.utils;

import com.constellio.data.conf.FoldersLocator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.app.services.extensions.plugins.JSPFPluginServices.NEW_JAR_EXTENSION;
import static java.util.Arrays.asList;

public class PluginManagementUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginManagementUtils.class);
	private final File pluginsFolder, libFolder, pluginsToMoveToLibFile;

	public PluginManagementUtils(File pluginsFolder, File libFolder, File pluginsToMoveToLibFile) {
		this.pluginsFolder = pluginsFolder;
		this.libFolder = libFolder;
		this.pluginsToMoveToLibFile = pluginsToMoveToLibFile;
	}

	public PluginManagementUtils(FoldersLocator foldersLocator) {
		this.pluginsFolder = foldersLocator.getPluginsJarsFolder();
		this.libFolder = foldersLocator.getLibFolder();
		this.pluginsToMoveToLibFile = foldersLocator.getPluginsToMoveOnStartupFile();
	}

	public Set<String> getPluginsToMove()
			throws IOException {
		Set<String> returnSet = new HashSet<>();
		if (!pluginsToMoveToLibFile.exists()) {
			fillFileWithAllPlugins();
		}
		for (String pluginName : FileUtils.readLines(pluginsToMoveToLibFile)) {
			if (StringUtils.isNotBlank(pluginName)) {
				returnSet.add(pluginName);
			}
		}
		return returnSet;
	}

	public void movePlugins(Collection<String> pluginsToUpdate)
			throws IOException {
		for (String pluginName : pluginsToUpdate) {
			copyPluginFromPluginsFolderToLibFolder(pluginName);
		}
		setNoPluginToMove();
	}

	public void addPluginToMove(String plugin)
			throws IOException {
		if (!pluginsToMoveToLibFile.exists()) {
			fillFileWithAllPlugins();
		} else {
			FileUtils.writeLines(pluginsToMoveToLibFile, asList(plugin), true);
		}
	}

	void setNoPluginToMove()
			throws IOException {
		FileUtils.writeStringToFile(pluginsToMoveToLibFile, "", false);
	}

	void copyPluginFromPluginsFolderToLibFolder(String pluginName)
			throws IOException {
		//priority to new jars
		File jarFile = new File(pluginsFolder, pluginName + "." + NEW_JAR_EXTENSION);
		if (!jarFile.exists()) {
			jarFile = new File(pluginsFolder, pluginName + ".jar");
		}
		if (jarFile.exists()) {
			File jarInLibs = new File(libFolder, pluginName + ".jar");
			LOGGER.info("Moving plugin " + jarFile.getPath() + " to " + jarInLibs.getPath());
			FileUtils.copyFile(jarFile, jarInLibs);
		} else {
			LOGGER.error("Plugin file listed but not found " + jarFile.getPath());
		}
	}

	void fillFileWithAllPlugins()
			throws IOException {
		//should be a set since we may have jar and .jar.new for the same plugin
		Set<String> pluginsNames = new HashSet<>();
		for (File newJarVersionFile : FileUtils.listFiles(pluginsFolder, new String[]{"jar"}, false)) {
			String pluginName = StringUtils.substringBeforeLast(newJarVersionFile.getName(), "." + "jar");
			pluginsNames.add(pluginName);
		}
		for (File newJarVersionFile : FileUtils.listFiles(pluginsFolder, new String[]{NEW_JAR_EXTENSION}, false)) {
			String pluginName = StringUtils.substringBeforeLast(newJarVersionFile.getName(), "." + NEW_JAR_EXTENSION);
			pluginsNames.add(pluginName);
		}
		FileUtils.writeLines(pluginsToMoveToLibFile, pluginsNames, false);
	}

	public static void markNewPluginsInNewWar(File webapp, String newPluginFilename) {
		File newPlugins = new File(webapp, "new-plugins");
		try {
			FileUtils.write(newPlugins, newPluginFilename + "\n", true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void clearNewPluginsInNewWar(File webapp) {
		File newPlugins = new File(webapp, "new-plugins");
		newPlugins.delete();
	}

	public static List<String> getNewPluginsInNewWar(File webapp) {
		File newPlugins = new File(webapp, "new-plugins");
		if (newPlugins.exists()) {
			try {
				List<File> files = new ArrayList<>();

				return FileUtils.readLines(newPlugins);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return Collections.emptyList();
		}
	}

}
