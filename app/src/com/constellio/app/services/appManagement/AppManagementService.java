package com.constellio.app.services.appManagement;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.services.appManagement.AppManagementServiceException.CannotSaveOldPlugins;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.AppManagementServiceRuntimeException_SameVersionsInDifferentFolders;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.CannotConnectToServer;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.WarFileNotFound;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.WarFileVersionMustBeHigher;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.app.services.extensions.plugins.InvalidPluginJarException;
import com.constellio.app.services.extensions.plugins.JSPFPluginServices;
import com.constellio.app.services.extensions.plugins.PluginServices;
import com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginInfo;
import com.constellio.app.services.extensions.plugins.utils.PluginManagementUtils;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.migrations.VersionValidator;
import com.constellio.app.services.migrations.VersionsComparator;
import com.constellio.app.services.recovery.ConstellioVersionInfo;
import com.constellio.app.services.recovery.UpgradeAppRecoveryService;
import com.constellio.app.services.systemSetup.SystemGlobalConfigsManager;
import com.constellio.app.services.systemSetup.SystemLocalConfigsManager;
import com.constellio.app.servlet.ConstellioMonitoringServlet;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.conf.FoldersLocatorMode;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.services.tenant.TenantProperties;
import com.constellio.data.services.tenant.TenantService;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.data.utils.TenantUtils;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.ENABLED;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.INVALID;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.READY_TO_INSTALL;

