package com.constellio.app.services.extensions.plugins.utils;

import static com.constellio.app.services.extensions.plugins.JSPFPluginServices.NEW_JAR_EXTENSION;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginManagementUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginManagementUtils.class);
	private final File pluginsFolder, libFolder, pluginsToMoveToLibFile;

	public PluginManagementUtils(File pluginsFolder, File libFolder, File pluginsToMoveToLibFile) {
		this.pluginsFolder = pluginsFolder;
		this.libFolder = libFolder;
		this.pluginsToMoveToLibFile = pluginsToMoveToLibFile;
	}

	public void setNoPluginToMove()
			throws IOException {
		FileUtils.writeStringToFile(pluginsToMoveToLibFile, "", false);
	}

	public void copyPluginFromPluginsFolderToLibFolder(String pluginName)
			throws IOException {
		//priority to new jars
		File jarFile = new File(pluginsFolder, pluginName + "." + NEW_JAR_EXTENSION);
		if (!jarFile.exists()) {
			jarFile = new File(pluginsFolder, pluginName + ".jar");
		}
		if (jarFile.exists()) {
			File jarInLibs = new File(libFolder, pluginName + ".jar");
			FileUtils.copyFile(jarFile, jarInLibs);
		} else {
			LOGGER.error("Plugin file listed but not found " + jarFile.getPath());
		}
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

	public void fillFileWithAllPlugins()
			throws IOException {
		//should be a set since we may have jar and .jar.new for the same plugin
		Set<String> pluginsNames = new HashSet<>();
		for (File newJarVersionFile : FileUtils.listFiles(pluginsFolder, new String[] { "jar" }, false)) {
			String pluginName = StringUtils.substringBeforeLast(newJarVersionFile.getName(), "." + "jar");
			pluginsNames.add(pluginName);
		}
		for (File newJarVersionFile : FileUtils.listFiles(pluginsFolder, new String[] { NEW_JAR_EXTENSION }, false)) {
			String pluginName = StringUtils.substringBeforeLast(newJarVersionFile.getName(), "." + NEW_JAR_EXTENSION);
			pluginsNames.add(pluginName);
		}
		FileUtils.writeLines(pluginsToMoveToLibFile, pluginsNames, false);
	}

	public void movePluginsAndSetNoPluginToMove(Collection<String> pluginsToUpdate)
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
			FileUtils.writeLines(pluginsToMoveToLibFile, Arrays.asList(plugin), true);
		}
	}
}
