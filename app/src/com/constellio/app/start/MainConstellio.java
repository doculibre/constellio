package com.constellio.app.start;

import static com.constellio.app.services.appManagement.AppManagementService.RESTART_COMMAND;
import static com.constellio.app.services.extensions.plugins.JSPFPluginServices.NEW_JAR_EXTENSION;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.services.appManagement.InstallationService;

public final class MainConstellio {

	private MainConstellio() {

	}

	public static void main(String[] args)
			throws IOException, InterruptedException, ZipServiceException {
		File constellioInstallationDir = new FoldersLocator().getWrapperInstallationFolder();

		FileService fileService = new FileService(null);

		try {
			fileService.ensureWritePermissions(constellioInstallationDir);
		} catch (IOException e) {
			throw new MainConstellioRuntimeException("No write permissions in installation dir '" + constellioInstallationDir
					+ "'", e);
		}

		if (isInInstallationMode()) {
			installApplication(constellioInstallationDir);
		} else {
			Set<String> pluginsToUpdate = getPluginsToUpdate();
			if (pluginsToUpdate.isEmpty()) {
				runApplication();
			} else {
				updatePluginsAndRestartApplication(pluginsToUpdate);
			}
		}
	}

	private static void updatePluginsAndRestartApplication(Set<String> pluginsToUpdate) {
		try{
			for (String pluginName : pluginsToUpdate) {
				movePluginToLib(pluginName);
			}
			setNoPluginToUpdate();
		}catch(IOException e){
			throw new MainConstellioRuntimeException("Error when handling plugins jars, please correct them manually", e);
		}
		restartApplication();
	}

	private static void restartApplication() {
		File commandFile = new FoldersLocator().getWrapperCommandFile();
		try {
			FileUtils.writeStringToFile(commandFile, RESTART_COMMAND, false);
		} catch (IOException e) {
			throw new MainConstellioRuntimeException("Cannot write in command file " + commandFile.getAbsolutePath(), e);
		}
	}

	private static void setNoPluginToUpdate()
			throws IOException {
		File pluginsToMoveFile = new FoldersLocator().getPluginsToMoveOnStartupFile();
		FileUtils.writeStringToFile(pluginsToMoveFile, "", false);
	}

	private static void movePluginToLib(String pluginName)
			throws IOException {
		//priority to new jars
		FoldersLocator folderLocator = new FoldersLocator();
		File jarsFolder = folderLocator.getPluginsJarsFolder();
		File jarFile = new File(jarsFolder, pluginName + "." + NEW_JAR_EXTENSION);
		if(!jarFile.exists()){
			jarFile = new File(jarsFolder, pluginName + ".jar");
		}
		File jarInLibs = new File(folderLocator.getLibFolder(), pluginName + ".jar");
		FileUtils.copyFile(jarFile, jarInLibs);
	}

	private static Set<String> getPluginsToUpdate()
			throws IOException {
		Set<String> returnSet = new HashSet<>();
		FoldersLocator folderLocator = new FoldersLocator();
		File pluginsToUpdate = folderLocator.getPluginsToMoveOnStartupFile();
		if (!pluginsToUpdate.exists()) {
			fillFileWithAllPlugins(new FoldersLocator().getPluginsJarsFolder(), pluginsToUpdate);
		}
		for(String pluginName : FileUtils.readLines(pluginsToUpdate)){
			if(StringUtils.isNotBlank(pluginName)){
				returnSet.add(pluginName);
			}
		}
		return returnSet;
	}

	public static void fillFileWithAllPlugins(File pluginsDirectory, File pluginsToUpdate)
			throws IOException {
		//should be a set since we may have jar and .jar.new for the same plugin
		Set<String> pluginsNames = new HashSet<>();
		for (File newJarVersionFile : FileUtils.listFiles(pluginsDirectory, new String[] { "jar" }, false)) {
			String pluginName = StringUtils.substringBeforeLast(newJarVersionFile.getName(), "." + "jar");
			pluginsNames.add(pluginName);
		}
		for (File newJarVersionFile : FileUtils.listFiles(pluginsDirectory, new String[] { NEW_JAR_EXTENSION }, false)) {
			String pluginName = StringUtils.substringBeforeLast(newJarVersionFile.getName(), "." + NEW_JAR_EXTENSION);
			pluginsNames.add(pluginName);
		}
		FileUtils.writeLines(pluginsToUpdate, pluginsNames);
	}

	private static boolean isInInstallationMode() {
		File configFile = new FoldersLocator().getWrapperConf();
		return configFile.exists() && configFile.length() == 0;
	}

	private static void installApplication(File constellioInstallationDir)
			throws IOException, InterruptedException, ZipServiceException {

		InstallationService installationService = new InstallationService(constellioInstallationDir);
		installationService.launchInstallation();
	}

	private static void runApplication()
			throws IOException {

		Map<String, String> properties = readProperties();

		ApplicationStarterParams params = new ApplicationStarterParams();
		params.setJoinServerThread(true);
		params.setWebContentDir(new FoldersLocator().getConstellioWebappFolder());

		String keyStorePassword = properties.get("server.keystorePassword");
		if (StringUtils.isNotBlank(keyStorePassword)) {
			params.setSSLWithKeystorePassword(keyStorePassword);
		}

		String serverPortConfig = properties.get("server.port");
		params.setPort(StringUtils.isNotBlank(serverPortConfig) ? Integer.valueOf(serverPortConfig) : 8080);

		ApplicationStarter.startApplication(params);
	}

	private static Map<String, String> readProperties() {
		return PropertyFileUtils.loadKeyValues(new FoldersLocator().getConstellioProperties());
	}

}