public class AppManagementService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppManagementService.class);

	static final String TEMP_DEPLOY_FOLDER = "AppManagementService-TempDeployFolder";
	static final String WRITE_WAR_FILE_STREAM = "AppManagementService-WriteWarFile";
	public static final String UPDATE_COMMAND = "UPDATE";
	public static final String RESTART_COMMAND = "RESTART";
	public static final String DUMP_COMMAND = "DUMP";
	//public static final String URL_CHANGELOG = "http://update.constellio.com/changelog5_1";
	//public static final String URL_WAR = "http://update.constellio.com/constellio5_1.war";
	private static String SERVER_URL = "http://updatecenter.constellio.com:8080";
	private static final int MAX_VERSION_TO_KEEP = 4;

	private final PluginServices pluginServices;
	private final ConstellioPluginManager pluginManager;
	private final SystemGlobalConfigsManager systemGlobalConfigsManager;
	private final SystemLocalConfigsManager systemLocalConfigsManager;
	private final FileService fileService;
	private final ZipService zipService;
	private final IOServices ioServices;
	private final FoldersLocator foldersLocator;
	protected final ConstellioEIMConfigs eimConfigs;
	private final UpgradeAppRecoveryService upgradeAppRecoveryService;

	public AppManagementService(AppLayerFactory appLayerFactory, FoldersLocator foldersLocator) {

		this.systemGlobalConfigsManager = appLayerFactory.getSystemGlobalConfigsManager();
		this.systemLocalConfigsManager = appLayerFactory.getSystemLocalConfigsManager();
		this.pluginManager = appLayerFactory.getPluginManager();
		this.fileService = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newFileService();
		this.zipService = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newZipService();
		this.foldersLocator = foldersLocator;
		this.ioServices = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newIOServices();
		this.eimConfigs = new ConstellioEIMConfigs(appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager());
		this.upgradeAppRecoveryService = appLayerFactory.newUpgradeAppRecoveryService();
		this.pluginServices = new JSPFPluginServices(ioServices);
	}

	public void restart()
			throws AppManagementServiceException {
		File commandFile = foldersLocator.getWrapperCommandFile();
		LOGGER.info("Sending command '" + RESTART_COMMAND + "' to wrapper command file '" + commandFile.getAbsolutePath() + "'");
		try {
			writeCommand(commandFile, RESTART_COMMAND);
		} catch (IOException e) {
			throw new AppManagementServiceException.CannotWriteInCommandFile(commandFile, e);
		}

		ConstellioMonitoringServlet.systemRestarting = true;
	}

	public void restartTenant() {
		ConstellioFactories.clear();
		ConstellioFactories.getInstance();

		ConstellioMonitoringServlet.tenantRestarting.set(true);
	}

	public void dump()
			throws AppManagementServiceException {
		File commandFile = foldersLocator.getWrapperCommandFile();
		LOGGER.info("Sending command '" + DUMP_COMMAND + "' to wrapper command file '" + commandFile.getAbsolutePath() + "'");
		try {
			writeCommand(commandFile, DUMP_COMMAND);
		} catch (IOException e) {
			throw new AppManagementServiceException.CannotWriteInCommandFile(commandFile, e);
		}
	}

	public void update(ProgressInfo progressInfo)
			throws AppManagementServiceException {
		ConstellioVersionInfo currentInstalledVersionInfo = getCurrentInstalledVersionInfo();

		File warFile = foldersLocator.getUploadConstellioWarFile();
		File tempFolder = fileService.newTemporaryFolder(TEMP_DEPLOY_FOLDER);

		if (!warFile.exists()) {
			throw new WarFileNotFound();
		}

		String task = "Updating web application using war '" + warFile.getAbsolutePath() + "' with size " + warFile.length();
		progressInfo.reset();
		progressInfo.setEnd(1);
		progressInfo.setTask(task);
		LOGGER.info(task);

		try {
			String currentStep = "Unzipping war in temp folder '" + tempFolder + "'";
			progressInfo.setProgressMessage(currentStep);
			LOGGER.info(currentStep);
			try {
				zipService.unzip(warFile, tempFolder);
			} catch (ZipServiceException e) {
				throw new RuntimeException(e);
			}
			String warVersion = findWarVersion(tempFolder);
			String currentWarVersion = GetWarVersionUtils.getWarVersionUsingGradleAsFallback(null);

			currentStep = "Based on jar file, the version of the new war is '" + warVersion + "', current version is '"
						  + currentWarVersion + "'";
			progressInfo.setProgressMessage(currentStep);
			LOGGER.info(currentStep);
			if (VersionsComparator.isFirstVersionBeforeSecond(warVersion, currentWarVersion)
				&& !Toggle.DANGER_DANGER_DANGER___ALLOW_UPDATE_TO_OLDER_VERSION___DANGER_DANGER_DANGER.isEnabled()) {
				LOGGER.warn("Trying to install lower version " + warVersion + "\n\tCurrent version is " + currentWarVersion);
				throw new WarFileVersionMustBeHigher();
			}

			currentStep = "Saving existing plugins to new version directory";
			progressInfo.setProgressMessage(currentStep);
			LOGGER.info(currentStep);

			File oldPluginsFolder = foldersLocator.getPluginsJarsFolder();
			copyCurrentLibsIfPatchWar(foldersLocator.getConstellioWebappFolder(), tempFolder);
			copyCurrentPlugins(oldPluginsFolder, tempFolder);
			movePluginsToNewLib(oldPluginsFolder, tempFolder);
			updatePluginsWithThoseInWar(tempFolder);

			currentStep = "Selecting SMB Library";
			selectSmbLibrary(tempFolder);
			LOGGER.info(currentStep);

			File currentAppFolder = foldersLocator.getConstellioWebappFolder().getAbsoluteFile();
			File deployFolder = findDeployFolder(currentAppFolder.getParentFile(), warVersion);

			currentStep = "Moving new webapp version in '" + deployFolder + "'";
			progressInfo.setProgressMessage(currentStep);
			LOGGER.info(currentStep);
			tempFolder.renameTo(deployFolder);

			currentStep = "Deleting war file";
			progressInfo.setProgressMessage(currentStep);
			LOGGER.info(currentStep);
			warFile.delete();

			if (eimConfigs.isCleanDuringInstall()) {
				currentStep = "Deleting older versions";
				progressInfo.setProgressMessage(currentStep);
				LOGGER.info(currentStep);
				keepOnlyLastFiveVersionsAndAtLeastOneVersionModifiedBeforeLastWeek(currentAppFolder.getParentFile(),
						deployFolder);
			}

			currentStep = "Updating wrapper conf to boot on new version";
			progressInfo.setProgressMessage(currentStep);
			LOGGER.info(currentStep);
			updateWrapperConf(deployFolder);
			upgradeAppRecoveryService.afterWarUpload(currentInstalledVersionInfo,
					new ConstellioVersionInfo(warVersion, deployFolder.getAbsolutePath()));
		} catch (AppManagementServiceException e) {
			//FIXME delete deployFolder if created and revert to previous wrapper conf then throw exception
			throw e;
		} finally {
			fileService.deleteQuietly(tempFolder);
		}
		progressInfo.setCurrentState(1);

	}

	private void copyCurrentLibsIfPatchWar(File constellioWebappFolder, File tempFolder) {
		File newAppLibs = new File(new File(tempFolder, "WEB-INF"), "lib");
		File currentAppLibs = new File(new File(constellioWebappFolder, "WEB-INF"), "lib");

		if (newAppLibs.listFiles() != null && currentAppLibs.listFiles() != null && newAppLibs.listFiles().length < 10) {
			for (File currentJarFile : currentAppLibs.listFiles()) {
				if (!currentJarFile.getName().startsWith("core-")
					&& !currentJarFile.getName().startsWith("plugin")) {
					try {
						FileUtils.copyFile(currentJarFile, new File(newAppLibs, currentJarFile.getName()));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}

	}

	private void updatePluginsWithThoseInWar(File nextWebapp) {

		if (TenantUtils.isSupportingTenants()) {
			String currentTenant = TenantUtils.getTenantId();
			try {
				for (TenantProperties tenantProperties : TenantService.getInstance().getTenants()) {
					ConstellioPluginManager pluginManager = getConstellioPluginManagerForTenant(tenantProperties.getId());
					updatePluginsOfCurrentTenant(nextWebapp, pluginManager, pluginServices, currentTenant);
					installPluginsForCurrentTenant(nextWebapp, pluginManager);
				}
			} finally {
				TenantUtils.setTenant(currentTenant);
			}
		} else {
			updatePluginsOfCurrentTenant(nextWebapp, pluginManager, pluginServices, "main");
			installPluginsForCurrentTenant(nextWebapp, pluginManager);
		}
	}

	ConstellioPluginManager getConstellioPluginManagerForTenant(byte tenantId) {
		TenantUtils.setTenant(tenantId);
		return ConstellioFactories.getInstance().getAppLayerFactory().getPluginManager();
	}

	private static void installPluginsForCurrentTenant(File nextWebapp, ConstellioPluginManager pluginManager) {
		File pluginsFolder = new File(nextWebapp, "plugins-to-install");
		if (pluginsFolder.exists() && pluginsFolder.listFiles() != null) {
			for (File pluginFile : pluginsFolder.listFiles()) {
				if (pluginFile.getName().toLowerCase().endsWith(".jar")) {
					LOGGER.info(pluginsFolder.getName() + "/" + pluginFile.getName() + ".jar : installed");
					pluginManager.prepareInstallablePluginInNextWebapp(pluginFile, nextWebapp);
				}
			}
		}
	}

	private static void updatePluginsOfCurrentTenant(File nextWebapp, ConstellioPluginManager pluginManager,
													 PluginServices pluginServices, String tenantId) {
		File pluginsFolder = new File(nextWebapp, "plugins-to-update");
		if (pluginsFolder.exists() && pluginsFolder.listFiles() != null) {
			Set<String> alreadyInstalledPluginsForTenant = pluginManager.getPlugins(ENABLED, READY_TO_INSTALL, INVALID)
					.stream().map(ConstellioPluginInfo::getCode).collect(Collectors.toSet());
			List<String> alreadyInstalledPluginsForAnyTenant = pluginManager.getPluginsFromAnyTenants(ENABLED, READY_TO_INSTALL, INVALID);


			for (File pluginFile : pluginsFolder.listFiles()) {
				if (pluginFile.getName().toLowerCase().endsWith(".jar")) {
					try {
						ConstellioPluginInfo info = pluginServices.extractPluginInfo(pluginFile);


						if (alreadyInstalledPluginsForTenant.contains(info.getCode())) {
							LOGGER.info(pluginsFolder.getName() + "/" + pluginFile.getName() + ".jar : installed for tenant " + tenantId);
							pluginManager.prepareInstallablePluginInNextWebapp(pluginFile, nextWebapp);
						} else if (!alreadyInstalledPluginsForAnyTenant.contains(info.getCode())) {
							LOGGER.info(pluginsFolder.getName() + "/" + pluginFile.getName() + ".jar : deleted");
							pluginFile.delete();
						}

					} catch (InvalidPluginJarException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	private void movePluginsToNewLib(File oldPluginsFolder, File newWebAppFolder)
			throws CannotSaveOldPlugins {
		File newLibsFolder = foldersLocator.getLibFolder(newWebAppFolder);
		LOGGER.info("plugins : copy to lib " + newLibsFolder.getPath());
		File pluginsToMoveFile = foldersLocator.getPluginsToMoveOnStartupFile(newWebAppFolder);
		PluginManagementUtils utils = new PluginManagementUtils(oldPluginsFolder, newLibsFolder, pluginsToMoveFile);

		try {
			utils.movePlugins(pluginManager.getPluginsOfEveryStatusFromAnyTenants());
		} catch (IOException e) {
			throw new CannotSaveOldPlugins(e);
		}
	}

	private void copyCurrentPlugins(File oldPluginsFolder, File newWebAppFolder)
			throws CannotSaveOldPlugins {
		if (oldPluginsFolder.exists()) {
			try {
				LOGGER.info("plugins : copy to " + newWebAppFolder);
				LOGGER.info("plugins : copy from ",
						oldPluginsFolder.getPath() + "to " + foldersLocator.getPluginsJarsFolder(newWebAppFolder).getPath());
				FileUtils.copyDirectory(oldPluginsFolder, foldersLocator.getPluginsJarsFolder(newWebAppFolder));
			} catch (IOException e) {
				throw new CannotSaveOldPlugins(e);
			}
		}
	}

	private void keepOnlyLastFiveVersionsAndAtLeastOneVersionModifiedBeforeLastWeek(File webAppsFolder,
																					File deployFolder) {
		Map<String, File> existingWebAppsMappedByVersion = getExistingVersionsFoldersMappedByVersion(webAppsFolder, deployFolder);

		if (existingWebAppsMappedByVersion.size() > MAX_VERSION_TO_KEEP) {
			Set<String> orderedVersions = new TreeSet<>(new VersionsComparator());
			orderedVersions.addAll(existingWebAppsMappedByVersion.keySet());
			int versionsToRemoveCount = existingWebAppsMappedByVersion.size() - MAX_VERSION_TO_KEEP;
			Iterator<String> versionsIterator = orderedVersions.iterator();
			int handledVersions = 0;
			boolean atLeastOneVersionBeforeLastWeek = false;
			while (versionsIterator.hasNext()) {
				String version = versionsIterator.next();
				handledVersions++;
				if (handledVersions > versionsToRemoveCount) {
					File webAppToRemove = existingWebAppsMappedByVersion.get(version);
					existingWebAppsMappedByVersion.remove(version);
					if (isModifiedBeforeLastWeek(webAppToRemove)) {
						atLeastOneVersionBeforeLastWeek = true;
					}
				}
			}
			if (!atLeastOneVersionBeforeLastWeek) {
				removeAllFilesAndKeepTheNewestOneBeforeLastWeek(existingWebAppsMappedByVersion.values());
			} else {
				removeAllFiles(existingWebAppsMappedByVersion.values());
			}
		}
	}

	private void removeAllFilesAndKeepTheNewestOneBeforeLastWeek(Collection<File> files) {
		List<File> filesList = new ArrayList<>(files);
		Collections.sort(filesList, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				LocalDate modificationDate1 = new LocalDate(o1.lastModified());
				LocalDate modificationDate2 = new LocalDate(o1.lastModified());
				return modificationDate1.compareTo(modificationDate2);
			}
		});
		for (int i = 0; i < filesList.size() - 1; i++) {
			File file = filesList.get(i);
			FileUtils.deleteQuietly(file);
		}
		File lastFile = filesList.get(filesList.size() - 1);
		if (!isModifiedBeforeLastWeek(lastFile)) {
			FileUtils.deleteQuietly(lastFile);
		}
	}

	private void removeAllFiles(Collection<File> files) {
		for (File file : files) {
			FileUtils.deleteQuietly(file);
		}
	}

	boolean isModifiedBeforeLastWeek(File webAppsFolder) {
		return new LocalDate(webAppsFolder.lastModified()).isBefore(TimeProvider.getLocalDate().minusDays(7));
	}

	private Map<String, File> getExistingVersionsFoldersMappedByVersion(File webAppsFolder, File deployFolder) {
		Map<String, File> existingWebAppsMappedByVersion = new HashMap<>();
		for (File webApp : webAppsFolder.listFiles(new WebAppFileNameFilter())) {
			String version = GetWarVersionUtils.getWarVersionFromFileName(webApp);
			if (VersionValidator.isValidVersion(version) && !webApp.getName().equals(deployFolder.getName())) {
				File associatedWebAppFolder = existingWebAppsMappedByVersion.get(version);
				if (associatedWebAppFolder != null) {
					throw new AppManagementServiceRuntimeException_SameVersionsInDifferentFolders(version, webApp.getName(),
							associatedWebAppFolder.getName());
				} else {
					existingWebAppsMappedByVersion.put(version, webApp);
				}
			}
		}
		return existingWebAppsMappedByVersion;
	}

	private void updateWrapperConf(File deployFolder) {

		LOGGER.info("New webapp path is '" + deployFolder.getAbsolutePath() + "'");
		File wrapperConf = foldersLocator.getWrapperConf();
		if (foldersLocator.getFoldersLocatorMode().equals(FoldersLocatorMode.PROJECT) && !wrapperConf.exists()) {
			return;
		}
		List<String> lines = fileService.readFileToLinesWithoutExpectableIOException(wrapperConf);
		for (int i = 0; i < lines.size(); i++) {

			String line = lines.get(i);
			if (line.startsWith("wrapper.java.classpath.2=")) {
				lines.set(i, "wrapper.java.classpath.2=" + deployFolder.getAbsolutePath() + "/WEB-INF/lib/*.jar");
			}
			if (line.startsWith("wrapper.java.classpath.3=")) {
				lines.set(i, "wrapper.java.classpath.3=" + deployFolder.getAbsolutePath() + "/WEB-INF/classes");
			}
			if (line.startsWith("wrapper.commandfile=")) {
				lines.set(i, "wrapper.commandfile=" + deployFolder.getAbsolutePath() + "/WEB-INF/command/cmd");
			}
		}
		fileService.writeLinesToFile(wrapperConf, lines);
	}

	File findDeployFolder(File parent, String version) {
		File deployFolder = null;
		String mostRecentVersion = "";
		for (File currentWebApp : parent.listFiles(new WebAppWithValidSubversionFilenameFilter(version))) {
			String currentVersion = StringUtils.substringAfter(currentWebApp.getName(), "webapp-");
			if (mostRecentVersion.isEmpty()) {
				deployFolder = currentWebApp;
				mostRecentVersion = currentVersion;
			} else {
				if (VersionsComparator.isFirstVersionBeforeSecond(mostRecentVersion, currentVersion)) {
					deployFolder = currentWebApp;
					mostRecentVersion = currentVersion;
				}
			}
		}

		int nextSubVersion;
		if (mostRecentVersion.isEmpty()) {
			deployFolder = new File(parent, "webapp-" + version);
			nextSubVersion = 0;
		} else if (mostRecentVersion.contains("-")) {
			nextSubVersion = Integer.valueOf(mostRecentVersion.split("-")[1]);
		} else {
			nextSubVersion = 0;
		}
		nextSubVersion++;

		while (deployFolder.exists()) {
			deployFolder = new File(parent, "webapp-" + version + "-" + nextSubVersion);
			nextSubVersion++;
		}
		return deployFolder;
	}

	private String findWarVersion(File tempFolder) {
		File webInf = new File(tempFolder, "WEB-INF");
		File libs = new File(webInf, "lib");
		if (libs.listFiles() != null) {
			for (File lib : libs.listFiles()) {
				if (lib.getName().startsWith("core-model-")) {
					return lib.getName().replace("core-model-", "").replace(".jar", "");
				}
			}
		}
		throw new RuntimeException("Cannot recover war version in " + libs.getAbsolutePath());
	}

	private void writeCommand(File file, String command)
			throws IOException {
		fileService.replaceFileContent(file, command);
	}

	public boolean isWarFileUploaded() {
		File warFile = foldersLocator.getUploadConstellioWarFile();
		return warFile.exists();
	}

	public StreamFactory<OutputStream> getWarFileDestination() {
		File warFile = foldersLocator.getUploadConstellioWarFile();
		return ioServices.newOutputStreamFactory(warFile, WRITE_WAR_FILE_STREAM);
	}

	public StreamFactory<OutputStream> getLastAlertFileDestination() {
		File lastAlertFile = foldersLocator.getLastAlertFile();
		return ioServices.newOutputStreamFactory(lastAlertFile, "alert");
	}

	public String getWarVersion() {
		return GetWarVersionUtils.getWarVersion(null);
	}

	public String getChangelogURLFromServer()
			throws AppManagementServiceRuntimeException.CannotConnectToServer {
		String serverUrl = SERVER_URL + "/changelog/";
		String changelogURL;
		try {
			changelogURL = sendPost(serverUrl, getInfosToSend());
		} catch (IOException ioe) {
			throw new AppManagementServiceRuntimeException.CannotConnectToServer(serverUrl);
		}

		return changelogURL;
	}

	public String getWarURLFromServer()
			throws AppManagementServiceRuntimeException.CannotConnectToServer {
		String serverUrl = SERVER_URL + "/url/";
		String warURL;
		try {
			warURL = sendPost(serverUrl, getInfosToSend());
		} catch (IOException ioe) {
			throw new AppManagementServiceRuntimeException.CannotConnectToServer(serverUrl);
		}

		return warURL;
	}

	public String getLastAlertURLFromServer() throws CannotConnectToServer {
		String serverUrl = SERVER_URL + "/alert/";
		String lastAlertURL;
		try {
			lastAlertURL = sendPost(serverUrl, getInfosToSend());
		} catch (IOException e) {
			throw new AppManagementServiceRuntimeException.CannotConnectToServer(serverUrl);
		}

		return lastAlertURL;
	}

	String getInfosToSend() {
		String delimiter = "*";
		StringBuilder sb = new StringBuilder();
		LicenseInfo licenseInfo = getLicenseInfo();
		if (licenseInfo != null) {
			sb.append(licenseInfo.getSignature());
		} else {
			sb.append("No license");
		}

		sb.append(delimiter);

		ConstellioVersionInfo versionInfo = getCurrentInstalledVersionInfo();
		if (versionInfo != null) {
			sb.append(versionInfo.getVersion());
		} else {
			sb.append("Unknown version");
		}
		return sb.toString();
	}

	String sendPost(String url, String infoSent)
			throws IOException {
		StringBuilder response = new StringBuilder();
		try (BufferedReader in = new BufferedReader(new InputStreamReader(getInputForPost(url, infoSent)))) {
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
		}
		return response.toString();
	}

	InputStream getInputForPost(String url, String infoSent)
			throws IOException {
		URL obj = new URL(url);

		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setConnectTimeout(10000);

		if (infoSent != null) {
			try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
				wr.writeBytes(infoSent);
				wr.flush();
			}
		}

		return con.getInputStream();
	}

	public String getChangelogFromServer()
			throws AppManagementServiceRuntimeException.CannotConnectToServer {
		String URL_CHANGELOG = getChangelogURLFromServer();

		String changelog = "";
		try (InputStream stream = getInputForPost(URL_CHANGELOG, getLicenseInfo().getSignature())) {
			if (stream == null) {
				throw new AppManagementServiceRuntimeException.CannotConnectToServer(URL_CHANGELOG);
			}

			try (BufferedReader in = new BufferedReader(new InputStreamReader(stream))) {
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					changelog += inputLine;
				}

				if (this.isProxyPage(changelog)) {
					throw new AppManagementServiceRuntimeException.CannotConnectToServer(URL_CHANGELOG);
				}
			}

		} catch (IOException | RuntimeException io) {
			throw new AppManagementServiceRuntimeException.CannotConnectToServer(URL_CHANGELOG, io);
		}

		return changelog;
	}

	public String getVersionFromServer()
			throws AppManagementServiceRuntimeException.CannotConnectToServer {
		String serverUrl = SERVER_URL + "/version/";

		try {
			serverUrl = sendPost(serverUrl, getInfosToSend());
		} catch (IOException ioe) {
			throw new AppManagementServiceRuntimeException.CannotConnectToServer(serverUrl);
		}

		return serverUrl;
	}

	boolean isProxyPage(String changelog) {
		return !changelog.contains("<version>");
	}

	public void getWarFromServer(ProgressInfo progressInfo)
			throws AppManagementServiceRuntimeException.CannotConnectToServer {
		String URL_WAR = getWarURLFromServer();

		System.out.println("URL FOR WAR => " + URL_WAR);

		try {
			progressInfo.reset();
			progressInfo.setTask("Getting WAR from server");
			progressInfo.setEnd(1);

			progressInfo.setProgressMessage("Downloading WAR");
			InputStream input = getInputForPost(URL_WAR, getLicenseInfo().getSignature());
			if (input == null) {
				throw new AppManagementServiceRuntimeException.CannotConnectToServer(URL_WAR);
			}

			progressInfo.setProgressMessage("Creating WAR file");

			try (
					CountingInputStream countingInputStream = new CountingInputStream(input);
					OutputStream warFileOutput = getWarFileDestination().create("war upload");
			) {
				progressInfo.setProgressMessage("Copying downloaded WAR");

				byte[] buffer = new byte[8 * 1024];
				int bytesRead;
				while ((bytesRead = countingInputStream.read(buffer)) != -1) {
					warFileOutput.write(buffer, 0, bytesRead);
					long totalBytesRead = countingInputStream.getByteCount();
					String progressMessage = FileUtils.byteCountToDisplaySize(totalBytesRead);
					progressInfo.setProgressMessage(progressMessage);
				}
			}
			progressInfo.setCurrentState(1);

		} catch (IOException ioe) {
			throw new AppManagementServiceRuntimeException.CannotConnectToServer(URL_WAR, ioe);
		}
	}

	@SuppressWarnings("deprecation")
	public File getLastAlertFromServer() throws CannotConnectToServer {
		String URL_LAST_ALERT = getLastAlertURLFromServer();

		InputStream lastAlertFileInput = null;
		OutputStream lastAlertFileOutput = null;
		try {
			String infoSent = getLicenseInfo() != null ? getLicenseInfo().getSignature() : null;
			lastAlertFileInput = getInputForPost(URL_LAST_ALERT, infoSent);

			if (lastAlertFileInput == null) {
				throw new AppManagementServiceRuntimeException.CannotConnectToServer(URL_LAST_ALERT);
			}

			lastAlertFileOutput = getLastAlertFileDestination().create("alert download");
			IOUtils.copy(lastAlertFileInput, lastAlertFileOutput);
		} catch (IOException | RuntimeException io) {
			throw new AppManagementServiceRuntimeException.CannotConnectToServer(URL_LAST_ALERT, io);
		} finally {
			IOUtils.closeQuietly(lastAlertFileInput);
			IOUtils.closeQuietly(lastAlertFileOutput);
		}

		return foldersLocator.getLastAlertFile();
	}

	public String getWebappFolderName() {
		return foldersLocator.getConstellioWebappFolder().getName();
	}

	public void markForReindexing() {
		systemLocalConfigsManager.setMarkedForReindexing(true);
	}

	public void markCacheForRebuildIfRequired() {
		if (systemLocalConfigsManager.isCacheRebuildRequired()) {
			systemLocalConfigsManager.setMarkedForCacheRebuild(true);
		}
	}

	public void markCacheForRebuild() {
		systemLocalConfigsManager.setMarkedForCacheRebuild(true);
	}

	public boolean isLicensedForAutomaticUpdate() {
		return getLicenseInfo() != null;
	}

	public void storeLicense(File uploadLicenseFile) {
		File licenseFile = foldersLocator.getLicenseFile();
		ioServices.copyFileWithoutExpectableIOException(uploadLicenseFile, licenseFile);
	}

	public LicenseInfo getLicenseInfo() {
		LicenseInfo license = null;
		try {
			String licenseString = ioServices.readFileToString(foldersLocator.getLicenseFile());
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(new StringReader(licenseString));

			String name = document.getRootElement().getChild("name").getContent().get(0).getValue();
			LocalDate date = new LocalDate(document.getRootElement().getChild("date").getContent().get(0).getValue());
			String signature = document.getRootElement().getChild("signature").getContent().get(0).getValue();

			license = new LicenseInfo(name, date, signature);
		} catch (IOException ioe) {
		} catch (JDOMException joe) {
		}

		return license;
	}

	private ConstellioVersionInfo getCurrentInstalledVersionInfo() {
		File versionDirectory = foldersLocator.getConstellioWebappFolder();
		String version = GetWarVersionUtils.getWarVersion(versionDirectory);
		return new ConstellioVersionInfo(version, versionDirectory.getAbsolutePath());
	}

	public void pointToVersionDuringApplicationStartup(ConstellioVersionInfo constellioVersionInfo) {
		updateWrapperConf(new File(constellioVersionInfo.getVersionDirectoryPath()));
	}

	private void selectSmbLibrary(File newWebAppFolder) {
		Map<String, String> configs = PropertyFileUtils.loadKeyValues(foldersLocator.getConstellioProperties());
		if ("true".equals(configs.get("smb.novell"))) {
			LOGGER.info("Replacing SMB library for Novell");
			File libFolder = foldersLocator.getLibFolder(newWebAppFolder);
			try {
				File novellSmbLib = new File(libFolder, "jcifs_novell.jar.disabled");
				LOGGER.info("Enabling : " + novellSmbLib);
				if (novellSmbLib.exists()) {
					FileUtils.moveFile(novellSmbLib, new File(libFolder, "jcifs_novell.jar"));
				}
				File defaultSmbLib = new File(libFolder, "jcifs_gcm-322.jar");
				LOGGER.info("Disabling : " + defaultSmbLib);
				if (defaultSmbLib.exists()) {
					FileUtils.moveFile(defaultSmbLib, new File(libFolder, "jcifs_gcm-322.jar.disabled"));
				}
			} catch (IOException ioe) {
				LOGGER.error("Could not install Novell/SMB libraries in the lib folder.", ioe);
			}
		}
	}

	private class WebAppFileNameFilter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String name) {
			return name.startsWith("webapp-");
		}
	}

	private class WebAppWithValidSubversionFilenameFilter implements FilenameFilter {
		final String version;

		public WebAppWithValidSubversionFilenameFilter(
				String version) {
			this.version = version;
		}

		@Override
		public boolean accept(File dir, String name) {
			if (name.equals("webapp-" + version)) {
				return true;
			}
			if (name.startsWith("webapp-" + version + "-")) {
				String currentVersion = StringUtils.substringAfter(name, "webapp-");
				if (VersionValidator.isValidVersion(currentVersion)) {
					return true;

				}
			}
			return false;
		}
	}

	public static class LicenseInfo implements Serializable {
		private final String clientName;
		private final LocalDate expirationDate;
		private final String signature;

		public LicenseInfo(String clientName, LocalDate expirationDate, String signature) {
			this.clientName = clientName;
			this.expirationDate = expirationDate;
			this.signature = signature;
		}

		public String getClientName() {
			return clientName;
		}

		public LocalDate getExpirationDate() {
			return expirationDate;
		}

		public String getSignature() {
			return signature;
		}
	}

}
